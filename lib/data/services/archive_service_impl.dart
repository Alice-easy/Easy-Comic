// lib/data/services/archive_service_impl.dart

import 'dart:io';
import 'dart:typed_data';
import 'package:archive/archive_io.dart';
import 'package:easy_comic/domain/services/archive_service.dart';

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