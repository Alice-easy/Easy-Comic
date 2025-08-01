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
    '.bmp',
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
    if (fileExtension != 'zip' && fileExtension != 'cbz' && fileExtension != 'rar' && fileExtension != 'cbr') {
      throw UnsupportedArchiveFormatException(fileExtension);
    }

    try {
      // 3. Read the archive from the file system.
      final bytes = await file.readAsBytes();
      final archive = ZipDecoder().decodeBytes(bytes);

      // 4. Filter for image files and sort them naturally
      final imageFiles = <_ImageFile>[];
      
      for (final fileInArchive in archive.files) {
        if (fileInArchive.isFile && fileInArchive.content != null) {
          final fileName = fileInArchive.name.toLowerCase();
          final cleanName = fileName.split('/').last; // Remove directory path
          
          if (_supportedImageExtensions.any((ext) => cleanName.endsWith(ext))) {
            // Skip hidden files and system files
            if (!cleanName.startsWith('.') && !cleanName.startsWith('__')) {
              imageFiles.add(_ImageFile(
                name: fileInArchive.name,
                content: fileInArchive.content as Uint8List,
              ));
            }
          }
        }
      }

      // 5. Sort images by filename naturally (handle numeric sequences properly)
      imageFiles.sort((a, b) => _naturalCompare(a.name, b.name));

      // 6. Extract sorted image data
      final imageBytesList = imageFiles.map((file) => file.content).toList();

      if (imageBytesList.isEmpty) {
        throw Exception('No valid images found in archive: $filePath');
      }

      return imageBytesList;
    } catch (e) {
      if (e is ArchiveFileNotFoundException || e is UnsupportedArchiveFormatException) {
        rethrow;
      }
      throw Exception('Failed to extract images from archive: ${e.toString()}');
    }
  }

  /// Get the first image as cover
  Future<Uint8List?> extractCoverImage(String filePath) async {
    try {
      final images = await extractImages(filePath);
      return images.isNotEmpty ? images.first : null;
    } catch (e) {
      return null; // Return null instead of throwing for cover extraction
    }
  }

  /// Get page count without extracting all images
  Future<int> getPageCount(String filePath) async {
    final file = File(filePath);
    
    if (!await file.exists()) {
      throw ArchiveFileNotFoundException(filePath);
    }

    try {
      final bytes = await file.readAsBytes();
      final archive = ZipDecoder().decodeBytes(bytes);
      
      int pageCount = 0;
      for (final fileInArchive in archive.files) {
        if (fileInArchive.isFile) {
          final fileName = fileInArchive.name.toLowerCase();
          final cleanName = fileName.split('/').last;
          
          if (_supportedImageExtensions.any((ext) => cleanName.endsWith(ext))) {
            if (!cleanName.startsWith('.') && !cleanName.startsWith('__')) {
              pageCount++;
            }
          }
        }
      }
      
      return pageCount;
    } catch (e) {
      throw Exception('Failed to get page count: ${e.toString()}');
    }
  }

  /// Natural string comparison for proper filename sorting
  int _naturalCompare(String a, String b) {
    // Extract directory and filename
    final aParts = a.split('/');
    final bParts = b.split('/');
    final aFile = aParts.last;
    final bFile = bParts.last;
    
    // Compare directories first if different
    if (aParts.length > 1 || bParts.length > 1) {
      final aDir = aParts.length > 1 ? aParts.sublist(0, aParts.length - 1).join('/') : '';
      final bDir = bParts.length > 1 ? bParts.sublist(0, bParts.length - 1).join('/') : '';
      final dirCompare = aDir.compareTo(bDir);
      if (dirCompare != 0) return dirCompare;
    }
    
    // Extract numeric parts for natural sorting
    final aMatch = RegExp(r'(\d+)').allMatches(aFile);
    final bMatch = RegExp(r'(\d+)').allMatches(bFile);
    
    if (aMatch.isNotEmpty && bMatch.isNotEmpty) {
      final aNumber = int.tryParse(aMatch.first.group(1)!) ?? 0;
      final bNumber = int.tryParse(bMatch.first.group(1)!) ?? 0;
      
      if (aNumber != bNumber) {
        return aNumber.compareTo(bNumber);
      }
    }
    
    // Fall back to string comparison
    return aFile.compareTo(bFile);
  }
}

/// Helper class to hold image file data
class _ImageFile {
  final String name;
  final Uint8List content;
  
  _ImageFile({required this.name, required this.content});
}