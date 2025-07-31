import 'dart:typed_data';
import '../entities/comic_page.dart';

abstract class CacheService {
  /// 获取缓存的图像数据
  Future<Uint8List?> getCachedImage(String imageId);

  /// 存储图像到缓存
  Future<void> cacheImage(String imageId, Uint8List imageData);

  /// 预加载页面
  Future<void> preloadPages(String comicId, int startPage, int count);

  /// 检查内存压力并清理缓存
  Future<void> checkMemoryPressure();

  /// 清理过期缓存
  Future<void> cleanupExpiredCache();

  /// 获取缓存统计信息
  Future<CacheStats> getCacheStats();

  /// 设置缓存配置
  Future<void> setCacheConfig(CacheConfig config);

  /// 清空所有缓存
  Future<void> clearAllCache();

  /// 清空指定漫画的缓存
  Future<void> clearComicCache(String comicId);

  /// 监听内存压力变化
  Stream<MemoryPressureLevel> watchMemoryPressure();
}

class CacheStats {
  final int totalItems;
  final double memoryUsage; // in MB
  final double diskUsage; // in MB
  final int hitCount;
  final int missCount;
  final double hitRate;

  const CacheStats({
    required this.totalItems,
    required this.memoryUsage,
    required this.diskUsage,
    required this.hitCount,
    required this.missCount,
    required this.hitRate,
  });
}

class CacheConfig {
  final int maxMemoryCacheSize; // in MB
  final int maxDiskCacheSize; // in MB
  final int maxCacheItems;
  final Duration cacheExpiration;
  final bool enablePreloading;
  final int preloadPageCount;

  const CacheConfig({
    this.maxMemoryCacheSize = 50,
    this.maxDiskCacheSize = 500,
    this.maxCacheItems = 200,
    this.cacheExpiration = const Duration(days: 7),
    this.enablePreloading = true,
    this.preloadPageCount = 3,
  });
}

enum MemoryPressureLevel {
  normal,    // < 80%
  warning,   // 80-90%
  critical,  // 90-95%
  emergency, // > 95%
}