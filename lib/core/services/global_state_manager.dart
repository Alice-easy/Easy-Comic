import 'dart:async';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../error/failures.dart';
import 'logging_service.dart';

/// 全局状态管理器 - 管理跨页面状态同步和持久化
class GlobalStateManager {
  static GlobalStateManager? _instance;
  static GlobalStateManager get instance => _instance ??= GlobalStateManager._();
  
  GlobalStateManager._();

  final LoggingService _loggingService = LoggingService();
  final Map<String, dynamic> _globalState = {};
  final Map<String, StreamController<dynamic>> _stateStreams = {};
  final Map<String, Timer> _persistenceTimers = {};
  SharedPreferences? _preferences;

  /// 状态变化事件通知
  final StreamController<StateChangeEvent> _stateChangeController = 
      StreamController<StateChangeEvent>.broadcast();
  
  /// 状态变化流
  Stream<StateChangeEvent> get stateChanges => _stateChangeController.stream;

  /// 初始化全局状态管理器
  Future<void> initialize() async {
    try {
      _preferences = await SharedPreferences.getInstance();
      await _loadPersistedState();
      _loggingService.info('GlobalStateManager initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize GlobalStateManager', e, stackTrace);
      rethrow;
    }
  }

  /// 设置状态值
  void setState<T>(String key, T value, {bool persist = true, bool notify = true}) {
    try {
      final oldValue = _globalState[key];
      _globalState[key] = value;

      if (notify) {
        // 发出特定状态流的通知
        if (_stateStreams.containsKey(key)) {
          _stateStreams[key]!.add(value);
        }

        // 发出全局状态变化事件
        _stateChangeController.add(StateChangeEvent(
          key: key,
          newValue: value,
          oldValue: oldValue,
          timestamp: DateTime.now(),
        ));
      }

      if (persist) {
        _schedulePersistence(key, value);
      }

      _loggingService.debug('State updated: $key = $value');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to set state for key: $key', e, stackTrace);
    }
  }

  /// 获取状态值
  T? getState<T>(String key) {
    try {
      return _globalState[key] as T?;
    } catch (e) {
      _loggingService.warning('Failed to get state for key: $key, error: $e');
      return null;
    }
  }

  /// 监听特定状态的变化
  Stream<T> watchState<T>(String key) {
    if (!_stateStreams.containsKey(key)) {
      _stateStreams[key] = StreamController<dynamic>.broadcast();
    }
    return _stateStreams[key]!.stream.cast<T>();
  }

  /// 批量设置状态
  void setStates(Map<String, dynamic> states, {bool persist = true, bool notify = true}) {
    for (final entry in states.entries) {
      setState(entry.key, entry.value, persist: persist, notify: notify);
    }
  }

  /// 移除状态
  void removeState(String key, {bool persist = true, bool notify = true}) {
    try {
      final oldValue = _globalState.remove(key);
      
      if (notify && oldValue != null) {
        _stateChangeController.add(StateChangeEvent(
          key: key,
          newValue: null,
          oldValue: oldValue,
          timestamp: DateTime.now(),
        ));
      }

      if (persist && _preferences != null) {
        _preferences!.remove('state_$key');
      }

      // 关闭对应的流控制器
      if (_stateStreams.containsKey(key)) {
        _stateStreams[key]!.close();
        _stateStreams.remove(key);
      }

      _loggingService.debug('State removed: $key');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to remove state for key: $key', e, stackTrace);
    }
  }

  /// 清空所有状态
  void clearAllStates({bool persist = true, bool notify = true}) {
    try {
      final keys = _globalState.keys.toList();
      for (final key in keys) {
        removeState(key, persist: persist, notify: notify);
      }
      _loggingService.info('All states cleared');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to clear all states', e, stackTrace);
    }
  }

  /// 获取所有状态的快照
  Map<String, dynamic> getStateSnapshot() {
    return Map.from(_globalState);
  }

  /// 恢复状态快照
  void restoreStateSnapshot(Map<String, dynamic> snapshot, {bool persist = true, bool notify = true}) {
    try {
      clearAllStates(persist: false, notify: false);
      setStates(snapshot, persist: persist, notify: notify);
      _loggingService.info('State snapshot restored with ${snapshot.length} items');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to restore state snapshot', e, stackTrace);
    }
  }

