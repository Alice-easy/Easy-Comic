import 'dart:ui';

import 'package:dynamic_color/dynamic_color.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:home_widget/home_widget.dart';

import 'core/background_task_manager.dart';
import 'core/constants.dart';
import 'core/task_registrar.dart';
import 'data/drift_db.dart';
import 'firebase_options.dart';
import 'home/home_page.dart';
import 'settings/settings_store.dart';

final seedColorProvider = StateProvider<Color>((ref) => Colors.deepPurple);
final settingsStoreProvider = ChangeNotifierProvider((ref) => SettingsStore());

// 数据库单例Provider
final dbProvider = Provider<DriftDb>((ref) {
  final db = DriftDb();
  ref.onDispose(() => db.close());
  return db;
});

Future<void> updateWidget() async {
  final container = ProviderContainer();
  final db = container.read(dbProvider);
  final minutes = await db.getThisWeekReadingMinutes();
  await HomeWidget.saveWidgetData<int>('minutes', minutes);
  await HomeWidget.updateWidget(
    name: AppConstants.comicsWidgetProvider,
    androidName: AppConstants.comicsWidgetProvider,
  );
  container.dispose();
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await HomeWidget.registerInteractivityCallback(backgroundCallback);

  // 初始化 Firebase
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

  // 设置 Crashlytics 错误处理器
  FlutterError.onError = FirebaseCrashlytics.instance.recordFlutterError;
  PlatformDispatcher.instance.onError = (error, stack) {
    FirebaseCrashlytics.instance.recordError(error, stack, fatal: true);
    return true;
  };

  // 模拟用户登录并设置用户ID
  await FirebaseCrashlytics.instance.setUserIdentifier(AppConstants.defaultUserId);

  // 初始化后台任务管理器
  await BackgroundTaskManager.initialize();
  // 注册后台任务
  await TaskRegistrar.registerTasks();

  await updateWidget();

  runApp(const ProviderScope(child: MyApp()));
}

void backgroundCallback(Uri? uri) {
  if (uri?.host == 'updatewidget') {
    updateWidget();
  }
}

class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final seedColor = ref.watch(seedColorProvider);
    final settings = ref.watch(settingsStoreProvider);

    return DynamicColorBuilder(
      builder: (lightDynamic, darkDynamic) {
        ColorScheme lightColorScheme;
        ColorScheme darkColorScheme;

        if (lightDynamic != null && darkDynamic != null) {
          lightColorScheme = lightDynamic;
          darkColorScheme = darkDynamic;
        } else {
          lightColorScheme = ColorScheme.fromSeed(seedColor: seedColor);
          darkColorScheme = ColorScheme.fromSeed(
            seedColor: seedColor,
            brightness: Brightness.dark,
          );
        }

        return MaterialApp(
          title: AppConstants.appName,
          theme: ThemeData(colorScheme: lightColorScheme, useMaterial3: true),
          darkTheme: ThemeData(
            colorScheme: darkColorScheme,
            useMaterial3: true,
          ),
          themeMode: settings.themeMode,
          home: const HomePage(),
        );
      },
    );
  }
}
