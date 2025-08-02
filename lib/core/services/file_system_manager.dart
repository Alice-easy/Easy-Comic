import 'dart:io';

abstract class FileSystemManager {
  bool isZipFile(String path);
  Future<bool> isDirectory(String path);
  Future<List<String>> getFilesInDirectory(String path);
}

class FileSystemManagerImpl implements FileSystemManager {
  @override
  bool isZipFile(String path) {
    return path.toLowerCase().endsWith('.zip') || path.toLowerCase().endsWith('.cbz');
  }

  @override
  Future<bool> isDirectory(String path) {
    return Directory(path).exists();
  }

  @override
  Future<List<String>> getFilesInDirectory(String path) async {
    final dir = Directory(path);
    final files = await dir.list().toList();
    return files.map((f) => f.path).toList();
  }
}