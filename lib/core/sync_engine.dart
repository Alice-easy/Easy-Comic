import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:path/path.dart' as p;
import '../data/drift_db.dart';
import '../models/sync_models.dart';
import 'webdav_service.dart';

/// 同步冲突解决策略
enum ConflictResolution {
  localFirst, // 本地优先
  remoteFirst, // 远程优先
  manual, // 手动解决
}

/// 同步状态
enum SyncStatus { idle, syncing, error }

import 'package:async/async.dart';

/// 同步引擎
class SyncEngine {
  SyncEngine({
    required this.db,
    required this.webdavService,
    this.conflictResolution = ConflictResolution.localFirst,
  });

  final DriftDb db;
  final WebDAVService webdavService;

  // 同步配置
  final int _concurrencyLimit = 3;
  final ConflictResolution conflictResolution;

  // 同步状态
  SyncStatus _status = SyncStatus.idle;
  final StreamController<SyncStatus> _statusController =
      StreamController<SyncStatus>.broadcast();

  // 进度报告
  final StreamController<double> _progressController =
      StreamController<double>.broadcast();

  // Getters
  SyncStatus get status => _status;
  Stream<SyncStatus> get statusStream => _statusController.stream;
  Stream<double> get progressStream => _progressController.stream;

  /// 设置同步状态
  void _setStatus(SyncStatus status) {
    _status = status;
    _statusController.add(status);
  }

  /// 设置进度
  void _setProgress(double progress) {
    _progressController.add(progress);
  }

  /// 执行双向同步
  Future<SyncResult> sync() async {

    if (_status == SyncStatus.syncing) {
      throw StateError('Sync already in progress');
    }

    _setStatus(SyncStatus.syncing);
    _setProgress(0);

    try {
      final result = await _performSync();
      _setStatus(SyncStatus.idle);
      _setProgress(1);
      return result;
    } catch (e) {
      _setStatus(SyncStatus.error);
      rethrow;
    }
  }

  /// 执行单次同步 (sync 方法的别名)
  Future<SyncResult> runOnce() => sync();

  /// 执行实际同步逻辑
  Future<SyncResult> _performSync() async {
    final errors = <String>[];
    var uploaded = 0;
    var downloaded = 0;
    var conflicts = 0;

    try {
      // 1. 获取本地数据
      _setProgress(0.1);
      final localData = await _getLocalData();

      // 2. 获取远程数据
      _setProgress(0.2);
      final remoteData = await _getRemoteData();

      // 3. 比较并生成操作列表
      _setProgress(0.3);
      final operations = _compareData(localData, remoteData);

      // 4. 并发执行同步操作
      _setProgress(0.4);
      final semaphore = Semaphore(_concurrencyLimit);
      final futures = <Future<void>>[];

      var completedOperations = 0;
      final totalOperations = operations.length;

      for (final operation in operations) {
        final future = semaphore.acquire().then((_) async {
          try {
            switch (operation.type) {
              case SyncOperationType.upload:
                await _uploadData(operation.item);
                uploaded++;
                break;
              case SyncOperationType.download:
                await _downloadData(operation.item);
                downloaded++;
                break;
              case SyncOperationType.conflict:
                await _resolveConflict(operation.item, operation.remoteItem!);
                conflicts++;
                break;
            }
          } catch (e) {
            errors.add('Failed to sync ${operation.item.fileHash}: $e');
          } finally {
            semaphore.release();
            completedOperations++;
            _setProgress(0.4 + (completedOperations / totalOperations) * 0.6);
          }
        });
        futures.add(future);
      }

      await Future.wait(futures);
    } catch (e) {
      errors.add('Sync failed: $e');
    }

    return SyncResult(
      uploaded: uploaded,
      downloaded: downloaded,
      conflicts: conflicts,
      errors: errors,
    );
  }

  /// 获取本地数据
  Future<Map<String, SyncDataItem>> _getLocalData() async {
    final progressList = await db.select(db.comicProgress).get();
    final result = <String, SyncDataItem>{};

    for (final progress in progressList) {
      result[progress.fileHash] = SyncDataItem(
        fileHash: progress.fileHash,
        currentPage: progress.currentPage,
        totalPages: progress.totalPages,
        updatedAt: progress.updatedAt,
        localEtag: progress.etag,
      );
    }

    return result;
  }

