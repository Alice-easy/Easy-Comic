package com.easycomic.localization

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * 国际化管理器
 * 
 * 处理应用的多语言支持，包括语言切换、本地化资源管理等
 */
object LocalizationManager {
    
    /**
     * 支持的语言列表
     */
    enum class SupportedLanguage(
        val code: String,
        val displayName: String,
        val nativeName: String,
        val locale: Locale
    ) {
        ENGLISH("en", "English", "English", Locale.ENGLISH),
        SIMPLIFIED_CHINESE("zh-CN", "Simplified Chinese", "简体中文", Locale.SIMPLIFIED_CHINESE),
        TRADITIONAL_CHINESE("zh-TW", "Traditional Chinese", "繁體中文", Locale.TRADITIONAL_CHINESE),
        JAPANESE("ja", "Japanese", "日本語", Locale.JAPANESE),
        KOREAN("ko", "Korean", "한국어", Locale.KOREAN);
        
        companion object {
            /**
             * 根据语言代码获取支持的语言
             */
            fun fromCode(code: String): SupportedLanguage? {
                return values().find { it.code == code }
            }
            
            /**
             * 根据Locale获取支持的语言
             */
            fun fromLocale(locale: Locale): SupportedLanguage? {
                // 精确匹配
                values().forEach { language ->
                    if (language.locale.language == locale.language && 
                        language.locale.country == locale.country) {
                        return language
                    }
                }
                
                // 仅语言匹配
                return values().find { it.locale.language == locale.language }
            }
            
            /**
             * 获取所有支持的语言列表
             */
            fun getAllLanguages(): List<SupportedLanguage> = values().toList()
        }
    }
    
    private const val PREFS_NAME = "localization_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"
    private const val KEY_USE_SYSTEM_LANGUAGE = "use_system_language"
    
    /**
     * 初始化国际化管理器
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useSystemLanguage = prefs.getBoolean(KEY_USE_SYSTEM_LANGUAGE, true)
        
        if (!useSystemLanguage) {
            val savedLanguageCode = prefs.getString(KEY_SELECTED_LANGUAGE, null)
            savedLanguageCode?.let { code ->
                SupportedLanguage.fromCode(code)?.let { language ->
                    applyLanguage(context, language)
                }
            }
        }
    }
    
    /**
     * 获取当前语言设置
     */
    fun getCurrentLanguage(context: Context): SupportedLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useSystemLanguage = prefs.getBoolean(KEY_USE_SYSTEM_LANGUAGE, true)
        
