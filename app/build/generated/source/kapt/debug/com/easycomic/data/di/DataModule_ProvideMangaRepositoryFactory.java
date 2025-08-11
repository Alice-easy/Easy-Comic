package com.easycomic.data.di;

import com.easycomic.data.dao.MangaDao;
import com.easycomic.domain.repository.MangaRepository;
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
public final class DataModule_ProvideMangaRepositoryFactory implements Factory<MangaRepository> {
  private final Provider<MangaDao> mangaDaoProvider;

  public DataModule_ProvideMangaRepositoryFactory(Provider<MangaDao> mangaDaoProvider) {
    this.mangaDaoProvider = mangaDaoProvider;
  }

  @Override
  public MangaRepository get() {
    return provideMangaRepository(mangaDaoProvider.get());
  }

  public static DataModule_ProvideMangaRepositoryFactory create(
      Provider<MangaDao> mangaDaoProvider) {
    return new DataModule_ProvideMangaRepositoryFactory(mangaDaoProvider);
  }

  public static MangaRepository provideMangaRepository(MangaDao mangaDao) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideMangaRepository(mangaDao));
  }
}
