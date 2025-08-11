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
public final class MonitorImportProgressUseCase_Factory implements Factory<MonitorImportProgressUseCase> {
  private final Provider<ImportProgressHolder> importProgressHolderProvider;

  public MonitorImportProgressUseCase_Factory(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    this.importProgressHolderProvider = importProgressHolderProvider;
  }

  @Override
  public MonitorImportProgressUseCase get() {
    return newInstance(importProgressHolderProvider.get());
  }

  public static MonitorImportProgressUseCase_Factory create(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    return new MonitorImportProgressUseCase_Factory(importProgressHolderProvider);
  }

  public static MonitorImportProgressUseCase newInstance(
      ImportProgressHolder importProgressHolder) {
    return new MonitorImportProgressUseCase(importProgressHolder);
  }
}
