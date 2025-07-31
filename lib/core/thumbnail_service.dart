import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';
import 'package:crypto/crypto.dart';
import 'dart:convert';

/// Service for generating and managing thumbnails
/// Provides efficient thumbnail generation with caching and memory management
class ThumbnailService {
  static const int _maxCacheSize = 50 * 1024 * 1024; // 50MB
  static const int _thumbnailSize = 200;
  static const int _thumbnailQuality = 85;
  static const String _cacheSubdir = 'thumbnails';

  static Directory? _cacheDir;
  static final Map<String, String> _memoryCache = {};
  static int _currentCacheSize = 0;

  /// Initialize the thumbnail service
  static Future<void> initialize() async {
    try {
      final appDir = await getApplicationDocumentsDirectory();
      _cacheDir = Directory(path.join(appDir.path, _cacheSubdir));
      
      if (!await _cacheDir!.exists()) {
        await _cacheDir!.create(recursive: true);
      }
      
      await _calculateCacheSize();
    } catch (e) {
      // Silently fail initialization, service will work without caching
    }
  }

  /// Generate thumbnail from image data
  static Future<String?> generateThumbnail(
    Uint8List imageData,
    String key, {
    int? size,
    int? quality,
  }) async {
    try {
      // Check if thumbnail already exists in cache
      final cachedPath = await getCachedThumbnail(key);
      if (cachedPath != null) {
        return cachedPath;
      }

      // Generate thumbnail
      final thumbnailData = await _resizeImage(
        imageData,
        size ?? _thumbnailSize,
        quality ?? _thumbnailQuality,
      );

      if (thumbnailData == null) return null;

      // Save to cache
      final thumbnailPath = await _saveThumbnail(key, thumbnailData);
      
      if (thumbnailPath != null) {
        _memoryCache[key] = thumbnailPath;
        await _cleanupCacheIfNeeded();
      }

      return thumbnailPath;
    } catch (e) {
      return null;
    }
  }

  /// Get cached thumbnail path
  static Future<String?> getCachedThumbnail(String key) async {
    try {
      // Check memory cache first
      if (_memoryCache.containsKey(key)) {
        final cachedPath = _memoryCache[key];
        if (cachedPath != null && await File(cachedPath).exists()) {
          return cachedPath;
        } else {
          _memoryCache.remove(key);
        }
      }

      // Check file system cache
      if (_cacheDir == null) return null;
      
      final hashedKey = _hashKey(key);
      final thumbnailFile = File(path.join(_cacheDir!.path, '$hashedKey.jpg'));
      
      if (await thumbnailFile.exists()) {
        _memoryCache[key] = thumbnailFile.path;
        return thumbnailFile.path;
      }

      return null;
    } catch (e) {
      return null;
    }
  }

  /// Generate thumbnails for multiple images in batch
  static Future<Map<String, String?>> generateBatchThumbnails(
    Map<String, Uint8List> imageDataMap, {
    int? size,
    int? quality,
  }) async {
    final Map<String, String?> results = {};
    
    for (final entry in imageDataMap.entries) {
      results[entry.key] = await generateThumbnail(
        entry.value,
        entry.key,
        size: size,
        quality: quality,
      );
    }
    
    return results;
  }

  /// Clear all cached thumbnails
  static Future<void> clearCache() async {
    try {
      _memoryCache.clear();
      
      if (_cacheDir != null && await _cacheDir!.exists()) {
        await for (final file in _cacheDir!.list()) {
          if (file is File) {
            await file.delete();
          }
        }
      }
      
      _currentCacheSize = 0;
    } catch (e) {
      // Silently fail cache clearing
    }
  }

  /// Remove specific thumbnail from cache
  static Future<void> removeThumbnail(String key) async {
    try {
      _memoryCache.remove(key);
      
      if (_cacheDir != null) {
        final hashedKey = _hashKey(key);
        final thumbnailFile = File(path.join(_cacheDir!.path, '$hashedKey.jpg'));
        
        if (await thumbnailFile.exists()) {
          final fileSize = await thumbnailFile.length();
          await thumbnailFile.delete();
          _currentCacheSize -= fileSize;
        }
      }
    } catch (e) {
      // Silently fail thumbnail removal
    }
  }

  /// Get current cache size in bytes
  static int getCurrentCacheSize() => _currentCacheSize;

  /// Get maximum cache size in bytes
  static int getMaxCacheSize() => _maxCacheSize;

  /// Check if cache cleanup is needed
  static bool needsCleanup() => _currentCacheSize > _maxCacheSize;

  /// Force cleanup of cache
  static Future<void> cleanupCache() async {
    await _cleanupCacheIfNeeded(force: true);
  }

  /// Resize image to thumbnail size
  static Future<Uint8List?> _resizeImage(
    Uint8List imageData,
    int size,
    int quality,
  ) async {
    try {
      final codec = await ui.instantiateImageCodec(
        imageData,
        targetWidth: size,
        targetHeight: size,
      );
      
      final frame = await codec.getNextFrame();
      final image = frame.image;
      
      final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
      if (byteData == null) return null;
      
      return byteData.buffer.asUint8List();
    } catch (e) {
      return null;
    }
  }

