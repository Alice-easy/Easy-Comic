import 'dart:typed_data';
import 'dart:developer' as developer;
import 'dart:async';
import 'dart:collection';

/// 预加载优先级枚举
enum PreloadPriority {
  critical(0),    // 当前页面 - 永不清除
  high(1),        // 下 1-3 页
  medium(2),      // 下 4-5 页  
  low(3),         // 后台预加载
  maintenance(4); // 缓存清理操作
  
  const PreloadPriority(this.level);
  final int level;
  
  String get name {
    switch (this) {
      case PreloadPriority.critical:
        return 'critical';
      case PreloadPriority.high:
        return 'high';
      case PreloadPriority.medium:
        return 'medium';
      case PreloadPriority.low:
        return 'low';
      case PreloadPriority.maintenance:
        return 'maintenance';
    }
  }
}

/// 内存压力级别
enum MemoryPressureLevel {
  low,      // < 50% 内存使用
  medium,   // 50-75% 内存使用
  high,     // 75-90% 内存使用
  critical, // > 90% 内存使用
}

/// 缓存条目
class CacheEntry {
  final String key;
  final Uint8List data;
  final DateTime createdAt;
  DateTime lastAccessed;
  PreloadPriority priority;
  int accessCount;
  String? comicId;
  int? pageIndex;

  CacheEntry({
    required this.key,
    required this.data,
    required this.priority,
    this.comicId,
    this.pageIndex,
    DateTime? createdAt,
    DateTime? lastAccessed,
    this.accessCount = 0,
  }) : createdAt = createdAt ?? DateTime.now(),
       lastAccessed = lastAccessed ?? DateTime.now();

  int get sizeInBytes => data.length;

  /// 缓存价值评分（越高越重要）
  double get valueScore {
    final ageInMinutes = DateTime.now().difference(lastAccessed).inMinutes;
    final priorityWeight = (5 - priority.level) * 10; // Critical=40, High=30, etc.
    final accessWeight = accessCount * 2;
    final ageWeight = ageInMinutes > 0 ? 100 / ageInMinutes : 100;
    
    return (priorityWeight + accessWeight + ageWeight).toDouble();
  }
}

/// 预加载任务
class PreloadTask {
  final String comicId;
  final int pageIndex;
  final PreloadPriority priority;
  final String cacheKey;
  final DateTime createdAt;
  final Completer<bool> completer;
  
  PreloadTask({
    required this.comicId,
    required this.pageIndex,
    required this.priority,
    required this.cacheKey,
  }) : createdAt = DateTime.now(),
       completer = Completer<bool>();

  Future<bool> get future => completer.future;
}

/// 增强的缓存服务接口
abstract class IEnhancedCacheService {
  /// 基础缓存操作
  Future<Uint8List?> getCachedImage(String key);
  Future<void> cacheImage(String key, Uint8List data, {PreloadPriority priority = PreloadPriority.medium});
  Future<void> clearCache(String key);
  Future<void> clearAllCache();
  Future<int> getCacheSize();
  Future<bool> hasCachedImage(String key);
  
  /// 优先级预加载
  Future<bool> preloadPage(String comicId, int pageIndex, PreloadPriority priority);
  Future<void> startPreloading(String comicId, int currentPage, List<String> pageKeys, PreloadPriority priority);
  Future<void> cancelPreloading(String comicId);
  
  /// 内存管理
  void updateMemoryPressure(MemoryPressureLevel level);
  Future<void> optimizeMemoryUsage();
  Future<void> emergencyCleanup();
  
  /// 缓存统计
  CacheStatistics getStatistics();
  
  /// 生命周期管理
  void dispose();
}

/// 缓存统计信息
class CacheStatistics {
  final int totalEntries;
  final int totalSizeBytes;
  final double hitRate;
  final Map<PreloadPriority, int> entriesByPriority;
  final int preloadingTasks;
  final MemoryPressureLevel memoryPressure;

  const CacheStatistics({
    required this.totalEntries,
    required this.totalSizeBytes,
    required this.hitRate,
    required this.entriesByPriority,
    required this.preloadingTasks,
    required this.memoryPressure,
  });

  String get formattedSize {
    if (totalSizeBytes < 1024 * 1024) {
      return '${(totalSizeBytes / 1024).toStringAsFixed(1)} KB';
    } else {
      return '${(totalSizeBytes / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
  }
}

/// 增强的缓存服务实现
class EnhancedCacheService implements IEnhancedCacheService {
  final Map<String, CacheEntry> _memoryCache = {};
  final Queue<PreloadTask> _preloadQueue = Queue<PreloadTask>();
  final Map<String, Set<PreloadTask>> _activePreloads = {};
  final Map<String, int> _hitCounts = {};
  final Map<String, int> _missCounts = {};
  
