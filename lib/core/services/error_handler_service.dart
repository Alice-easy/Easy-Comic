import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import '../error_handler.dart';
import '../error/failures.dart';
import '../error/retry_mechanism.dart';
import 'logging_service.dart';
import 'message_service.dart';

/// 统一的错误处理服务
class ErrorHandlerService {
  final LoggingService _loggingService;
  final MessageService _messageService;
  
  ErrorHandlerService({
    required LoggingService loggingService,
    required MessageService messageService,
  }) : _loggingService = loggingService,
       _messageService = messageService;

  /// 处理并记录错误，可选择是否显示用户消息
  Future<void> handleError(
    dynamic error, {
    StackTrace? stackTrace,
    String? context,
    bool fatal = false,
    bool showUserMessage = true,
    String? customUserMessage,
    bool enableRetry = false,
    VoidCallback? onRetry,
  }) async {
    final appError = _convertToAppError(error);
    
    // 记录错误
    await _logError(appError, stackTrace: stackTrace, context: context, fatal: fatal);
    
    // 显示用户消息
    if (showUserMessage) {
      final userMessage = customUserMessage ?? appError.userMessage;
      
      if (enableRetry && onRetry != null) {
        _messageService.showErrorWithRetry(
          userMessage,
          onRetry: onRetry,
        );
      } else {
        _messageService.showError(userMessage);
      }
    }
  }

  /// 包装函数调用并处理错误
  Future<T?> catchError<T>(
    Future<T> Function() function, {
    String? context,
    T? fallback,
    bool shouldRethrow = false,
    bool showUserMessage = true,
    String? customUserMessage,
    bool enableRetry = false,
    VoidCallback? onRetry,
  }) async {
    try {
      return await function();
    } catch (error, stackTrace) {
      await handleError(
        error,
        stackTrace: stackTrace,
        context: context,
        showUserMessage: showUserMessage,
        customUserMessage: customUserMessage,
        enableRetry: enableRetry,
        onRetry: onRetry,
      );
      
      if (shouldRethrow) {
        rethrow;
      }
      
      return fallback;
    }
  }

  /// 带重试的错误处理
  Future<T> handleWithRetry<T>(
    Future<T> Function() operation, {
    String? context,
    RetryConfig? retryConfig,
    bool Function(Exception)? shouldRetry,
    bool showUserMessage = true,
    String? customUserMessage,
  }) async {
    final retry = ExponentialBackoffRetry();
    
    try {
      return await retry.execute(
        operation,
        config: retryConfig ?? RetryConfig.network,
        shouldRetry: shouldRetry ?? ExponentialBackoffRetry.defaultShouldRetry,
        onRetry: (attempt, error) {
          _loggingService.warning(
            'Retrying operation (attempt $attempt): ${error.toString()}',
            error,
          );
          
          if (showUserMessage && attempt == 1) {
            _messageService.showWarning('操作失败，正在重试...');
          }
        },
      );
    } catch (error, stackTrace) {
      await handleError(
        error,
        stackTrace: stackTrace,
        context: context,
        showUserMessage: showUserMessage,
        customUserMessage: customUserMessage ?? '操作失败，已达到最大重试次数',
      );
      rethrow;
    }
  }

  /// 处理验证错误
  void handleValidationError(String message, {String? field}) {
    final context = field != null ? 'Validation error in field: $field' : 'Validation error';
    _loggingService.warning(context, message);
    _messageService.showWarning(message);
  }

  /// 处理网络错误
  Future<void> handleNetworkError(
    dynamic error, {
    String? context,
    bool showUserMessage = true,
    VoidCallback? onRetry,
  }) async {
    final appError = AppError(
      message: error.toString(),
      type: AppErrorType.network,
      originalException: error,
    );

    await _logError(appError, context: context ?? 'Network operation');
    
    if (showUserMessage) {
      if (onRetry != null) {
        _messageService.showErrorWithRetry(
          '网络连接异常，请检查网络设置',
          onRetry: onRetry,
        );
      } else {
        _messageService.showError('网络连接异常，请检查网络设置');
      }
    }
  }

