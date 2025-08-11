package com.easycomic.domain.di;

import com.easycomic.domain.usecase.manga.BatchImportComicsUseCase;
import com.easycomic.domain.usecase.manga.ImportComicUseCase;
import com.easycomic.domain.usecase.manga.ImportProgressHolder;
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
public final class DomainModule_ProvideBatchImportComicsUseCaseFactory implements Factory<BatchImportComicsUseCase> {
  private final Provider<ImportComicUseCase> importComicUseCaseProvider;

  private final Provider<ImportProgressHolder> importProgressHolderProvider;

  public DomainModule_ProvideBatchImportComicsUseCaseFactory(
      Provider<ImportComicUseCase> importComicUseCaseProvider,
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    this.importComicUseCaseProvider = importComicUseCaseProvider;
    this.importProgressHolderProvider = importProgressHolderProvider;
  }

  @Override
  public BatchImportComicsUseCase get() {
    return provideBatchImportComicsUseCase(importComicUseCaseProvider.get(), importProgressHolderProvider.get());
  }

  public static DomainModule_ProvideBatchImportComicsUseCaseFactory create(
      Provider<ImportComicUseCase> importComicUseCaseProvider,
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    return new DomainModule_ProvideBatchImportComicsUseCaseFactory(importComicUseCaseProvider, importProgressHolderProvider);
  }

  public static BatchImportComicsUseCase provideBatchImportComicsUseCase(
      ImportComicUseCase importComicUseCase, ImportProgressHolder importProgressHolder) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideBatchImportComicsUseCase(importComicUseCase, importProgressHolder));
  }
}
