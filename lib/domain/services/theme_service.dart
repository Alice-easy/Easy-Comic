import '../entities/reader_settings.dart';

abstract class ThemeService {
  /// 应用主题
  Future<void> applyTheme(BackgroundTheme theme);

  /// 获取当前主题
  BackgroundTheme getCurrentTheme();

  /// 监听主题变化
  Stream<BackgroundTheme> watchThemeChanges();

  /// 设置亮度
  Future<void> setBrightness(double brightness);

  /// 获取当前亮度
  double getCurrentBrightness();

  /// 启用/禁用全屏模式
  Future<void> setFullscreenMode(bool enabled);

  /// 检查是否为全屏模式
  bool isFullscreenMode();

  /// 自动调整亮度
  Future<void> autoAdjustBrightness();

  /// 获取系统亮度
  Future<double> getSystemBrightness();

  /// 重置亮度设置
  Future<void> resetBrightness();
}