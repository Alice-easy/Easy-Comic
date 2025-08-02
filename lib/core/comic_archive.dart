import 'dart:io';
import 'dart:typed_data';
import 'dart:async';
import 'dart:developer' as developer;
import 'package:archive/archive.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart' as p;
import '../domain/entities/comic_page.dart';
import 'error/retry_mechanism.dart';
import 'security/input_validator.dart';
import 'services/logging_service.dart';
import 'services/global_error_handler.dart';

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

/// Archive extraction progress information with enhanced details
class ExtractionProgress {
  final int currentFile;
  final int totalFiles;
  final String currentFileName;
  final double percentage;
  final int bytesExtracted;
  final int totalBytes;
  final Duration? estimatedTimeRemaining;
  final double? extractionSpeed; // bytes per second
  final String? currentOperation; // e.g., "Validating", "Extracting", "Processing"
  final Map<String, dynamic>? additionalInfo;
  
  const ExtractionProgress({
    required this.currentFile,
    required this.totalFiles,
    required this.currentFileName,
    required this.percentage,
    required this.bytesExtracted,
    required this.totalBytes,
    this.estimatedTimeRemaining,
    this.extractionSpeed,
    this.currentOperation,
    this.additionalInfo,
  });
  
  /// Create progress for initialization phase  
  ExtractionProgress.initializing({
    required this.totalFiles,
    this.additionalInfo,
  }) : currentFile = 0,
       currentFileName = 'Initializing extraction...',
       percentage = 0.0,
       bytesExtracted = 0,
       totalBytes = 0,
       estimatedTimeRemaining = null,
       extractionSpeed = null,
       currentOperation = 'Initializing';
       
  /// Create progress for validation phase
  ExtractionProgress.validating({
    required this.totalFiles,
    this.additionalInfo,
  }) : currentFile = 0,
       currentFileName = 'Validating archive...',
       percentage = 5.0,
       bytesExtracted = 0,
       totalBytes = 0,
       estimatedTimeRemaining = null,
       extractionSpeed = null,
       currentOperation = 'Validating';
  
  /// Create progress for completion
  ExtractionProgress.completed({
    required this.totalFiles,
    required this.totalBytes,
    this.additionalInfo,
  }) : currentFile = totalFiles,
       currentFileName = 'Extraction complete',
       percentage = 100.0,
       bytesExtracted = totalBytes,
       estimatedTimeRemaining = Duration.zero,
       extractionSpeed = null,
       currentOperation = 'Completed';
  
  @override
  String toString() {
    final buffer = StringBuffer('ExtractionProgress(');
    buffer.write('${currentFile}/${totalFiles} files, ');
    buffer.write('${percentage.toStringAsFixed(1)}%, ');
    buffer.write(currentFileName);
    
    if (currentOperation != null) {
      buffer.write(', op: $currentOperation');
    }
    
    if (extractionSpeed != null) {
      final speedKB = (extractionSpeed! / 1024).toStringAsFixed(1);
      buffer.write(', speed: ${speedKB}KB/s');
    }
    
    if (estimatedTimeRemaining != null) {
      buffer.write(', eta: ${estimatedTimeRemaining!.inSeconds}s');
    }
    
    buffer.write(')');
    return buffer.toString();
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
      developer.log('Returning cached pages', name: 'ComicArchive', error: {
        'cachedPageCount': _cachedPages!.length,
      });
      
      // Still report progress for cached pages
      if (progressController != null) {
        progressController.add(ExtractionProgress.completed(
          totalFiles: _cachedPages!.length,
          totalBytes: 0, // We don't track this for cached pages
          additionalInfo: {'cached': true},
        ));
      }
      
      return _cachedPages!;
    }

    // Report initialization progress
    progressController?.add(ExtractionProgress.initializing(
      totalFiles: 0, // Unknown at this stage
      additionalInfo: {
        'archivePath': path,
        'hasBytes': bytes != null,
      },
    ));