  /// Save thumbnail to file system
  static Future<String?> _saveThumbnail(String key, Uint8List thumbnailData) async {
    try {
      if (_cacheDir == null) return null;
      
      final hashedKey = _hashKey(key);
      final thumbnailFile = File(path.join(_cacheDir!.path, '$hashedKey.jpg'));
      
      await thumbnailFile.writeAsBytes(thumbnailData);
      _currentCacheSize += thumbnailData.length;
      
      return thumbnailFile.path;
    } catch (e) {
      return null;
    }
  }

  /// Hash key for filename
  static String _hashKey(String key) {
    final bytes = utf8.encode(key);
    final digest = sha256.convert(bytes);
    return digest.toString();
  }

  /// Calculate current cache size
  static Future<void> _calculateCacheSize() async {
    try {
      _currentCacheSize = 0;
      
      if (_cacheDir == null || !await _cacheDir!.exists()) return;
      
      await for (final file in _cacheDir!.list()) {
        if (file is File) {
          final stat = await file.stat();
          _currentCacheSize += stat.size;
        }
      }
    } catch (e) {
      _currentCacheSize = 0;
    }
  }

  /// Cleanup cache if needed using LRU strategy
  static Future<void> _cleanupCacheIfNeeded({bool force = false}) async {
    if (!force && !needsCleanup()) return;
    
    try {
      if (_cacheDir == null || !await _cacheDir!.exists()) return;
      
      final files = <FileSystemEntity>[];
      await for (final file in _cacheDir!.list()) {
        if (file is File) {
          files.add(file);
        }
      }
      
      // Sort by last accessed time (oldest first)
      files.sort((a, b) {
        final statA = a.statSync();
        final statB = b.statSync();
        return statA.accessed.compareTo(statB.accessed);
      });
      
      // Remove oldest files until cache size is acceptable
      final targetSize = (_maxCacheSize * 0.8).round(); // 80% of max size
      
      for (final file in files) {
        if (_currentCacheSize <= targetSize) break;
        
        final stat = file.statSync();
        await file.delete();
        _currentCacheSize -= stat.size;
        
        // Remove from memory cache
        _memoryCache.removeWhere((key, value) => value == file.path);
      }
    } catch (e) {
      // Silently fail cleanup
    }
  }
}

/// Widget for displaying thumbnails with loading states
class ThumbnailImage extends StatefulWidget {
  final String thumbnailKey;
  final Uint8List? imageData;
  final double? width;
  final double? height;
  final BoxFit fit;
  final Widget? placeholder;
  final Widget? errorWidget;
  final bool generateIfMissing;

  const ThumbnailImage({
    super.key,
    required this.thumbnailKey,
    this.imageData,
    this.width,
    this.height,
    this.fit = BoxFit.cover,
    this.placeholder,
    this.errorWidget,
    this.generateIfMissing = true,
  });

  @override
  State<ThumbnailImage> createState() => _ThumbnailImageState();
}

class _ThumbnailImageState extends State<ThumbnailImage> {
  String? _thumbnailPath;
  bool _isLoading = false;
  bool _hasError = false;

  @override
  void initState() {
    super.initState();
    _loadThumbnail();
  }

  @override
  void didUpdateWidget(ThumbnailImage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.thumbnailKey != oldWidget.thumbnailKey) {
      _loadThumbnail();
    }
  }

  Future<void> _loadThumbnail() async {
    setState(() {
      _isLoading = true;
      _hasError = false;
    });

    try {
      String? thumbnailPath = await ThumbnailService.getCachedThumbnail(widget.thumbnailKey);
      
      if (thumbnailPath == null && widget.generateIfMissing && widget.imageData != null) {
        thumbnailPath = await ThumbnailService.generateThumbnail(
          widget.imageData!,
          widget.thumbnailKey,
        );
      }

      if (mounted) {
        setState(() {
          _thumbnailPath = thumbnailPath;
          _isLoading = false;
          _hasError = thumbnailPath == null;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _hasError = true;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return widget.placeholder ??
          Container(
            width: widget.width,
            height: widget.height,
            color: Colors.grey[300],
            child: const Center(
              child: CircularProgressIndicator(),
            ),
          );
    }

    if (_hasError || _thumbnailPath == null) {
      return widget.errorWidget ??
          Container(
            width: widget.width,
            height: widget.height,
            color: Colors.grey[300],
            child: const Icon(Icons.error_outline),
          );
    }

    return Image.file(
      File(_thumbnailPath!),
      width: widget.width,
      height: widget.height,
      fit: widget.fit,
      errorBuilder: (context, error, stackTrace) {
        return widget.errorWidget ??
            Container(
              width: widget.width,
              height: widget.height,
              color: Colors.grey[300],
              child: const Icon(Icons.error_outline),
            );
      },
    );
  }
}