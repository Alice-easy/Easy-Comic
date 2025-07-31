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

enum AutoPageEvent {
  nextPage,
  pause,
  resume,
  stop,
}

class AutoPageConfig {
  final int defaultInterval;
  final bool pauseOnUserInteraction;
  final bool pauseOnAppBackground;
  final bool pauseOnLowBattery;
  final bool stopAtLastPage;

  const AutoPageConfig({
    this.defaultInterval = 5,
    this.pauseOnUserInteraction = true,
    this.pauseOnAppBackground = true,
    this.pauseOnLowBattery = true,
    this.stopAtLastPage = true,
  });
}