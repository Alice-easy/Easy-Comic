import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// 消息类型
enum MessageType {
  success,
  error,
  warning,
  info,
}

/// 消息优先级
enum MessagePriority {
  low,
  normal,
  high,
  critical,
}

/// 消息项
class MessageItem {
  final String message;
  final MessageType type;
  final MessagePriority priority;
  final Duration? duration;
  final String? actionLabel;
  final VoidCallback? onAction;
  final DateTime timestamp;
  final String id;

  MessageItem({
    required this.message,
    required this.type,
    this.priority = MessagePriority.normal,
    this.duration,
    this.actionLabel,
    this.onAction,
    DateTime? timestamp,
  }) : timestamp = timestamp ?? DateTime.now(),
       id = DateTime.now().millisecondsSinceEpoch.toString();

  /// 获取默认持续时间
  Duration get defaultDuration {
    switch (type) {
      case MessageType.success:
        return const Duration(seconds: 3);
      case MessageType.error:
        return const Duration(seconds: 5);
      case MessageType.warning:
        return const Duration(seconds: 4);
      case MessageType.info:
        return const Duration(seconds: 3);
    }
  }

  /// 获取图标
  IconData get icon {
    switch (type) {
      case MessageType.success:
        return Icons.check_circle;
      case MessageType.error:
        return Icons.error;
      case MessageType.warning:
        return Icons.warning;
      case MessageType.info:
        return Icons.info;
    }
  }

  /// 获取颜色
  Color getColor(BuildContext context) {
    final theme = Theme.of(context);
    switch (type) {
      case MessageType.success:
        return Colors.green;
      case MessageType.error:
        return theme.colorScheme.error;
      case MessageType.warning:
        return Colors.orange;
      case MessageType.info:
        return theme.colorScheme.primary;
    }
  }
}

/// 统一的用户消息服务
class MessageService {
  static final MessageService _instance = MessageService._internal();
  factory MessageService() => _instance;
  MessageService._internal();

  final List<MessageItem> _messageQueue = [];
  bool _isShowingMessage = false;
  BuildContext? _context;

  /// 设置当前上下文
  void setContext(BuildContext context) {
    _context = context;
  }

  /// 显示成功消息
  void showSuccess(
    String message, {
    Duration? duration,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    _showMessage(MessageItem(
      message: message,
      type: MessageType.success,
      duration: duration,
      actionLabel: actionLabel,
      onAction: onAction,
    ));
  }

  /// 显示错误消息
  void showError(
    String message, {
    Duration? duration,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    _showMessage(MessageItem(
      message: message,
      type: MessageType.error,
      priority: MessagePriority.high,
      duration: duration,
      actionLabel: actionLabel,
      onAction: onAction,
    ));
  }

  /// 显示错误消息（带重试按钮）
  void showErrorWithRetry(
    String message, {
    required VoidCallback onRetry,
    Duration? duration,
  }) {
    showError(
      message,
      duration: duration,
      actionLabel: '重试',
      onAction: onRetry,
    );
  }

  /// 显示警告消息
  void showWarning(
    String message, {
    Duration? duration,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    _showMessage(MessageItem(
      message: message,
      type: MessageType.warning,
      duration: duration,
      actionLabel: actionLabel,
      onAction: onAction,
    ));
  }

  /// 显示信息消息
  void showInfo(
    String message, {
    Duration? duration,
    String? actionLabel,
    VoidCallback? onAction,
  }) {
    _showMessage(MessageItem(
      message: message,
      type: MessageType.info,
      duration: duration,
      actionLabel: actionLabel,
      onAction: onAction,
    ));
  }

  /// 显示加载消息
  void showLoading(String message) {
    if (_context == null) return;

    showDialog(
      context: _context!,
      barrierDismissible: false,
      builder: (context) => PopScope(
        canPop: false,
        child: AlertDialog(
          content: Row(
            children: [
              const CircularProgressIndicator(),
              const SizedBox(width: 16),
              Expanded(child: Text(message)),
            ],
          ),
        ),
      ),
    );
  }

  /// 隐藏加载对话框
  void hideLoading() {
    if (_context == null) return;
    Navigator.of(_context!).pop();
  }

  /// 显示操作成功反馈
  void showOperationSuccess(String operation) {
    showSuccess('$operation 成功');
    HapticFeedback.lightImpact(); // 触觉反馈
  }

  /// 显示操作失败反馈
  void showOperationError(String operation, {VoidCallback? onRetry}) {
    if (onRetry != null) {
      showErrorWithRetry('$operation 失败', onRetry: onRetry);
    } else {
      showError('$operation 失败');
    }
    HapticFeedback.heavyImpact(); // 触觉反馈
  }

  /// 显示网络错误
  void showNetworkError({VoidCallback? onRetry}) {
    showErrorWithRetry(
      '网络连接失败，请检查网络设置',
      onRetry: onRetry ?? () {},
    );
  }

  /// 显示确认对话框
  Future<bool> showConfirmDialog(
    String title,
    String content, {
    String confirmText = '确认',
    String cancelText = '取消',
    bool isDestructive = false,
  }) async {
    if (_context == null) return false;

    final result = await showDialog<bool>(
      context: _context!,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(content),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: Text(cancelText),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: isDestructive
                ? TextButton.styleFrom(
                    foregroundColor: Theme.of(context).colorScheme.error,
                  )
                : null,
            child: Text(confirmText),
          ),
        ],
      ),
    );

    return result ?? false;
  }

