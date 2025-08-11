package com.easycomic.data.di;

import android.content.Context;
import com.easycomic.data.repository.MangaRepositoryImpl;
import com.easycomic.data.service.ComicImportService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DataModule_ProvideComicImportServiceFactory implements Factory<ComicImportService> {
  private final Provider<Context> contextProvider;

  private final Provider<MangaRepositoryImpl> mangaRepositoryImplProvider;

  public DataModule_ProvideComicImportServiceFactory(Provider<Context> contextProvider,
      Provider<MangaRepositoryImpl> mangaRepositoryImplProvider) {
    this.contextProvider = contextProvider;
    this.mangaRepositoryImplProvider = mangaRepositoryImplProvider;
  }

  @Override
  public ComicImportService get() {
    return provideComicImportService(contextProvider.get(), mangaRepositoryImplProvider.get());
  }

  public static DataModule_ProvideComicImportServiceFactory create(
      Provider<Context> contextProvider,
      Provider<MangaRepositoryImpl> mangaRepositoryImplProvider) {
    return new DataModule_ProvideComicImportServiceFactory(contextProvider, mangaRepositoryImplProvider);
  }

  public static ComicImportService provideComicImportService(Context context,
      MangaRepositoryImpl mangaRepositoryImpl) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideComicImportService(context, mangaRepositoryImpl));
  }
}
