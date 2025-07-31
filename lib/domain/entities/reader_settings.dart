import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

enum ReadingMode {
  single,
  dual,
  continuous,
  vertical;

  String get displayName {
    switch (this) {
      case ReadingMode.single:
        return '单页模式';
      case ReadingMode.dual:
        return '双页模式';
      case ReadingMode.continuous:
        return '连续滚动';
      case ReadingMode.vertical:
        return '垂直阅读';
    }
  }

  static ReadingMode fromString(String value) {
    return ReadingMode.values.firstWhere(
      (mode) => mode.name == value,
      orElse: () => ReadingMode.single,
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
  final double leftZoneRatio;
  final double rightZoneRatio;
  final bool enableTapToFlip;

  const TapZoneConfig({
    this.leftZoneRatio = 0.25,
    this.rightZoneRatio = 0.25,
    this.enableTapToFlip = true,
  });

  TapZoneConfig copyWith({
    double? leftZoneRatio,
    double? rightZoneRatio,
    bool? enableTapToFlip,
  }) {
    return TapZoneConfig(
      leftZoneRatio: leftZoneRatio ?? this.leftZoneRatio,
      rightZoneRatio: rightZoneRatio ?? this.rightZoneRatio,
      enableTapToFlip: enableTapToFlip ?? this.enableTapToFlip,
    );
  }

  @override
  List<Object?> get props => [leftZoneRatio, rightZoneRatio, enableTapToFlip];
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

  const ReaderSettings({
    this.readingMode = ReadingMode.single,
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
      ];
}