import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:dartz/dartz.dart';
import 'package:flutter/foundation.dart';
import '../error/failures.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';
import 'real_time_notification_service.dart';
import 'network_service.dart';
import '../../domain/entities/webdav_config.dart';
import '../../domain/services/webdav_service.dart';

/// WebDAV同步状态管理器
class WebDAVSyncStateManager {
  static WebDAVSyncStateManager? _instance;
  static WebDAVSyncStateManager get instance => _instance ??= WebDAVSyncStateManager._();
  
  WebDAVSyncStateManager._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  final RealTimeNotificationService _notificationService = RealTimeNotificationService.instance;
  late final NetworkService _networkService;
  
  late final WebDAVService _webdavService;
  
  final Map<String, SyncSession> _activeSessions = {};
  final Map<String, SyncQueue> _syncQueues = {};
  final Map<String, SyncConflict> _conflicts = {};
  final List<SyncHistoryEntry> _syncHistory = [];
  
  Timer? _periodicSyncTimer;
  Timer? _retryTimer;
  bool _initialized = false;
  
  WebDAVConfig? _currentConfig;
  SyncStatus _globalSyncStatus = SyncStatus.idle;

  /// 初始化WebDAV同步状态管理器
  Future<void> initialize(WebDAVService webdavService, NetworkService networkService) async {
    if (_initialized) return;

    try {
      _webdavService = webdavService;
      _networkService = networkService;
      
      // 创建同步事件通道
      _notificationService.createChannel('webdav_sync');
      
      // 监听网络状态变化
      _networkService.statusStream.listen((status) => _handleNetworkChange(status.isConnected));
      
      // 恢复未完成的同步会话
      await _restoreSyncSessions();
      
      _initialized = true;
      _loggingService.info('WebDAVSyncStateManager initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize WebDAVSyncStateManager', e, stackTrace);
      rethrow;
    }
  }

  /// 配置WebDAV连接
  Future<void> configure(WebDAVConfig config) async {
    try {
      _currentConfig = config;
      
      // 测试连接
      final testResult = await _testConnection(config);
      if (testResult.isLeft()) {
        throw Exception('WebDAV connection test failed');
      }
      
      // 保存配置到全局状态
      _globalStateManager.setState('webdav_config', config);
      
      _notificationService.notify(
        'webdav_sync',
        'config_updated',
        {'success': true},
      );
      
      _loggingService.info('WebDAV configuration updated');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to configure WebDAV', e, stackTrace);
      
      _notificationService.notify(
        'webdav_sync',
        'config_error',
        {'error': e.toString()},
      );
      
      rethrow;
    }
  }

  /// 开始同步会话
  Future<String> startSyncSession({
    required String sessionName,
    required Map<String, dynamic> data,
    SyncMode mode = SyncMode.bidirectional,
    SyncStrategy strategy = SyncStrategy.automatic,
    Duration? timeout,
  }) async {
    try {
      if (_currentConfig == null) {
        throw Exception('WebDAV not configured');
      }

      final sessionId = _generateSessionId();
      final session = SyncSession(
        id: sessionId,
        name: sessionName,
        mode: mode,
        strategy: strategy,
        status: SyncSessionStatus.preparing,
        data: data,
        startTime: DateTime.now(),
        timeout: timeout ?? const Duration(minutes: 30),
        config: _currentConfig!,
      );

      _activeSessions[sessionId] = session;
      
      // 创建同步队列
      _syncQueues[sessionId] = SyncQueue(sessionId: sessionId);
      
      // 开始同步流程
      unawaited(_executeSyncSession(session));
      
      _notificationService.notify(
        'webdav_sync',
        'session_started',
        {'sessionId': sessionId, 'sessionName': sessionName},
      );
      
      _loggingService.info('Started sync session: $sessionName ($sessionId)');
      return sessionId;
    } catch (e, stackTrace) {
      _loggingService.error('Failed to start sync session: $sessionName', e, stackTrace);
      rethrow;
    }
  }

