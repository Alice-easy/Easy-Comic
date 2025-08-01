import 'dart:async';
import 'dart:collection';
import 'dart:typed_data';
import '../background/background_processor.dart';
import '../../core/services/cache_service.dart';

/// Priority levels for preloading operations
enum PreloadPriority {
  critical,  // Currently visible page
  high,      // Next 3 pages
  medium,    // Next 5 pages (4-8)
  low,       // Background preloading (9+)
}

/// Individual preload task
class PreloadTask {
  final String pageKey;
  final int pageIndex;
  final PreloadPriority priority;
  final DateTime createdAt;
  final Completer<bool> completer;
  
  PreloadTask({
    required this.pageKey,
    required this.pageIndex,
    required this.priority,
    required this.completer,
  }) : createdAt = DateTime.now();
  
  /// Check if task has expired (for cleanup)
  bool get isExpired {
    final age = DateTime.now().difference(createdAt);
    return age.inMinutes > 5; // Tasks expire after 5 minutes
  }
}

/// Priority-based preloading engine with separate queues
class PreloadingEngine {
  final ICacheService _cacheService;
  final BackgroundProcessor _backgroundProcessor;
  
  // Separate queues for different priorities
  final Queue<PreloadTask> _criticalQueue = Queue<PreloadTask>();
  final Queue<PreloadTask> _highQueue = Queue<PreloadTask>();
  final Queue<PreloadTask> _mediumQueue = Queue<PreloadTask>();
  final Queue<PreloadTask> _lowQueue = Queue<PreloadTask>();
  
  // Active tasks tracking
  final Set<String> _activeTasks = <String>{};
  final Map<String, PreloadTask> _taskMap = <String, PreloadTask>{};
  
  Timer? _processingTimer;
  Timer? _cleanupTimer;
  bool _isProcessing = false;
  
  // Configuration
  final int maxConcurrentTasks;
  final int maxQueueSize;
  final Duration processingInterval;
  
  PreloadingEngine({
    required ICacheService cacheService,
    required BackgroundProcessor backgroundProcessor,
    this.maxConcurrentTasks = 3,
    this.maxQueueSize = 50,
    this.processingInterval = const Duration(milliseconds: 100),
  }) : _cacheService = cacheService,
       _backgroundProcessor = backgroundProcessor {
    _startProcessingLoop();
    _startCleanupTimer();
  }
  
  /// Add pages to preload with priority-based queue assignment
  Future<void> enqueuePreloading(List<String> pageKeys, int currentPageIndex) async {
    if (pageKeys.isEmpty) return;
    
    // Clear existing tasks for pages no longer needed
    _cancelOutdatedTasks(currentPageIndex);
    
    for (int i = 0; i < pageKeys.length; i++) {
      final pageKey = pageKeys[i];
      final pageIndex = currentPageIndex + i;
      
      // Skip if already cached or being processed
      if (await _cacheService.getImage(pageKey) != null || _activeTasks.contains(pageKey)) {
        continue;
      }
      
      // Determine priority based on distance from current page
      PreloadPriority priority;
      if (i == 0) {
        priority = PreloadPriority.critical; // Current page
      } else if (i <= 3) {
        priority = PreloadPriority.high; // Next 3 pages
      } else if (i <= 8) {
        priority = PreloadPriority.medium; // Next 5 pages (4-8)
      } else {
        priority = PreloadPriority.low; // Background preloading
      }
      
      _enqueueTask(pageKey, pageIndex, priority);
    }
  }
  
  void _enqueueTask(String pageKey, int pageIndex, PreloadPriority priority) {
    // Remove existing task if present
    _removeExistingTask(pageKey);
    
    final task = PreloadTask(
      pageKey: pageKey,
      pageIndex: pageIndex,
      priority: priority,
      completer: Completer<bool>(),
    );
    
    // Add to appropriate queue based on priority
    Queue<PreloadTask> targetQueue;
    switch (priority) {
      case PreloadPriority.critical:
        targetQueue = _criticalQueue;
        break;
      case PreloadPriority.high:
        targetQueue = _highQueue;
        break;
      case PreloadPriority.medium:
        targetQueue = _mediumQueue;
        break;
      case PreloadPriority.low:
        targetQueue = _lowQueue;
        break;
    }
    
    // Check queue size limits
    if (targetQueue.length >= maxQueueSize ~/ 4) {
      // Remove oldest task from this queue
      if (targetQueue.isNotEmpty) {
        final oldTask = targetQueue.removeFirst();
        _taskMap.remove(oldTask.pageKey);
        oldTask.completer.complete(false);
      }
    }
    
    targetQueue.add(task);
    _taskMap[pageKey] = task;
  }
  
  void _removeExistingTask(String pageKey) {
    final existingTask = _taskMap.remove(pageKey);
    if (existingTask != null) {
      _criticalQueue.remove(existingTask);
      _highQueue.remove(existingTask);
      _mediumQueue.remove(existingTask);
      _lowQueue.remove(existingTask);
      existingTask.completer.complete(false);
    }
  }
  
