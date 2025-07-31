import 'package:flutter/material.dart';
import '../data/drift_db.dart' as drift;
import '../data/drift_db.dart';

// ComicInfo model with lastReadPage
class ComicInfo {
  final String id;
  final String name;
  final int lastReadPage;
  
  const ComicInfo({
    required this.id, 
    required this.name,
    this.lastReadPage = 0,
  });
}

// Enhanced Bookmark model
class Bookmark {
  final int id;
  final int comicId;
  final int page;
  final String? label;
  final DateTime createdAt;
  final String? thumbnailPath;
  
  const Bookmark({
    required this.id, 
    required this.comicId,
    required this.page,
    this.label,
    required this.createdAt,
    this.thumbnailPath,
  });

  factory Bookmark.fromData(drift.Bookmark data, {String? thumbnailPath}) {
    return Bookmark(
      id: data.id,
      comicId: data.comicId,
      page: data.pageIndex,
      label: data.label,
      createdAt: data.createdAt,
      thumbnailPath: thumbnailPath,
    );
  }
}

// ComicPage model for reader state
class ComicPage {
  final String imagePath;
  final int index;
  final int? customIndex;
  
  const ComicPage({
    required this.imagePath,
    required this.index,
    this.customIndex,
  });

  bool get hasCustomOrder => customIndex != null;
  int get displayIndex => customIndex ?? index;
}

// Reading Mode Enums
enum ReadingMode {
  single('single', 'Single Page'),
  dual('dual', 'Dual Page'),
  vertical('vertical', 'Vertical Scroll'),
  continuous('continuous', 'Continuous Scroll');

  const ReadingMode(this.value, this.displayName);
  final String value;
  final String displayName;

  String toValueString() => value;

  static ReadingMode fromString(String value) {
    return ReadingMode.values.firstWhere(
      (mode) => mode.value == value,
      orElse: () => ReadingMode.single,
    );
  }
}

enum NavigationDirection {
  horizontal('horizontal', 'Left to Right'),
  vertical('vertical', 'Top to Bottom'),
  rtl('rtl', 'Right to Left');

  const NavigationDirection(this.value, this.displayName);
  final String value;
  final String displayName;

  String toValueString() => value;

  static NavigationDirection fromString(String value) {
    return NavigationDirection.values.firstWhere(
      (direction) => direction.value == value,
      orElse: () => NavigationDirection.horizontal,
    );
  }
}

enum BackgroundTheme {
  black('black', 'Black', Colors.black),
  white('white', 'White', Colors.white),
  grey('grey', 'Grey', Color(0xFF424242)),
  sepia('sepia', 'Sepia', Color(0xFFF5F5DC));

  const BackgroundTheme(this.value, this.displayName, this.color);
  final String value;
  final String displayName;
  final Color color;

  String toValueString() => value;

  static BackgroundTheme fromString(String value) {
    return BackgroundTheme.values.firstWhere(
      (theme) => theme.value == value,
      orElse: () => BackgroundTheme.black,
    );
  }
}

enum TransitionType {
  none('none', 'None'),
  slide('slide', 'Slide'),
  fade('fade', 'Fade'),
  scale('scale', 'Scale'),
  flip('flip', 'Flip');

  const TransitionType(this.value, this.displayName);
  final String value;
  final String displayName;

  String toValueString() => value;

  static TransitionType fromString(String value) {
    return TransitionType.values.firstWhere(
      (type) => type.value == value,
      orElse: () => TransitionType.none,
    );
  }
}

// Legacy enums for backward compatibility
enum PageTurnMode { horizontal, vertical }
enum PageTurnAnimation { slide, fade, none }

// Enhanced ReaderSettings model
class ReaderSettings {
  final String? userId;
  final ReadingMode readingMode;
  final NavigationDirection navigationDirection;
  final BackgroundTheme backgroundTheme;
  final TransitionType transitionType;
  final double brightness;
  final bool showThumbnails;
  final DateTime updatedAt;
  final String? etag;

  const ReaderSettings({
    this.userId,
    this.readingMode = ReadingMode.single,
    this.navigationDirection = NavigationDirection.horizontal,
    this.backgroundTheme = BackgroundTheme.black,
    this.transitionType = TransitionType.none,
    this.brightness = 1.0,
    this.showThumbnails = true,
    required this.updatedAt,
    this.etag,
  });

  factory ReaderSettings.defaultSettings([String? userId]) {
    return ReaderSettings(
      userId: userId,
      updatedAt: DateTime.now(),
    );
  }

