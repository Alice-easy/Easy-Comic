import 'dart:async';
import 'dart:developer' as developer;
import 'dart:typed_data';
import 'package:easy_comic/core/services/enhanced_cache_service.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';

/// 预加载策略
enum PreloadingStrategy {
  conservative, // 保守策略：仅预加载下一页
  standard,     // 标准策略：预加载前后各2页
  aggressive,   // 激进策略：预加载前后各5页
  adaptive,     // 自适应策略：根据内存和网络条件调整
}

/// 预加载状态
enum PreloadingTaskStatus {
  queued,     // 已排队
  loading,    // 正在加载
  completed,  // 已完成
  failed,     // 失败
  cancelled,  // 已取消
}

/// 预加载任务状态信息
class PreloadingStatus {
  final String comicId;
  final List<PreloadingTaskInfo> activeTasks;
  final List<PreloadingTaskInfo> queuedTasks;
  final MemoryPressureLevel memoryPressure;
  final DateTime lastUpdated;
  final PreloadingStrategy currentStrategy;

  const PreloadingStatus({
    required this.comicId,
    required this.activeTasks,
    required this.queuedTasks,
    required this.memoryPressure,
    required this.lastUpdated,
    required this.currentStrategy,
  });

  int get totalTasks => activeTasks.length + queuedTasks.length;
  
  bool get isActive => totalTasks > 0;
}

/// 预加载任务信息
class PreloadingTaskInfo {
  final String comicId;
  final int pageIndex;
  final PreloadPriority priority;
  final PreloadingTaskStatus status;
  final DateTime createdAt;
  final DateTime? startedAt;
  final DateTime? completedAt;
  final String? errorMessage;
  final int? fileSizeBytes;

  const PreloadingTaskInfo({
    required this.comicId,
    required this.pageIndex,
    required this.priority,
    required this.status,
    required this.createdAt,
    this.startedAt,
    this.completedAt,
    this.errorMessage,
    this.fileSizeBytes,
  });

  Duration? get duration {
    if (startedAt != null && completedAt != null) {
      return completedAt!.difference(startedAt!);
    }
    return null;
  }
}

/// 预加载结果
class PreloadResult {
  final bool success;
  final String? errorMessage;
  final Duration? loadTime;
  final int? fileSizeBytes;

  const PreloadResult({
    required this.success,
    this.errorMessage,
    this.loadTime,
    this.fileSizeBytes,
  });

  factory PreloadResult.success({Duration? loadTime, int? fileSizeBytes}) =>
      PreloadResult(
        success: true,
        loadTime: loadTime,
        fileSizeBytes: fileSizeBytes,
      );

  factory PreloadResult.failure(String errorMessage) =>
      PreloadResult(
        success: false,
        errorMessage: errorMessage,
      );
}

/// 页面预加载服务接口
abstract class IPagePreloadingService {
  /// 开始预加载，支持优先级管理
  Future<void> startPreloading(
    String comicId, 
    int currentPage, 
    List<ComicPage> pages,
    PreloadingStrategy strategy,
  );
  
  /// 取消预加载操作
  Future<void> cancelPreloading(String comicId);
  
  /// 获取预加载状态
  PreloadingStatus getStatus(String comicId);
  
  /// 更新内存压力状态
  void updateMemoryPressure(MemoryPressureLevel level);
  
  /// 预加载特定页面，带优先级
  Future<PreloadResult> preloadPage(
    String comicId, 
    int pageIndex, 
    PreloadPriority priority,
  );

  /// 暂停预加载（app后台时）
  void pausePreloading();
  
  /// 恢复预加载
  void resumePreloading();
  
  /// 获取预加载统计信息
  PreloadingStatistics getStatistics();
  
  /// 清理资源
  void dispose();
}

/// 预加载统计信息
class PreloadingStatistics {
  final int totalTasksCreated;
  final int tasksCompleted;
  final int tasksFailed;
  final int tasksCancelled;
  final double averageLoadTime;
  final int totalBytesLoaded;
  final double cacheHitRate;
  final MemoryPressureLevel currentMemoryPressure;

