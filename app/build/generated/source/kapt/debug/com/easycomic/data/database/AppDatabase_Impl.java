package com.easycomic.data.database;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import com.easycomic.data.database.dao.BookmarkDao;
import com.easycomic.data.database.dao.BookmarkDao_Impl;
import com.easycomic.data.database.dao.MangaDao;
import com.easycomic.data.database.dao.MangaDao_Impl;
import com.easycomic.data.database.dao.ReadingHistoryDao;
import com.easycomic.data.database.dao.ReadingHistoryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MangaDao _mangaDao;

  private volatile BookmarkDao _bookmarkDao;

  private volatile ReadingHistoryDao _readingHistoryDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(1, "bd5d7ac5530cd16f9b39af71364037ac", "baab9075fd42eaa3e89b5621aa96d4ba") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `manga` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `author` TEXT, `description` TEXT, `file_path` TEXT NOT NULL, `file_size` INTEGER NOT NULL, `format` TEXT NOT NULL, `cover_path` TEXT, `page_count` INTEGER NOT NULL, `current_page` INTEGER NOT NULL, `reading_progress` REAL NOT NULL, `is_favorite` INTEGER NOT NULL, `is_completed` INTEGER NOT NULL, `date_added` INTEGER NOT NULL, `last_read` INTEGER, `reading_time` INTEGER NOT NULL, `rating` REAL NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_manga_title` ON `manga` (`title`)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_manga_last_read` ON `manga` (`last_read`)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_manga_favorite` ON `manga` (`is_favorite`, `last_read`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `bookmark` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga_id` INTEGER NOT NULL, `page_number` INTEGER NOT NULL, `bookmark_name` TEXT, `notes` TEXT, `created_at` INTEGER NOT NULL, FOREIGN KEY(`manga_id`) REFERENCES `manga`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_bookmark_manga_id` ON `bookmark` (`manga_id`)");
        SQLite.execSQL(connection, "CREATE UNIQUE INDEX IF NOT EXISTS `index_bookmark_manga_id_page_number` ON `bookmark` (`manga_id`, `page_number`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `reading_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga_id` INTEGER NOT NULL, `page_number` INTEGER NOT NULL, `reading_time` INTEGER NOT NULL, `session_start` INTEGER NOT NULL, `session_end` INTEGER NOT NULL, `reading_speed` REAL, `created_at` INTEGER NOT NULL, FOREIGN KEY(`manga_id`) REFERENCES `manga`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_history_manga_id` ON `reading_history` (`manga_id`)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `idx_history_session` ON `reading_history` (`session_start`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bd5d7ac5530cd16f9b39af71364037ac')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `manga`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `bookmark`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `reading_history`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsManga = new HashMap<String, TableInfo.Column>(19);
        _columnsManga.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("author", new TableInfo.Column("author", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_path", new TableInfo.Column("file_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_size", new TableInfo.Column("file_size", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("format", new TableInfo.Column("format", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("cover_path", new TableInfo.Column("cover_path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("page_count", new TableInfo.Column("page_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("current_page", new TableInfo.Column("current_page", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("reading_progress", new TableInfo.Column("reading_progress", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("is_favorite", new TableInfo.Column("is_favorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("is_completed", new TableInfo.Column("is_completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("date_added", new TableInfo.Column("date_added", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("last_read", new TableInfo.Column("last_read", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("reading_time", new TableInfo.Column("reading_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("rating", new TableInfo.Column("rating", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysManga = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesManga = new HashSet<TableInfo.Index>(3);
        _indicesManga.add(new TableInfo.Index("idx_manga_title", false, Arrays.asList("title"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("idx_manga_last_read", false, Arrays.asList("last_read"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("idx_manga_favorite", false, Arrays.asList("is_favorite", "last_read"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoManga = new TableInfo("manga", _columnsManga, _foreignKeysManga, _indicesManga);
        final TableInfo _existingManga = TableInfo.read(connection, "manga");
        if (!_infoManga.equals(_existingManga)) {
          return new RoomOpenDelegate.ValidationResult(false, "manga(com.easycomic.data.database.entity.MangaEntity).\n"
                  + " Expected:\n" + _infoManga + "\n"
                  + " Found:\n" + _existingManga);
        }
        final Map<String, TableInfo.Column> _columnsBookmark = new HashMap<String, TableInfo.Column>(6);
        _columnsBookmark.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("manga_id", new TableInfo.Column("manga_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("page_number", new TableInfo.Column("page_number", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("bookmark_name", new TableInfo.Column("bookmark_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysBookmark = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysBookmark.add(new TableInfo.ForeignKey("manga", "CASCADE", "NO ACTION", Arrays.asList("manga_id"), Arrays.asList("id")));
        final Set<TableInfo.Index> _indicesBookmark = new HashSet<TableInfo.Index>(2);
        _indicesBookmark.add(new TableInfo.Index("idx_bookmark_manga_id", false, Arrays.asList("manga_id"), Arrays.asList("ASC")));
        _indicesBookmark.add(new TableInfo.Index("index_bookmark_manga_id_page_number", true, Arrays.asList("manga_id", "page_number"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoBookmark = new TableInfo("bookmark", _columnsBookmark, _foreignKeysBookmark, _indicesBookmark);
        final TableInfo _existingBookmark = TableInfo.read(connection, "bookmark");
        if (!_infoBookmark.equals(_existingBookmark)) {
          return new RoomOpenDelegate.ValidationResult(false, "bookmark(com.easycomic.data.database.entity.BookmarkEntity).\n"
                  + " Expected:\n" + _infoBookmark + "\n"
                  + " Found:\n" + _existingBookmark);
        }
        final Map<String, TableInfo.Column> _columnsReadingHistory = new HashMap<String, TableInfo.Column>(8);
        _columnsReadingHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("manga_id", new TableInfo.Column("manga_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("page_number", new TableInfo.Column("page_number", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("reading_time", new TableInfo.Column("reading_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("session_start", new TableInfo.Column("session_start", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("session_end", new TableInfo.Column("session_end", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("reading_speed", new TableInfo.Column("reading_speed", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysReadingHistory = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReadingHistory.add(new TableInfo.ForeignKey("manga", "CASCADE", "NO ACTION", Arrays.asList("manga_id"), Arrays.asList("id")));
        final Set<TableInfo.Index> _indicesReadingHistory = new HashSet<TableInfo.Index>(2);
        _indicesReadingHistory.add(new TableInfo.Index("idx_history_manga_id", false, Arrays.asList("manga_id"), Arrays.asList("ASC")));
        _indicesReadingHistory.add(new TableInfo.Index("idx_history_session", false, Arrays.asList("session_start"), Arrays.asList("ASC")));
        final TableInfo _infoReadingHistory = new TableInfo("reading_history", _columnsReadingHistory, _foreignKeysReadingHistory, _indicesReadingHistory);
        final TableInfo _existingReadingHistory = TableInfo.read(connection, "reading_history");
        if (!_infoReadingHistory.equals(_existingReadingHistory)) {
          return new RoomOpenDelegate.ValidationResult(false, "reading_history(com.easycomic.data.database.entity.ReadingHistoryEntity).\n"
                  + " Expected:\n" + _infoReadingHistory + "\n"
                  + " Found:\n" + _existingReadingHistory);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "manga", "bookmark", "reading_history");
  }

  @Override
  public void clearAllTables() {
    super.performClear(true, "manga", "bookmark", "reading_history");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MangaDao.class, MangaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BookmarkDao.class, BookmarkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReadingHistoryDao.class, ReadingHistoryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MangaDao mangaDao() {
    if (_mangaDao != null) {
      return _mangaDao;
    } else {
      synchronized(this) {
        if(_mangaDao == null) {
          _mangaDao = new MangaDao_Impl(this);
        }
        return _mangaDao;
      }
    }
  }

  @Override
  public BookmarkDao bookmarkDao() {
    if (_bookmarkDao != null) {
      return _bookmarkDao;
    } else {
      synchronized(this) {
        if(_bookmarkDao == null) {
          _bookmarkDao = new BookmarkDao_Impl(this);
        }
        return _bookmarkDao;
      }
    }
  }

  @Override
  public ReadingHistoryDao readingHistoryDao() {
    if (_readingHistoryDao != null) {
      return _readingHistoryDao;
    } else {
      synchronized(this) {
        if(_readingHistoryDao == null) {
          _readingHistoryDao = new ReadingHistoryDao_Impl(this);
        }
        return _readingHistoryDao;
      }
    }
  }
}
