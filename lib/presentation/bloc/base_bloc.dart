import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../core/services/error_handler_service.dart';
import '../../core/services/message_service.dart';
import '../../core/services/logging_service.dart';
import '../../core/error/failures.dart';
import '../../injection_container.dart';

/// 基础事件类
abstract class BaseEvent extends Equatable {
  const BaseEvent();
  
  @override
  List<Object?> get props => [];
}

/// 基础状态类
abstract class BaseState extends Equatable {
  const BaseState();
  
  @override
  List<Object?> get props => [];
}

/// 通用错误状态
class ErrorState extends BaseState {
  final String message;
  final Failure? failure;
  final bool canRetry;
  final VoidCallback? onRetry;

  const ErrorState({
    required this.message,
    this.failure,
    this.canRetry = false,
    this.onRetry,
  });

  @override
  List<Object?> get props => [message, failure, canRetry];
}

/// 通用加载状态
class LoadingState extends BaseState {
  final String? message;
  final double? progress;

  const LoadingState({this.message, this.progress});

  @override
  List<Object?> get props => [message, progress];
}

/// 通用初始状态
class InitialState extends BaseState {
  const InitialState();
}

/// 通用成功状态
class SuccessState extends BaseState {
  final String? message;

  const SuccessState({this.message});

  @override
  List<Object?> get props => [message];
}

/// 基础BLoC抽象类
abstract class BaseBloc<Event extends BaseEvent, State extends BaseState>
    extends Bloc<Event, State> {
  
  final ErrorHandlerService _errorHandler = sl<ErrorHandlerService>();
  final MessageService _messageService = sl<MessageService>();
  final LoggingService _loggingService = sl<LoggingService>();

  BaseBloc(super.initialState) {
    // 设置错误处理
    on<Event>(_handleEvent);
  }

  /// 处理事件的统一入口
  Future<void> _handleEvent(Event event, Emitter<State> emit) async {
    try {
      await handleEvent(event, emit);
    } catch (error, stackTrace) {
      await _handleBlocError(error, stackTrace, emit, event);
    }
  }

  /// 子类实现的事件处理方法
  Future<void> handleEvent(Event event, Emitter<State> emit);

  /// 统一的错误处理
  Future<void> _handleBlocError(
    dynamic error,
    StackTrace stackTrace,
    Emitter<State> emit,
    Event event,
  ) async {
    final contextInfo = 'BLoC: ${runtimeType.toString()}, Event: ${event.runtimeType.toString()}';
    
    await _errorHandler.handleError(
      error,
      stackTrace: stackTrace,
      context: contextInfo,
      showUserMessage: shouldShowUserMessage(event, error),
      customUserMessage: getCustomErrorMessage(event, error),
    );

    // 发出错误状态
    final errorState = createErrorState(error, event);
    emit(errorState as State);
  }

  /// 安全地执行异步操作
  Future<T> safeExecute<T>(
    Future<T> Function() operation, {
    String? context,
    T? fallback,
    bool showLoading = true,
    String? loadingMessage,
    bool showUserMessage = true,
    String? customErrorMessage,
  }) async {
    try {
      return await _errorHandler.catchError<T>(
        operation,
        context: context ?? runtimeType.toString(),
        fallback: fallback,
        shouldRethrow: true,
        showUserMessage: showUserMessage,
        customUserMessage: customErrorMessage,
      ) ?? fallback as T;
    } catch (e) {
      rethrow;
    }
  }

  /// 带重试的安全执行
  Future<T> safeExecuteWithRetry<T>(
    Future<T> Function() operation, {
    String? context,
    bool showUserMessage = true,
    String? customErrorMessage,
  }) async {
    return await _errorHandler.handleWithRetry<T>(
      operation,
      context: context ?? runtimeType.toString(),
      showUserMessage: showUserMessage,
      customUserMessage: customErrorMessage,
    );
  }

  /// 发出加载状态
  void emitLoading(Emitter<State> emit, {String? message, double? progress}) {
    emit(LoadingState(message: message, progress: progress) as State);
  }

  /// 发出成功状态
  void emitSuccess(Emitter<State> emit, {String? message}) {
    emit(SuccessState(message: message) as State);
  }

  /// 发出错误状态
  void emitError(Emitter<State> emit, String message, {bool canRetry = false, VoidCallback? onRetry}) {
    emit(ErrorState(
      message: message,
      canRetry: canRetry,
      onRetry: onRetry,
    ) as State);
  }

  /// 处理Failure对象
  void handleFailure(Emitter<State> emit, Failure failure, {bool canRetry = false, VoidCallback? onRetry}) {
    emit(ErrorState(
      message: failure.message,
      failure: failure,
      canRetry: canRetry,
      onRetry: onRetry,
    ) as State);
  }

  /// 显示操作成功消息
  void showSuccessMessage(String operation) {
    _messageService.showOperationSuccess(operation);
  }

  /// 显示操作失败消息
  void showErrorMessage(String operation, {VoidCallback? onRetry}) {
    _messageService.showOperationError(operation, onRetry: onRetry);
  }

  /// 子类可重写：是否显示用户消息
  bool shouldShowUserMessage(Event event, dynamic error) => true;

  /// 子类可重写：获取自定义错误消息
  String? getCustomErrorMessage(Event event, dynamic error) => null;

  /// 子类可重写：创建错误状态
  BaseState createErrorState(dynamic error, Event event) {
    if (error is Failure) {
      return ErrorState(
        message: error.message,
        failure: error,
        canRetry: isRetryableError(error),
        onRetry: canRetryEvent(event) ? () => add(event) : null,
      );
    }

    return ErrorState(
      message: error.toString(),
      canRetry: isRetryableError(error),
      onRetry: canRetryEvent(event) ? () => add(event) : null,
    );
  }

  /// 子类可重写：判断是否可重试的错误
  bool isRetryableError(dynamic error) {
    if (error is Failure) {
      return error is ServerFailure || error is CacheFailure;
    }
    
    final errorString = error.toString().toLowerCase();
    return errorString.contains('network') ||
           errorString.contains('connection') ||
           errorString.contains('timeout');
  }

  /// 子类可重写：判断事件是否可重试
  bool canRetryEvent(Event event) => true;

  /// 记录调试信息
  void logDebug(String message, [dynamic data]) {
    _loggingService.debug('${runtimeType.toString()}: $message', data);
  }

  /// 记录信息
  void logInfo(String message, [dynamic data]) {
    _loggingService.info('${runtimeType.toString()}: $message', data);
  }

  /// 记录警告
  void logWarning(String message, [dynamic error]) {
    _loggingService.warning('${runtimeType.toString()}: $message', error);
  }

  /// 记录错误
  void logError(String message, dynamic error, [StackTrace? stackTrace]) {
    _loggingService.error('${runtimeType.toString()}: $message', error, stackTrace);
  }
}

