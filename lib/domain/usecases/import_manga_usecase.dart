import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/repositories/manga_repository.dart';
import 'package:equatable/equatable.dart';

class ImportMangaUseCase implements UseCase<Manga, ImportMangaParams> {
  final MangaRepository repository;

  ImportMangaUseCase(this.repository);

  @override
  Future<Either<Failure, Manga>> call(ImportMangaParams params) async {
    return await repository.importMangaFromFile(params.file);
  }
}

class ImportMangaParams extends Equatable {
  final File file;

  const ImportMangaParams({required this.file});

  @override
  List<Object> get props => [file];
}