  factory ReaderSettings.fromData(drift.ReaderSetting data) {
    return ReaderSettings(
      userId: data.userId,
      readingMode: ReadingMode.fromString(data.readingMode),
      navigationDirection: NavigationDirection.fromString(data.navigationDirection),
      backgroundTheme: BackgroundTheme.fromString(data.backgroundTheme),
      transitionType: TransitionType.fromString(data.transitionType),
      brightness: data.brightness,
      showThumbnails: data.showThumbnails,
      updatedAt: data.updatedAt,
      etag: data.etag,
    );
  }

  ReaderSettings copyWith({
    String? userId,
    ReadingMode? readingMode,
    NavigationDirection? navigationDirection,
    BackgroundTheme? backgroundTheme,
    TransitionType? transitionType,
    double? brightness,
    bool? showThumbnails,
    DateTime? updatedAt,
    String? etag,
  }) {
    return ReaderSettings(
      userId: userId ?? this.userId,
      readingMode: readingMode ?? this.readingMode,
      navigationDirection: navigationDirection ?? this.navigationDirection,
      backgroundTheme: backgroundTheme ?? this.backgroundTheme,
      transitionType: transitionType ?? this.transitionType,
      brightness: brightness ?? this.brightness,
      showThumbnails: showThumbnails ?? this.showThumbnails,
      updatedAt: updatedAt ?? this.updatedAt,
      etag: etag ?? this.etag,
    );
  }

  // Legacy properties for backward compatibility
  Color get backgroundColor => backgroundTheme.color;
  PageTurnMode get pageTurnMode => navigationDirection == NavigationDirection.vertical 
      ? PageTurnMode.vertical 
      : PageTurnMode.horizontal;
  PageTurnAnimation get pageTurnAnimation {
    switch (transitionType) {
      case TransitionType.slide:
        return PageTurnAnimation.slide;
      case TransitionType.fade:
        return PageTurnAnimation.fade;
      default:
        return PageTurnAnimation.none;
    }
  }

  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'readingMode': readingMode.value,
      'navigationDirection': navigationDirection.value,
      'backgroundTheme': backgroundTheme.value,
      'transitionType': transitionType.value,
      'brightness': brightness,
      'showThumbnails': showThumbnails,
      'updatedAt': updatedAt.toIso8601String(),
      'etag': etag,
    };
  }

  factory ReaderSettings.fromJson(Map<String, dynamic> json) {
    return ReaderSettings(
      userId: json['userId'],
      readingMode: ReadingMode.fromString(json['readingMode'] ?? 'single'),
      navigationDirection: NavigationDirection.fromString(json['navigationDirection'] ?? 'horizontal'),
      backgroundTheme: BackgroundTheme.fromString(json['backgroundTheme'] ?? 'black'),
      transitionType: TransitionType.fromString(json['transitionType'] ?? 'none'),
      brightness: (json['brightness'] ?? 1.0).toDouble(),
      showThumbnails: json['showThumbnails'] ?? true,
      updatedAt: DateTime.parse(json['updatedAt'] ?? DateTime.now().toIso8601String()),
      etag: json['etag'],
    );
  }
}

// Reading Session Model
class ReadingSession {
  final String sessionId;
  final int comicId;
  final DateTime startTime;
  final DateTime? endTime;
  final List<int> pagesRead;
  final Map<String, dynamic> metadata;

  const ReadingSession({
    required this.sessionId,
    required this.comicId,
    required this.startTime,
    this.endTime,
    this.pagesRead = const [],
    this.metadata = const {},
  });

  Duration get duration {
    final end = endTime ?? DateTime.now();
    return end.difference(startTime);
  }

  bool get isActive => endTime == null;

  ReadingSession copyWith({
    String? sessionId,
    int? comicId,
    DateTime? startTime,
    DateTime? endTime,
    List<int>? pagesRead,
    Map<String, dynamic>? metadata,
  }) {
    return ReadingSession(
      sessionId: sessionId ?? this.sessionId,
      comicId: comicId ?? this.comicId,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      pagesRead: pagesRead ?? this.pagesRead,
      metadata: metadata ?? this.metadata,
    );
  }
}

// Page Ordering Model
class PageOrder {
  final int comicId;
  final List<int> originalOrder;
  final List<int> customOrder;
  final DateTime createdAt;

  const PageOrder({
    required this.comicId,
    required this.originalOrder,
    required this.customOrder,
    required this.createdAt,
  });

