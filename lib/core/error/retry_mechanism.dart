import 'dart:async';
import 'dart:math';

/// Configuration for retry operations
class RetryConfig {
  final int maxAttempts;
  final Duration initialDelay;
  final double backoffMultiplier;
  final Duration maxDelay;
  final bool enableJitter;
  final double jitterFactor;
  
  const RetryConfig({
    this.maxAttempts = 3,
    this.initialDelay = const Duration(seconds: 1),
    this.backoffMultiplier = 2.0,
    this.maxDelay = const Duration(seconds: 30),
    this.enableJitter = true,
    this.jitterFactor = 0.1,
  });
  
  /// Default config for network operations (1s, 2s, 4s)
  static const RetryConfig network = RetryConfig(
    maxAttempts: 3,
    initialDelay: Duration(seconds: 1),
    backoffMultiplier: 2.0,
    maxDelay: Duration(seconds: 4),
    enableJitter: true,
  );
  
  /// Config for file operations (shorter delays)
  static const RetryConfig file = RetryConfig(
    maxAttempts: 3,
    initialDelay: Duration(milliseconds: 100),
    backoffMultiplier: 2.0,
    maxDelay: Duration(seconds: 1),
    enableJitter: false,
  );
  
  /// Config for database operations
  static const RetryConfig database = RetryConfig(
    maxAttempts: 5,
    initialDelay: Duration(milliseconds: 50),
    backoffMultiplier: 1.5,
    maxDelay: Duration(seconds: 2),
    enableJitter: true,
  );
}

/// Types of errors that should be retried
enum RetryableErrorType {
  network,
  timeout,
  temporary,
  rateLimit,
  serverError,
}

/// Result of a retry operation
class RetryResult<T> {
  final T? result;
  final Exception? lastError;
  final int attemptsMade;
  final Duration totalDuration;
  final bool succeeded;
  
  const RetryResult({
    this.result,
    this.lastError,
    required this.attemptsMade,
    required this.totalDuration,
    required this.succeeded,
  });
}

/// Cancellation token for retry operations
class CancelToken {
  bool _isCancelled = false;
  final Completer<void> _completer = Completer<void>();
  
  /// Cancel the operation
  void cancel() {
    if (!_isCancelled) {
      _isCancelled = true;
      _completer.complete();
    }
  }
  
  /// Check if operation is cancelled
  bool get isCancelled => _isCancelled;
  
  /// Future that completes when cancelled
  Future<void> get cancelled => _completer.future;
}

/// Exception thrown when retry operation is cancelled
class RetryCancelledException implements Exception {
  final String message;
  const RetryCancelledException(this.message);
  
  @override
  String toString() => 'RetryCancelledException: $message';
}

/// Exponential backoff retry mechanism with jitter and cancellation support
class ExponentialBackoffRetry {
  final Random _random = Random();
  
  /// Execute operation with retry logic
  Future<T> execute<T>(
    Future<T> Function() operation, {
    RetryConfig config = const RetryConfig(),
    bool Function(Exception)? shouldRetry,
    CancelToken? cancelToken,
    void Function(int attempt, Exception error)? onRetry,
  }) async {
    final stopwatch = Stopwatch()..start();
    Exception? lastError;
    
    for (int attempt = 1; attempt <= config.maxAttempts; attempt++) {
      // Check for cancellation
      if (cancelToken?.isCancelled == true) {
        throw RetryCancelledException('Operation cancelled after $attempt attempts');
      }
      
      try {
        final result = await operation();
        stopwatch.stop();
        return result;
      } catch (e) {
        lastError = e is Exception ? e : Exception(e.toString());
        
        // Check if we should retry this error
        if (shouldRetry != null && !shouldRetry(lastError)) {
          break;
        }
        
        // Check if this is the last attempt
        if (attempt >= config.maxAttempts) {
          break;
        }
        
        // Calculate delay for next attempt
        final delay = _calculateDelay(attempt, config);
        
        // Notify retry callback
        onRetry?.call(attempt, lastError);
        
        // Wait before next attempt (with cancellation support)
        await _delayWithCancellation(delay, cancelToken);
      }
    }
    
    stopwatch.stop();
    throw lastError ?? Exception('Operation failed after ${config.maxAttempts} attempts');
  }
  
  /// Execute operation and return result with metadata
  Future<RetryResult<T>> executeWithResult<T>(
    Future<T> Function() operation, {
    RetryConfig config = const RetryConfig(),
    bool Function(Exception)? shouldRetry,
    CancelToken? cancelToken,
    void Function(int attempt, Exception error)? onRetry,
  }) async {
    final stopwatch = Stopwatch()..start();
    Exception? lastError;
    int attempts = 0;
    
    try {
      final result = await execute<T>(
        operation,
        config: config,
        shouldRetry: shouldRetry,
        cancelToken: cancelToken,
        onRetry: (attempt, error) {
          attempts = attempt;
          lastError = error;
          onRetry?.call(attempt, error);
        },
      );
      
      stopwatch.stop();
      return RetryResult<T>(
        result: result,
        attemptsMade: attempts + 1,
        totalDuration: stopwatch.elapsed,
        succeeded: true,
      );
    } catch (e) {
      stopwatch.stop();
      return RetryResult<T>(
        lastError: e is Exception ? e : Exception(e.toString()),
        attemptsMade: attempts,
        totalDuration: stopwatch.elapsed,
        succeeded: false,
      );
    }
  }
  
