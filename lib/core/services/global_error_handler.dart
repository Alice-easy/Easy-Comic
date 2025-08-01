import 'dart:async';
import 'dart:isolate';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'error_handler_service.dart';
import 'message_service.dart';
import 'logging_service.dart';
import '../error_handler.dart';

/// 全局错误处理服务
class GlobalErrorHandler {
  static GlobalErrorHandler? _instance;
  static GlobalErrorHandler get instance => _instance ??= GlobalErrorHandler._internal();
  
  GlobalErrorHandler._internal();

  late ErrorHandlerService _errorHandlerService;
  late MessageService _messageService;
  late LoggingService _loggingService;
  
  bool _isInitialized = false;
  int _crashCount = 0;
  DateTime? _lastCrashTime;
  final int _maxCrashesPerHour = 5;

  /// 初始化全局错误处理
  void initialize({
    required ErrorHandlerService errorHandlerService,
    required MessageService messageService,
    required LoggingService loggingService,
  }) {
    if (_isInitialized) return;

    _errorHandlerService = errorHandlerService;
    _messageService = messageService;
    _loggingService = loggingService;

    _setupGlobalErrorHandling();
    _isInitialized = true;
    
    _loggingService.info('Global error handler initialized');
  }

  /// 设置全局错误处理
  void _setupGlobalErrorHandling() {
    // Flutter框架错误处理
    FlutterError.onError = _handleFlutterError;

    // 平台错误处理（iOS/Android原生代码错误）
    PlatformDispatcher.instance.onError = _handlePlatformError;

    // Isolate错误处理
    Isolate.current.addErrorListener(
      RawReceivePort((List<dynamic> errorAndStacktrace) {
        _handleIsolateError(errorAndStacktrace[0], errorAndStacktrace[1]);
      }).sendPort,
    );

    // Firebase Crashlytics配置
    if (!kDebugMode) {
      FirebaseCrashlytics.instance.setCrashlyticsCollectionEnabled(true);
    }
  }

  /// 处理Flutter框架错误
  void _handleFlutterError(FlutterErrorDetails details) async {
    // 检查崩溃频率
    if (_shouldPreventCrashLoop()) {
      _loggingService.error(
        'Preventing crash loop - too many crashes in short time',
        details.exception,
        details.stack,
      );
      return;
    }

    final isDev = kDebugMode;
    final isFatal = _isFatalError(details);

    // 记录错误
    await _logError(
      'Flutter Framework Error',
      details.exception,
      details.stack,
      isFatal,
      {
        'library': details.library,
        'context': details.context?.toString(),
        'informationCollector': details.informationCollector?.call(),
        'silent': details.silent,
      },
    );

    // 开发模式下显示红屏
    if (isDev) {
      FlutterError.presentError(details);
    }

    // 用户反馈
    if (!details.silent && _shouldShowUserFeedback(details)) {
      _showErrorFeedback(
        '应用遇到异常',
        _getUserFriendlyMessage(details.exception),
        canRestart: isFatal,
      );
    }

    // 更新崩溃计数
    _updateCrashCount();
  }

  /// 处理平台错误
  bool _handlePlatformError(Object error, StackTrace stack) {
    // 检查崩溃频率
    if (_shouldPreventCrashLoop()) {
      _loggingService.error(
        'Preventing crash loop - platform error',
        error,
        stack,
      );
      return true; // 标记为已处理
    }

    _logError('Platform Error', error, stack, true);
    
    _showErrorFeedback(
      '系统级错误',
      '应用遇到系统级错误，建议重启应用',
      canRestart: true,
    );

    _updateCrashCount();
    return true; // 标记为已处理，防止应用崩溃
  }

  /// 处理Isolate错误
  void _handleIsolateError(dynamic error, dynamic stackTrace) async {
    final stack = stackTrace is StackTrace ? stackTrace : StackTrace.current;
    
    await _logError('Isolate Error', error, stack, true);
    
    _showErrorFeedback(
      '后台任务错误',
      '后台任务执行失败，部分功能可能不可用',
      canRestart: false,
    );
  }

  /// 记录错误到多个目标
  Future<void> _logError(
    String type,
    dynamic error,
    StackTrace? stackTrace,
    bool isFatal, [
    Map<String, dynamic>? additionalInfo,
  ]) async {
    final errorInfo = {
      'type': type,
      'error': error.toString(),
      'stackTrace': stackTrace.toString(),
      'isFatal': isFatal,
      'timestamp': DateTime.now().toIso8601String(),
      'crashCount': _crashCount,
      if (additionalInfo != null) ...additionalInfo,
    };

    // 记录到本地日志
    _loggingService.error(type, error, stackTrace);

    // 记录到Crashlytics（生产环境）
    if (!kDebugMode) {
      try {
        await FirebaseCrashlytics.instance.recordError(
          error,
          stackTrace,
          fatal: isFatal,
          information: [
            type,
            'Fatal: $isFatal',
            'Crash Count: $_crashCount',
            if (additionalInfo != null)
              ...additionalInfo.entries.map((e) => '${e.key}: ${e.value}'),
          ],
        );

        // 设置自定义键值
        await FirebaseCrashlytics.instance.setCustomKey('error_type', type);
        await FirebaseCrashlytics.instance.setCustomKey('crash_count', _crashCount);
        await FirebaseCrashlytics.instance.setCustomKey('is_fatal', isFatal);
      } catch (e) {
        _loggingService.warning('Failed to record error to Crashlytics', e);
      }
    }

    // 记录到错误处理服务
    await _errorHandlerService.handleError(
      error,
      stackTrace: stackTrace,
      context: type,
      fatal: isFatal,
      showUserMessage: false, // 统一由此处处理用户反馈
    );
  }

