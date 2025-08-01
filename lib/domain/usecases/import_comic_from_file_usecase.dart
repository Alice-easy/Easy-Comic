import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/domain/services/archive_service.dart';
import 'package:path/path.dart' as p;
import 'dart:io';

class ImportComicFromFileUsecase {
  final ComicRepository _comicRepository;
  final ArchiveService _archiveService;

  ImportComicFromFileUsecase(this._comicRepository, this._archiveService);

  Future<Either<Failure, Comic>> call(String filePath, int bookshelfId) async {
    try {
      // Validate file exists
      final file = File(filePath);
      if (!await file.exists()) {
        return Left(CacheFailure('File does not exist: $filePath'));
      }

      // Validate file format
      final fileName = p.basename(filePath);
      final extension = p.extension(fileName).toLowerCase();
      if (!_isSupportedFormat(extension)) {
        return Left(CacheFailure('Unsupported file format: $extension'));
      }

      // Extract metadata from archive
      int pageCount = 0;
      String coverPath = '';
      
      try {
        // Get page count efficiently
        pageCount = await _archiveService.getPageCount(filePath);
        
        // Extract cover image
        final coverData = await _archiveService.extractCoverImage(filePath);
        if (coverData != null) {
          // Save cover to cache directory
          coverPath = await _saveCoverImage(fileName, coverData);
        }
      } catch (e) {
        // Log error but continue with basic metadata
        print('Warning: Failed to extract archive metadata: $e');
      }

      // Create comic object with extracted metadata
      final comic = Comic(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        title: _extractTitle(fileName),
        author: 'Unknown Author', // Default author
        path: filePath,
        filePath: filePath,
        fileName: fileName,
        coverPath: coverPath,
        pageCount: pageCount,
        addTime: DateTime.now(),
        lastReadTime: DateTime.now(),
        progress: 0,
        bookshelfId: bookshelfId,
        isFavorite: false,
        tags: _extractTags(fileName),
        metadata: {
          'fileSize': await file.length(),
          'originalFormat': extension,
          'importDate': DateTime.now().toIso8601String(),
        },
        // Additional required properties
        addedAt: DateTime.now(),
        lastReadAt: DateTime.now(),
        currentPage: 0,
        totalPages: pageCount,
        pages: const [],
      );

      // Add comic to repository
      final result = await _comicRepository.addComic(comic);
      return result.fold(
        (failure) => Left(failure),
        (_) => Right(comic),
      );
    } catch (e) {
      return Left(CacheFailure('Failed to import comic from file: ${e.toString()}'));
    }
  }

  bool _isSupportedFormat(String extension) {
    const supportedFormats = {'.zip', '.cbz', '.rar', '.cbr'};
    return supportedFormats.contains(extension);
  }

  String _extractTitle(String fileName) {
    // Remove extension and clean up filename
    String title = p.basenameWithoutExtension(fileName);
    
    // Replace common separators with spaces
    title = title.replaceAll(RegExp(r'[._-]'), ' ');
    
    // Clean up multiple spaces
    title = title.replaceAll(RegExp(r'\s+'), ' ').trim();
    
    // Capitalize words
    return title.split(' ').map((word) {
      if (word.isEmpty) return word;
      return word[0].toUpperCase() + word.substring(1).toLowerCase();
    }).join(' ');
  }

  List<String> _extractTags(String fileName) {
    final tags = <String>[];
    
    // Extract format tag
    final extension = p.extension(fileName).toLowerCase();
    if (extension == '.cbz' || extension == '.zip') {
      tags.add('CBZ');
    } else if (extension == '.cbr' || extension == '.rar') {
      tags.add('CBR');
    }
    
    // Add import tag
    tags.add('Imported');
    
    return tags;
  }

  Future<String> _saveCoverImage(String fileName, List<int> coverData) async {
    try {
      // Create covers directory if it doesn't exist
      final coversDir = Directory('covers');
      if (!await coversDir.exists()) {
        await coversDir.create(recursive: true);
      }
      
      // Generate unique cover filename
      final baseName = p.basenameWithoutExtension(fileName);
      final coverFileName = '${baseName}_cover_${DateTime.now().millisecondsSinceEpoch}.jpg';
      final coverFile = File(p.join(coversDir.path, coverFileName));
      
      // Write cover data
      await coverFile.writeAsBytes(coverData);
      
      return coverFile.path;
    } catch (e) {
      print('Warning: Failed to save cover image: $e');
      return '';
    }
  }
}