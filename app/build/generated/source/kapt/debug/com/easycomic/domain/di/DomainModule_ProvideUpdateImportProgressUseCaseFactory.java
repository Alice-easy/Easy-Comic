package com.easycomic.domain.di;

import com.easycomic.domain.usecase.manga.ImportProgressHolder;
import com.easycomic.domain.usecase.manga.UpdateImportProgressUseCase;
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
public final class DomainModule_ProvideUpdateImportProgressUseCaseFactory implements Factory<UpdateImportProgressUseCase> {
  private final Provider<ImportProgressHolder> importProgressHolderProvider;

  public DomainModule_ProvideUpdateImportProgressUseCaseFactory(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    this.importProgressHolderProvider = importProgressHolderProvider;
  }

  @Override
  public UpdateImportProgressUseCase get() {
    return provideUpdateImportProgressUseCase(importProgressHolderProvider.get());
  }

  public static DomainModule_ProvideUpdateImportProgressUseCaseFactory create(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    return new DomainModule_ProvideUpdateImportProgressUseCaseFactory(importProgressHolderProvider);
  }

  public static UpdateImportProgressUseCase provideUpdateImportProgressUseCase(
      ImportProgressHolder importProgressHolder) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideUpdateImportProgressUseCase(importProgressHolder));
  }
}
