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
import com.easycomic.data.entity.ReadingHistoryEntity;
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
public final class ReadingHistoryDao_Impl implements ReadingHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReadingHistoryEntity> __insertionAdapterOfReadingHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<ReadingHistoryEntity> __deletionAdapterOfReadingHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<ReadingHistoryEntity> __updateAdapterOfReadingHistoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteReadingHistoryById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteReadingHistoryByMangaId;

  private final SharedSQLiteStatement __preparedStmtOfCleanupOldReadingHistory;

  public ReadingHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReadingHistoryEntity = new EntityInsertionAdapter<ReadingHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reading_history` (`id`,`manga_id`,`page_number`,`progress_percentage`,`reading_duration`,`read_at`,`session_id`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReadingHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMangaId());
        statement.bindLong(3, entity.getPageNumber());
        statement.bindDouble(4, entity.getProgressPercentage());
        statement.bindLong(5, entity.getReadingDuration());
        statement.bindLong(6, entity.getReadAt());
        statement.bindString(7, entity.getSessionId());
      }
    };
    this.__deletionAdapterOfReadingHistoryEntity = new EntityDeletionOrUpdateAdapter<ReadingHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `reading_history` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReadingHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfReadingHistoryEntity = new EntityDeletionOrUpdateAdapter<ReadingHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reading_history` SET `id` = ?,`manga_id` = ?,`page_number` = ?,`progress_percentage` = ?,`reading_duration` = ?,`read_at` = ?,`session_id` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReadingHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMangaId());
        statement.bindLong(3, entity.getPageNumber());
        statement.bindDouble(4, entity.getProgressPercentage());
        statement.bindLong(5, entity.getReadingDuration());
        statement.bindLong(6, entity.getReadAt());
        statement.bindString(7, entity.getSessionId());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteReadingHistoryById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reading_history WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteReadingHistoryByMangaId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM reading_history WHERE manga_id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfCleanupOldReadingHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        DELETE FROM reading_history \n"
                + "        WHERE read_at < ?\n"
                + "    ";
        return _query;
      }
    };
  }

  @Override
  public Object insertReadingHistory(final ReadingHistoryEntity history,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfReadingHistoryEntity.insertAndReturnId(history);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllReadingHistory(final List<ReadingHistoryEntity> historyList,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfReadingHistoryEntity.insertAndReturnIdsList(historyList);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteReadingHistory(final ReadingHistoryEntity history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReadingHistoryEntity.handle(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllReadingHistory(final List<ReadingHistoryEntity> historyList,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfReadingHistoryEntity.handleMultiple(historyList);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateReadingHistory(final ReadingHistoryEntity history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReadingHistoryEntity.handle(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteReadingHistoryById(final long historyId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteReadingHistoryById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, historyId);
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
          __preparedStmtOfDeleteReadingHistoryById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteReadingHistoryByMangaId(final long mangaId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteReadingHistoryByMangaId.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeleteReadingHistoryByMangaId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object cleanupOldReadingHistory(final long cutoffTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCleanupOldReadingHistory.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, cutoffTime);
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
          __preparedStmtOfCleanupOldReadingHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReadingHistoryEntity>> getReadingHistoryByMangaId(final long mangaId) {
    final String _sql = "\n"
            + "        SELECT * FROM reading_history \n"
            + "        WHERE manga_id = ? \n"
            + "        ORDER BY read_at DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mangaId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reading_history"}, new Callable<List<ReadingHistoryEntity>>() {
      @Override
      @NonNull
      public List<ReadingHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMangaId = CursorUtil.getColumnIndexOrThrow(_cursor, "manga_id");
          final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "page_number");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progress_percentage");
          final int _cursorIndexOfReadingDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_duration");
          final int _cursorIndexOfReadAt = CursorUtil.getColumnIndexOrThrow(_cursor, "read_at");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final List<ReadingHistoryEntity> _result = new ArrayList<ReadingHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReadingHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMangaId;
            _tmpMangaId = _cursor.getLong(_cursorIndexOfMangaId);
            final int _tmpPageNumber;
            _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
            final float _tmpProgressPercentage;
            _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
            final long _tmpReadingDuration;
            _tmpReadingDuration = _cursor.getLong(_cursorIndexOfReadingDuration);
            final long _tmpReadAt;
            _tmpReadAt = _cursor.getLong(_cursorIndexOfReadAt);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            _item = new ReadingHistoryEntity(_tmpId,_tmpMangaId,_tmpPageNumber,_tmpProgressPercentage,_tmpReadingDuration,_tmpReadAt,_tmpSessionId);
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
  public Flow<List<ReadingHistoryEntity>> getRecentReadingHistory(final int limit) {
    final String _sql = "\n"
            + "        SELECT * FROM reading_history \n"
            + "        ORDER BY read_at DESC \n"
            + "        LIMIT ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reading_history"}, new Callable<List<ReadingHistoryEntity>>() {
      @Override
      @NonNull
      public List<ReadingHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMangaId = CursorUtil.getColumnIndexOrThrow(_cursor, "manga_id");
          final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "page_number");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progress_percentage");
          final int _cursorIndexOfReadingDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_duration");
          final int _cursorIndexOfReadAt = CursorUtil.getColumnIndexOrThrow(_cursor, "read_at");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final List<ReadingHistoryEntity> _result = new ArrayList<ReadingHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReadingHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMangaId;
            _tmpMangaId = _cursor.getLong(_cursorIndexOfMangaId);
            final int _tmpPageNumber;
            _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
            final float _tmpProgressPercentage;
            _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
            final long _tmpReadingDuration;
            _tmpReadingDuration = _cursor.getLong(_cursorIndexOfReadingDuration);
            final long _tmpReadAt;
            _tmpReadAt = _cursor.getLong(_cursorIndexOfReadAt);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            _item = new ReadingHistoryEntity(_tmpId,_tmpMangaId,_tmpPageNumber,_tmpProgressPercentage,_tmpReadingDuration,_tmpReadAt,_tmpSessionId);
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
  public Object getLatestReadingHistory(final long mangaId,
      final Continuation<? super ReadingHistoryEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM reading_history \n"
            + "        WHERE manga_id = ? \n"
            + "        ORDER BY read_at DESC \n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mangaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReadingHistoryEntity>() {
      @Override
      @Nullable
      public ReadingHistoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMangaId = CursorUtil.getColumnIndexOrThrow(_cursor, "manga_id");
          final int _cursorIndexOfPageNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "page_number");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progress_percentage");
          final int _cursorIndexOfReadingDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "reading_duration");
          final int _cursorIndexOfReadAt = CursorUtil.getColumnIndexOrThrow(_cursor, "read_at");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "session_id");
          final ReadingHistoryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpMangaId;
            _tmpMangaId = _cursor.getLong(_cursorIndexOfMangaId);
            final int _tmpPageNumber;
            _tmpPageNumber = _cursor.getInt(_cursorIndexOfPageNumber);
            final float _tmpProgressPercentage;
            _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
            final long _tmpReadingDuration;
            _tmpReadingDuration = _cursor.getLong(_cursorIndexOfReadingDuration);
            final long _tmpReadAt;
            _tmpReadAt = _cursor.getLong(_cursorIndexOfReadAt);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            _result = new ReadingHistoryEntity(_tmpId,_tmpMangaId,_tmpPageNumber,_tmpProgressPercentage,_tmpReadingDuration,_tmpReadAt,_tmpSessionId);
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
  public Flow<Integer> getReadingHistoryCount() {
    final String _sql = "SELECT COUNT(*) FROM reading_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reading_history"}, new Callable<Integer>() {
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
  public Object getReadingHistoryCountByMangaId(final long mangaId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM reading_history WHERE manga_id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mangaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Long> getTotalReadingDuration() {
    final String _sql = "SELECT SUM(reading_duration) FROM reading_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reading_history"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public Object getReadingDurationByMangaId(final long mangaId,
      final Continuation<? super Long> $completion) {
    final String _sql = "\n"
            + "        SELECT SUM(reading_duration) \n"
            + "        FROM reading_history \n"
            + "        WHERE manga_id = ?\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mangaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
