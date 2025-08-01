import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/foundation.dart';
import 'logging_service.dart';
import 'network_service.dart';
import 'global_state_manager.dart';
import 'real_time_notification_service.dart';

/// 离线队列管理器
class OfflineQueueManager {
  static OfflineQueueManager? _instance;
  static OfflineQueueManager get instance => _instance ??= OfflineQueueManager._();
  
  OfflineQueueManager._();

  final LoggingService _loggingService = LoggingService();
  late final NetworkService _networkService;
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  final RealTimeNotificationService _notificationService = RealTimeNotificationService.instance;
  
  final List<QueuedOperation> _operationQueue = [];
  final Map<String, OperationHandler> _handlers = {};
  final Map<String, RetryConfig> _retryConfigs = {};
  
  Timer? _processingTimer;
  bool _initialized = false;
  bool _isProcessing = false;
  
  // 配置参数
  Duration _processingInterval = const Duration(seconds: 5);
  int _maxRetryAttempts = 3;
  Duration _baseRetryDelay = const Duration(seconds: 2);
  int _maxQueueSize = 1000;
  
  /// 初始化离线队列管理器
  Future<void> initialize(NetworkService networkService) async {
    if (_initialized) return;

    try {
      _networkService = networkService;
      
      // 创建队列事件通道
      _notificationService.createChannel('offline_queue');
      
      // 监听网络状态变化
      _networkService.statusStream.listen(_handleNetworkChange);
      
      // 恢复保存的队列
      await _restoreQueue();
      
      // 注册默认操作处理器
      _registerDefaultHandlers();
      
      // 开始处理队列
      _startProcessing();
      
      _initialized = true;
      _loggingService.info('OfflineQueueManager initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize OfflineQueueManager', e, stackTrace);
      rethrow;
    }
  }

  /// 添加操作到队列
  Future<String> enqueue({
    required String operationType,
    required Map<String, dynamic> data,
    OperationPriority priority = OperationPriority.normal,
    Map<String, String>? metadata,
    Duration? timeout,
  }) async {
    try {
      final operation = QueuedOperation(
        id: _generateOperationId(),
        type: operationType,
        data: data,
        priority: priority,
        status: OperationStatus.pending,
        createdAt: DateTime.now(),
        attempts: 0,
        metadata: metadata ?? {},
        timeout: timeout ?? const Duration(minutes: 5),
      );

      // 检查队列大小限制
      if (_operationQueue.length >= _maxQueueSize) {
        await _cleanupOldOperations();
      }

      _operationQueue.add(operation);
      _sortQueueByPriority();
      
      // 保存队列状态
      await _saveQueue();
      
      _notificationService.notify(
        'offline_queue',
        'operation_enqueued',
        {
          'operationId': operation.id,
          'type': operation.type,
          'priority': operation.priority.name,
        },
      );
      
      _loggingService.info('Operation enqueued: ${operation.type} (${operation.id})');
      return operation.id;
    } catch (e, stackTrace) {
      _loggingService.error('Failed to enqueue operation', e, stackTrace);
      rethrow;
    }
  }

  /// 注册操作处理器
  void registerHandler(String operationType, OperationHandler handler, {RetryConfig? retryConfig}) {
    _handlers[operationType] = handler;
    if (retryConfig != null) {
      _retryConfigs[operationType] = retryConfig;
    }
    _loggingService.info('Registered handler for operation type: $operationType');
  }

  /// 移除队列中的操作
  bool removeOperation(String operationId) {
    final index = _operationQueue.indexWhere((op) => op.id == operationId);
    if (index != -1) {
      final operation = _operationQueue.removeAt(index);
      _saveQueue();
      
      _notificationService.notify(
        'offline_queue',
        'operation_removed',
        {'operationId': operationId},
      );
      
      _loggingService.info('Operation removed from queue: $operationId');
      return true;
    }
    return false;
  }

  /// 清空队列
  Future<void> clearQueue() async {
    final count = _operationQueue.length;
    _operationQueue.clear();
    await _saveQueue();
    
    _notificationService.notify(
      'offline_queue',
      'queue_cleared',
      {'removedCount': count},
    );
    
    _loggingService.info('Queue cleared, removed $count operations');
  }

  /// 获取队列状态
  QueueStatus getQueueStatus() {
    final pendingCount = _operationQueue.where((op) => op.status == OperationStatus.pending).length;
    final failedCount = _operationQueue.where((op) => op.status == OperationStatus.failed).length;
    final processingCount = _operationQueue.where((op) => op.status == OperationStatus.processing).length;
    
    return QueueStatus(
      totalOperations: _operationQueue.length,
      pendingOperations: pendingCount,
      failedOperations: failedCount,
      processingOperations: processingCount,
      isOnline: _networkService.isConnected,
      isProcessing: _isProcessing,
      lastProcessedAt: _getLastProcessedTime(),
    );
  }

