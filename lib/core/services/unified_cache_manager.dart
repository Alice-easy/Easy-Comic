import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';
import '../error/failures.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';
import 'real_time_notification_service.dart';

/// 统一缓存管理器
class UnifiedCacheManager {
  static UnifiedCacheManager? _instance;
  static UnifiedCacheManager get instance => _instance ??= UnifiedCacheManager._();
  
  UnifiedCacheManager._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  final RealTimeNotificationService _notificationService = RealTimeNotificationService.instance;
  
  final Map<String, CacheLayer> _cacheLayers = {};
  final Map<String, CachePolicy> _policies = {};
  final Map<String, Timer> _cleanupTimers = {};
  
  Directory? _cacheDirectory;
  bool _initialized = false;

  /// 初始化缓存管理器
  Future<void> initialize() async {
    if (_initialized) return;

    try {
      _cacheDirectory = await getTemporaryDirectory();
      
      // 创建默认缓存层
      await _createDefaultCacheLayers();
      
      // 设置默认策略
      _setupDefaultPolicies();
      
      // 启动清理任务
      _startCleanupTasks();
      
      // 创建通知通道
      _notificationService.createChannel('cache_events');
      
      _initialized = true;
      _loggingService.info('UnifiedCacheManager initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize UnifiedCacheManager', e, stackTrace);
      rethrow;
    }
  }

  /// 创建默认缓存层
  Future<void> _createDefaultCacheLayers() async {
    // 内存缓存层 - 最快访问
    await createCacheLayer(
      'memory',
      CacheLayerConfig(
        type: CacheLayerType.memory,
        maxSize: 50 * 1024 * 1024, // 50MB
        maxEntries: 1000,
        ttl: const Duration(hours: 1),
      ),
    );

    // 磁盘缓存层 - 持久化存储
    await createCacheLayer(
      'disk',
      CacheLayerConfig(
        type: CacheLayerType.disk,
        maxSize: 500 * 1024 * 1024, // 500MB
        maxEntries: 10000,
        ttl: const Duration(days: 7),
        directory: path.join(_cacheDirectory!.path, 'disk_cache'),
      ),
    );

    // 预加载缓存层 - 预测性缓存
    await createCacheLayer(
      'preload',
      CacheLayerConfig(
        type: CacheLayerType.hybrid,
        maxSize: 100 * 1024 * 1024, // 100MB
        maxEntries: 500,
        ttl: const Duration(hours: 6),
        directory: path.join(_cacheDirectory!.path, 'preload_cache'),
      ),
    );
  }

  /// 设置默认策略
  void _setupDefaultPolicies() {
    // 图片缓存策略
    _policies['images'] = CachePolicy(
      layers: ['memory', 'disk'],
      writeStrategy: CacheWriteStrategy.writeThrough,
      evictionStrategy: CacheEvictionStrategy.lru,
      compressionEnabled: true,
      encryptionEnabled: false,
    );

    // 数据缓存策略
    _policies['data'] = CachePolicy(
      layers: ['memory'],
      writeStrategy: CacheWriteStrategy.writeBack,
      evictionStrategy: CacheEvictionStrategy.lfu,
      compressionEnabled: false,
      encryptionEnabled: true,
    );

    // 预加载策略
    _policies['preload'] = CachePolicy(
      layers: ['preload'],
      writeStrategy: CacheWriteStrategy.writeThrough,
      evictionStrategy: CacheEvictionStrategy.fifo,
      compressionEnabled: true,
      encryptionEnabled: false,
    );
  }

  /// 创建缓存层
  Future<void> createCacheLayer(String layerId, CacheLayerConfig config) async {
    try {
      final layer = await CacheLayer.create(layerId, config);
      _cacheLayers[layerId] = layer;
      
      _loggingService.debug('Created cache layer: $layerId');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to create cache layer: $layerId', e, stackTrace);
      rethrow;
    }
  }

  /// 设置缓存策略
  void setCachePolicy(String policyName, CachePolicy policy) {
    _policies[policyName] = policy;
    _loggingService.debug('Set cache policy: $policyName');
  }

