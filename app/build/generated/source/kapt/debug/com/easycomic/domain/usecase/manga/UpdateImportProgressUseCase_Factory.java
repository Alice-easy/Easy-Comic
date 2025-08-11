package com.easycomic.domain.usecase.manga;

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
public final class UpdateImportProgressUseCase_Factory implements Factory<UpdateImportProgressUseCase> {
  private final Provider<ImportProgressHolder> importProgressHolderProvider;

  public UpdateImportProgressUseCase_Factory(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    this.importProgressHolderProvider = importProgressHolderProvider;
  }

  @Override
  public UpdateImportProgressUseCase get() {
    return newInstance(importProgressHolderProvider.get());
  }

  public static UpdateImportProgressUseCase_Factory create(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    return new UpdateImportProgressUseCase_Factory(importProgressHolderProvider);
  }

  public static UpdateImportProgressUseCase newInstance(ImportProgressHolder importProgressHolder) {
    return new UpdateImportProgressUseCase(importProgressHolder);
  }
}
