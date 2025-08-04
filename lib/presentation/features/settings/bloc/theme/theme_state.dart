part of 'theme_bloc.dart';

@immutable
abstract class ThemeState {
  final ThemeMode themeMode;

  const ThemeState(this.themeMode);
}

class ThemeInitial extends ThemeState {
  const ThemeInitial() : super(ThemeMode.system);
}

class ThemeLoadSuccess extends ThemeState {
  const ThemeLoadSuccess(ThemeMode themeMode) : super(themeMode);
}