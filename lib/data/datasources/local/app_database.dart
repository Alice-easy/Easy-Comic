import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

// Import entities, which will be used by Drift to generate tables.
// Note: These files don't exist yet, we will create them.
// This is a placeholder for the actual entities.
// We will use Drift's ability to generate data classes from tables.

part 'app_database.g.dart';

// Define Tables
class Mangas extends Table {
  TextColumn get id => text()();
  TextColumn get title => text()();
  TextColumn get filePath => text()();
  TextColumn get coverPath => text()();
  IntColumn get totalPages => integer()();
  IntColumn get currentPage => integer()();
  DateTimeColumn get lastRead => dateTime()();
  DateTimeColumn get dateAdded => dateTime()();
  TextColumn get tags => text().map(const ListOfStringConverter())();
  BoolColumn get isFavorite => boolean()();

  @override
  Set<Column> get primaryKey => {id};
}

class AppSettings extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get readingDirection => text()(); // Enum as text
  TextColumn get readingMode => text()(); // Enum as text
  TextColumn get pageTurnAnimation => text()(); // Enum as text
  BoolColumn get autoPageTurn => boolean()();
  IntColumn get autoPageTurnInterval => integer()();
  BoolColumn get volumeKeyPageTurn => boolean()();
  TextColumn get tapSensitivity => text().map(const MapOfStringDoubleConverter())(); // Map as JSON string
  TextColumn get themeMode => text()(); // Enum as text
  TextColumn get customTheme => text().nullable()(); // Stored as JSON string
  RealColumn get fontScale => real()();
}

class WebDAVConfigs extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get serverUrl => text()();
  TextColumn get username => text()();
  TextColumn get password => text()();
  BoolColumn get autoSync => boolean()();
  TextColumn get avatarPath => text()();
}

class Collections extends Table {
  TextColumn get id => text()();
  TextColumn get name => text()();

  @override
  Set<Column> get primaryKey => {id};
}

class MangaCollectionEntries extends Table {
  TextColumn get mangaId => text().references(Mangas, #id)();
  TextColumn get collectionId => text().references(Collections, #id)();

  @override
  Set<Column> get primaryKey => {mangaId, collectionId};
}


// Type Converters
class ListOfStringConverter extends TypeConverter<List<String>, String> {
  const ListOfStringConverter();
  @override
  List<String> fromSql(String fromDb) {
    return (fromDb.split(',')).where((s) => s.isNotEmpty).toList();
  }

  @override
  String toSql(List<String> value) {
    return value.join(',');
  }
}

class MapOfStringDoubleConverter extends TypeConverter<Map<String, double>, String> {
  const MapOfStringDoubleConverter();
  @override
  Map<String, double> fromSql(String fromDb) {
    // A simple implementation, assuming the string is a JSON map.
    // For production, you'd want a more robust JSON parsing.
    // This is a placeholder.
    return {};
  }

  @override
  String toSql(Map<String, double> value) {
    // A simple implementation.
    // This is a placeholder.
    return value.toString();
  }
}


@DriftDatabase(tables: [Mangas, AppSettings, WebDAVConfigs, Collections, MangaCollectionEntries])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    final file = File(p.join(dbFolder.path, 'db.sqlite'));
    return NativeDatabase(file);
  });
}