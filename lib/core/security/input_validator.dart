import 'dart:io';
import 'package:path/path.dart' as path;

/// Exception for input validation failures
class ValidationException implements Exception {
  final String message;
  final String? field;
  final ValidationErrorType type;
  
  const ValidationException(
    this.message, {
    this.field,
    this.type = ValidationErrorType.general,
  });
  
  @override
  String toString() => 'ValidationException: $message${field != null ? ' (field: $field)' : ''}';
}

/// Types of validation errors
enum ValidationErrorType {
  general,
  pathTraversal,
  invalidExtension,
  fileNotFound,
  permissionDenied,
  fileTooLarge,
  invalidFormat,
}

/// Comprehensive input validation service with security focus
class InputValidator {
  // Security configuration
  static const int maxFileSize = 500 * 1024 * 1024; // 500MB
  static const int maxPathLength = 260; // Windows MAX_PATH limit
  static const List<String> allowedComicExtensions = [
    '.cbz', '.cbr', '.zip', '.rar', '.7z',
    '.pdf', '.epub', '.mobi',
  ];
  static const List<String> allowedImageExtensions = [
    '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp',
  ];
  
  // Dangerous path patterns
  static final List<RegExp> _dangerousPatterns = [
    RegExp(r'\.\.[\\/]'), // Directory traversal
    RegExp(r'^[\\/]'), // Absolute paths
    RegExp(r'[\\/]\.\.[\\/]'), // Traversal in middle
    RegExp(r'[\\/]\.\.$'), // Traversal at end
    RegExp(r'^\.\.[\\\/]'), // Traversal at start
    RegExp(r'[<>:"|?*]'), // Windows invalid characters
    RegExp(r'[\x00-\x1f]'), // Control characters
  ];
  
  // Safe base directories
  final Set<String> _allowedBasePaths = <String>{};
  
  InputValidator() {
    _initializeAllowedPaths();
  }
  
  Future<void> _initializeAllowedPaths() async {
    try {
      // Add application documents directory
      final documentsDir = Directory.systemTemp.parent;
      _allowedBasePaths.add(path.canonicalize(documentsDir.path));
      
      // Add temporary directory
      final tempDir = Directory.systemTemp;
      _allowedBasePaths.add(path.canonicalize(tempDir.path));
      
      // Add current directory
      final currentDir = Directory.current;
      _allowedBasePaths.add(path.canonicalize(currentDir.path));
      
    } catch (e) {
      // If we can't determine safe paths, use very restrictive validation
    }
  }
  
  /// Validate file path for comic files with security checks
  Future<String> validateComicFilePath(String filePath) async {
    return _validateFilePath(
      filePath,
      allowedExtensions: allowedComicExtensions,
      field: 'comicFilePath',
    );
  }
  
  /// Validate file path for image files with security checks
  Future<String> validateImageFilePath(String filePath) async {
    return _validateFilePath(
      filePath,
      allowedExtensions: allowedImageExtensions,
      field: 'imageFilePath',
    );
  }
  
  /// Core file path validation with comprehensive security checks
  Future<String> _validateFilePath(
    String filePath, {
    required List<String> allowedExtensions,
    String? field,
  }) async {
    // Basic null/empty checks
    if (filePath.isEmpty) {
      throw ValidationException(
        'File path cannot be empty',
        field: field,
        type: ValidationErrorType.general,
      );
    }
    
    // Length validation
    if (filePath.length > maxPathLength) {
      throw ValidationException(
        'File path too long (max $maxPathLength characters)',
        field: field,
        type: ValidationErrorType.general,
      );
    }
    
    // Directory traversal attack prevention
    _checkForPathTraversal(filePath, field);
    
    // Normalize and canonicalize path
    String normalizedPath;
    try {
      normalizedPath = path.normalize(filePath);
      // Additional canonicalization if file exists
      if (await File(normalizedPath).exists()) {
        normalizedPath = path.canonicalize(normalizedPath);
      }
    } catch (e) {
      throw ValidationException(
        'Invalid file path format: $e',
        field: field,
        type: ValidationErrorType.invalidFormat,
      );
    }
    
    // Verify path is within allowed directories
    _checkAllowedBasePath(normalizedPath, field);
    
    // Extension validation
    _validateFileExtension(normalizedPath, allowedExtensions, field);
    
    // File existence and accessibility
    await _validateFileAccess(normalizedPath, field);
    
    return normalizedPath;
  }
  
