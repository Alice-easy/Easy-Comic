import 'dart:async';
import 'package:easy_comic/domain/entities/comic_progress.dart';

/// 进度持久化管理器接口
abstract class IProgressPersistenceManager {
  /// 保存阅读进度（支持批处理优化）
  Future<ProgressResult> saveProgress(
    String comicId, 
    int currentPage, {
    int? totalPages,
    bool forceImmediate = false,
    bool? isCompleted,
    Map<String, dynamic>? metadata,
  });
  
  /// 加载阅读进度（支持缓存）
  Future<ProgressResult> loadProgress(String comicId);
  
  /// 批量保存多个进度更新
  Future<List<ProgressResult>> saveBatchProgress(
    List<ProgressUpdate> updates
  );
  
  /// 获取待同步的进度记录
  Future<List<ComicProgress>> getPendingSync();
  
  /// 标记进度为已同步
  Future<void> markSynced(String comicId, String etag);
  
  /// 标记进度为冲突状态
  Future<void> markConflict(String comicId);
  
  /// 监听进度变化
  Stream<ComicProgress?> watchProgress(String comicId);
  
  /// 获取所有完成的漫画
  Future<List<ComicProgress>> getCompletedComics();
  
  /// 删除指定漫画的进度
  Future<void> deleteProgress(String comicId);
  
  /// 更新阅读时长
  Future<void> updateReadingTime(String comicId, int additionalSeconds);
  
  /// 清理批处理缓冲区（强制保存所有待处理的更新）
  Future<void> flushBatchBuffer();
  
  /// 获取统计信息
  Future<ProgressStatistics> getStatistics();
}

/// 进度统计信息
class ProgressStatistics {
  final int totalComics;
  final int completedComics;
  final int pendingSyncComics;
  final double averageProgress;
  final int totalReadingTimeSeconds;

  const ProgressStatistics({
    required this.totalComics,
    required this.completedComics,
    required this.pendingSyncComics,
    required this.averageProgress,
    required this.totalReadingTimeSeconds,
  });

  double get completionRate => 
      totalComics > 0 ? (completedComics / totalComics) * 100 : 0.0;

  String get formattedTotalReadingTime {
    final hours = totalReadingTimeSeconds ~/ 3600;
    final minutes = (totalReadingTimeSeconds % 3600) ~/ 60;
    
    if (hours > 0) {
      return '${hours}小时${minutes}分钟';
    } else {
      return '${minutes}分钟';
    }
  }
}