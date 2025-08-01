import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:archive/archive.dart';
import 'package:crypto/crypto.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:path/path.dart' as p;

class CacheServiceImpl implements CacheService {
  final BaseCacheManager _cacheManager;

  CacheServiceImpl({BaseCacheManager? cacheManager})
      : _cacheManager = cacheManager ?? DefaultCacheManager();

  @override
  Future<File> getCoverImage(String comicPath) async {
    final cacheKey = _generateCacheKey(comicPath);

    final fileInfo = await _cacheManager.getFileFromCache(cacheKey);
    if (fileInfo != null && await fileInfo.file.exists()) {
      return fileInfo.file;
    }

    final coverData = await _extractFirstImageFromArchive(comicPath);
    if (coverData == null) {
      throw Exception('Could not extract cover from $comicPath');
    }

    final file = await _cacheManager.putFile(
      cacheKey,
      coverData,
      fileExtension: _getFileExtensionFromData(coverData) ?? 'jpg',
    );
    return file;
  }

  String _generateCacheKey(String comicPath) {
    return sha256.convert(utf8.encode(comicPath)).toString();
  }

  String? _getFileExtensionFromData(Uint8List data) {
    if (data.length > 2 && data[0] == 0xFF && data[1] == 0xD8) {
      return 'jpg';
    }
    if (data.length > 8 &&
        data[0] == 0x89 &&
        data[1] == 0x50 &&
        data[2] == 0x4E &&
        data[3] == 0x47 &&
        data[4] == 0x0D &&
        data[5] == 0x0A &&
        data[6] == 0x1A &&
        data[7] == 0x0A) {
      return 'png';
    }
    if (data.length > 4 && data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
      return 'gif';
    }
    if (data.length > 12 && data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
      return 'webp';
    }
    return null;
  }

  Future<Uint8List?> _extractFirstImageFromArchive(String path) async {
    final bytes = await File(path).readAsBytes();
    final archive = ZipDecoder().decodeBytes(bytes);

    for (final file in archive) {
      if (file.isFile) {
        final lowerCaseName = file.name.toLowerCase();
        if (lowerCaseName.endsWith('.jpg') ||
            lowerCaseName.endsWith('.jpeg') ||
            lowerCaseName.endsWith('.png') ||
            lowerCaseName.endsWith('.webp') ||
            lowerCaseName.endsWith('.gif')) {
          return file.content as Uint8List;
        }
      }
    }
    return null;
  }
}