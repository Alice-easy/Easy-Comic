package com.easycomic.data.di;

import com.easycomic.data.dao.BookmarkDao;
import com.easycomic.domain.repository.BookmarkRepository;
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
public final class DataModule_ProvideBookmarkRepositoryFactory implements Factory<BookmarkRepository> {
  private final Provider<BookmarkDao> bookmarkDaoProvider;

  public DataModule_ProvideBookmarkRepositoryFactory(Provider<BookmarkDao> bookmarkDaoProvider) {
    this.bookmarkDaoProvider = bookmarkDaoProvider;
  }

  @Override
  public BookmarkRepository get() {
    return provideBookmarkRepository(bookmarkDaoProvider.get());
  }

  public static DataModule_ProvideBookmarkRepositoryFactory create(
      Provider<BookmarkDao> bookmarkDaoProvider) {
    return new DataModule_ProvideBookmarkRepositoryFactory(bookmarkDaoProvider);
  }

  public static BookmarkRepository provideBookmarkRepository(BookmarkDao bookmarkDao) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideBookmarkRepository(bookmarkDao));
  }
}
