package com.easycomic.domain.di;

import com.easycomic.domain.repository.MangaRepository;
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase;
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
public final class DomainModule_ProvideUpdateReadingProgressUseCaseFactory implements Factory<UpdateReadingProgressUseCase> {
  private final Provider<MangaRepository> mangaRepositoryProvider;

  public DomainModule_ProvideUpdateReadingProgressUseCaseFactory(
      Provider<MangaRepository> mangaRepositoryProvider) {
    this.mangaRepositoryProvider = mangaRepositoryProvider;
  }

  @Override
  public UpdateReadingProgressUseCase get() {
    return provideUpdateReadingProgressUseCase(mangaRepositoryProvider.get());
  }

  public static DomainModule_ProvideUpdateReadingProgressUseCaseFactory create(
      Provider<MangaRepository> mangaRepositoryProvider) {
    return new DomainModule_ProvideUpdateReadingProgressUseCaseFactory(mangaRepositoryProvider);
  }

  public static UpdateReadingProgressUseCase provideUpdateReadingProgressUseCase(
      MangaRepository mangaRepository) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideUpdateReadingProgressUseCase(mangaRepository));
  }
}
