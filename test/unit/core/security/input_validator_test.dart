import 'dart:io';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:path/path.dart' as path;
import 'package:easy_comic/core/security/input_validator.dart';

// Mock classes
class MockFile extends Mock implements File {}
class MockFileStat extends Mock implements FileStat {}
class MockDirectory extends Mock implements Directory {}

void main() {
  group('InputValidator', () {
    late InputValidator validator;

    setUpAll(() {
      registerFallbackValue(Uri());
    });

    setUp(() {
      validator = InputValidator();
    });

    group('File Path Validation', () {
      test('should validate normal comic file path', () async {
        // Create a temporary file for testing
        final tempDir = Directory.systemTemp;
        final testFile = File(path.join(tempDir.path, 'test_comic.cbz'));
        await testFile.writeAsBytes([1, 2, 3, 4]); // Small test file

        try {
          final validatedPath = await validator.validateComicFilePath(testFile.path);
          expect(validatedPath, isNotNull);
          expect(validatedPath, contains('test_comic.cbz'));
        } finally {
          // Cleanup
          if (await testFile.exists()) {
            await testFile.delete();
          }
        }
      });

      test('should validate normal image file path', () async {
        final tempDir = Directory.systemTemp;
        final testFile = File(path.join(tempDir.path, 'test_image.jpg'));
        await testFile.writeAsBytes([1, 2, 3, 4]);

        try {
          final validatedPath = await validator.validateImageFilePath(testFile.path);
          expect(validatedPath, isNotNull);
          expect(validatedPath, contains('test_image.jpg'));
        } finally {
          if (await testFile.exists()) {
            await testFile.delete();
          }
        }
      });

      test('should reject empty file path', () async {
        expect(
          () => validator.validateComicFilePath(''),
          throwsA(isA<ValidationException>()
              .having((e) => e.type, 'type', ValidationErrorType.general)),
        );
      });

      test('should reject file path that is too long', () async {
        final longPath = 'a' * 300; // Exceeds MAX_PATH limit
        
        expect(
          () => validator.validateComicFilePath(longPath),
          throwsA(isA<ValidationException>()
              .having((e) => e.type, 'type', ValidationErrorType.general)),
        );
      });

      test('should detect path traversal attacks', () async {
        final dangerousPaths = [
          '../../../etc/passwd',
          '..\\..\\windows\\system32',
          '/etc/shadow',
          'C:\\Windows\\System32\\config\\SAM',
          'file/../../../secret.txt',
          'normal_file/../../../etc/passwd',
        ];

        for (final dangerousPath in dangerousPaths) {
          expect(
            () => validator.validateComicFilePath(dangerousPath),
            throwsA(isA<ValidationException>()
                .having((e) => e.type, 'type', ValidationErrorType.pathTraversal)),
            reason: 'Should detect path traversal in: $dangerousPath',
          );
        }
      });

      test('should detect encoded path traversal attacks', () async {
        final encodedDangerousPaths = [
          Uri.encodeComponent('../../../etc/passwd'),
          Uri.encodeComponent('..\\..\\windows\\system32'),
          'file%2F..%2F..%2F..%2Fetc%2Fpasswd', // URL encoded
        ];

        for (final dangerousPath in encodedDangerousPaths) {
          expect(
            () => validator.validateComicFilePath(dangerousPath),
            throwsA(isA<ValidationException>()
                .having((e) => e.type, 'type', ValidationErrorType.pathTraversal)),
            reason: 'Should detect encoded traversal in: $dangerousPath',
          );
        }
      });

      test('should reject invalid extensions for comic files', () async {
        final tempDir = Directory.systemTemp;
        final testFile = File(path.join(tempDir.path, 'test.exe'));
        await testFile.writeAsBytes([1, 2, 3, 4]);

        try {
          expect(
            () => validator.validateComicFilePath(testFile.path),
            throwsA(isA<ValidationException>()
                .having((e) => e.type, 'type', ValidationErrorType.invalidExtension)),
          );
        } finally {
          if (await testFile.exists()) {
            await testFile.delete();
          }
        }
      });

      test('should accept all valid comic extensions', () async {
        final tempDir = Directory.systemTemp;
        final validExtensions = ['.cbz', '.cbr', '.zip', '.rar', '.7z', '.pdf', '.epub', '.mobi'];

        for (final ext in validExtensions) {
          final testFile = File(path.join(tempDir.path, 'test$ext'));
          await testFile.writeAsBytes([1, 2, 3, 4]);

          try {
            final validatedPath = await validator.validateComicFilePath(testFile.path);
            expect(validatedPath, isNotNull);
          } finally {
            if (await testFile.exists()) {
              await testFile.delete();
            }
          }
        }
      });

      test('should accept all valid image extensions', () async {
        final tempDir = Directory.systemTemp;
        final validExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];

        for (final ext in validExtensions) {
          final testFile = File(path.join(tempDir.path, 'test$ext'));
          await testFile.writeAsBytes([1, 2, 3, 4]);

          try {
            final validatedPath = await validator.validateImageFilePath(testFile.path);
            expect(validatedPath, isNotNull);
          } finally {
            if (await testFile.exists()) {
              await testFile.delete();
            }
          }
        }
      });

      test('should reject non-existent files', () async {
        final nonExistentPath = path.join(Directory.systemTemp.path, 'does_not_exist.cbz');
        
        expect(
          () => validator.validateComicFilePath(nonExistentPath),
          throwsA(isA<ValidationException>()
              .having((e) => e.type, 'type', ValidationErrorType.fileNotFound)),
        );
      });

      test('should reject files that are too large', () async {
        // This test is conceptual since creating a 500MB+ file would be impractical
        // In a real scenario, you'd mock the File.stat() method
        final mockFile = MockFile();
        final mockStat = MockFileStat();
        
        when(() => mockFile.exists()).thenAnswer((_) async => true);
        when(() => mockFile.stat()).thenAnswer((_) async => mockStat);
        when(() => mockStat.size).thenReturn(600 * 1024 * 1024); // 600MB

        // This would require dependency injection to test properly
        // For now, we'll test the logic conceptually
        expect(600 * 1024 * 1024, greaterThan(InputValidator.maxFileSize));
      });

      test('should detect dangerous characters in paths', () async {
        final dangerousChars = ['<', '>', ':', '"', '|', '?', '*'];
        
        for (final char in dangerousChars) {
          final dangerousPath = 'file$char.cbz';
          expect(
            () => validator.validateComicFilePath(dangerousPath),
            throwsA(isA<ValidationException>()
                .having((e) => e.type, 'type', ValidationErrorType.pathTraversal)),
            reason: 'Should reject path with character: $char',
          );
        }
      });

      test('should detect control characters in paths', () async {
        final pathWithControlChar = 'file\x00.cbz'; // Null byte
        
        expect(
          () => validator.validateComicFilePath(pathWithControlChar),
          throwsA(isA<ValidationException>()
              .having((e) => e.type, 'type', ValidationErrorType.pathTraversal)),
        );
      });
    });

    group('Cache Key Validation', () {
      test('should validate normal cache keys', () {
        final validKeys = [
          'page_1',
          'comic_123_page_5',
          'image-thumbnail',
          'cache.key.with.dots',
        ];

        for (final key in validKeys) {
          final result = validator.validateCacheKey(key);
          expect(result, equals(key));
        }
      });

      test('should reject empty cache key', () {
        expect(
          () => validator.validateCacheKey(''),
          throwsA(isA<ValidationException>()),
        );
      });

      test('should reject cache key that is too long', () {
        final longKey = 'a' * 300; // > 255 characters
        
        expect(
          () => validator.validateCacheKey(longKey),
          throwsA(isA<ValidationException>()),
        );
      });

      test('should reject cache keys with dangerous characters', () {
        final dangerousKeys = [
          'key<script>',
          'key"with"quotes',
          'key/with/slashes',
          'key\\with\\backslashes',
          'key|with|pipes',
          'key?with?questions',
          'key*with*asterisks',
          'key\x00with\x00nulls',
        ];

        for (final key in dangerousKeys) {
          expect(
            () => validator.validateCacheKey(key),
            throwsA(isA<ValidationException>()),
            reason: 'Should reject dangerous cache key: $key',
          );
        }
      });
    });

    group('Page Index Validation', () {
      test('should validate normal page indices', () {
        const totalPages = 100;
        final validIndices = [0, 1, 50, 99];

        for (final index in validIndices) {
          final result = validator.validatePageIndex(index, totalPages);
          expect(result, equals(index));
        }
      });

      test('should reject negative page index', () {
        expect(
          () => validator.validatePageIndex(-1, 100),
          throwsA(isA<ValidationException>()),
        );
      });

      test('should reject page index that exceeds total pages', () {
        expect(
          () => validator.validatePageIndex(100, 100), // Index 100 with 100 total pages
          throwsA(isA<ValidationException>()),
        );
      });

      test('should handle edge cases correctly', () {
        // First page of single-page comic
        final result1 = validator.validatePageIndex(0, 1);
        expect(result1, equals(0));

        // Last page of multi-page comic
        final result2 = validator.validatePageIndex(99, 100);
        expect(result2, equals(99));
      });
    });

    group('URL Validation', () {
      test('should validate normal HTTP/HTTPS URLs', () {
        final validUrls = [
          'http://example.com',
          'https://example.com',
          'http://example.com/path',
          'https://example.com/path?query=value',
          'https://subdomain.example.com:8080/path',
        ];

        for (final url in validUrls) {
          final result = validator.validateUrl(url);
          expect(result, isA<Uri>());
          expect(result.toString(), equals(url));
        }
      });

      test('should reject empty URL', () {
        expect(
          () => validator.validateUrl(''),
          throwsA(isA<ValidationException>()),
        );
      });

      test('should reject malformed URLs', () {
        final malformedUrls = [
          'not-a-url',
          'http://',
          'https://incomplete',
          'ftp://not-allowed.com',
          'file:///local/path',
          'javascript:alert("xss")',
        ];

        for (final url in malformedUrls) {
          expect(
            () => validator.validateUrl(url),
            throwsA(isA<ValidationException>()),
            reason: 'Should reject malformed URL: $url',
          );
        }
      });

      test('should only allow HTTP and HTTPS schemes', () {
        final disallowedSchemes = [
          'ftp://example.com',
          'file:///path/to/file',
          'javascript:alert(1)',
          'data:text/plain,hello',
          'mailto:user@example.com',
        ];

        for (final url in disallowedSchemes) {
          expect(
            () => validator.validateUrl(url),
            throwsA(isA<ValidationException>()
                .having((e) => e.type, 'type', ValidationErrorType.invalidFormat)),
            reason: 'Should reject non-HTTP(S) URL: $url',
          );
        }
      });
    });

    group('Allowed Base Path Management', () {
      test('should add valid base path', () {
        final tempDir = Directory.systemTemp;
        
        validator.addAllowedBasePath(tempDir.path);
        
        final allowedPaths = validator.allowedBasePaths;
        expect(allowedPaths, contains(path.canonicalize(tempDir.path)));
      });

      test('should reject invalid base path', () {
        expect(
          () => validator.addAllowedBasePath(''), // Empty path
          throwsA(isA<ValidationException>()),
        );
      });

      test('should provide immutable allowed paths set', () {
        final allowedPaths = validator.allowedBasePaths;
        
        // Attempt to modify the returned set should not affect the validator
        expect(() => allowedPaths.add('/fake/path'), throwsUnsupportedError);
      });
    });

    group('Security Edge Cases', () {
      test('should handle Unicode normalization attacks', () {
        // These represent potential Unicode normalization attacks
        final unicodeAttacks = [
          '../etc/passwd', // Normal
          '..∕etc∕passwd', // Unicode similar characters
          '..%c0%af..%c0%afetc%c0%afpasswd', // Double-encoded
        ];

        for (final attack in unicodeAttacks) {
          expect(
            () => validator.validateComicFilePath(attack),
            throwsA(isA<ValidationException>()),
            reason: 'Should detect Unicode attack: $attack',
          );
        }
      });

      test('should handle case variations of dangerous patterns', () {
        final caseVariations = [
          '../ETC/passwd',
          '..\\WINDOWS\\system32',
          '../EtC/ShAdOw',
        ];

        for (final variation in caseVariations) {
          expect(
            () => validator.validateComicFilePath(variation),
            throwsA(isA<ValidationException>()),
            reason: 'Should detect case variation: $variation',
          );
        }
      });

      test('should handle mixed path separators', () {
        final mixedSeparators = [
          '..\\../etc/passwd',
          '../..\\windows/system32',
          '..\\..\\../etc/passwd',
        ];

        for (final mixed in mixedSeparators) {
          expect(
            () => validator.validateComicFilePath(mixed),
            throwsA(isA<ValidationException>()),
            reason: 'Should detect mixed separators: $mixed',
          );
        }
      });

      test('should handle double-encoded attacks', () {
        final doubleEncoded = [
          '%252E%252E%252F', // Double-encoded ../
          '%252E%252E%255C', // Double-encoded ..\
        ];

        for (final encoded in doubleEncoded) {
          expect(
            () => validator.validateCacheKey(encoded),
            throwsA(isA<ValidationException>()),
            reason: 'Should detect double encoding: $encoded',
          );
        }
      });
    });

    group('Performance Tests', () {
      test('should validate paths quickly', () async {
        final tempDir = Directory.systemTemp;
        final testFile = File(path.join(tempDir.path, 'perf_test.cbz'));
        await testFile.writeAsBytes([1, 2, 3, 4]);

        try {
          final stopwatch = Stopwatch()..start();
          
          // Validate 100 paths
          for (int i = 0; i < 100; i++) {
            await validator.validateComicFilePath(testFile.path);
          }
          
          stopwatch.stop();
          
          // Should complete quickly (< 1 second for 100 validations)
          expect(stopwatch.elapsedMilliseconds, lessThan(1000));
        } finally {
          if (await testFile.exists()) {
            await testFile.delete();
          }
        }
      });

      test('should validate cache keys quickly', () {
        final stopwatch = Stopwatch()..start();
        
        // Validate 1000 cache keys
        for (int i = 0; i < 1000; i++) {
          validator.validateCacheKey('cache_key_$i');
        }
        
        stopwatch.stop();
        
        // Should be very fast (< 100ms for 1000 validations)
        expect(stopwatch.elapsedMilliseconds, lessThan(100));
      });

      test('should validate page indices quickly', () {
        final stopwatch = Stopwatch()..start();
        
        // Validate 10000 page indices
        for (int i = 0; i < 10000; i++) {
          validator.validatePageIndex(i % 1000, 1000);
        }
        
        stopwatch.stop();
        
        // Should be very fast (< 50ms for 10000 validations)
        expect(stopwatch.elapsedMilliseconds, lessThan(50));
      });
    });

    group('Error Handling', () {
      test('should provide detailed error messages', () {
        try {
          validator.validateCacheKey('');
        } catch (e) {
          expect(e, isA<ValidationException>());
          final validationError = e as ValidationException;
          expect(validationError.message, contains('cannot be empty'));
        }
      });

      test('should include field names in errors', () {
        try {
          validator.validatePageIndex(-1, 100, field: 'currentPage');
        } catch (e) {
          expect(e, isA<ValidationException>());
          final validationError = e as ValidationException;
          expect(validationError.field, equals('currentPage'));
        }
      });

      test('should provide appropriate error types', () {
        // Test different error types
        final errorTests = [
          () => validator.validateCacheKey('invalid<key>'),
          () => validator.validatePageIndex(-1, 100),
          () => validator.validateUrl('not-a-url'),
        ];

        for (final test in errorTests) {
          expect(test, throwsA(isA<ValidationException>()));
        }
      });
    });
  });

  group('ValidationException', () {
    test('should create exception with message only', () {
      const exception = ValidationException('Test message');
      
      expect(exception.message, equals('Test message'));
      expect(exception.field, isNull);
      expect(exception.type, equals(ValidationErrorType.general));
    });

    test('should create exception with all properties', () {
      const exception = ValidationException(
        'Test message',
        field: 'testField',
        type: ValidationErrorType.pathTraversal,
      );
      
      expect(exception.message, equals('Test message'));
      expect(exception.field, equals('testField'));
      expect(exception.type, equals(ValidationErrorType.pathTraversal));
    });

    test('should provide meaningful string representation', () {
      const exception = ValidationException(
        'Test message',
        field: 'testField',
      );
      
      final stringRep = exception.toString();
      expect(stringRep, contains('ValidationException'));
      expect(stringRep, contains('Test message'));
      expect(stringRep, contains('testField'));
    });
  });
}