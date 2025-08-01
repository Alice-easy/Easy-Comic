import 'dart:io';

abstract class CacheService {
  Future<File> getCoverImage(String comicPath);
}