  /// 获取队列中的操作列表
  List<QueuedOperation> getOperations({OperationStatus? status}) {
    if (status == null) {
      return List.unmodifiable(_operationQueue);
    }
    return _operationQueue.where((op) => op.status == status).toList();
  }

  /// 重试失败的操作
  Future<void> retryFailedOperations() async {
    final failedOperations = _operationQueue
        .where((op) => op.status == OperationStatus.failed)
        .toList();
    
    for (final operation in failedOperations) {
      operation.status = OperationStatus.pending;
      operation.attempts = 0;
      operation.lastError = null;
      operation.nextRetryAt = null;
    }
    
    await _saveQueue();
    
    _notificationService.notify(
      'offline_queue',
      'failed_operations_reset',
      {'count': failedOperations.length},
    );
    
    _loggingService.info('Reset ${failedOperations.length} failed operations for retry');
  }

  /// 处理网络状态变化
  void _handleNetworkChange(NetworkStatus status) {
    if (status.isConnected && !_isProcessing) {
      _loggingService.info('Network connected, starting queue processing');
      _processQueue();
    } else if (!status.isConnected) {
      _loggingService.info('Network disconnected, operations will be queued');
    }
  }

  /// 开始处理队列
  void _startProcessing() {
    _processingTimer = Timer.periodic(_processingInterval, (_) async {
      if (_networkService.isConnected && !_isProcessing) {
        await _processQueue();
      }
    });
  }

  /// 处理队列中的操作
  Future<void> _processQueue() async {
    if (_isProcessing || !_networkService.isConnected) return;
    
    _isProcessing = true;
    
    try {
      final pendingOperations = _operationQueue
          .where((op) => op.status == OperationStatus.pending || 
                        (op.status == OperationStatus.failed && _shouldRetry(op)))
          .toList();
      
      if (pendingOperations.isEmpty) {
        return;
      }
      
      _notificationService.notify(
        'offline_queue',
        'processing_started',
        {'operationCount': pendingOperations.length},
      );
      
      for (final operation in pendingOperations) {
        if (!_networkService.isConnected) {
          _loggingService.info('Network disconnected during processing, stopping');
          break;
        }
        
        await _processOperation(operation);
      }
      
      await _saveQueue();
      
    } catch (e, stackTrace) {
      _loggingService.error('Error during queue processing', e, stackTrace);
    } finally {
      _isProcessing = false;
      
      _notificationService.notify(
        'offline_queue',
        'processing_completed',
        {'queueStatus': getQueueStatus().toJson()},
      );
    }
  }

  /// 处理单个操作
  Future<void> _processOperation(QueuedOperation operation) async {
    try {
      operation.status = OperationStatus.processing;
      operation.attempts++;
      operation.lastAttemptAt = DateTime.now();
      
      final handler = _handlers[operation.type];
      if (handler == null) {
        throw Exception('No handler registered for operation type: ${operation.type}');
      }
      
      _loggingService.info('Processing operation: ${operation.type} (${operation.id})');
      
      // 检查超时
      if (_isOperationExpired(operation)) {
        throw TimeoutException('Operation expired');
      }
      
      // 执行操作
      final result = await handler.execute(operation).timeout(operation.timeout);
      
      if (result.success) {
        operation.status = OperationStatus.completed;
        operation.result = result.data;
        operation.completedAt = DateTime.now();
        
        _notificationService.notify(
          'offline_queue',
          'operation_completed',
          {
            'operationId': operation.id,
            'type': operation.type,
            'result': result.data,
          },
        );
        
        _loggingService.info('Operation completed successfully: ${operation.id}');
      } else {
        throw Exception(result.error ?? 'Operation failed');
      }
      
    } catch (e, stackTrace) {
      _loggingService.error('Operation failed: ${operation.id}', e, stackTrace);
      
      operation.lastError = e.toString();
      
      final retryConfig = _retryConfigs[operation.type] ?? RetryConfig.defaultConfig();
      
      if (operation.attempts < retryConfig.maxAttempts) {
        operation.status = OperationStatus.failed;
        operation.nextRetryAt = DateTime.now().add(
          Duration(milliseconds: (retryConfig.baseDelay.inMilliseconds * 
              pow(retryConfig.backoffMultiplier, operation.attempts - 1)).round())
        );
        
        _loggingService.info('Operation will be retried: ${operation.id} (attempt ${operation.attempts}/${retryConfig.maxAttempts})');
      } else {
        operation.status = OperationStatus.failed;
        operation.nextRetryAt = null;
        
        _notificationService.notify(
          'offline_queue',
          'operation_failed_permanently',
          {
            'operationId': operation.id,
            'type': operation.type,
            'error': e.toString(),
          },
        );
        
        _loggingService.warning('Operation failed permanently: ${operation.id}');
      }
    }
  }

