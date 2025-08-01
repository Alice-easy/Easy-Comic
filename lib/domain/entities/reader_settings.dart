import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

enum ReadingMode {
  leftToRight,
  rightToLeft,
  vertical,
  webtoon;

  String get displayName {
    switch (this) {
      case ReadingMode.leftToRight:
        return '从左到右';
      case ReadingMode.rightToLeft:
        return '从右到左';
      case ReadingMode.vertical:
        return '垂直阅读';
      case ReadingMode.webtoon:
        return '长条模式';
    }
  }

  static ReadingMode fromString(String value) {
    return ReadingMode.values.firstWhere(
      (mode) => mode.name == value,
      orElse: () => ReadingMode.leftToRight,
    );
  }
}

enum NavigationDirection {
  horizontal,
  rtl,
  vertical;

  String get displayName {
    switch (this) {
      case NavigationDirection.horizontal:
        return '从左到右';
      case NavigationDirection.rtl:
        return '从右到左';
      case NavigationDirection.vertical:
        return '从上到下';
    }
  }

  static NavigationDirection fromString(String value) {
    return NavigationDirection.values.firstWhere(
      (direction) => direction.name == value,
      orElse: () => NavigationDirection.horizontal,
    );
  }
}

enum BackgroundTheme {
  black,
  white,
  grey,
  sepia;

  String get displayName {
    switch (this) {
      case BackgroundTheme.black:
        return '黑色';
      case BackgroundTheme.white:
        return '白色';
      case BackgroundTheme.grey:
        return '灰色';
      case BackgroundTheme.sepia:
        return '护眼';
    }
  }

  Color get color {
    switch (this) {
      case BackgroundTheme.black:
        return Colors.black;
      case BackgroundTheme.white:
        return Colors.white;
      case BackgroundTheme.grey:
        return Colors.grey[800]!;
      case BackgroundTheme.sepia:
        return const Color(0xFFF5F5DC);
    }
  }

  static BackgroundTheme fromString(String value) {
    return BackgroundTheme.values.firstWhere(
      (theme) => theme.name == value,
      orElse: () => BackgroundTheme.black,
    );
  }
}

enum TransitionType {
  none,
  slide,
  fade,
  scale;

  String get displayName {
    switch (this) {
      case TransitionType.none:
        return '无动画';
      case TransitionType.slide:
        return '滑动';
      case TransitionType.fade:
        return '淡入淡出';
      case TransitionType.scale:
        return '缩放';
    }
  }

  static TransitionType fromString(String value) {
    return TransitionType.values.firstWhere(
      (type) => type.name == value,
      orElse: () => TransitionType.none,
    );
  }
}

class TapZoneConfig extends Equatable {
  final double leftZoneSize;
  final double rightZoneSize;
  final double leftZoneRatio;
  final double rightZoneRatio;
  final bool enableTapToFlip;

  const TapZoneConfig({
    this.leftZoneSize = 0.25,
    this.rightZoneSize = 0.25,
    this.leftZoneRatio = 0.25,
    this.rightZoneRatio = 0.25,
    this.enableTapToFlip = true,
  });

  TapZoneConfig copyWith({
    double? leftZoneSize,
    double? rightZoneSize,
    double? leftZoneRatio,
    double? rightZoneRatio,
    bool? enableTapToFlip,
  }) {
    return TapZoneConfig(
      leftZoneSize: leftZoneSize ?? this.leftZoneSize,
      rightZoneSize: rightZoneSize ?? this.rightZoneSize,
      leftZoneRatio: leftZoneRatio ?? this.leftZoneRatio,
      rightZoneRatio: rightZoneRatio ?? this.rightZoneRatio,
      enableTapToFlip: enableTapToFlip ?? this.enableTapToFlip,
    );
  }

  @override
  List<Object?> get props => [leftZoneSize, rightZoneSize, leftZoneRatio, rightZoneRatio, enableTapToFlip];
}

class AutoPageConfig extends Equatable {
  final int defaultInterval;
  final bool pauseOnUserInteraction;
  final bool pauseOnAppBackground;
  final bool showProgressIndicator;
  final bool stopAtLastPage;
  final Duration interactionPauseDelay;

  const AutoPageConfig({
    this.defaultInterval = 5,
    this.pauseOnUserInteraction = true,
    this.pauseOnAppBackground = true,
    this.showProgressIndicator = true,
    this.stopAtLastPage = true,
    this.interactionPauseDelay = const Duration(seconds: 3),
  });

