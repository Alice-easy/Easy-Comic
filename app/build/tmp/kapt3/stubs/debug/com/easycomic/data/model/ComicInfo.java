package com.easycomic.data.model;

/**
 * 漫画信息数据模型
 * 用于存储解析后的漫画文件信息
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b)\n\u0002\u0010\u000b\n\u0002\b\u0007\b\u0087\b\u0018\u00002\u00020\u0001:\u0001=B{\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0011\u001a\u00020\b\u0012\b\b\u0002\u0010\u0012\u001a\u00020\b\u00a2\u0006\u0002\u0010\u0013J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\t\u0010+\u001a\u00020\bH\u00c6\u0003J\t\u0010,\u001a\u00020\bH\u00c6\u0003J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\t\u0010.\u001a\u00020\u0003H\u00c6\u0003J\t\u0010/\u001a\u00020\u0003H\u00c6\u0003J\t\u00100\u001a\u00020\bH\u00c6\u0003J\t\u00101\u001a\u00020\nH\u00c6\u0003J\u000f\u00102\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00c6\u0003J\u000b\u00103\u001a\u0004\u0018\u00010\rH\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\u0089\u0001\u00105\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\u000e\b\u0002\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\b2\b\b\u0002\u0010\u0012\u001a\u00020\bH\u00c6\u0001J\u0013\u00106\u001a\u0002072\b\u00108\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0010\u00109\u001a\u00020\u00032\u0006\u0010:\u001a\u00020\bH\u0002J\t\u0010;\u001a\u00020\nH\u00d6\u0001J\t\u0010<\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0011\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0012\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u001e\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010\u0015R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0015R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0019R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0015R\u0011\u0010$\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b%\u0010\u0015R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0015\u00a8\u0006>"}, d2 = {"Lcom/easycomic/data/model/ComicInfo;", "", "title", "", "filePath", "fileUri", "fileFormat", "fileSize", "", "pageCount", "", "entries", "", "Lcom/easycomic/data/model/ComicInfo$ComicEntry;", "coverEntry", "author", "description", "dateAdded", "dateModified", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JILjava/util/List;Lcom/easycomic/data/model/ComicInfo$ComicEntry;Ljava/lang/String;Ljava/lang/String;JJ)V", "getAuthor", "()Ljava/lang/String;", "getCoverEntry", "()Lcom/easycomic/data/model/ComicInfo$ComicEntry;", "getDateAdded", "()J", "getDateModified", "getDescription", "getEntries", "()Ljava/util/List;", "fileExtension", "getFileExtension", "getFileFormat", "getFilePath", "getFileSize", "getFileUri", "formattedFileSize", "getFormattedFileSize", "getPageCount", "()I", "getTitle", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "formatFileSize", "size", "hashCode", "toString", "ComicEntry", "app_debug"})
public final class ComicInfo {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String filePath = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fileUri = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fileFormat = null;
    private final long fileSize = 0L;
    private final int pageCount = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.easycomic.data.model.ComicInfo.ComicEntry> entries = null;
    @org.jetbrains.annotations.Nullable()
    private final com.easycomic.data.model.ComicInfo.ComicEntry coverEntry = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String author = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String description = null;
    private final long dateAdded = 0L;
    private final long dateModified = 0L;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    public final long component11() {
        return 0L;
    }
    
    public final long component12() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.easycomic.data.model.ComicInfo.ComicEntry> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.data.model.ComicInfo.ComicEntry component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.easycomic.data.model.ComicInfo copy(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, @org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.model.ComicInfo.ComicEntry> entries, @org.jetbrains.annotations.Nullable()
    com.easycomic.data.model.ComicInfo.ComicEntry coverEntry, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, long dateAdded, long dateModified) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public ComicInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, @org.jetbrains.annotations.NotNull()
    java.lang.String fileUri, @org.jetbrains.annotations.NotNull()
    java.lang.String fileFormat, long fileSize, int pageCount, @org.jetbrains.annotations.NotNull()
    java.util.List<com.easycomic.data.model.ComicInfo.ComicEntry> entries, @org.jetbrains.annotations.Nullable()
    com.easycomic.data.model.ComicInfo.ComicEntry coverEntry, @org.jetbrains.annotations.NotNull()
    java.lang.String author, @org.jetbrains.annotations.NotNull()
    java.lang.String description, long dateAdded, long dateModified) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFilePath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileUri() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileFormat() {
        return null;
    }
    
    public final long getFileSize() {
        return 0L;
    }
    
    public final int getPageCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.easycomic.data.model.ComicInfo.ComicEntry> getEntries() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.easycomic.data.model.ComicInfo.ComicEntry getCoverEntry() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAuthor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDescription() {
        return null;
    }
    
    public final long getDateAdded() {
        return 0L;
    }
    
    public final long getDateModified() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedFileSize() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFileExtension() {
        return null;
    }
    
    /**
     * 格式化文件大小
     */
    private final java.lang.String formatFileSize(long size) {
        return null;
    }
    
    /**
     * 漫画条目信息
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b!\b\u0087\b\u0018\u00002\u00020\u0001BI\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\u0006\u0012\u0006\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0006H\u00c6\u0003J\t\u0010#\u001a\u00020\u0006H\u00c6\u0003J\t\u0010$\u001a\u00020\nH\u00c6\u0003J\t\u0010%\u001a\u00020\fH\u00c6\u0003J\t\u0010&\u001a\u00020\fH\u00c6\u0003JY\u0010\'\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\fH\u00c6\u0001J\u0013\u0010(\u001a\u00020\f2\b\u0010)\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\u0010\u0010*\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\t\u0010+\u001a\u00020\nH\u00d6\u0001J\t\u0010,\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0011\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0014\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0013R\u0011\u0010\u0016\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0013R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u001aR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\u001aR\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0013R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0010\u00a8\u0006-"}, d2 = {"Lcom/easycomic/data/model/ComicInfo$ComicEntry;", "", "name", "", "path", "size", "", "compressedSize", "lastModified", "index", "", "isImage", "", "isCover", "(Ljava/lang/String;Ljava/lang/String;JJJIZZ)V", "getCompressedSize", "()J", "extension", "getExtension", "()Ljava/lang/String;", "formattedCompressedSize", "getFormattedCompressedSize", "formattedSize", "getFormattedSize", "getIndex", "()I", "()Z", "getLastModified", "getName", "getPath", "getSize", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "equals", "other", "formatFileSize", "hashCode", "toString", "app_debug"})
    public static final class ComicEntry {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String path = null;
        private final long size = 0L;
        private final long compressedSize = 0L;
        private final long lastModified = 0L;
        private final int index = 0;
        private final boolean isImage = false;
        private final boolean isCover = false;
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final long component3() {
            return 0L;
        }
        
        public final long component4() {
            return 0L;
        }
        
        public final long component5() {
            return 0L;
        }
        
        public final int component6() {
            return 0;
        }
        
        public final boolean component7() {
            return false;
        }
        
        public final boolean component8() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.easycomic.data.model.ComicInfo.ComicEntry copy(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String path, long size, long compressedSize, long lastModified, int index, boolean isImage, boolean isCover) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
        
        public ComicEntry(@org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String path, long size, long compressedSize, long lastModified, int index, boolean isImage, boolean isCover) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPath() {
            return null;
        }
        
        public final long getSize() {
            return 0L;
        }
        
        public final long getCompressedSize() {
            return 0L;
        }
        
        public final long getLastModified() {
            return 0L;
        }
        
        public final int getIndex() {
            return 0;
        }
        
        public final boolean isImage() {
            return false;
        }
        
        public final boolean isCover() {
            return false;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getExtension() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFormattedSize() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFormattedCompressedSize() {
            return null;
        }
        
        /**
         * 格式化文件大小
         */
        private final java.lang.String formatFileSize(long size) {
            return null;
        }
    }
}