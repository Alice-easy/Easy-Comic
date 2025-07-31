import 'dart:typed_data';
import 'dart:collection';
import 'dart:async';
import '../../domain/services/cache_service.dart';

class CacheServiceImpl implements CacheService {
  final Map<String, CacheEntry> _memoryCache = LinkedHashMap<String, CacheEntry>();
  final int _maxMemorySize;
  int _currentMemorySize = 0;
  Timer? _memoryPressureTimer;
  
  static const int _defaultMaxSize = 100 * 1024 * 1024; // 100MB
  static const double _memoryWarningThreshold = 0.8;
  static const double _memoryDangerThreshold = 0.9;
  static const double _memoryCriticalThreshold = 0.95;

  CacheServiceImpl({int maxMemorySize = _defaultMaxSize}) 
      : _maxMemorySize = maxMemorySize {
    _startMemoryPressureMonitoring();
  }

  @override
  Future<Uint8List?> getImage(String key) async {
    final entry = _memoryCache[key];
    if (entry != null) {
      // Update LRU order
      _memoryCache.remove(key);
      _memoryCache[key] = entry.copyWith(lastAccess: DateTime.now());
      return entry.data;
    }
    return null;
  }

  @override
  Future<void> putImage(String key, Uint8List data, CachePriority priority) async {
    final entry = CacheEntry(
      key: key,
      data: data,
      priority: priority,
      size: data.length,
      createdAt: DateTime.now(),
      lastAccess: DateTime.now(),
    );

    // Remove existing entry if present
    if (_memoryCache.containsKey(key)) {
      final existingEntry = _memoryCache[key]!;
      _currentMemorySize -= existingEntry.size;
      _memoryCache.remove(key);
    }

    // Check if we need to make space
    while (_currentMemorySize + entry.size > _maxMemorySize && _memoryCache.isNotEmpty) {
      _evictLeastRecentlyUsed();
    }

    // Add new entry
    _memoryCache[key] = entry;
    _currentMemorySize += entry.size;
  }

  @override
  Future<void> removeImage(String key) async {
    final entry = _memoryCache.remove(key);
    if (entry != null) {
      _currentMemorySize -= entry.size;
    }
  }

  @override
  Future<void> clear() async {
    _memoryCache.clear();
    _currentMemorySize = 0;
  }

  @override
  Future<bool> contains(String key) async {
    return _memoryCache.containsKey(key);
  }

  @override
  Future<int> size() async {
    return _currentMemorySize;
  }

  @override
  Future<void> preloadImages(List<String> keys, CachePriority priority) async {
    // This would typically involve loading images from storage
    // For now, it's a placeholder implementation
    for (final key in keys) {
      if (!_memoryCache.containsKey(key)) {
        // In a real implementation, load the image from file system or network
        // await putImage(key, loadedImageData, priority);
      }
    }
  }

  @override
  Future<double> getMemoryPressure() async {
    return _currentMemorySize / _maxMemorySize;
  }

  @override
  Stream<MemoryPressureLevel> watchMemoryPressure() {
    return Stream.periodic(const Duration(seconds: 1), (_) {
      final pressure = _currentMemorySize / _maxMemorySize;
      if (pressure >= _memoryCriticalThreshold) {
        return MemoryPressureLevel.critical;
      } else if (pressure >= _memoryDangerThreshold) {
        return MemoryPressureLevel.danger;
      } else if (pressure >= _memoryWarningThreshold) {
        return MemoryPressureLevel.warning;
      } else {
        return MemoryPressureLevel.normal;
      }
    });
  }

  void _evictLeastRecentlyUsed() {
    if (_memoryCache.isEmpty) return;

    // Find the least recently used entry with lowest priority
    String? keyToRemove;
    DateTime oldestAccess = DateTime.now();
    CachePriority lowestPriority = CachePriority.high;

    for (final entry in _memoryCache.entries) {
      final cacheEntry = entry.value;
      if (cacheEntry.priority.index <= lowestPriority.index &&
          cacheEntry.lastAccess.isBefore(oldestAccess)) {
        keyToRemove = entry.key;
        oldestAccess = cacheEntry.lastAccess;
        lowestPriority = cacheEntry.priority;
      }
    }

    if (keyToRemove != null) {
      final entry = _memoryCache.remove(keyToRemove)!;
      _currentMemorySize -= entry.size;
    }
  }

  void _startMemoryPressureMonitoring() {
    _memoryPressureTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      final pressure = _currentMemorySize / _maxMemorySize;
      
      if (pressure >= _memoryCriticalThreshold) {
        // Aggressive cleanup - remove 50% of cache
        _cleanupCache(0.5);
      } else if (pressure >= _memoryDangerThreshold) {
        // Moderate cleanup - remove 25% of cache
        _cleanupCache(0.25);
      } else if (pressure >= _memoryWarningThreshold) {
        // Light cleanup - remove low priority items
        _cleanupLowPriorityItems();
      }
    });
  }

  void _cleanupCache(double percentage) {
    final targetSize = (_currentMemorySize * (1.0 - percentage)).round();
    
    // Sort entries by priority and last access time
    final sortedEntries = _memoryCache.entries.toList()
      ..sort((a, b) {
        final priorityCompare = a.value.priority.index.compareTo(b.value.priority.index);
        if (priorityCompare != 0) return priorityCompare;
        return a.value.lastAccess.compareTo(b.value.lastAccess);
      });

    for (final entry in sortedEntries) {
      if (_currentMemorySize <= targetSize) break;
      
      _memoryCache.remove(entry.key);
      _currentMemorySize -= entry.value.size;
    }
  }

  void _cleanupLowPriorityItems() {
    final keysToRemove = <String>[];
    
    for (final entry in _memoryCache.entries) {
      if (entry.value.priority == CachePriority.low) {
        keysToRemove.add(entry.key);
      }
    }

    for (final key in keysToRemove) {
      final entry = _memoryCache.remove(key)!;
      _currentMemorySize -= entry.size;
    }
  }

  @override
  void dispose() {
    _memoryPressureTimer?.cancel();
    clear();
  }
}

class CacheEntry {
  final String key;
  final Uint8List data;
  final CachePriority priority;
  final int size;
  final DateTime createdAt;
  final DateTime lastAccess;

  const CacheEntry({
    required this.key,
    required this.data,
    required this.priority,
    required this.size,
    required this.createdAt,
    required this.lastAccess,
  });

  CacheEntry copyWith({
    String? key,
    Uint8List? data,
    CachePriority? priority,
    int? size,
    DateTime? createdAt,
    DateTime? lastAccess,
  }) {
    return CacheEntry(
      key: key ?? this.key,
      data: data ?? this.data,
      priority: priority ?? this.priority,
      size: size ?? this.size,
      createdAt: createdAt ?? this.createdAt,
      lastAccess: lastAccess ?? this.lastAccess,
    );
  }
}