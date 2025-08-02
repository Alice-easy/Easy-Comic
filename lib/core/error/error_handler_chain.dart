import 'dart:async';
import 'dart:developer' as developer;
import 'package:flutter/foundation.dart';

/// ErrorCallback type alias for Flutter error handler
typedef ErrorCallback = FlutterExceptionHandler?;

/// 错误严重程度
enum ErrorSeverity {
  low,      // 轻微错误，不影响核心功能
  medium,   // 中等错误，影响部分功能
  high,     // 严重错误，影响主要功能
  critical, // 致命错误，可能导致应用崩溃
}

/// 错误上下文信息
class ErrorContext {
  final String? comicId;
  final int? currentPage;
  final String userAction;
  final Map<String, dynamic> appState;
  final DeviceInfo deviceInfo;
  final String buildVersion;
  
  const ErrorContext({
    this.comicId,
    this.currentPage,
    required this.userAction,
    required this.appState,
    required this.deviceInfo,
    required this.buildVersion,
  });

  Map<String, dynamic> toReportableMap() {
    return {
      'comicId': comicId != null ? _anonymizeId(comicId!) : null,
      'currentPage': currentPage,
      'userAction': userAction,
      'appState': _sanitizeAppState(appState),
      'deviceInfo': deviceInfo.toMap(),
      'buildVersion': buildVersion,
      'timestamp': DateTime.now().toIso8601String(),
    };
  }

  ErrorContext anonymize() {
    return ErrorContext(
      comicId: comicId != null ? _anonymizeId(comicId!) : null,
      currentPage: currentPage,
      userAction: userAction,
      appState: _sanitizeAppState(appState),
      deviceInfo: deviceInfo.anonymize(),
      buildVersion: buildVersion,
    );
  }

  String _anonymizeId(String id) {
    // 简单的匿名化：只保留前后各2个字符
    if (id.length <= 4) return '****';
    return '${id.substring(0, 2)}***${id.substring(id.length - 2)}';
  }

  Map<String, dynamic> _sanitizeAppState(Map<String, dynamic> state) {
    final sanitized = <String, dynamic>{};
    for (final entry in state.entries) {
      // 移除敏感信息
      if (_isSensitiveKey(entry.key)) {
        sanitized[entry.key] = '[REDACTED]';
      } else {
        sanitized[entry.key] = entry.value;
      }
    }
    return sanitized;
  }

  bool _isSensitiveKey(String key) {
    final sensitiveKeys = ['password', 'token', 'key', 'secret', 'auth'];
    return sensitiveKeys.any((sensitive) => 
        key.toLowerCase().contains(sensitive));
  }
}

/// 设备信息
class DeviceInfo {
  final String platform;
  final String? model;
  final String? operatingSystem;
  final String? operatingSystemVersion;
  final double? screenWidth;
  final double? screenHeight;
  final int? memoryMB;

  const DeviceInfo({
    required this.platform,
    this.model,
    this.operatingSystem,
    this.operatingSystemVersion,
    this.screenWidth,
    this.screenHeight,
    this.memoryMB,
  });

  Map<String, dynamic> toMap() {
    return {
      'platform': platform,
      'model': model,
      'operatingSystem': operatingSystem,
      'operatingSystemVersion': operatingSystemVersion,
      'screenWidth': screenWidth,
      'screenHeight': screenHeight,
      'memoryMB': memoryMB,
    };
  }

  DeviceInfo anonymize() {
    return DeviceInfo(
      platform: platform,
      model: model != null ? _hashString(model!) : null,
      operatingSystem: operatingSystem,
      operatingSystemVersion: operatingSystemVersion,
      screenWidth: screenWidth,
      screenHeight: screenHeight,
      memoryMB: memoryMB,
    );
  }

  String _hashString(String input) {
    // 简单的哈希函数（生产环境应使用更安全的方法）
    return 'device_${input.hashCode.abs()}';
  }
}

/// 错误报告
class ErrorReport {
  final String id;
  final ErrorSeverity severity;
  final String message;
  final String stackTrace;
  final ErrorContext context;
  final DateTime timestamp;
  final bool isReported;
  final String? resolutionStrategy;