  /// 显示选择对话框
  Future<T?> showChoiceDialog<T>(
    String title,
    List<MapEntry<T, String>> choices, {
    String? content,
  }) async {
    if (_context == null) return null;

    return await showDialog<T>(
      context: _context!,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: content != null ? Text(content) : null,
        actions: [
          ...choices.map((choice) => TextButton(
            onPressed: () => Navigator.of(context).pop(choice.key),
            child: Text(choice.value),
          )),
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
        ],
      ),
    );
  }

  /// 显示底部操作表
  Future<T?> showBottomSheet<T>(
    String title,
    List<MapEntry<T, String>> actions, {
    bool isDestructive = false,
  }) async {
    if (_context == null) return null;

    return await showModalBottomSheet<T>(
      context: _context!,
      builder: (context) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text(
                title,
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            const Divider(height: 1),
            ...actions.map((action) => ListTile(
              title: Text(
                action.value,
                style: isDestructive
                    ? TextStyle(color: Theme.of(context).colorScheme.error)
                    : null,
              ),
              onTap: () => Navigator.of(context).pop(action.key),
            )),
            const SizedBox(height: 8),
            ListTile(
              title: const Text('取消'),
              onTap: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      ),
    );
  }

  /// 显示输入对话框
  Future<String?> showInputDialog(
    String title, {
    String? content,
    String? initialValue,
    String? hintText,
    String confirmText = '确认',
    String cancelText = '取消',
    TextInputType? keyboardType,
    int? maxLength,
    String? Function(String?)? validator,
  }) async {
    if (_context == null) return null;

    final controller = TextEditingController(text: initialValue);
    final formKey = GlobalKey<FormState>();

    final result = await showDialog<String>(
      context: _context!,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Form(
          key: formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (content != null) ...[
                Text(content),
                const SizedBox(height: 16),
              ],
              TextFormField(
                controller: controller,
                decoration: InputDecoration(hintText: hintText),
                keyboardType: keyboardType,
                maxLength: maxLength,
                validator: validator,
                autofocus: true,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text(cancelText),
          ),
          TextButton(
            onPressed: () {
              if (formKey.currentState?.validate() ?? true) {
                Navigator.of(context).pop(controller.text);
              }
            },
            child: Text(confirmText),
          ),
        ],
      ),
    );

    controller.dispose();
    return result;
  }

  /// 私有方法：显示消息
  void _showMessage(MessageItem messageItem) {
    if (_context == null) return;

    // 添加到队列
    _messageQueue.add(messageItem);

    // 如果当前没有显示消息，则开始处理队列
    if (!_isShowingMessage) {
      _processMessageQueue();
    }
  }

  /// 处理消息队列
  void _processMessageQueue() async {
    if (_messageQueue.isEmpty || _isShowingMessage) return;

    _isShowingMessage = true;

    while (_messageQueue.isNotEmpty) {
      // 按优先级排序
      _messageQueue.sort((a, b) => b.priority.index.compareTo(a.priority.index));
      
      final message = _messageQueue.removeAt(0);
      await _displayMessage(message);
    }

    _isShowingMessage = false;
  }

  /// 显示单个消息
  Future<void> _displayMessage(MessageItem message) async {
    if (_context == null) return;

    final messenger = ScaffoldMessenger.of(_context!);
    final completer = Completer<void>();

    final snackBar = SnackBar(
      content: Row(
        children: [
          Icon(message.icon, color: Colors.white),
          const SizedBox(width: 8),
          Expanded(child: Text(message.message)),
        ],
      ),
      backgroundColor: message.getColor(_context!),
      duration: message.duration ?? message.defaultDuration,
      action: message.actionLabel != null && message.onAction != null
          ? SnackBarAction(
              label: message.actionLabel!,
              textColor: Colors.white,
              onPressed: message.onAction!,
            )
          : null,
    );

    messenger.showSnackBar(snackBar).closed.then((_) {
      if (!completer.isCompleted) {
        completer.complete();
      }
    });

    await completer.future;
  }

  /// 清除所有消息
  void clearMessages() {
    _messageQueue.clear();
    if (_context != null) {
      ScaffoldMessenger.of(_context!).clearSnackBars();
    }
  }

  /// 获取消息历史
  List<MessageItem> get messageHistory => List.unmodifiable(_messageQueue);
}