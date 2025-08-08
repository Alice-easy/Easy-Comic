package com.easycomic.data.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.easycomic.data.database.entity.ReadingHistoryEntity;
import java.lang.Class;
import java.lang.Float;
import java.lang.Long;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class ReadingHistoryDao_Impl implements ReadingHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<ReadingHistoryEntity> __insertAdapterOfReadingHistoryEntity;

  public ReadingHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfReadingHistoryEntity = new EntityInsertAdapter<ReadingHistoryEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `reading_history` (`id`,`manga_id`,`page_number`,`reading_time`,`session_start`,`session_end`,`reading_speed`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final ReadingHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMangaId());
        statement.bindLong(3, entity.getPageNumber());
        statement.bindLong(4, entity.getReadingTime());
        statement.bindLong(5, entity.getSessionStart());
        statement.bindLong(6, entity.getSessionEnd());
        if (entity.getReadingSpeed() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getReadingSpeed());
        }
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insert(final ReadingHistoryEntity entity,
      final Continuation<? super Long> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfReadingHistoryEntity.insertAndReturnId(_connection, entity);
    }, $completion);
  }

  @Override
  public Flow<List<ReadingHistoryEntity>> observeByManga(final long mangaId) {
    final String _sql = "SELECT * FROM reading_history WHERE manga_id = ? ORDER BY session_start DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"reading_history"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, mangaId);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfMangaId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "manga_id");
        final int _columnIndexOfPageNumber = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "page_number");
        final int _columnIndexOfReadingTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_time");
        final int _columnIndexOfSessionStart = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "session_start");
        final int _columnIndexOfSessionEnd = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "session_end");
        final int _columnIndexOfReadingSpeed = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_speed");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<ReadingHistoryEntity> _result = new ArrayList<ReadingHistoryEntity>();
        while (_stmt.step()) {
          final ReadingHistoryEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpMangaId;
          _tmpMangaId = _stmt.getLong(_columnIndexOfMangaId);
          final int _tmpPageNumber;
          _tmpPageNumber = (int) (_stmt.getLong(_columnIndexOfPageNumber));
          final long _tmpReadingTime;
          _tmpReadingTime = _stmt.getLong(_columnIndexOfReadingTime);
          final long _tmpSessionStart;
          _tmpSessionStart = _stmt.getLong(_columnIndexOfSessionStart);
          final long _tmpSessionEnd;
          _tmpSessionEnd = _stmt.getLong(_columnIndexOfSessionEnd);
          final Float _tmpReadingSpeed;
          if (_stmt.isNull(_columnIndexOfReadingSpeed)) {
            _tmpReadingSpeed = null;
          } else {
            _tmpReadingSpeed = (float) (_stmt.getDouble(_columnIndexOfReadingSpeed));
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new ReadingHistoryEntity(_tmpId,_tmpMangaId,_tmpPageNumber,_tmpReadingTime,_tmpSessionStart,_tmpSessionEnd,_tmpReadingSpeed,_tmpCreatedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
