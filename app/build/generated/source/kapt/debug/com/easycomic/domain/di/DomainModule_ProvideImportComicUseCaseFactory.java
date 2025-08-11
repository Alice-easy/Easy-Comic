package com.easycomic.domain.di;

import com.easycomic.data.service.ComicImportService;
import com.easycomic.domain.usecase.manga.ImportComicUseCase;
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
public final class DomainModule_ProvideImportComicUseCaseFactory implements Factory<ImportComicUseCase> {
  private final Provider<ComicImportService> comicImportServiceProvider;

  public DomainModule_ProvideImportComicUseCaseFactory(
      Provider<ComicImportService> comicImportServiceProvider) {
    this.comicImportServiceProvider = comicImportServiceProvider;
  }

  @Override
  public ImportComicUseCase get() {
    return provideImportComicUseCase(comicImportServiceProvider.get());
  }

  public static DomainModule_ProvideImportComicUseCaseFactory create(
      Provider<ComicImportService> comicImportServiceProvider) {
    return new DomainModule_ProvideImportComicUseCaseFactory(comicImportServiceProvider);
  }

  public static ImportComicUseCase provideImportComicUseCase(
      ComicImportService comicImportService) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideImportComicUseCase(comicImportService));
  }
}
