import 'dart:async';
import 'dart:convert';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../error/failures.dart';
import 'logging_service.dart';
import 'global_state_manager.dart';

/// BLoC状态持久化服务
class BlocPersistenceService {
  static BlocPersistenceService? _instance;
  static BlocPersistenceService get instance => _instance ??= BlocPersistenceService._();
  
  BlocPersistenceService._();

  final LoggingService _loggingService = LoggingService();
  final GlobalStateManager _globalStateManager = GlobalStateManager.instance;
  final Map<String, BlocPersistConfig> _blocConfigs = {};
  final Map<String, Timer> _saveTimers = {};
  SharedPreferences? _preferences;

  /// 初始化持久化服务
  Future<void> initialize() async {
    try {
      _preferences = await SharedPreferences.getInstance();
      _loggingService.info('BlocPersistenceService initialized successfully');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize BlocPersistenceService', e, stackTrace);
      rethrow;
    }
  }

  /// 注册BLoC持久化配置
  void registerBloc<T extends StateStreamable<S>, S>(
    String blocKey,
    T bloc, {
    required S Function(Map<String, dynamic>) fromJson,
    required Map<String, dynamic> Function(S) toJson,
    Duration saveDelay = const Duration(milliseconds: 500),
    bool autoSave = true,
    List<Type>? skipStates,
  }) {
    final config = BlocPersistConfig<T, S>(
      blocKey: blocKey,
      bloc: bloc,
      fromJson: fromJson,
      toJson: toJson,
      saveDelay: saveDelay,
      autoSave: autoSave,
      skipStates: skipStates ?? [],
    );

    _blocConfigs[blocKey] = config;

    if (autoSave) {
      _setupAutoSave(config);
    }

    _loggingService.debug('Registered BLoC for persistence: $blocKey');
  }

  /// 设置自动保存监听
  void _setupAutoSave<T extends StateStreamable<S>, S>(BlocPersistConfig<T, S> config) {
    config.bloc.stream.listen((state) {
      if (_shouldSkipState(state, config.skipStates)) {
        return;
      }

      _scheduleSave(config, state);
    });
  }

  /// 检查是否应该跳过保存此状态
  bool _shouldSkipState<S>(S state, List<Type> skipStates) {
    return skipStates.any((skipType) => state.runtimeType == skipType);
  }

  /// 调度保存状态
  void _scheduleSave<T extends StateStreamable<S>, S>(BlocPersistConfig<T, S> config, S state) {
    // 取消之前的定时器
    _saveTimers[config.blocKey]?.cancel();
    
    // 设置新的定时器
    _saveTimers[config.blocKey] = Timer(config.saveDelay, () {
      _saveState(config, state);
      _saveTimers.remove(config.blocKey);
    });
  }

  /// 保存BLoC状态
  Future<void> _saveState<T extends StateStreamable<S>, S>(
    BlocPersistConfig<T, S> config,
    S state,
  ) async {
    try {
      final stateData = config.toJson(state);
      final jsonString = json.encode(stateData);
      
      if (_preferences != null) {
        await _preferences!.setString('bloc_${config.blocKey}', jsonString);
      }
      
      // 同时保存到全局状态管理器
      _globalStateManager.setState('bloc_${config.blocKey}', stateData, persist: false);
      
      _loggingService.debug('Saved BLoC state: ${config.blocKey}');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to save BLoC state: ${config.blocKey}', e, stackTrace);
    }
  }

  /// 恢复BLoC状态
  Future<S?> restoreState<T extends StateStreamable<S>, S>(String blocKey) async {
    try {
      final config = _blocConfigs[blocKey] as BlocPersistConfig<T, S>?;
      if (config == null) {
        _loggingService.warning('No persistence config found for BLoC: $blocKey');
        return null;
      }

      String? jsonString;
      
      // 优先从SharedPreferences恢复
      if (_preferences != null) {
        jsonString = _preferences!.getString('bloc_$blocKey');
      }
      
      // 如果SharedPreferences中没有，尝试从全局状态管理器恢复
      if (jsonString == null) {
        final stateData = _globalStateManager.getState<Map<String, dynamic>>('bloc_$blocKey');
        if (stateData != null) {
          jsonString = json.encode(stateData);
        }
      }

      if (jsonString != null) {
        final stateData = json.decode(jsonString) as Map<String, dynamic>;
        final restoredState = config.fromJson(stateData);
        
        _loggingService.debug('Restored BLoC state: $blocKey');
        return restoredState;
      }
      
      return null;
    } catch (e, stackTrace) {
      _loggingService.error('Failed to restore BLoC state: $blocKey', e, stackTrace);
      return null;
    }
  }

  /// 手动保存BLoC当前状态
  Future<void> saveCurrentState<T extends StateStreamable<S>, S>(String blocKey) async {
    final config = _blocConfigs[blocKey] as BlocPersistConfig<T, S>?;
    if (config == null) {
      _loggingService.warning('No persistence config found for BLoC: $blocKey');
      return;
    }

    final currentState = config.bloc.state;
    await _saveState(config, currentState);
  }

