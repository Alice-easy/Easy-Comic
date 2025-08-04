import 'package.bloc/bloc.dart';
import 'package:easy_comic/domain/entities/app_settings.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

part 'theme_event.dart';
part 'theme_state.dart';

class ThemeBloc extends Bloc<ThemeEvent, ThemeState> {
  final ISettingsRepository settingsRepository;

  ThemeBloc({required this.settingsRepository}) : super(const ThemeInitial()) {
    on<ThemeLoadStarted>(_onThemeLoadStarted);
    on<ThemeChanged>(_onThemeChanged);
  }

  void _onThemeLoadStarted(
      ThemeLoadStarted event, Emitter<ThemeState> emit) async {
    final settingsEither = await settingsRepository.getSettings();
    settingsEither.fold(
      (failure) => emit(const ThemeLoadSuccess(ThemeMode.system)),
      (settings) => emit(ThemeLoadSuccess(settings.themeMode)),
    );
  }

  void _onThemeChanged(ThemeChanged event, Emitter<ThemeState> emit) async {
    final currentSettingsEither = await settingsRepository.getSettings();
    currentSettingsEither.fold(
      (failure) {
        // Handle failure, maybe emit an error state
      },
      (currentSettings) async {
        final newSettings = currentSettings.copyWith(themeMode: event.themeMode);
        await settingsRepository.saveSettings(newSettings);
        emit(ThemeLoadSuccess(event.themeMode));
      },
    );
  }
}