  /// 检查操作是否应该重试
  bool _shouldRetry(QueuedOperation operation) {
    if (operation.nextRetryAt == null) return false;
    return DateTime.now().isAfter(operation.nextRetryAt!);
  }

  /// 检查操作是否已过期
  bool _isOperationExpired(QueuedOperation operation) {
    final expirationTime = operation.createdAt.add(operation.timeout);
    return DateTime.now().isAfter(expirationTime);
  }

  /// 按优先级排序队列
  void _sortQueueByPriority() {
    _operationQueue.sort((a, b) {
      // 优先级排序
      final priorityCompare = b.priority.index.compareTo(a.priority.index);
      if (priorityCompare != 0) return priorityCompare;
      
      // 相同优先级按创建时间排序
      return a.createdAt.compareTo(b.createdAt);
    });
  }

  /// 清理旧操作
  Future<void> _cleanupOldOperations() async {
    final cutoffTime = DateTime.now().subtract(const Duration(days: 7));
    final initialCount = _operationQueue.length;
    
    _operationQueue.removeWhere((op) => 
        (op.status == OperationStatus.completed || op.status == OperationStatus.failed) &&
        op.createdAt.isBefore(cutoffTime));
    
    final removedCount = initialCount - _operationQueue.length;
    if (removedCount > 0) {
      _loggingService.info('Cleaned up $removedCount old operations');
    }
  }

  /// 注册默认操作处理器
  void _registerDefaultHandlers() {
    // 数据同步操作
    registerHandler('sync_data', DataSyncHandler(), 
        retryConfig: RetryConfig(maxAttempts: 5, baseDelay: const Duration(seconds: 5)));
    
    // 设置更新操作
    registerHandler('update_settings', SettingsUpdateHandler(),
        retryConfig: RetryConfig(maxAttempts: 3, baseDelay: const Duration(seconds: 2)));
    
    // 文件上传操作
    registerHandler('upload_file', FileUploadHandler(),
        retryConfig: RetryConfig(maxAttempts: 3, baseDelay: const Duration(seconds: 10)));
  }

  /// 保存队列到持久存储
  Future<void> _saveQueue() async {
    try {
      final queueData = _operationQueue.map((op) => op.toJson()).toList();
      _globalStateManager.setState('offline_queue', queueData);
    } catch (e, stackTrace) {
      _loggingService.error('Failed to save queue', e, stackTrace);
    }
  }

  /// 从持久存储恢复队列
  Future<void> _restoreQueue() async {
    try {
      final queueData = _globalStateManager.getState<List<dynamic>>('offline_queue');
      if (queueData != null) {
        _operationQueue.clear();
        for (final operationData in queueData) {
          try {
            final operation = QueuedOperation.fromJson(operationData);
            _operationQueue.add(operation);
          } catch (e) {
            _loggingService.warning('Failed to restore operation: $e');
          }
        }
        _sortQueueByPriority();
        _loggingService.info('Restored ${_operationQueue.length} operations from storage');
      }
    } catch (e, stackTrace) {
      _loggingService.error('Failed to restore queue', e, stackTrace);
    }
  }

  /// 获取最后处理时间
  DateTime? _getLastProcessedTime() {
    final completedOps = _operationQueue
        .where((op) => op.status == OperationStatus.completed)
        .toList();
    
    if (completedOps.isEmpty) return null;
    
    completedOps.sort((a, b) => b.completedAt!.compareTo(a.completedAt!));
    return completedOps.first.completedAt;
  }

  /// 生成操作ID
  String _generateOperationId() {
    return 'op_${DateTime.now().millisecondsSinceEpoch}_${Random().nextInt(9999)}';
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      _processingTimer?.cancel();
      await _saveQueue();
      _operationQueue.clear();
      _handlers.clear();
      _retryConfigs.clear();
      _initialized = false;
      _loggingService.info('OfflineQueueManager disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during OfflineQueueManager disposal', e, stackTrace);
    }
  }
}

/// 队列中的操作
class QueuedOperation {
  final String id;
  final String type;
  final Map<String, dynamic> data;
  final OperationPriority priority;
  final DateTime createdAt;
  final Duration timeout;
  final Map<String, String> metadata;
  
  OperationStatus status;
  int attempts;
  String? lastError;
  DateTime? lastAttemptAt;
  DateTime? nextRetryAt;
  DateTime? completedAt;
  Map<String, dynamic>? result;

