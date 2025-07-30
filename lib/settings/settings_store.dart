import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

class SettingsStore with ChangeNotifier {
  VoidCallback? _brightnessCallback;

  SettingsStore() {
    final dispatcher = SchedulerBinding.instance.platformDispatcher;
    _brightnessCallback = () {
      final brightness = dispatcher.platformBrightness;
      _themeMode = brightness == Brightness.dark
          ? ThemeMode.dark
          : ThemeMode.light;
      notifyListeners();
    };
    dispatcher.onPlatformBrightnessChanged = _brightnessCallback;
  }
  
  ThemeMode _themeMode = ThemeMode.system;
  ThemeMode get themeMode => _themeMode;

  void setThemeMode(ThemeMode mode) {
    _themeMode = mode;
    notifyListeners();
  }

  @override
  void dispose() {
    // 清理平台亮度监听器
    final dispatcher = SchedulerBinding.instance.platformDispatcher;
    if (dispatcher.onPlatformBrightnessChanged == _brightnessCallback) {
      dispatcher.onPlatformBrightnessChanged = null;
    }
    _brightnessCallback = null;
    super.dispose();
  }
}
