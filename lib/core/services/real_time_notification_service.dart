import 'dart:async';
import 'dart:collection';
import 'package:flutter/foundation.dart';
import '../error/failures.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';

/// 实时数据更新通知服务
class RealTimeNotificationService {
  static RealTimeNotificationService? _instance;
  static RealTimeNotificationService get instance => _instance ??= RealTimeNotificationService._();
  
  RealTimeNotificationService._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  
  final Map<String, NotificationChannel> _channels = {};
  final Map<String, Set<NotificationSubscriber>> _subscribers = {};
  final Queue<NotificationEvent> _eventQueue = Queue<NotificationEvent>();
  final Set<String> _processingEvents = {};
  
  /// 通知事件流控制器
  final StreamController<NotificationEvent> _eventController = 
      StreamController<NotificationEvent>.broadcast();
  
  /// 通知事件流
  Stream<NotificationEvent> get events => _eventController.stream;

  Timer? _batchProcessTimer;
  bool _isProcessing = false;

  /// 初始化实时通知服务
  Future<void> initialize() async {
    try {
      // 监听全局状态变化
      _globalStateManager.stateChanges.listen(_handleGlobalStateChange);
      
      // 启动批量处理定时器
      _startBatchProcessing();
      
      _loggingService.info('RealTimeNotificationService initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize RealTimeNotificationService', e, stackTrace);
      rethrow;
    }
  }

  /// 创建通知通道
  void createChannel(String channelId, {
    NotificationPriority priority = NotificationPriority.normal,
    Duration batchDelay = const Duration(milliseconds: 100),
    int maxBatchSize = 50,
    bool enableFiltering = true,
    NotificationFilter? filter,
  }) {
    final channel = NotificationChannel(
      id: channelId,
      priority: priority,
      batchDelay: batchDelay,
      maxBatchSize: maxBatchSize,
      enableFiltering: enableFiltering,
      filter: filter,
    );

    _channels[channelId] = channel;
    _subscribers[channelId] = <NotificationSubscriber>{};
    
    _loggingService.debug('Created notification channel: $channelId');
  }

  /// 订阅通知
  NotificationSubscription subscribe(
    String channelId,
    NotificationCallback callback, {
    NotificationFilter? filter,
    String? subscriberId,
  }) {
    final channel = _channels[channelId];
    if (channel == null) {
      throw ArgumentError('Channel not found: $channelId');
    }

    final subscriber = NotificationSubscriber(
      id: subscriberId ?? _generateSubscriberId(),
      callback: callback,
      filter: filter,
      subscriptionTime: DateTime.now(),
    );

    _subscribers[channelId]!.add(subscriber);
    
    final subscription = NotificationSubscription(
      channelId: channelId,
      subscriberId: subscriber.id,
      unsubscribe: () => _unsubscribe(channelId, subscriber.id),
    );

    _loggingService.debug('Added subscriber ${subscriber.id} to channel $channelId');
    
    return subscription;
  }

  /// 发送通知
  Future<void> notify(
    String channelId,
    String eventType,
    dynamic data, {
    NotificationPriority? priority,
    Map<String, dynamic>? metadata,
    bool batch = true,
  }) async {
    try {
      final channel = _channels[channelId];
      if (channel == null) {
        _loggingService.warning('Channel not found for notification: $channelId');
        return;
      }

      final event = NotificationEvent(
        id: _generateEventId(),
        channelId: channelId,
        eventType: eventType,
        data: data,
        priority: priority ?? channel.priority,
        metadata: metadata ?? {},
        timestamp: DateTime.now(),
      );

      if (batch && priority != NotificationPriority.immediate) {
        _queueEvent(event);
      } else {
        await _processEvent(event);
      }

      _loggingService.debug('Notification sent: $channelId.$eventType');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to send notification: $channelId.$eventType', e, stackTrace);
      rethrow;
    }
  }

