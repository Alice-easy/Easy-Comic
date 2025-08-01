import 'dart:async';
import 'package:flutter/foundation.dart';
import '../error/failures.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';

/// 跨页面状态同步服务
class CrossPageSyncService {
  static CrossPageSyncService? _instance;
  static CrossPageSyncService get instance => _instance ??= CrossPageSyncService._();
  
  CrossPageSyncService._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  
  final Map<String, SyncChannel> _syncChannels = {};
  final Map<String, Timer> _conflictResolutionTimers = {};
  final Set<String> _activeSyncs = {};

  /// 同步事件流控制器
  final StreamController<SyncEvent> _syncEventController = 
      StreamController<SyncEvent>.broadcast();
  
  /// 同步事件流
  Stream<SyncEvent> get syncEvents => _syncEventController.stream;

  /// 初始化跨页面同步服务
  Future<void> initialize() async {
    try {
      // 监听全局状态变化
      _globalStateManager.stateChanges.listen(_handleGlobalStateChange);
      
      _loggingService.info('CrossPageSyncService initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize CrossPageSyncService', e, stackTrace);
      rethrow;
    }
  }

  /// 注册同步通道
  void registerSyncChannel(String channelKey, {
    required SyncStrategy strategy,
    Duration conflictResolutionDelay = const Duration(milliseconds: 300),
    List<String>? syncKeys,
    SyncFilter? filter,
    ConflictResolver? conflictResolver,
  }) {
    final channel = SyncChannel(
      key: channelKey,
      strategy: strategy,
      conflictResolutionDelay: conflictResolutionDelay,
      syncKeys: syncKeys,
      filter: filter,
      conflictResolver: conflictResolver ?? _defaultConflictResolver,
    );

    _syncChannels[channelKey] = channel;
    
    _loggingService.debug('Registered sync channel: $channelKey');
  }

  /// 同步数据到指定通道
  Future<void> syncData(String channelKey, String dataKey, dynamic data, {
    SyncMetadata? metadata,
    bool force = false,
  }) async {
    try {
      final channel = _syncChannels[channelKey];
      if (channel == null) {
        _loggingService.warning('Sync channel not found: $channelKey');
        return;
      }

      // 检查是否应该同步此数据
      if (!_shouldSync(channel, dataKey, data)) {
        return;
      }

      final syncId = '${channelKey}_${dataKey}_${DateTime.now().millisecondsSinceEpoch}';
      
      if (_activeSyncs.contains(syncId) && !force) {
        _loggingService.debug('Sync already in progress: $syncId');
        return;
      }

      _activeSyncs.add(syncId);

      try {
        await _performSync(channel, dataKey, data, metadata ?? SyncMetadata());
        
        _syncEventController.add(SyncEvent(
          type: SyncEventType.success,
          channelKey: channelKey,
          dataKey: dataKey,
          data: data,
          timestamp: DateTime.now(),
        ));
        
        _loggingService.debug('Data synced successfully: $channelKey.$dataKey');
      } finally {
        _activeSyncs.remove(syncId);
      }
    } catch (e, stackTrace) {
      _loggingService.error('Failed to sync data: $channelKey.$dataKey', e, stackTrace);
      
      _syncEventController.add(SyncEvent(
        type: SyncEventType.error,
        channelKey: channelKey,
        dataKey: dataKey,
        error: e,
        timestamp: DateTime.now(),
      ));
      
      rethrow;
    }
  }

  /// 执行同步操作
  Future<void> _performSync(SyncChannel channel, String dataKey, dynamic data, SyncMetadata metadata) async {
    switch (channel.strategy) {
      case SyncStrategy.immediate:
        await _immediateSync(channel, dataKey, data, metadata);
        break;
      case SyncStrategy.debounced:
        _debouncedSync(channel, dataKey, data, metadata);
        break;
      case SyncStrategy.batch:
        await _batchSync(channel, dataKey, data, metadata);
        break;
      case SyncStrategy.conflictResolution:
        await _conflictResolutionSync(channel, dataKey, data, metadata);
        break;
    }
  }

  /// 立即同步
  Future<void> _immediateSync(SyncChannel channel, String dataKey, dynamic data, SyncMetadata metadata) async {
    final syncedData = SyncedData(
      key: dataKey,
      data: data,
      metadata: metadata,
      timestamp: DateTime.now(),
    );

    // 更新全局状态
    _globalStateManager.setState('sync_${channel.key}_$dataKey', syncedData);
    
    // 通知其他监听者
    _notifyChannelSubscribers(channel.key, dataKey, syncedData);
  }

  /// 防抖同步
  void _debouncedSync(SyncChannel channel, String dataKey, dynamic data, SyncMetadata metadata) {
    final timerKey = '${channel.key}_$dataKey';
    
    _conflictResolutionTimers[timerKey]?.cancel();
    _conflictResolutionTimers[timerKey] = Timer(channel.conflictResolutionDelay, () async {
      await _immediateSync(channel, dataKey, data, metadata);
      _conflictResolutionTimers.remove(timerKey);
    });
  }

