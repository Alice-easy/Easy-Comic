// lib/domain/services/archive_service.dart

import 'dart:typed_data';

// --- Custom Exceptions ---

/// Exception thrown when the archive file is not found.
class ArchiveFileNotFoundException implements Exception {
  final String path;
  ArchiveFileNotFoundException(this.path);
  @override
  String toString() => 'Archive file not found at path: $path';
}

/// Exception thrown for unsupported archive formats.
class UnsupportedArchiveFormatException implements Exception {
  final String format;
  UnsupportedArchiveFormatException(this.format);
  @override
  String toString() =>
      'Unsupported archive format: .$format. Only .zip and .cbz are supported.';
}

// --- Service Interface ---

/// A service for handling comic archive files (.zip, .cbz).
abstract class ArchiveService {
  /// Extracts all image files from a given archive file path.
  ///
  /// Takes a [filePath] to a .zip or .cbz file.
  /// Returns a list of [Uint8List], where each list item is the byte data of an image.
  ///
  /// Throws [ArchiveFileNotFoundException] if the file does not exist.
  /// Throws [UnsupportedArchiveFormatException] if the file is not a .zip or .cbz file.
  Future<List<Uint8List>> extractImages(String filePath);
}