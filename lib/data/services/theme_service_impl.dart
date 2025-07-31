import 'dart:async';
import '../../domain/services/theme_service.dart';
import '../../domain/repositories/settings_repository.dart';

class ThemeServiceImpl implements ThemeService {
  final SettingsRepository settingsRepository;
  bool _isDarkMode = false;
  double _brightness = 0.5;
  StreamController<bool>? _themeController;
  StreamController<double>? _brightnessController;

  ThemeServiceImpl({required this.settingsRepository});

  @override
  Future<bool> isDarkMode() async {
    return _isDarkMode;
  }

  @override
  Future<void> setDarkMode(bool isDark) async {
    if (_isDarkMode != isDark) {
      _isDarkMode = isDark;
      _themeController?.add(isDark);
    }
  }

  @override
  Future<void> toggleTheme() async {
    await setDarkMode(!_isDarkMode);
  }

  @override
  Stream<bool> watchThemeChanges() {
    _themeController ??= StreamController<bool>.broadcast();
    return _themeController!.stream;
  }

  @override
  Future<double> getBrightness() async {
    return _brightness;
  }

  @override
  Future<void> setBrightness(double brightness) async {
    final clampedBrightness = brightness.clamp(0.0, 1.0);
    if (_brightness != clampedBrightness) {
      _brightness = clampedBrightness;
      _brightnessController?.add(clampedBrightness);
    }
  }

  @override
  Stream<double> watchBrightnessChanges() {
    _brightnessController ??= StreamController<double>.broadcast();
    return _brightnessController!.stream;
  }

  @override
  void dispose() {
    _themeController?.close();
    _brightnessController?.close();
  }
}