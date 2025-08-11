package com.easycomic.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.easycomic.data.database.Converters;
import com.easycomic.data.entity.MangaEntity;
import com.easycomic.data.entity.ReadingStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MangaDao_Impl implements MangaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MangaEntity> __insertionAdapterOfMangaEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<MangaEntity> __deletionAdapterOfMangaEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateCurrentPage;

  private final SharedSQLiteStatement __preparedStmtOfUpdateReadingProgress;

  private final SharedSQLiteStatement __preparedStmtOfToggleFavorite;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRating;

  public MangaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMangaEntity = new EntityInsertionAdapter<MangaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `manga` (`id`,`title`,`author`,`description`,`file_path`,`file_uri`,`file_format`,`file_size`,`page_count`,`current_page`,`cover_image_path`,`thumbnail_path`,`rating`,`is_favorite`,`reading_status`,`tags`,`last_read`,`date_added`,`date_modified`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MangaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getAuthor());
        statement.bindString(4, entity.getDescription());
        statement.bindString(5, entity.getFilePath());
        if (entity.getFileUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFileUri());
        }
        statement.bindString(7, entity.getFileFormat());
        statement.bindLong(8, entity.getFileSize());
        statement.bindLong(9, entity.getPageCount());
        statement.bindLong(10, entity.getCurrentPage());
        if (entity.getCoverImagePath() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getCoverImagePath());
        }
        if (entity.getThumbnailPath() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getThumbnailPath());
        }
        statement.bindDouble(13, entity.getRating());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(14, _tmp);
        final String _tmp_1 = __converters.fromReadingStatus(entity.getReadingStatus());
        statement.bindString(15, _tmp_1);
        statement.bindString(16, entity.getTags());
        statement.bindLong(17, entity.getLastRead());
        statement.bindLong(18, entity.getDateAdded());
        statement.bindLong(19, entity.getDateModified());
      }
    };
    this.__deletionAdapterOfMangaEntity = new EntityDeletionOrUpdateAdapter<MangaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `manga` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MangaEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateCurrentPage = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE manga \n"
                + "        SET current_page = ?, \n"
                + "            last_read = ? \n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateReadingProgress = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE manga \n"
                + "        SET current_page = ?, \n"
                + "            reading_status = ?,\n"
                + "            last_read = ? \n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfToggleFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE manga \n"
                + "        SET is_favorite = NOT is_favorite, \n"
                + "            date_modified = ? \n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateRating = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE manga \n"
                + "        SET rating = ?, \n"
                + "            date_modified = ? \n"
                + "        WHERE id = ?\n"
                + "    ";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdateManga(final MangaEntity manga,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMangaEntity.insertAndReturnId(manga);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllManga(final List<MangaEntity> mangaList,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfMangaEntity.insertAndReturnIdsList(mangaList);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteManga(final MangaEntity manga, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMangaEntity.handle(manga);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllManga(final List<MangaEntity> mangaList,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMangaEntity.handleMultiple(mangaList);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateCurrentPage(final long mangaId, final int currentPage, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateCurrentPage.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentPage);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, mangaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateCurrentPage.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateReadingProgress(final long mangaId, final int currentPage,
      final ReadingStatus status, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateReadingProgress.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, currentPage);
        _argIndex = 2;
        final String _tmp = __converters.fromReadingStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, mangaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateReadingProgress.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object toggleFavorite(final long mangaId, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfToggleFavorite.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, mangaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfToggleFavorite.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRating(final long mangaId, final float rating, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRating.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, rating);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, mangaId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateRating.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MangaEntity>> getAllManga() {
    final String _sql = "\n"
            + "        SELECT `manga`.`id` AS `id`, `manga`.`title` AS `title`, `manga`.`author` AS `author`, `manga`.`description` AS `description`, `manga`.`file_path` AS `file_path`, `manga`.`file_uri` AS `file_uri`, `manga`.`file_format` AS `file_format`, `manga`.`file_size` AS `file_size`, `manga`.`page_count` AS `page_count`, `manga`.`current_page` AS `current_page`, `manga`.`cover_image_path` AS `cover_image_path`, `manga`.`thumbnail_path` AS `thumbnail_path`, `manga`.`rating` AS `rating`, `manga`.`is_favorite` AS `is_favorite`, `manga`.`reading_status` AS `reading_status`, `manga`.`tags` AS `tags`, `manga`.`last_read` AS `last_read`, `manga`.`date_added` AS `date_added`, `manga`.`date_modified` AS `date_modified` FROM manga \n"
            + "        ORDER BY date_added DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<List<MangaEntity>>() {
      @Override
      @NonNull
      public List<MangaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfTitle = 1;
          final int _cursorIndexOfAuthor = 2;
          final int _cursorIndexOfDescription = 3;
          final int _cursorIndexOfFilePath = 4;
          final int _cursorIndexOfFileUri = 5;
          final int _cursorIndexOfFileFormat = 6;
          final int _cursorIndexOfFileSize = 7;
          final int _cursorIndexOfPageCount = 8;
          final int _cursorIndexOfCurrentPage = 9;
          final int _cursorIndexOfCoverImagePath = 10;
          final int _cursorIndexOfThumbnailPath = 11;
          final int _cursorIndexOfRating = 12;
          final int _cursorIndexOfIsFavorite = 13;
          final int _cursorIndexOfReadingStatus = 14;
          final int _cursorIndexOfTags = 15;
          final int _cursorIndexOfLastRead = 16;
          final int _cursorIndexOfDateAdded = 17;
          final int _cursorIndexOfDateModified = 18;
          final List<MangaEntity> _result = new ArrayList<MangaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MangaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMangaById(final long mangaId,
      final Continuation<? super MangaEntity> $completion) {
    final String _sql = "SELECT * FROM manga WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mangaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MangaEntity>() {
      @Override
      @Nullable
      public MangaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "file_path");
          final int _cursorIndexOfFileUri = CursorUtil.getColumnIndexOrThrow(_cursor, "file_uri");
          final int _cursorIndexOfFileFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "file_format");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "file_size");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "page_count");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "current_page");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_image_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "is_favorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfLastRead = CursorUtil.getColumnIndexOrThrow(_cursor, "last_read");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "date_modified");
          final MangaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _result = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMangaByFilePath(final String filePath,
      final Continuation<? super MangaEntity> $completion) {
    final String _sql = "SELECT * FROM manga WHERE file_path = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, filePath);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MangaEntity>() {
      @Override
      @Nullable
      public MangaEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "file_path");
          final int _cursorIndexOfFileUri = CursorUtil.getColumnIndexOrThrow(_cursor, "file_uri");
          final int _cursorIndexOfFileFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "file_format");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "file_size");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "page_count");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "current_page");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_image_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "is_favorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfLastRead = CursorUtil.getColumnIndexOrThrow(_cursor, "last_read");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "date_modified");
          final MangaEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _result = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MangaEntity>> searchManga(final String query) {
    final String _sql = "\n"
            + "        SELECT * FROM manga \n"
            + "        WHERE title LIKE '%' || ? || '%' \n"
            + "           OR author LIKE '%' || ? || '%' \n"
            + "           OR tags LIKE '%' || ? || '%'\n"
            + "        ORDER BY \n"
            + "            CASE \n"
            + "                WHEN title LIKE ? THEN 1\n"
            + "                WHEN author LIKE ? THEN 2\n"
            + "                ELSE 3\n"
            + "            END,\n"
            + "            title ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    _argIndex = 4;
    _statement.bindString(_argIndex, query);
    _argIndex = 5;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<List<MangaEntity>>() {
      @Override
      @NonNull
      public List<MangaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "file_path");
          final int _cursorIndexOfFileUri = CursorUtil.getColumnIndexOrThrow(_cursor, "file_uri");
          final int _cursorIndexOfFileFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "file_format");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "file_size");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "page_count");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "current_page");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_image_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "is_favorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfLastRead = CursorUtil.getColumnIndexOrThrow(_cursor, "last_read");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "date_modified");
          final List<MangaEntity> _result = new ArrayList<MangaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MangaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MangaEntity>> getFavoriteManga() {
    final String _sql = "\n"
            + "        SELECT `manga`.`id` AS `id`, `manga`.`title` AS `title`, `manga`.`author` AS `author`, `manga`.`description` AS `description`, `manga`.`file_path` AS `file_path`, `manga`.`file_uri` AS `file_uri`, `manga`.`file_format` AS `file_format`, `manga`.`file_size` AS `file_size`, `manga`.`page_count` AS `page_count`, `manga`.`current_page` AS `current_page`, `manga`.`cover_image_path` AS `cover_image_path`, `manga`.`thumbnail_path` AS `thumbnail_path`, `manga`.`rating` AS `rating`, `manga`.`is_favorite` AS `is_favorite`, `manga`.`reading_status` AS `reading_status`, `manga`.`tags` AS `tags`, `manga`.`last_read` AS `last_read`, `manga`.`date_added` AS `date_added`, `manga`.`date_modified` AS `date_modified` FROM manga \n"
            + "        WHERE is_favorite = 1 \n"
            + "        ORDER BY last_read DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<List<MangaEntity>>() {
      @Override
      @NonNull
      public List<MangaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfTitle = 1;
          final int _cursorIndexOfAuthor = 2;
          final int _cursorIndexOfDescription = 3;
          final int _cursorIndexOfFilePath = 4;
          final int _cursorIndexOfFileUri = 5;
          final int _cursorIndexOfFileFormat = 6;
          final int _cursorIndexOfFileSize = 7;
          final int _cursorIndexOfPageCount = 8;
          final int _cursorIndexOfCurrentPage = 9;
          final int _cursorIndexOfCoverImagePath = 10;
          final int _cursorIndexOfThumbnailPath = 11;
          final int _cursorIndexOfRating = 12;
          final int _cursorIndexOfIsFavorite = 13;
          final int _cursorIndexOfReadingStatus = 14;
          final int _cursorIndexOfTags = 15;
          final int _cursorIndexOfLastRead = 16;
          final int _cursorIndexOfDateAdded = 17;
          final int _cursorIndexOfDateModified = 18;
          final List<MangaEntity> _result = new ArrayList<MangaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MangaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MangaEntity>> getMangaByStatus(final ReadingStatus status) {
    final String _sql = "\n"
            + "        SELECT * FROM manga \n"
            + "        WHERE reading_status = ? \n"
            + "        ORDER BY last_read DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromReadingStatus(status);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<List<MangaEntity>>() {
      @Override
      @NonNull
      public List<MangaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "file_path");
          final int _cursorIndexOfFileUri = CursorUtil.getColumnIndexOrThrow(_cursor, "file_uri");
          final int _cursorIndexOfFileFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "file_format");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "file_size");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "page_count");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "current_page");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_image_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "is_favorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfLastRead = CursorUtil.getColumnIndexOrThrow(_cursor, "last_read");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "date_modified");
          final List<MangaEntity> _result = new ArrayList<MangaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MangaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_2);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MangaEntity>> getRecentManga(final int limit) {
    final String _sql = "\n"
            + "        SELECT * FROM manga \n"
            + "        WHERE last_read > 0 \n"
            + "        ORDER BY last_read DESC \n"
            + "        LIMIT ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<List<MangaEntity>>() {
      @Override
      @NonNull
      public List<MangaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "file_path");
          final int _cursorIndexOfFileUri = CursorUtil.getColumnIndexOrThrow(_cursor, "file_uri");
          final int _cursorIndexOfFileFormat = CursorUtil.getColumnIndexOrThrow(_cursor, "file_format");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "file_size");
          final int _cursorIndexOfPageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "page_count");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "current_page");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_image_path");
          final int _cursorIndexOfThumbnailPath = CursorUtil.getColumnIndexOrThrow(_cursor, "thumbnail_path");
          final int _cursorIndexOfRating = CursorUtil.getColumnIndexOrThrow(_cursor, "rating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "is_favorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_status");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfLastRead = CursorUtil.getColumnIndexOrThrow(_cursor, "last_read");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfDateModified = CursorUtil.getColumnIndexOrThrow(_cursor, "date_modified");
          final List<MangaEntity> _result = new ArrayList<MangaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MangaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpFileUri;
            if (_cursor.isNull(_cursorIndexOfFileUri)) {
              _tmpFileUri = null;
            } else {
              _tmpFileUri = _cursor.getString(_cursorIndexOfFileUri);
            }
            final String _tmpFileFormat;
            _tmpFileFormat = _cursor.getString(_cursorIndexOfFileFormat);
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpPageCount;
            _tmpPageCount = _cursor.getInt(_cursorIndexOfPageCount);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpThumbnailPath;
            if (_cursor.isNull(_cursorIndexOfThumbnailPath)) {
              _tmpThumbnailPath = null;
            } else {
              _tmpThumbnailPath = _cursor.getString(_cursorIndexOfThumbnailPath);
            }
            final float _tmpRating;
            _tmpRating = _cursor.getFloat(_cursorIndexOfRating);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final ReadingStatus _tmpReadingStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfReadingStatus);
            _tmpReadingStatus = __converters.toReadingStatus(_tmp_1);
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final long _tmpLastRead;
            _tmpLastRead = _cursor.getLong(_cursorIndexOfLastRead);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpDateModified;
            _tmpDateModified = _cursor.getLong(_cursorIndexOfDateModified);
            _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileUri,_tmpFileFormat,_tmpFileSize,_tmpPageCount,_tmpCurrentPage,_tmpCoverImagePath,_tmpThumbnailPath,_tmpRating,_tmpIsFavorite,_tmpReadingStatus,_tmpTags,_tmpLastRead,_tmpDateAdded,_tmpDateModified);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getMangaCount() {
    final String _sql = "SELECT COUNT(*) FROM manga";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getFavoriteCount() {
    final String _sql = "SELECT COUNT(*) FROM manga WHERE is_favorite = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> getCompletedCount() {
    final String _sql = "SELECT COUNT(*) FROM manga WHERE reading_status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"manga"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
