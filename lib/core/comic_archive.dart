import 'dart:io';
import 'dart:typed_data';
import 'dart:async';
import 'package:archive/archive.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart' as p;
import '../domain/entities/comic_page.dart';
import 'error/retry_mechanism.dart';
import 'security/input_validator.dart';

/// Supported archive formats
enum ArchiveFormat {
  cbz,  // Comic Book ZIP
  cbr,  // Comic Book RAR  
  zip,  // Standard ZIP
  rar,  // Standard RAR
  sevenZ, // 7-Zip
  pdf,  // Portable Document Format
  epub, // Electronic Publication
}

/// Archive extraction progress information
class ExtractionProgress {
  final int currentFile;
  final int totalFiles;
  final String currentFileName;
  final double percentage;
  final int bytesExtracted;
  final int totalBytes;
  
  const ExtractionProgress({
    required this.currentFile,
    required this.totalFiles,
    required this.currentFileName,
    required this.percentage,
    required this.bytesExtracted,
    required this.totalBytes,
  });
  
  @override
  String toString() {
    return 'ExtractionProgress(${currentFile}/${totalFiles} files, '
        '${percentage.toStringAsFixed(1)}%, ${currentFileName})';
  }
}

/// Enhanced archive error types
enum ArchiveErrorType {
  corruption,
  unsupportedFormat,
  passwordRequired,
  extractionFailed,
  noValidImages,
  fileTooLarge,
  invalidStructure,
}

/// Archive-specific exception
class ArchiveException implements Exception {
  final String message;
  final ArchiveErrorType type;
  final String? filePath;
  final String? details;
  
  const ArchiveException(
    this.message, {
    required this.type,
    this.filePath,
    this.details,
  });
  
  @override
  String toString() {
    final buffer = StringBuffer('ArchiveException: $message');
    if (filePath != null) buffer.write(' (file: $filePath)');
    if (details != null) buffer.write(' - $details');
    return buffer.toString();
  }
}

/// Performs a "natural sort" on strings, correctly handling numbers.
/// For example, "page10.jpg" will come after "page2.jpg".
int naturalCompare(String s1, String s2) {
  final re = RegExp(r'([0-9]+|[^0-9]+)');
  final s1parts = re.allMatches(s1).map((m) => m.group(0)!).toList();
  final s2parts = re.allMatches(s2).map((m) => m.group(0)!).toList();

  final len = s1parts.length < s2parts.length ? s1parts.length : s2parts.length;

  for (var i = 0; i < len; i++) {
    final p1 = s1parts[i];
    final p2 = s2parts[i];
    final n1 = int.tryParse(p1);
    final n2 = int.tryParse(p2);

    if (n1 != null && n2 != null) {
      final cmp = n1.compareTo(n2);
      if (cmp != 0) return cmp;
    } else {
      final cmp = p1.compareTo(p2);
      if (cmp != 0) return cmp;
    }
  }

  return s1parts.length.compareTo(s2parts.length);
}

/// Enhanced ComicArchive class with comprehensive format support and error handling
class ComicArchive {
  final String? path;
  final Uint8List? bytes;
  final InputValidator _validator = InputValidator();
  final ExponentialBackoffRetry _retry = ExponentialBackoffRetry();
  
  Archive? _archive;
  ArchiveFormat? _format;
  List<ComicPage>? _cachedPages;

  ComicArchive({this.path, this.bytes})
      : assert(
          path != null || bytes != null,
          'Either path or bytes must be provided.',
        );

  /// Create ComicArchive from URI with security validation
  static Future<ComicArchive> fromUri(String uri) async {
    final validator = InputValidator();
    
    // Validate URI format
    final validatedUri = validator.validateUrl(uri, field: 'archiveUri');
    
    if (uri.startsWith('content://')) {
      // Handle Android content URIs through platform channel
      final bytes = await _readContentUri(uri);
      return ComicArchive(bytes: bytes, path: uri);
    }
    
    // Handle regular file paths with security validation
    final validatedPath = await validator.validateComicFilePath(uri);
    final file = File(validatedPath);
    
    final bytes = await RetryUtils.retryFile(() => file.readAsBytes());
    return ComicArchive(bytes: bytes, path: validatedPath);
  }

