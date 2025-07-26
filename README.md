# Easy Comic - 您的专属漫画阅读器

![Easy Comic Icon](assets/icon/icon.png)

**Easy Comic** 是一款使用 Flutter 构建的、简洁且开源的漫画阅读器。它让您可以轻松地在设备上阅读本地漫画，并提供了将您的漫画收藏备份到自己的 WebDAV 服务器的选项，确保您的数据安全且私密。

## ✨ 主要功能

- **本地阅读**: 直接从您的设备存储中选择图片文件或整个文件夹来创建和阅读漫画。
- **流畅的阅读体验**: 提供流畅的左右翻页体验，专注于阅读本身。
- **WebDAV 备份**: 支持将您的漫画文件上传到您自己的 WebDAV 服务器，方便您在不同设备间同步或备份。
- **个性化设置**: 可自由配置 WebDAV 服务器地址、用户名和密码。
- **跨平台**: 基于 Flutter 构建，未来可以轻松扩展到其他平台。

## 🚀 开始使用

请确保您已在本地安装并配置好 Flutter 开发环境。

### 环境要求

- Flutter SDK: `^3.8.1`
- Dart SDK: `^3.8.1`

### 安装与运行

1.  **克隆仓库**
    ```sh
    git clone https://github.com/your_username/easy_comic.git
    cd easy_comic
    ```

2.  **安装依赖**
    ```sh
    flutter pub get
    ```

3.  **运行应用**
    ```sh
    flutter run
    ```

## 📦 主要依赖

本应用使用了以下优秀的开源库：

- [`image_picker`](https://pub.dev/packages/image_picker): 用于从设备相册中选择图片。
- [`webdav_client`](https://pub.dev/packages/webdav_client): 用于与 WebDAV 服务器进行通信。
- [`shared_preferences`](https://pub.dev/packages/shared_preferences): 用于在本地存储应用设置。
- [`flutter_launcher_icons`](https://pub.dev/packages/flutter_launcher_icons): 用于自动生成应用图标。

感谢这些项目的开发者们！

