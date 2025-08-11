package com.easycomic.domain.usecase.manga;

import com.easycomic.data.service.ComicImportService;
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
public final class ImportComicUseCase_Factory implements Factory<ImportComicUseCase> {
  private final Provider<ComicImportService> comicImportServiceProvider;

  public ImportComicUseCase_Factory(Provider<ComicImportService> comicImportServiceProvider) {
    this.comicImportServiceProvider = comicImportServiceProvider;
  }

  @Override
  public ImportComicUseCase get() {
    return newInstance(comicImportServiceProvider.get());
  }

  public static ImportComicUseCase_Factory create(
      Provider<ComicImportService> comicImportServiceProvider) {
    return new ImportComicUseCase_Factory(comicImportServiceProvider);
  }

  public static ImportComicUseCase newInstance(ComicImportService comicImportService) {
    return new ImportComicUseCase(comicImportService);
  }
}
