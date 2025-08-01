import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/foundation.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';
import 'real_time_notification_service.dart';
import 'cross_page_sync_service.dart';

/// 数据流优化器 - 优化页面间数据同步性能
class DataFlowOptimizer {
  static DataFlowOptimizer? _instance;
  static DataFlowOptimizer get instance => _instance ??= DataFlowOptimizer._();
  
  DataFlowOptimizer._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  final RealTimeNotificationService _notificationService = RealTimeNotificationService.instance;
  final CrossPageSyncService _crossPageSyncService = CrossPageSyncService.instance;
  
  final Map<String, DataFlowRoute> _routes = {};
  final Map<String, DataDependency> _dependencies = {};
  final Map<String, DataVersionControl> _versionControls = {};
  final Map<String, Timer> _debounceTimers = {};
  final List<DataChangeEvent> _pendingChanges = [];
  
  Timer? _batchProcessingTimer;
  bool _initialized = false;
  
  // 性能配置
  Duration _batchProcessingInterval = const Duration(milliseconds: 50);
  Duration _debounceDelay = const Duration(milliseconds: 100);
  int _maxBatchSize = 50;
  int _maxHistoryLength = 100;

  /// 初始化数据流优化器
  Future<void> initialize() async {
    if (_initialized) return;

    try {
      // 创建数据流事件通道
      _notificationService.createChannel('data_flow');
      
      // 开始批量处理
      _startBatchProcessing();
      
      // 注册默认路由
      _registerDefaultRoutes();
      
      _initialized = true;
      _loggingService.info('DataFlowOptimizer initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize DataFlowOptimizer', e, stackTrace);
      rethrow;
    }
  }

  /// 注册数据流路由
  void registerRoute({
    required String routeId,
    required String sourceKey,
    required List<String> targetKeys,
    SyncStrategy strategy = SyncStrategy.realTime,
    DataTransform? transform,
    ConflictResolution conflictResolution = ConflictResolution.lastWriteWins,
    List<String> dependencies = const [],
  }) {
    final route = DataFlowRoute(
      id: routeId,
      sourceKey: sourceKey,
      targetKeys: targetKeys,
      strategy: strategy,
      transform: transform,
      conflictResolution: conflictResolution,
      dependencies: dependencies,
      createdAt: DateTime.now(),
    );

    _routes[routeId] = route;
    
    // 注册依赖关系
    for (final dep in dependencies) {
      _registerDependency(sourceKey, dep);
    }
    
    _loggingService.info('Data flow route registered: $routeId');
  }

  /// 注册数据依赖关系
  void _registerDependency(String dependentKey, String dependencyKey) {
    final dependency = _dependencies[dependentKey] ??= DataDependency(
      key: dependentKey,
      dependencies: [],
      dependents: [],
    );
    
    if (!dependency.dependencies.contains(dependencyKey)) {
      dependency.dependencies.add(dependencyKey);
    }
    
    final reverseDependency = _dependencies[dependencyKey] ??= DataDependency(
      key: dependencyKey,
      dependencies: [],
      dependents: [],
    );
    
    if (!reverseDependency.dependents.contains(dependentKey)) {
      reverseDependency.dependents.add(dependentKey);
    }
  }

  /// 优化数据同步
  Future<void> optimizeSync({
    required String dataKey,
    required dynamic newValue,
    dynamic oldValue,
    String? sourcePageId,
    Map<String, dynamic>? metadata,
  }) async {
    try {
      final changeEvent = DataChangeEvent(
        id: _generateEventId(),
        dataKey: dataKey,
        newValue: newValue,
        oldValue: oldValue,
        sourcePageId: sourcePageId,
        metadata: metadata ?? {},
        timestamp: DateTime.now(),
      );

      // 检查是否需要防抖处理
      final routesForKey = _getRoutesForSourceKey(dataKey);
      final needsDebounce = routesForKey.any((route) => route.strategy == SyncStrategy.debounced);
      
      if (needsDebounce) {
        _handleDebouncedSync(changeEvent);
      } else {
        await _processChangeEvent(changeEvent);
      }
      
    } catch (e, stackTrace) {
      _loggingService.error('Failed to optimize sync for $dataKey', e, stackTrace);
    }
  }

