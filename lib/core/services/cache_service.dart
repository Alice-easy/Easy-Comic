import 'dart:typed_data';

/// Cache service interface for managing image and data caching
abstract class ICacheService {
  /// Get cached image data by key
  Future<Uint8List?> getCachedImage(String key);
  
  /// Cache image data with key
  Future<void> cacheImage(String key, Uint8List data);
  
  /// Clear specific cache entry
  Future<void> clearCache(String key);
  
  /// Clear all cache
  Future<void> clearAllCache();
  
  /// Get cache size in bytes
  Future<int> getCacheSize();
  
  /// Check if key exists in cache
  Future<bool> hasCachedImage(String key);
}

/// Implementation of cache service
class CacheService implements ICacheService {
  final Map<String, Uint8List> _memoryCache = {};
  final int _maxCacheSize;
  
  CacheService({int maxCacheSize = 100 * 1024 * 1024}) // 100MB default
      : _maxCacheSize = maxCacheSize;

  @override
  Future<Uint8List?> getCachedImage(String key) async {
    return _memoryCache[key];
  }

  @override
  Future<void> cacheImage(String key, Uint8List data) async {
    // Simple LRU implementation
    if (_getCurrentCacheSize() + data.length > _maxCacheSize) {
      _evictOldEntries();
    }
    _memoryCache[key] = data;
  }

  @override
  Future<void> clearCache(String key) async {
    _memoryCache.remove(key);
  }

  @override
  Future<void> clearAllCache() async {
    _memoryCache.clear();
  }

  @override
  Future<int> getCacheSize() async {
    return _getCurrentCacheSize();
  }

  @override
  Future<bool> hasCachedImage(String key) async {
    return _memoryCache.containsKey(key);
  }

  /// Clear all cache entries
  Future<void> clearAll() async {
    await clearAllCache();
  }

  int _getCurrentCacheSize() {
    return _memoryCache.values.fold(0, (total, data) => total + data.length);
  }

  void _evictOldEntries() {
    // Simple eviction: remove half of entries
    final keys = _memoryCache.keys.toList();
    final half = keys.length ~/ 2;
    for (int i = 0; i < half; i++) {
      _memoryCache.remove(keys[i]);
    }
  }
}