  /// 批量同步
  Future<void> _batchSync(SyncChannel channel, String dataKey, dynamic data, SyncMetadata metadata) async {
    final batchKey = 'batch_${channel.key}';
    final existingBatch = _globalStateManager.getState<List<SyncedData>>(batchKey) ?? <SyncedData>[];
    
    final syncedData = SyncedData(
      key: dataKey,
      data: data,
      metadata: metadata,
      timestamp: DateTime.now(),
    );

    existingBatch.add(syncedData);
    _globalStateManager.setState(batchKey, existingBatch);
    
    // 定期处理批量数据
    _scheduleBatchProcessing(channel);
  }

  /// 冲突解决同步
  Future<void> _conflictResolutionSync(SyncChannel channel, String dataKey, dynamic data, SyncMetadata metadata) async {
    final existingData = _globalStateManager.getState<SyncedData>('sync_${channel.key}_$dataKey');
    
    if (existingData != null) {
      // 检测冲突
      final hasConflict = _detectConflict(existingData, data, metadata);
      
      if (hasConflict) {
        final resolvedData = await channel.conflictResolver!(existingData, data, metadata);
        await _immediateSync(channel, dataKey, resolvedData, metadata);
        
        _syncEventController.add(SyncEvent(
          type: SyncEventType.conflictResolved,
          channelKey: channel.key,
          dataKey: dataKey,
          data: resolvedData,
          timestamp: DateTime.now(),
        ));
        
        return;
      }
    }
    
    await _immediateSync(channel, dataKey, data, metadata);
  }

  /// 检测数据冲突
  bool _detectConflict(SyncedData existingData, dynamic newData, SyncMetadata newMetadata) {
    // 基于时间戳的简单冲突检测
    if (newMetadata.version != null && existingData.metadata.version != null) {
      return newMetadata.version! <= existingData.metadata.version!;
    }
    
    // 基于数据内容的冲突检测
    return existingData.data.toString() != newData.toString() &&
           existingData.timestamp.isAfter(newMetadata.timestamp ?? DateTime.now().subtract(const Duration(seconds: 1)));
  }

  /// 默认冲突解决器
  Future<dynamic> _defaultConflictResolver(SyncedData existingData, dynamic newData, SyncMetadata newMetadata) async {
    // 最后写入获胜策略
    final existingTime = existingData.timestamp;
    final newTime = newMetadata.timestamp ?? DateTime.now();
    
    return newTime.isAfter(existingTime) ? newData : existingData.data;
  }

  /// 调度批量处理
  void _scheduleBatchProcessing(SyncChannel channel) {
    final batchTimerKey = 'batch_timer_${channel.key}';
    
    if (_conflictResolutionTimers.containsKey(batchTimerKey)) {
      return; // 已经调度过了
    }
    
    _conflictResolutionTimers[batchTimerKey] = Timer(const Duration(seconds: 1), () async {
      await _processBatch(channel);
      _conflictResolutionTimers.remove(batchTimerKey);
    });
  }

  /// 处理批量数据
  Future<void> _processBatch(SyncChannel channel) async {
    final batchKey = 'batch_${channel.key}';
    final batch = _globalStateManager.getState<List<SyncedData>>(batchKey);
    
    if (batch == null || batch.isEmpty) {
      return;
    }
    
    // 清空批量数据
    _globalStateManager.removeState(batchKey);
    
    // 处理每个数据项
    for (final syncedData in batch) {
      await _immediateSync(channel, syncedData.key, syncedData.data, syncedData.metadata);
    }
    
    _loggingService.debug('Processed batch of ${batch.length} items for channel: ${channel.key}');
  }

  /// 通知通道订阅者
  void _notifyChannelSubscribers(String channelKey, String dataKey, SyncedData syncedData) {
    _syncEventController.add(SyncEvent(
      type: SyncEventType.dataUpdated,
      channelKey: channelKey,
      dataKey: dataKey,
      data: syncedData.data,
      timestamp: syncedData.timestamp,
    ));
  }

  /// 检查是否应该同步数据
  bool _shouldSync(SyncChannel channel, String dataKey, dynamic data) {
    // 检查同步键过滤
    if (channel.syncKeys != null && !channel.syncKeys!.contains(dataKey)) {
      return false;
    }
    
    // 应用自定义过滤器
    if (channel.filter != null) {
      return channel.filter!(dataKey, data);
    }
    
    return true;
  }