  const PreloadingStatistics({
    required this.totalTasksCreated,
    required this.tasksCompleted,
    required this.tasksFailed,
    required this.tasksCancelled,
    required this.averageLoadTime,
    required this.totalBytesLoaded,
    required this.cacheHitRate,
    required this.currentMemoryPressure,
  });

  double get successRate => totalTasksCreated > 0 ? tasksCompleted / totalTasksCreated : 0.0;

  String get formattedTotalSize {
    if (totalBytesLoaded < 1024 * 1024) {
      return '${(totalBytesLoaded / 1024).toStringAsFixed(1)} KB';
    } else {
      return '${(totalBytesLoaded / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
  }
}

/// 页面预加载服务实现
class PagePreloadingService implements IPagePreloadingService {
  final IEnhancedCacheService _cacheService;
  final Map<String, PreloadingStatus> _activePreloads = {};
  final Map<String, List<PreloadingTaskInfo>> _taskHistory = {};
  
  MemoryPressureLevel _currentMemoryPressure = MemoryPressureLevel.low;
  bool _isPaused = false;
  Timer? _statusUpdateTimer;
  
  // 统计信息
  int _totalTasksCreated = 0;
  int _tasksCompleted = 0;
  int _tasksFailed = 0;
  int _tasksCancelled = 0;
  int _totalBytesLoaded = 0;
  final List<double> _loadTimes = [];

  PagePreloadingService(this._cacheService) {
    _startStatusUpdateTimer();
  }

  @override
  Future<void> startPreloading(
    String comicId,
    int currentPage,
    List<ComicPage> pages,
    PreloadingStrategy strategy,
  ) async {
    if (_isPaused) {
      developer.log(
        'Preloading is paused, skipping start request for comic $comicId',
        name: 'PagePreloadingService',
      );
      return;
    }

    // 取消现有的预加载
    await cancelPreloading(comicId);

    // 根据策略和内存压力确定预加载范围
    final preloadRange = _calculatePreloadRange(strategy, currentPage, pages.length);
    final tasks = <PreloadingTaskInfo>[];

    developer.log(
      'Starting preloading for comic $comicId: current page $currentPage, strategy ${strategy.name}, range ${preloadRange.start}-${preloadRange.end}',
      name: 'PagePreloadingService',
    );

    // 创建预加载任务
    for (int i = preloadRange.start; i <= preloadRange.end; i++) {
      if (i < 0 || i >= pages.length || i == currentPage) continue;

      final priority = _calculatePriority(i, currentPage);
      final task = PreloadingTaskInfo(
        comicId: comicId,
        pageIndex: i,
        priority: priority,
        status: PreloadingTaskStatus.queued,
        createdAt: DateTime.now(),
      );

      tasks.add(task);
      _totalTasksCreated++;

      // 异步启动预加载任务
      _executePreloadTask(task);
    }

    // 更新状态
    _activePreloads[comicId] = PreloadingStatus(
      comicId: comicId,
      activeTasks: tasks.where((t) => t.status == PreloadingTaskStatus.loading).toList(),
      queuedTasks: tasks.where((t) => t.status == PreloadingTaskStatus.queued).toList(),
      memoryPressure: _currentMemoryPressure,
      lastUpdated: DateTime.now(),
      currentStrategy: strategy,
    );
  }

  @override
  Future<void> cancelPreloading(String comicId) async {
    final status = _activePreloads.remove(comicId);
    if (status != null) {
      final totalTasks = status.totalTasks;
      await _cacheService.cancelPreloading(comicId);
      
      _tasksCancelled += totalTasks;
      
      developer.log(
        'Cancelled $totalTasks preload tasks for comic $comicId',
        name: 'PagePreloadingService',
      );
    }
  }

  @override
  PreloadingStatus getStatus(String comicId) {
    return _activePreloads[comicId] ?? PreloadingStatus(
      comicId: comicId,
      activeTasks: [],
      queuedTasks: [],
      memoryPressure: _currentMemoryPressure,
      lastUpdated: DateTime.now(),
      currentStrategy: PreloadingStrategy.standard,
    );
  }