  bool get hasCustomOrder => !_listEquals(originalOrder, customOrder);

  static bool _listEquals<T>(List<T> a, List<T> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}

/// Gesture details for reading strategy implementations
class GestureDetails {
  final Offset position;
  final GestureType type;
  final double? velocity;
  final double? scale;

  const GestureDetails({
    required this.position,
    required this.type,
    this.velocity,
    this.scale,
  });
}

/// Gesture types for reader interactions
enum GestureType {
  tap,
  doubleTap,
  longPress,
  swipeLeft,
  swipeRight,
  swipeUp,
  swipeDown,
  pinch,
  pan,
}

/// Enhanced bookmark model with thumbnail support
class EnhancedBookmark {
  final int id;
  final int comicId;
  final int pageIndex;
  final String? label;
  final String? thumbnailPath;
  final DateTime createdAt;

  const EnhancedBookmark({
    required this.id,
    required this.comicId,  
    required this.pageIndex,
    this.label,
    this.thumbnailPath,
    required this.createdAt,
  });

  EnhancedBookmark copyWith({
    int? id,
    int? comicId,
    int? pageIndex,
    String? label,
    String? thumbnailPath,
    DateTime? createdAt,
  }) {
    return EnhancedBookmark(
      id: id ?? this.id,
      comicId: comicId ?? this.comicId,
      pageIndex: pageIndex ?? this.pageIndex,
      label: label ?? this.label,
      thumbnailPath: thumbnailPath ?? this.thumbnailPath,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

/// Reader state model for UI state management
class ReaderState {
  final ReadingMode mode;
  final NavigationDirection direction;
  final BackgroundTheme backgroundTheme;
  final TransitionType transitionType;
  final double brightness;
  final bool showThumbnails;
  final List<int> customPageOrder;
  final Map<int, String> thumbnailCache;
  final bool isLoading;
  final String? error;

  const ReaderState({
    required this.mode,
    required this.direction,
    required this.backgroundTheme,
    required this.transitionType,
    required this.brightness,
    required this.showThumbnails,
    required this.customPageOrder,
    required this.thumbnailCache,
    this.isLoading = false,
    this.error,
  });

  static const ReaderState initial = ReaderState(
    mode: ReadingMode.single,
    direction: NavigationDirection.horizontal,
    backgroundTheme: BackgroundTheme.black,
    transitionType: TransitionType.none,
    brightness: 1.0,
    showThumbnails: true,
    customPageOrder: [],
    thumbnailCache: {},
  );

  ReaderState copyWith({
    ReadingMode? mode,
    NavigationDirection? direction,
    BackgroundTheme? backgroundTheme,
    TransitionType? transitionType,
    double? brightness,
    bool? showThumbnails,
    List<int>? customPageOrder,
    Map<int, String>? thumbnailCache,
    bool? isLoading,
    String? error,
  }) {
    return ReaderState(
      mode: mode ?? this.mode,
      direction: direction ?? this.direction,
      backgroundTheme: backgroundTheme ?? this.backgroundTheme,
      transitionType: transitionType ?? this.transitionType,
      brightness: brightness ?? this.brightness,
      showThumbnails: showThumbnails ?? this.showThumbnails,
      customPageOrder: customPageOrder ?? this.customPageOrder,
      thumbnailCache: thumbnailCache ?? this.thumbnailCache,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
    );
  }
}

// Reader UI State Model
class ReaderUIState {
  final bool showControls;
  final bool showProgress;
  final bool showThumbnails;
  final bool fullscreen;
  final double? customBrightness;
  final bool brightnessOverlayEnabled;

  const ReaderUIState({
    this.showControls = true,
    this.showProgress = true,
    this.showThumbnails = false,
    this.fullscreen = false,
    this.customBrightness,
    this.brightnessOverlayEnabled = false,
  });

  ReaderUIState copyWith({
    bool? showControls,
    bool? showProgress,
    bool? showThumbnails,
    bool? fullscreen,
    double? customBrightness,
    bool? brightnessOverlayEnabled,
  }) {
    return ReaderUIState(
      showControls: showControls ?? this.showControls,
      showProgress: showProgress ?? this.showProgress,
      showThumbnails: showThumbnails ?? this.showThumbnails,
      fullscreen: fullscreen ?? this.fullscreen,
      customBrightness: customBrightness ?? this.customBrightness,
      brightnessOverlayEnabled: brightnessOverlayEnabled ?? this.brightnessOverlayEnabled,
    );
  }
}