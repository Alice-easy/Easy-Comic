import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:workmanager/workmanager.dart';

/// 任务注册器
class TaskRegistrar {
  static const String syncTaskName = 'syncTask';
  static const String syncTaskIdentifier = 'com.easycomic.sync';

  /// 注册后台任务
  static Future<void> registerTasks() async {
    if (Platform.isAndroid) {
      await Workmanager().registerPeriodicTask(
        syncTaskIdentifier,
        syncTaskName,
        frequency: const Duration(hours: 4),
        constraints: Constraints(
          networkType: NetworkType.connected,
          requiresBatteryNotLow: true,
        ),
        backoffPolicy: BackoffPolicy.exponential,
        backoffPolicyDelay: const Duration(minutes: 15),
      );
      if (kDebugMode) {
        print('Android: 后台同步任务已注册，每4小时执行一次');
      }
    } else if (Platform.isIOS) {
      // TODO(developer): iOS 后台任务集成
      if (kDebugMode) {
        print('iOS: 后台任务待实现');
      }
    }
  }
}