  /// 处理防抖同步
  void _handleDebouncedSync(DataChangeEvent changeEvent) {
    final timer = _debounceTimers[changeEvent.dataKey];
    timer?.cancel();
    
    _debounceTimers[changeEvent.dataKey] = Timer(_debounceDelay, () async {
      await _processChangeEvent(changeEvent);
      _debounceTimers.remove(changeEvent.dataKey);
    });
  }

  /// 处理数据变更事件
  Future<void> _processChangeEvent(DataChangeEvent changeEvent) async {
    try {
      // 检查版本控制
      await _updateVersionControl(changeEvent);
      
      // 检查依赖关系
      final dependencyChain = _resolveDependencies(changeEvent.dataKey);
      
      // 获取相关的同步路由
      final routes = _getRoutesForSourceKey(changeEvent.dataKey);
      
      if (routes.isEmpty) {
        _loggingService.debug('No routes found for data key: ${changeEvent.dataKey}');
        return;
      }
      
      // 根据策略处理同步
      for (final route in routes) {
        await _executeSyncRoute(route, changeEvent, dependencyChain);
      }
      
      _notificationService.notify(
        'data_flow',
        'sync_optimized',
        {
          'dataKey': changeEvent.dataKey,
          'routeCount': routes.length,
          'dependencyCount': dependencyChain.length,
        },
      );
      
    } catch (e, stackTrace) {
      _loggingService.error('Failed to process change event: ${changeEvent.id}', e, stackTrace);
    }
  }

  /// 执行同步路由
  Future<void> _executeSyncRoute(
    DataFlowRoute route,
    DataChangeEvent changeEvent,
    List<String> dependencyChain,
  ) async {
    try {
      dynamic syncValue = changeEvent.newValue;
      
      // 应用数据转换
      if (route.transform != null) {
        syncValue = await route.transform!(syncValue, changeEvent.metadata);
      }
      
      // 检查冲突
      final conflictResult = await _checkConflicts(route, changeEvent, syncValue);
      if (conflictResult.hasConflict) {
        syncValue = await _resolveConflict(route, conflictResult, syncValue);
      }
      
      // 根据策略执行同步
      switch (route.strategy) {
        case SyncStrategy.realTime:
          await _executeRealTimeSync(route, syncValue, changeEvent);
          break;
        case SyncStrategy.batched:
          _addToBatch(route, syncValue, changeEvent);
          break;
        case SyncStrategy.debounced:
          await _executeRealTimeSync(route, syncValue, changeEvent);
          break;
        case SyncStrategy.lazy:
          await _executeLazySync(route, syncValue, changeEvent);
          break;
      }
      
    } catch (e, stackTrace) {
      _loggingService.error('Failed to execute sync route: ${route.id}', e, stackTrace);
    }
  }

  /// 执行实时同步
  Future<void> _executeRealTimeSync(
    DataFlowRoute route,
    dynamic value,
    DataChangeEvent changeEvent,
  ) async {
    for (final targetKey in route.targetKeys) {
      await _crossPageSyncService.syncData(
        route.sourceKey,
        targetKey,
        value,
        metadata: SyncMetadata(
          source: route.id,
          customData: {
            ...changeEvent.metadata,
            'routeId': route.id,
            'syncStrategy': route.strategy.name,
            'timestamp': changeEvent.timestamp.toIso8601String(),
          },
        ),
      );
    }
  }

  /// 添加到批处理队列
  void _addToBatch(DataFlowRoute route, dynamic value, DataChangeEvent changeEvent) {
    _pendingChanges.add(changeEvent.copyWith(
      metadata: {
        ...changeEvent.metadata,
        'routeId': route.id,
        'targetKeys': route.targetKeys,
        'processedValue': value,
      },
    ));
    
    // 检查是否达到最大批量大小
    if (_pendingChanges.length >= _maxBatchSize) {
      _processBatch();
    }
  }

  /// 执行延迟同步
  Future<void> _executeLazySync(
    DataFlowRoute route,
    dynamic value,
    DataChangeEvent changeEvent,
  ) async {
    // 延迟同步通常在页面激活时执行
    // 这里先记录待同步数据
    final lazyData = {
      'routeId': route.id,
      'value': value,
      'timestamp': changeEvent.timestamp.toIso8601String(),
      'targetKeys': route.targetKeys,
    };
    
    _globalStateManager.setState('lazy_sync_${route.id}', lazyData);
  }

