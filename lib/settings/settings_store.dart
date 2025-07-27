import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

class SettingsStore with ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.system;
  ThemeMode get themeMode => _themeMode;

  SettingsStore() {
    final dispatcher = SchedulerBinding.instance.platformDispatcher;
    dispatcher.onPlatformBrightnessChanged = () {
      final brightness = dispatcher.platformBrightness;
      _themeMode = brightness == Brightness.dark
          ? ThemeMode.dark
          : ThemeMode.light;
      notifyListeners();
    };
  }

  void setThemeMode(ThemeMode mode) {
    _themeMode = mode;
    notifyListeners();
  }
}
