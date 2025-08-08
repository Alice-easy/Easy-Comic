package com.easycomic.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0004H\u0007J\b\u0010\u0006\u001a\u00020\u0004H\u0007J\b\u0010\u0007\u001a\u00020\u0004H\u0007\u00a8\u0006\b"}, d2 = {"Lcom/easycomic/di/CoroutineModule;", "", "()V", "provideDefaultDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "provideIoDispatcher", "provideMainDispatcher", "provideMainImmediateDispatcher", "app_release"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class CoroutineModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.easycomic.di.CoroutineModule INSTANCE = null;
    
    private CoroutineModule() {
        super();
    }
    
    @dagger.Provides()
    @IoDispatcher()
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CoroutineDispatcher provideIoDispatcher() {
        return null;
    }
    
    @dagger.Provides()
    @DefaultDispatcher()
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CoroutineDispatcher provideDefaultDispatcher() {
        return null;
    }
    
    @dagger.Provides()
    @MainDispatcher()
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CoroutineDispatcher provideMainDispatcher() {
        return null;
    }
    
    @dagger.Provides()
    @MainImmediateDispatcher()
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.CoroutineDispatcher provideMainImmediateDispatcher() {
        return null;
    }
}