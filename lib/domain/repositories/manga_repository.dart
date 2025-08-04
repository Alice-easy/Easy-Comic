import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/manga.dart';

abstract class MangaRepository {
  Future<Either<Failure, List<Manga>>> getLibraryMangas();
  Future<Either<Failure, Manga>> importMangaFromFile(File file);
  Future<Either<Failure, Manga>> getMangaDetails(String mangaId);
  Future<Either<Failure, void>> deleteManga(String mangaId);
  Future<Either<Failure, void>> toggleMangaFavoriteStatus(String mangaId);
}