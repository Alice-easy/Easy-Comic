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
}

class ComicProgress extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get fileHash => text().unique()();
  IntColumn get currentPage => integer()();
  IntColumn get totalPages => integer()();
  DateTimeColumn get updatedAt => dateTime()();
  TextColumn get etag => text().nullable()(); // 用于同步
}

// Database class
@DriftDatabase(tables: [Comics, ComicProgress])
class DriftDb extends _$DriftDb {
  DriftDb() : super(_openConnection());

  // Constructor for testing
  DriftDb.withExecutor(super.executor);

  @override
  int get schemaVersion => 3;

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
      )..where((tbl) => tbl.fileHash.equals(fileHash)))
          .write(
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

  Future<ComicProgressData?> getProgress(String fileHash) =>
      (select(comicProgress)..where((tbl) => tbl.fileHash.equals(fileHash)))
          .getSingleOrNull();
}

// Connection setup
LazyDatabase _openConnection() => LazyDatabase(() async {
      final dbFolder = await getApplicationDocumentsDirectory();
      final file = File(p.join(dbFolder.path, 'comic_progress.db'));
      return NativeDatabase(file);
    });
