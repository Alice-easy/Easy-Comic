import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

// --- Enums ---

enum AppTheme { Light, Dark, System }

enum ReadingDirection { LTR, RTL }

// --- Entity ---

class ReaderSettings extends Equatable {
  final AppTheme appTheme;
  final ReadingDirection readingDirection;
  final int autoPageInterval;
  final WebDavConfig? webDavConfig;
  // Can add more settings here in the future

  const ReaderSettings({
    this.appTheme = AppTheme.System,
    this.readingDirection = ReadingDirection.LTR,
    this.autoPageInterval = 5,
    this.webDavConfig,
  });

  @override
  List<Object?> get props => [
        appTheme,
        readingDirection,
        autoPageInterval,
        webDavConfig,
      ];

  ReaderSettings copyWith({
    AppTheme? appTheme,
    ReadingDirection? readingDirection,
    int? autoPageInterval,
    WebDavConfig? webDavConfig,
  }) {
    return ReaderSettings(
      appTheme: appTheme ?? this.appTheme,
      readingDirection: readingDirection ?? this.readingDirection,
      autoPageInterval: autoPageInterval ?? this.autoPageInterval,
      webDavConfig: webDavConfig ?? this.webDavConfig,
    );
  }

  // --- JSON Serialization ---

  factory ReaderSettings.fromJson(Map<String, dynamic> json) {
    return ReaderSettings(
      appTheme: AppTheme.values[json['appTheme'] ?? AppTheme.System.index],
      readingDirection: ReadingDirection.values[json['readingDirection'] ?? ReadingDirection.LTR.index],
      autoPageInterval: json['autoPageInterval'] ?? 5,
      webDavConfig: json['webDavConfig'] != null
          ? WebDavConfig.fromJson(json['webDavConfig'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'appTheme': appTheme.index,
      'readingDirection': readingDirection.index,
      'autoPageInterval': autoPageInterval,
      'webDavConfig': webDavConfig?.toJson(),
    };
  }
}