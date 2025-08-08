package com.easycomic.domain.usecase.manga;

import com.easycomic.domain.repository.MangaRepository;
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
public final class GetMangaListUseCase_Factory implements Factory<GetMangaListUseCase> {
  private final Provider<MangaRepository> repositoryProvider;

  public GetMangaListUseCase_Factory(Provider<MangaRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetMangaListUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetMangaListUseCase_Factory create(Provider<MangaRepository> repositoryProvider) {
    return new GetMangaListUseCase_Factory(repositoryProvider);
  }

  public static GetMangaListUseCase newInstance(MangaRepository repository) {
    return new GetMangaListUseCase(repository);
  }
}