  void _cancelOutdatedTasks(int currentPageIndex) {
    final tasksToRemove = <String>[];
    
    for (final entry in _taskMap.entries) {
      final task = entry.value;
      final distanceFromCurrent = (task.pageIndex - currentPageIndex).abs();
      
      // Cancel tasks that are too far from current page or expired
      if (distanceFromCurrent > 15 || task.isExpired) {
        tasksToRemove.add(entry.key);
      }
    }
    
    for (final pageKey in tasksToRemove) {
      _removeExistingTask(pageKey);
      _activeTasks.remove(pageKey);
    }
  }
  
  void _startProcessingLoop() {
    _processingTimer = Timer.periodic(processingInterval, (_) {
      if (!_isProcessing) {
        _processNextTasks();
      }
    });
  }
  
  Future<void> _processNextTasks() async {
    if (_activeTasks.length >= maxConcurrentTasks) return;
    
    _isProcessing = true;
    
    try {
      final tasksToProcess = <PreloadTask>[];
      
      // Process tasks in priority order
      while (tasksToProcess.length < maxConcurrentTasks - _activeTasks.length) {
        PreloadTask? nextTask;
        
        // Check queues in priority order
        if (_criticalQueue.isNotEmpty) {
          nextTask = _criticalQueue.removeFirst();
        } else if (_highQueue.isNotEmpty) {
          nextTask = _highQueue.removeFirst();
        } else if (_mediumQueue.isNotEmpty) {
          nextTask = _mediumQueue.removeFirst();
        } else if (_lowQueue.isNotEmpty) {
          nextTask = _lowQueue.removeFirst();
        }
        
        if (nextTask == null) break;
        
        tasksToProcess.add(nextTask);
        _activeTasks.add(nextTask.pageKey);
      }
      
      // Process tasks concurrently
      final futures = tasksToProcess.map(_processTask);
      await Future.wait(futures);
      
    } finally {
      _isProcessing = false;
    }
  }
  
  Future<void> _processTask(PreloadTask task) async {
    try {
      // Simulate image loading and processing
      // In a real implementation, this would load image data from file/network
      // and process it using the background processor
      
      final config = ImageProcessingConfig(
        quality: _getQualityForPriority(task.priority),
        outputFormat: ImageFormat.jpeg,
      );
      
      // Process image in background isolate
      final processedImage = await _backgroundProcessor.processImageInBackground(
        task.pageKey, // In reality, this would be a file path
        config,
      );
      
      // Cache the processed image
      await _cacheService.setImage(task.pageKey, processedImage);
      
      task.completer.complete(true);
      
    } catch (e) {
      task.completer.complete(false);
    } finally {
      _activeTasks.remove(task.pageKey);
      _taskMap.remove(task.pageKey);
    }
  }
  
  double _getQualityForPriority(PreloadPriority priority) {
    switch (priority) {
      case PreloadPriority.critical:
        return 1.0; // Maximum quality for current page
      case PreloadPriority.high:
        return 0.9; // High quality for next few pages
      case PreloadPriority.medium:
        return 0.7; // Medium quality for upcoming pages
      case PreloadPriority.low:
        return 0.5; // Lower quality for background preloading
    }
  }
  
  void _startCleanupTimer() {
    _cleanupTimer = Timer.periodic(const Duration(minutes: 1), (_) {
      _cleanupExpiredTasks();
    });
  }
  
  void _cleanupExpiredTasks() {
    final expiredKeys = <String>[];
    
    for (final entry in _taskMap.entries) {
      if (entry.value.isExpired) {
        expiredKeys.add(entry.key);
      }
    }
    
    for (final key in expiredKeys) {
      _removeExistingTask(key);
      _activeTasks.remove(key);
    }
  }
  
  /// Get current queue statistics
  PreloadingStats getStats() {
    return PreloadingStats(
      criticalQueueSize: _criticalQueue.length,
      highQueueSize: _highQueue.length,
      mediumQueueSize: _mediumQueue.length,
      lowQueueSize: _lowQueue.length,
      activeTasks: _activeTasks.length,
      totalTasks: _taskMap.length,
    );
  }
  
  /// Cancel all pending tasks
  void cancelAllTasks() {
    for (final task in _taskMap.values) {
      task.completer.complete(false);
    }
    
    _criticalQueue.clear();
    _highQueue.clear();
    _mediumQueue.clear();
    _lowQueue.clear();
    _taskMap.clear();
    _activeTasks.clear();
  }
  
  /// Dispose the preloading engine
  void dispose() {
    _processingTimer?.cancel();
    _cleanupTimer?.cancel();
    cancelAllTasks();
  }
}

/// Statistics for preloading engine
class PreloadingStats {
  final int criticalQueueSize;
  final int highQueueSize;
  final int mediumQueueSize;
  final int lowQueueSize;
  final int activeTasks;
  final int totalTasks;
  
  const PreloadingStats({
    required this.criticalQueueSize,
    required this.highQueueSize,
    required this.mediumQueueSize,
    required this.lowQueueSize,
    required this.activeTasks,
    required this.totalTasks,
  });
  
  @override
  String toString() {
    return 'PreloadingStats('
        'critical: $criticalQueueSize, '
        'high: $highQueueSize, '
        'medium: $mediumQueueSize, '
        'low: $lowQueueSize, '
        'active: $activeTasks, '
        'total: $totalTasks)';
  }
}