    try {
      return await _retry.execute(
        () => _performExtraction(progressController, cancelToken),
        config: RetryConfig.file,
        shouldRetry: (error) => _shouldRetryExtraction(error),
        onRetry: (attempt, error) {
          developer.log('Retrying extraction', name: 'ComicArchive', level: 900, error: {
            'attempt': attempt,
            'error': error.toString(),
          });
          
          // Report retry progress
          progressController?.add(ExtractionProgress(
            currentFile: 0,
            totalFiles: 0,
            currentFileName: 'Retrying extraction (attempt $attempt)...',
            percentage: 0,
            bytesExtracted: 0,
            totalBytes: 0,
            currentOperation: 'Retrying',
            additionalInfo: {'attempt': attempt, 'error': error.toString()},
          ));
        },
      );
    } catch (e, stackTrace) {
      developer.log('Extraction failed after retries', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
      
      // Report failure progress
      progressController?.add(ExtractionProgress(
        currentFile: 0,
        totalFiles: 0,
        currentFileName: 'Extraction failed: ${e.toString()}',
        percentage: 0,
        bytesExtracted: 0,
        totalBytes: 0,
        currentOperation: 'Failed',
        additionalInfo: {'error': e.toString()},
      ));
      
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
    final extractionStartTime = DateTime.now();
    developer.log('Starting archive extraction', name: 'ComicArchive', error: {
      'archivePath': path,
      'hasBytes': bytes != null,
      'bytesLength': bytes?.length,
    });
    
    try {
      // Report validation progress
      progressController?.add(ExtractionProgress.validating(
        totalFiles: 0, // Still unknown
        additionalInfo: {
          'stage': 'validation',
          'archiveFormat': _format?.name,
        },
      ));
      
      // Validate and get archive
      final archive = await _getValidatedArchive();
      developer.log('Archive validated successfully', name: 'ComicArchive', error: {
        'totalFiles': archive.files.length,
        'archiveFormat': _format?.name,
      });
      
      final imageFiles = _filterImageFiles(archive.files);
      developer.log('Image files filtered', name: 'ComicArchive', error: {
        'totalFiles': archive.files.length,
        'imageFiles': imageFiles.length,
      });
      
      if (imageFiles.isEmpty) {
        final errorMsg = 'No valid image files found in archive';
        developer.log(errorMsg, name: 'ComicArchive', level: 1000);
        
        // Log file details for debugging
        final fileDetails = archive.files.map((f) => {
          'name': f.name,
          'isFile': f.isFile,
          'size': f.size,
          'extension': p.extension(f.name),
        }).toList();
        developer.log('Archive file details', name: 'ComicArchive', error: fileDetails);
        
        throw ArchiveException(
          errorMsg,
          type: ArchiveErrorType.noValidImages,
          filePath: path,
          details: 'Found ${archive.files.length} total files, 0 valid images',
        );
      }

      // Sort files naturally (page1, page2, page10)
      imageFiles.sort((a, b) => naturalCompare(a.name, b.name));
      developer.log('Files sorted naturally', name: 'ComicArchive', error: {
        'firstFile': imageFiles.first.name,
        'lastFile': imageFiles.last.name,
      });

      final pages = <ComicPage>[];
      int totalBytes = imageFiles.fold(0, (sum, file) => sum + file.size);
      int processedBytes = 0;
      int validPages = 0;
      int skippedFiles = 0;
      
      // Initialize progress tracking
      final progressStartTime = DateTime.now();
      var lastProgressTime = progressStartTime;
      
      // Report initial extraction progress
      progressController?.add(ExtractionProgress(
        currentFile: 0,
        totalFiles: imageFiles.length,
        currentFileName: 'Starting extraction...',
        percentage: 10.0, // After validation
        bytesExtracted: 0,
        totalBytes: totalBytes,
        currentOperation: 'Extracting',
        additionalInfo: {
          'imageFiles': imageFiles.length,
          'totalUncompressedSize': totalBytes,
        },
      ));

      for (int i = 0; i < imageFiles.length; i++) {
        // Check for cancellation
        if (cancelToken?.isCancelled == true) {
          final cancelMsg = 'Extraction cancelled by user at file ${i + 1}/${imageFiles.length}';
          developer.log(cancelMsg, name: 'ComicArchive', level: 900);
          throw RetryCancelledException(cancelMsg);
        }

        final file = imageFiles[i];
        final currentTime = DateTime.now();
        
        // Calculate extraction speed and ETA
        final elapsedTime = currentTime.difference(progressStartTime);
        final elapsedSeconds = elapsedTime.inMilliseconds / 1000.0;
        double? extractionSpeed;
        Duration? estimatedTimeRemaining;
        
        if (elapsedSeconds > 0 && processedBytes > 0) {
          extractionSpeed = processedBytes / elapsedSeconds; // bytes per second
          
          if (extractionSpeed > 0) {
            final remainingBytes = totalBytes - processedBytes;
            final remainingSeconds = remainingBytes / extractionSpeed;
            estimatedTimeRemaining = Duration(milliseconds: (remainingSeconds * 1000).round());
          }
        }
        
        // Report progress with enhanced timing information
        final basePercentage = 10.0; // After validation
        final extractionPercentage = 80.0; // Extraction phase takes 80% of progress
        final currentPercentage = basePercentage + (extractionPercentage * (i + 1) / imageFiles.length);
        
        final progress = ExtractionProgress(
          currentFile: i + 1,
          totalFiles: imageFiles.length,
          currentFileName: file.name,
          percentage: currentPercentage,
          bytesExtracted: processedBytes,
          totalBytes: totalBytes,
          estimatedTimeRemaining: estimatedTimeRemaining,
          extractionSpeed: extractionSpeed,
          currentOperation: 'Extracting',
          additionalInfo: {
            'validPages': validPages,
            'skippedFiles': skippedFiles,
            'averageFileSize': processedBytes > 0 && validPages > 0 ? (processedBytes / validPages).round() : 0,
          },
        );
        
        progressController?.add(progress);
        lastProgressTime = currentTime;
        
        if (i % 10 == 0) { // Log every 10th file to avoid spam
          developer.log('Extraction progress', name: 'ComicArchive', error: {
            'progress': '${progress.percentage.toStringAsFixed(1)}%',
            'currentFile': file.name,
            'validPages': validPages,
            'skippedFiles': skippedFiles,
            'extractionSpeed': extractionSpeed != null ? '${(extractionSpeed / 1024).toStringAsFixed(1)}KB/s' : null,
            'eta': estimatedTimeRemaining?.inSeconds,
          });
        }

        try {
          // Extract file data with validation
          if (file.content == null) {
            developer.log('Skipping file with null content: ${file.name}', name: 'ComicArchive', level: 900);
            skippedFiles++;
            continue;
          }
          
          final content = file.content as List<int>;
          final imageData = Uint8List.fromList(content);
          
          // Validate image data
          if (imageData.isEmpty) {
            developer.log('Skipping empty file: ${file.name}', name: 'ComicArchive', level: 900);
            skippedFiles++;
            continue;
          }
          
          // Additional image validation - check for common image headers
          if (!_isValidImageData(imageData)) {
            developer.log('Skipping invalid image data: ${file.name}', name: 'ComicArchive', level: 900);
            skippedFiles++;
            continue;
          }

          // Create comic page
          final page = ComicPage(
            pageIndex: validPages, // Use validPages counter instead of i
            imageData: imageData,
            path: file.name,
          );

          pages.add(page);
          processedBytes += file.size;
          validPages++;

        } catch (e, stackTrace) {
          // Enhanced error logging but continue with other files
          final errorMsg = 'Failed to extract ${file.name}: $e';
          developer.log(errorMsg, name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
          
          // Report to global error handler but don't fail
          GlobalErrorHandler.reportError(
            e,
            stackTrace: stackTrace,
            context: 'ComicArchive._performExtraction',
            additionalInfo: {
              'fileName': file.name,
              'fileSize': file.size,
              'fileIndex': i,
              'totalFiles': imageFiles.length,
            },
          );
          
          skippedFiles++;
          continue;
        }
      }

      final extractionDuration = DateTime.now().difference(extractionStartTime);
      
      if (pages.isEmpty) {
        final errorMsg = 'Failed to extract any valid pages from ${imageFiles.length} image files';
        developer.log(errorMsg, name: 'ComicArchive', level: 1000, error: {
          'totalFiles': imageFiles.length,
          'skippedFiles': skippedFiles,
          'extractionDuration': extractionDuration.inMilliseconds,
        });
        
        throw ArchiveException(
          errorMsg,
          type: ArchiveErrorType.noValidImages,
          filePath: path,
          details: 'All ${imageFiles.length} image files were invalid or corrupted',
        );
      }

      // Report completion with final timing information
      final completionTime = DateTime.now();
      final totalExtractionDuration = completionTime.difference(extractionStartTime);
      
      final completionProgress = ExtractionProgress.completed(
        totalFiles: imageFiles.length,
        totalBytes: totalBytes,
        additionalInfo: {
          'validPages': validPages,
          'skippedFiles': skippedFiles,
          'extractionDuration': totalExtractionDuration.inMilliseconds,
          'averagePageSize': validPages > 0 ? (totalBytes / validPages / 1024).toStringAsFixed(1) + 'KB' : '0KB',
          'extractionSpeed': totalExtractionDuration.inMilliseconds > 0 
              ? '${(totalBytes / (totalExtractionDuration.inMilliseconds / 1000) / 1024).toStringAsFixed(1)}KB/s'
              : 'N/A',
        },
      );
      progressController?.add(completionProgress);
      
      developer.log('Archive extraction completed successfully', name: 'ComicArchive', error: {
        'validPages': validPages,
        'skippedFiles': skippedFiles,
        'totalProcessed': imageFiles.length,
        'extractionDuration': '${extractionDuration.inMilliseconds}ms',
        'averagePageSize': '${(totalBytes / validPages / 1024).toStringAsFixed(1)}KB',
      });

      _cachedPages = pages;
      return pages;
      
    } catch (e, stackTrace) {
      final extractionDuration = DateTime.now().difference(extractionStartTime);
      developer.log('Archive extraction failed', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
      
      // Report to global error handler
      GlobalErrorHandler.reportError(
        e,
        stackTrace: stackTrace,
        context: 'ComicArchive._performExtraction',
        additionalInfo: {
          'archivePath': path,
          'hasBytes': bytes != null,
          'extractionDuration': extractionDuration.inMilliseconds,
        },
      );
      
      rethrow;
    }
  }

  Future<Archive> _getValidatedArchive() async {
    if (_archive != null) {
      return _archive!;
    }

    developer.log('Starting archive validation', name: 'ComicArchive', error: {
      'hasPath': path != null,
      'hasBytes': bytes != null,
      'pathExtension': path != null ? p.extension(path!) : null,
    });

    // Detect and validate format with enhanced validation
    _format = _detectArchiveFormat();
    
    if (_format == null) {
      final errorMsg = 'Unsupported archive format';
      final extension = path != null ? p.extension(path!) : 'unknown';
      developer.log(errorMsg, name: 'ComicArchive', level: 1000, error: {
        'detectedExtension': extension,
        'supportedFormats': ArchiveFormat.values.map((f) => f.name).toList(),
      });
      
      throw ArchiveException(
        errorMsg,
        type: ArchiveErrorType.unsupportedFormat,
        filePath: path,
        details: 'File extension: $extension. Supported formats: CBZ, ZIP',
      );
    }

    // Get file bytes with validation
    Uint8List fileBytes;
    if (bytes != null) {
      fileBytes = bytes!;
      developer.log('Using provided bytes', name: 'ComicArchive', error: {
        'byteLength': fileBytes.length,
      });
    } else if (path != null) {
      try {
        final validatedPath = await _validator.validateComicFilePath(path!);
        final file = File(validatedPath);
        
        // Check if file exists
        if (!await file.exists()) {
          throw ArchiveException(
            'Archive file does not exist',
            type: ArchiveErrorType.extractionFailed,
            filePath: path,
            details: 'File path: $validatedPath',
          );
        }
        
        // Get file stats for validation
        final stat = await file.stat();
        developer.log('Reading archive file', name: 'ComicArchive', error: {
          'filePath': validatedPath,
          'fileSize': stat.size,
          'modified': stat.modified.toIso8601String(),
        });
        
        fileBytes = await file.readAsBytes();
        
      } catch (e, stackTrace) {
        developer.log('Failed to read archive file', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
        
        if (e is ArchiveException) rethrow;
        
        throw ArchiveException(
          'Failed to read archive file: $e',
          type: ArchiveErrorType.extractionFailed,
          filePath: path,
          details: 'Error type: ${e.runtimeType}',
        );
      }
    } else {
      throw ArchiveException(
        'No file data available',
        type: ArchiveErrorType.extractionFailed,
        details: 'Neither path nor bytes provided',
      );
    }

    // Enhanced file size validation with detailed reporting
    const maxSizeBytes = 500 * 1024 * 1024; // 500MB limit
    if (fileBytes.length > maxSizeBytes) {
      final sizeMB = (fileBytes.length / (1024 * 1024)).toStringAsFixed(1);
      final maxSizeMB = (maxSizeBytes / (1024 * 1024)).toStringAsFixed(0);
      
      developer.log('Archive file too large', name: 'ComicArchive', level: 1000, error: {
        'fileSize': fileBytes.length,
        'sizeMB': sizeMB,
        'maxSizeMB': maxSizeMB,
      });
      
      throw ArchiveException(
        'Archive file too large: ${sizeMB}MB (max: ${maxSizeMB}MB)',
        type: ArchiveErrorType.fileTooLarge,
        filePath: path,
        details: 'Consider splitting the archive or using a smaller file',
      );
    }
    
    // Validate minimum file size (avoid empty or corrupted files)
    if (fileBytes.length < 100) { // Less than 100 bytes is suspicious
      developer.log('Archive file too small', name: 'ComicArchive', level: 1000, error: {
        'fileSize': fileBytes.length,
      });
      
      throw ArchiveException(
        'Archive file is too small: ${fileBytes.length} bytes',
        type: ArchiveErrorType.corruption,
        filePath: path,
        details: 'File may be empty or corrupted',
      );
    }
    
    // Cross-validate format detection with magic bytes
    final magicFormat = _detectFromMagicBytes();
    final expectedExtensions = {
      ArchiveFormat.cbz: ['.cbz', '.zip'],
      ArchiveFormat.zip: ['.cbz', '.zip'],
      ArchiveFormat.cbr: ['.cbr', '.rar'],
      ArchiveFormat.rar: ['.cbr', '.rar'],
    };
    
    if (magicFormat.isNotEmpty) {
      final pathExtension = path != null ? p.extension(path!).toLowerCase() : '';
      final formatExtensions = expectedExtensions[_format] ?? [];
      
      // Check for format mismatch
      if (pathExtension.isNotEmpty && !formatExtensions.contains(pathExtension) && magicFormat != pathExtension) {
        developer.log('Format mismatch detected', name: 'ComicArchive', level: 900, error: {
          'pathExtension': pathExtension,
          'magicFormat': magicFormat,
          'detectedFormat': _format?.name,
        });
        
        // Not a fatal error, but log for debugging
        GlobalErrorHandler.addLog('Archive format mismatch: path=$pathExtension, magic=$magicFormat');
      }
    }

    // Decode archive with format-specific handling and comprehensive error recovery
    try {
      developer.log('Decoding archive', name: 'ComicArchive', error: {
        'format': _format?.name,
        'fileSize': fileBytes.length,
      });
      
      _archive = await _decodeArchive(fileBytes, _format!);
      
      // Validate decoded archive
      if (_archive!.files.isEmpty) {
        throw ArchiveException(
          'Archive contains no files',
          type: ArchiveErrorType.invalidStructure,
          filePath: path,
          details: 'Archive decoded successfully but contains 0 files',
        );
      }
      
      developer.log('Archive validation completed successfully', name: 'ComicArchive', error: {
        'format': _format?.name,
        'totalFiles': _archive!.files.length,
        'fileSize': fileBytes.length,
      });
      
      return _archive!;
      
    } catch (e, stackTrace) {
      developer.log('Archive decoding failed during validation', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
      
      if (e is ArchiveException) {
        // Add more context to existing archive exceptions
        throw ArchiveException(
          e.message,
          type: e.type,
          filePath: e.filePath ?? path,
          details: '${e.details ?? ""} | Validation context: format=${_format?.name}, size=${fileBytes.length}',
        );
      }
      
      throw ArchiveException(
        'Failed to decode archive during validation: $e',
        type: ArchiveErrorType.corruption,
        filePath: path,
        details: 'Format: ${_format?.name}, Size: ${fileBytes.length} bytes, Error: ${e.runtimeType}',
      );
    }
  }

  ArchiveFormat? _detectArchiveFormat() {
    String extension;
    
    if (path != null) {
      extension = p.extension(path!).toLowerCase();
      developer.log('Detecting format from path extension', name: 'ComicArchive', error: {
        'path': path,
        'extension': extension,
      });
    } else {
      // Try to detect from magic bytes if no path
      extension = _detectFromMagicBytes();
      developer.log('Detecting format from magic bytes', name: 'ComicArchive', error: {
        'detectedExtension': extension,
        'hasBytes': bytes != null,
        'byteLength': bytes?.length,
      });
    }

    final format = switch (extension) {
      '.cbz' || '.zip' => ArchiveFormat.cbz,
      '.cbr' || '.rar' => ArchiveFormat.cbr,
      '.7z' => ArchiveFormat.sevenZ,
      '.pdf' => ArchiveFormat.pdf,
      '.epub' => ArchiveFormat.epub,
      _ => null,
    };
    
    developer.log('Format detection result', name: 'ComicArchive', error: {
      'extension': extension,
      'detectedFormat': format?.name,
      'isSupported': format != null,
    });
    
    return format;
  }

  String _detectFromMagicBytes() {
    if (bytes == null || bytes!.length < 4) {
      developer.log('Insufficient bytes for magic detection', name: 'ComicArchive', level: 900, error: {
        'hasBytes': bytes != null,
        'length': bytes?.length ?? 0,
      });
      return '';
    }
    
    final header = bytes!.take(8).toList(); // Take more bytes for better detection
    final headerHex = header.map((b) => b.toRadixString(16).padLeft(2, '0')).join(' ');
    
    developer.log('Detecting format from magic bytes', name: 'ComicArchive', error: {
      'headerHex': headerHex,
      'totalBytes': bytes!.length,
    });
    
    // ZIP magic bytes: PK (0x50 0x4B)
    if (header.length >= 2 && header[0] == 0x50 && header[1] == 0x4B) {
      developer.log('ZIP format detected', name: 'ComicArchive');
      return '.zip';
    }
    
    // RAR magic bytes: Rar! (0x52 0x61 0x72 0x21)
    if (header.length >= 4 && header[0] == 0x52 && header[1] == 0x61 && 
        header[2] == 0x72 && header[3] == 0x21) {
      developer.log('RAR format detected', name: 'ComicArchive');
      return '.rar';
    }
    
    // PDF magic bytes: %PDF
    if (header.length >= 4 && header[0] == 0x25 && header[1] == 0x50 && 
        header[2] == 0x44 && header[3] == 0x46) {
      developer.log('PDF format detected', name: 'ComicArchive');
      return '.pdf';
    }
    
    // 7-Zip magic bytes: 7z¼¯'\x1C
    if (header.length >= 6 && header[0] == 0x37 && header[1] == 0x7A && 
        header[2] == 0xBC && header[3] == 0xAF && header[4] == 0x27 && header[5] == 0x1C) {
      developer.log('7-Zip format detected', name: 'ComicArchive');
      return '.7z';
    }
    
    developer.log('Unknown format - magic bytes not recognized', name: 'ComicArchive', level: 900, error: {
      'headerHex': headerHex,
    });
    return '';
  }

  Future<Archive> _decodeArchive(Uint8List fileBytes, ArchiveFormat format) async {
    developer.log('Decoding archive', name: 'ComicArchive', error: {
      'format': format.name,
      'fileSize': fileBytes.length,
    });
    
    try {
      switch (format) {
        case ArchiveFormat.cbz:
        case ArchiveFormat.zip:
          final archive = ZipDecoder().decodeBytes(fileBytes);
          developer.log('ZIP decoding successful', name: 'ComicArchive', error: {
            'totalFiles': archive.files.length,
          });
          return archive;
          
        case ArchiveFormat.cbr:
        case ArchiveFormat.rar:
          // RAR format requires external library or platform-specific implementation
          final errorMsg = 'RAR format not yet supported - requires platform-specific decoder';
          developer.log(errorMsg, name: 'ComicArchive', level: 1000);
          throw ArchiveException(
            errorMsg,
            type: ArchiveErrorType.unsupportedFormat,
            filePath: path,
            details: 'Consider converting to CBZ format. File size: ${fileBytes.length} bytes',
          );
          
        case ArchiveFormat.sevenZ:
          final errorMsg = '7-Zip format not yet supported';
          developer.log(errorMsg, name: 'ComicArchive', level: 1000);
          throw ArchiveException(
            errorMsg,
            type: ArchiveErrorType.unsupportedFormat,
            filePath: path,
            details: 'File size: ${fileBytes.length} bytes',
          );
          
        case ArchiveFormat.pdf:
        case ArchiveFormat.epub:
          final errorMsg = 'PDF/EPUB formats require specialized handling';
          developer.log(errorMsg, name: 'ComicArchive', level: 1000);
          throw ArchiveException(
            errorMsg,
            type: ArchiveErrorType.unsupportedFormat,
            filePath: path,
            details: 'File size: ${fileBytes.length} bytes',
          );
      }
    } catch (e, stackTrace) {
      developer.log('Archive decoding failed', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
      
      // Enhanced error reporting for decoding failures
      if (e is ArchiveException) rethrow;
      
      // Check for common ZIP corruption patterns
      if (format == ArchiveFormat.cbz || format == ArchiveFormat.zip) {
        final errorString = e.toString().toLowerCase();
        if (errorString.contains('invalid') || errorString.contains('corrupt')) {
          throw ArchiveException(
            'Archive file appears to be corrupted',
            type: ArchiveErrorType.corruption,
            filePath: path,
            details: 'ZIP decoding error: $e. File size: ${fileBytes.length} bytes',
          );
        }
      }
      
      throw ArchiveException(
        'Failed to decode ${format.name} archive: $e',
        type: ArchiveErrorType.extractionFailed,
        filePath: path,
        details: 'File size: ${fileBytes.length} bytes, Error: ${e.runtimeType}',
      );
    }
  }

  List<ArchiveFile> _filterImageFiles(List<ArchiveFile> files) {
    const imageExtensions = {'.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.jfif', '.tiff', '.tif'};
    const systemPrefixes = {'.', '__MACOSX', 'Thumbs.db', '.DS_Store'};
    const systemDirectories = {'__MACOSX', '.git', '.svn', 'System Volume Information'};
    
    developer.log('Filtering image files', name: 'ComicArchive', error: {
      'totalFiles': files.length,
      'supportedExtensions': imageExtensions.toList(),
    });
    
    final imageFiles = <ArchiveFile>[];
    int skippedDirectories = 0;
    int skippedExtensions = 0;
    int skippedSystemFiles = 0;
    int validImages = 0;
    
    for (final file in files) {
      // Skip directories
      if (file.isFile == false) {
        skippedDirectories++;
        continue;
      }
      
      final fileName = p.basename(file.name);
      final extension = p.extension(file.name).toLowerCase();
      final directory = p.dirname(file.name);
      
      // Skip system files and directories
      if (systemPrefixes.any((prefix) => fileName.startsWith(prefix)) ||
          systemDirectories.any((sysDir) => directory.contains(sysDir))) {
        skippedSystemFiles++;
        continue;
      }
      
      // Check extension
      if (!imageExtensions.contains(extension)) {
        skippedExtensions++;
        continue;
      }
      
      // Additional validation - check file size
      if (file.size <= 0) {
        developer.log('Skipping zero-size file: ${file.name}', name: 'ComicArchive', level: 900);
        skippedSystemFiles++;
        continue;
      }
      
      // Skip very small files (likely thumbnails or corrupted)
      if (file.size < 1024) { // Less than 1KB
        developer.log('Skipping very small file: ${file.name} (${file.size} bytes)', name: 'ComicArchive', level: 900);
        skippedSystemFiles++;
        continue;
      }
      
      imageFiles.add(file);
      validImages++;
    }
    
    developer.log('Image filtering completed', name: 'ComicArchive', error: {
      'validImages': validImages,
      'skippedDirectories': skippedDirectories,
      'skippedExtensions': skippedExtensions,
      'skippedSystemFiles': skippedSystemFiles,
      'totalProcessed': files.length,
    });
    
    return imageFiles;
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
    developer.log('Getting archive metadata', name: 'ComicArchive');
    
    try {
      final archive = await _getValidatedArchive();
      final imageFiles = _filterImageFiles(archive.files);
      
      // Calculate total uncompressed size
      final totalUncompressedSize = imageFiles.fold<int>(0, (sum, file) => sum + file.size);
      
      // Analyze file types
      final fileExtensions = <String, int>{};
      for (final file in imageFiles) {
        final ext = p.extension(file.name).toLowerCase();
        fileExtensions[ext] = (fileExtensions[ext] ?? 0) + 1;
      }
      
      final metadata = {
        'format': _format?.name,
        'totalFiles': archive.files.length,
        'imageFiles': imageFiles.length,
        'fileSize': bytes?.length,
        'filePath': path,
        'hasPassword': false, // TODO: Implement password detection
        'totalUncompressedSize': totalUncompressedSize,
        'fileExtensions': fileExtensions,
        'averageFileSize': imageFiles.isNotEmpty ? (totalUncompressedSize / imageFiles.length).round() : 0,
        'compressionRatio': bytes != null && totalUncompressedSize > 0 
            ? (bytes!.length / totalUncompressedSize * 100).toStringAsFixed(1) + '%'
            : 'unknown',
        'lastModified': path != null ? await _getFileModificationTime(path!) : null,
      };
      
      developer.log('Archive metadata extracted', name: 'ComicArchive', error: metadata);
      return metadata;
      
    } catch (e, stackTrace) {
      developer.log('Failed to get archive metadata', name: 'ComicArchive', level: 1000, error: e, stackTrace: stackTrace);
      
      // Return basic metadata even if archive processing fails
      return {
        'format': _format?.name ?? 'unknown',
        'totalFiles': 0,
        'imageFiles': 0,
        'fileSize': bytes?.length,
        'filePath': path,
        'hasPassword': false,
        'error': e.toString(),
      };
    }
  }
  
  /// Get file modification time
  Future<String?> _getFileModificationTime(String filePath) async {
    try {
      final file = File(filePath);
      if (await file.exists()) {
        final stat = await file.stat();
        return stat.modified.toIso8601String();
      }
    } catch (e) {
      developer.log('Failed to get file modification time', name: 'ComicArchive', level: 900, error: e);
    }
    return null;
  }

  /// Validate if data contains valid image headers
  bool _isValidImageData(Uint8List data) {
    if (data.length < 8) return false;
    
    // JPEG: FF D8 FF
    if (data[0] == 0xFF && data[1] == 0xD8 && data[2] == 0xFF) {
      return true;
    }
    
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    if (data.length >= 8 &&
        data[0] == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47 &&
        data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
      return true;
    }
    
    // GIF: GIF87a or GIF89a
    if (data.length >= 6 &&
        data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46 &&
        data[3] == 0x38 && (data[4] == 0x37 || data[4] == 0x38) && data[5] == 0x61) {
      return true;
    }
    
    // BMP: BM
    if (data[0] == 0x42 && data[1] == 0x4D) {
      return true;
    }
    
    // WebP: RIFF...WEBP
    if (data.length >= 12 &&
        data[0] == 0x52 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x46 &&
        data[8] == 0x57 && data[9] == 0x45 && data[10] == 0x42 && data[11] == 0x50) {
      return true;
    }
    
    // TIFF: II*\0 or MM\0*
    if (data.length >= 4 &&
        ((data[0] == 0x49 && data[1] == 0x49 && data[2] == 0x2A && data[3] == 0x00) ||
         (data[0] == 0x4D && data[1] == 0x4D && data[2] == 0x00 && data[3] == 0x2A))) {
      return true;
    }
    
    return false;
  }
  
  /// Dispose cached data
  void dispose() {
    developer.log('Disposing ComicArchive', name: 'ComicArchive', error: {
      'hadArchive': _archive != null,
      'hadCachedPages': _cachedPages != null,
      'cachedPageCount': _cachedPages?.length ?? 0,
    });
    
    _archive = null;
    _cachedPages = null;
  }
}