  /// 广播通知到所有通道
  Future<void> broadcast(
    String eventType,
    dynamic data, {
    NotificationPriority priority = NotificationPriority.normal,
    Map<String, dynamic>? metadata,
    List<String>? excludeChannels,
  }) async {
    final channelsToNotify = _channels.keys
        .where((channelId) => excludeChannels?.contains(channelId) != true)
        .toList();

    for (final channelId in channelsToNotify) {
      await notify(
        channelId,
        eventType,
        data,
        priority: priority,
        metadata: metadata,
      );
    }

    _loggingService.debug('Broadcast notification: $eventType to ${channelsToNotify.length} channels');
  }

  /// 队列事件
  void _queueEvent(NotificationEvent event) {
    _eventQueue.add(event);
    
    // 如果队列太长，处理高优先级事件
    if (_eventQueue.length > 100) {
      _processHighPriorityEvents();
    }
  }

  /// 处理事件
  Future<void> _processEvent(NotificationEvent event) async {
    final eventKey = '${event.channelId}_${event.id}';
    
    if (_processingEvents.contains(eventKey)) {
      return; // 避免重复处理
    }

    _processingEvents.add(eventKey);

    try {
      final channel = _channels[event.channelId];
      final subscribers = _subscribers[event.channelId] ?? <NotificationSubscriber>{};

      if (channel == null || subscribers.isEmpty) {
        return;
      }

      // 应用通道过滤器
      if (channel.enableFiltering && channel.filter != null) {
        if (!channel.filter!(event)) {
          return;
        }
      }

      // 发送给订阅者
      final futures = <Future<void>>[];
      
      for (final subscriber in subscribers) {
        // 应用订阅者过滤器
        if (subscriber.filter != null && !subscriber.filter!(event)) {
          continue;
        }

        futures.add(_notifySubscriber(subscriber, event));
      }

      await Future.wait(futures);

      // 发送到事件流
      _eventController.add(event);

      _loggingService.debug('Processed event: ${event.channelId}.${event.eventType}');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to process event: ${event.channelId}.${event.eventType}', e, stackTrace);
    } finally {
      _processingEvents.remove(eventKey);
    }
  }

  /// 通知订阅者
  Future<void> _notifySubscriber(NotificationSubscriber subscriber, NotificationEvent event) async {
    try {
      await subscriber.callback(event);
    } catch (e, stackTrace) {
      _loggingService.error('Error in subscriber callback: ${subscriber.id}', e, stackTrace);
    }
  }

  /// 启动批量处理
  void _startBatchProcessing() {
    _batchProcessTimer = Timer.periodic(const Duration(milliseconds: 50), (_) {
      if (!_isProcessing && _eventQueue.isNotEmpty) {
        _processBatch();
      }
    });
  }

  /// 处理批量事件
  Future<void> _processBatch() async {
    if (_isProcessing) return;
    
    _isProcessing = true;

    try {
      final batch = <NotificationEvent>[];
      final now = DateTime.now();

      // 按通道分组处理
      final channelBatches = <String, List<NotificationEvent>>{};

      while (_eventQueue.isNotEmpty && batch.length < 50) {
        final event = _eventQueue.removeFirst();
        
        // 检查事件是否过期（超过5秒）
        if (now.difference(event.timestamp).inSeconds > 5) {
          _loggingService.warning('Dropping expired event: ${event.channelId}.${event.eventType}');
          continue;
        }

        batch.add(event);
        
        // 按通道分组
        channelBatches.putIfAbsent(event.channelId, () => <NotificationEvent>[]);
        channelBatches[event.channelId]!.add(event);
      }

      // 按优先级排序和处理
      for (final channelEntry in channelBatches.entries) {
        final channelEvents = channelEntry.value;
        final channel = _channels[channelEntry.key];
        
        if (channel == null) continue;

        // 按优先级排序
        channelEvents.sort((a, b) => b.priority.index.compareTo(a.priority.index));

        // 限制批量大小
        final eventsToProcess = channelEvents.take(channel.maxBatchSize).toList();
        
        // 处理事件
        for (final event in eventsToProcess) {
          await _processEvent(event);
        }
      }

      if (batch.isNotEmpty) {
        _loggingService.debug('Processed batch of ${batch.length} events');
      }
    } finally {
      _isProcessing = false;
    }
  }