  /// 显示错误反馈给用户
  void _showErrorFeedback(
    String title,
    String message, {
    bool canRestart = false,
  }) {
    if (canRestart) {
      _messageService.showErrorWithRetry(
        '$title\n$message',
        onRetry: _restartApp,
      );
    } else {
      _messageService.showError('$title\n$message');
    }
  }

  /// 判断是否为致命错误
  bool _isFatalError(FlutterErrorDetails details) {
    final errorString = details.exception.toString().toLowerCase();
    
    // 致命错误类型
    final fatalKeywords = [
      'out of memory',
      'stack overflow',
      'segmentation fault',
      'null pointer',
      'assertion failed',
      'fatal exception',
    ];

    return fatalKeywords.any((keyword) => errorString.contains(keyword));
  }

  /// 判断是否应该显示用户反馈
  bool _shouldShowUserFeedback(FlutterErrorDetails details) {
    // 静默错误不显示反馈
    if (details.silent) return false;

    // 渲染错误通常不需要用户反馈
    if (details.library == 'rendering') return false;

    // 布局错误通常不需要用户反馈
    if (details.library == 'rendering' &&
        details.exception.toString().contains('RenderFlex')) {
      return false;
    }

    return true;
  }

  /// 获取用户友好的错误消息
  String _getUserFriendlyMessage(dynamic error) {
    final errorString = error.toString().toLowerCase();

    if (errorString.contains('network') || errorString.contains('connection')) {
      return '网络连接异常，请检查网络设置';
    }
    
    if (errorString.contains('permission')) {
      return '权限不足，请检查应用权限设置';
    }
    
    if (errorString.contains('storage') || errorString.contains('disk')) {
      return '存储空间不足，请清理设备存储';
    }
    
    if (errorString.contains('memory')) {
      return '内存不足，请关闭其他应用后重试';
    }
    
    if (errorString.contains('timeout')) {
      return '操作超时，请稍后重试';
    }

    return '应用遇到未知错误，请尝试重启应用';
  }

  /// 检查是否应该防止崩溃循环
  bool _shouldPreventCrashLoop() {
    final now = DateTime.now();
    
    // 重置计数器（每小时重置）
    if (_lastCrashTime == null || 
        now.difference(_lastCrashTime!).inHours >= 1) {
      _crashCount = 0;
      _lastCrashTime = now;
      return false;
    }

    return _crashCount >= _maxCrashesPerHour;
  }

  /// 更新崩溃计数
  void _updateCrashCount() {
    _crashCount++;
    _lastCrashTime = DateTime.now();
    
    if (_crashCount >= _maxCrashesPerHour) {
      _loggingService.warning(
        'High crash frequency detected: $_crashCount crashes in the last hour',
      );
    }
  }

  /// 重启应用
  void _restartApp() {
    _loggingService.info('User initiated app restart due to error');
    
    // 清理资源
    _cleanup();
    
    // 重启应用（需要原生支持或使用restart插件）
    // 这里使用热重启的简单实现
    runApp(MyApp()); // 需要从main.dart导入
  }

  /// 清理资源
  void _cleanup() {
    _loggingService.info('Cleaning up resources before restart');
    
    // 清理缓存
    // 关闭数据库连接
    // 取消网络请求
    // 等等...
  }

  /// 手动上报错误
  static Future<void> reportError(
    dynamic error, {
    StackTrace? stackTrace,
    String? context,
    Map<String, dynamic>? additionalInfo,
    bool isFatal = false,
  }) async {
    if (instance._isInitialized) {
      await instance._logError(
        context ?? 'Manual Report',
        error,
        stackTrace ?? StackTrace.current,
        isFatal,
        additionalInfo,
      );
    }
  }

  /// 设置用户信息（用于错误报告）
  static Future<void> setUserInfo({
    String? userId,
    String? email,
    String? name,
  }) async {
    if (!kDebugMode) {
      try {
        if (userId != null) {
          await FirebaseCrashlytics.instance.setUserIdentifier(userId);
        }
        
        await FirebaseCrashlytics.instance.setCustomKey('user_email', email ?? 'unknown');
        await FirebaseCrashlytics.instance.setCustomKey('user_name', name ?? 'unknown');
      } catch (e) {
        instance._loggingService.warning('Failed to set user info in Crashlytics', e);
      }
    }
  }

  /// 添加自定义日志
  static void addLog(String message) {
    if (!kDebugMode) {
      try {
        FirebaseCrashlytics.instance.log(message);
      } catch (e) {
        instance._loggingService.warning('Failed to add log to Crashlytics', e);
      }
    }
    
    instance._loggingService.info('Custom Log: $message');
  }

  /// 获取错误统计
  Map<String, dynamic> getErrorStats() {
    return {
      'crashCount': _crashCount,
      'lastCrashTime': _lastCrashTime?.toIso8601String(),
      'isInitialized': _isInitialized,
      'maxCrashesPerHour': _maxCrashesPerHour,
    };
  }
}

/// 全局错误处理包装器函数
Future<void> runAppWithErrorHandling(Widget app) async {
  await runZonedGuarded<Future<void>>(() async {
    runApp(app);
  }, (error, stackTrace) {
    // Zone错误处理
    GlobalErrorHandler.instance._logError(
      'Zone Error',
      error,
      stackTrace,
      true,
    );
  });
}

// 临时的MyApp引用，实际应该从main.dart导入
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(); // 临时实现
  }
}