  const ErrorReport({
    required this.id,
    required this.severity,
    required this.message,
    required this.stackTrace,
    required this.context,
    required this.timestamp,
    required this.isReported,
    this.resolutionStrategy,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'severity': severity.name,
      'message': message,
      'stackTrace': stackTrace,
      'context': context.toReportableMap(),
      'timestamp': timestamp.toIso8601String(),
      'isReported': isReported,
      'resolutionStrategy': resolutionStrategy,
    };
  }
}

/// 错误处理器接口
abstract class ErrorProcessor {
  Future<void> processError(FlutterErrorDetails details, ErrorContext? context);
}

/// 生产环境错误处理器
class ProductionErrorProcessor implements ErrorProcessor {
  @override
  Future<void> processError(FlutterErrorDetails details, ErrorContext? context) async {
    try {
      // 记录到崩溃报告服务
      await _logToCrashlytics(details, context);
      
      // 本地存储用于离线分析
      await _storeErrorLocally(details, context);
      
      // 尝试错误恢复
      await _attemptRecovery(details, context);
    } catch (e) {
      developer.log(
        'Error in production error processor: $e',
        name: 'ProductionErrorProcessor',
        level: 1000,
      );
    }
  }

  Future<void> _logToCrashlytics(FlutterErrorDetails details, ErrorContext? context) async {
    // 这里集成 Firebase Crashlytics 或其他崩溃报告服务
    developer.log(
      'Would log to crashlytics: ${details.exception}',
      name: 'ProductionErrorProcessor',
    );
  }

  Future<void> _storeErrorLocally(FlutterErrorDetails details, ErrorContext? context) async {
    // 存储错误到本地数据库或文件
    developer.log(
      'Storing error locally: ${details.exception}',
      name: 'ProductionErrorProcessor',
    );
  }

  Future<void> _attemptRecovery(FlutterErrorDetails details, ErrorContext? context) async {
    // 实现错误自动恢复逻辑
    developer.log(
      'Attempting error recovery for: ${details.exception}',
      name: 'ProductionErrorProcessor',
    );
  }
}

/// 开发环境错误处理器
class DevelopmentErrorProcessor implements ErrorProcessor {
  @override
  Future<void> processError(FlutterErrorDetails details, ErrorContext? context) async {
    // 详细的控制台日志
    _logToConsole(details, context);
    
    // 显示开发者对话框
    _showDeveloperDialog(details, context);
    
    // 写入调试日志文件
    await _writeToDebugLog(details, context);
  }

  void _logToConsole(FlutterErrorDetails details, ErrorContext? context) {
    developer.log(
      'Development Error Details',
      name: 'DevelopmentErrorProcessor',
      level: 1000,
      error: details.exception,
      stackTrace: details.stack,
    );
    
    if (context != null) {
      developer.log(
        'Error Context: ${context.toReportableMap()}',
        name: 'DevelopmentErrorProcessor',
      );
    }
  }

  void _showDeveloperDialog(FlutterErrorDetails details, ErrorContext? context) {
    // 在开发模式下显示详细错误信息的对话框
    developer.log(
      'Would show developer dialog with full error details',
      name: 'DevelopmentErrorProcessor',
    );
  }

  Future<void> _writeToDebugLog(FlutterErrorDetails details, ErrorContext? context) async {
    // 写入详细的调试日志文件
    developer.log(
      'Writing to debug log file',
      name: 'DevelopmentErrorProcessor',
    );
  }
}

/// 非破坏性错误处理器链
class ErrorHandlerChain {
  ErrorCallback? _previousHandler;
  final List<ErrorProcessor> _processors = [];
  final StreamController<ErrorReport> _errorReportController = StreamController<ErrorReport>.broadcast();
  bool _isInitialized = false;

  /// 获取错误报告流
  Stream<ErrorReport> get errorReportStream => _errorReportController.stream;

  /// 初始化错误处理器链
  void initialize({bool isDevelopment = kDebugMode}) {
    if (_isInitialized) {
      developer.log(
        'ErrorHandlerChain already initialized',
        name: 'ErrorHandlerChain',
        level: 900,
      );
      return;
    }

    // 保留现有的错误处理器
    _previousHandler = FlutterError.onError;
    
    // 添加适当的错误处理器
    if (isDevelopment) {
      _processors.add(DevelopmentErrorProcessor());
    } else {
      _processors.add(ProductionErrorProcessor());
    }
    
    // 设置链式错误处理器
    FlutterError.onError = _chainedErrorHandler;
    
    _isInitialized = true;
    
    developer.log(
      'ErrorHandlerChain initialized with ${_processors.length} processors (development: $isDevelopment)',
      name: 'ErrorHandlerChain',
    );
  }