  /// 缓存数据
  Future<void> cache(
    String key,
    dynamic data, {
    String? policyName,
    Duration? ttl,
    Map<String, dynamic>? metadata,
  }) async {
    try {
      final policy = _policies[policyName] ?? _policies['data']!;
      final entry = CacheEntry(
        key: key,
        data: data,
        metadata: metadata ?? {},
        timestamp: DateTime.now(),
        ttl: ttl ?? policy.layers.first.let((layerId) => _cacheLayers[layerId]?.config.ttl),
        size: _calculateDataSize(data),
      );

      await _cacheToLayers(entry, policy);
      
      // 通知缓存事件
      _notificationService.notify(
        'cache_events',
        'cache_set',
        {'key': key, 'policy': policyName},
      );

      _loggingService.debug('Cached data: $key');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to cache data: $key', e, stackTrace);
      rethrow;
    }
  }

  /// 获取缓存数据
  Future<T?> get<T>(
    String key, {
    String? policyName,
    bool updateAccessTime = true,
  }) async {
    try {
      final policy = _policies[policyName] ?? _policies['data']!;
      
      for (final layerId in policy.layers) {
        final layer = _cacheLayers[layerId];
        if (layer == null) continue;

        final entry = await layer.get(key);
        if (entry != null) {
          // 检查是否过期
          if (_isExpired(entry)) {
            await layer.remove(key);
            continue;
          }

          // 更新访问时间
          if (updateAccessTime) {
            entry.lastAccessed = DateTime.now();
            entry.accessCount++;
            await layer.put(key, entry);
          }

          // 通知缓存命中
          _notificationService.notify(
            'cache_events',
            'cache_hit',
            {'key': key, 'layer': layerId},
          );

          _loggingService.debug('Cache hit: $key in layer $layerId');
          return entry.data as T?;
        }
      }

      // 通知缓存未命中
      _notificationService.notify(
        'cache_events',
        'cache_miss',
        {'key': key, 'policy': policyName},
      );

      _loggingService.debug('Cache miss: $key');
      return null;
    } catch (e, stackTrace) {
      _loggingService.error('Failed to get cached data: $key', e, stackTrace);
      return null;
    }
  }

  /// 缓存到多个层
  Future<void> _cacheToLayers(CacheEntry entry, CachePolicy policy) async {
    switch (policy.writeStrategy) {
      case CacheWriteStrategy.writeThrough:
        await _writeThroughCache(entry, policy);
        break;
      case CacheWriteStrategy.writeBack:
        await _writeBackCache(entry, policy);
        break;
      case CacheWriteStrategy.writeAround:
        await _writeAroundCache(entry, policy);
        break;
    }
  }

  /// 直写缓存
  Future<void> _writeThroughCache(CacheEntry entry, CachePolicy policy) async {
    for (final layerId in policy.layers) {
      final layer = _cacheLayers[layerId];
      if (layer != null) {
        await layer.put(entry.key, entry);
      }
    }
  }

  /// 写回缓存
  Future<void> _writeBackCache(CacheEntry entry, CachePolicy policy) async {
    // 只写入第一层（通常是内存）
    if (policy.layers.isNotEmpty) {
      final layer = _cacheLayers[policy.layers.first];
      if (layer != null) {
        entry.isDirty = true;
        await layer.put(entry.key, entry);
      }
    }
  }

  /// 绕写缓存
  Future<void> _writeAroundCache(CacheEntry entry, CachePolicy policy) async {
    // 跳过第一层，写入其他层
    for (int i = 1; i < policy.layers.length; i++) {
      final layer = _cacheLayers[policy.layers[i]];
      if (layer != null) {
        await layer.put(entry.key, entry);
      }
    }
  }

  /// 检查是否过期
  bool _isExpired(CacheEntry entry) {
    if (entry.ttl == null) return false;
    return DateTime.now().difference(entry.timestamp) > entry.ttl!;
  }

  /// 计算数据大小
  int _calculateDataSize(dynamic data) {
    try {
      if (data is Uint8List) {
        return data.length;
      } else if (data is String) {
        return utf8.encode(data).length;
      } else {
        return utf8.encode(json.encode(data)).length;
      }
    } catch (e) {
      return 0;
    }
  }

