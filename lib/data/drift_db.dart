import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:uuid/uuid.dart';

part 'drift_db.g.dart';

// --- 数据表定义 (MOD-02) ---

/// 漫画核心信息表
@DataClassName('ComicModel')
@TableIndex(name: 'comics_last_read_time_idx', columns: {#lastReadTime})
class Comics extends Table {
  /// 主键，使用 UUID
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  /// 文件路径，唯一
  TextColumn get filePath => text().unique()();
  /// 文件名
  TextColumn get fileName => text()();
  /// 封面图片本地缓存路径
  TextColumn get coverPath => text()();
  /// 页面总数
  IntColumn get pageCount => integer()();
  /// 添加时间
  DateTimeColumn get addTime => dateTime()();
  /// 最后阅读时间
  DateTimeColumn get lastReadTime => dateTime()();
  /// 阅读进度 (已读页数)
  IntColumn get progress => integer()();
  /// 所属书架ID
  IntColumn get bookshelfId => integer().references(Bookshelves, #id)();
  /// 是否为收藏
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  /// 标签 (JSON 编码的列表)
  TextColumn get tags => text().withDefault(const Constant('[]'))();
  /// 元数据 (JSON 编码)
  TextColumn get metadata => text().withDefault(const Constant('{}'))();

  @override
  Set<Column> get primaryKey => {id};
}

/// 书架表
@DataClassName('BookshelfModel')
class Bookshelves extends Table {
  /// 主键，自增
  IntColumn get id => integer().autoIncrement()();
  /// 名称 (e.g., "阅读中", "已完成")
  TextColumn get name => text().unique()();
  /// 封面图片路径
  TextColumn get coverImage => text().nullable()();
  /// 创建时间
  DateTimeColumn get createTime => dateTime()();
}

/// 收藏夹表 (文件夹)
@DataClassName('FavoriteModel')
class Favorites extends Table {
  /// 主键，自增
  IntColumn get id => integer().autoIncrement()();
  /// 收藏夹名称
  TextColumn get name => text()();
  /// 父ID，用于实现层级结构
  IntColumn get parentId => integer().nullable().references(Favorites, #id)();
  /// 描述
  TextColumn get description => text().nullable()();
  /// 创建时间
  DateTimeColumn get createTime => dateTime()();
}

/// 漫画与收藏夹的关联表
class ComicFavoriteLinks extends Table {
  /// 漫画ID
  TextColumn get comicId => text().references(Comics, #id)();
  /// 收藏夹ID
  IntColumn get favoriteId => integer().references(Favorites, #id)();

  @override
  Set<Column> get primaryKey => {comicId, favoriteId};
}

/// 阅读历史记录表
@DataClassName('ReadingHistoryModel')
class ReadingHistory extends Table {
  /// 主键，自增
  IntColumn get id => integer().autoIncrement()();
  /// 漫画ID
  TextColumn get comicId => text().references(Comics, #id)();
  /// 阅读到的页码
  IntColumn get pageNumber => integer()();
  /// 时间戳
  DateTimeColumn get timestamp => dateTime()();
}


// --- 数据库访问对象 (DAO) ---

@DriftAccessor(tables: [Comics, ComicFavoriteLinks])
class ComicsDao extends DatabaseAccessor<AppDatabase> with _$ComicsDaoMixin {
  ComicsDao(AppDatabase db) : super(db);

  Stream<List<ComicModel>> watchComicsInBookshelf(int bookshelfId) {
    return (select(comics)..where((tbl) => tbl.bookshelfId.equals(bookshelfId))).watch();
  }

  Future<List<ComicModel>> getComicsInBookshelf(int bookshelfId, {int limit = 20, int offset = 0}) {
    final query = select(comics)
      ..where((tbl) => tbl.bookshelfId.equals(bookshelfId))
      ..orderBy([(t) => OrderingTerm(expression: t.lastReadTime, mode: OrderingMode.desc)])
      ..limit(limit, offset: offset);
    return query.get();
  }

  Future<void> addComic(ComicsCompanion entry) => into(comics).insert(entry);

  Future<void> updateComic(ComicsCompanion entry) => update(comics).replace(entry);

  Future<void> deleteComic(String id) => (delete(comics)..where((tbl) => tbl.id.equals(id))).go();
  
  Future<ComicModel?> getComic(String id) => (select(comics)..where((tbl) => tbl.id.equals(id))).getSingleOrNull();

  Future<bool> comicExists(String filePath) async {
    final result = await (select(comics)..where((tbl) => tbl.filePath.equals(filePath))).get();
    return result.isNotEmpty;
  }

  Future<List<ComicModel>> getAllComics() => select(comics).get();

  Future<void> clearAndInsertComics(List<ComicsCompanion> entries) async {
    await batch((batch) {
      batch.deleteAll(comics);
      batch.insertAll(comics, entries);
    });
  }

  Future<List<ComicModel>> searchComicsInBookshelf(int bookshelfId, String query) {
    if (query.isEmpty) {
      return getComicsInBookshelf(bookshelfId);
    }
    
    final lowercaseQuery = query.toLowerCase();
    final searchQuery = select(comics)
      ..where((tbl) => tbl.bookshelfId.equals(bookshelfId) &
          tbl.fileName.lower().contains(lowercaseQuery))
      ..orderBy([(t) => OrderingTerm(expression: t.lastReadTime, mode: OrderingMode.desc)]);
    
    return searchQuery.get();
  }

  Future<List<ComicModel>> sortComicsInBookshelf(int bookshelfId, SortType sortType) {
    final query = select(comics)..where((tbl) => tbl.bookshelfId.equals(bookshelfId));
    
    switch (sortType) {
      case SortType.dateAdded:
        query.orderBy([(t) => OrderingTerm(expression: t.addTime, mode: OrderingMode.desc)]);
        break;
      case SortType.title:
        query.orderBy([(t) => OrderingTerm(expression: t.fileName, mode: OrderingMode.asc)]);
        break;
      case SortType.author:
        // 由于我们没有作者字段，按文件名排序作为替代
        query.orderBy([(t) => OrderingTerm(expression: t.fileName, mode: OrderingMode.asc)]);
        break;
    }
    
    return query.get();
  }
}

@DriftAccessor(tables: [Bookshelves])
class BookshelvesDao extends DatabaseAccessor<AppDatabase> with _$BookshelvesDaoMixin {
  BookshelvesDao(AppDatabase db) : super(db);

  Stream<List<BookshelfModel>> watchAllBookshelves() => select(bookshelves).watch();

  Future<int> addBookshelf(BookshelvesCompanion entry) => into(bookshelves).insert(entry);

  Future<void> updateBookshelf(BookshelvesCompanion entry) => update(bookshelves).replace(entry);

  Future<void> deleteBookshelf(int id) => (delete(bookshelves)..where((tbl) => tbl.id.equals(id))).go();
}

@DriftAccessor(tables: [Favorites, ComicFavoriteLinks])
class FavoritesDao extends DatabaseAccessor<AppDatabase> with _$FavoritesDaoMixin {
  FavoritesDao(AppDatabase db) : super(db);

  Stream<List<FavoriteModel>> watchAllFavorites() => select(favorites).watch();

  Future<void> addComicToFavorite(String comicId, int favoriteId) {
    return into(comicFavoriteLinks).insert(
      ComicFavoriteLinksCompanion(
        comicId: Value(comicId),
        favoriteId: Value(favoriteId),
      ),
    );
  }

  Future<List<FavoriteModel>> getAllFavorites() => select(favorites).get();

  Future<void> clearAndInsertFavorites(List<FavoritesCompanion> entries) async {
    await batch((batch) {
      batch.deleteAll(favorites);
      batch.insertAll(favorites, entries);
    });
  }

  Future<int> createFavorite(FavoritesCompanion entry) => into(favorites).insert(entry);

  Future<void> deleteFavorite(int id) => (delete(favorites)..where((tbl) => tbl.id.equals(id))).go();

  Future<void> removeComicFromFavorite(String comicId, int favoriteId) {
    return (delete(comicFavoriteLinks)
          ..where((tbl) => tbl.comicId.equals(comicId) & tbl.favoriteId.equals(favoriteId)))
        .go();
  }

  Future<List<ComicModel>> getComicsInFavorite(int favoriteId) {
    final query = select(comics).join([
      innerJoin(comicFavoriteLinks, comicFavoriteLinks.comicId.equalsExp(comics.id))
    ])
      ..where(comicFavoriteLinks.favoriteId.equals(favoriteId));
    
    return query.map((row) => row.readTable(comics)).get();
  }
}


// --- 数据库主类 ---

@DriftDatabase(
  tables: [Comics, Bookshelves, Favorites, ComicFavoriteLinks, ReadingHistory],
  daos: [ComicsDao, BookshelvesDao, FavoritesDao],
)
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  AppDatabase.forTesting(DatabaseConnection connection) : super(connection);

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onCreate: (m) async {
      await m.createAll();
      // 创建一个默认书架
      await into(bookshelves).insert(
        BookshelvesCompanion(
          name: const Value('默认书架'),
          createTime: Value(DateTime.now()),
        ),
      );
    },
  );
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'easy_comic.db'));
    return NativeDatabase(file);
  });
}
