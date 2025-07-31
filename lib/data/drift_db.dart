import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

part 'drift_db.g.dart';

// Table definitions
class Comics extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get filePath => text().unique()();
  TextColumn get fileName => text()();
  TextColumn get coverImage => text().nullable()();
  DateTimeColumn get addedAt => dateTime()();
  BoolColumn get isFavorite => boolean().withDefault(const Constant(false))();
  DateTimeColumn get lastReadAt => dateTime().nullable()();
  RealColumn get progress => real().withDefault(const Constant(0.0))();
}

class Bookmarks extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get comicId => integer().references(Comics, #id)();
  IntColumn get pageIndex => integer()();
  TextColumn get label => text().nullable()();
  DateTimeColumn get createdAt => dateTime()();
}

class ComicProgress extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get fileHash => text().unique()();
  IntColumn get currentPage => integer()();
  IntColumn get totalPages => integer()();
  DateTimeColumn get updatedAt => dateTime()();
  TextColumn get etag => text().nullable()(); // 用于同步
}

class ReadingSessions extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get fileHash => text()();
  DateTimeColumn get startTime => dateTime()();
  DateTimeColumn get endTime => dateTime()();
  IntColumn get durationInSeconds => integer()();
}

// Reader settings table for enhanced reading features
class ReaderSettings extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get userId => text().nullable()(); // For future multi-user support
  TextColumn get readingMode => text().withDefault(const Constant('single'))();
  TextColumn get navigationDirection => text().withDefault(const Constant('horizontal'))();
  TextColumn get backgroundTheme => text().withDefault(const Constant('black'))();
  TextColumn get transitionType => text().withDefault(const Constant('none'))();
  RealColumn get brightness => real().withDefault(const Constant(1.0))();
  BoolColumn get showThumbnails => boolean().withDefault(const Constant(true))();
  DateTimeColumn get updatedAt => dateTime()();
  TextColumn get etag => text().nullable()(); // For WebDAV sync
}

// Page custom ordering for drag-and-drop reordering
class PageCustomOrder extends Table {
  IntColumn get comicId => integer().references(Comics, #id)();
  IntColumn get originalIndex => integer()();
  IntColumn get customIndex => integer()();
  DateTimeColumn get createdAt => dateTime()();
  
  @override
  Set<Column> get primaryKey => {comicId, originalIndex};
}

// Enhanced reading history with session tracking
class ReadingHistory extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get comicId => integer().references(Comics, #id)();
  IntColumn get lastPageRead => integer()();
  DateTimeColumn get lastReadAt => dateTime()();
  IntColumn get totalTimeSeconds => integer()();
  TextColumn get sessionId => text()(); // For session grouping
}

// Bookmark thumbnails for visual bookmark navigation
class BookmarkThumbnails extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get bookmarkId => integer().references(Bookmarks, #id)();
  TextColumn get thumbnailPath => text()(); // Local file path
  DateTimeColumn get createdAt => dateTime()();
}

// Database class
@DriftDatabase(tables: [Comics, ComicProgress, ReadingSessions, Bookmarks, ReaderSettings, PageCustomOrder, ReadingHistory, BookmarkThumbnails])
class DriftDb extends _$DriftDb {
  DriftDb() : super(_openConnection());

  // Constructor for testing
  DriftDb.withExecutor(super.executor);

  @override
  int get schemaVersion => 6;

  @override
  MigrationStrategy get migration => MigrationStrategy(
    onCreate: (m) => m.createAll(),
    onUpgrade: (Migrator m, int from, int to) async {
      if (from < 2) {
        await m.addColumn(comicProgress, comicProgress.etag);
      }
      if (from < 3) {
        await m.createTable(comics);
        await m.addColumn(comicProgress, comicProgress.totalPages);
      }
      if (from < 4) {
        await m.createTable(readingSessions);
      }
      if (from < 5) {
        await m.addColumn(comics, comics.isFavorite);
        await m.addColumn(comics, comics.lastReadAt);
        await m.addColumn(comics, comics.progress);
        await m.createTable(bookmarks);
      }
      if (from < 6) {
        await m.createTable(readerSettings);
        await m.createTable(pageCustomOrder);
        await m.createTable(readingHistory);
        await m.createTable(bookmarkThumbnails);
        
        // Create default reader settings entry
        await into(readerSettings).insert(
          ReaderSettingsCompanion(
            readingMode: const Value('single'),
            navigationDirection: const Value('horizontal'),
            backgroundTheme: const Value('black'),
            transitionType: const Value('none'),
            brightness: const Value(1.0),
            showThumbnails: const Value(true),
            updatedAt: Value(DateTime.now()),
          ),
        );
      }
    },
  );

  // DAO methods
  Future<void> upsertProgress(
    String fileHash,
    int currentPage,
    int totalPages, {
    String? etag,
  }) async {
    final existing = await getProgress(fileHash);
    if (existing != null) {
      await (update(
        comicProgress,
      )..where((tbl) => tbl.fileHash.equals(fileHash))).write(
        ComicProgressCompanion(
          currentPage: Value(currentPage),
          totalPages: Value(totalPages),
          updatedAt: Value(DateTime.now()),
          etag: Value(etag),
        ),
      );
    } else {
      await into(comicProgress).insert(
        ComicProgressCompanion(
          fileHash: Value(fileHash),
          currentPage: Value(currentPage),
          totalPages: Value(totalPages),
          updatedAt: Value(DateTime.now()),
          etag: Value(etag),
        ),
      );
    }
  }