  /// 预加载数据
  Future<void> preload(
    String key,
    Future<dynamic> Function() dataLoader, {
    Duration? ttl,
    int priority = 0,
  }) async {
    try {
      final preloadLayer = _cacheLayers['preload'];
      if (preloadLayer == null) return;

      // 检查是否已缓存
      final existing = await preloadLayer.get(key);
      if (existing != null && !_isExpired(existing)) {
        return;
      }

      // 异步加载数据
      unawaited(_performPreload(key, dataLoader, ttl, priority));
      
      _loggingService.debug('Scheduled preload: $key');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to schedule preload: $key', e, stackTrace);
    }
  }

  /// 执行预加载
  Future<void> _performPreload(
    String key,
    Future<dynamic> Function() dataLoader,
    Duration? ttl,
    int priority,
  ) async {
    try {
      final data = await dataLoader();
      await cache(key, data, policyName: 'preload', ttl: ttl);
      
      _notificationService.notify(
        'cache_events',
        'preload_complete',
        {'key': key, 'priority': priority},
      );
    } catch (e, stackTrace) {
      _loggingService.error('Preload failed: $key', e, stackTrace);
    }
  }

  /// 移除缓存
  Future<void> remove(String key, {String? policyName}) async {
    try {
      final policy = _policies[policyName] ?? _policies['data']!;
      
      for (final layerId in policy.layers) {
        final layer = _cacheLayers[layerId];
        if (layer != null) {
          await layer.remove(key);
        }
      }

      _notificationService.notify(
        'cache_events',
        'cache_remove',
        {'key': key, 'policy': policyName},
      );

      _loggingService.debug('Removed cache: $key');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to remove cache: $key', e, stackTrace);
    }
  }

  /// 清理缓存
  Future<void> cleanup() async {
    try {
      for (final layer in _cacheLayers.values) {
        await layer.cleanup();
      }
      
      _notificationService.notify('cache_events', 'cleanup_complete', {});
      _loggingService.info('Cache cleanup completed');
    } catch (e, stackTrace) {
      _loggingService.error('Cache cleanup failed', e, stackTrace);
    }
  }

  /// 启动清理任务
  void _startCleanupTasks() {
    // 定期清理过期数据
    _cleanupTimers['expired'] = Timer.periodic(const Duration(minutes: 10), (_) {
      _cleanupExpiredEntries();
    });

    // 定期检查内存压力
    _cleanupTimers['memory'] = Timer.periodic(const Duration(minutes: 5), (_) {
      _checkMemoryPressure();
    });
  }

  /// 清理过期条目
  Future<void> _cleanupExpiredEntries() async {
    for (final layer in _cacheLayers.values) {
      await layer.cleanupExpired();
    }
  }

  /// 检查内存压力
  Future<void> _checkMemoryPressure() async {
    final memoryLayer = _cacheLayers['memory'];
    if (memoryLayer == null) return;

    final stats = await memoryLayer.getStats();
    final usageRatio = stats.currentSize / stats.maxSize;
    
    if (usageRatio > 0.8) {
      await memoryLayer.evict(stats.entryCount ~/ 4); // 清理25%
      
      _notificationService.notify(
        'cache_events',
        'memory_pressure_cleanup',
        {'usageRatio': usageRatio, 'evicted': stats.entryCount ~/ 4},
      );
    }
  }

  /// 获取缓存统计信息
  Future<CacheManagerStats> getStats() async {
    final layerStats = <String, CacheLayerStats>{};
    
    for (final entry in _cacheLayers.entries) {
      layerStats[entry.key] = await entry.value.getStats();
    }

    return CacheManagerStats(
      layers: layerStats,
      policies: _policies.keys.toList(),
      totalLayers: _cacheLayers.length,
    );
  }

  /// 清空所有缓存
  Future<void> clearAll() async {
    try {
      for (final layer in _cacheLayers.values) {
        await layer.clear();
      }
      
      _notificationService.notify('cache_events', 'clear_all', {});
      _loggingService.info('Cleared all caches');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to clear all caches', e, stackTrace);
    }
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      // 取消清理定时器
      for (final timer in _cleanupTimers.values) {
        timer.cancel();
      }
      _cleanupTimers.clear();

      // 清理所有缓存层
      for (final layer in _cacheLayers.values) {
        await layer.dispose();
      }
      _cacheLayers.clear();

      // 清空策略
      _policies.clear();

      _initialized = false;
      _loggingService.info('UnifiedCacheManager disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during UnifiedCacheManager disposal', e, stackTrace);
    }
  }
}

