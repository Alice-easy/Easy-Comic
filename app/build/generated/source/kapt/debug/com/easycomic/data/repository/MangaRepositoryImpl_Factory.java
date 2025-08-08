package com.easycomic.data.repository;

import com.easycomic.data.database.dao.MangaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MangaRepositoryImpl_Factory implements Factory<MangaRepositoryImpl> {
  private final Provider<MangaDao> mangaDaoProvider;

  public MangaRepositoryImpl_Factory(Provider<MangaDao> mangaDaoProvider) {
    this.mangaDaoProvider = mangaDaoProvider;
  }

  @Override
  public MangaRepositoryImpl get() {
    return newInstance(mangaDaoProvider.get());
  }

  public static MangaRepositoryImpl_Factory create(Provider<MangaDao> mangaDaoProvider) {
    return new MangaRepositoryImpl_Factory(mangaDaoProvider);
  }

  public static MangaRepositoryImpl newInstance(MangaDao mangaDao) {
    return new MangaRepositoryImpl(mangaDao);
  }
}
