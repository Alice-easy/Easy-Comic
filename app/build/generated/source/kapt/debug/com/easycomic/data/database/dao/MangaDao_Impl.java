package com.easycomic.data.database.dao;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.coroutines.FlowUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.easycomic.data.database.entity.MangaEntity;
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
public final class MangaDao_Impl implements MangaDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<MangaEntity> __insertAdapterOfMangaEntity;

  private final EntityDeleteOrUpdateAdapter<MangaEntity> __deleteAdapterOfMangaEntity;

  private final EntityDeleteOrUpdateAdapter<MangaEntity> __updateAdapterOfMangaEntity;

  public MangaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfMangaEntity = new EntityInsertAdapter<MangaEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `manga` (`id`,`title`,`author`,`description`,`file_path`,`file_size`,`format`,`cover_path`,`page_count`,`current_page`,`reading_progress`,`is_favorite`,`is_completed`,`date_added`,`last_read`,`reading_time`,`rating`,`created_at`,`updated_at`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MangaEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getAuthor() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getAuthor());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getDescription());
        }
        if (entity.getFilePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getFilePath());
        }
        statement.bindLong(6, entity.getFileSize());
        if (entity.getFormat() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getFormat());
        }
        if (entity.getCoverPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getCoverPath());
        }
        statement.bindLong(9, entity.getPageCount());
        statement.bindLong(10, entity.getCurrentPage());
        statement.bindDouble(11, entity.getReadingProgress());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindLong(14, entity.getDateAdded());
        if (entity.getLastRead() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getLastRead());
        }
        statement.bindLong(16, entity.getReadingTime());
        statement.bindDouble(17, entity.getRating());
        statement.bindLong(18, entity.getCreatedAt());
        statement.bindLong(19, entity.getUpdatedAt());
      }
    };
    this.__deleteAdapterOfMangaEntity = new EntityDeleteOrUpdateAdapter<MangaEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `manga` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MangaEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMangaEntity = new EntityDeleteOrUpdateAdapter<MangaEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `manga` SET `id` = ?,`title` = ?,`author` = ?,`description` = ?,`file_path` = ?,`file_size` = ?,`format` = ?,`cover_path` = ?,`page_count` = ?,`current_page` = ?,`reading_progress` = ?,`is_favorite` = ?,`is_completed` = ?,`date_added` = ?,`last_read` = ?,`reading_time` = ?,`rating` = ?,`created_at` = ?,`updated_at` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          @NonNull final MangaEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getTitle());
        }
        if (entity.getAuthor() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getAuthor());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getDescription());
        }
        if (entity.getFilePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getFilePath());
        }
        statement.bindLong(6, entity.getFileSize());
        if (entity.getFormat() == null) {
          statement.bindNull(7);
        } else {
          statement.bindText(7, entity.getFormat());
        }
        if (entity.getCoverPath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getCoverPath());
        }
        statement.bindLong(9, entity.getPageCount());
        statement.bindLong(10, entity.getCurrentPage());
        statement.bindDouble(11, entity.getReadingProgress());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(12, _tmp);
        final int _tmp_1 = entity.isCompleted() ? 1 : 0;
        statement.bindLong(13, _tmp_1);
        statement.bindLong(14, entity.getDateAdded());
        if (entity.getLastRead() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getLastRead());
        }
        statement.bindLong(16, entity.getReadingTime());
        statement.bindDouble(17, entity.getRating());
        statement.bindLong(18, entity.getCreatedAt());
        statement.bindLong(19, entity.getUpdatedAt());
        statement.bindLong(20, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final MangaEntity entity, final Continuation<? super Long> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      return __insertAdapterOfMangaEntity.insertAndReturnId(_connection, entity);
    }, $completion);
  }

  @Override
  public Object delete(final MangaEntity entity, final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __deleteAdapterOfMangaEntity.handle(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Object update(final MangaEntity entity, final Continuation<? super Unit> $completion) {
    if (entity == null) throw new NullPointerException();
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      __updateAdapterOfMangaEntity.handle(_connection, entity);
      return Unit.INSTANCE;
    }, $completion);
  }

  @Override
  public Flow<List<MangaEntity>> observeAll() {
    final String _sql = "SELECT * FROM manga ORDER BY (last_read IS NULL) ASC, last_read DESC, date_added DESC";
    return FlowUtil.createFlow(__db, false, new String[] {"manga"}, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfAuthor = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "author");
        final int _columnIndexOfDescription = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "description");
        final int _columnIndexOfFilePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "file_path");
        final int _columnIndexOfFileSize = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "file_size");
        final int _columnIndexOfFormat = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "format");
        final int _columnIndexOfCoverPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "cover_path");
        final int _columnIndexOfPageCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "page_count");
        final int _columnIndexOfCurrentPage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "current_page");
        final int _columnIndexOfReadingProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_progress");
        final int _columnIndexOfIsFavorite = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "is_favorite");
        final int _columnIndexOfIsCompleted = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "is_completed");
        final int _columnIndexOfDateAdded = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date_added");
        final int _columnIndexOfLastRead = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "last_read");
        final int _columnIndexOfReadingTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_time");
        final int _columnIndexOfRating = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rating");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updated_at");
        final List<MangaEntity> _result = new ArrayList<MangaEntity>();
        while (_stmt.step()) {
          final MangaEntity _item;
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpAuthor;
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null;
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor);
          }
          final String _tmpDescription;
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null;
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription);
          }
          final String _tmpFilePath;
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null;
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath);
          }
          final long _tmpFileSize;
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize);
          final String _tmpFormat;
          if (_stmt.isNull(_columnIndexOfFormat)) {
            _tmpFormat = null;
          } else {
            _tmpFormat = _stmt.getText(_columnIndexOfFormat);
          }
          final String _tmpCoverPath;
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null;
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath);
          }
          final int _tmpPageCount;
          _tmpPageCount = (int) (_stmt.getLong(_columnIndexOfPageCount));
          final int _tmpCurrentPage;
          _tmpCurrentPage = (int) (_stmt.getLong(_columnIndexOfCurrentPage));
          final float _tmpReadingProgress;
          _tmpReadingProgress = (float) (_stmt.getDouble(_columnIndexOfReadingProgress));
          final boolean _tmpIsFavorite;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsFavorite));
          _tmpIsFavorite = _tmp != 0;
          final boolean _tmpIsCompleted;
          final int _tmp_1;
          _tmp_1 = (int) (_stmt.getLong(_columnIndexOfIsCompleted));
          _tmpIsCompleted = _tmp_1 != 0;
          final long _tmpDateAdded;
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded);
          final Long _tmpLastRead;
          if (_stmt.isNull(_columnIndexOfLastRead)) {
            _tmpLastRead = null;
          } else {
            _tmpLastRead = _stmt.getLong(_columnIndexOfLastRead);
          }
          final long _tmpReadingTime;
          _tmpReadingTime = _stmt.getLong(_columnIndexOfReadingTime);
          final float _tmpRating;
          _tmpRating = (float) (_stmt.getDouble(_columnIndexOfRating));
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _item = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileSize,_tmpFormat,_tmpCoverPath,_tmpPageCount,_tmpCurrentPage,_tmpReadingProgress,_tmpIsFavorite,_tmpIsCompleted,_tmpDateAdded,_tmpLastRead,_tmpReadingTime,_tmpRating,_tmpCreatedAt,_tmpUpdatedAt);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super MangaEntity> $completion) {
    final String _sql = "SELECT * FROM manga WHERE id = ?";
    return DBUtil.performSuspending(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfTitle = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "title");
        final int _columnIndexOfAuthor = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "author");
        final int _columnIndexOfDescription = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "description");
        final int _columnIndexOfFilePath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "file_path");
        final int _columnIndexOfFileSize = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "file_size");
        final int _columnIndexOfFormat = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "format");
        final int _columnIndexOfCoverPath = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "cover_path");
        final int _columnIndexOfPageCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "page_count");
        final int _columnIndexOfCurrentPage = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "current_page");
        final int _columnIndexOfReadingProgress = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_progress");
        final int _columnIndexOfIsFavorite = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "is_favorite");
        final int _columnIndexOfIsCompleted = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "is_completed");
        final int _columnIndexOfDateAdded = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "date_added");
        final int _columnIndexOfLastRead = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "last_read");
        final int _columnIndexOfReadingTime = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "reading_time");
        final int _columnIndexOfRating = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rating");
        final int _columnIndexOfCreatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "created_at");
        final int _columnIndexOfUpdatedAt = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "updated_at");
        final MangaEntity _result;
        if (_stmt.step()) {
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          final String _tmpTitle;
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle);
          }
          final String _tmpAuthor;
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null;
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor);
          }
          final String _tmpDescription;
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null;
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription);
          }
          final String _tmpFilePath;
          if (_stmt.isNull(_columnIndexOfFilePath)) {
            _tmpFilePath = null;
          } else {
            _tmpFilePath = _stmt.getText(_columnIndexOfFilePath);
          }
          final long _tmpFileSize;
          _tmpFileSize = _stmt.getLong(_columnIndexOfFileSize);
          final String _tmpFormat;
          if (_stmt.isNull(_columnIndexOfFormat)) {
            _tmpFormat = null;
          } else {
            _tmpFormat = _stmt.getText(_columnIndexOfFormat);
          }
          final String _tmpCoverPath;
          if (_stmt.isNull(_columnIndexOfCoverPath)) {
            _tmpCoverPath = null;
          } else {
            _tmpCoverPath = _stmt.getText(_columnIndexOfCoverPath);
          }
          final int _tmpPageCount;
          _tmpPageCount = (int) (_stmt.getLong(_columnIndexOfPageCount));
          final int _tmpCurrentPage;
          _tmpCurrentPage = (int) (_stmt.getLong(_columnIndexOfCurrentPage));
          final float _tmpReadingProgress;
          _tmpReadingProgress = (float) (_stmt.getDouble(_columnIndexOfReadingProgress));
          final boolean _tmpIsFavorite;
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsFavorite));
          _tmpIsFavorite = _tmp != 0;
          final boolean _tmpIsCompleted;
          final int _tmp_1;
          _tmp_1 = (int) (_stmt.getLong(_columnIndexOfIsCompleted));
          _tmpIsCompleted = _tmp_1 != 0;
          final long _tmpDateAdded;
          _tmpDateAdded = _stmt.getLong(_columnIndexOfDateAdded);
          final Long _tmpLastRead;
          if (_stmt.isNull(_columnIndexOfLastRead)) {
            _tmpLastRead = null;
          } else {
            _tmpLastRead = _stmt.getLong(_columnIndexOfLastRead);
          }
          final long _tmpReadingTime;
          _tmpReadingTime = _stmt.getLong(_columnIndexOfReadingTime);
          final float _tmpRating;
          _tmpRating = (float) (_stmt.getDouble(_columnIndexOfRating));
          final long _tmpCreatedAt;
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt);
          final long _tmpUpdatedAt;
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt);
          _result = new MangaEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpDescription,_tmpFilePath,_tmpFileSize,_tmpFormat,_tmpCoverPath,_tmpPageCount,_tmpCurrentPage,_tmpReadingProgress,_tmpIsFavorite,_tmpIsCompleted,_tmpDateAdded,_tmpLastRead,_tmpReadingTime,_tmpRating,_tmpCreatedAt,_tmpUpdatedAt);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object updateProgress(final long id, final int page, final float progress,
      final long lastRead, final long updatedAt, final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE manga SET current_page = ?, reading_progress = ?, last_read = ?, updated_at = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, page);
        _argIndex = 2;
        _stmt.bindDouble(_argIndex, progress);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, lastRead);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 5;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @Override
  public Object updateFavorite(final long id, final boolean favorite, final long updatedAt,
      final Continuation<? super Unit> $completion) {
    final String _sql = "UPDATE manga SET is_favorite = ?, updated_at = ? WHERE id = ?";
    return DBUtil.performSuspending(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        final int _tmp = favorite ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, updatedAt);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return Unit.INSTANCE;
      } finally {
        _stmt.close();
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
