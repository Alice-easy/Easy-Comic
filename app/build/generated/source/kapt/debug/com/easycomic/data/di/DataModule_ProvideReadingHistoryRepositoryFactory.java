package com.easycomic.data.di;

import com.easycomic.data.dao.ReadingHistoryDao;
import com.easycomic.domain.repository.ReadingHistoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DataModule_ProvideReadingHistoryRepositoryFactory implements Factory<ReadingHistoryRepository> {
  private final Provider<ReadingHistoryDao> readingHistoryDaoProvider;

  public DataModule_ProvideReadingHistoryRepositoryFactory(
      Provider<ReadingHistoryDao> readingHistoryDaoProvider) {
    this.readingHistoryDaoProvider = readingHistoryDaoProvider;
  }

  @Override
  public ReadingHistoryRepository get() {
    return provideReadingHistoryRepository(readingHistoryDaoProvider.get());
  }

  public static DataModule_ProvideReadingHistoryRepositoryFactory create(
      Provider<ReadingHistoryDao> readingHistoryDaoProvider) {
    return new DataModule_ProvideReadingHistoryRepositoryFactory(readingHistoryDaoProvider);
  }

  public static ReadingHistoryRepository provideReadingHistoryRepository(
      ReadingHistoryDao readingHistoryDao) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideReadingHistoryRepository(readingHistoryDao));
  }
}
