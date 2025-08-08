package com.easycomic.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\u0007"}, d2 = {"Lcom/easycomic/di/NetworkModule;", "", "()V", "bindWebDavDataSource", "Lcom/easycomic/data/remote/WebDavDataSource;", "impl", "Lcom/easycomic/data/remote/SardineWebDavDataSource;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class NetworkModule {
    
    public NetworkModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.easycomic.data.remote.WebDavDataSource bindWebDavDataSource(@org.jetbrains.annotations.NotNull()
    com.easycomic.data.remote.SardineWebDavDataSource impl);
}