  @override
  void updateMemoryPressure(MemoryPressureLevel level) {
    if (_currentMemoryPressure != level) {
      developer.log(
        'Memory pressure updated in preloading service: ${_currentMemoryPressure.name} -> ${level.name}',
        name: 'PagePreloadingService',
      );
      
      _currentMemoryPressure = level;
      _cacheService.updateMemoryPressure(level);
      
      // 根据内存压力调整预加载行为
      switch (level) {
        case MemoryPressureLevel.critical:
          // 取消所有低优先级预加载
          _cancelLowPriorityTasks();
          break;
        case MemoryPressureLevel.high:
          // 减少并发预加载数量
          _reduceActivePreloads();
          break;
        case MemoryPressureLevel.medium:
        case MemoryPressureLevel.low:
          // 正常操作
          break;
      }
    }
  }

  @override
  Future<PreloadResult> preloadPage(
    String comicId,
    int pageIndex,
    PreloadPriority priority,
  ) async {
    if (_isPaused) {
      return PreloadResult.failure('Preloading is paused');
    }

    final startTime = DateTime.now();
    
    try {
      final success = await _cacheService.preloadPage(comicId, pageIndex, priority);
      final loadTime = DateTime.now().difference(startTime);
      
      if (success) {
        _tasksCompleted++;
        _loadTimes.add(loadTime.inMilliseconds.toDouble());
        
        developer.log(
          'Successfully preloaded page $pageIndex for comic $comicId in ${loadTime.inMilliseconds}ms',
          name: 'PagePreloadingService',
        );
        
        return PreloadResult.success(loadTime: loadTime);
      } else {
        _tasksFailed++;
        return PreloadResult.failure('Failed to preload page');
      }
    } catch (e) {
      _tasksFailed++;
      developer.log(
        'Error preloading page $pageIndex for comic $comicId: $e',
        name: 'PagePreloadingService',
        level: 900,
      );
      return PreloadResult.failure(e.toString());
    }
  }

  @override
  void pausePreloading() {
    if (!_isPaused) {
      _isPaused = true;
      developer.log('Preloading paused', name: 'PagePreloadingService');
    }
  }

  @override
  void resumePreloading() {
    if (_isPaused) {
      _isPaused = false;
      developer.log('Preloading resumed', name: 'PagePreloadingService');
    }
  }

  @override
  PreloadingStatistics getStatistics() {
    final averageLoadTime = _loadTimes.isNotEmpty 
        ? _loadTimes.reduce((a, b) => a + b) / _loadTimes.length 
        : 0.0;
    
    final cacheStats = _cacheService.getStatistics();
    
    return PreloadingStatistics(
      totalTasksCreated: _totalTasksCreated,
      tasksCompleted: _tasksCompleted,
      tasksFailed: _tasksFailed,
      tasksCancelled: _tasksCancelled,
      averageLoadTime: averageLoadTime,
      totalBytesLoaded: _totalBytesLoaded,
      cacheHitRate: cacheStats.hitRate,
      currentMemoryPressure: _currentMemoryPressure,
    );
  }

  @override
  void dispose() {
    _statusUpdateTimer?.cancel();
    
    // 取消所有活动的预加载
    for (final comicId in _activePreloads.keys.toList()) {
      cancelPreloading(comicId);
    }
    
    _activePreloads.clear();
    _taskHistory.clear();
    _loadTimes.clear();
    
    developer.log('PagePreloadingService disposed', name: 'PagePreloadingService');
  }

  // 私有方法

