package com.easycomic.domain.di;

import com.easycomic.domain.usecase.manga.ImportProgressHolder;
import com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase;
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
public final class DomainModule_ProvideMonitorImportProgressUseCaseFactory implements Factory<MonitorImportProgressUseCase> {
  private final Provider<ImportProgressHolder> importProgressHolderProvider;

  public DomainModule_ProvideMonitorImportProgressUseCaseFactory(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    this.importProgressHolderProvider = importProgressHolderProvider;
  }

  @Override
  public MonitorImportProgressUseCase get() {
    return provideMonitorImportProgressUseCase(importProgressHolderProvider.get());
  }

  public static DomainModule_ProvideMonitorImportProgressUseCaseFactory create(
      Provider<ImportProgressHolder> importProgressHolderProvider) {
    return new DomainModule_ProvideMonitorImportProgressUseCaseFactory(importProgressHolderProvider);
  }

  public static MonitorImportProgressUseCase provideMonitorImportProgressUseCase(
      ImportProgressHolder importProgressHolder) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideMonitorImportProgressUseCase(importProgressHolder));
  }
}
