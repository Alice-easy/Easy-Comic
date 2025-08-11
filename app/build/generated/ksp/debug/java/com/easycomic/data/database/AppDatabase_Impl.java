package com.easycomic.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.data.dao.BookmarkDao_Impl;
import com.easycomic.data.dao.MangaDao;
import com.easycomic.data.dao.MangaDao_Impl;
import com.easycomic.data.dao.ReadingHistoryDao;
import com.easycomic.data.dao.ReadingHistoryDao_Impl;
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
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MangaDao _mangaDao;

  private volatile BookmarkDao _bookmarkDao;

  private volatile ReadingHistoryDao _readingHistoryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `manga` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `description` TEXT NOT NULL, `file_path` TEXT NOT NULL, `file_uri` TEXT, `file_format` TEXT NOT NULL, `file_size` INTEGER NOT NULL, `page_count` INTEGER NOT NULL, `current_page` INTEGER NOT NULL, `cover_image_path` TEXT, `thumbnail_path` TEXT, `rating` REAL NOT NULL, `is_favorite` INTEGER NOT NULL, `reading_status` TEXT NOT NULL, `tags` TEXT NOT NULL, `last_read` INTEGER NOT NULL, `date_added` INTEGER NOT NULL, `date_modified` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_manga_title` ON `manga` (`title`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_manga_author` ON `manga` (`author`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_manga_last_read` ON `manga` (`last_read`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_manga_date_added` ON `manga` (`date_added`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_manga_is_favorite` ON `manga` (`is_favorite`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `bookmark` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga_id` INTEGER NOT NULL, `page_number` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, FOREIGN KEY(`manga_id`) REFERENCES `manga`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_manga_id` ON `bookmark` (`manga_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_page_number` ON `bookmark` (`page_number`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_created_at` ON `bookmark` (`created_at`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reading_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manga_id` INTEGER NOT NULL, `page_number` INTEGER NOT NULL, `progress_percentage` REAL NOT NULL, `reading_duration` INTEGER NOT NULL, `read_at` INTEGER NOT NULL, `session_id` TEXT NOT NULL, FOREIGN KEY(`manga_id`) REFERENCES `manga`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_history_manga_id` ON `reading_history` (`manga_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_history_read_at` ON `reading_history` (`read_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reading_history_reading_duration` ON `reading_history` (`reading_duration`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd7247e9096dd734b049427a8e137a9b6')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `manga`");
        db.execSQL("DROP TABLE IF EXISTS `bookmark`");
        db.execSQL("DROP TABLE IF EXISTS `reading_history`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsManga = new HashMap<String, TableInfo.Column>(19);
        _columnsManga.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("author", new TableInfo.Column("author", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_path", new TableInfo.Column("file_path", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_uri", new TableInfo.Column("file_uri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_format", new TableInfo.Column("file_format", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("file_size", new TableInfo.Column("file_size", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("page_count", new TableInfo.Column("page_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("current_page", new TableInfo.Column("current_page", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("cover_image_path", new TableInfo.Column("cover_image_path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("thumbnail_path", new TableInfo.Column("thumbnail_path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("rating", new TableInfo.Column("rating", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("is_favorite", new TableInfo.Column("is_favorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("reading_status", new TableInfo.Column("reading_status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("last_read", new TableInfo.Column("last_read", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("date_added", new TableInfo.Column("date_added", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsManga.put("date_modified", new TableInfo.Column("date_modified", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysManga = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesManga = new HashSet<TableInfo.Index>(5);
        _indicesManga.add(new TableInfo.Index("index_manga_title", false, Arrays.asList("title"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("index_manga_author", false, Arrays.asList("author"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("index_manga_last_read", false, Arrays.asList("last_read"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("index_manga_date_added", false, Arrays.asList("date_added"), Arrays.asList("ASC")));
        _indicesManga.add(new TableInfo.Index("index_manga_is_favorite", false, Arrays.asList("is_favorite"), Arrays.asList("ASC")));
        final TableInfo _infoManga = new TableInfo("manga", _columnsManga, _foreignKeysManga, _indicesManga);
        final TableInfo _existingManga = TableInfo.read(db, "manga");
        if (!_infoManga.equals(_existingManga)) {
          return new RoomOpenHelper.ValidationResult(false, "manga(com.easycomic.data.entity.MangaEntity).\n"
                  + " Expected:\n" + _infoManga + "\n"
                  + " Found:\n" + _existingManga);
        }
        final HashMap<String, TableInfo.Column> _columnsBookmark = new HashMap<String, TableInfo.Column>(7);
        _columnsBookmark.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("manga_id", new TableInfo.Column("manga_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("page_number", new TableInfo.Column("page_number", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBookmark.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBookmark = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysBookmark.add(new TableInfo.ForeignKey("manga", "CASCADE", "NO ACTION", Arrays.asList("manga_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesBookmark = new HashSet<TableInfo.Index>(3);
        _indicesBookmark.add(new TableInfo.Index("index_bookmark_manga_id", false, Arrays.asList("manga_id"), Arrays.asList("ASC")));
        _indicesBookmark.add(new TableInfo.Index("index_bookmark_page_number", false, Arrays.asList("page_number"), Arrays.asList("ASC")));
        _indicesBookmark.add(new TableInfo.Index("index_bookmark_created_at", false, Arrays.asList("created_at"), Arrays.asList("ASC")));
        final TableInfo _infoBookmark = new TableInfo("bookmark", _columnsBookmark, _foreignKeysBookmark, _indicesBookmark);
        final TableInfo _existingBookmark = TableInfo.read(db, "bookmark");
        if (!_infoBookmark.equals(_existingBookmark)) {
          return new RoomOpenHelper.ValidationResult(false, "bookmark(com.easycomic.data.entity.BookmarkEntity).\n"
                  + " Expected:\n" + _infoBookmark + "\n"
                  + " Found:\n" + _existingBookmark);
        }
        final HashMap<String, TableInfo.Column> _columnsReadingHistory = new HashMap<String, TableInfo.Column>(7);
        _columnsReadingHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("manga_id", new TableInfo.Column("manga_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("page_number", new TableInfo.Column("page_number", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("progress_percentage", new TableInfo.Column("progress_percentage", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("reading_duration", new TableInfo.Column("reading_duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("read_at", new TableInfo.Column("read_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReadingHistory.put("session_id", new TableInfo.Column("session_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReadingHistory = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReadingHistory.add(new TableInfo.ForeignKey("manga", "CASCADE", "NO ACTION", Arrays.asList("manga_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReadingHistory = new HashSet<TableInfo.Index>(3);
        _indicesReadingHistory.add(new TableInfo.Index("index_reading_history_manga_id", false, Arrays.asList("manga_id"), Arrays.asList("ASC")));
        _indicesReadingHistory.add(new TableInfo.Index("index_reading_history_read_at", false, Arrays.asList("read_at"), Arrays.asList("ASC")));
        _indicesReadingHistory.add(new TableInfo.Index("index_reading_history_reading_duration", false, Arrays.asList("reading_duration"), Arrays.asList("ASC")));
        final TableInfo _infoReadingHistory = new TableInfo("reading_history", _columnsReadingHistory, _foreignKeysReadingHistory, _indicesReadingHistory);
        final TableInfo _existingReadingHistory = TableInfo.read(db, "reading_history");
        if (!_infoReadingHistory.equals(_existingReadingHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "reading_history(com.easycomic.data.entity.ReadingHistoryEntity).\n"
                  + " Expected:\n" + _infoReadingHistory + "\n"
                  + " Found:\n" + _existingReadingHistory);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d7247e9096dd734b049427a8e137a9b6", "594a09e1ceeed4d1e92631aff0e6872b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "manga","bookmark","reading_history");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `manga`");
      _db.execSQL("DELETE FROM `bookmark`");
      _db.execSQL("DELETE FROM `reading_history`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MangaDao.class, MangaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BookmarkDao.class, BookmarkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReadingHistoryDao.class, ReadingHistoryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
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