  int _maxCacheSize;
  int _maxConcurrentPreloads;
  MemoryPressureLevel _currentMemoryPressure = MemoryPressureLevel.low;
  Timer? _maintenanceTimer;
  Timer? _preloadTimer;
  int _preloadingCount = 0;
  
  // 配置参数
  static const int _defaultMaxCacheSize = 100 * 1024 * 1024; // 100MB
  static const int _defaultMaxConcurrentPreloads = 2;
  static const Duration _maintenanceInterval = Duration(minutes: 5);
  static const Duration _preloadCheckInterval = Duration(milliseconds: 100);

  EnhancedCacheService({
    int? maxCacheSize,
    int? maxConcurrentPreloads,
  }) : _maxCacheSize = maxCacheSize ?? _defaultMaxCacheSize,
       _maxConcurrentPreloads = maxConcurrentPreloads ?? _defaultMaxConcurrentPreloads {
    _startMaintenanceTimer();
    _startPreloadProcessor();
  }

  @override
  Future<Uint8List?> getCachedImage(String key) async {
    final entry = _memoryCache[key];
    if (entry != null) {
      // 更新访问统计
      entry.lastAccessed = DateTime.now();
      entry.accessCount++;
      _hitCounts[key] = (_hitCounts[key] ?? 0) + 1;
      
      developer.log(
        'Cache hit for key: $key (${entry.priority.name})',
        name: 'EnhancedCacheService',
        level: 500,
      );
      
      return entry.data;
    } else {
      _missCounts[key] = (_missCounts[key] ?? 0) + 1;
      
      developer.log(
        'Cache miss for key: $key',
        name: 'EnhancedCacheService',
        level: 500,
      );
      
      return null;
    }
  }

  @override
  Future<void> cacheImage(String key, Uint8List data, {PreloadPriority priority = PreloadPriority.medium}) async {
    // 检查内存压力并调整策略
    if (_currentMemoryPressure == MemoryPressureLevel.critical && priority != PreloadPriority.critical) {
      developer.log(
        'Skipping cache due to critical memory pressure: $key',
        name: 'EnhancedCacheService',
        level: 900,
      );
      return;
    }

    final entry = CacheEntry(
      key: key,
      data: data,
      priority: priority,
      comicId: _extractComicId(key),
      pageIndex: _extractPageIndex(key),
    );

    // 确保有足够空间
    await _ensureSpace(data.length);

    _memoryCache[key] = entry;
    
    developer.log(
      'Cached image: $key (${entry.priority.name}, ${_formatBytes(data.length)})',
      name: 'EnhancedCacheService',
    );
  }

  @override
  Future<void> clearCache(String key) async {
    final entry = _memoryCache.remove(key);
    if (entry != null) {
      developer.log(
        'Cleared cache entry: $key (${_formatBytes(entry.sizeInBytes)})',
        name: 'EnhancedCacheService',
      );
    }
  }

  @override
  Future<void> clearAllCache() async {
    final totalSize = _getCurrentCacheSize();
    _memoryCache.clear();
    _hitCounts.clear();
    _missCounts.clear();
    
    developer.log(
      'Cleared all cache entries (${_formatBytes(totalSize)})',
      name: 'EnhancedCacheService',
    );
  }

  @override
  Future<int> getCacheSize() async {
    return _getCurrentCacheSize();
  }

  @override
  Future<bool> hasCachedImage(String key) async {
    return _memoryCache.containsKey(key);
  }

  @override
  Future<bool> preloadPage(String comicId, int pageIndex, PreloadPriority priority) async {
    if (_currentMemoryPressure == MemoryPressureLevel.critical && priority != PreloadPriority.critical) {
      return false;
    }

    final cacheKey = _generateCacheKey(comicId, pageIndex);
    
    // 如果已经缓存，直接返回成功
    if (_memoryCache.containsKey(cacheKey)) {
      // 更新优先级如果新的更高
      if (_memoryCache[cacheKey]!.priority.level > priority.level) {
        _memoryCache[cacheKey]!.priority = priority;
      }
      return true;
    }

    // 创建预加载任务
    final task = PreloadTask(
      comicId: comicId,
      pageIndex: pageIndex,
      priority: priority,
      cacheKey: cacheKey,
    );

    _preloadQueue.add(task);
    _activePreloads.putIfAbsent(comicId, () => <PreloadTask>{}).add(task);
    
    developer.log(
      'Queued preload task: $comicId page $pageIndex (${priority.name})',
      name: 'EnhancedCacheService',
    );

    return await task.future;
  }

  @override
  Future<void> startPreloading(String comicId, int currentPage, List<String> pageKeys, PreloadPriority priority) async {
    for (int i = 0; i < pageKeys.length; i++) {
      final pageIndex = currentPage + i + 1;
      if (pageIndex < pageKeys.length) {
        await preloadPage(comicId, pageIndex, priority);
      }
    }
  }

