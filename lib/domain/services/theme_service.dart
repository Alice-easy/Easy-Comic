import 'package:flutter/material.dart';

abstract class ThemeService {
  /// 应用主题模式
  Future<void> setThemeMode(ThemeMode mode);

  /// 获取当前主题模式
  Future<ThemeMode> getThemeMode();

  /// 监听主题模式变化
  Stream<ThemeMode> watchThemeMode();

  /// 设置亮度
  Future<void> setBrightness(double brightness);

  /// 获取当前亮度
  Future<double> getBrightness();

  /// 监听亮度变化
  Stream<double> watchBrightness();

  /// 启用/禁用全屏模式
  Future<void> setFullscreenMode(bool enabled);

  /// 检查是否为全屏模式
  Future<bool> isFullscreen();
}