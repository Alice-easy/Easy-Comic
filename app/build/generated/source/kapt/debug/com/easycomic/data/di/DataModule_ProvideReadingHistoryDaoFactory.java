package com.easycomic.data.di;

import com.easycomic.data.dao.ReadingHistoryDao;
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
public final class DataModule_ProvideReadingHistoryDaoFactory implements Factory<ReadingHistoryDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideReadingHistoryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ReadingHistoryDao get() {
    return provideReadingHistoryDao(databaseProvider.get());
  }

  public static DataModule_ProvideReadingHistoryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideReadingHistoryDaoFactory(databaseProvider);
  }

  public static ReadingHistoryDao provideReadingHistoryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideReadingHistoryDao(database));
  }
}