  /// 检查数据冲突
  Future<ConflictCheckResult> _checkConflicts(
    DataFlowRoute route,
    DataChangeEvent changeEvent,
    dynamic value,
  ) async {
    final conflicts = <DataConflict>[];
    
    for (final targetKey in route.targetKeys) {
      final currentValue = _globalStateManager.getState(targetKey);
      if (currentValue != null && currentValue != value) {
        // 检查时间戳
        final currentMetadata = _globalStateManager.getState('${targetKey}_metadata');
        final currentTimestamp = currentMetadata?['timestamp'];
        
        if (currentTimestamp != null) {
          final currentTime = DateTime.parse(currentTimestamp);
          if (currentTime.isAfter(changeEvent.timestamp)) {
            conflicts.add(DataConflict(
              targetKey: targetKey,
              currentValue: currentValue,
              newValue: value,
              currentTimestamp: currentTime,
              newTimestamp: changeEvent.timestamp,
            ));
          }
        }
      }
    }
    
    return ConflictCheckResult(
      hasConflict: conflicts.isNotEmpty,
      conflicts: conflicts,
    );
  }

  /// 解决数据冲突
  Future<dynamic> _resolveConflict(
    DataFlowRoute route,
    ConflictCheckResult conflictResult,
    dynamic value,
  ) async {
    switch (route.conflictResolution) {
      case ConflictResolution.lastWriteWins:
        return value; // 新值获胜
      
      case ConflictResolution.firstWriteWins:
        return conflictResult.conflicts.first.currentValue; // 保持当前值
      
      case ConflictResolution.merge:
        return await _mergeValues(value, conflictResult.conflicts.first.currentValue);
      
      case ConflictResolution.manual:
        // 发送冲突通知，等待手动解决
        _notificationService.notify(
          'data_flow',
          'conflict_detected',
          {
            'routeId': route.id,
            'conflicts': conflictResult.conflicts.map((c) => c.toJson()).toList(),
          },
        );
        return conflictResult.conflicts.first.currentValue; // 临时保持当前值
    }
  }

  /// 合并值
  Future<dynamic> _mergeValues(dynamic newValue, dynamic currentValue) async {
    if (newValue is Map && currentValue is Map) {
      final merged = Map<String, dynamic>.from(currentValue);
      for (final entry in (newValue as Map<String, dynamic>).entries) {
        merged[entry.key] = entry.value;
      }
      return merged;
    }
    
    if (newValue is List && currentValue is List) {
      final merged = List.from(currentValue);
      merged.addAll(newValue as List);
      return merged;
    }
    
    return newValue; // 无法合并时使用新值
  }

  /// 解析依赖关系
  List<String> _resolveDependencies(String dataKey) {
    final resolved = <String>[];
    final visiting = <String>{};
    
    void _visit(String key) {
      if (resolved.contains(key) || visiting.contains(key)) return;
      
      visiting.add(key);
      final dependency = _dependencies[key];
      
      if (dependency != null) {
        for (final dep in dependency.dependencies) {
          _visit(dep);
        }
      }
      
      visiting.remove(key);
      resolved.add(key);
    }
    
    _visit(dataKey);
    return resolved;
  }

  /// 获取数据键对应的路由
  List<DataFlowRoute> _getRoutesForSourceKey(String sourceKey) {
    return _routes.values
        .where((route) => route.sourceKey == sourceKey)
        .toList();
  }

  /// 更新版本控制
  Future<void> _updateVersionControl(DataChangeEvent changeEvent) async {
    final versionControl = _versionControls[changeEvent.dataKey] ??= DataVersionControl(
      dataKey: changeEvent.dataKey,
      currentVersion: 0,
      history: [],
    );
    
    versionControl.currentVersion++;
    versionControl.history.add(DataVersion(
      version: versionControl.currentVersion,
      value: changeEvent.newValue,
      timestamp: changeEvent.timestamp,
      sourcePageId: changeEvent.sourcePageId,
      metadata: changeEvent.metadata,
    ));
    
    // 限制历史记录长度
    if (versionControl.history.length > _maxHistoryLength) {
      versionControl.history.removeAt(0);
    }
    
    _versionControls[changeEvent.dataKey] = versionControl;
  }

  /// 开始批量处理
  void _startBatchProcessing() {
    _batchProcessingTimer = Timer.periodic(_batchProcessingInterval, (_) {
      if (_pendingChanges.isNotEmpty) {
        _processBatch();
      }
    });
  }