  /// 处理全局状态变化
  void _handleGlobalStateChange(StateChangeEvent event) {
    // 如果是同步相关的状态变化，广播给相关通道
    if (event.key.startsWith('sync_')) {
      final parts = event.key.split('_');
      if (parts.length >= 3) {
        final channelKey = parts[1];
        final dataKey = parts.skip(2).join('_');
        
        _syncEventController.add(SyncEvent(
          type: SyncEventType.globalStateChanged,
          channelKey: channelKey,
          dataKey: dataKey,
          data: event.newValue,
          timestamp: event.timestamp,
        ));
      }
    }
  }

  /// 获取同步数据
  T? getSyncedData<T>(String channelKey, String dataKey) {
    final syncedData = _globalStateManager.getState<SyncedData>('sync_${channelKey}_$dataKey');
    return syncedData?.data as T?;
  }

  /// 监听特定通道的同步事件
  Stream<SyncEvent> watchChannel(String channelKey) {
    return syncEvents.where((event) => event.channelKey == channelKey);
  }

  /// 监听特定数据的同步事件
  Stream<SyncEvent> watchData(String channelKey, String dataKey) {
    return syncEvents.where((event) => 
        event.channelKey == channelKey && event.dataKey == dataKey);
  }

  /// 强制同步所有通道
  Future<void> syncAllChannels() async {
    for (final channel in _syncChannels.values) {
      final batchKey = 'batch_${channel.key}';
      final batch = _globalStateManager.getState<List<SyncedData>>(batchKey);
      
      if (batch != null && batch.isNotEmpty) {
        await _processBatch(channel);
      }
    }
    
    _loggingService.info('Forced sync for all channels completed');
  }

  /// 清除通道数据
  Future<void> clearChannel(String channelKey) async {
    final channel = _syncChannels[channelKey];
    if (channel == null) return;
    
    // 清除批量数据
    _globalStateManager.removeState('batch_$channelKey');
    
    // 清除所有同步数据
    final stateKeys = _globalStateManager.getStateKeys();
    for (final key in stateKeys) {
      if (key.startsWith('sync_${channelKey}_')) {
        _globalStateManager.removeState(key);
      }
    }
    
    _loggingService.debug('Cleared all data for channel: $channelKey');
  }

  /// 取消注册同步通道
  void unregisterSyncChannel(String channelKey) {
    _syncChannels.remove(channelKey);
    
    // 取消相关定时器
    final timersToRemove = _conflictResolutionTimers.keys
        .where((key) => key.contains(channelKey))
        .toList();
    
    for (final timerKey in timersToRemove) {
      _conflictResolutionTimers[timerKey]?.cancel();
      _conflictResolutionTimers.remove(timerKey);
    }
    
    _loggingService.debug('Unregistered sync channel: $channelKey');
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      // 强制同步所有数据
      await syncAllChannels();
      
      // 取消所有定时器
      for (final timer in _conflictResolutionTimers.values) {
        timer.cancel();
      }
      _conflictResolutionTimers.clear();
      
      // 关闭事件流
      await _syncEventController.close();
      
      // 清空通道
      _syncChannels.clear();
      _activeSyncs.clear();
      
      _loggingService.info('CrossPageSyncService disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during CrossPageSyncService disposal', e, stackTrace);
    }
  }
}

/// 同步通道配置
class SyncChannel {
  final String key;
  final SyncStrategy strategy;
  final Duration conflictResolutionDelay;
  final List<String>? syncKeys;
  final SyncFilter? filter;
  final ConflictResolver conflictResolver;

  const SyncChannel({
    required this.key,
    required this.strategy,
    required this.conflictResolutionDelay,
    required this.syncKeys,
    required this.filter,
    required this.conflictResolver,
  });
}

/// 同步策略
enum SyncStrategy {
  immediate,           // 立即同步
  debounced,          // 防抖同步
  batch,              // 批量同步
  conflictResolution, // 冲突解决同步
}

/// 同步事件
class SyncEvent {
  final SyncEventType type;
  final String channelKey;
  final String? dataKey;
  final dynamic data;
  final dynamic error;
  final DateTime timestamp;

  const SyncEvent({
    required this.type,
    required this.channelKey,
    this.dataKey,
    this.data,
    this.error,
    required this.timestamp,
  });
}

/// 同步事件类型
enum SyncEventType {
  success,
  error,
  conflictResolved,
  dataUpdated,
  globalStateChanged,
}

/// 同步数据包装
class SyncedData {
  final String key;
  final dynamic data;
  final SyncMetadata metadata;
  final DateTime timestamp;

  const SyncedData({
    required this.key,
    required this.data,
    required this.metadata,
    required this.timestamp,
  });
}

/// 同步元数据
class SyncMetadata {
  final String? source;
  final int? version;
  final Map<String, dynamic>? customData;
  final DateTime? timestamp;

  const SyncMetadata({
    this.source,
    this.version,
    this.customData,
    this.timestamp,
  });
}

/// 类型定义
typedef SyncFilter = bool Function(String dataKey, dynamic data);
typedef ConflictResolver = Future<dynamic> Function(SyncedData existingData, dynamic newData, SyncMetadata newMetadata);