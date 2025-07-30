import 'package:flutter/material.dart';

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

// Bookmark model
class Bookmark {
  final int id;
  final int page;
  
  const Bookmark({required this.id, required this.page});
}

// ComicPage model for reader state
class ComicPage {
  final String imagePath;
  
  const ComicPage({required this.imagePath});
}

enum PageTurnMode { horizontal, vertical }
enum PageTurnAnimation { slide, fade, none }

class ReaderSettings {
  final double brightness;
  final Color backgroundColor;
  final PageTurnMode pageTurnMode;
  final PageTurnAnimation pageTurnAnimation;

  const ReaderSettings({
    this.brightness = 1.0,
    this.backgroundColor = Colors.white,
    this.pageTurnMode = PageTurnMode.horizontal,
    this.pageTurnAnimation = PageTurnAnimation.slide,
  });

  ReaderSettings copyWith({
    double? brightness,
    Color? backgroundColor,
    PageTurnMode? pageTurnMode,
    PageTurnAnimation? pageTurnAnimation,
  }) => ReaderSettings(
        brightness: brightness ?? this.brightness,
        backgroundColor: backgroundColor ?? this.backgroundColor,
        pageTurnMode: pageTurnMode ?? this.pageTurnMode,
        pageTurnAnimation: pageTurnAnimation ?? this.pageTurnAnimation,
      );
}