package com.easycomic.ui.bookshelf;

import com.easycomic.domain.usecase.manga.BatchImportComicsUseCase;
import com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase;
import com.easycomic.domain.usecase.manga.DeleteMangaUseCase;
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase;
import com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase;
import com.easycomic.domain.usecase.manga.GetRecentMangaUseCase;
import com.easycomic.domain.usecase.manga.ImportComicUseCase;
import com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase;
import com.easycomic.domain.usecase.manga.SearchMangaUseCase;
import com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase;
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
public final class BookshelfViewModel_Factory implements Factory<BookshelfViewModel> {
  private final Provider<GetAllMangaUseCase> getAllMangaUseCaseProvider;

  private final Provider<SearchMangaUseCase> searchMangaUseCaseProvider;

  private final Provider<GetFavoriteMangaUseCase> getFavoriteMangaUseCaseProvider;

  private final Provider<GetRecentMangaUseCase> getRecentMangaUseCaseProvider;

  private final Provider<DeleteMangaUseCase> deleteMangaUseCaseProvider;

  private final Provider<DeleteAllMangaUseCase> deleteAllMangaUseCaseProvider;

  private final Provider<ToggleFavoriteUseCase> toggleFavoriteUseCaseProvider;

  private final Provider<ImportComicUseCase> importComicUseCaseProvider;

  private final Provider<BatchImportComicsUseCase> batchImportComicsUseCaseProvider;

  private final Provider<MonitorImportProgressUseCase> monitorImportProgressUseCaseProvider;

  public BookshelfViewModel_Factory(Provider<GetAllMangaUseCase> getAllMangaUseCaseProvider,
      Provider<SearchMangaUseCase> searchMangaUseCaseProvider,
      Provider<GetFavoriteMangaUseCase> getFavoriteMangaUseCaseProvider,
      Provider<GetRecentMangaUseCase> getRecentMangaUseCaseProvider,
      Provider<DeleteMangaUseCase> deleteMangaUseCaseProvider,
      Provider<DeleteAllMangaUseCase> deleteAllMangaUseCaseProvider,
      Provider<ToggleFavoriteUseCase> toggleFavoriteUseCaseProvider,
      Provider<ImportComicUseCase> importComicUseCaseProvider,
      Provider<BatchImportComicsUseCase> batchImportComicsUseCaseProvider,
      Provider<MonitorImportProgressUseCase> monitorImportProgressUseCaseProvider) {
    this.getAllMangaUseCaseProvider = getAllMangaUseCaseProvider;
    this.searchMangaUseCaseProvider = searchMangaUseCaseProvider;
    this.getFavoriteMangaUseCaseProvider = getFavoriteMangaUseCaseProvider;
    this.getRecentMangaUseCaseProvider = getRecentMangaUseCaseProvider;
    this.deleteMangaUseCaseProvider = deleteMangaUseCaseProvider;
    this.deleteAllMangaUseCaseProvider = deleteAllMangaUseCaseProvider;
    this.toggleFavoriteUseCaseProvider = toggleFavoriteUseCaseProvider;
    this.importComicUseCaseProvider = importComicUseCaseProvider;
    this.batchImportComicsUseCaseProvider = batchImportComicsUseCaseProvider;
    this.monitorImportProgressUseCaseProvider = monitorImportProgressUseCaseProvider;
  }

  @Override
  public BookshelfViewModel get() {
    return newInstance(getAllMangaUseCaseProvider.get(), searchMangaUseCaseProvider.get(), getFavoriteMangaUseCaseProvider.get(), getRecentMangaUseCaseProvider.get(), deleteMangaUseCaseProvider.get(), deleteAllMangaUseCaseProvider.get(), toggleFavoriteUseCaseProvider.get(), importComicUseCaseProvider.get(), batchImportComicsUseCaseProvider.get(), monitorImportProgressUseCaseProvider.get());
  }

  public static BookshelfViewModel_Factory create(
      Provider<GetAllMangaUseCase> getAllMangaUseCaseProvider,
      Provider<SearchMangaUseCase> searchMangaUseCaseProvider,
      Provider<GetFavoriteMangaUseCase> getFavoriteMangaUseCaseProvider,
      Provider<GetRecentMangaUseCase> getRecentMangaUseCaseProvider,
      Provider<DeleteMangaUseCase> deleteMangaUseCaseProvider,
      Provider<DeleteAllMangaUseCase> deleteAllMangaUseCaseProvider,
      Provider<ToggleFavoriteUseCase> toggleFavoriteUseCaseProvider,
      Provider<ImportComicUseCase> importComicUseCaseProvider,
      Provider<BatchImportComicsUseCase> batchImportComicsUseCaseProvider,
      Provider<MonitorImportProgressUseCase> monitorImportProgressUseCaseProvider) {
    return new BookshelfViewModel_Factory(getAllMangaUseCaseProvider, searchMangaUseCaseProvider, getFavoriteMangaUseCaseProvider, getRecentMangaUseCaseProvider, deleteMangaUseCaseProvider, deleteAllMangaUseCaseProvider, toggleFavoriteUseCaseProvider, importComicUseCaseProvider, batchImportComicsUseCaseProvider, monitorImportProgressUseCaseProvider);
  }

  public static BookshelfViewModel newInstance(GetAllMangaUseCase getAllMangaUseCase,
      SearchMangaUseCase searchMangaUseCase, GetFavoriteMangaUseCase getFavoriteMangaUseCase,
      GetRecentMangaUseCase getRecentMangaUseCase, DeleteMangaUseCase deleteMangaUseCase,
      DeleteAllMangaUseCase deleteAllMangaUseCase, ToggleFavoriteUseCase toggleFavoriteUseCase,
      ImportComicUseCase importComicUseCase, BatchImportComicsUseCase batchImportComicsUseCase,
      MonitorImportProgressUseCase monitorImportProgressUseCase) {
    return new BookshelfViewModel(getAllMangaUseCase, searchMangaUseCase, getFavoriteMangaUseCase, getRecentMangaUseCase, deleteMangaUseCase, deleteAllMangaUseCase, toggleFavoriteUseCase, importComicUseCase, batchImportComicsUseCase, monitorImportProgressUseCase);
  }
}