  QueuedOperation({
    required this.id,
    required this.type,
    required this.data,
    required this.priority,
    required this.status,
    required this.createdAt,
    required this.attempts,
    required this.timeout,
    required this.metadata,
    this.lastError,
    this.lastAttemptAt,
    this.nextRetryAt,
    this.completedAt,
    this.result,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'type': type,
    'data': data,
    'priority': priority.name,
    'status': status.name,
    'createdAt': createdAt.toIso8601String(),
    'timeout': timeout.inMilliseconds,
    'metadata': metadata,
    'attempts': attempts,
    'lastError': lastError,
    'lastAttemptAt': lastAttemptAt?.toIso8601String(),
    'nextRetryAt': nextRetryAt?.toIso8601String(),
    'completedAt': completedAt?.toIso8601String(),
    'result': result,
  };

  factory QueuedOperation.fromJson(Map<String, dynamic> json) => QueuedOperation(
    id: json['id'],
    type: json['type'],
    data: Map<String, dynamic>.from(json['data']),
    priority: OperationPriority.values.byName(json['priority']),
    status: OperationStatus.values.byName(json['status']),
    createdAt: DateTime.parse(json['createdAt']),
    timeout: Duration(milliseconds: json['timeout']),
    metadata: Map<String, String>.from(json['metadata']),
    attempts: json['attempts'],
    lastError: json['lastError'],
    lastAttemptAt: json['lastAttemptAt'] != null ? DateTime.parse(json['lastAttemptAt']) : null,
    nextRetryAt: json['nextRetryAt'] != null ? DateTime.parse(json['nextRetryAt']) : null,
    completedAt: json['completedAt'] != null ? DateTime.parse(json['completedAt']) : null,
    result: json['result'],
  );
}

/// 队列状态
class QueueStatus {
  final int totalOperations;
  final int pendingOperations;
  final int failedOperations;
  final int processingOperations;
  final bool isOnline;
  final bool isProcessing;
  final DateTime? lastProcessedAt;

  const QueueStatus({
    required this.totalOperations,
    required this.pendingOperations,
    required this.failedOperations,
    required this.processingOperations,
    required this.isOnline,
    required this.isProcessing,
    this.lastProcessedAt,
  });

  Map<String, dynamic> toJson() => {
    'totalOperations': totalOperations,
    'pendingOperations': pendingOperations,
    'failedOperations': failedOperations,
    'processingOperations': processingOperations,
    'isOnline': isOnline,
    'isProcessing': isProcessing,
    'lastProcessedAt': lastProcessedAt?.toIso8601String(),
  };
}

/// 操作处理器接口
abstract class OperationHandler {
  Future<OperationResult> execute(QueuedOperation operation);
}

/// 操作结果
class OperationResult {
  final bool success;
  final Map<String, dynamic>? data;
  final String? error;

  const OperationResult({
    required this.success,
    this.data,
    this.error,
  });

  factory OperationResult.success([Map<String, dynamic>? data]) =>
      OperationResult(success: true, data: data);

  factory OperationResult.failure(String error) =>
      OperationResult(success: false, error: error);
}

/// 重试配置
class RetryConfig {
  final int maxAttempts;
  final Duration baseDelay;
  final double backoffMultiplier;

  const RetryConfig({
    required this.maxAttempts,
    required this.baseDelay,
    this.backoffMultiplier = 2.0,
  });

  factory RetryConfig.defaultConfig() => const RetryConfig(
    maxAttempts: 3,
    baseDelay: Duration(seconds: 2),
    backoffMultiplier: 2.0,
  );
}

/// 默认操作处理器实现

/// 数据同步处理器
class DataSyncHandler implements OperationHandler {
  @override
  Future<OperationResult> execute(QueuedOperation operation) async {
    try {
      // 模拟数据同步操作
      await Future.delayed(const Duration(seconds: 1));
      
      final data = operation.data;
      // 这里应该实现实际的数据同步逻辑
      
      return OperationResult.success({'synced': true, 'timestamp': DateTime.now().toIso8601String()});
    } catch (e) {
      return OperationResult.failure(e.toString());
    }
  }
}

/// 设置更新处理器
class SettingsUpdateHandler implements OperationHandler {
  @override
  Future<OperationResult> execute(QueuedOperation operation) async {
    try {
      // 模拟设置更新操作
      await Future.delayed(const Duration(milliseconds: 500));
      
      final settings = operation.data;
      // 这里应该实现实际的设置更新逻辑
      
      return OperationResult.success({'updated': true});
    } catch (e) {
      return OperationResult.failure(e.toString());
    }
  }
}

/// 文件上传处理器
class FileUploadHandler implements OperationHandler {
  @override
  Future<OperationResult> execute(QueuedOperation operation) async {
    try {
      // 模拟文件上传操作
      await Future.delayed(const Duration(seconds: 2));
      
      final fileData = operation.data;
      // 这里应该实现实际的文件上传逻辑
      
      return OperationResult.success({'uploaded': true, 'url': 'https://example.com/file'});
    } catch (e) {
      return OperationResult.failure(e.toString());
    }
  }
}

/// 枚举定义
enum OperationPriority { low, normal, high, critical }
enum OperationStatus { pending, processing, completed, failed }