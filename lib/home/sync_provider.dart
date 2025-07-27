import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/sync_engine.dart';
import '../core/webdav_service.dart';
import '../models/sync_models.dart';
import 'home_page.dart';

final webdavServiceProvider = Provider<WebDAVService>(
  (ref) => WebDAVService(
    host: 'https://dav.jianguoyun.com/dav/',
    user: 'user',
    password: 'password',
  ),
);

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

final syncProvider = AsyncNotifierProvider<SyncNotifier, SyncResult>(
  SyncNotifier.new,
);