  Duration _calculateDelay(int attempt, RetryConfig config) {
    // Calculate base delay with exponential backoff
    final baseDelay = config.initialDelay.inMilliseconds * 
        pow(config.backoffMultiplier, attempt - 1);
    
    // Apply maximum delay limit
    final cappedDelay = min(baseDelay, config.maxDelay.inMilliseconds.toDouble());
    
    // Add jitter if enabled
    double finalDelay = cappedDelay.toDouble();
    if (config.enableJitter) {
      final jitter = cappedDelay * config.jitterFactor * (_random.nextDouble() - 0.5);
      finalDelay = cappedDelay + jitter;
    }
    
    return Duration(milliseconds: max(0, finalDelay.round()));
  }
  
  Future<void> _delayWithCancellation(Duration delay, CancelToken? cancelToken) async {
    if (cancelToken == null) {
      await Future.delayed(delay);
      return;
    }
    
    final delayFuture = Future.delayed(delay);
    final cancelFuture = cancelToken.cancelled;
    
    final result = await Future.any([delayFuture, cancelFuture]);
    
    if (cancelToken.isCancelled) {
      throw RetryCancelledException('Operation cancelled during delay');
    }
  }
  
  /// Default retry condition for common error types
  static bool defaultShouldRetry(Exception error) {
    final errorString = error.toString().toLowerCase();
    
    // Network-related errors
    if (errorString.contains('connection') ||
        errorString.contains('timeout') ||
        errorString.contains('network') ||
        errorString.contains('host') ||
        errorString.contains('socket')) {
      return true;
    }
    
    // HTTP status codes that should be retried
    if (errorString.contains('429') || // Rate limit
        errorString.contains('500') || // Internal server error
        errorString.contains('502') || // Bad gateway
        errorString.contains('503') || // Service unavailable
        errorString.contains('504')) { // Gateway timeout
      return true;
    }
    
    // File system temporary errors
    if (errorString.contains('temporarily unavailable') ||
        errorString.contains('resource busy') ||
        errorString.contains('try again')) {
      return true;
    }
    
    return false;
  }
  
  /// Retry condition for network operations
  static bool networkShouldRetry(Exception error) {
    return defaultShouldRetry(error);
  }
  
  /// Retry condition for file operations
  static bool fileShouldRetry(Exception error) {
    final errorString = error.toString().toLowerCase();
    
    // Don't retry permission or not found errors
    if (errorString.contains('permission') ||
        errorString.contains('not found') ||
        errorString.contains('access denied')) {
      return false;
    }
    
    return defaultShouldRetry(error);
  }
  
  /// Retry condition for database operations
  static bool databaseShouldRetry(Exception error) {
    final errorString = error.toString().toLowerCase();
    
    // Don't retry constraint violations or schema errors
    if (errorString.contains('constraint') ||
        errorString.contains('schema') ||
        errorString.contains('syntax')) {
      return false;
    }
    
    // Retry lock and busy errors
    if (errorString.contains('locked') ||
        errorString.contains('busy') ||
        errorString.contains('deadlock')) {
      return true;
    }
    
    return defaultShouldRetry(error);
  }
}

/// Convenience functions for common retry scenarios
class RetryUtils {
  static final _retry = ExponentialBackoffRetry();
  
  /// Retry network operation with default config
  static Future<T> retryNetwork<T>(
    Future<T> Function() operation, {
    CancelToken? cancelToken,
    void Function(int attempt, Exception error)? onRetry,
  }) {
    return _retry.execute(
      operation,
      config: RetryConfig.network,
      shouldRetry: ExponentialBackoffRetry.networkShouldRetry,
      cancelToken: cancelToken,
      onRetry: onRetry,
    );
  }
  
  /// Retry file operation with default config
  static Future<T> retryFile<T>(
    Future<T> Function() operation, {
    CancelToken? cancelToken,
    void Function(int attempt, Exception error)? onRetry,
  }) {
    return _retry.execute(
      operation,
      config: RetryConfig.file,
      shouldRetry: ExponentialBackoffRetry.fileShouldRetry,
      cancelToken: cancelToken,
      onRetry: onRetry,
    );
  }
  
  /// Retry database operation with default config
  static Future<T> retryDatabase<T>(
    Future<T> Function() operation, {
    CancelToken? cancelToken,
    void Function(int attempt, Exception error)? onRetry,
  }) {
    return _retry.execute(
      operation,
      config: RetryConfig.database,
      shouldRetry: ExponentialBackoffRetry.databaseShouldRetry,
      cancelToken: cancelToken,
      onRetry: onRetry,
    );
  }
}