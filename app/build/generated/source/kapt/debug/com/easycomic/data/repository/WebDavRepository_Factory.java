package com.easycomic.data.repository;

import com.easycomic.data.remote.WebDavDataSource;
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
public final class WebDavRepository_Factory implements Factory<WebDavRepository> {
  private final Provider<WebDavDataSource> dataSourceProvider;

  public WebDavRepository_Factory(Provider<WebDavDataSource> dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public WebDavRepository get() {
    return newInstance(dataSourceProvider.get());
  }

  public static WebDavRepository_Factory create(Provider<WebDavDataSource> dataSourceProvider) {
    return new WebDavRepository_Factory(dataSourceProvider);
  }

  public static WebDavRepository newInstance(WebDavDataSource dataSource) {
    return new WebDavRepository(dataSource);
  }
}