  @override
  Future<void> cancelPreloading(String comicId) async {
    final tasks = _activePreloads.remove(comicId);
    if (tasks != null) {
      for (final task in tasks) {
        if (!task.completer.isCompleted) {
          task.completer.complete(false);
        }
      }
      
      // 从队列中移除相关任务
      _preloadQueue.removeWhere((task) => task.comicId == comicId);
      
      developer.log(
        'Cancelled ${tasks.length} preload tasks for comic: $comicId',
        name: 'EnhancedCacheService',
      );
    }
  }

  @override
  void updateMemoryPressure(MemoryPressureLevel level) {
    if (_currentMemoryPressure != level) {
      developer.log(
        'Memory pressure updated: ${_currentMemoryPressure.name} -> ${level.name}',
        name: 'EnhancedCacheService',
      );
      
      _currentMemoryPressure = level;
      
      // 根据内存压力调整行为
      switch (level) {
        case MemoryPressureLevel.low:
          _maxConcurrentPreloads = 3;
          break;
        case MemoryPressureLevel.medium:
          _maxConcurrentPreloads = 2;
          break;
        case MemoryPressureLevel.high:
          _maxConcurrentPreloads = 1;
          _evictLowPriorityEntries();
          break;
        case MemoryPressureLevel.critical:
          _maxConcurrentPreloads = 0;
          emergencyCleanup();
          break;
      }
    }
  }

  @override
  Future<void> optimizeMemoryUsage() async {
    final initialSize = _getCurrentCacheSize();
    
    // 按价值评分排序，移除低价值条目
    final entries = _memoryCache.values.toList();
    entries.sort((a, b) => a.valueScore.compareTo(b.valueScore));
    
    int targetSize = (_maxCacheSize * 0.8).round(); // 保持在80%以下
    int currentSize = initialSize;
    int removedCount = 0;
    
    for (final entry in entries) {
      if (currentSize <= targetSize) break;
      if (entry.priority == PreloadPriority.critical) continue; // 永不移除关键缓存
      
      _memoryCache.remove(entry.key);
      currentSize -= entry.sizeInBytes;
      removedCount++;
    }
    
    if (removedCount > 0) {
      developer.log(
        'Optimized cache: removed $removedCount entries, freed ${_formatBytes(initialSize - currentSize)}',
        name: 'EnhancedCacheService',
      );
    }
  }

  @override
  Future<void> emergencyCleanup() async {
    final initialSize = _getCurrentCacheSize();
    
    // 清除所有非关键缓存
    _memoryCache.removeWhere((key, entry) => entry.priority != PreloadPriority.critical);
    
    // 取消所有预加载任务
    for (final tasks in _activePreloads.values) {
      for (final task in tasks) {
        if (!task.completer.isCompleted) {
          task.completer.complete(false);
        }
      }
    }
    _activePreloads.clear();
    _preloadQueue.clear();
    
    final finalSize = _getCurrentCacheSize();
    developer.log(
      'Emergency cleanup: freed ${_formatBytes(initialSize - finalSize)}',
      name: 'EnhancedCacheService',
    );
  }

  @override
  CacheStatistics getStatistics() {
    final entriesByPriority = <PreloadPriority, int>{};
    for (final priority in PreloadPriority.values) {
      entriesByPriority[priority] = 0;
    }
    
    for (final entry in _memoryCache.values) {
      entriesByPriority[entry.priority] = (entriesByPriority[entry.priority] ?? 0) + 1;
    }
    
    final totalHits = _hitCounts.values.fold(0, (sum, count) => sum + count);
    final totalMisses = _missCounts.values.fold(0, (sum, count) => sum + count);
    final hitRate = totalHits + totalMisses > 0 ? totalHits / (totalHits + totalMisses) : 0.0;
    
    return CacheStatistics(
      totalEntries: _memoryCache.length,
      totalSizeBytes: _getCurrentCacheSize(),
      hitRate: hitRate,
      entriesByPriority: entriesByPriority,
      preloadingTasks: _preloadQueue.length,
      memoryPressure: _currentMemoryPressure,
    );
  }

  @override
  void dispose() {
    _maintenanceTimer?.cancel();
    _preloadTimer?.cancel();
    
    // 完成所有待处理的预加载任务
    for (final tasks in _activePreloads.values) {
      for (final task in tasks) {
        if (!task.completer.isCompleted) {
          task.completer.complete(false);
        }
      }
    }
    
    _memoryCache.clear();
    _preloadQueue.clear();
    _activePreloads.clear();
    _hitCounts.clear();
    _missCounts.clear();
    
    developer.log('EnhancedCacheService disposed', name: 'EnhancedCacheService');
  }