  /// Read content URI through platform channel
  static Future<Uint8List> _readContentUri(String uri) async {
    try {
      const channel = MethodChannel('comic_reader/file_access');
      final result = await channel.invokeMethod<Uint8List>('readContentUri', {'uri': uri});
      
      if (result == null) {
        throw ArchiveException(
          'Failed to read content URI',
          type: ArchiveErrorType.extractionFailed,
          filePath: uri,
        );
      }
      
      return result;
    } on PlatformException catch (e) {
      throw ArchiveException(
        'Platform error reading content URI: ${e.message}',
        type: ArchiveErrorType.extractionFailed,
        filePath: uri,
        details: e.code,
      );
    }
  }

  /// Extract pages with progress reporting and cancellation support
  Future<List<ComicPage>> extractPages({
    StreamController<ExtractionProgress>? progressController,
    CancelToken? cancelToken,
  }) async {
    if (_cachedPages != null) {
      return _cachedPages!;
    }

    try {
      return await _retry.execute(
        () => _performExtraction(progressController, cancelToken),
        config: RetryConfig.file,
        shouldRetry: (error) => _shouldRetryExtraction(error),
      );
    } catch (e) {
      if (e is ArchiveException) rethrow;
      
      throw ArchiveException(
        'Failed to extract pages: $e',
        type: ArchiveErrorType.extractionFailed,
        filePath: path,
        details: e.toString(),
      );
    }
  }

  Future<List<ComicPage>> _performExtraction(
    StreamController<ExtractionProgress>? progressController,
    CancelToken? cancelToken,
  ) async {
    // Validate and get archive
    final archive = await _getValidatedArchive();
    final imageFiles = _filterImageFiles(archive.files);
    
    if (imageFiles.isEmpty) {
      throw ArchiveException(
        'No valid image files found in archive',
        type: ArchiveErrorType.noValidImages,
        filePath: path,
      );
    }

    // Sort files naturally (page1, page2, page10)
    imageFiles.sort((a, b) => naturalCompare(a.name, b.name));

    final pages = <ComicPage>[];
    int totalBytes = imageFiles.fold(0, (sum, file) => sum + file.size);
    int processedBytes = 0;

    for (int i = 0; i < imageFiles.length; i++) {
      // Check for cancellation
      if (cancelToken?.isCancelled == true) {
        throw RetryCancelledException('Extraction cancelled by user');
      }

      final file = imageFiles[i];
      
      // Report progress
      progressController?.add(ExtractionProgress(
        currentFile: i + 1,
        totalFiles: imageFiles.length,
        currentFileName: file.name,
        percentage: ((i + 1) / imageFiles.length) * 100,
        bytesExtracted: processedBytes,
        totalBytes: totalBytes,
      ));

      try {
        // Extract file data
        final content = file.content as List<int>;
        final imageData = Uint8List.fromList(content);
        
        // Validate image data
        if (imageData.isEmpty) {
          continue; // Skip empty files
        }

        // Create comic page
        final page = ComicPage(
          pageIndex: i,
          imageData: imageData,
          path: file.name,
        );

        pages.add(page);
        processedBytes += file.size;

      } catch (e) {
        // Log error but continue with other files
        print('Warning: Failed to extract ${file.name}: $e');
        continue;
      }
    }

    if (pages.isEmpty) {
      throw ArchiveException(
        'Failed to extract any valid pages',
        type: ArchiveErrorType.noValidImages,
        filePath: path,
      );
    }

    // Report completion
    progressController?.add(ExtractionProgress(
      currentFile: imageFiles.length,
      totalFiles: imageFiles.length,
      currentFileName: 'Extraction complete',
      percentage: 100.0,
      bytesExtracted: totalBytes,
      totalBytes: totalBytes,
    ));

    _cachedPages = pages;
    return pages;
  }

  Future<Archive> _getValidatedArchive() async {
    if (_archive != null) {
      return _archive!;
    }

    // Detect and validate format
    _format = _detectArchiveFormat();
    
    if (_format == null) {
      throw ArchiveException(
        'Unsupported archive format',
        type: ArchiveErrorType.unsupportedFormat,
        filePath: path,
      );
    }

    // Get file bytes
    Uint8List fileBytes;
    if (bytes != null) {
      fileBytes = bytes!;
    } else if (path != null) {
      final validatedPath = await _validator.validateComicFilePath(path!);
      fileBytes = await File(validatedPath).readAsBytes();
    } else {
      throw ArchiveException(
        'No file data available',
        type: ArchiveErrorType.extractionFailed,
      );
    }

    // Validate file size
    if (fileBytes.length > 500 * 1024 * 1024) { // 500MB limit
      throw ArchiveException(
        'Archive file too large: ${fileBytes.length} bytes',
        type: ArchiveErrorType.fileTooLarge,
        filePath: path,
      );
    }

    // Decode archive with format-specific handling
    try {
      _archive = await _decodeArchive(fileBytes, _format!);
      return _archive!;
    } catch (e) {
      throw ArchiveException(
        'Failed to decode archive: $e',
        type: ArchiveErrorType.corruption,
        filePath: path,
        details: 'Format: $_format',
      );
    }
  }

