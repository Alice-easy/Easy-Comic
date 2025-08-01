// lib/presentation/features/settings/general/bloc/settings_state.dart
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:equatable/equatable.dart';

abstract class SettingsState extends Equatable {
  const SettingsState();

  @override
  List<Object?> get props => [];
}

class SettingsInitial extends SettingsState {}

class SettingsLoading extends SettingsState {}

class SettingsLoaded extends SettingsState {
  final ReaderSettings settings;
  final bool isSearching;
  final String searchQuery;
  final List<String> searchResults;

  const SettingsLoaded({
    required this.settings,
    this.isSearching = false,
    this.searchQuery = '',
    this.searchResults = const [],
  });

  @override
  List<Object> get props => [
        settings,
        isSearching,
        searchQuery,
        searchResults,
      ];

  SettingsLoaded copyWith({
    ReaderSettings? settings,
    bool? isSearching,
    String? searchQuery,
    List<String>? searchResults,
  }) {
    return SettingsLoaded(
      settings: settings ?? this.settings,
      isSearching: isSearching ?? this.isSearching,
      searchQuery: searchQuery ?? this.searchQuery,
      searchResults: searchResults ?? this.searchResults,
    );
  }
}

class SettingsError extends SettingsState {
  final String message;

  const SettingsError(this.message);

  @override
  List<Object> get props => [message];
}

class SettingsSaving extends SettingsState {
  final ReaderSettings settings;

  const SettingsSaving(this.settings);

  @override
  List<Object> get props => [settings];
}

class SettingsSaved extends SettingsState {
  final ReaderSettings settings;

  const SettingsSaved(this.settings);

  @override
  List<Object> get props => [settings];
}

class SettingsExporting extends SettingsState {
  final ReaderSettings settings;

  const SettingsExporting(this.settings);

  @override
  List<Object> get props => [settings];
}

class SettingsExported extends SettingsState {
  final String exportPath;
  final ReaderSettings settings;

  const SettingsExported({
    required this.exportPath,
    required this.settings,
  });

  @override
  List<Object> get props => [exportPath, settings];
}

class SettingsImporting extends SettingsState {}

class SettingsImported extends SettingsState {
  final ReaderSettings settings;

  const SettingsImported(this.settings);

  @override
  List<Object> get props => [settings];
}

class SettingsResetting extends SettingsState {}

class SettingsReset extends SettingsState {
  final ReaderSettings settings;

  const SettingsReset(this.settings);

  @override
  List<Object> get props => [settings];
}

class CacheClearing extends SettingsState {}

class CacheCleared extends SettingsState {
  final String message;

  const CacheCleared(this.message);

  @override
  List<Object> get props => [message];
}

class AppDataResetting extends SettingsState {}

class AppDataReset extends SettingsState {
  final String message;

  const AppDataReset(this.message);

  @override
  List<Object> get props => [message];
}