import 'dart:io';
import 'dart:typed_data';
import 'package:archive/archive_io.dart';

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
  String toString() => 'Unsupported archive format: .$format. Only .zip and .cbz are supported.';
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

// --- Service Implementation ---

class ArchiveServiceImpl implements ArchiveService {
  // A set of supported image file extensions.
  static const _supportedImageExtensions = {
    '.jpg',
    '.jpeg',
    '.png',
    '.gif',
    '.webp',
  };

  @override
  Future<List<Uint8List>> extractImages(String filePath) async {
    final file = File(filePath);

    // 1. Check if the file exists.
    if (!await file.exists()) {
      throw ArchiveFileNotFoundException(filePath);
    }

    // 2. Check for supported file format.
    final fileExtension = filePath.split('.').last.toLowerCase();
    if (fileExtension != 'zip' && fileExtension != 'cbz') {
      throw UnsupportedArchiveFormatException(fileExtension);
    }

    final List<Uint8List> imageBytesList = [];

    try {
      // 3. Read the archive from the file system.
      final inputStream = InputFileStream(filePath);
      final archive = ZipDecoder().decodeBytes(await inputStream.toUint8List());

      // 4. Filter for image files and extract their data.
      for (final fileInArchive in archive.files) {
        if (fileInArchive.isFile) {
          final fileName = fileInArchive.name.toLowerCase();
          if (_supportedImageExtensions.any((ext) => fileName.endsWith(ext))) {
            // Add the image data to the list.
            imageBytesList.add(fileInArchive.content as Uint8List);
          }
        }
      }
      
      // Ensure the input stream is closed.
      await inputStream.close();

      return imageBytesList;
    } catch (e) {
      // Rethrow any other exceptions that might occur during extraction.
      // This could be a corrupted archive file, for example.
      rethrow;
    }
  }
}