# 应用签名配置指南

## 📋 概述

本文档说明如何为 Easy Comic 应用配置签名，以便发布到 Google Play Store 或其他应用商店。

## 🔑 生成 Release Keystore

### 1. 使用 Android Studio 生成

1. 在 Android Studio 中，选择菜单 `Build` > `Generate Signed Bundle / APK`
2. 选择 `Android App Bundle` 或 `APK`
3. 点击 `Create new...` 创建新的 keystore
4. 填写以下信息：
   - **Key store path**: `<项目根目录>/keystore/release.keystore`
   - **Password**: 设置一个强密码
   - **Key alias**: `easy-comic-release`
   - **Key password**: 设置密钥密码（可与 keystore 密码相同）
   - **Validity (years)**: 25 年或更长
   - **Certificate**: 填写个人或公司信息

### 2. 使用命令行生成

```bash
# 确保在项目根目录下
cd /path/to/Easy-Comic

# 生成release keystore
keytool -genkey -v -keystore keystore/release.keystore -alias easy-comic-release -keyalg RSA -keysize 2048 -validity 10000

# 按提示输入信息：
# - Keystore密码
# - 密钥密码
# - 您的姓名
# - 组织单位
# - 组织
# - 城市
# - 省份
# - 国家代码 (CN)
```

## 🔧 配置签名信息

### 1. 本地开发配置

在项目根目录创建 `keystore.properties` 文件（不要提交到 Git）：

```properties
storePassword=your_keystore_password
keyPassword=your_key_password
keyAlias=easy-comic-release
storeFile=keystore/release.keystore
```

### 2. 手动发布配置

对于手动发布，在本地配置签名信息：

```
SIGNING_KEY_ALIAS=easy-comic-release
SIGNING_KEY_PASSWORD=your_key_password
SIGNING_STORE_PASSWORD=your_keystore_password
SIGNING_STORE_FILE=keystore/release.keystore
```

### 3. 更新 gradle.properties

在项目的 `gradle.properties` 文件中添加：

```properties
# 签名配置（默认值）
SIGNING_KEY_ALIAS=easy-comic-release
SIGNING_STORE_FILE=keystore/release.keystore
# 密码通过环境变量或keystore.properties提供
```

## 🏗️ 构建签名 APK

### 1. 本地构建

```bash
# 构建Release AAB (推荐用于Google Play)
./gradlew bundleRelease

# 构建Release APK
./gradlew assembleRelease

# 生成的文件位置：
# AAB: app/build/outputs/bundle/release/app-release.aab
# APK: app/build/outputs/apk/release/app-release.apk
```

### 2. 验证签名

```bash
# 验证APK签名
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# 查看签名信息
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

## 🔒 安全最佳实践

### 1. Keystore 安全

- **备份 keystore 文件**：丢失 keystore 将无法更新应用
- **安全存储密码**：使用密码管理器
- **限制访问权限**：只有必要的人员才能访问
- **定期轮换密码**：建议每年更换一次密码

### 2. 版本控制

```bash
# 将keystore目录添加到.gitignore
echo "keystore/" >> .gitignore
echo "keystore.properties" >> .gitignore

# 确保敏感文件不被提交
git rm --cached keystore/release.keystore 2>/dev/null || true
git rm --cached keystore.properties 2>/dev/null || true
```

### 3. 环境分离

```
Development (debug):
- 使用debug keystore
- ApplicationId: com.easycomic.debug

Beta Testing:
- 使用release keystore
- ApplicationId: com.easycomic.beta

Production:
- 使用release keystore
- ApplicationId: com.easycomic
```

## 📱 发布流程

### 1. 版本准备

```bash
# 1. 更新版本号
# 编辑 app/build.gradle.kts
versionCode = 2
versionName = "0.7.0"

# 2. 更新CHANGELOG.md
# 添加新版本的更新内容

# 3. 提交更改
git add .
git commit -m "Release v0.7.0"
git tag v0.7.0
git push origin main --tags
```

### 2. 手动发布

手动构建和发布：

```bash
# 构建发布版本
./gradlew assembleRelease
./gradlew bundleRelease

# 手动创建 tag
git tag v0.7.0
git push origin v0.7.0
```

### 3. Google Play Console

1. 登录 [Google Play Console](https://play.google.com/console/)
2. 选择 Easy Comic 应用
3. 上传 AAB 文件到相应 track（内测、公开测试、生产）
4. 填写发布说明
5. 提交审核

## 🔧 故障排除

### 常见问题

1. **签名不匹配**

   ```
   错误: The apk must be signed with the same certificates as the previous version
   ```

   解决：确保使用相同的 keystore 和 alias

2. **Keystore 密码错误**

   ```
   错误: Keystore was tampered with, or password was incorrect
   ```

   解决：检查 keystore.properties 中的密码

3. **找不到 keystore 文件**
   ```
   错误: Keystore file not found
   ```
   解决：检查文件路径，确保 keystore 文件存在

### 调试命令

```bash
# 查看keystore信息
keytool -list -v -keystore keystore/release.keystore

# 查看APK签名
aapt dump badging app/build/outputs/apk/release/app-release.apk

# 验证AAB文件
bundletool validate --bundle=app/build/outputs/bundle/release/app-release.aab
```

## 📚 相关文档

- [Android 官方签名文档](https://developer.android.com/studio/publish/app-signing)
- [Google Play 发布指南](https://support.google.com/googleplay/android-developer/answer/9859348)
- [AAB 格式说明](https://developer.android.com/guide/app-bundle)

---

**重要提醒**:

- 始终备份您的 keystore 文件
- 绝不要将 keystore 文件提交到版本控制系统
- 使用强密码保护您的签名密钥
- 定期测试发布流程以确保配置正确
