part of 'theme_bloc.dart';

@immutable
abstract class ThemeEvent {}

class ThemeLoadStarted extends ThemeEvent {}

class ThemeChanged extends ThemeEvent {
  final ThemeMode themeMode;

  ThemeChanged(this.themeMode);
}