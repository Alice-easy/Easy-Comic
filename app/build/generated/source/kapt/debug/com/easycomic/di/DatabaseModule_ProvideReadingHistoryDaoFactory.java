package com.easycomic.di;

import com.easycomic.data.database.AppDatabase;
import com.easycomic.data.database.dao.ReadingHistoryDao;
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
public final class DatabaseModule_ProvideReadingHistoryDaoFactory implements Factory<ReadingHistoryDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideReadingHistoryDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ReadingHistoryDao get() {
    return provideReadingHistoryDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideReadingHistoryDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideReadingHistoryDaoFactory(dbProvider);
  }

  public static ReadingHistoryDao provideReadingHistoryDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideReadingHistoryDao(db));
  }
}
