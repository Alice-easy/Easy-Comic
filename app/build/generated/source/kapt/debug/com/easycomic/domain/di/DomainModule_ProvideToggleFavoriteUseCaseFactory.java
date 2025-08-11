package com.easycomic.domain.di;

import com.easycomic.domain.repository.MangaRepository;
import com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase;
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
public final class DomainModule_ProvideToggleFavoriteUseCaseFactory implements Factory<ToggleFavoriteUseCase> {
  private final Provider<MangaRepository> mangaRepositoryProvider;

  public DomainModule_ProvideToggleFavoriteUseCaseFactory(
      Provider<MangaRepository> mangaRepositoryProvider) {
    this.mangaRepositoryProvider = mangaRepositoryProvider;
  }

  @Override
  public ToggleFavoriteUseCase get() {
    return provideToggleFavoriteUseCase(mangaRepositoryProvider.get());
  }

  public static DomainModule_ProvideToggleFavoriteUseCaseFactory create(
      Provider<MangaRepository> mangaRepositoryProvider) {
    return new DomainModule_ProvideToggleFavoriteUseCaseFactory(mangaRepositoryProvider);
  }

  public static ToggleFavoriteUseCase provideToggleFavoriteUseCase(
      MangaRepository mangaRepository) {
    return Preconditions.checkNotNullFromProvides(DomainModule.INSTANCE.provideToggleFavoriteUseCase(mangaRepository));
  }
}