  void _checkForPathTraversal(String filePath, String? field) {
    for (final pattern in _dangerousPatterns) {
      if (pattern.hasMatch(filePath)) {
        throw ValidationException(
          'Path contains dangerous characters or traversal patterns',
          field: field,
          type: ValidationErrorType.pathTraversal,
        );
      }
    }
    
    // Additional checks for encoded traversal attempts
    final decodedPath = Uri.decodeComponent(filePath);
    if (decodedPath != filePath) {
      // Check decoded version as well
      for (final pattern in _dangerousPatterns) {
        if (pattern.hasMatch(decodedPath)) {
          throw ValidationException(
            'Path contains encoded traversal patterns',
            field: field,
            type: ValidationErrorType.pathTraversal,
          );
        }
      }
    }
  }
  
  void _checkAllowedBasePath(String normalizedPath, String? field) {
    if (_allowedBasePaths.isEmpty) {
      // If no allowed paths set, skip this check (fallback mode)
      return;
    }
    
    bool isAllowed = false;
    for (final allowedPath in _allowedBasePaths) {
      if (normalizedPath.startsWith(allowedPath)) {
        isAllowed = true;
        break;
      }
    }
    
    if (!isAllowed) {
      throw ValidationException(
        'File path is outside allowed directories',
        field: field,
        type: ValidationErrorType.pathTraversal,
      );
    }
  }
  
  void _validateFileExtension(
    String filePath,
    List<String> allowedExtensions, 
    String? field,
  ) {
    final extension = path.extension(filePath).toLowerCase();
    
    if (!allowedExtensions.contains(extension)) {
      throw ValidationException(
        'File extension $extension is not allowed. Allowed: ${allowedExtensions.join(', ')}',
        field: field,
        type: ValidationErrorType.invalidExtension,
      );
    }
  }
  
  Future<void> _validateFileAccess(String filePath, String? field) async {
    final file = File(filePath);
    
    // Check if file exists
    if (!await file.exists()) {
      throw ValidationException(
        'File does not exist: $filePath',
        field: field,
        type: ValidationErrorType.fileNotFound,
      );
    }
    
    // Check file size
    try {
      final stat = await file.stat();
      if (stat.size > maxFileSize) {
        throw ValidationException(
          'File too large: ${stat.size} bytes (max: $maxFileSize)',
          field: field,
          type: ValidationErrorType.fileTooLarge,
        );
      }
    } catch (e) {
      throw ValidationException(
        'Cannot access file: $e',
        field: field,
        type: ValidationErrorType.permissionDenied,
      );
    }
  }
  
  /// Validate cache key to prevent cache poisoning
  String validateCacheKey(String key) {
    if (key.isEmpty) {
      throw ValidationException('Cache key cannot be empty');
    }
    
    if (key.length > 255) {
      throw ValidationException('Cache key too long (max 255 characters)');
    }
    
    // Check for dangerous characters
    if (RegExp(r'[<>:"/\\|?*\x00-\x1f]').hasMatch(key)) {
      throw ValidationException('Cache key contains invalid characters');
    }
    
    return key;
  }
  
  /// Validate page index to prevent array bounds issues
  int validatePageIndex(int pageIndex, int totalPages, {String? field}) {
    if (pageIndex < 0) {
      throw ValidationException(
        'Page index cannot be negative',
        field: field,
      );
    }
    
    if (pageIndex >= totalPages) {
      throw ValidationException(
        'Page index $pageIndex exceeds total pages $totalPages',
        field: field,
      );
    }
    
    return pageIndex;
  }
  
  /// Validate URL for network resources
  Uri validateUrl(String url, {String? field}) {
    if (url.isEmpty) {
      throw ValidationException(
        'URL cannot be empty',
        field: field,
      );
    }
    
    Uri uri;
    try {
      uri = Uri.parse(url);
    } catch (e) {
      throw ValidationException(
        'Invalid URL format: $e',
        field: field,
        type: ValidationErrorType.invalidFormat,
      );
    }
    
    // Only allow http and https schemes
    if (!['http', 'https'].contains(uri.scheme)) {
      throw ValidationException(
        'Only HTTP and HTTPS URLs are allowed',
        field: field,
        type: ValidationErrorType.invalidFormat,
      );
    }
    
    return uri;
  }
  
  /// Add additional allowed base path
  void addAllowedBasePath(String basePath) {
    try {
      final canonicalPath = path.canonicalize(basePath);
      _allowedBasePaths.add(canonicalPath);
    } catch (e) {
      throw ValidationException('Invalid base path: $e');
    }
  }
  
  /// Get current allowed base paths
  Set<String> get allowedBasePaths => Set.unmodifiable(_allowedBasePaths);
}