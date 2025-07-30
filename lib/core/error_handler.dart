import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/foundation.dart';

/// 应用错误类型
enum AppErrorType {
  network,
  storage,
  sync,
  fileSystem,
  unknown,
}

/// 统一的应用错误类
class AppError implements Exception {
  const AppError({
    required this.message,
    required this.type,
    this.originalException,
    this.stackTrace,
  });

  final String message;
  final AppErrorType type;
  final dynamic originalException;
  final StackTrace? stackTrace;

  @override
  String toString() => 'AppError($type): $message';

  /// 获取用户友好的错误消息
  String get userMessage {
    switch (type) {
      case AppErrorType.network:
        return '网络连接错误，请检查网络设置';
      case AppErrorType.storage:
        return '存储访问错误，请检查存储权限';
      case AppErrorType.sync:
        return '同步失败，请稍后重试';
      case AppErrorType.fileSystem:
        return '文件操作失败，请检查文件是否存在';
      case AppErrorType.unknown:
        return '未知错误，请稍后重试';
    }
  }
}

/// 统一错误处理器
class ErrorHandler {
  /// 处理并记录错误
  static Future<void> handleError(
    dynamic error, {
    StackTrace? stackTrace,
    String? context,
    bool fatal = false,
  }) async {
    // 记录到控制台（开发模式）
    if (kDebugMode) {
      debugPrint('ErrorHandler: ${context ?? 'Unknown context'}');
      debugPrint('Error: $error');
      if (stackTrace != null) {
        debugPrint('StackTrace: $stackTrace');
      }
    }

    // 记录到 Crashlytics
    try {
      await FirebaseCrashlytics.instance.recordError(
        error,
        stackTrace,
        fatal: fatal,
        information: context != null ? [context] : [],
      );
    } catch (e) {
      // 如果 Crashlytics 记录失败，只在调试模式下输出
      if (kDebugMode) {
        debugPrint('Failed to record error to Crashlytics: $e');
      }
    }
  }

  /// 包装函数调用并处理错误
  static Future<T?> catchError<T>(
    Future<T> Function() function, {
    String? context,
    T? fallback,
    bool shouldRethrow = false,
  }) async {
    try {
      return await function();
    } catch (error, stackTrace) {
      await handleError(error, stackTrace: stackTrace, context: context);
      
      if (shouldRethrow) {
        rethrow;
      }
      
      return fallback;
    }
  }

  /// 将通用异常转换为应用错误  
  static AppError convertToAppError(dynamic error) {
    if (error is AppError) {
      return error;
    }

    // 根据异常类型判断错误类型
    var type = AppErrorType.unknown;
    final message = error.toString();

    if (error.toString().contains('network') || 
        error.toString().contains('connection')) {
      type = AppErrorType.network;
    } else if (error.toString().contains('file') || 
               error.toString().contains('path')) {
      type = AppErrorType.fileSystem;
    } else if (error.toString().contains('sync')) {
      type = AppErrorType.sync;
    }

    return AppError(
      message: message,
      type: type,
      originalException: error,
    );
  }
}