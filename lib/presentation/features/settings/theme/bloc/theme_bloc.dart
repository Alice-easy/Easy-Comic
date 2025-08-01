import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';
import 'package:easy_comic/domain/services/theme_service.dart';

part 'theme_event.dart';
part 'theme_state.dart';

class ThemeBloc extends Bloc<ThemeEvent, ThemeState> {
  final ThemeService _themeService;

  ThemeBloc({required ThemeService themeService})
      : _themeService = themeService,
        super(ThemeState(
          themeData: ThemeData.light(),
          themeMode: ThemeMode.system,
        )) {
    on<GetTheme>(_onGetTheme);
    on<ThemeModeChanged>(_onThemeModeChanged);
  }

  Future<void> _onGetTheme(GetTheme event, Emitter<ThemeState> emit) async {
    final themeMode = await _themeService.getThemeMode();
    emit(ThemeState(
      themeData: _getThemeData(themeMode),
      themeMode: themeMode,
    ));
  }

  Future<void> _onThemeModeChanged(
      ThemeModeChanged event, Emitter<ThemeState> emit) async {
    await _themeService.setThemeMode(event.themeMode);
    emit(ThemeState(
      themeData: _getThemeData(event.themeMode),
      themeMode: event.themeMode,
    ));
  }

  ThemeData _getThemeData(ThemeMode themeMode) {
    switch (themeMode) {
      case ThemeMode.dark:
        return ThemeData.dark();
      case ThemeMode.light:
        return ThemeData.light();
      case ThemeMode.system:
        return ThemeData.light(); // Default to light, system will handle it
    }
  }
}