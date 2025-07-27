import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:webdav_client/webdav_client.dart';
import '../core/sync_engine.dart';
import '../core/webdav_service.dart';
import '../data/drift_db.dart';
import '../models/sync_models.dart';
import 'home_page.dart';

final webdavServiceProvider = Provider<WebDAVService>((ref) {
  // 在实际应用中，这些配置应该来自用户的设置
  return WebDAVService(
    client: Client(
      'https://dav.jianguoyun.com/dav/',
      'user',
      'password',
    ),
  );
});

final syncEngineProvider = Provider<SyncEngine>((ref) {
  final db = ref.watch(dbProvider);
  final webdavService = ref.watch(webdavServiceProvider);
  return SyncEngine(db: db, webdavService: webdavService);
});

class SyncNotifier extends AsyncNotifier<SyncResult> {
  @override
  FutureOr<SyncResult> build() =>
      SyncResult(uploaded: 0, downloaded: 0, conflicts: 0, errors: []);

  Future<void> sync() async {
    state = const AsyncValue.loading();
    final syncEngine = ref.read(syncEngineProvider);

    try {
      final result = await syncEngine.sync();
      state = AsyncValue.data(result);
    } catch (error, stackTrace) {
      state = AsyncValue.error(error, stackTrace);
    }
  }
}

final syncProvider =
    AsyncNotifierProvider<SyncNotifier, SyncResult>(SyncNotifier.new);
