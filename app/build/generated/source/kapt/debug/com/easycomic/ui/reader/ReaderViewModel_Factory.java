package com.easycomic.ui.reader;

import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase;
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase;
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
public final class ReaderViewModel_Factory implements Factory<ReaderViewModel> {
  private final Provider<GetMangaByIdUseCase> getMangaByIdUseCaseProvider;

  private final Provider<UpdateReadingProgressUseCase> updateReadingProgressUseCaseProvider;

  public ReaderViewModel_Factory(Provider<GetMangaByIdUseCase> getMangaByIdUseCaseProvider,
      Provider<UpdateReadingProgressUseCase> updateReadingProgressUseCaseProvider) {
    this.getMangaByIdUseCaseProvider = getMangaByIdUseCaseProvider;
    this.updateReadingProgressUseCaseProvider = updateReadingProgressUseCaseProvider;
  }

  @Override
  public ReaderViewModel get() {
    return newInstance(getMangaByIdUseCaseProvider.get(), updateReadingProgressUseCaseProvider.get());
  }

  public static ReaderViewModel_Factory create(
      Provider<GetMangaByIdUseCase> getMangaByIdUseCaseProvider,
      Provider<UpdateReadingProgressUseCase> updateReadingProgressUseCaseProvider) {
    return new ReaderViewModel_Factory(getMangaByIdUseCaseProvider, updateReadingProgressUseCaseProvider);
  }

  public static ReaderViewModel newInstance(GetMangaByIdUseCase getMangaByIdUseCase,
      UpdateReadingProgressUseCase updateReadingProgressUseCase) {
    return new ReaderViewModel(getMangaByIdUseCase, updateReadingProgressUseCase);
  }
}
