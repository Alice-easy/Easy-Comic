import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'dart:developer' as developer;
import 'package:easy_comic/core/error/error_handler_chain.dart';

/// A widget that catches and handles errors gracefully, similar to React's Error Boundary
/// 
/// This widget now uses non-destructive error handling that preserves existing error handlers
/// while adding comprehensive error reporting and recovery capabilities.
class ErrorBoundaryWidget extends StatefulWidget {
  final Widget child;
  final Widget Function(Object error, StackTrace? stackTrace)? onError;
  final String? context;
  final ErrorContext? errorContext;
  final bool enableRecovery;

  const ErrorBoundaryWidget({
    super.key,
    required this.child,
    this.onError,
    this.context,
    this.errorContext,
    this.enableRecovery = true,
  });

  @override
  State<ErrorBoundaryWidget> createState() => _ErrorBoundaryWidgetState();
}

class _ErrorBoundaryWidgetState extends State<ErrorBoundaryWidget> {
  Object? _error;
  StackTrace? _stackTrace;
  ErrorHandlerChain? _errorHandlerChain;
  late final String _instanceId;
  int _retryCount = 0;
  static const int _maxRetries = 3;

  @override
  void initState() {
    super.initState();
    _instanceId = 'boundary_${widget.hashCode}_${DateTime.now().millisecondsSinceEpoch}';
    _setupErrorHandling();
  }

  void _setupErrorHandling() {
    // 创建并初始化非破坏性错误处理器链
    _errorHandlerChain = ErrorHandlerChain();
    _errorHandlerChain!.initialize(isDevelopment: kDebugMode);
    
    // 添加自定义错误处理器来处理这个边界的错误
    _errorHandlerChain!.addProcessor(_BoundaryErrorProcessor(
      onBoundaryError: _handleBoundaryError,
      boundaryContext: widget.context ?? 'Unknown context',
      instanceId: _instanceId,
    ));
    
    // 监听错误报告
    _errorHandlerChain!.errorReportStream.listen((report) {
      _processErrorReport(report);
    });
    
    developer.log(
      'ErrorBoundaryWidget initialized with non-destructive error handling',
      name: 'ErrorBoundaryWidget',
    );
  }

  void _handleBoundaryError(Object error, StackTrace? stackTrace) {
    if (mounted && _error != error) { // 避免重复处理同一个错误
      setState(() {
        _error = error;
        _stackTrace = stackTrace;
      });
    }
  }

  void _processErrorReport(ErrorReport report) {
    developer.log(
      'Error report received in boundary ${_instanceId}: ${report.message}',
      name: 'ErrorBoundaryWidget',
      level: _getLogLevel(report.severity),
    );
  }

  int _getLogLevel(ErrorSeverity severity) {
    switch (severity) {
      case ErrorSeverity.low:
        return 500;
      case ErrorSeverity.medium:
        return 700;
      case ErrorSeverity.high:
        return 900;
      case ErrorSeverity.critical:
        return 1000;
    }
  }

  @override
  void dispose() {
    // 正确清理错误处理器链，恢复原始处理器
    _errorHandlerChain?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_error != null) {
      if (widget.onError != null) {
        return widget.onError!(_error!, _stackTrace);
      }
      
      return _buildDefaultErrorWidget();
    }

