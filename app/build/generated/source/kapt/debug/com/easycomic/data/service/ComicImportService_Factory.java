package com.easycomic.data.service;

import android.content.Context;
import com.easycomic.data.repository.MangaRepositoryImpl;
import com.easycomic.data.util.FileHelper;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ComicImportService_Factory implements Factory<ComicImportService> {
  private final Provider<Context> contextProvider;

  private final Provider<MangaRepositoryImpl> mangaRepositoryImplProvider;

  private final Provider<FileHelper> fileHelperProvider;

  public ComicImportService_Factory(Provider<Context> contextProvider,
      Provider<MangaRepositoryImpl> mangaRepositoryImplProvider,
      Provider<FileHelper> fileHelperProvider) {
    this.contextProvider = contextProvider;
    this.mangaRepositoryImplProvider = mangaRepositoryImplProvider;
    this.fileHelperProvider = fileHelperProvider;
  }

  @Override
  public ComicImportService get() {
    return newInstance(contextProvider.get(), mangaRepositoryImplProvider.get(), fileHelperProvider.get());
  }

  public static ComicImportService_Factory create(Provider<Context> contextProvider,
      Provider<MangaRepositoryImpl> mangaRepositoryImplProvider,
      Provider<FileHelper> fileHelperProvider) {
    return new ComicImportService_Factory(contextProvider, mangaRepositoryImplProvider, fileHelperProvider);
  }

  public static ComicImportService newInstance(Context context,
      MangaRepositoryImpl mangaRepositoryImpl, FileHelper fileHelper) {
    return new ComicImportService(context, mangaRepositoryImpl, fileHelper);
  }
}