  /// 清除BLoC持久化状态
  Future<void> clearState(String blocKey) async {
    try {
      if (_preferences != null) {
        await _preferences!.remove('bloc_$blocKey');
      }
      
      _globalStateManager.removeState('bloc_$blocKey');
      
      _loggingService.debug('Cleared BLoC state: $blocKey');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to clear BLoC state: $blocKey', e, stackTrace);
    }
  }

  /// 清除所有BLoC持久化状态
  Future<void> clearAllStates() async {
    try {
      final keys = _blocConfigs.keys.toList();
      for (final key in keys) {
        await clearState(key);
      }
      
      _loggingService.info('Cleared all BLoC states');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to clear all BLoC states', e, stackTrace);
    }
  }

  /// 强制保存所有BLoC状态
  Future<void> saveAllStates() async {
    try {
      for (final entry in _blocConfigs.entries) {
        await saveCurrentState(entry.key);
      }
      
      _loggingService.info('Saved all BLoC states');
    } catch (e, stackTrace) {
      _loggingService.error('Failed to save all BLoC states', e, stackTrace);
    }
  }

  /// 获取已注册的BLoC键列表
  List<String> getRegisteredBlocKeys() {
    return _blocConfigs.keys.toList();
  }

  /// 检查BLoC是否已注册
  bool isBlocRegistered(String blocKey) {
    return _blocConfigs.containsKey(blocKey);
  }

  /// 取消注册BLoC
  void unregisterBloc(String blocKey) {
    _blocConfigs.remove(blocKey);
    _saveTimers[blocKey]?.cancel();
    _saveTimers.remove(blocKey);
    
    _loggingService.debug('Unregistered BLoC: $blocKey');
  }

  /// 释放资源
  Future<void> dispose() async {
    try {
      // 保存所有当前状态
      await saveAllStates();
      
      // 取消所有定时器
      for (final timer in _saveTimers.values) {
        timer.cancel();
      }
      _saveTimers.clear();
      
      // 清空配置
      _blocConfigs.clear();
      
      _loggingService.info('BlocPersistenceService disposed');
    } catch (e, stackTrace) {
      _loggingService.error('Error during BlocPersistenceService disposal', e, stackTrace);
    }
  }
}

/// BLoC持久化配置
class BlocPersistConfig<T extends StateStreamable<S>, S> {
  final String blocKey;
  final T bloc;
  final S Function(Map<String, dynamic>) fromJson;
  final Map<String, dynamic> Function(S) toJson;
  final Duration saveDelay;
  final bool autoSave;
  final List<Type> skipStates;

  const BlocPersistConfig({
    required this.blocKey,
    required this.bloc,
    required this.fromJson,
    required this.toJson,
    required this.saveDelay,
    required this.autoSave,
    required this.skipStates,
  });
}

/// BLoC持久化混入 - 为BLoC添加持久化功能
mixin BlocPersistenceMixin<Event, State> on BlocBase<State> {
  String get persistenceKey;
  
  State Function(Map<String, dynamic>)? get fromJson => null;
  Map<String, dynamic> Function(State)? get toJson => null;
  
  Duration get saveDelay => const Duration(milliseconds: 500);
  bool get autoSave => true;
  List<Type> get skipStates => [];

  final BlocPersistenceService _persistenceService = BlocPersistenceService.instance;

  /// 初始化持久化
  Future<void> initializePersistence() async {
    if (fromJson != null && toJson != null) {
      _persistenceService.registerBloc(
        persistenceKey,
        this,
        fromJson: fromJson!,
        toJson: toJson!,
        saveDelay: saveDelay,
        autoSave: autoSave,
        skipStates: skipStates,
      );

      // 尝试恢复状态
      final restoredState = await _persistenceService.restoreState<BlocBase<State>, State>(persistenceKey);
      if (restoredState != null) {
        emit(restoredState);
      }
    }
  }

  /// 手动保存当前状态
  Future<void> saveCurrentState() async {
    await _persistenceService.saveCurrentState(persistenceKey);
  }

  /// 清除持久化状态
  Future<void> clearPersistedState() async {
    await _persistenceService.clearState(persistenceKey);
  }

  @override
  Future<void> close() async {
    _persistenceService.unregisterBloc(persistenceKey);
    return super.close();
  }
}

/// 状态持久化辅助工具
class StatePersistenceHelper {
  /// 序列化状态为JSON
  static Map<String, dynamic> serializeState(dynamic state) {
    if (state == null) return {};
    
    final Map<String, dynamic> json = {};
    
    // 使用反射获取所有字段（简化实现）
    json['_type'] = state.runtimeType.toString();
    json['_timestamp'] = DateTime.now().toIso8601String();
    
    return json;
  }

  /// 从JSON反序列化状态
  static T? deserializeState<T>(Map<String, dynamic> json, T Function(Map<String, dynamic>) fromJson) {
    try {
      return fromJson(json);
    } catch (e) {
      return null;
    }
  }

  /// 检查状态是否可序列化
  static bool isSerializable(dynamic state) {
    try {
      json.encode(state);
      return true;
    } catch (e) {
      return false;
    }
  }
}