    // 使用安全的构建方式，捕获构建时的错误
    return _SafeBuilder(
      builder: () => widget.child,
      onError: (error, stackTrace) {
        _handleBoundaryError(error, stackTrace);
        
        // 向错误处理器链报告错误
        if (_errorHandlerChain != null && widget.errorContext != null) {
          _errorHandlerChain!.reportError(error, stackTrace, widget.errorContext!);
        }
      },
      fallback: () => _buildDefaultErrorWidget(error: _error, stackTrace: _stackTrace),
    );
  }

  Widget _buildDefaultErrorWidget({Object? error, StackTrace? stackTrace}) {
    final displayError = error ?? _error;
    final displayStackTrace = stackTrace ?? _stackTrace;
    final canRetry = widget.enableRecovery && _retryCount < _maxRetries;
    
    return Material(
      child: Container(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              _getErrorIcon(),
              color: _getErrorColor(),
              size: 64,
            ),
            const SizedBox(height: 16),
            Text(
              _getErrorTitle(),
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: _getErrorColor(),
              ),
              textAlign: TextAlign.center,
            ),
            if (widget.context != null) ...[
              const SizedBox(height: 8),
              Text(
                'Context: ${widget.context}',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Colors.grey[600],
                ),
                textAlign: TextAlign.center,
              ),
            ],
            const SizedBox(height: 16),
            if (kDebugMode && displayError != null) ...[
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.grey[100],
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.grey[300]!),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Error Details (Debug Mode):',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.grey[700],
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      displayError.toString(),
                      style: TextStyle(
                        fontFamily: 'monospace',
                        fontSize: 12,
                        color: Colors.grey[700],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
            ],
            Wrap(
              spacing: 12,
              children: [
                if (canRetry)
                  ElevatedButton.icon(
                    onPressed: _retry,
                    icon: const Icon(Icons.refresh),
                    label: Text('Retry${_retryCount > 0 ? ' ($_retryCount/$_maxRetries)' : ''}'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Theme.of(context).primaryColor,
                      foregroundColor: Colors.white,
                    ),
                  ),
                OutlinedButton.icon(
                  onPressed: _reset,
                  icon: const Icon(Icons.home),
                  label: const Text('Go to Home'),
                ),
                if (kDebugMode)
                  TextButton.icon(
                    onPressed: () => _showDetailedError(context),
                    icon: const Icon(Icons.bug_report),
                    label: const Text('Show Details'),
                  ),
              ],
            ),
            if (!canRetry && _retryCount >= _maxRetries) ...[
              const SizedBox(height: 16),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.orange[50],
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.orange[200]!),
                ),
                child: Row(
                  children: [
                    Icon(Icons.warning_amber, color: Colors.orange[700]),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Maximum retry attempts reached. Please restart the app or contact support.',
                        style: TextStyle(color: Colors.orange[700]),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  IconData _getErrorIcon() {
    final severity = _determineErrorSeverity(_error);
    switch (severity) {
      case ErrorSeverity.low:
        return Icons.warning_amber;
      case ErrorSeverity.medium:
        return Icons.error_outline;
      case ErrorSeverity.high:
        return Icons.error;
      case ErrorSeverity.critical:
        return Icons.dangerous;
    }
  }

  Color _getErrorColor() {
    final severity = _determineErrorSeverity(_error);
    switch (severity) {
      case ErrorSeverity.low:
        return Colors.orange;
      case ErrorSeverity.medium:
        return Colors.deepOrange;
      case ErrorSeverity.high:
        return Colors.red;
      case ErrorSeverity.critical:
        return Colors.red[900]!;
    }
  }

  String _getErrorTitle() {
    final severity = _determineErrorSeverity(_error);
    switch (severity) {
      case ErrorSeverity.low:
        return 'Minor Issue Detected';
      case ErrorSeverity.medium:
        return 'Something Went Wrong';
      case ErrorSeverity.high:
        return 'Serious Error Occurred';
      case ErrorSeverity.critical:
        return 'Critical Error';
    }
  }

  ErrorSeverity _determineErrorSeverity(Object? error) {
    if (error == null) return ErrorSeverity.low;
    
    final errorString = error.toString().toLowerCase();
    if (errorString.contains('critical') || errorString.contains('fatal')) {
      return ErrorSeverity.critical;
    } else if (errorString.contains('assertion') || errorString.contains('state')) {
      return ErrorSeverity.high;
    } else if (errorString.contains('null') || errorString.contains('range')) {
      return ErrorSeverity.medium;
    } else {
      return ErrorSeverity.low;
    }
  }

  void _retry() {
    if (_retryCount < _maxRetries) {
      setState(() {
        _error = null;
        _stackTrace = null;
        _retryCount++;
      });
      
      developer.log(
        'ErrorBoundaryWidget retry attempt $_retryCount/$_maxRetries',
        name: 'ErrorBoundaryWidget',
      );
    }
  }

  void _reset() {
    setState(() {
      _error = null;
      _stackTrace = null;
      _retryCount = 0;
    });
    
    // 这里可以导航到首页或安全状态
    developer.log(
      'ErrorBoundaryWidget reset to initial state',
      name: 'ErrorBoundaryWidget',
    );
  }

  void _showDetailedError(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Error Details'),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              if (_error != null) ...[
                const Text('Error:', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(_error.toString(), style: const TextStyle(fontFamily: 'monospace')),
                const SizedBox(height: 16),
              ],
              if (_stackTrace != null) ...[
                const Text('Stack Trace:', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Text(_stackTrace.toString(), style: const TextStyle(fontFamily: 'monospace', fontSize: 10)),
              ],
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }
}

/// 自定义错误处理器，专门处理边界错误
class _BoundaryErrorProcessor implements ErrorProcessor {
  final void Function(Object error, StackTrace? stackTrace) onBoundaryError;
  final String boundaryContext;
  final String instanceId;

  _BoundaryErrorProcessor({
    required this.onBoundaryError,
    required this.boundaryContext,
    required this.instanceId,
  });

  @override
  Future<void> processError(FlutterErrorDetails details, ErrorContext? context) async {
    // 只处理与此边界相关的错误
    if (context?.userAction.contains(instanceId) == true ||
        details.context?.toString().contains(boundaryContext) == true) {
      
      developer.log(
        'Processing boundary error in $boundaryContext: ${details.exception}',
        name: '_BoundaryErrorProcessor',
      );
      
      // 通知边界处理错误
      onBoundaryError(details.exception, details.stack);
    }
  }
}

/// 安全构建器，捕获构建时的异常
class _SafeBuilder extends StatelessWidget {
  final Widget Function() builder;
  final void Function(Object error, StackTrace stackTrace) onError;
  final Widget Function() fallback;

  const _SafeBuilder({
    required this.builder,
    required this.onError,
    required this.fallback,
  });

  @override
  Widget build(BuildContext context) {
    try {
      return builder();
    } catch (error, stackTrace) {
      developer.log(
        'SafeBuilder caught error during build',
        name: '_SafeBuilder',
        level: 1000,
        error: error,
        stackTrace: stackTrace,
      );
      
      onError(error, stackTrace);
      return fallback();
    }
  }
}