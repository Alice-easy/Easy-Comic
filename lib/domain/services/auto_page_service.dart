import 'package:easy_comic/domain/entities/reader_settings.dart';

abstract class AutoPageService {
  /// 开始自动翻页
  Future<void> startAutoPage(int intervalSeconds);

  /// 停止自动翻页
  Future<void> stopAutoPage();

  /// 暂停自动翻页
  Future<void> pauseAutoPage();

  /// 恢复自动翻页
  Future<void> resumeAutoPage();

  /// 检查是否正在自动翻页
  bool get isAutoPageActive;

  /// 检查是否已暂停
  bool get isAutoPagePaused;

  /// 获取当前间隔时间
  int get currentInterval;

  /// 设置自动翻页间隔
  Future<void> setInterval(int intervalSeconds);

  /// 监听自动翻页状态变化
  Stream<AutoPageState> watchAutoPageState();

  /// 监听自动翻页事件（用于触发翻页）
  Stream<AutoPageEvent> get autoPageEventStream;

  /// 重置自动翻页定时器
  Future<void> resetTimer();

  /// 当用户交互时暂停（临时）
  Future<void> pauseForUserInteraction();

  /// 设置自动翻页配置
  Future<void> setAutoPageConfig(AutoPageConfig config);
}

class AutoPageState {
  final bool isActive;
  final bool isPaused;
  final int intervalSeconds;
  final int remainingSeconds;

  const AutoPageState({
    required this.isActive,
    required this.isPaused,
    required this.intervalSeconds,
    required this.remainingSeconds,
  });
}

class AutoPageEvent {
  final AutoPageEventType type;
  final dynamic data;

  const AutoPageEvent(this.type, {this.data});

  factory AutoPageEvent.nextPage() => AutoPageEvent(AutoPageEventType.nextPage);
  factory AutoPageEvent.pageChanged(int newPage) =>
      AutoPageEvent(AutoPageEventType.pageChanged, data: newPage);
  factory AutoPageEvent.completed() => AutoPageEvent(AutoPageEventType.completed);
  factory AutoPageEvent.started() => AutoPageEvent(AutoPageEventType.started);
  factory AutoPageEvent.stop() => AutoPageEvent(AutoPageEventType.stop);
  factory AutoPageEvent.pause() => AutoPageEvent(AutoPageEventType.pause);
  factory AutoPageEvent.resume() => AutoPageEvent(AutoPageEventType.resume);
  factory AutoPageEvent.intervalChanged(Duration interval) =>
      AutoPageEvent(AutoPageEventType.intervalChanged, data: interval);
}

enum AutoPageEventType {
  nextPage,
  pause,
  resume,
  stop,
  pageChanged,
  completed,
  started,
  intervalChanged,
}