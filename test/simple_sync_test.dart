import 'package:easy_comic/core/sync_engine.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('SyncEngine can be instantiated', () {
    final syncEngine = SyncEngine.instance;
    expect(syncEngine, isNotNull);
    expect(syncEngine.status, equals(SyncStatus.idle));
  });
}