/// 缓存层
class CacheLayer {
  final String id;
  final CacheLayerConfig config;
  final Map<String, CacheEntry> _memoryCache = {};
  Directory? _diskDirectory;

  CacheLayer._(this.id, this.config);

  static Future<CacheLayer> create(String id, CacheLayerConfig config) async {
    final layer = CacheLayer._(id, config);
    await layer._initialize();
    return layer;
  }

  Future<void> _initialize() async {
    if (config.type == CacheLayerType.disk || config.type == CacheLayerType.hybrid) {
      if (config.directory != null) {
        _diskDirectory = Directory(config.directory!);
        if (!await _diskDirectory!.exists()) {
          await _diskDirectory!.create(recursive: true);
        }
      }
    }
  }

  Future<CacheEntry?> get(String key) async {
    // 先检查内存缓存
    if (config.type == CacheLayerType.memory || config.type == CacheLayerType.hybrid) {
      final entry = _memoryCache[key];
      if (entry != null) return entry;
    }

    // 检查磁盘缓存
    if ((config.type == CacheLayerType.disk || config.type == CacheLayerType.hybrid) && 
        _diskDirectory != null) {
      return await _getDiskEntry(key);
    }

    return null;
  }

  Future<void> put(String key, CacheEntry entry) async {
    // 检查容量限制
    await _ensureCapacity();

    // 存储到内存
    if (config.type == CacheLayerType.memory || config.type == CacheLayerType.hybrid) {
      _memoryCache[key] = entry;
    }

    // 存储到磁盘
    if ((config.type == CacheLayerType.disk || config.type == CacheLayerType.hybrid) && 
        _diskDirectory != null) {
      await _putDiskEntry(key, entry);
    }
  }

  Future<void> remove(String key) async {
    _memoryCache.remove(key);
    
    if (_diskDirectory != null) {
      final file = File(path.join(_diskDirectory!.path, '$key.cache'));
      if (await file.exists()) {
        await file.delete();
      }
    }
  }

  Future<void> cleanup() async {
    await cleanupExpired();
  }

  Future<void> cleanupExpired() async {
    final now = DateTime.now();
    final expiredKeys = <String>[];

    // 清理内存中的过期条目
    for (final entry in _memoryCache.entries) {
      if (entry.value.ttl != null && 
          now.difference(entry.value.timestamp) > entry.value.ttl!) {
        expiredKeys.add(entry.key);
      }
    }

    for (final key in expiredKeys) {
      await remove(key);
    }
  }

  Future<void> evict(int count) async {
    final entries = _memoryCache.entries.toList();
    entries.sort((a, b) => a.value.lastAccessed.compareTo(b.value.lastAccessed));
    
    for (int i = 0; i < count && i < entries.length; i++) {
      await remove(entries[i].key);
    }
  }

  Future<void> clear() async {
    _memoryCache.clear();
    
    if (_diskDirectory != null && await _diskDirectory!.exists()) {
      await for (final entity in _diskDirectory!.list()) {
        if (entity is File && entity.path.endsWith('.cache')) {
          await entity.delete();
        }
      }
    }
  }

  Future<CacheLayerStats> getStats() async {
    int memorySize = 0;
    int diskSize = 0;
    
    for (final entry in _memoryCache.values) {
      memorySize += entry.size;
    }

    if (_diskDirectory != null && await _diskDirectory!.exists()) {
      await for (final entity in _diskDirectory!.list()) {
        if (entity is File) {
          diskSize += await entity.length();
        }
      }
    }

    return CacheLayerStats(
      id: id,
      type: config.type,
      entryCount: _memoryCache.length,
      currentSize: memorySize + diskSize,
      maxSize: config.maxSize,
      hitCount: 0, // TODO: 实现命中计数
      missCount: 0, // TODO: 实现未命中计数
    );
  }

