# 🌍 Easy Comic 国际化指南

## 📋 概述

Easy Comic 支持多语言国际化，为全球用户提供本地化的使用体验。本文档说明如何管理和扩展应用的多语言支持。

## 🗣️ 支持的语言

### 当前支持的语言

| 语言        | 代码    | 区域      | 完成度 | 维护状态    |
| ----------- | ------- | --------- | ------ | ----------- |
| 🇺🇸 English  | `en`    | 全球      | 100%   | ✅ 主要语言 |
| 🇨🇳 简体中文 | `zh-CN` | 中国大陆  | 100%   | ✅ 完整支持 |
| 🇹🇼 繁體中文 | `zh-TW` | 台湾/香港 | 100%   | ✅ 完整支持 |
| 🇯🇵 日本語   | `ja`    | 日本      | 100%   | ✅ 完整支持 |
| 🇰🇷 한국어   | `ko`    | 韩国      | 100%   | ✅ 完整支持 |

### 计划支持的语言

| 语言        | 代码 | 优先级 | 预计完成时间 |
| ----------- | ---- | ------ | ------------ |
| 🇪🇸 Español  | `es` | 高     | v0.8.0       |
| 🇫🇷 Français | `fr` | 中     | v0.9.0       |
| 🇩🇪 Deutsch  | `de` | 中     | v0.9.0       |
| 🇷🇺 Русский  | `ru` | 低     | v1.0.0       |

## 🏗️ 架构设计

### 文件结构

```
app/src/main/res/
├── values/                     # 默认语言（英语）
│   └── strings.xml
├── values-zh-rCN/             # 简体中文
│   └── strings.xml
├── values-zh-rTW/             # 繁体中文
│   └── strings.xml
├── values-ja/                 # 日语
│   └── strings.xml
├── values-ko/                 # 韩语
│   └── strings.xml
└── values-night/              # 夜间模式资源
    └── colors.xml
```

### 代码结构

```kotlin
com.easycomic.localization/
├── LocalizationManager.kt     # 国际化管理器
├── SupportedLanguage.kt      # 支持的语言枚举
└── LanguagePreference.kt     # 语言偏好设置
```

## 🔧 使用方法

### 1. 初始化国际化

在 Application 类中初始化：

```kotlin
class EasyComicApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化国际化管理器
        LocalizationManager.initialize(this)
    }
}
```

### 2. 获取当前语言

```kotlin
// 获取当前应用语言
val currentLanguage = LocalizationManager.getCurrentLanguage(context)

// 检查是否使用系统语言
val isUsingSystemLanguage = LocalizationManager.isUsingSystemLanguage(context)

// 获取系统语言
val systemLanguage = LocalizationManager.getSystemLanguage()
```

### 3. 切换语言

```kotlin
// 设置特定语言
LocalizationManager.setLanguage(context, SupportedLanguage.SIMPLIFIED_CHINESE)

// 设置使用系统语言
LocalizationManager.setUseSystemLanguage(context, true)

// 需要重启Activity以生效
recreate()
```

### 4. 获取本地化字符串

```kotlin
// 在当前语言环境下获取字符串
val text = getString(R.string.app_name)

// 获取指定语言的字符串
val chineseText = LocalizationManager.getLocalizedString(
    context,
    R.string.app_name,
    SupportedLanguage.SIMPLIFIED_CHINESE
)

// 格式化字符串
val pageText = LocalizationManager.getLocalizedString(
    context,
    R.string.reader_page_number,
    SupportedLanguage.JAPANESE,
    currentPage,
    totalPages
)
```

## 📝 翻译规范

### 字符串命名规范

```xml
<!-- 功能模块_控件类型_具体含义 -->
<string name="bookshelf_button_import">Import</string>
<string name="reader_message_loading">Loading page...</string>
<string name="settings_title_theme">Theme</string>
<string name="common_action_ok">OK</string>
```

### 翻译质量标准

#### 1. 准确性

- 保持原意不变
- 符合目标语言的表达习惯
- 专业术语使用准确

#### 2. 一致性

- 相同概念使用相同翻译
- 保持界面术语一致性
- 遵循平台约定

#### 3. 简洁性

- 避免冗长表达
- 考虑界面空间限制
- 保持文字简洁明了

### 各语言特殊注意事项

#### 中文（简体/繁体）

```xml
<!-- 避免直译，符合中文表达习惯 -->
<string name="bookshelf_empty">暂无漫画</string>  <!-- 好 -->
<!-- <string name="bookshelf_empty">没有漫画被找到</string>  坏 -->

<!-- 繁体中文用词差异 -->
<!-- 简体：文件夹、软件、网络 -->
<!-- 繁体：資料夾、軟體、網路 -->
```

#### 日语

```xml
<!-- 使用适当的敬语级别 -->
<string name="permission_storage_message">このアプリはコミックファイルを読み取るためにストレージ権限が必要です</string>

<!-- 避免过长的片假名 -->
<string name="settings_title">設定</string>  <!-- 好 -->
<!-- <string name="settings_title">セッティング</string>  可读性差 -->
```

#### 韩语

```xml
<!-- 使用适当的敬语 -->
<string name="permission_storage_message">이 앱은 만화 파일을 읽기 위해 저장소 권한이 필요합니다</string>

<!-- 避免过度汉字词 -->
<string name="bookshelf_title">내 만화</string>  <!-- 好 -->
<!-- <string name="bookshelf_title">나의 만화 도서관</string>  过于正式 -->
```