  /// 调度持久化
  void _schedulePersistence(String key, dynamic value) {
    // 取消之前的定时器
    _persistenceTimers[key]?.cancel();
    
    // 设置新的定时器，延迟500ms执行持久化
    _persistenceTimers[key] = Timer(const Duration(milliseconds: 500), () {
      _persistState(key, value);
      _persistenceTimers.remove(key);
    });
  }

  /// 持久化状态到本地存储
  Future<void> _persistState(String key, dynamic value) async {
    try {
      if (_preferences == null) return;

      if (value == null) {
        await _preferences!.remove('state_$key');
      } else {
        final jsonString = json.encode(_serializeValue(value));
        await _preferences!.setString('state_$key', jsonString);
      }
    } catch (e, stackTrace) {
      _loggingService.error('Failed to persist state for key: $key', e, stackTrace);
    }
  }

  /// 加载持久化的状态
  Future<void> _loadPersistedState() async {
    try {
      if (_preferences == null) return;

      final keys = _preferences!.getKeys()
          .where((key) => key.startsWith('state_'))
          .toList();

      for (final prefKey in keys) {
        final stateKey = prefKey.substring(6); // 移除 'state_' 前缀
        final jsonString = _preferences!.getString(prefKey);
        
        if (jsonString != null) {
          try {
            final value = json.decode(jsonString);
            _globalState[stateKey] = _deserializeValue(value);
          } catch (e) {
            _loggingService.warning('Failed to deserialize state for key: $stateKey, removing...');
            await _preferences!.remove(prefKey);
          }
        }
      }

      _loggingService.info('Loaded ${_globalState.length} persisted states');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to load persisted state', e, stackTrace);
    }
  }

  /// 序列化值以便JSON存储
  dynamic _serializeValue(dynamic value) {
    if (value is DateTime) {
      return {'_type': 'DateTime', '_value': value.toIso8601String()};
    } else if (value is Duration) {
      return {'_type': 'Duration', '_value': value.inMicroseconds};
    } else if (value is Enum) {
      return {'_type': 'Enum', '_class': value.runtimeType.toString(), '_value': value.name};
    }
    return value;
  }

  /// 反序列化值
  dynamic _deserializeValue(dynamic value) {
    if (value is Map && value.containsKey('_type')) {
      switch (value['_type']) {
        case 'DateTime':
          return DateTime.parse(value['_value']);
        case 'Duration':
          return Duration(microseconds: value['_value']);
        // Enum 反序列化需要具体的枚举类型，这里先返回原值
        case 'Enum':
          return value['_value'];
      }
    }
    return value;
  }

  /// 强制持久化所有状态
  Future<void> flushAllStates() async {
    try {
      // 取消所有定时器并立即执行持久化
      for (final timer in _persistenceTimers.values) {
        timer.cancel();
      }
      _persistenceTimers.clear();

      for (final entry in _globalState.entries) {
        await _persistState(entry.key, entry.value);
      }
      
      _loggingService.info('All states flushed to persistence');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to flush all states', e, stackTrace);
    }
  }

  /// 检查状态是否存在
  bool hasState(String key) {
    return _globalState.containsKey(key);
  }

  /// 获取状态键列表
  List<String> getStateKeys() {
    return _globalState.keys.toList();
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      // 强制持久化所有状态
      await flushAllStates();
      
      // 关闭所有流控制器
      for (final controller in _stateStreams.values) {
        await controller.close();
      }
      _stateStreams.clear();
      
      // 关闭状态变化流
      await _stateChangeController.close();
      
      // 取消所有定时器
      for (final timer in _persistenceTimers.values) {
        timer.cancel();
      }
      _persistenceTimers.clear();
      
      _loggingService.info('GlobalStateManager disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during GlobalStateManager disposal', e, stackTrace);
    }
  }
}

/// 状态变化事件
class StateChangeEvent {
  final String key;
  final dynamic newValue;
  final dynamic oldValue;
  final DateTime timestamp;

  const StateChangeEvent({
    required this.key,
    required this.newValue,
    required this.oldValue,
    required this.timestamp,
  });

  @override
  String toString() {
    return 'StateChangeEvent{key: $key, newValue: $newValue, oldValue: $oldValue, timestamp: $timestamp}';
  }
}

/// 全局状态管理扩展
extension GlobalStateExtensions on GlobalStateManager {
  /// 获取或设置状态的简便方法
  T call<T>(String key, [T? value]) {
    if (value != null) {
      setState(key, value);
    }
    return getState<T>(key) ?? value as T;
  }
}