import 'dart:ui';

import 'package:dynamic_color/dynamic_color.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:home_widget/home_widget.dart';

import 'core/background_task_manager.dart';
import 'core/constants.dart';
import 'core/services/settings_service.dart';
import 'core/task_registrar.dart';
import 'data/drift_db.dart';
import 'firebase_options.dart';
import 'injection_container.dart' as di;
import 'presentation/features/reader/bloc/reader_bloc.dart';
import 'presentation/pages/home_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await di.init();
  // await HomeWidget.registerInteractivityCallback(backgroundCallback);

  // 初始化 Firebase
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  // 设置 Crashlytics 错误处理器
  FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterError;
  PlatformDispatcher.instance.onError = (error, stack) {
    FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
    return true;
  };

  // 模拟用户登录并设置用户ID
  await FirebaseCrashlytics.instance
      .setUserIdentifier(AppConstants.defaultUserId);

  // 初始化后台任务管理器
  await BackgroundTaskManager.initialize();
  // 注册后台任务
  await TaskRegistrar.registerTasks();

  // await updateWidget();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<ReaderBloc>(
          create: (context) => di.sl<ReaderBloc>(),
        ),
      ],
      child: DynamicColorBuilder(
        builder: (lightDynamic, darkDynamic) {
          ColorScheme lightColorScheme;
          ColorScheme darkColorScheme;

          if (lightDynamic != null && darkDynamic != null) {
            lightColorScheme = lightDynamic;
            darkColorScheme = darkDynamic;
          } else {
            lightColorScheme =
                ColorScheme.fromSeed(seedColor: Colors.deepPurple);
            darkColorScheme = ColorScheme.fromSeed(
              seedColor: Colors.deepPurple,
              brightness: Brightness.dark,
            );
          }

          return MaterialApp(
            title: AppConstants.appName,
            theme:
                ThemeData(colorScheme: lightColorScheme, useMaterial3: true),
            darkTheme: ThemeData(
              colorScheme: darkColorScheme,
              useMaterial3: true,
            ),
            themeMode: ThemeMode.system, // Or load from settings
            home: const HomeScreen(),
          );
        },
      ),
    );
  }
}
