package com.easycomic.data.remote;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SardineWebDavDataSource_Factory implements Factory<SardineWebDavDataSource> {
  @Override
  public SardineWebDavDataSource get() {
    return newInstance();
  }

  public static SardineWebDavDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SardineWebDavDataSource newInstance() {
    return new SardineWebDavDataSource();
  }

  private static final class InstanceHolder {
    private static final SardineWebDavDataSource_Factory INSTANCE = new SardineWebDavDataSource_Factory();
  }
}