  /// 处理批量数据
  void _processBatch() {
    if (_pendingChanges.isEmpty) return;
    
    try {
      final batch = List<DataChangeEvent>.from(_pendingChanges);
      _pendingChanges.clear();
      
      // 按路由分组
      final groupedByRoute = <String, List<DataChangeEvent>>{};
      for (final change in batch) {
        final routeId = change.metadata['routeId'] as String;
        groupedByRoute.putIfAbsent(routeId, () => []).add(change);
      }
      
      // 处理每个路由的批量数据
      for (final entry in groupedByRoute.entries) {
        final routeId = entry.key;
        final changes = entry.value;
        
        _processBatchForRoute(routeId, changes);
      }
      
      _notificationService.notify(
        'data_flow',
        'batch_processed',
        {
          'batchSize': batch.length,
          'routeCount': groupedByRoute.length,
        },
      );
      
    } catch (e, stackTrace) {
      _loggingService.error('Failed to process batch', e, stackTrace);
    }
  }

  /// 处理特定路由的批量数据
  Future<void> _processBatchForRoute(String routeId, List<DataChangeEvent> changes) async {
    final route = _routes[routeId];
    if (route == null) return;
    
    try {
      // 合并相同数据键的变更（只保留最新的）
      final mergedChanges = <String, DataChangeEvent>{};
      for (final change in changes) {
        mergedChanges[change.dataKey] = change;
      }
      
      // 批量同步
      for (final change in mergedChanges.values) {
        final value = change.metadata['processedValue'];
        final targetKeys = change.metadata['targetKeys'] as List<String>;
        
        for (final targetKey in targetKeys) {
          await _crossPageSyncService.syncData(
            route.sourceKey,
            targetKey,
            value,
            metadata: SyncMetadata(
              source: routeId,
              customData: {
                'routeId': routeId,
                'batchSize': changes.length,
                'timestamp': change.timestamp.toIso8601String(),
              },
            ),
          );
        }
      }
      
    } catch (e, stackTrace) {
      _loggingService.error('Failed to process batch for route: $routeId', e, stackTrace);
    }
  }

  /// 注册默认路由
  void _registerDefaultRoutes() {
    // 用户设置同步路由
    registerRoute(
      routeId: 'user_settings_sync',
      sourceKey: 'user_settings',
      targetKeys: ['settings_page', 'reader_page', 'bookshelf_page'],
      strategy: SyncStrategy.debounced,
      conflictResolution: ConflictResolution.lastWriteWins,
    );
    
    // 阅读进度同步路由
    registerRoute(
      routeId: 'reading_progress_sync',
      sourceKey: 'reading_progress',
      targetKeys: ['reader_page', 'bookshelf_page', 'history_page'],
      strategy: SyncStrategy.realTime,
      conflictResolution: ConflictResolution.lastWriteWins,
    );
    
    // 书签同步路由
    registerRoute(
      routeId: 'bookmarks_sync',
      sourceKey: 'bookmarks',
      targetKeys: ['reader_page', 'bookmarks_page'],
      strategy: SyncStrategy.batched,
      conflictResolution: ConflictResolution.merge,
    );
  }

  /// 获取性能统计
  DataFlowPerformanceStats getPerformanceStats() {
    return DataFlowPerformanceStats(
      activeRoutes: _routes.length,
      pendingChanges: _pendingChanges.length,
      activeDependencies: _dependencies.length,
      averageBatchSize: _pendingChanges.length > 0 ? _pendingChanges.length : 0,
      memoryUsage: _calculateMemoryUsage(),
    );
  }

  /// 计算内存使用量
  int _calculateMemoryUsage() {
    int size = 0;
    
    // 计算路由数据大小
    size += _routes.length * 200; // 估算每个路由200字节
    
    // 计算依赖关系大小
    size += _dependencies.length * 100; // 估算每个依赖100字节
    
    // 计算版本控制大小
    for (final vc in _versionControls.values) {
      size += vc.history.length * 300; // 估算每个版本300字节
    }
    
    // 计算待处理变更大小
    size += _pendingChanges.length * 150; // 估算每个变更150字节
    
    return size;
  }

  /// 生成事件ID
  String _generateEventId() {
    return 'event_${DateTime.now().millisecondsSinceEpoch}_${Random().nextInt(9999)}';
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      _batchProcessingTimer?.cancel();
      for (final timer in _debounceTimers.values) {
        timer.cancel();
      }
      
      _routes.clear();
      _dependencies.clear();
      _versionControls.clear();
      _debounceTimers.clear();
      _pendingChanges.clear();
      
      _initialized = false;
      _loggingService.info('DataFlowOptimizer disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during DataFlowOptimizer disposal', e, stackTrace);
    }
  }
}

