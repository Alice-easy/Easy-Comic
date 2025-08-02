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

/// 漫画阅读进度表 - 用于详细的进度跟踪和同步
@DataClassName('ComicProgressModel')
@TableIndex(name: 'comic_progress_comic_id_idx', columns: {#comicId})
@TableIndex(name: 'comic_progress_last_updated_idx', columns: {#lastUpdated})
class ComicProgress extends Table {
  /// 主键，使用 UUID
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  /// 漫画ID (外键)
  TextColumn get comicId => text().references(Comics, #id)();
  /// 当前页码
  IntColumn get currentPage => integer()();
  /// 总页数
  IntColumn get totalPages => integer()();
  /// 最后更新时间
  DateTimeColumn get lastUpdated => dateTime()();
  /// 是否已完成
  BoolColumn get isCompleted => boolean().withDefault(const Constant(false))();
  
  /// 同步管理
  /// 同步状态: 'pending', 'synced', 'conflict'
  TextColumn get syncStatus => text().withDefault(const Constant('pending'))();
  /// WebDAV 同步的 ETag
  TextColumn get syncETag => text().nullable()();
  /// 最后同步时间
  DateTimeColumn get lastSyncTime => dateTime().nullable()();
  
  /// 性能跟踪
  /// 阅读时长（秒）
  IntColumn get readingTimeSeconds => integer().withDefault(const Constant(0))();
  /// 元数据 JSON
  TextColumn get metadata => text().withDefault(const Constant('{}'))();
  
  @override
  Set<Column> get primaryKey => {id};
}

/// 缓存元数据表 - 用于缓存管理和性能优化
@DataClassName('CacheMetadataModel')
@TableIndex(name: 'cache_metadata_comic_id_idx', columns: {#comicId})
@TableIndex(name: 'cache_metadata_last_accessed_idx', columns: {#lastAccessed})
class CacheMetadata extends Table {
  /// 缓存键
  TextColumn get cacheKey => text()();
  /// 漫画ID
  TextColumn get comicId => text().references(Comics, #id)();
  /// 页面索引
  IntColumn get pageIndex => integer()();
  /// 文件大小（字节）
  IntColumn get sizeBytes => integer()();
  /// 最后访问时间
  DateTimeColumn get lastAccessed => dateTime()();
  /// 创建时间
  DateTimeColumn get createdAt => dateTime()();
  /// 访问次数
  IntColumn get accessCount => integer().withDefault(const Constant(0))();
  /// 优先级: 'critical', 'high', 'medium', 'low'
  TextColumn get priority => text().withDefault(const Constant('low'))();
  
  @override
  Set<Column> get primaryKey => {cacheKey};
}

/// 性能指标表 - 用于监控和优化
@DataClassName('PerformanceMetricsModel')
@TableIndex(name: 'performance_metrics_timestamp_idx', columns: {#timestamp})
class PerformanceMetrics extends Table {
  /// 主键，使用 UUID
  TextColumn get id => text().clientDefault(() => const Uuid().v4())();
  /// 时间戳
  DateTimeColumn get timestamp => dateTime()();
  /// 指标类型: 'memory', 'performance', 'error', 'cache'
  TextColumn get metricType => text()();
  /// 相关漫画ID（可选）
  TextColumn get comicId => text().nullable().references(Comics, #id)();
  /// 指标值
  RealColumn get value => real()();
  /// 单位: 'MB', 'ms', 'count', 'percentage'
  TextColumn get unit => text()();
  /// 上下文信息 JSON
  TextColumn get context => text().withDefault(const Constant('{}'))();
  
  @override
  Set<Column> get primaryKey => {id};
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

/// 漫画进度数据访问对象
@DriftAccessor(tables: [ComicProgress])
class ComicProgressDao extends DatabaseAccessor<AppDatabase> with _$ComicProgressDaoMixin {
  ComicProgressDao(AppDatabase db) : super(db);

  /// 获取指定漫画的进度
  Future<ComicProgressModel?> getProgress(String comicId) =>
      (select(comicProgress)..where((tbl) => tbl.comicId.equals(comicId))).getSingleOrNull();

  /// 监听指定漫画的进度变化
  Stream<ComicProgressModel?> watchProgress(String comicId) =>
      (select(comicProgress)..where((tbl) => tbl.comicId.equals(comicId))).watchSingleOrNull();

  /// 保存或更新进度
  Future<void> saveProgress(ComicProgressCompanion entry) async {
    await into(comicProgress).insert(entry, mode: InsertMode.insertOrReplace);
  }

  /// 批量保存进度
  Future<void> batchSaveProgress(List<ComicProgressCompanion> entries) async {
    await batch((batch) {
      for (final entry in entries) {
        batch.insert(comicProgress, entry, mode: InsertMode.insertOrReplace);
      }
    });
  }

  /// 获取需要同步的进度记录
  Future<List<ComicProgressModel>> getPendingSyncProgress() =>
      (select(comicProgress)..where((tbl) => tbl.syncStatus.equals('pending'))).get();

  /// 标记进度为已同步
  Future<void> markAsSynced(String comicId, String etag) async {
    await (update(comicProgress)..where((tbl) => tbl.comicId.equals(comicId)))
        .write(ComicProgressCompanion(
      syncStatus: const Value('synced'),
      syncETag: Value(etag),
      lastSyncTime: Value(DateTime.now()),
    ));
  }

  /// 获取所有完成的漫画
  Future<List<ComicProgressModel>> getCompletedComics() =>
      (select(comicProgress)..where((tbl) => tbl.isCompleted.equals(true))).get();

  /// 删除指定漫画的进度
  Future<void> deleteProgress(String comicId) =>
      (delete(comicProgress)..where((tbl) => tbl.comicId.equals(comicId))).go();

  /// 更新阅读时长
  Future<void> updateReadingTime(String comicId, int additionalSeconds) async {
    final existing = await getProgress(comicId);
    if (existing != null) {
      await (update(comicProgress)..where((tbl) => tbl.comicId.equals(comicId)))
          .write(ComicProgressCompanion(
        readingTimeSeconds: Value(existing.readingTimeSeconds + additionalSeconds),
        lastUpdated: Value(DateTime.now()),
      ));
    }
  }
}

/// 缓存元数据数据访问对象
@DriftAccessor(tables: [CacheMetadata])
class CacheMetadataDao extends DatabaseAccessor<AppDatabase> with _$CacheMetadataDaoMixin {
  CacheMetadataDao(AppDatabase db) : super(db);

  /// 添加缓存元数据
  Future<void> addCacheMetadata(CacheMetadataCompanion entry) =>
      into(cacheMetadata).insert(entry, mode: InsertMode.insertOrReplace);

  /// 获取缓存元数据
  Future<CacheMetadataModel?> getCacheMetadata(String cacheKey) =>
      (select(cacheMetadata)..where((tbl) => tbl.cacheKey.equals(cacheKey))).getSingleOrNull();

  /// 更新访问信息
  Future<void> updateAccess(String cacheKey) async {
    final existing = await getCacheMetadata(cacheKey);
    if (existing != null) {
      await (update(cacheMetadata)..where((tbl) => tbl.cacheKey.equals(cacheKey)))
          .write(CacheMetadataCompanion(
        lastAccessed: Value(DateTime.now()),
        accessCount: Value(existing.accessCount + 1),
      ));
    }
  }

  /// 获取低优先级的缓存项用于清理
  Future<List<CacheMetadataModel>> getLowPriorityCache(int limit) =>
      (select(cacheMetadata)
        ..where((tbl) => tbl.priority.isNotIn(['critical', 'high']))
        ..orderBy([(t) => OrderingTerm(expression: t.lastAccessed, mode: OrderingMode.asc)])
        ..limit(limit))
          .get();

  /// 删除缓存元数据
  Future<void> deleteCacheMetadata(String cacheKey) =>
      (delete(cacheMetadata)..where((tbl) => tbl.cacheKey.equals(cacheKey))).go();

  /// 获取指定漫画的所有缓存
  Future<List<CacheMetadataModel>> getComicCacheMetadata(String comicId) =>
      (select(cacheMetadata)..where((tbl) => tbl.comicId.equals(comicId))).get();

  /// 清理过期缓存元数据
  Future<void> cleanupOldCache(DateTime cutoffTime) =>
      (delete(cacheMetadata)..where((tbl) => tbl.lastAccessed.isSmallerThanValue(cutoffTime))).go();
}

/// 性能指标数据访问对象
@DriftAccessor(tables: [PerformanceMetrics])
class PerformanceMetricsDao extends DatabaseAccessor<AppDatabase> with _$PerformanceMetricsDaoMixin {
  PerformanceMetricsDao(AppDatabase db) : super(db);

  /// 添加性能指标
  Future<void> addMetric(PerformanceMetricsCompanion entry) =>
      into(performanceMetrics).insert(entry);

  /// 获取指定类型的最新指标
  Future<List<PerformanceMetricsModel>> getRecentMetrics(String metricType, int limit) =>
      (select(performanceMetrics)
        ..where((tbl) => tbl.metricType.equals(metricType))
        ..orderBy([(t) => OrderingTerm(expression: t.timestamp, mode: OrderingMode.desc)])
        ..limit(limit))
          .get();

  /// 获取指定时间范围内的指标
  Future<List<PerformanceMetricsModel>> getMetricsInRange(
      String metricType, DateTime startTime, DateTime endTime) =>
      (select(performanceMetrics)
        ..where((tbl) => tbl.metricType.equals(metricType) &
            tbl.timestamp.isBetweenValues(startTime, endTime))
        ..orderBy([(t) => OrderingTerm(expression: t.timestamp, mode: OrderingMode.desc)]))
          .get();

  /// 清理旧的性能指标
  Future<void> cleanupOldMetrics(DateTime cutoffTime) =>
      (delete(performanceMetrics)..where((tbl) => tbl.timestamp.isSmallerThanValue(cutoffTime))).go();

  /// 获取平均性能指标
  Future<double?> getAverageMetric(String metricType, DateTime since) async {
    final query = selectOnly(performanceMetrics)
      ..addColumns([performanceMetrics.value.avg()])
      ..where(performanceMetrics.metricType.equals(metricType) &
          performanceMetrics.timestamp.isBiggerThanValue(since));
    
    final result = await query.getSingleOrNull();
    return result?.read(performanceMetrics.value.avg());
  }
}


// --- 数据库主类 ---

@DriftDatabase(
  tables: [
    Comics, 
    Bookshelves, 
    Favorites, 
    ComicFavoriteLinks, 
    ReadingHistory,
    ComicProgress,
    CacheMetadata,
    PerformanceMetrics,
  ],
  daos: [
    ComicsDao, 
    BookshelvesDao, 
    FavoritesDao, 
    ComicProgressDao,
    CacheMetadataDao,
    PerformanceMetricsDao,
  ],
)
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  AppDatabase.forTesting(DatabaseConnection connection) : super(connection);

  @override
  int get schemaVersion => 2;

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
    onUpgrade: (m, from, to) async {
      if (from < 2) {
        // 添加新的表格
        await m.createTable(comicProgress);
        await m.createTable(cacheMetadata);
        await m.createTable(performanceMetrics);
      }
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
