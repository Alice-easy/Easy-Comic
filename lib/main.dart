import 'package:dynamic_color/dynamic_color.dart';
import 'package:easy_comic/presentation/features/settings/theme/bloc/theme_bloc.dart';
import 'dart:async';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/presentation/pages/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:easy_comic/injection_container.dart' as di;
import 'package:flutter_bloc/flutter_bloc.dart';
import 'core/constants.dart';
import 'injection_container.dart';

void main() async {
  runZonedGuarded<Future<void>>(() async {
    // Ensure that widget binding is initialized
    WidgetsFlutterBinding.ensureInitialized();

    // Initialize dependency injection
    await di.init();

    // Set up error handling
    FlutterError.onError = (FlutterErrorDetails details) {
      // Forward to the zone's error handler
      Zone.current.handleUncaughtError(details.exception, details.stack ?? StackTrace.current);
    };

    // Run the app
    runApp(const MyApp());
  }, (error, stackTrace) {
    // This is the global error handler.
    // Use the LoggingService to record the error.
    final loggingService = sl<LoggingService>();
    loggingService.error('Unhandled error caught by runZonedGuarded', error, stackTrace);
    // You might also want to report this to a remote service like Firebase Crashlytics
    // For example: FirebaseCrashlytics.instance.recordError(error, stackTrace, fatal: true);
  });
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<ThemeBloc>()..add(GetTheme()),
      child: BlocBuilder<ThemeBloc, ThemeState>(
        builder: (context, state) {
          return DynamicColorBuilder(
            builder: (lightDynamic, darkDynamic) {
              final colorScheme = _createColorScheme(lightDynamic, darkDynamic);
              final darkColorScheme =
                  _createColorScheme(lightDynamic, darkDynamic, isDark: true);

              return MaterialApp(
                title: AppConstants.appName,
                theme: ThemeData(
                  colorScheme: colorScheme,
                  useMaterial3: true,
                ),
                darkTheme: ThemeData(
                  colorScheme: darkColorScheme,
                  useMaterial3: true,
                ),
                themeMode: state.themeMode,
                home: const HomeScreen(),
              );
            },
          );
        },
      ),
    );
  }

  ColorScheme _createColorScheme(
      ColorScheme? lightDynamic, ColorScheme? darkDynamic,
      {bool isDark = false}) {
    if (lightDynamic != null && darkDynamic != null) {
      return isDark ? darkDynamic : lightDynamic;
    } else {
      return ColorScheme.fromSeed(
        seedColor: Colors.deepPurple,
        brightness: isDark ? Brightness.dark : Brightness.light,
      );
    }
  }
}
