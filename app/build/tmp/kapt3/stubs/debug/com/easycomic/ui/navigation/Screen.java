package com.easycomic.ui.navigation;

/**
 * 导航控制器
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0002\u0007\bB\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\u0002\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/easycomic/ui/navigation/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "Bookshelf", "Reader", "Lcom/easycomic/ui/navigation/Screen$Bookshelf;", "Lcom/easycomic/ui/navigation/Screen$Reader;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/easycomic/ui/navigation/Screen$Bookshelf;", "Lcom/easycomic/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Bookshelf extends com.easycomic.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.easycomic.ui.navigation.Screen.Bookshelf INSTANCE = null;
        
        private Bookshelf() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/easycomic/ui/navigation/Screen$Reader;", "Lcom/easycomic/ui/navigation/Screen;", "()V", "createRoute", "", "mangaId", "", "app_debug"})
    public static final class Reader extends com.easycomic.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.easycomic.ui.navigation.Screen.Reader INSTANCE = null;
        
        private Reader() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(long mangaId) {
            return null;
        }
    }
}