  ArchiveFormat? _detectArchiveFormat() {
    String extension;
    
    if (path != null) {
      extension = p.extension(path!).toLowerCase();
    } else {
      // Try to detect from magic bytes if no path
      extension = _detectFromMagicBytes();
    }

    switch (extension) {
      case '.cbz':
      case '.zip':
        return ArchiveFormat.cbz;
      case '.cbr':
      case '.rar':
        return ArchiveFormat.cbr;
      case '.7z':
        return ArchiveFormat.sevenZ;
      case '.pdf':
        return ArchiveFormat.pdf;
      case '.epub':
        return ArchiveFormat.epub;
      default:
        return null;
    }
  }

  String _detectFromMagicBytes() {
    if (bytes == null || bytes!.length < 4) return '';
    
    final header = bytes!.take(4).toList();
    
    // ZIP magic bytes: PK (0x50 0x4B)
    if (header[0] == 0x50 && header[1] == 0x4B) {
      return '.zip';
    }
    
    // RAR magic bytes: Rar! (0x52 0x61 0x72 0x21)
    if (header[0] == 0x52 && header[1] == 0x61 && 
        header[2] == 0x72 && header[3] == 0x21) {
      return '.rar';
    }
    
    // PDF magic bytes: %PDF
    if (header[0] == 0x25 && header[1] == 0x50 && 
        header[2] == 0x44 && header[3] == 0x46) {
      return '.pdf';
    }
    
    return '';
  }

  Future<Archive> _decodeArchive(Uint8List fileBytes, ArchiveFormat format) async {
    switch (format) {
      case ArchiveFormat.cbz:
      case ArchiveFormat.zip:
        return ZipDecoder().decodeBytes(fileBytes);
        
      case ArchiveFormat.cbr:
      case ArchiveFormat.rar:
        // RAR format requires external library or platform-specific implementation
        throw ArchiveException(
          'RAR format not yet supported - requires platform-specific decoder',
          type: ArchiveErrorType.unsupportedFormat,
          filePath: path,
          details: 'Consider converting to CBZ format',
        );
        
      case ArchiveFormat.sevenZ:
        throw ArchiveException(
          '7-Zip format not yet supported',
          type: ArchiveErrorType.unsupportedFormat,
          filePath: path,
        );
        
      case ArchiveFormat.pdf:
      case ArchiveFormat.epub:
        throw ArchiveException(
          'PDF/EPUB formats require specialized handling',
          type: ArchiveErrorType.unsupportedFormat,
          filePath: path,
        );
    }
  }

  List<ArchiveFile> _filterImageFiles(List<ArchiveFile> files) {
    const imageExtensions = {'.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'};
    
    return files.where((file) {
      // Skip directories
      if (file.isFile == false) return false;
      
      // Check extension
      final extension = p.extension(file.name).toLowerCase();
      if (!imageExtensions.contains(extension)) return false;
      
      // Skip system files
      final fileName = p.basename(file.name);
      if (fileName.startsWith('.') || fileName.startsWith('__MACOSX')) {
        return false;
      }
      
      return true;
    }).toList();
  }

  bool _shouldRetryExtraction(Exception error) {
    if (error is ArchiveException) {
      // Don't retry format or structure errors
      return ![
        ArchiveErrorType.unsupportedFormat,
        ArchiveErrorType.invalidStructure,
        ArchiveErrorType.passwordRequired,
      ].contains(error.type);
    }
    
    return ExponentialBackoffRetry.fileShouldRetry(error);
  }

  /// Get archive metadata without full extraction
  Future<Map<String, dynamic>> getMetadata() async {
    final archive = await _getValidatedArchive();
    final imageFiles = _filterImageFiles(archive.files);
    
    return {
      'format': _format?.name,
      'totalFiles': archive.files.length,
      'imageFiles': imageFiles.length,
      'fileSize': bytes?.length,
      'filePath': path,
      'hasPassword': false, // TODO: Implement password detection
    };
  }

  /// Dispose cached data
  void dispose() {
    _archive = null;
    _cachedPages = null;
  }
}
