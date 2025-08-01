import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../domain/services/theme_service.dart';
import '../../domain/repositories/settings_repository.dart';

class ThemeServiceImpl implements ThemeService {
  final SettingsRepository settingsRepository;
  final _themeModeController = StreamController<ThemeMode>.broadcast();

  ThemeServiceImpl({required this.settingsRepository});

  @override
  Future<ThemeMode> getThemeMode() async {
    return await settingsRepository.getThemeMode();
  }

  @override
  Future<void> setThemeMode(ThemeMode mode) async {
    await settingsRepository.setThemeMode(mode);
    _themeModeController.add(mode);
  }

  @override
  Stream<ThemeMode> watchThemeMode() {
    return _themeModeController.stream;
  }

  @override
  Future<double> getBrightness() async {
    return await settingsRepository.getBrightness();
  }

  @override
  Future<void> setBrightness(double brightness) async {
    await settingsRepository.setBrightness(brightness);
  }

  @override
  Stream<double> watchBrightness() {
    // This should ideally be implemented in SettingsRepository if it needs to be reactive
    // For now, returning an empty stream as the repository doesn't support watching.
    return Stream.value(0.5);
  }

  @override
  Future<bool> isFullscreen() async {
    // This is a simplification. A more robust solution would involve a platform channel
    // to query the current UI mode, as SystemChrome.setEnabledSystemUIMode is a setter.
    // For now, we'll assume it's not in fullscreen by default if we haven't explicitly set it.
    return false;
  }

  @override
  Future<void> setFullscreenMode(bool enabled) async {
    if (enabled) {
      await SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersive);
    } else {
      await SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    }
  }

  void dispose() {
    _themeModeController.close();
  }
}