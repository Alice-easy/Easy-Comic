package com.easycomic.domain.model;

/**
 * 导入状态枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/easycomic/domain/model/ImportStatus;", "", "(Ljava/lang/String;I)V", "IDLE", "VALIDATING", "PARSING", "EXTRACTING", "SAVING", "COMPLETED", "FAILED", "CANCELLED", "app_debug"})
public enum ImportStatus {
    /*public static final*/ IDLE /* = new IDLE() */,
    /*public static final*/ VALIDATING /* = new VALIDATING() */,
    /*public static final*/ PARSING /* = new PARSING() */,
    /*public static final*/ EXTRACTING /* = new EXTRACTING() */,
    /*public static final*/ SAVING /* = new SAVING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ FAILED /* = new FAILED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */;
    
    ImportStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.easycomic.domain.model.ImportStatus> getEntries() {
        return null;
    }
}