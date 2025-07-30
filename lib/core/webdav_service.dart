import 'dart:io' as io;
import 'package:webdav_client/webdav_client.dart';

/// 远程操作异常
class RemoteException implements Exception {
  RemoteException(this.message, [this.originalException]);

  final String message;
  final Exception? originalException;

  @override
  String toString() => 'RemoteException: $message';
}

/// WebDAV文件信息
class WebDAVFileInfo {
  WebDAVFileInfo({
    required this.name,
    required this.path,
    required this.isDirectory,
    this.size,
    this.modifiedTime,
    this.etag,
  });

  final String name;
  final String path;
  final bool isDirectory;
  final int? size;
  final DateTime? modifiedTime;
  final String? etag;

  @override
  String toString() =>
      'WebDAVFileInfo(name: $name, path: $path, isDirectory: $isDirectory, size: $size, etag: $etag)';
}

/// WebDAV服务类
class WebDAVService {
  WebDAVService({
    required this.host,
    required this.user,
    required this.password,
  }) {
    try {
      _client = newClient(host, user: user, password: password);
    } catch (e) {
      throw RemoteException(
        'Failed to initialize WebDAV client',
        e as Exception?,
      );
    }
  }

  late final Client _client;
  final String host;
  final String user;
  final String password;

  /// 列出目录内容
  Future<List<WebDAVFileInfo>> listDir(String remotePath) async {
    try {
      final files = await _client.readDir(remotePath);
      return files
          .map(
            (file) => WebDAVFileInfo(
              name: file.name ?? '',
              path: file.path ?? '',
              isDirectory: file.isDir ?? false,
              size: file.size,
              modifiedTime: file.mTime,
              etag: file.eTag,
            ),
          )
          .toList();
    } catch (e) {
      throw RemoteException(
        'Failed to list directory: $remotePath',
        e as Exception?,
      );
    }
  }

  /// 获取文件信息（包括etag）
  Future<WebDAVFileInfo?> getFileInfo(String remotePath) async {
    try {
      final stat = await _client.readDir(remotePath);
      if (stat.isEmpty) {
        return null;
      }

      final file = stat.first;
      return WebDAVFileInfo(
        name: file.name ?? '',
        path: file.path ?? '',
        isDirectory: file.isDir ?? false,
        size: file.size,
        modifiedTime: file.mTime,
        etag: file.eTag,
      );
    } catch (e) {
      return null; // 文件不存在
    }
  }

  /// 下载文件
  Future<void> download(String remotePath, String localPath) async {
    try {
      final bytes = await _client.read(remotePath);
      final file = io.File(localPath);

      // 确保本地目录存在
      await file.parent.create(recursive: true);
      await file.writeAsBytes(bytes);
    } catch (e) {
      throw RemoteException(
        'Failed to download file from $remotePath to $localPath',
        e as Exception?,
      );
    }
  }

  /// 上传文件
  Future<void> upload(String localPath, String remotePath) async {
    try {
      final file = io.File(localPath);
      if (!await file.exists()) {
        throw RemoteException('Local file does not exist: $localPath');
      }

      final bytes = await file.readAsBytes();
      await _client.write(remotePath, bytes);
    } catch (e) {
      throw RemoteException(
        'Failed to upload file from $localPath to $remotePath',
        e as Exception?,
      );
    }
  }

  /// 创建目录
  Future<void> mkdir(String remotePath) async {
    try {
      await _client.mkdir(remotePath);
    } catch (e) {
      throw RemoteException(
        'Failed to create directory: $remotePath',
        e as Exception?,
      );
    }
  }

  /// 删除文件或目录
  Future<void> remove(String remotePath) async {
    try {
      await _client.remove(remotePath);
    } catch (e) {
      throw RemoteException('Failed to remove: $remotePath', e as Exception?);
    }
  }

  /// 检查文件或目录是否存在
  Future<bool> exists(String remotePath) async {
    try {
      // 使用更高效的 HEAD 请求风格检查（通过 getFileInfo）
      final fileInfo = await getFileInfo(remotePath);
      return fileInfo != null;
    } catch (e) {
      return false;
    }
  }

  /// 移动/重命名文件
  Future<void> move(String fromPath, String toPath) async {
    try {
      // WebDAV客户端可能不支持move方法，使用copy + remove替代
      await copy(fromPath, toPath);
      await remove(fromPath);
    } catch (e) {
      throw RemoteException(
        'Failed to move from $fromPath to $toPath',
        e as Exception?,
      );
    }
  }

  /// 复制文件
  Future<void> copy(String fromPath, String toPath) async {
    try {
      // 先下载文件，再上传到新位置
      final tempFile = io.File('${io.Directory.systemTemp.path}/temp_copy_${DateTime.now().millisecondsSinceEpoch}');
      await download(fromPath, tempFile.path);
      await upload(tempFile.path, toPath);
      await tempFile.delete();
    } catch (e) {
      throw RemoteException(
        'Failed to copy from $fromPath to $toPath',
        e as Exception?,
      );
    }
  }
}
