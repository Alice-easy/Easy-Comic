import 'package:easy_comic/core/sync_engine.dart';
import 'package:easy_comic/core/webdav_service.dart';
import 'package:easy_comic/data/drift_db.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

class MockDriftDb extends Mock implements DriftDb {}

class MockWebDAVService extends Mock implements WebDAVService {}

void main() {
  test('SyncEngine can be instantiated', () {
    final syncEngine = SyncEngine(
      db: MockDriftDb(),
      webdavService: MockWebDAVService(),
    );
    expect(syncEngine, isNotNull);
    expect(syncEngine.status, equals(SyncStatus.idle));
  });
}
