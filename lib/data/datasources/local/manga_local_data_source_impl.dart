import 'package:drift/drift.dart';
import 'package:easy_comic/data/datasources/local/app_database.dart' as db;
import 'package:easy_comic/data/datasources/local/manga_local_data_source.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/domain/entities/collection.dart';

class MangaLocalDataSourceImpl implements MangaLocalDataSource {
  final db.AppDatabase database;

  MangaLocalDataSourceImpl({required this.database});

  @override
  Future<void> addManga(Manga manga) {
    final mangaCompanion = manga.toCompanion();
    return database.into(database.mangas).insert(mangaCompanion, mode: InsertMode.replace);
  }

  @override
  Future<void> deleteManga(String mangaId) {
    return (database.delete(database.mangas)..where((tbl) => tbl.id.equals(mangaId))).go();
  }

  @override
  Future<List<Manga>> getAllMangas() async {
    final mangaDataList = await database.select(database.mangas).get();
    return mangaDataList.map<Manga>((mangaData) => MangaFactory.fromDrift(mangaData)).toList();
  }

  @override
  Future<Manga?> getMangaById(String mangaId) async {
    final query = database.select(database.mangas)..where((tbl) => tbl.id.equals(mangaId));
    final mangaData = await query.getSingleOrNull();
    return mangaData != null ? MangaFactory.fromDrift(mangaData) : null;
  }

  @override
  Future<void> updateManga(Manga manga) {
    final mangaCompanion = manga.toCompanion();
    return (database.update(database.mangas)..where((tbl) => tbl.id.equals(manga.id))).write(mangaCompanion);
  }

  @override
  Future<void> addMangaToCollection(String mangaId, String collectionId) {
    // TODO: implement addMangaToCollection
    throw UnimplementedError();
  }

  @override
  Future<void> createCollection(Collection collection) {
    // TODO: implement createCollection
    throw UnimplementedError();
  }

  @override
  Future<void> deleteCollection(String collectionId) {
    // TODO: implement deleteCollection
    throw UnimplementedError();
  }

  @override
  Future<List<Collection>> getAllCollections() {
    // TODO: implement getAllCollections
    throw UnimplementedError();
  }

  @override
  Future<void> removeMangaFromCollection(String mangaId, String collectionId) {
    // TODO: implement removeMangaFromCollection
    throw UnimplementedError();
  }

  @override
  Future<void> renameCollection(String collectionId, String newName) {
    // TODO: implement renameCollection
    throw UnimplementedError();
  }
}

extension on Manga {
  db.MangasCompanion toCompanion() {
    return db.MangasCompanion(
      id: Value(id),
      title: Value(title),
      filePath: Value(filePath),
      coverPath: Value(coverPath),
      totalPages: Value(totalPages),
      currentPage: Value(currentPage),
      lastRead: Value(lastRead),
      dateAdded: Value(dateAdded),
      tags: Value(tags),
      isFavorite: Value(isFavorite),
    );
  }
}

extension on db.Manga {
  Manga toEntity() {
    return Manga(
      id: id,
      title: title,
      filePath: filePath,
      coverPath: coverPath,
      totalPages: totalPages,
      currentPage: currentPage,
      lastRead: lastRead,
      dateAdded: dateAdded,
      tags: tags,
      isFavorite: isFavorite,
    );
  }
}

// This factory will be used in the main fromDrift method
extension MangaFactory on Manga {
  static Manga fromDrift(db.Manga data) {
    return data.toEntity();
  }
}