  AutoPageConfig copyWith({
    int? defaultInterval,
    bool? pauseOnUserInteraction,
    bool? pauseOnAppBackground,
    bool? showProgressIndicator,
    bool? stopAtLastPage,
    Duration? interactionPauseDelay,
  }) {
    return AutoPageConfig(
      defaultInterval: defaultInterval ?? this.defaultInterval,
      pauseOnUserInteraction: pauseOnUserInteraction ?? this.pauseOnUserInteraction,
      pauseOnAppBackground: pauseOnAppBackground ?? this.pauseOnAppBackground,
      showProgressIndicator: showProgressIndicator ?? this.showProgressIndicator,
      stopAtLastPage: stopAtLastPage ?? this.stopAtLastPage,
      interactionPauseDelay: interactionPauseDelay ?? this.interactionPauseDelay,
    );
  }

  @override
  List<Object?> get props => [
    defaultInterval,
    pauseOnUserInteraction,
    pauseOnAppBackground,
    showProgressIndicator,
    stopAtLastPage,
    interactionPauseDelay,
  ];
}

class CacheConfig extends Equatable {
  final int memoryCacheSizeMB;
  final int diskCacheSizeMB;
  final int preloadDistance;
  final FilterQuality defaultQuality;
  final bool enableAdaptiveQuality;

  const CacheConfig({
    this.memoryCacheSizeMB = 50,
    this.diskCacheSizeMB = 200,
    this.preloadDistance = 3,
    this.defaultQuality = FilterQuality.high,
    this.enableAdaptiveQuality = true,
  });

  CacheConfig copyWith({
    int? memoryCacheSizeMB,
    int? diskCacheSizeMB,
    int? preloadDistance,
    FilterQuality? defaultQuality,
    bool? enableAdaptiveQuality,
  }) {
    return CacheConfig(
      memoryCacheSizeMB: memoryCacheSizeMB ?? this.memoryCacheSizeMB,
      diskCacheSizeMB: diskCacheSizeMB ?? this.diskCacheSizeMB,
      preloadDistance: preloadDistance ?? this.preloadDistance,
      defaultQuality: defaultQuality ?? this.defaultQuality,
      enableAdaptiveQuality: enableAdaptiveQuality ?? this.enableAdaptiveQuality,
    );
  }

  @override
  List<Object?> get props => [
    memoryCacheSizeMB,
    diskCacheSizeMB,
    preloadDistance,
    defaultQuality,
    enableAdaptiveQuality,
  ];
}

class GestureConfig extends Equatable {
  final TapZoneConfig tapZoneConfig;
  final double pinchSensitivity;
  final double panSensitivity;
  final bool enableHapticFeedback;
  final Duration doubleTapTimeout;

  const GestureConfig({
    this.tapZoneConfig = const TapZoneConfig(),
    this.pinchSensitivity = 1.0,
    this.panSensitivity = 1.0,
    this.enableHapticFeedback = true,
    this.doubleTapTimeout = const Duration(milliseconds: 300),
  });

  GestureConfig copyWith({
    TapZoneConfig? tapZoneConfig,
    double? pinchSensitivity,
    double? panSensitivity,
    bool? enableHapticFeedback,
    Duration? doubleTapTimeout,
  }) {
    return GestureConfig(
      tapZoneConfig: tapZoneConfig ?? this.tapZoneConfig,
      pinchSensitivity: pinchSensitivity ?? this.pinchSensitivity,
      panSensitivity: panSensitivity ?? this.panSensitivity,
      enableHapticFeedback: enableHapticFeedback ?? this.enableHapticFeedback,
      doubleTapTimeout: doubleTapTimeout ?? this.doubleTapTimeout,
    );
  }

  @override
  List<Object?> get props => [
    tapZoneConfig,
    pinchSensitivity,
    panSensitivity,
    enableHapticFeedback,
    doubleTapTimeout,
  ];
}

class PerformanceConfig extends Equatable {
  final bool enableProgressiveLoading;
  final bool enableMemoryOptimization;
  final double maxMemoryUsageMB;
  final int targetFPS;
  final bool enableHardwareAcceleration;

  const PerformanceConfig({
    this.enableProgressiveLoading = true,
    this.enableMemoryOptimization = true,
    this.maxMemoryUsageMB = 100.0,
    this.targetFPS = 60,
    this.enableHardwareAcceleration = true,
  });

  PerformanceConfig copyWith({
    bool? enableProgressiveLoading,
    bool? enableMemoryOptimization,
    double? maxMemoryUsageMB,
    int? targetFPS,
    bool? enableHardwareAcceleration,
  }) {
    return PerformanceConfig(
      enableProgressiveLoading: enableProgressiveLoading ?? this.enableProgressiveLoading,
      enableMemoryOptimization: enableMemoryOptimization ?? this.enableMemoryOptimization,
      maxMemoryUsageMB: maxMemoryUsageMB ?? this.maxMemoryUsageMB,
      targetFPS: targetFPS ?? this.targetFPS,
      enableHardwareAcceleration: enableHardwareAcceleration ?? this.enableHardwareAcceleration,
    );
  }

