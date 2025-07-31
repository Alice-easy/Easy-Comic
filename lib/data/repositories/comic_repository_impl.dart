import 'dart:typed_data';
import 'package:path/path.dart' as p;
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/services/archive_service.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';

class ComicRepositoryImpl implements ComicRepository {
  final ArchiveService archiveService;

  ComicRepositoryImpl({required this.archiveService});

  @override
  Future<Either<Failure, Comic>> getComic(String filePath) async {
    try {
      final List<Uint8List> imageBytesList = await archiveService.extractImages(filePath);

      final pages = imageBytesList
          .asMap()
          .map((index, data) => MapEntry(index, ComicPage(pageIndex: index, imageData: data)))
          .values
          .toList();

      final comic = Comic(
        id: filePath.hashCode,
        filePath: filePath,
        title: p.basenameWithoutExtension(filePath),
        pages: pages,
      );

      return Right(comic);
    } catch (e) {
      // Here you could check for specific exception types from ArchiveService
      // and return more specific Failures if needed.
      return Left(ServerFailure('Could not extract comic file.'));
    }
  }
}