import 'package:drift/drift.dart';
import '../data/drift_db.dart';
import '../models/reader_models.dart' as models;

/// Service for managing reader settings and preferences
class ReaderSettingsService {
  final DriftDb _db;

  ReaderSettingsService(this._db);

  /// Get current reader settings, creating default if none exist
  Future<models.ReaderSettings> getSettings([String? userId]) async {
    var settingsData = await _db.getReaderSettings(userId);
    
    if (settingsData == null) {
      // Create default settings if none exist
      await _createDefaultSettings(userId);
      settingsData = await _db.getReaderSettings(userId);
    }
    
    return models.ReaderSettings.fromData(settingsData!);
  }

  /// Update reader settings
  Future<void> updateSettings(models.ReaderSettings settings) async {
    final settingsData = await _db.getReaderSettings(settings.userId);
    if (settingsData != null) {
      await _db.updateReaderSettings(settingsData.copyWith(
        readingMode: settings.readingMode.toValueString(),
        navigationDirection: settings.navigationDirection.toValueString(),
        backgroundTheme: settings.backgroundTheme.toValueString(),
        transitionType: settings.transitionType.toValueString(),
        brightness: settings.brightness,
        showThumbnails: settings.showThumbnails,
        updatedAt: DateTime.now(),
      ));
    } else {
      // Create new settings if none exist
      await _createDefaultSettings(settings.userId);
      final newSettingsData = await _db.getReaderSettings(settings.userId);
      if (newSettingsData != null) {
        await _db.updateReaderSettings(newSettingsData.copyWith(
          readingMode: settings.readingMode.toValueString(),
          navigationDirection: settings.navigationDirection.toValueString(),
          backgroundTheme: settings.backgroundTheme.toValueString(),
          transitionType: settings.transitionType.toValueString(),
          brightness: settings.brightness,
          showThumbnails: settings.showThumbnails,
          updatedAt: DateTime.now(),
        ));
      }
    }
  }

  /// Update specific setting without affecting others
  Future<void> updateReadingMode(models.ReadingMode mode, [String? userId]) async {
    final settings = await getSettings(userId);
    await updateSettings(settings.copyWith(readingMode: mode));
  }

  /// Update navigation direction
  Future<void> updateNavigationDirection(models.NavigationDirection direction, [String? userId]) async {
    final settings = await getSettings(userId);
    await updateSettings(settings.copyWith(navigationDirection: direction));
  }

  /// Update background theme
  Future<void> updateBackgroundTheme(models.BackgroundTheme theme, [String? userId]) async {
    final settings = await getSettings(userId);
    await updateSettings(settings.copyWith(backgroundTheme: theme));
  }

  /// Update transition type
  Future<void> updateTransitionType(models.TransitionType type, [String? userId]) async {
    final settings = await getSettings(userId);
    await updateSettings(settings.copyWith(transitionType: type));
  }

  /// Update brightness level (0.0 to 1.0)
  Future<void> updateBrightness(double brightness, [String? userId]) async {
    final settings = await getSettings(userId);
    final clampedBrightness = brightness.clamp(0.0, 1.0);
    await updateSettings(settings.copyWith(brightness: clampedBrightness));
  }

  /// Toggle thumbnail visibility
  Future<void> toggleThumbnails([String? userId]) async {
    final settings = await getSettings(userId);
    await updateSettings(settings.copyWith(showThumbnails: !settings.showThumbnails));
  }

  /// Reset settings to default values
  Future<void> resetToDefaults([String? userId]) async {
    final settingsData = await _db.getReaderSettings(userId);
    if (settingsData != null) {
      await _db.updateReaderSettings(settingsData.copyWith(
        readingMode: models.ReadingMode.single.toValueString(),
        navigationDirection: models.NavigationDirection.horizontal.toValueString(),
        backgroundTheme: models.BackgroundTheme.black.toValueString(),
        transitionType: models.TransitionType.none.toValueString(),
        brightness: 1.0,
        showThumbnails: true,
        updatedAt: DateTime.now(),
      ));
    } else {
      await _createDefaultSettings(userId);
    }
  }

  /// Check if custom settings exist
  Future<bool> hasCustomSettings([String? userId]) async {
    final settings = await getSettings(userId);
    final defaultSettings = models.ReaderSettings.defaultSettings(userId);
    
    return settings.readingMode != defaultSettings.readingMode ||
           settings.navigationDirection != defaultSettings.navigationDirection ||
           settings.backgroundTheme != defaultSettings.backgroundTheme ||
           settings.transitionType != defaultSettings.transitionType ||
           settings.brightness != defaultSettings.brightness ||
           settings.showThumbnails != defaultSettings.showThumbnails;
  }

  /// Create default settings entry
  Future<void> _createDefaultSettings([String? userId]) async {
    await _db.into(_db.readerSettings).insert(
      ReaderSettingsCompanion.insert(
        userId: Value(userId),
        readingMode: Value(models.ReadingMode.single.toValueString()),
        navigationDirection: Value(models.NavigationDirection.horizontal.toValueString()),
        backgroundTheme: Value(models.BackgroundTheme.black.toValueString()),
        transitionType: Value(models.TransitionType.none.toValueString()),
        brightness: Value(1.0),
        showThumbnails: Value(true),
        updatedAt: DateTime.now(),
      ),
    );
  }

  /// Get reading preferences summary for display
  Future<Map<String, dynamic>> getPreferencesSummary([String? userId]) async {
    final settings = await getSettings(userId);
    
    return {
      'readingMode': settings.readingMode.displayName,
      'navigationDirection': settings.navigationDirection.displayName,
      'backgroundTheme': settings.backgroundTheme.displayName,
      'transitionType': settings.transitionType.displayName,
      'brightness': '${(settings.brightness * 100).round()}%',
      'showThumbnails': settings.showThumbnails ? 'Enabled' : 'Disabled',
    };
  }

  /// Stream of settings changes for reactive UI
  Stream<models.ReaderSettings> watchSettings([String? userId]) async* {
    // Initial settings
    yield await getSettings(userId);
    
    // Watch for database changes
    await for (final _ in _db.select(_db.readerSettings).watch()) {
      yield await getSettings(userId);
    }
  }
}