  /// 执行同步会话
  Future<void> _executeSyncSession(SyncSession session) async {
    try {
      _updateSessionStatus(session.id, SyncSessionStatus.analyzing);
      
      // 分析本地和远程数据
      final analysisResult = await _analyzeDataDifferences(session);
      
      if (analysisResult.conflicts.isNotEmpty) {
        _updateSessionStatus(session.id, SyncSessionStatus.conflicted);
        await _handleConflicts(session.id, analysisResult.conflicts);
        return;
      }
      
      _updateSessionStatus(session.id, SyncSessionStatus.syncing);
      
      // 执行同步操作
      await _performSync(session, analysisResult);
      
      _updateSessionStatus(session.id, SyncSessionStatus.completed);
      
      // 记录同步历史
      _recordSyncHistory(session, analysisResult);
      
    } catch (e, stackTrace) {
      _updateSessionStatus(session.id, SyncSessionStatus.failed);
      _loggingService.error('Sync session failed: ${session.name}', e, stackTrace);
      
      _notificationService.notify(
        'webdav_sync',
        'session_failed',
        {'sessionId': session.id, 'error': e.toString()},
      );
    } finally {
      // 清理会话
      Timer(const Duration(minutes: 5), () {
        _cleanupSession(session.id);
      });
    }
  }

  /// 分析数据差异
  Future<SyncAnalysisResult> _analyzeDataDifferences(SyncSession session) async {
    final localData = session.data;
    final conflicts = <SyncConflict>[];
    final operations = <SyncOperation>[];
    
    try {
      // 尝试获取远程数据
      final remoteResult = await _webdavService.restore(
        config: session.config,
        fileName: '${session.name}.json',
      );
      
      if (remoteResult.isRight()) {
        final remoteBytes = remoteResult.getOrElse(() => Uint8List(0));
        final remoteDataStr = utf8.decode(remoteBytes);
        final remoteData = json.decode(remoteDataStr) as Map<String, dynamic>;
        
        // 比较本地和远程数据
        final comparison = _compareData(localData, remoteData);
        
        for (final change in comparison.changes) {
          if (change.hasConflict && change.conflictType != null) {
            conflicts.add(SyncConflict(
              id: _generateConflictId(),
              sessionId: session.id,
              path: change.path,
              localValue: change.localValue,
              remoteValue: change.remoteValue,
              conflictType: change.conflictType!,
              timestamp: DateTime.now(),
            ));
          } else {
            operations.add(SyncOperation(
              id: _generateOperationId(),
              type: change.operationType,
              path: change.path,
              data: change.newValue,
              timestamp: DateTime.now(),
            ));
          }
        }
      } else {
        // 远程文件不存在，创建上传操作
        operations.add(SyncOperation(
          id: _generateOperationId(),
          type: SyncOperationType.create,
          path: '${session.name}.json',
          data: localData,
          timestamp: DateTime.now(),
        ));
      }
      
    } catch (e) {
      // 处理分析错误
      _loggingService.warning('Failed to analyze remote data: $e');
    }
    
    return SyncAnalysisResult(
      conflicts: conflicts,
      operations: operations,
      summary: SyncSummary(
        totalChanges: operations.length,
        conflicts: conflicts.length,
        estimatedTime: Duration(seconds: operations.length * 2),
      ),
    );
  }

  /// 比较数据
  DataComparison _compareData(Map<String, dynamic> local, Map<String, dynamic> remote) {
    final changes = <DataChange>[];
    final allKeys = {...local.keys, ...remote.keys};
    
    for (final key in allKeys) {
      final localValue = local[key];
      final remoteValue = remote[key];
      
      if (localValue == null && remoteValue != null) {
        // 远程新增
        changes.add(DataChange(
          path: key,
          operationType: SyncOperationType.create,
          newValue: remoteValue,
          hasConflict: false,
        ));
      } else if (localValue != null && remoteValue == null) {
        // 本地新增
        changes.add(DataChange(
          path: key,
          operationType: SyncOperationType.create,
          newValue: localValue,
          hasConflict: false,
        ));
      } else if (localValue != remoteValue) {
        // 数据冲突
        changes.add(DataChange(
          path: key,
          operationType: SyncOperationType.update,
          localValue: localValue,
          remoteValue: remoteValue,
          hasConflict: true,
          conflictType: ConflictType.dataConflict,
        ));
      }
    }
    
    return DataComparison(changes: changes);
  }