  @override
  List<Object?> get props => [
    enableProgressiveLoading,
    enableMemoryOptimization,
    maxMemoryUsageMB,
    targetFPS,
    enableHardwareAcceleration,
  ];
}

class ReaderSettings extends Equatable {
  final ReadingMode readingMode;
  final NavigationDirection navigationDirection;
  final BackgroundTheme backgroundTheme;
  final TransitionType transitionType;
  final double brightness;
  final bool enableAutoPage;
  final int autoPageInterval;
  final bool enableWakelock;
  final bool showProgress;
  final bool showPageInfo;
  final bool enableVolumeKeys;
  final TapZoneConfig tapZoneConfig;
  final double zoomSensitivity;
  final bool enableDoubleTapZoom;
  final bool enableFullscreen;
  
  // Enhanced configuration properties
  final AutoPageConfig autoPageConfig;
  final CacheConfig cacheConfig;
  final GestureConfig gestureConfig;
  final PerformanceConfig performanceConfig;

  const ReaderSettings({
    this.readingMode = ReadingMode.leftToRight,
    this.navigationDirection = NavigationDirection.horizontal,
    this.backgroundTheme = BackgroundTheme.black,
    this.transitionType = TransitionType.none,
    this.brightness = 1.0,
    this.enableAutoPage = false,
    this.autoPageInterval = 5,
    this.enableWakelock = true,
    this.showProgress = true,
    this.showPageInfo = true,
    this.enableVolumeKeys = true,
    this.tapZoneConfig = const TapZoneConfig(),
    this.zoomSensitivity = 1.0,
    this.enableDoubleTapZoom = true,
    this.enableFullscreen = false,
    this.autoPageConfig = const AutoPageConfig(),
    this.cacheConfig = const CacheConfig(),
    this.gestureConfig = const GestureConfig(),
    this.performanceConfig = const PerformanceConfig(),
  });

  ReaderSettings copyWith({
    ReadingMode? readingMode,
    NavigationDirection? navigationDirection,
    BackgroundTheme? backgroundTheme,
    TransitionType? transitionType,
    double? brightness,
    bool? enableAutoPage,
    int? autoPageInterval,
    bool? enableWakelock,
    bool? showProgress,
    bool? showPageInfo,
    bool? enableVolumeKeys,
    TapZoneConfig? tapZoneConfig,
    double? zoomSensitivity,
    bool? enableDoubleTapZoom,
    bool? enableFullscreen,
    AutoPageConfig? autoPageConfig,
    CacheConfig? cacheConfig,
    GestureConfig? gestureConfig,
    PerformanceConfig? performanceConfig,
  }) {
    return ReaderSettings(
      readingMode: readingMode ?? this.readingMode,
      navigationDirection: navigationDirection ?? this.navigationDirection,
      backgroundTheme: backgroundTheme ?? this.backgroundTheme,
      transitionType: transitionType ?? this.transitionType,
      brightness: brightness ?? this.brightness,
      enableAutoPage: enableAutoPage ?? this.enableAutoPage,
      autoPageInterval: autoPageInterval ?? this.autoPageInterval,
      enableWakelock: enableWakelock ?? this.enableWakelock,
      showProgress: showProgress ?? this.showProgress,
      showPageInfo: showPageInfo ?? this.showPageInfo,
      enableVolumeKeys: enableVolumeKeys ?? this.enableVolumeKeys,
      tapZoneConfig: tapZoneConfig ?? this.tapZoneConfig,
      zoomSensitivity: zoomSensitivity ?? this.zoomSensitivity,
      enableDoubleTapZoom: enableDoubleTapZoom ?? this.enableDoubleTapZoom,
      enableFullscreen: enableFullscreen ?? this.enableFullscreen,
      autoPageConfig: autoPageConfig ?? this.autoPageConfig,
      cacheConfig: cacheConfig ?? this.cacheConfig,
      gestureConfig: gestureConfig ?? this.gestureConfig,
      performanceConfig: performanceConfig ?? this.performanceConfig,
    );
  }

  @override
  List<Object?> get props => [
        readingMode,
        navigationDirection,
        backgroundTheme,
        transitionType,
        brightness,
        enableAutoPage,
        autoPageInterval,
        enableWakelock,
        showProgress,
        showPageInfo,
        enableVolumeKeys,
        tapZoneConfig,
        zoomSensitivity,
        enableDoubleTapZoom,
        enableFullscreen,
        autoPageConfig,
        cacheConfig,
        gestureConfig,
        performanceConfig,
      ];
}