  Future<ComicProgressData?> getProgress(String fileHash) => (select(
    comicProgress,
  )..where((tbl) => tbl.fileHash.equals(fileHash))).getSingleOrNull();

  Future<void> addReadingSession(ReadingSession session) =>
      into(readingSessions).insert(session);

  Future<int> getThisWeekReadingMinutes() async {
    final now = DateTime.now();
    final startOfWeek = now.subtract(Duration(days: now.weekday - 1));
    final endOfWeek = startOfWeek.add(const Duration(days: 7));
    final sessions =
        await (select(readingSessions)..where(
              (tbl) =>
                  tbl.startTime.isBiggerOrEqualValue(startOfWeek) &
                  tbl.endTime.isSmallerOrEqualValue(endOfWeek),
            ))
            .get();
    final totalSeconds = sessions.fold<int>(
      0,
      (previousValue, element) => previousValue + element.durationInSeconds,
    );
    return totalSeconds ~/ 60;
  }

  // Reader Settings DAO methods
  Future<ReaderSetting?> getReaderSettings([String? userId]) async {
    final query = select(readerSettings);
    if (userId != null) {
      query.where((tbl) => tbl.userId.equals(userId));
    } else {
      query.where((tbl) => tbl.userId.isNull());
    }
    return await query.getSingleOrNull();
  }

  Future<void> updateReaderSettings(ReaderSetting settings) async {
    await (update(readerSettings)..where((tbl) => tbl.id.equals(settings.id)))
        .write(settings.toCompanion(true).copyWith(updatedAt: Value(DateTime.now())));
  }

  // Page Custom Order DAO methods
  Future<List<PageCustomOrderData>> getCustomPageOrder(int comicId) async {
    return await (select(pageCustomOrder)
          ..where((tbl) => tbl.comicId.equals(comicId))
          ..orderBy([(tbl) => OrderingTerm.asc(tbl.customIndex)]))
        .get();
  }

  Future<void> setCustomPageOrder(int comicId, List<int> newOrder) async {
    await transaction(() async {
      // Delete existing custom order
      await (delete(pageCustomOrder)..where((tbl) => tbl.comicId.equals(comicId))).go();
      
      // Insert new order
      for (int i = 0; i < newOrder.length; i++) {
        await into(pageCustomOrder).insert(PageCustomOrderCompanion(
          comicId: Value(comicId),
          originalIndex: Value(newOrder[i]),
          customIndex: Value(i),
          createdAt: Value(DateTime.now()),
        ));
      }
    });
  }

  Future<void> clearCustomPageOrder(int comicId) async {
    await (delete(pageCustomOrder)..where((tbl) => tbl.comicId.equals(comicId))).go();
  }

  // Reading History DAO methods
  Future<void> addToReadingHistory(int comicId, int pageRead, String sessionId) async {
    final existing = await (select(readingHistory)
          ..where((tbl) => tbl.comicId.equals(comicId))
          ..orderBy([(tbl) => OrderingTerm.desc(tbl.lastReadAt)])
          ..limit(1))
        .getSingleOrNull();

    if (existing != null) {
      await (update(readingHistory)..where((tbl) => tbl.id.equals(existing.id)))
          .write(ReadingHistoryCompanion(
        lastPageRead: Value(pageRead),
        lastReadAt: Value(DateTime.now()),
        sessionId: Value(sessionId),
      ));
    } else {
      await into(readingHistory).insert(ReadingHistoryCompanion(
        comicId: Value(comicId),
        lastPageRead: Value(pageRead),
        lastReadAt: Value(DateTime.now()),
        totalTimeSeconds: const Value(0),
        sessionId: Value(sessionId),
      ));
    }
  }

  Future<List<ReadingHistoryData>> getReadingHistory({int limit = 50}) async {
    return await (select(readingHistory)
          ..orderBy([(tbl) => OrderingTerm.desc(tbl.lastReadAt)])
          ..limit(limit))
        .get();
  }

  Future<void> cleanupReadingHistory({int keepCount = 50}) async {
    final allHistory = await (select(readingHistory)
          ..orderBy([(tbl) => OrderingTerm.desc(tbl.lastReadAt)]))
        .get();
    
    if (allHistory.length > keepCount) {
      final toDelete = allHistory.skip(keepCount).map((h) => h.id).toList();
      await (delete(readingHistory)..where((tbl) => tbl.id.isIn(toDelete))).go();
    }
  }

  // Bookmark Thumbnails DAO methods
  Future<void> addBookmarkThumbnail(int bookmarkId, String thumbnailPath) async {
    await into(bookmarkThumbnails).insert(BookmarkThumbnailsCompanion(
      bookmarkId: Value(bookmarkId),
      thumbnailPath: Value(thumbnailPath),
      createdAt: Value(DateTime.now()),
    ));
  }

  Future<BookmarkThumbnail?> getBookmarkThumbnail(int bookmarkId) async {
    return await (select(bookmarkThumbnails)
          ..where((tbl) => tbl.bookmarkId.equals(bookmarkId)))
        .getSingleOrNull();
  }

  Future<void> deleteBookmarkThumbnail(int bookmarkId) async {
    await (delete(bookmarkThumbnails)..where((tbl) => tbl.bookmarkId.equals(bookmarkId))).go();
  }
}

// Connection setup
LazyDatabase _openConnection() => LazyDatabase(() async {
  final dbFolder = await getApplicationDocumentsDirectory();
  final file = File(p.join(dbFolder.path, 'comic_progress.db'));
  return NativeDatabase(file);
});