        return if (useSystemLanguage) {
            getSystemLanguage()
        } else {
            val savedCode = prefs.getString(KEY_SELECTED_LANGUAGE, null)
            SupportedLanguage.fromCode(savedCode ?: "") ?: getSystemLanguage()
        }
    }
    
    /**
     * 获取系统语言
     */
    fun getSystemLanguage(): SupportedLanguage {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault()
        } else {
            Locale.getDefault()
        }
        
        return SupportedLanguage.fromLocale(systemLocale) ?: SupportedLanguage.ENGLISH
    }
    
    /**
     * 是否使用系统语言
     */
    fun isUsingSystemLanguage(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_SYSTEM_LANGUAGE, true)
    }
    
    /**
     * 设置使用系统语言
     */
    fun setUseSystemLanguage(context: Context, useSystemLanguage: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_USE_SYSTEM_LANGUAGE, useSystemLanguage)
            .apply()
        
        if (useSystemLanguage) {
            applyLanguage(context, getSystemLanguage())
        }
    }
    
    /**
     * 设置应用语言
     */
    fun setLanguage(context: Context, language: SupportedLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SELECTED_LANGUAGE, language.code)
            .putBoolean(KEY_USE_SYSTEM_LANGUAGE, false)
            .apply()
        
        applyLanguage(context, language)
    }
    
    /**
     * 应用语言设置
     */
    private fun applyLanguage(context: Context, language: SupportedLanguage) {
        val locale = language.locale
        Locale.setDefault(locale)
        
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(android.os.LocaleList(locale))
        }
        
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
    
    /**
     * 创建带有指定语言的Context
     */
    fun createLocalizedContext(context: Context, language: SupportedLanguage): Context {
        val locale = language.locale
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(android.os.LocaleList(locale))
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            context.createConfigurationContext(configuration)
        }
    }
    
    /**
     * 获取本地化字符串
     */
    fun getLocalizedString(context: Context, resourceId: Int, language: SupportedLanguage): String {
        val localizedContext = createLocalizedContext(context, language)
        return localizedContext.getString(resourceId)
    }
    
    /**
     * 格式化本地化字符串
     */
    fun getLocalizedString(
        context: Context, 
        resourceId: Int, 
        language: SupportedLanguage, 
        vararg formatArgs: Any
    ): String {
        val localizedContext = createLocalizedContext(context, language)
        return localizedContext.getString(resourceId, *formatArgs)
    }
    
    /**
     * 检查是否为RTL语言
     */
    fun isRTL(language: SupportedLanguage): Boolean {
        // 目前支持的语言都是LTR，如果以后添加阿拉伯语等RTL语言需要在这里处理
        return false
    }
    
    /**
     * 获取语言的字体资源（如果需要特殊字体）
     */
    fun getFontResourceForLanguage(language: SupportedLanguage): Int? {
        return when (language) {
            SupportedLanguage.SIMPLIFIED_CHINESE,
            SupportedLanguage.TRADITIONAL_CHINESE -> {
                // 可以返回中文字体资源ID
                null // 或者 R.font.noto_sans_cjk
            }
            SupportedLanguage.JAPANESE -> {
                // 可以返回日文字体资源ID
                null // 或者 R.font.noto_sans_jp
            }
            SupportedLanguage.KOREAN -> {
                // 可以返回韩文字体资源ID
                null // 或者 R.font.noto_sans_kr
            }
            else -> null
        }
    }
    
    /**
     * 获取语言显示名称（使用当前语言）
     */
    fun getDisplayName(context: Context, language: SupportedLanguage): String {
        val currentLanguage = getCurrentLanguage(context)
        
        // 如果当前语言是目标语言，返回原生名称
        if (currentLanguage == language) {
            return language.nativeName
        }
        
        // 否则返回当前语言环境下的显示名称
        return when (currentLanguage) {
            SupportedLanguage.SIMPLIFIED_CHINESE -> when (language) {
                SupportedLanguage.ENGLISH -> "英语"
                SupportedLanguage.TRADITIONAL_CHINESE -> "繁体中文"
                SupportedLanguage.JAPANESE -> "日语"
                SupportedLanguage.KOREAN -> "韩语"
                else -> language.nativeName
            }
            SupportedLanguage.TRADITIONAL_CHINESE -> when (language) {
                SupportedLanguage.ENGLISH -> "英語"
                SupportedLanguage.SIMPLIFIED_CHINESE -> "簡體中文"
                SupportedLanguage.JAPANESE -> "日語"
                SupportedLanguage.KOREAN -> "韓語"
                else -> language.nativeName
            }
            SupportedLanguage.JAPANESE -> when (language) {
                SupportedLanguage.ENGLISH -> "英語"
                SupportedLanguage.SIMPLIFIED_CHINESE -> "中国語（簡体字）"
                SupportedLanguage.TRADITIONAL_CHINESE -> "中国語（繁体字）"
                SupportedLanguage.KOREAN -> "韓国語"
                else -> language.nativeName
            }
            SupportedLanguage.KOREAN -> when (language) {
                SupportedLanguage.ENGLISH -> "영어"
                SupportedLanguage.SIMPLIFIED_CHINESE -> "중국어(간체)"
                SupportedLanguage.TRADITIONAL_CHINESE -> "중국어(번체)"
                SupportedLanguage.JAPANESE -> "일본어"
                else -> language.nativeName
            }
            else -> language.displayName
        }
    }
}