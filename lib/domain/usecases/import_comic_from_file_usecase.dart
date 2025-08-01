import 'package:easy_comic/core/error/failures.dart';
import 'package:dartz/dartz.dart';
import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:path/path.dart' as p;

class ImportComicFromFileUsecase {
  final ComicRepository _comicRepository;

  ImportComicFromFileUsecase(this._comicRepository);

  Future<Either<Failure, void>> call(String filePath, int bookshelfId) async {
    try {
      // TODO: Add metadata parsing from the comic file (e.g., zip, cbz)
      // For now, create a basic comic object.
      final fileName = p.basename(filePath);
      final comic = Comic(
        id: DateTime.now().millisecondsSinceEpoch.toString(), // Temporary ID
        title: fileName.split('.').first,
        path: filePath,
        filePath: filePath,
        fileName: fileName,
        coverPath: '', // TODO: Generate or extract cover
        pageCount: 0, // TODO: Extract page count
        addTime: DateTime.now(),
        lastReadTime: DateTime.now(),
        progress: 0,
        bookshelfId: bookshelfId,
        isFavorite: false,
        tags: [],
        metadata: {},
      );

      return await _comicRepository.addComic(comic);
    } catch (e) {
      return Left(CacheFailure('Failed to import comic from file.'));
    }
  }
}