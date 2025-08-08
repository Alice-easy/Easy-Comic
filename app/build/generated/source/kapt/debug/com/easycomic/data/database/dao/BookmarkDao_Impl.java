package com.easycomic.data.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.easycomic.data.database.entity.BookmarkEntity;
import java.lang.Class;
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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class BookmarkDao_Impl implements BookmarkDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<BookmarkEntity> __insertAdapterOfBookmarkEntity;

  private final EntityDeleteOrUpdateAdapter<BookmarkEntity> __deleteAdapterOfBookmarkEntity;

  public BookmarkDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfBookmarkEntity = new EntityInsertAdapter<BookmarkEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bookmark` (`id`,`manga_id`,`page_number`,`bookmark_name`,`notes`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final BookmarkEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getMangaId());
        statement.bindLong(3, entity.getPageNumber());
        if (entity.getBookmarkName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getBookmarkName());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getNotes());
        }
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deleteAdapterOfBookmarkEntity = new EntityDeleteOrUpdateAdapter<BookmarkEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `bookmark` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final BookmarkEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object upsert(final BookmarkEntity entity, final Continuation<? super Long> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfBookmarkEntity.insertAndReturnId(_connection, entity);
    }, $completion);
  }

  @Override
  public Object delete(final BookmarkEntity entity, final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfBookmarkEntity.handle(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<BookmarkEntity>> observeByManga(final long mangaId) {
    final String _sql = "SELECT * FROM bookmark WHERE manga_id = ? ORDER BY page_number ASC";
    return FlowUtil.createFlow(__db, false, new String[] {"bookmark"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, mangaId);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfMangaId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "manga_id");
        final int _columnIndexOfPageNumber = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "page_number");
        final int _columnIndexOfBookmarkName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "bookmark_name");
        final int _columnIndexOfNotes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "notes");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final List<BookmarkEntity> _result = new ArrayList<BookmarkEntity>();
        while (_stmt.step()) {
          final BookmarkEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final long _tmpMangaId;
          _tmpMangaId = _stmt.getLong(_columnIndexOfMangaId);
          final int _tmpPageNumber;
          _tmpPageNumber = (int) (_stmt.getLong(_columnIndexOfPageNumber));
          final String _tmpBookmarkName;
          if (_stmt.isNull(_columnIndexOfBookmarkName)) {
            _tmpBookmarkName = null;
          } else {
            _tmpBookmarkName = _stmt.getText(_columnIndexOfBookmarkName);
          }
          final String _tmpNotes;
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null;
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          _item = new BookmarkEntity(_tmpId,_tmpMangaId,_tmpPageNumber,_tmpBookmarkName,_tmpNotes,_tmpCreatedAt);
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
