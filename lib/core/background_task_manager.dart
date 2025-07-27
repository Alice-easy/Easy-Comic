import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:workmanager/workmanager.dart';

import '../data/drift_db.dart';
import 'sync_engine.dart';
import 'task_registrar.dart';
import 'webdav_service.dart';

/// 后台任务管理器
class BackgroundTaskManager {
  /// 初始化 WorkManager
  static Future<void> initialize() async {
    if (Platform.isAndroid) {
      await Workmanager().initialize(
        callbackDispatcher,
        isInDebugMode: kDebugMode,
      );
    } else if (Platform.isIOS) {
      // TODO(developer): iOS 后台任务集成
      if (kDebugMode) {
        print('iOS: 后台任务待实现');
      }
    }
  }
}

/// WorkManager 的回调分发器
/// 注意：这个函数必须是顶层函数
@pragma('vm:entry-point')
void callbackDispatcher() {
  Workmanager().executeTask((task, inputData) {
    if (kDebugMode) {
      print('执行后台任务: $task');
    }

    switch (task) {
      case TaskRegistrar.syncTaskName:
        return _handleSyncTask(inputData);
      default:
        return Future.value(true);
    }
  });
}

/// 处理同步任务
Future<bool> _handleSyncTask(Map<String, dynamic>? inputData) async {
  try {
    if (kDebugMode) {
      print('开始执行后台同步任务');
    }

    // 直接实例化依赖项，避免使用 ProviderContainer
    final db = DriftDb();
    // 在实际应用中，这些配置应该来自用户的设置
    final webdavService = WebDAVService(
      host: 'https://dav.jianguoyun.com/dav/',
      user: 'user',
      password: 'password',
    );
    final syncEngine = SyncEngine(db: db, webdavService: webdavService);

    await syncEngine.sync();

    if (kDebugMode) {
      print('后台同步任务完成');
    }

    return true;
  } catch (e) {
    if (kDebugMode) {
      print('后台同步任务失败: $e');
    }
    return false;
  }
}