  Future<void> _ensureCapacity() async {
    if (_memoryCache.length >= config.maxEntries) {
      await evict(_memoryCache.length - config.maxEntries + 1);
    }
  }

  Future<CacheEntry?> _getDiskEntry(String key) async {
    try {
      final file = File(path.join(_diskDirectory!.path, '$key.cache'));
      if (!await file.exists()) return null;

      final jsonString = await file.readAsString();
      final json = jsonDecode(jsonString) as Map<String, dynamic>;
      return CacheEntry.fromJson(json);
    } catch (e) {
      return null;
    }
  }

  Future<void> _putDiskEntry(String key, CacheEntry entry) async {
    try {
      final file = File(path.join(_diskDirectory!.path, '$key.cache'));
      final jsonString = jsonEncode(entry.toJson());
      await file.writeAsString(jsonString);
    } catch (e) {
      // 忽略磁盘写入错误
    }
  }

  Future<void> dispose() async {
    _memoryCache.clear();
  }
}

/// 缓存条目
class CacheEntry {
  final String key;
  final dynamic data;
  final Map<String, dynamic> metadata;
  final DateTime timestamp;
  final Duration? ttl;
  final int size;
  
  DateTime lastAccessed;
  int accessCount;
  bool isDirty;

  CacheEntry({
    required this.key,
    required this.data,
    required this.metadata,
    required this.timestamp,
    this.ttl,
    required this.size,
    DateTime? lastAccessed,
    this.accessCount = 0,
    this.isDirty = false,
  }) : lastAccessed = lastAccessed ?? timestamp;

  Map<String, dynamic> toJson() => {
    'key': key,
    'data': data,
    'metadata': metadata,
    'timestamp': timestamp.toIso8601String(),
    'ttl': ttl?.inMilliseconds,
    'size': size,
    'lastAccessed': lastAccessed.toIso8601String(),
    'accessCount': accessCount,
    'isDirty': isDirty,
  };

  factory CacheEntry.fromJson(Map<String, dynamic> json) => CacheEntry(
    key: json['key'],
    data: json['data'],
    metadata: Map<String, dynamic>.from(json['metadata']),
    timestamp: DateTime.parse(json['timestamp']),
    ttl: json['ttl'] != null ? Duration(milliseconds: json['ttl']) : null,
    size: json['size'],
    lastAccessed: DateTime.parse(json['lastAccessed']),
    accessCount: json['accessCount'] ?? 0,
    isDirty: json['isDirty'] ?? false,
  );
}

/// 缓存层配置
class CacheLayerConfig {
  final CacheLayerType type;
  final int maxSize;
  final int maxEntries;
  final Duration ttl;
  final String? directory;

  const CacheLayerConfig({
    required this.type,
    required this.maxSize,
    required this.maxEntries,
    required this.ttl,
    this.directory,
  });
}

/// 缓存策略
class CachePolicy {
  final List<String> layers;
  final CacheWriteStrategy writeStrategy;
  final CacheEvictionStrategy evictionStrategy;
  final bool compressionEnabled;
  final bool encryptionEnabled;

  const CachePolicy({
    required this.layers,
    required this.writeStrategy,
    required this.evictionStrategy,
    required this.compressionEnabled,
    required this.encryptionEnabled,
  });
}

/// 缓存统计信息
class CacheManagerStats {
  final Map<String, CacheLayerStats> layers;
  final List<String> policies;
  final int totalLayers;

  const CacheManagerStats({
    required this.layers,
    required this.policies,
    required this.totalLayers,
  });
}

class CacheLayerStats {
  final String id;
  final CacheLayerType type;
  final int entryCount;
  final int currentSize;
  final int maxSize;
  final int hitCount;
  final int missCount;

  const CacheLayerStats({
    required this.id,
    required this.type,
    required this.entryCount,
    required this.currentSize,
    required this.maxSize,
    required this.hitCount,
    required this.missCount,
  });
}

/// 枚举定义
enum CacheLayerType { memory, disk, hybrid }
enum CacheWriteStrategy { writeThrough, writeBack, writeAround }
enum CacheEvictionStrategy { lru, lfu, fifo, random }

/// 扩展方法
extension on String {
  T let<T>(T Function(String) operation) => operation(this);
}