  /// 获取远程数据
  Future<Map<String, SyncDataItem>> _getRemoteData() async {
    try {
      // 确保远程目录存在
      await webdavService.mkdir('/comic_progress/');
    } catch (e) {
      // 目录可能已存在，忽略错误
    }

    try {
      final files = await webdavService.listDir('/comic_progress/');
      final result = <String, SyncDataItem>{};

      for (final file in files) {
        if (!file.isDirectory && file.name.endsWith('.json')) {
          try {
            // 使用临时文件下载
            final tempFile = File(p.join(Directory.systemTemp.path, file.name));
            await webdavService.download(
              '/comic_progress/${file.name}',
              tempFile.path,
            );
            final content = await tempFile.readAsString();
            await tempFile.delete(); // 清理临时文件

            final jsonData = json.decode(content) as Map<String, dynamic>;
            final item = SyncDataItem.fromJson(jsonData);
            result[item.fileHash] = SyncDataItem(
              fileHash: item.fileHash,
              currentPage: item.currentPage,
              updatedAt: item.updatedAt,
              remoteEtag: file.etag,
            );
          } catch (e) {
            // 忽略无法解析的文件
          }
        }
      }

      return result;
    } catch (e) {
      // 远程目录不存在或无法访问
      return {};
    }
  }

  /// 比较本地和远程数据，生成操作列表
  List<SyncOperation> _compareData(
    Map<String, SyncDataItem> localData,
    Map<String, SyncDataItem> remoteData,
  ) {
    final operations = <SyncOperation>[];
    final allKeys = {...localData.keys, ...remoteData.keys};

    for (final key in allKeys) {
      final local = localData[key];
      final remote = remoteData[key];

      if (local == null) {
        // 只有远程有，下载
        operations.add(SyncOperation(SyncOperationType.download, remote!));
      } else if (remote == null) {
        // 只有本地有，上传
        operations.add(SyncOperation(SyncOperationType.upload, local));
      } else {
        // 本地和远程都有，检查是否需要同步
        if (local.localEtag != remote.remoteEtag) {
          // ETag不同，可能存在冲突
          if (local.updatedAt.isAfter(remote.updatedAt)) {
            operations.add(SyncOperation(SyncOperationType.upload, local));
          } else if (remote.updatedAt.isAfter(local.updatedAt)) {
            operations.add(SyncOperation(SyncOperationType.download, remote));
          } else {
            // 时间相同但ETag不同，存在冲突
            operations.add(
              SyncOperation(SyncOperationType.conflict, local, remote),
            );
          }
        }
      }
    }

    return operations;
  }

  /// 上传数据到远程
  Future<void> _uploadData(SyncDataItem item) async {
    final fileName = '${item.fileHash}.json';
    final remotePath = '/comic_progress/$fileName';

    // 创建临时文件
    final tempFile = File(p.join(Directory.systemTemp.path, fileName));
    await tempFile.writeAsString(json.encode(item.toJson()));

    try {
      await webdavService.upload(tempFile.path, remotePath);

      // 获取新的ETag并更新本地数据库
      final fileInfo = await webdavService.getFileInfo(remotePath);
      if (fileInfo != null) {
        await db.upsertProgress(
          item.fileHash,
          item.currentPage,
          item.totalPages,
          etag: fileInfo.etag,
        );
      }
    } finally {
      // 清理临时文件
      if (await tempFile.exists()) {
        await tempFile.delete();
      }
    }
  }

  /// 从远程下载数据
  Future<void> _downloadData(SyncDataItem item) async {
    await db.upsertProgress(
      item.fileHash,
      item.currentPage,
      item.totalPages,
      etag: item.remoteEtag,
    );
  }

  /// 解决冲突
  Future<void> _resolveConflict(
    SyncDataItem localItem,
    SyncDataItem remoteItem,
  ) async {
    switch (conflictResolution) {
      case ConflictResolution.localFirst:
        await _uploadData(localItem);
        break;
      case ConflictResolution.remoteFirst:
        await _downloadData(remoteItem);
        break;
      case ConflictResolution.manual:
        // 在实际应用中，这里应该触发用户界面让用户手动选择
        // 目前默认使用本地优先
        await _uploadData(localItem);
        break;
    }
  }

  /// 清理资源
  void dispose() {
    _statusController.close();
    _progressController.close();
  }
}

/// 同步操作类型
enum SyncOperationType { upload, download, conflict }

/// 同步操作
class SyncOperation {
  SyncOperation(this.type, this.item, [this.remoteItem]);

  final SyncOperationType type;
  final SyncDataItem item;
  final SyncDataItem? remoteItem;
}