  void _startStatusUpdateTimer() {
    _statusUpdateTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      _updatePreloadingStatuses();
    });
  }

  void _updatePreloadingStatuses() {
    final now = DateTime.now();
    
    for (final entry in _activePreloads.entries) {
      final comicId = entry.key;
      final status = entry.value;
      
      // 更新状态时间戳
      _activePreloads[comicId] = PreloadingStatus(
        comicId: status.comicId,
        activeTasks: status.activeTasks,
        queuedTasks: status.queuedTasks,
        memoryPressure: _currentMemoryPressure,
        lastUpdated: now,
        currentStrategy: status.currentStrategy,
      );
    }
  }

  Range _calculatePreloadRange(PreloadingStrategy strategy, int currentPage, int totalPages) {
    int start, end;
    
    switch (strategy) {
      case PreloadingStrategy.conservative:
        start = currentPage + 1;
        end = currentPage + 1;
        break;
      case PreloadingStrategy.standard:
        start = currentPage - 1;
        end = currentPage + 3;
        break;
      case PreloadingStrategy.aggressive:
        start = currentPage - 2;
        end = currentPage + 5;
        break;
      case PreloadingStrategy.adaptive:
        // 根据内存压力调整
        switch (_currentMemoryPressure) {
          case MemoryPressureLevel.low:
            start = currentPage - 2;
            end = currentPage + 5;
            break;
          case MemoryPressureLevel.medium:
            start = currentPage - 1;
            end = currentPage + 3;
            break;
          case MemoryPressureLevel.high:
            start = currentPage + 1;
            end = currentPage + 2;
            break;
          case MemoryPressureLevel.critical:
            start = currentPage + 1;
            end = currentPage + 1;
            break;
        }
        break;
    }
    
    // 确保范围在有效边界内
    start = start.clamp(0, totalPages - 1);
    end = end.clamp(0, totalPages - 1);
    
    return Range(start, end);
  }

  PreloadPriority _calculatePriority(int pageIndex, int currentPage) {
    final distance = (pageIndex - currentPage).abs();
    
    if (distance == 0) {
      return PreloadPriority.critical;
    } else if (distance <= 3) {
      return PreloadPriority.high;
    } else if (distance <= 5) {
      return PreloadPriority.medium;
    } else {
      return PreloadPriority.low;
    }
  }

  Future<void> _executePreloadTask(PreloadingTaskInfo task) async {
    if (_isPaused) return;
    
    final updatedTask = PreloadingTaskInfo(
      comicId: task.comicId,
      pageIndex: task.pageIndex,
      priority: task.priority,
      status: PreloadingTaskStatus.loading,
      createdAt: task.createdAt,
      startedAt: DateTime.now(),
    );
    
    try {
      final result = await preloadPage(task.comicId, task.pageIndex, task.priority);
      
      final finalTask = PreloadingTaskInfo(
        comicId: task.comicId,
        pageIndex: task.pageIndex,
        priority: task.priority,
        status: result.success ? PreloadingTaskStatus.completed : PreloadingTaskStatus.failed,
        createdAt: task.createdAt,
        startedAt: updatedTask.startedAt,
        completedAt: DateTime.now(),
        errorMessage: result.errorMessage,
        fileSizeBytes: result.fileSizeBytes,
      );
      
      // 更新任务历史
      _taskHistory.putIfAbsent(task.comicId, () => []).add(finalTask);
      
      if (result.fileSizeBytes != null) {
        _totalBytesLoaded += result.fileSizeBytes!;
      }
      
    } catch (e) {
      developer.log(
        'Preload task execution failed: ${task.comicId} page ${task.pageIndex} - $e',
        name: 'PagePreloadingService',
        level: 900,
      );
    }
  }

  void _cancelLowPriorityTasks() {
    for (final comicId in _activePreloads.keys.toList()) {
      final status = _activePreloads[comicId]!;
      final lowPriorityTasks = status.activeTasks
          .where((task) => task.priority == PreloadPriority.low)
          .length;
      
      if (lowPriorityTasks > 0) {
        developer.log(
          'Cancelling $lowPriorityTasks low priority tasks for comic $comicId due to memory pressure',
          name: 'PagePreloadingService',
        );
      }
    }
  }

  void _reduceActivePreloads() {
    // 实现减少活动预加载的逻辑
    // 这里可以暂停一些中等优先级的任务
  }
}

/// 简单的范围类
class Range {
  final int start;
  final int end;
  
  const Range(this.start, this.end);
}