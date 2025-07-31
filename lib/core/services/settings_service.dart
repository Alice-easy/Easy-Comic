// lib/core/services/settings_service.dart

import 'package:shared_preferences/shared_preferences.dart';

// 定义设置的枚举类型
enum ReadingMode { PageView, Continuous }
enum ReadingDirection { LTR, RTL }
enum AppTheme { Light, Dark }

/// Settings Service 接口
///
/// 定义了应用设置的契约，用于获取和存储用户的偏好设置。
abstract class SettingsService {
  // 阅读模式
  Future<ReadingMode> getReadingMode();
  Future<void> setReadingMode(ReadingMode mode);

  // 阅读方向
  Future<ReadingDirection> getReadingDirection();
  Future<void> setReadingDirection(ReadingDirection direction);

  // 应用主题
  Future<AppTheme> getAppTheme();
  Future<void> setAppTheme(AppTheme theme);

  // 自动翻页间隔
  Future<int> getAutoPageInterval();
  Future<void> setAutoPageInterval(int interval);
}

/// Settings Service 实现
///
/// 使用 `shared_preferences` 来持久化存储和读取用户的设置。
class SettingsServiceImpl implements SettingsService {
  final SharedPreferences _prefs;

  SettingsServiceImpl(this._prefs);

  // --- Keys ---
  static const String _readingModeKey = 'reading_mode';
  static const String _readingDirectionKey = 'reading_direction';
  static const String _appThemeKey = 'app_theme';
  static const String _autoPageIntervalKey = 'auto_page_interval';

  // --- Reading Mode ---
  @override
  Future<ReadingMode> getReadingMode() async {
    final modeString = _prefs.getString(_readingModeKey);
    return ReadingMode.values.firstWhere(
      (e) => e.toString() == modeString,
      orElse: () => ReadingMode.PageView, // 默认值
    );
  }

  @override
  Future<void> setReadingMode(ReadingMode mode) async {
    await _prefs.setString(_readingModeKey, mode.toString());
  }

  // --- Reading Direction ---
  @override
  Future<ReadingDirection> getReadingDirection() async {
    final directionString = _prefs.getString(_readingDirectionKey);
    return ReadingDirection.values.firstWhere(
      (e) => e.toString() == directionString,
      orElse: () => ReadingDirection.LTR, // 默认值
    );
  }

  @override
  Future<void> setReadingDirection(ReadingDirection direction) async {
    await _prefs.setString(_readingDirectionKey, direction.toString());
  }

  // --- App Theme ---
  @override
  Future<AppTheme> getAppTheme() async {
    final themeString = _prefs.getString(_appThemeKey);
    return AppTheme.values.firstWhere(
      (e) => e.toString() == themeString,
      orElse: () => AppTheme.Light, // 默认值
    );
  }

  @override
  Future<void> setAppTheme(AppTheme theme) async {
    await _prefs.setString(_appThemeKey, theme.toString());
  }

  // --- Auto Page Interval ---
  @override
  Future<int> getAutoPageInterval() async {
    return _prefs.getInt(_autoPageIntervalKey) ?? 5; // 默认值 5 秒
  }

  @override
  Future<void> setAutoPageInterval(int interval) async {
    await _prefs.setInt(_autoPageIntervalKey, interval);
  }
}