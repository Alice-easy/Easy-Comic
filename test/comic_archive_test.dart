import 'dart:io';

import 'package:easy_comic/core/comic_archive.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:path/path.dart' as p;

void main() {
  group('ComicArchive', () {
    test('listPages returns correct number of pages for sample.cbz', () async {
      // Get the directory where the test file is located
      final testDir = p.dirname(Platform.script.toFilePath());
      final fixturePath = p.join(testDir, 'fixtures', 'sample.cbz');

      // Check if the fixture file exists
      final file = File(fixturePath);
      if (!await file.exists()) {
        // If fixture doesn't exist, skip the test
        return;
      }

      final comic = ComicArchive(fixturePath);
      final pages = await comic.listPages();

      // Assert that there are 30 pages
      expect(pages.length, 30);
    });

    test('throws error for unsupported file types', () {
      final comic = ComicArchive('file.pdf');
      expect(() async => comic.listPages(), throwsUnsupportedError);
    });
  });
}
