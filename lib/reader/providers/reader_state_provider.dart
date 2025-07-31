import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/drift_db.dart';
import '../../main.dart';
import '../../models/reader_models.dart';

/// Provider for the reader state notifier
final readerStateProvider = StateNotifierProvider.family<ReaderStateNotifier, ReaderState, int>(
  (ref, comicId) {
    final db = ref.watch(dbProvider);
    return ReaderStateNotifier(db, comicId);
  },
);

/// State notifier for managing reader state
class ReaderStateNotifier extends StateNotifier<ReaderState> {
  final DriftDb _db;
  final int _comicId;
  Timer? _debounceTimer;
  
  ReaderStateNotifier(this._db, this._comicId) : super(ReaderState.initial) {
    _loadSettings();
    _loadCustomPageOrder();
  }

  @override
  void dispose() {
    _debounceTimer?.cancel();
    super.dispose();
  }

  /// Load reader settings from database
  Future<void> _loadSettings() async {
    state = state.copyWith(isLoading: true);
    
    try {
      final settings = await _db.getReaderSettings();
      if (settings != null) {
        state = state.copyWith(
          mode: models.ReadingMode.fromString(settings.readingMode),
          direction: models.NavigationDirection.fromString(settings.navigationDirection),
          backgroundTheme: models.BackgroundTheme.fromString(settings.backgroundTheme),
          transitionType: models.TransitionType.fromString(settings.transitionType),
          brightness: settings.brightness,
          showThumbnails: settings.showThumbnails,
          isLoading: false,
          error: null,
        );
      } else {
        state = state.copyWith(isLoading: false);
      }
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: 'Failed to load settings: $e',
      );
    }
  }

  /// Load custom page order for the current comic
  Future<void> _loadCustomPageOrder() async {
    try {
      final customOrder = await _db.getCustomPageOrder(_comicId);
      if (customOrder.isNotEmpty) {
        final orderList = customOrder
            .map((order) => order.originalIndex)
            .toList();
        state = state.copyWith(customPageOrder: orderList);
      }
    } catch (e) {
      // Custom page order is optional, don't error if it fails
    }
  }

  /// Set reading mode and persist to database
  Future<void> setReadingMode(models.ReadingMode mode) async {
    if (state.mode == mode) return;
    
    state = state.copyWith(mode: mode);
    await _debouncedSave();
  }

  /// Set navigation direction and persist to database
  Future<void> setNavigationDirection(models.NavigationDirection direction) async {
    if (state.direction == direction) return;
    
    state = state.copyWith(direction: direction);
    await _debouncedSave();
  }

  /// Set background theme and persist to database
  Future<void> setBackgroundTheme(models.BackgroundTheme theme) async {
    if (state.backgroundTheme == theme) return;
    
    state = state.copyWith(backgroundTheme: theme);
    await _debouncedSave();
  }

  /// Set transition type and persist to database
  Future<void> setTransitionType(models.TransitionType transitionType) async {
    if (state.transitionType == transitionType) return;
    
    state = state.copyWith(transitionType: transitionType);
    await _debouncedSave();
  }

  /// Set brightness and persist to database
  Future<void> setBrightness(double brightness) async {
    brightness = brightness.clamp(0.1, 1.0);
    if ((state.brightness - brightness).abs() < 0.01) return;
    
    state = state.copyWith(brightness: brightness);
    await _debouncedSave();
  }

  /// Set show thumbnails preference and persist to database
  Future<void> setShowThumbnails(bool showThumbnails) async {
    if (state.showThumbnails == showThumbnails) return;
    
    state = state.copyWith(showThumbnails: showThumbnails);
    await _debouncedSave();
  }

  /// Update custom page order for the current comic
  Future<void> updateCustomPageOrder(List<int> newOrder) async {
    if (_listEquals(state.customPageOrder, newOrder)) return;
    
    try {
      await _db.setCustomPageOrder(_comicId, newOrder);
      state = state.copyWith(customPageOrder: newOrder);
    } catch (e) {
      state = state.copyWith(error: 'Failed to update page order: $e');
    }
  }

  /// Clear custom page order for the current comic
  Future<void> clearCustomPageOrder() async {
    if (state.customPageOrder.isEmpty) return;
    
    try {
      await _db.clearCustomPageOrder(_comicId);
      state = state.copyWith(customPageOrder: []);
    } catch (e) {
      state = state.copyWith(error: 'Failed to clear page order: $e');
    }
  }

  /// Add thumbnail to cache
  void addThumbnailToCache(int pageIndex, String thumbnailPath) {
    final newCache = Map<int, String>.from(state.thumbnailCache);
    newCache[pageIndex] = thumbnailPath;
    state = state.copyWith(thumbnailCache: newCache);
  }

  /// Remove thumbnail from cache
  void removeThumbnailFromCache(int pageIndex) {
    final newCache = Map<int, String>.from(state.thumbnailCache);
    newCache.remove(pageIndex);
    state = state.copyWith(thumbnailCache: newCache);
  }

  /// Clear all thumbnails from cache
  void clearThumbnailCache() {
    if (state.thumbnailCache.isEmpty) return;
    state = state.copyWith(thumbnailCache: {});
  }

  /// Reset all settings to defaults
  Future<void> resetToDefaults() async {
    state = state.copyWith(
      mode: models.ReadingMode.single,
      direction: models.NavigationDirection.horizontal,
      backgroundTheme: models.BackgroundTheme.black,
      transitionType: models.TransitionType.none,
      brightness: 1.0,
      showThumbnails: true,
    );
    await _saveSettings();
  }

  /// Debounced save to prevent excessive database writes
  Future<void> _debouncedSave() async {
    _debounceTimer?.cancel();
    _debounceTimer = Timer(const Duration(milliseconds: 500), () {
      _saveSettings();
    });
  }

  /// Save current settings to database
  Future<void> _saveSettings() async {
    try {
      final existingSettings = await _db.getReaderSettings();
      // For now, we'll handle this through the main settings service
      // The ReaderStateNotifier is mainly for UI state, not persistence
      // Settings persistence should go through ReaderSettingsService
    } catch (e) {
      state = state.copyWith(error: 'Failed to save settings: $e');
    }
  }

  /// Clear any error state
  void clearError() {
    if (state.error != null) {
      state = state.copyWith(error: null);
    }
  }

  /// Compare two lists for equality
  bool _listEquals<T>(List<T> a, List<T> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}