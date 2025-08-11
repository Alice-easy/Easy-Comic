package com.easycomic.domain.usecase.manga;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ImportProgressHolder_Factory implements Factory<ImportProgressHolder> {
  @Override
  public ImportProgressHolder get() {
    return newInstance();
  }

  public static ImportProgressHolder_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ImportProgressHolder newInstance() {
    return new ImportProgressHolder();
  }

  private static final class InstanceHolder {
    private static final ImportProgressHolder_Factory INSTANCE = new ImportProgressHolder_Factory();
  }
}