/// BLoC状态扩展类
extension BlocStateExtensions on BaseState {
  /// 是否为加载状态
  bool get isLoading => this is LoadingState;

  /// 是否为错误状态
  bool get isError => this is ErrorState;

  /// 是否为成功状态
  bool get isSuccess => this is SuccessState;

  /// 是否为初始状态
  bool get isInitial => this is InitialState;

  /// 获取错误消息（如果是错误状态）
  String? get errorMessage => this is ErrorState ? (this as ErrorState).message : null;

  /// 获取加载消息（如果是加载状态）
  String? get loadingMessage => this is LoadingState ? (this as LoadingState).message : null;

  /// 获取加载进度（如果是加载状态）
  double? get loadingProgress => this is LoadingState ? (this as LoadingState).progress : null;

  /// 是否可重试（如果是错误状态）
  bool get canRetry => this is ErrorState ? (this as ErrorState).canRetry : false;

  /// 重试回调（如果是错误状态）
  VoidCallback? get onRetry => this is ErrorState ? (this as ErrorState).onRetry : null;
}

/// BLoC监听器辅助类
class BlocListenerHelper {
  static void handleState<T extends BaseState>(
    T state, {
    void Function()? onLoading,
    void Function(String message)? onError,
    void Function(String? message)? onSuccess,
    void Function()? onInitial,
  }) {
    if (state.isLoading && onLoading != null) {
      onLoading();
    } else if (state.isError && onError != null) {
      onError(state.errorMessage!);
    } else if (state.isSuccess && onSuccess != null) {
      onSuccess((state as SuccessState).message);
    } else if (state.isInitial && onInitial != null) {
      onInitial();
    }
  }
}

/// BLoC UI组件辅助混入
mixin BlocWidgetMixin {
  /// 显示错误对话框
  void showErrorDialog(BuildContext context, String message, {VoidCallback? onRetry}) {
    final messageService = sl<MessageService>();
    if (onRetry != null) {
      messageService.showErrorWithRetry(message, onRetry: onRetry);
    } else {
      messageService.showError(message);
    }
  }

  /// 显示加载对话框
  void showLoadingDialog(BuildContext context, {String? message}) {
    final messageService = sl<MessageService>();
    messageService.showLoading(message ?? '加载中...');
  }

  /// 隐藏加载对话框
  void hideLoadingDialog() {
    final messageService = sl<MessageService>();
    messageService.hideLoading();
  }

  /// 显示成功消息
  void showSuccessMessage(String message) {
    final messageService = sl<MessageService>();
    messageService.showSuccess(message);
  }
}