  /// 执行同步操作
  Future<void> _performSync(SyncSession session, SyncAnalysisResult analysis) async {
    for (final operation in analysis.operations) {
      try {
        await _executeOperation(session, operation);
        
        _notificationService.notify(
          'webdav_sync',
          'operation_completed',
          {
            'sessionId': session.id,
            'operationId': operation.id,
            'type': operation.type.name,
          },
        );
        
      } catch (e, stackTrace) {
        _loggingService.error('Sync operation failed: ${operation.id}', e, stackTrace);
        
        // 根据策略决定是否继续
        if (session.strategy == SyncStrategy.failFast) {
          rethrow;
        }
      }
    }
  }

  /// 执行同步操作
  Future<void> _executeOperation(SyncSession session, SyncOperation operation) async {
    switch (operation.type) {
      case SyncOperationType.create:
      case SyncOperationType.update:
        final dataBytes = utf8.encode(json.encode(operation.data));
        await _webdavService.backup(
          config: session.config,
          fileName: operation.path,
          data: Uint8List.fromList(dataBytes),
        );
        break;
        
      case SyncOperationType.delete:
        // WebDAV删除操作（需要扩展WebDAV服务）
        break;
    }
  }

  /// 处理冲突
  Future<void> _handleConflicts(String sessionId, List<SyncConflict> conflicts) async {
    for (final conflict in conflicts) {
      _conflicts[conflict.id] = conflict;
      
      _notificationService.notify(
        'webdav_sync',
        'conflict_detected',
        {
          'sessionId': sessionId,
          'conflictId': conflict.id,
          'path': conflict.path,
          'type': conflict.conflictType.name,
        },
      );
    }
    
    // 等待用户解决冲突或自动解决
    if (_activeSessions[sessionId]?.strategy == SyncStrategy.automatic) {
      await _autoResolveConflicts(sessionId, conflicts);
    }
  }

  /// 自动解决冲突
  Future<void> _autoResolveConflicts(String sessionId, List<SyncConflict> conflicts) async {
    for (final conflict in conflicts) {
      ConflictResolution resolution;
      
      // 简单的自动解决策略：最新写入获胜
      if (conflict.localValue is Map && conflict.remoteValue is Map) {
        final localMap = conflict.localValue as Map<String, dynamic>;
        final remoteMap = conflict.remoteValue as Map<String, dynamic>;
        
        final localTimestamp = DateTime.tryParse(localMap['timestamp'] ?? '');
        final remoteTimestamp = DateTime.tryParse(remoteMap['timestamp'] ?? '');
        
        if (localTimestamp != null && remoteTimestamp != null) {
          if (localTimestamp.isAfter(remoteTimestamp)) {
            resolution = ConflictResolution.useLocal;
          } else {
            resolution = ConflictResolution.useRemote;
          }
        } else {
          resolution = ConflictResolution.useLocal; // 默认使用本地
        }
      } else {
        resolution = ConflictResolution.useLocal;
      }
      
      await resolveConflict(conflict.id, resolution);
    }
  }