  /// 处理高优先级事件
  void _processHighPriorityEvents() {
    final highPriorityEvents = <NotificationEvent>[];
    final remainingEvents = <NotificationEvent>[];

    // 分离高优先级事件
    while (_eventQueue.isNotEmpty) {
      final event = _eventQueue.removeFirst();
      if (event.priority == NotificationPriority.high || 
          event.priority == NotificationPriority.immediate) {
        highPriorityEvents.add(event);
      } else {
        remainingEvents.add(event);
      }
    }

    // 重新添加剩余事件
    for (final event in remainingEvents) {
      _eventQueue.add(event);
    }

    // 立即处理高优先级事件
    for (final event in highPriorityEvents) {
      _processEvent(event);
    }
  }

  /// 处理全局状态变化
  void _handleGlobalStateChange(StateChangeEvent event) {
    // 将全局状态变化转换为通知事件
    broadcast(
      'global_state_changed',
      {
        'key': event.key,
        'newValue': event.newValue,
        'oldValue': event.oldValue,
      },
      priority: NotificationPriority.normal,
      metadata: {
        'source': 'global_state_manager',
        'timestamp': event.timestamp.toIso8601String(),
      },
    );
  }

  /// 取消订阅
  void _unsubscribe(String channelId, String subscriberId) {
    final subscribers = _subscribers[channelId];
    if (subscribers != null) {
      subscribers.removeWhere((subscriber) => subscriber.id == subscriberId);
      _loggingService.debug('Removed subscriber $subscriberId from channel $channelId');
    }
  }

  /// 监听特定通道的事件
  Stream<NotificationEvent> watchChannel(String channelId) {
    return events.where((event) => event.channelId == channelId);
  }

  /// 监听特定事件类型
  Stream<NotificationEvent> watchEventType(String eventType) {
    return events.where((event) => event.eventType == eventType);
  }

  /// 获取通道信息
  NotificationChannelInfo? getChannelInfo(String channelId) {
    final channel = _channels[channelId];
    final subscribers = _subscribers[channelId];
    
    if (channel == null) return null;

    return NotificationChannelInfo(
      channel: channel,
      subscriberCount: subscribers?.length ?? 0,
      queuedEventCount: _eventQueue.where((e) => e.channelId == channelId).length,
    );
  }

  /// 获取所有通道信息
  List<NotificationChannelInfo> getAllChannelInfo() {
    return _channels.keys
        .map((channelId) => getChannelInfo(channelId))
        .where((info) => info != null)
        .cast<NotificationChannelInfo>()
        .toList();
  }

  /// 清空通道队列
  void clearChannelQueue(String channelId) {
    _eventQueue.removeWhere((event) => event.channelId == channelId);
    _loggingService.debug('Cleared queue for channel: $channelId');
  }

  /// 删除通道
  void deleteChannel(String channelId) {
    _channels.remove(channelId);
    _subscribers.remove(channelId);
    clearChannelQueue(channelId);
    
    _loggingService.debug('Deleted channel: $channelId');
  }

  /// 暂停通道
  void pauseChannel(String channelId) {
    final channel = _channels[channelId];
    if (channel != null) {
      _channels[channelId] = channel.copyWith(paused: true);
      _loggingService.debug('Paused channel: $channelId');
    }
  }

  /// 恢复通道
  void resumeChannel(String channelId) {
    final channel = _channels[channelId];
    if (channel != null) {
      _channels[channelId] = channel.copyWith(paused: false);
      _loggingService.debug('Resumed channel: $channelId');
    }
  }

  /// 生成订阅者ID
  String _generateSubscriberId() {
    return 'subscriber_${DateTime.now().millisecondsSinceEpoch}_${_subscribers.length}';
  }

