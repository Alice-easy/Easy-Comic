package com.easycomic.di;

import com.easycomic.data.database.AppDatabase;
import com.easycomic.data.database.dao.BookmarkDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideBookmarkDaoFactory implements Factory<BookmarkDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideBookmarkDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BookmarkDao get() {
    return provideBookmarkDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideBookmarkDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideBookmarkDaoFactory(dbProvider);
  }

  public static BookmarkDao provideBookmarkDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBookmarkDao(db));
  }
}
