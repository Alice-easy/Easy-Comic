import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'dart:typed_data';

import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/core/services/unified_manga_importer_service.dart';
import 'package:easy_comic/domain/services/archive_service.dart';
import 'package:easy_comic/core/services/file_system_manager.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

// Mocks
class MockArchiveService extends Mock implements ArchiveService {}
class MockFileSystemManager extends Mock implements FileSystemManager {}
class MockLoggingService extends Mock implements LoggingService {}

void main() {
  late UnifiedMangaImporterService importer;
  late MockArchiveService mockArchiveService;
  late MockFileSystemManager mockFileSystemManager;
  late MockLoggingService mockLoggingService;

  setUp(() {
    mockArchiveService = MockArchiveService();
    mockFileSystemManager = MockFileSystemManager();
    mockLoggingService = MockLoggingService();
    importer = UnifiedMangaImporterService(
      archiveService: mockArchiveService,
      fileSystemManager: mockFileSystemManager,
      logger: mockLoggingService,
    );
    // Mock logger calls to avoid null pointer exceptions
    when(() => mockLoggingService.info(any())).thenAnswer((_) {});
    when(() => mockLoggingService.error(any(), any(), any())).thenAnswer((_) {});
  });

  group('UnifiedMangaImporterService', () {
    group('naturalSort', () {
      test('should sort file names naturally', () {
        // Arrange
        final files = [
          'page_10.jpg',
          'page_2.jpg',
          'page_1.jpg',
        ];

        // Act
        files.sort(importer.naturalSort);

        // Assert
        expect(files, equals(['page_1.jpg', 'page_2.jpg', 'page_10.jpg']));
      });
    });

    group('importFromPath', () {
      test('should call archiveService for zip files', () async {
        // Arrange
        const path = 'comic.zip';
        when(() => mockFileSystemManager.isZipFile(path)).thenReturn(true);
        when(() => mockArchiveService.extractImages(path)).thenAnswer((_) async => [Uint8List(0)]);

        // Act
        await importer.importFromPath(path);

        // Assert
        verify(() => mockArchiveService.extractImages(path)).called(1);
      });

      test('should call fileSystemManager for directories', () async {
        // Arrange
        const path = 'comic_folder';
        when(() => mockFileSystemManager.isZipFile(path)).thenReturn(false);
        when(() => mockFileSystemManager.isDirectory(path)).thenAnswer((_) async => true);
        when(() => mockFileSystemManager.getFilesInDirectory(path)).thenAnswer((_) async => []);

        // Act
        await importer.importFromPath(path);

        // Assert
        verify(() => mockFileSystemManager.getFilesInDirectory(path)).called(1);
      });

      test('should throw an exception for unsupported file types', () async {
        // Arrange
        const path = 'comic.txt';
        when(() => mockFileSystemManager.isZipFile(path)).thenReturn(false);
        when(() => mockFileSystemManager.isDirectory(path)).thenAnswer((_) async => false);

        // Act & Assert
        expect(() => importer.importFromPath(path), throwsA(isA<UnsupportedError>()));
      });
    });
  });
}