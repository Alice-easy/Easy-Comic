import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/core/utils/either.dart';
import 'package:easy_comic/domain/entities/comic.dart';

abstract class ComicRepository {
  Future<Either<Failure, Comic>> getComic(String filePath);
}