import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/services/file_processing_service.dart';
import 'package:easy_comic/data/datasources/local/manga_local_data_source.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';
import 'package:uuid/uuid.dart';
import 'package:path/path.dart' as p;

import 'package:easy_comic/core/services/file_processing_service.dart';
import 'package:path/path.dart' as p;

class MangaRepositoryImpl implements MangaRepository {
  final MangaLocalDataSource localDataSource;
  final FileProcessingService fileProcessingService;

  MangaRepositoryImpl({
    required this.localDataSource,
    required this.fileProcessingService,
  });

  @override
  Future<Either<Failure, List<Manga>>> getLibraryMangas() async {
    try {
      final mangaModels = await localDataSource.getAllMangas();
      return Right(mangaModels);
    } catch (e) {
      return Left(CacheFailure("Failed to get library mangas"));
    }
  }

  @override
  Future<Either<Failure, Manga>> importMangaFromFile(File file) async {
    try {
      // 1. Process file to extract images and metadata
      final processedData = await fileProcessingService.processMangaFile(file);

      // 2. Create manga object
      final now = DateTime.now();
      final manga = Manga(
        id: Uuid().v4(),
        title: p.basenameWithoutExtension(file.path),
        filePath: file.path,
        coverPath: processedData.coverImagePath,
        totalPages: processedData.pageCount,
        currentPage: 0,
        lastRead: now,
        dateAdded: now,
        tags: [],
        isFavorite: false,
      );

      // 3. Save to local database
      await localDataSource.addManga(manga);

      return Right(manga);
    } catch (e) {
      return Left(DatabaseFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Manga>> getMangaDetails(String mangaId) async {
    try {
      final manga = await localDataSource.getMangaById(mangaId);
      if (manga == null) {
        return Left(CacheFailure("Manga not found"));
      }
      final pagePaths = await fileProcessingService.getPageImagePaths(manga.filePath);
      final mangaWithPages = manga.copyWith(pagePaths: pagePaths);
      return Right(mangaWithPages);
    } catch (e) {
      return Left(DatabaseFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, void>> deleteManga(String mangaId) async {
    try {
      await localDataSource.deleteManga(mangaId);
      // Note: This does not delete the actual comic file from storage.
      // That would require another service and more complex logic.
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure("Failed to delete manga"));
    }
  }

  @override
  Future<Either<Failure, void>> toggleMangaFavoriteStatus(String mangaId) async {
    try {
      final manga = await localDataSource.getMangaById(mangaId);
      if (manga == null) {
        return Left(CacheFailure("Manga not found"));
      }
      final updatedManga = manga.copyWith(isFavorite: !manga.isFavorite);
      await localDataSource.updateManga(updatedManga);
      return const Right(null);
    } catch (e) {
      return Left(CacheFailure("Failed to toggle favorite status"));
    }
  }
}