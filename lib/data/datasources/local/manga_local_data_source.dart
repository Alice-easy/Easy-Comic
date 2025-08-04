import 'package:easy_comic/domain/entities/manga.dart';

import 'package:easy_comic/domain/entities/collection.dart';

abstract class MangaLocalDataSource {
  Future<List<Manga>> getAllMangas();
  Future<Manga?> getMangaById(String mangaId);
  Future<void> addManga(Manga manga);
  Future<void> updateManga(Manga manga);
  Future<void> deleteManga(String mangaId);

  // Collections
  Future<List<Collection>> getAllCollections();
  Future<void> createCollection(Collection collection);
  Future<void> renameCollection(String collectionId, String newName);
  Future<void> deleteCollection(String collectionId);
  Future<void> addMangaToCollection(String mangaId, String collectionId);
  Future<void> removeMangaFromCollection(String mangaId, String collectionId);
}