  /// 处理文件系统错误
  Future<void> handleFileSystemError(
    dynamic error, {
    String? context,
    String? filePath,
    bool showUserMessage = true,
  }) async {
    final appError = AppError(
      message: error.toString(),
      type: AppErrorType.fileSystem,
      originalException: error,
    );

    final contextInfo = context ?? 'File system operation';
    final fullContext = filePath != null ? '$contextInfo (file: $filePath)' : contextInfo;

    await _logError(appError, context: fullContext);
    
    if (showUserMessage) {
      String userMessage = '文件操作失败';
      if (error.toString().contains('permission')) {
        userMessage = '文件访问权限不足，请检查应用权限';
      } else if (error.toString().contains('not found')) {
        userMessage = '文件不存在或已被删除';
      } else if (error.toString().contains('space')) {
        userMessage = '存储空间不足，请清理磁盘空间';
      }
      
      _messageService.showError(userMessage);
    }
  }

  /// 处理数据库错误
  Future<void> handleDatabaseError(
    dynamic error, {
    String? context,
    String? operation,
    bool showUserMessage = true,
  }) async {
    final appError = AppError(
      message: error.toString(),
      type: AppErrorType.storage,
      originalException: error,
    );

    final contextInfo = context ?? 'Database operation';
    final fullContext = operation != null ? '$contextInfo ($operation)' : contextInfo;

    await _logError(appError, context: fullContext);
    
    if (showUserMessage) {
      String userMessage = '数据库操作失败';
      if (error.toString().contains('constraint')) {
        userMessage = '数据冲突，请检查输入信息';
      } else if (error.toString().contains('locked')) {
        userMessage = '数据库忙碌，请稍后重试';
      }
      
      _messageService.showError(userMessage);
    }
  }

  /// 处理同步错误
  Future<void> handleSyncError(
    dynamic error, {
    String? context,
    bool showUserMessage = true,
    VoidCallback? onRetry,
  }) async {
    final appError = AppError(
      message: error.toString(),
      type: AppErrorType.sync,
      originalException: error,
    );

    await _logError(appError, context: context ?? 'Sync operation');
    
    if (showUserMessage) {
      if (onRetry != null) {
        _messageService.showErrorWithRetry(
          '同步失败，请检查网络连接',
          onRetry: onRetry,
        );
      } else {
        _messageService.showError('同步失败，请检查网络连接');
      }
    }
  }

  /// 记录错误到日志和崩溃报告
  Future<void> _logError(
    AppError error, {
    StackTrace? stackTrace,
    String? context,
    bool fatal = false,
  }) async {
    // 记录到本地日志
    _loggingService.error(
      '${context ?? 'Unknown context'}: ${error.message}',
      error.originalException ?? error,
      stackTrace,
    );

    // 记录到 Crashlytics（生产环境）
    if (!kDebugMode) {
      try {
        await FirebaseCrashlytics.instance.recordError(
          error.originalException ?? error,
          stackTrace,
          fatal: fatal,
          information: [
            if (context != null) 'Context: $context',
            'Error Type: ${error.type}',
            'User Message: ${error.userMessage}',
          ],
        );
      } catch (e) {
        _loggingService.warning('Failed to record error to Crashlytics', e);
      }
    }
  }

  /// 转换为应用错误
  AppError _convertToAppError(dynamic error) {
    if (error is AppError) {
      return error;
    }

    if (error is Failure) {
      AppErrorType type;
      if (error is ServerFailure) {
        type = AppErrorType.network;
      } else if (error is DatabaseFailure || error is CacheFailure) {
        type = AppErrorType.storage;
      } else if (error is NotFoundFailure) {
        type = AppErrorType.fileSystem;
      } else {
        type = AppErrorType.unknown;
      }

      return AppError(
        message: error.message,
        type: type,
        originalException: error,
      );
    }

    return ErrorHandler.convertToAppError(error);
  }

  /// 获取错误恢复建议
  List<String> getRecoverySteps(AppError error) {
    switch (error.type) {
      case AppErrorType.network:
        return [
          '检查网络连接是否正常',
          '尝试切换到其他网络',
          '检查防火墙设置',
          '稍后重试',
        ];
      case AppErrorType.storage:
        return [
          '检查存储空间是否充足',
          '重启应用',
          '清理应用缓存',
          '联系技术支持',
        ];
      case AppErrorType.sync:
        return [
          '检查网络连接',
          '检查服务器状态',
          '稍后重试同步',
          '手动备份重要数据',
        ];
      case AppErrorType.fileSystem:
        return [
          '检查文件是否存在',
          '检查应用权限',
          '重新导入文件',
          '重启设备',
        ];
      case AppErrorType.unknown:
        return [
          '重启应用',
          '检查设备存储空间',
          '更新应用到最新版本',
          '联系技术支持',
        ];
    }
  }
}