  /// 生成事件ID
  String _generateEventId() {
    return 'event_${DateTime.now().millisecondsSinceEpoch}_${_eventQueue.length}';
  }

  /// 获取统计信息
  NotificationServiceStats getStats() {
    return NotificationServiceStats(
      totalChannels: _channels.length,
      totalSubscribers: _subscribers.values.map((s) => s.length).fold(0, (a, b) => a + b),
      queuedEvents: _eventQueue.length,
      processingEvents: _processingEvents.length,
      isProcessing: _isProcessing,
    );
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      // 处理剩余事件
      if (_eventQueue.isNotEmpty) {
        await _processBatch();
      }
      
      // 停止定时器
      _batchProcessTimer?.cancel();
      
      // 关闭事件流
      await _eventController.close();
      
      // 清空数据
      _channels.clear();
      _subscribers.clear();
      _eventQueue.clear();
      _processingEvents.clear();
      
      _loggingService.info('RealTimeNotificationService disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during RealTimeNotificationService disposal', e, stackTrace);
    }
  }
}

/// 通知通道
class NotificationChannel {
  final String id;
  final NotificationPriority priority;
  final Duration batchDelay;
  final int maxBatchSize;
  final bool enableFiltering;
  final NotificationFilter? filter;
  final bool paused;

  const NotificationChannel({
    required this.id,
    required this.priority,
    required this.batchDelay,
    required this.maxBatchSize,
    required this.enableFiltering,
    this.filter,
    this.paused = false,
  });

  NotificationChannel copyWith({
    String? id,
    NotificationPriority? priority,
    Duration? batchDelay,
    int? maxBatchSize,
    bool? enableFiltering,
    NotificationFilter? filter,
    bool? paused,
  }) {
    return NotificationChannel(
      id: id ?? this.id,
      priority: priority ?? this.priority,
      batchDelay: batchDelay ?? this.batchDelay,
      maxBatchSize: maxBatchSize ?? this.maxBatchSize,
      enableFiltering: enableFiltering ?? this.enableFiltering,
      filter: filter ?? this.filter,
      paused: paused ?? this.paused,
    );
  }
}

/// 通知优先级
enum NotificationPriority {
  low,
  normal,
  high,
  immediate,
}

/// 通知事件
class NotificationEvent {
  final String id;
  final String channelId;
  final String eventType;
  final dynamic data;
  final NotificationPriority priority;
  final Map<String, dynamic> metadata;
  final DateTime timestamp;

  const NotificationEvent({
    required this.id,
    required this.channelId,
    required this.eventType,
    required this.data,
    required this.priority,
    required this.metadata,
    required this.timestamp,
  });
}

/// 通知订阅者
class NotificationSubscriber {
  final String id;
  final NotificationCallback callback;
  final NotificationFilter? filter;
  final DateTime subscriptionTime;

  const NotificationSubscriber({
    required this.id,
    required this.callback,
    this.filter,
    required this.subscriptionTime,
  });
}

/// 通知订阅
class NotificationSubscription {
  final String channelId;
  final String subscriberId;
  final VoidCallback unsubscribe;

  const NotificationSubscription({
    required this.channelId,
    required this.subscriberId,
    required this.unsubscribe,
  });
}

/// 通道信息
class NotificationChannelInfo {
  final NotificationChannel channel;
  final int subscriberCount;
  final int queuedEventCount;

  const NotificationChannelInfo({
    required this.channel,
    required this.subscriberCount,
    required this.queuedEventCount,
  });
}

/// 服务统计信息
class NotificationServiceStats {
  final int totalChannels;
  final int totalSubscribers;
  final int queuedEvents;
  final int processingEvents;
  final bool isProcessing;

  const NotificationServiceStats({
    required this.totalChannels,
    required this.totalSubscribers,
    required this.queuedEvents,
    required this.processingEvents,
    required this.isProcessing,
  });
}

/// 类型定义
typedef NotificationCallback = Future<void> Function(NotificationEvent event);
typedef NotificationFilter = bool Function(NotificationEvent event);