import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:json_annotation/json_annotation.dart';

part 'comic_progress.freezed.dart';
part 'comic_progress.g.dart';

/// 同步状态枚举
@freezed
class SyncStatus with _$SyncStatus {
  const factory SyncStatus.pending() = _Pending;
  const factory SyncStatus.synced() = _Synced;
  const factory SyncStatus.conflict() = _Conflict;
  const factory SyncStatus.error(String message) = _Error;

  factory SyncStatus.fromString(String status) {
    switch (status.toLowerCase()) {
      case 'synced':
        return const SyncStatus.synced();
      case 'conflict':
        return const SyncStatus.conflict();
      case 'pending':
      default:
        return const SyncStatus.pending();
    }
  }

  const SyncStatus._();

  String get value {
    return when(
      pending: () => 'pending',
      synced: () => 'synced',
      conflict: () => 'conflict',
      error: (message) => 'error',
    );
  }
}

/// JSON converter for SyncStatus
class SyncStatusConverter implements JsonConverter<SyncStatus, String> {
  const SyncStatusConverter();

  @override
  SyncStatus fromJson(String json) => SyncStatus.fromString(json);

  @override
  String toJson(SyncStatus object) => object.value;
}

/// 漫画阅读进度实体
@freezed
class ComicProgress with _$ComicProgress {
  const factory ComicProgress({
    required String id,
    required String comicId,
    required int currentPage,
    required int totalPages,
    required DateTime lastUpdated,
    required bool isCompleted,
    @SyncStatusConverter() required SyncStatus syncStatus,
    String? syncETag,
    DateTime? lastSyncTime,
    required int readingTimeSeconds,
    required Map<String, dynamic> metadata,
  }) = _ComicProgress;

  factory ComicProgress.fromJson(Map<String, dynamic> json) =>
      _$ComicProgressFromJson(json);

  const ComicProgress._();

  /// 计算阅读进度百分比
  double get progressPercentage =>
      totalPages > 0 ? (currentPage / totalPages) * 100 : 0.0;

  /// 是否需要同步
  bool get needsSync => syncStatus == const SyncStatus.pending();

  /// 阅读时长（格式化为可读字符串）
  String get formattedReadingTime {
    final hours = readingTimeSeconds ~/ 3600;
    final minutes = (readingTimeSeconds % 3600) ~/ 60;
    final seconds = readingTimeSeconds % 60;

    if (hours > 0) {
      return '${hours}h ${minutes}m ${seconds}s';
    } else if (minutes > 0) {
      return '${minutes}m ${seconds}s';
    } else {
      return '${seconds}s';
    }
  }
}

/// 进度更新请求
@freezed
class ProgressUpdate with _$ProgressUpdate {
  const factory ProgressUpdate({
    required String comicId,
    required int currentPage,
    required DateTime timestamp,
    @Default(false) bool forceImmediate,
    int? totalPages,
    bool? isCompleted,
    Map<String, dynamic>? metadata,
  }) = _ProgressUpdate;

  factory ProgressUpdate.fromJson(Map<String, dynamic> json) =>
      _$ProgressUpdateFromJson(json);
}

/// 进度保存结果
@freezed
class ProgressResult with _$ProgressResult {
  const factory ProgressResult.success(ComicProgress progress) = _Success;
  const factory ProgressResult.failure(ProgressError error) = _Failure;
}

/// 进度操作错误
@freezed
class ProgressError with _$ProgressError {
  const factory ProgressError.saveFailed(String message) = _SaveFailed;
  const factory ProgressError.loadFailed(String message) = _LoadFailed;
  const factory ProgressError.syncConflict(ComicProgress local, ComicProgress remote) = _SyncConflict;
  const factory ProgressError.databaseCorrupted(String message) = _DatabaseCorrupted;
  const factory ProgressError.networkError(String message) = _NetworkError;
  const factory ProgressError.invalidData(String message) = _InvalidData;

  const ProgressError._();

  String get userMessage {
    return when(
      saveFailed: (message) => '保存进度失败：$message',
      loadFailed: (message) => '加载进度失败：$message',
      syncConflict: (local, remote) => '进度同步冲突，请选择要保留的版本',
      databaseCorrupted: (message) => '数据库损坏：$message',
      networkError: (message) => '网络错误：$message',
      invalidData: (message) => '数据格式错误：$message',
    );
  }

  bool get isRetryable {
    return when(
      saveFailed: (_) => true,
      loadFailed: (_) => true,
      syncConflict: (_, __) => false,
      databaseCorrupted: (_) => false,
      networkError: (_) => true,
      invalidData: (_) => false,
    );
  }
}