  /// 解决冲突
  Future<void> resolveConflict(String conflictId, ConflictResolution resolution) async {
    final conflict = _conflicts[conflictId];
    if (conflict == null) return;
    
    final session = _activeSessions[conflict.sessionId];
    if (session == null) return;
    
    dynamic resolvedValue;
    switch (resolution) {
      case ConflictResolution.useLocal:
        resolvedValue = conflict.localValue;
        break;
      case ConflictResolution.useRemote:
        resolvedValue = conflict.remoteValue;
        break;
      case ConflictResolution.merge:
        resolvedValue = _mergeValues(conflict.localValue, conflict.remoteValue);
        break;
    }
    
    // 更新会话数据
    _updateSessionData(session.id, conflict.path, resolvedValue);
    
    // 移除冲突
    _conflicts.remove(conflictId);
    
    _notificationService.notify(
      'webdav_sync',
      'conflict_resolved',
      {
        'conflictId': conflictId,
        'resolution': resolution.name,
      },
    );
    
    // 检查是否所有冲突都已解决
    final remainingConflicts = _conflicts.values
        .where((c) => c.sessionId == conflict.sessionId)
        .toList();
    
    if (remainingConflicts.isEmpty) {
      // 继续同步
      _updateSessionStatus(session.id, SyncSessionStatus.syncing);
      unawaited(_executeSyncSession(session));
    }
  }

  /// 合并值
  dynamic _mergeValues(dynamic local, dynamic remote) {
    if (local is Map && remote is Map) {
      final merged = Map<String, dynamic>.from(local);
      for (final entry in remote.entries) {
        merged[entry.key] = entry.value;
      }
      return merged;
    }
    return local; // 默认返回本地值
  }

  /// 更新会话状态
  void _updateSessionStatus(String sessionId, SyncSessionStatus status) {
    final session = _activeSessions[sessionId];
    if (session != null) {
      _activeSessions[sessionId] = session.copyWith(status: status);
      
      _notificationService.notify(
        'webdav_sync',
        'session_status_changed',
        {
          'sessionId': sessionId,
          'status': status.name,
        },
      );
    }
  }

  /// 更新会话数据
  void _updateSessionData(String sessionId, String path, dynamic value) {
    final session = _activeSessions[sessionId];
    if (session != null) {
      final updatedData = Map<String, dynamic>.from(session.data);
      updatedData[path] = value;
      _activeSessions[sessionId] = session.copyWith(data: updatedData);
    }
  }