/// 数据流路由
class DataFlowRoute {
  final String id;
  final String sourceKey;
  final List<String> targetKeys;
  final SyncStrategy strategy;
  final DataTransform? transform;
  final ConflictResolution conflictResolution;
  final List<String> dependencies;
  final DateTime createdAt;

  const DataFlowRoute({
    required this.id,
    required this.sourceKey,
    required this.targetKeys,
    required this.strategy,
    this.transform,
    required this.conflictResolution,
    required this.dependencies,
    required this.createdAt,
  });
}

/// 数据依赖关系
class DataDependency {
  final String key;
  final List<String> dependencies;
  final List<String> dependents;

  const DataDependency({
    required this.key,
    required this.dependencies,
    required this.dependents,
  });
}

/// 数据版本控制
class DataVersionControl {
  final String dataKey;
  int currentVersion;
  final List<DataVersion> history;

  DataVersionControl({
    required this.dataKey,
    required this.currentVersion,
    required this.history,
  });
}

/// 数据版本
class DataVersion {
  final int version;
  final dynamic value;
  final DateTime timestamp;
  final String? sourcePageId;
  final Map<String, dynamic> metadata;

  const DataVersion({
    required this.version,
    required this.value,
    required this.timestamp,
    this.sourcePageId,
    required this.metadata,
  });
}

/// 数据变更事件
class DataChangeEvent {
  final String id;
  final String dataKey;
  final dynamic newValue;
  final dynamic oldValue;
  final String? sourcePageId;
  final Map<String, dynamic> metadata;
  final DateTime timestamp;

  const DataChangeEvent({
    required this.id,
    required this.dataKey,
    required this.newValue,
    this.oldValue,
    this.sourcePageId,
    required this.metadata,
    required this.timestamp,
  });

  DataChangeEvent copyWith({
    String? id,
    String? dataKey,
    dynamic newValue,
    dynamic oldValue,
    String? sourcePageId,
    Map<String, dynamic>? metadata,
    DateTime? timestamp,
  }) {
    return DataChangeEvent(
      id: id ?? this.id,
      dataKey: dataKey ?? this.dataKey,
      newValue: newValue ?? this.newValue,
      oldValue: oldValue ?? this.oldValue,
      sourcePageId: sourcePageId ?? this.sourcePageId,
      metadata: metadata ?? this.metadata,
      timestamp: timestamp ?? this.timestamp,
    );
  }
}

/// 冲突检查结果
class ConflictCheckResult {
  final bool hasConflict;
  final List<DataConflict> conflicts;

  const ConflictCheckResult({
    required this.hasConflict,
    required this.conflicts,
  });
}

/// 数据冲突
class DataConflict {
  final String targetKey;
  final dynamic currentValue;
  final dynamic newValue;
  final DateTime currentTimestamp;
  final DateTime newTimestamp;

  const DataConflict({
    required this.targetKey,
    required this.currentValue,
    required this.newValue,
    required this.currentTimestamp,
    required this.newTimestamp,
  });

  Map<String, dynamic> toJson() => {
    'targetKey': targetKey,
    'currentValue': currentValue,
    'newValue': newValue,
    'currentTimestamp': currentTimestamp.toIso8601String(),
    'newTimestamp': newTimestamp.toIso8601String(),
  };
}

/// 性能统计
class DataFlowPerformanceStats {
  final int activeRoutes;
  final int pendingChanges;
  final int activeDependencies;
  final int averageBatchSize;
  final int memoryUsage;

  const DataFlowPerformanceStats({
    required this.activeRoutes,
    required this.pendingChanges,
    required this.activeDependencies,
    required this.averageBatchSize,
    required this.memoryUsage,
  });

  Map<String, dynamic> toJson() => {
    'activeRoutes': activeRoutes,
    'pendingChanges': pendingChanges,
    'activeDependencies': activeDependencies,
    'averageBatchSize': averageBatchSize,
    'memoryUsage': memoryUsage,
  };
}

/// 类型定义
typedef DataTransform = Future<dynamic> Function(dynamic value, Map<String, dynamic> metadata);

/// 枚举定义
enum SyncStrategy { realTime, batched, debounced, lazy }
enum ConflictResolution { lastWriteWins, firstWriteWins, merge, manual }