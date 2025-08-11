package com.easycomic.data.di;

import com.easycomic.data.dao.MangaDao;
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
public final class DataModule_ProvideMangaDaoFactory implements Factory<MangaDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideMangaDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MangaDao get() {
    return provideMangaDao(databaseProvider.get());
  }

  public static DataModule_ProvideMangaDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideMangaDaoFactory(databaseProvider);
  }

  public static MangaDao provideMangaDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideMangaDao(database));
  }
}
