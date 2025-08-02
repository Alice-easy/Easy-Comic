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

/// A service for handling comic archive files (.zip, .cbz, .rar, .cbr).
abstract class ArchiveService {
  /// Extracts all image files from a given archive file path.
  ///
  /// Takes a [filePath] to a .zip, .cbz, .rar, or .cbr file.
  /// Returns a list of [Uint8List], where each list item is the byte data of an image.
  /// Images are sorted naturally by filename.
  ///
  /// Throws [ArchiveFileNotFoundException] if the file does not exist.
  /// Throws [UnsupportedArchiveFormatException] if the file format is not supported.
  Future<List<Uint8List>> extractImages(String filePath);

  /// Extracts the first image from an archive as a cover image.
  ///
  /// Returns null if no images are found or extraction fails.
  Future<Uint8List?> extractCoverImage(String filePath);

  /// Gets the page count without extracting all images.
  ///
  /// More efficient than extracting all images when only count is needed.
  ///
  /// Throws [ArchiveFileNotFoundException] if the file does not exist.
  Future<int> getPageCount(String filePath);

  /// Extracts an archive to a specified directory and returns a list of file paths.
  Future<List<String>> extractArchive(String filePath, String destinationPath);
}