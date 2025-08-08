package com.easycomic.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata("com.easycomic.di.MainImmediateDispatcher")
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
public final class CoroutineModule_ProvideMainImmediateDispatcherFactory implements Factory<CoroutineDispatcher> {
  @Override
  public CoroutineDispatcher get() {
    return provideMainImmediateDispatcher();
  }

  public static CoroutineModule_ProvideMainImmediateDispatcherFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineDispatcher provideMainImmediateDispatcher() {
    return Preconditions.checkNotNullFromProvides(CoroutineModule.INSTANCE.provideMainImmediateDispatcher());
  }

  private static final class InstanceHolder {
    private static final CoroutineModule_ProvideMainImmediateDispatcherFactory INSTANCE = new CoroutineModule_ProvideMainImmediateDispatcherFactory();
  }
}