  /// 添加自定义错误处理器
  void addProcessor(ErrorProcessor processor) {
    _processors.add(processor);
    developer.log(
      'Added custom error processor: ${processor.runtimeType}',
      name: 'ErrorHandlerChain',
    );
  }

  /// 链式错误处理器
  void _chainedErrorHandler(FlutterErrorDetails details) {
    // 处理通过我们的处理器
    _processError(details);
    
    // 调用之前的处理器（如果存在）
    if (_previousHandler != null) {
      try {
        _previousHandler!(details);
      } catch (e) {
        developer.log(
          'Error in previous handler: $e',
          name: 'ErrorHandlerChain',
          level: 900,
        );
      }
    }
  }

  /// 处理错误
  Future<void> _processError(FlutterErrorDetails details) async {
    try {
      // 创建错误上下文
      final context = _createErrorContext(details);
      
      // 创建错误报告
      final report = _createErrorReport(details, context);
      
      // 通过所有处理器处理错误
      for (final processor in _processors) {
        try {
          await processor.processError(details, context);
        } catch (e) {
          developer.log(
            'Error in processor ${processor.runtimeType}: $e',
            name: 'ErrorHandlerChain',
            level: 900,
          );
        }
      }
      
      // 发出错误报告
      _errorReportController.add(report);
      
    } catch (e) {
      developer.log(
        'Critical error in error processing: $e',
        name: 'ErrorHandlerChain',
        level: 1000,
      );
    }
  }

  /// 创建错误上下文
  ErrorContext _createErrorContext(FlutterErrorDetails details) {
    return ErrorContext(
      userAction: 'Unknown action', // 应该由上层提供
      appState: {
        'errorLibrary': details.library,
        'errorContext': details.context?.toString(),
        'silent': details.silent,
      },
      deviceInfo: _getCurrentDeviceInfo(),
      buildVersion: 'Unknown', // 应该从应用配置获取
    );
  }

  /// 获取当前设备信息
  DeviceInfo _getCurrentDeviceInfo() {
    // 这里应该获取实际的设备信息
    return const DeviceInfo(
      platform: 'Unknown',
    );
  }

  /// 创建错误报告
  ErrorReport _createErrorReport(FlutterErrorDetails details, ErrorContext context) {
    return ErrorReport(
      id: _generateErrorId(),
      severity: _determineSeverity(details),
      message: details.exceptionAsString(),
      stackTrace: details.stack?.toString() ?? 'No stack trace available',
      context: context,
      timestamp: DateTime.now(),
      isReported: false,
    );
  }

  /// 生成错误ID
  String _generateErrorId() {
    return 'error_${DateTime.now().millisecondsSinceEpoch}_${Object().hashCode}';
  }

  /// 确定错误严重程度
  ErrorSeverity _determineSeverity(FlutterErrorDetails details) {
    final exceptionString = details.exceptionAsString().toLowerCase();
    
    if (exceptionString.contains('renderflex') || 
        exceptionString.contains('overflow')) {
      return ErrorSeverity.low;
    } else if (exceptionString.contains('state') || 
               exceptionString.contains('null')) {
      return ErrorSeverity.medium;
    } else if (exceptionString.contains('assertion') ||
               exceptionString.contains('range')) {
      return ErrorSeverity.high;
    } else {
      return ErrorSeverity.critical;
    }
  }

  /// 恢复原始错误处理器
  void dispose() {
    if (_isInitialized) {
      FlutterError.onError = _previousHandler;
      _previousHandler = null;
      _processors.clear();
      _errorReportController.close();
      _isInitialized = false;
      
      developer.log(
        'ErrorHandlerChain disposed and original handler restored',
        name: 'ErrorHandlerChain',
      );
    }
  }

  /// 手动报告错误
  void reportError(Object error, StackTrace? stackTrace, ErrorContext? context) {
    final details = FlutterErrorDetails(
      exception: error,
      stack: stackTrace,
      library: 'Manual Report',
      context: ErrorDescription('Manually reported error'),
    );
    
    _chainedErrorHandler(details);
  }
}