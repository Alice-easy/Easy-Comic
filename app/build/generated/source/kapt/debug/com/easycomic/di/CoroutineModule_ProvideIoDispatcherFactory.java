package com.easycomic.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata("com.easycomic.di.IoDispatcher")
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
public final class CoroutineModule_ProvideIoDispatcherFactory implements Factory<CoroutineDispatcher> {
  @Override
  public CoroutineDispatcher get() {
    return provideIoDispatcher();
  }

  public static CoroutineModule_ProvideIoDispatcherFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineDispatcher provideIoDispatcher() {
    return Preconditions.checkNotNullFromProvides(CoroutineModule.INSTANCE.provideIoDispatcher());
  }

  private static final class InstanceHolder {
    private static final CoroutineModule_ProvideIoDispatcherFactory INSTANCE = new CoroutineModule_ProvideIoDispatcherFactory();
  }
}
