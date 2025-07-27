import 'package:drift/native.dart';
import 'package:easy_comic/data/drift_db.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('drift db test - insert and retrieve currentPage = 2', () async {
    // Create an in-memory database for testing
    final database = DriftDb.withExecutor(NativeDatabase.memory());

    try {
      const testFileHash = 'test_hash_123';
      const expectedCurrentPage = 2;
      const totalPages = 10;

      // Insert progress
      await database.upsertProgress(testFileHash, expectedCurrentPage, totalPages);

      // Retrieve progress
      final progress = await database.getProgress(testFileHash);

      // Assertions
      expect(progress, isNotNull, reason: 'Progress should not be null');
      expect(
        progress!.fileHash,
        equals(testFileHash),
        reason: 'FileHash should match',
      );
      expect(
        progress.currentPage,
        equals(expectedCurrentPage),
        reason: 'CurrentPage should be 2',
      );
    } finally {
      await database.close();
    }
  });
}