  // 私有方法

  void _startMaintenanceTimer() {
    _maintenanceTimer = Timer.periodic(_maintenanceInterval, (_) {
      optimizeMemoryUsage();
    });
  }

  void _startPreloadProcessor() {
    _preloadTimer = Timer.periodic(_preloadCheckInterval, (_) {
      _processPreloadQueue();
    });
  }

  Future<void> _processPreloadQueue() async {
    while (_preloadQueue.isNotEmpty && _preloadingCount < _maxConcurrentPreloads) {
      final task = _preloadQueue.removeFirst();
      _preloadingCount++;
      
      // 异步处理预加载任务
      _processPreloadTask(task).then((success) {
        _preloadingCount--;
        if (!task.completer.isCompleted) {
          task.completer.complete(success);
        }
        
        // 从活动预加载中移除
        _activePreloads[task.comicId]?.remove(task);
        if (_activePreloads[task.comicId]?.isEmpty == true) {
          _activePreloads.remove(task.comicId);
        }
      }).catchError((e) {
        _preloadingCount--;
        developer.log(
          'Preload task failed: ${task.cacheKey} - $e',
          name: 'EnhancedCacheService',
          level: 900,
        );
        if (!task.completer.isCompleted) {
          task.completer.complete(false);
        }
      });
    }
  }

  Future<bool> _processPreloadTask(PreloadTask task) async {
    try {
      // 这里应该调用实际的图像加载逻辑
      // 现在只是模拟预加载
      developer.log(
        'Processing preload task: ${task.cacheKey}',
        name: 'EnhancedCacheService',
        level: 500,
      );
      
      // 模拟加载延迟
      await Future.delayed(const Duration(milliseconds: 100));
      
      // 模拟数据加载（这里应该是实际的图像加载）
      final mockData = Uint8List(1024); // 1KB mock data
      
      await cacheImage(task.cacheKey, mockData, priority: task.priority);
      
      return true;
    } catch (e) {
      developer.log(
        'Failed to process preload task: ${task.cacheKey} - $e',
        name: 'EnhancedCacheService',
        level: 900,
      );
      return false;
    }
  }

  Future<void> _ensureSpace(int requiredBytes) async {
    int currentSize = _getCurrentCacheSize();
    if (currentSize + requiredBytes <= _maxCacheSize) {
      return; // 有足够空间
    }

    // 需要清理空间
    final targetSize = _maxCacheSize - requiredBytes;
    await _evictToSize(targetSize);
  }

  Future<void> _evictToSize(int targetSize) async {
    final entries = _memoryCache.values.toList();
    entries.sort((a, b) => a.valueScore.compareTo(b.valueScore));
    
    int currentSize = _getCurrentCacheSize();
    int removedCount = 0;
    
    for (final entry in entries) {
      if (currentSize <= targetSize) break;
      if (entry.priority == PreloadPriority.critical) continue;
      
      _memoryCache.remove(entry.key);
      currentSize -= entry.sizeInBytes;
      removedCount++;
    }
    
    if (removedCount > 0) {
      developer.log(
        'Evicted $removedCount entries to free space',
        name: 'EnhancedCacheService',
      );
    }
  }

  void _evictLowPriorityEntries() {
    final lowPriorityKeys = _memoryCache.entries
        .where((entry) => entry.value.priority == PreloadPriority.low)
        .map((entry) => entry.key)
        .toList();
    
    for (final key in lowPriorityKeys) {
      _memoryCache.remove(key);
    }
    
    if (lowPriorityKeys.isNotEmpty) {
      developer.log(
        'Evicted ${lowPriorityKeys.length} low priority entries',
        name: 'EnhancedCacheService',
      );
    }
  }

  int _getCurrentCacheSize() {
    return _memoryCache.values.fold(0, (total, entry) => total + entry.sizeInBytes);
  }

  String _generateCacheKey(String comicId, int pageIndex) {
    return '${comicId}_page_$pageIndex';
  }

  String? _extractComicId(String cacheKey) {
    final parts = cacheKey.split('_');
    return parts.isNotEmpty ? parts[0] : null;
  }

  int? _extractPageIndex(String cacheKey) {
    final parts = cacheKey.split('_');
    if (parts.length >= 3 && parts[1] == 'page') {
      return int.tryParse(parts[2]);
    }
    return null;
  }

  String _formatBytes(int bytes) {
    if (bytes < 1024) {
      return '${bytes}B';
    } else if (bytes < 1024 * 1024) {
      return '${(bytes / 1024).toStringAsFixed(1)}KB';
    } else {
      return '${(bytes / (1024 * 1024)).toStringAsFixed(1)}MB';
    }
  }
}