  /// 测试连接
  Future<Either<Failure, Unit>> _testConnection(WebDAVConfig config) async {
    try {
      // 尝试创建一个测试文件
      final testData = utf8.encode(json.encode({'test': 'connection', 'timestamp': DateTime.now().toIso8601String()}));
      return await _webdavService.backup(
        config: config,
        fileName: '.connection_test',
        data: Uint8List.fromList(testData),
      );
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  /// 处理网络变化
  void _handleNetworkChange(bool isConnected) {
    if (isConnected) {
      _resumePendingSyncs();
    } else {
      _pauseActiveSyncs();
    }
  }

  /// 恢复待处理的同步
  void _resumePendingSyncs() {
    for (final session in _activeSessions.values) {
      if (session.status == SyncSessionStatus.paused) {
        _updateSessionStatus(session.id, SyncSessionStatus.preparing);
        unawaited(_executeSyncSession(session));
      }
    }
  }

  /// 暂停活动同步
  void _pauseActiveSyncs() {
    for (final session in _activeSessions.values) {
      if (session.status == SyncSessionStatus.syncing) {
        _updateSessionStatus(session.id, SyncSessionStatus.paused);
      }
    }
  }

  /// 记录同步历史
  void _recordSyncHistory(SyncSession session, SyncAnalysisResult analysis) {
    final historyEntry = SyncHistoryEntry(
      sessionId: session.id,
      sessionName: session.name,
      startTime: session.startTime,
      endTime: DateTime.now(),
      status: session.status,
      operationCount: analysis.operations.length,
      conflictCount: analysis.conflicts.length,
      dataSize: _calculateDataSize(session.data),
    );
    
    _syncHistory.add(historyEntry);
    
    // 限制历史记录数量
    if (_syncHistory.length > 100) {
      _syncHistory.removeAt(0);
    }
    
    // 保存到全局状态
    _globalStateManager.setState('sync_history', _syncHistory);
  }

  /// 计算数据大小
  int _calculateDataSize(Map<String, dynamic> data) {
    try {
      return utf8.encode(json.encode(data)).length;
    } catch (e) {
      return 0;
    }
  }

  /// 恢复同步会话
  Future<void> _restoreSyncSessions() async {
    // 从全局状态恢复未完成的会话
    final savedSessions = _globalStateManager.getState<List<dynamic>>('active_sync_sessions');
    if (savedSessions != null) {
      for (final sessionData in savedSessions) {
        try {
          final session = SyncSession.fromJson(sessionData);
          if (session.status != SyncSessionStatus.completed && 
              session.status != SyncSessionStatus.failed) {
            _activeSessions[session.id] = session;
          }
        } catch (e) {
          _loggingService.warning('Failed to restore sync session: $e');
        }
      }
    }
  }

  /// 清理会话
  void _cleanupSession(String sessionId) {
    _activeSessions.remove(sessionId);
    _syncQueues.remove(sessionId);
    
    // 移除相关冲突
    _conflicts.removeWhere((key, conflict) => conflict.sessionId == sessionId);
    
    // 更新全局状态
    _saveSessions();
  }

  /// 保存会话状态
  void _saveSessions() {
    final sessionList = _activeSessions.values
        .map((session) => session.toJson())
        .toList();
    _globalStateManager.setState('active_sync_sessions', sessionList);
  }

  /// 生成ID
  String _generateSessionId() => 'session_${DateTime.now().millisecondsSinceEpoch}';
  String _generateConflictId() => 'conflict_${DateTime.now().millisecondsSinceEpoch}';
  String _generateOperationId() => 'operation_${DateTime.now().millisecondsSinceEpoch}';

  /// 获取同步状态
  SyncStateInfo getSyncState() {
    return SyncStateInfo(
      globalStatus: _globalSyncStatus,
      activeSessions: _activeSessions.values.toList(),
      pendingConflicts: _conflicts.values.toList(),
      recentHistory: _syncHistory.take(10).toList(),
    );
  }

  /// 获取会话详情
  SyncSession? getSession(String sessionId) {
    return _activeSessions[sessionId];
  }

  /// 取消同步会话
  Future<void> cancelSession(String sessionId) async {
    final session = _activeSessions[sessionId];
    if (session != null) {
      _updateSessionStatus(sessionId, SyncSessionStatus.cancelled);
      _cleanupSession(sessionId);
      
      _notificationService.notify(
        'webdav_sync',
        'session_cancelled',
        {'sessionId': sessionId},
      );
    }
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      _periodicSyncTimer?.cancel();
      _retryTimer?.cancel();
      
      // 保存当前状态
      _saveSessions();
      
      // 清空数据
      _activeSessions.clear();
      _syncQueues.clear();
      _conflicts.clear();
      
      _initialized = false;
      _loggingService.info('WebDAVSyncStateManager disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during WebDAVSyncStateManager disposal', e, stackTrace);
    }
  }
}

/// 同步会话
class SyncSession {
  final String id;
  final String name;
  final SyncMode mode;
  final SyncStrategy strategy;
  final SyncSessionStatus status;
  final Map<String, dynamic> data;
  final DateTime startTime;
  final Duration timeout;
  final WebDAVConfig config;

  const SyncSession({
    required this.id,
    required this.name,
    required this.mode,
    required this.strategy,
    required this.status,
    required this.data,
    required this.startTime,
    required this.timeout,
    required this.config,
  });

  SyncSession copyWith({
    SyncSessionStatus? status,
    Map<String, dynamic>? data,
  }) {
    return SyncSession(
      id: id,
      name: name,
      mode: mode,
      strategy: strategy,
      status: status ?? this.status,
      data: data ?? this.data,
      startTime: startTime,
      timeout: timeout,
      config: config,
    );
  }

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'mode': mode.name,
    'strategy': strategy.name,
    'status': status.name,
    'data': data,
    'startTime': startTime.toIso8601String(),
    'timeout': timeout.inMilliseconds,
  };

