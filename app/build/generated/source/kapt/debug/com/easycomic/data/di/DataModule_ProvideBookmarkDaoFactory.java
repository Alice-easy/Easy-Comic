package com.easycomic.data.di;

import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.data.database.AppDatabase;
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
public final class DataModule_ProvideBookmarkDaoFactory implements Factory<BookmarkDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideBookmarkDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BookmarkDao get() {
    return provideBookmarkDao(databaseProvider.get());
  }

  public static DataModule_ProvideBookmarkDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideBookmarkDaoFactory(databaseProvider);
  }

  public static BookmarkDao provideBookmarkDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideBookmarkDao(database));
  }
}