## 🧪 测试指南

### 1. 多语言测试

```kotlin
@Test
fun testAllSupportedLanguages() {
    SupportedLanguage.getAllLanguages().forEach { language ->
        val context = LocalizationManager.createLocalizedContext(context, language)

        // 验证关键字符串是否正确翻译
        val appName = context.getString(R.string.app_name)
        assertThat(appName).isNotEmpty()
        assertThat(appName).isNotEqualTo("app_name") // 确保不是key本身
    }
}
```

### 2. 字符串格式测试

```kotlin
@Test
fun testStringFormatting() {
    val context = LocalizationManager.createLocalizedContext(
        context,
        SupportedLanguage.JAPANESE
    )

    val pageText = context.getString(R.string.reader_page_number, 5, 10)
    assertThat(pageText).contains("5")
    assertThat(pageText).contains("10")
}
```

### 3. RTL 支持测试

```kotlin
@Test
fun testRTLSupport() {
    // 当前支持的语言都是LTR，但为未来RTL语言做准备
    SupportedLanguage.getAllLanguages().forEach { language ->
        val isRTL = LocalizationManager.isRTL(language)
        // 验证RTL处理逻辑
    }
}
```

## 🔄 添加新语言

### 1. 创建资源文件

```bash
# 创建新语言资源目录
mkdir app/src/main/res/values-es/

# 复制英语字符串文件
cp app/src/main/res/values/strings.xml app/src/main/res/values-es/
```

### 2. 翻译字符串

```xml
<!-- app/src/main/res/values-es/strings.xml -->
<resources>
    <string name="app_name">Cómic Fácil</string>
    <string name="nav_bookshelf">Biblioteca</string>
    <string name="nav_favorites">Favoritos</string>
    <!-- ... 其他翻译 ... -->
</resources>
```

### 3. 更新 LocalizationManager

```kotlin
enum class SupportedLanguage(/*...*/) {
    // 现有语言...
    SPANISH("es", "Spanish", "Español", Locale("es")),
    // ...
}
```

### 4. 测试新语言

```kotlin
@Test
fun testSpanishLocalization() {
    val context = LocalizationManager.createLocalizedContext(
        context,
        SupportedLanguage.SPANISH
    )

    val appName = context.getString(R.string.app_name)
    assertThat(appName).isEqualTo("Cómic Fácil")
}
```

## 🛠️ 开发工具

### 1. 字符串提取脚本

```bash
#!/bin/bash
# extract_strings.sh - 提取所有字符串资源

echo "Extracting strings from layouts..."
find app/src/main/res/layout -name "*.xml" -exec grep -H "android:text=\"@string/" {} \;

echo "Extracting strings from code..."
find app/src/main/java -name "*.kt" -exec grep -H "R.string." {} \;
```

### 2. 翻译完整性检查

```bash
#!/bin/bash
# check_translations.sh - 检查翻译完整性

BASE_FILE="app/src/main/res/values/strings.xml"
LANG_DIRS=$(find app/src/main/res -name "values-*" -type d)

for dir in $LANG_DIRS; do
    echo "Checking $dir..."
    # 比较字符串数量
    base_count=$(grep -c "<string name=" $BASE_FILE)
    lang_count=$(grep -c "<string name=" $dir/strings.xml)

    echo "Base: $base_count, $dir: $lang_count"
done
```

## 📊 质量保证

### 翻译质量检查清单

- [ ] 所有字符串都已翻译
- [ ] 格式化字符串正确处理占位符
- [ ] 复数形式正确处理
- [ ] 界面文字长度适合控件
- [ ] 专业术语使用一致
- [ ] 符合目标语言表达习惯
- [ ] 通过母语使用者审核

### 自动化检查

```kotlin
// 在CI/CD中运行的翻译检查
class TranslationValidationTest {

    @Test
    fun validateAllTranslations() {
        val languages = SupportedLanguage.getAllLanguages()
        val baseStrings = getStringResourceIds()

        languages.forEach { language ->
            baseStrings.forEach { stringId ->
                val translated = getTranslatedString(language, stringId)
                assertThat(translated).isNotEmpty()
                assertThat(translated).isNotEqualTo(getStringResourceName(stringId))
            }
        }
    }
}
```

## 🔗 相关资源

### 官方文档

- [Android 国际化指南](https://developer.android.com/guide/topics/resources/localization)
- [Material Design 国际化](https://material.io/design/usability/bidirectionality.html)

### 翻译工具

- [Android String Resource Editor](https://github.com/google/android-strings-editor)
- [Google Translate Toolkit](https://translate.google.com/toolkit/)
- [Crowdin](https://crowdin.com/) - 协作翻译平台

### 语言资源

- [Unicode CLDR](http://cldr.unicode.org/) - 语言环境数据
- [ISO 639 语言代码](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)

---

## 🤝 贡献翻译

我们欢迎社区贡献翻译！如果您想帮助翻译 Easy Comic 到您的语言：

1. 查看[CONTRIBUTING.md](CONTRIBUTING.md)了解贡献流程
2. 创建 Issue 说明您想添加的语言
3. Fork 仓库并添加翻译文件
4. 提交 Pull Request
5. 通过母语使用者审核

**让我们一起让 Easy Comic 为更多用户提供优质的本地化体验！** 🌍✨
