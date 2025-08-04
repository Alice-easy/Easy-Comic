import 'package:flutter/material.dart';

enum ReadingDirection { ltr, rtl }
enum ReadingMode { singlePage, doublePage, longStrip }
enum PageTurnAnimation { slide, fade, none }

class AppSettings {
  // Browsing settings
  final ReadingDirection readingDirection;
  final ReadingMode readingMode;
  final PageTurnAnimation pageTurnAnimation;

  // Reading preferences
  final bool autoPageTurn;
  final int autoPageTurnInterval; // in seconds
  final bool volumeKeyPageTurn;
  final Map<String, double> tapSensitivity; // e.g., {'next': 0.3, 'prev': 0.3}

  // Appearance
  final ThemeMode themeMode;
  final double fontScale;

  AppSettings({
    this.readingDirection = ReadingDirection.ltr,
    this.readingMode = ReadingMode.singlePage,
    this.pageTurnAnimation = PageTurnAnimation.slide,
    this.autoPageTurn = false,
    this.autoPageTurnInterval = 5,
    this.volumeKeyPageTurn = true,
    this.tapSensitivity = const {'next': 0.3, 'prev': 0.3},
    this.themeMode = ThemeMode.system,
    this.fontScale = 1.0,
  });

  AppSettings copyWith({
    ReadingDirection? readingDirection,
    ReadingMode? readingMode,
    PageTurnAnimation? pageTurnAnimation,
    bool? autoPageTurn,
    int? autoPageTurnInterval,
    bool? volumeKeyPageTurn,
    Map<String, double>? tapSensitivity,
    ThemeMode? themeMode,
    double? fontScale,
  }) {
    return AppSettings(
      readingDirection: readingDirection ?? this.readingDirection,
      readingMode: readingMode ?? this.readingMode,
      pageTurnAnimation: pageTurnAnimation ?? this.pageTurnAnimation,
      autoPageTurn: autoPageTurn ?? this.autoPageTurn,
      autoPageTurnInterval: autoPageTurnInterval ?? this.autoPageTurnInterval,
      volumeKeyPageTurn: volumeKeyPageTurn ?? this.volumeKeyPageTurn,
      tapSensitivity: tapSensitivity ?? this.tapSensitivity,
      themeMode: themeMode ?? this.themeMode,
      fontScale: fontScale ?? this.fontScale,
    );
  }
}