  factory SyncSession.fromJson(Map<String, dynamic> json) => SyncSession(
    id: json['id'],
    name: json['name'],
    mode: SyncMode.values.byName(json['mode']),
    strategy: SyncStrategy.values.byName(json['strategy']),
    status: SyncSessionStatus.values.byName(json['status']),
    data: Map<String, dynamic>.from(json['data']),
    startTime: DateTime.parse(json['startTime']),
    timeout: Duration(milliseconds: json['timeout']),
    config: WebDAVConfig.fromJson(json['config']),
  );
}

/// 同步冲突
class SyncConflict {
  final String id;
  final String sessionId;
  final String path;
  final dynamic localValue;
  final dynamic remoteValue;
  final ConflictType conflictType;
  final DateTime timestamp;

  const SyncConflict({
    required this.id,
    required this.sessionId,
    required this.path,
    required this.localValue,
    required this.remoteValue,
    required this.conflictType,
    required this.timestamp,
  });
}

/// 同步操作
class SyncOperation {
  final String id;
  final SyncOperationType type;
  final String path;
  final dynamic data;
  final DateTime timestamp;

  const SyncOperation({
    required this.id,
    required this.type,
    required this.path,
    required this.data,
    required this.timestamp,
  });
}

/// 同步分析结果
class SyncAnalysisResult {
  final List<SyncConflict> conflicts;
  final List<SyncOperation> operations;
  final SyncSummary summary;

  const SyncAnalysisResult({
    required this.conflicts,
    required this.operations,
    required this.summary,
  });
}

/// 同步摘要
class SyncSummary {
  final int totalChanges;
  final int conflicts;
  final Duration estimatedTime;

  const SyncSummary({
    required this.totalChanges,
    required this.conflicts,
    required this.estimatedTime,
  });
}

/// 数据比较结果
class DataComparison {
  final List<DataChange> changes;

  const DataComparison({required this.changes});
}

/// 数据变化
class DataChange {
  final String path;
  final SyncOperationType operationType;
  final dynamic localValue;
  final dynamic remoteValue;
  final dynamic newValue;
  final bool hasConflict;
  final ConflictType? conflictType;

  const DataChange({
    required this.path,
    required this.operationType,
    this.localValue,
    this.remoteValue,
    this.newValue,
    required this.hasConflict,
    this.conflictType,
  });
}

/// 同步队列
class SyncQueue {
  final String sessionId;
  final List<SyncOperation> operations = [];

  SyncQueue({required this.sessionId});
}

/// 同步历史条目
class SyncHistoryEntry {
  final String sessionId;
  final String sessionName;
  final DateTime startTime;
  final DateTime endTime;
  final SyncSessionStatus status;
  final int operationCount;
  final int conflictCount;
  final int dataSize;

  const SyncHistoryEntry({
    required this.sessionId,
    required this.sessionName,
    required this.startTime,
    required this.endTime,
    required this.status,
    required this.operationCount,
    required this.conflictCount,
    required this.dataSize,
  });
}

/// 同步状态信息
class SyncStateInfo {
  final SyncStatus globalStatus;
  final List<SyncSession> activeSessions;
  final List<SyncConflict> pendingConflicts;
  final List<SyncHistoryEntry> recentHistory;

  const SyncStateInfo({
    required this.globalStatus,
    required this.activeSessions,
    required this.pendingConflicts,
    required this.recentHistory,
  });
}

/// 枚举定义
enum SyncStatus { idle, syncing, paused, error }
enum SyncMode { upload, download, bidirectional }
enum SyncStrategy { automatic, manual, failFast }
enum SyncSessionStatus { preparing, analyzing, syncing, conflicted, paused, completed, failed, cancelled }
enum SyncOperationType { create, update, delete }
enum ConflictType { dataConflict, versionConflict, typeConflict }
enum ConflictResolution { useLocal, useRemote, merge }