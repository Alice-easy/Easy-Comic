import 'dart:io';
import 'dart:typed_data';
import 'package:archive/archive.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart' as p;

/// Performs a "natural sort" on strings, correctly handling numbers.
/// For example, "page10.jpg" will come after "page2.jpg".
int naturalCompare(String s1, String s2) {
  final re = RegExp(r'([0-9]+|[^0-9]+)');
  final s1parts = re.allMatches(s1).map((m) => m.group(0)!).toList();
  final s2parts = re.allMatches(s2).map((m) => m.group(0)!).toList();

  final len = s1parts.length < s2parts.length ? s1parts.length : s2parts.length;

  for (var i = 0; i < len; i++) {
    final p1 = s1parts[i];
    final p2 = s2parts[i];
    final n1 = int.tryParse(p1);
    final n2 = int.tryParse(p2);

    if (n1 != null && n2 != null) {
      final cmp = n1.compareTo(n2);
      if (cmp != 0) return cmp;
    } else {
      final cmp = p1.compareTo(p2);
      if (cmp != 0) return cmp;
    }
  }

  return s1parts.length.compareTo(s2parts.length);
}

class ComicArchive {
  ComicArchive({this.path, this.bytes})
      : assert(
          path != null || bytes != null,
          'Either path or bytes must be provided.',
        );

  final String? path;
  final Uint8List? bytes;
  Archive? _archive;

  // In a real app, this would use a platform channel to read the URI content.
  static Future<ComicArchive> fromUri(String uri) async {
    // This is a mock implementation.
    // A real implementation would use a platform channel like this:
    // final Uint8List bytes = await MethodChannel('com.example.easy_comic/saf')
    //     .invokeMethod('readFile', {'uri': uri});
    // For now, we'll throw an error if we try to use a URI directly.
    if (uri.startsWith('content://')) {
      throw UnsupportedError(
        'Reading from content URIs requires a platform channel implementation.',
      );
    }
    // Fallback for regular paths for testing purposes.
    final file = File(uri);
    final bytes = await file.readAsBytes();
    return ComicArchive(bytes: bytes, path: uri);
  }

  Future<Archive> _getArchive() async {
    if (_archive != null) {
      return _archive!;
    }

    final fileExtension = p.extension(path ?? 'file.cbz').toLowerCase();
    if (!_isSupportedFormat(fileExtension)) {
      throw UnsupportedError('Unsupported file type: $fileExtension');
    }

    Uint8List fileBytes;
    if (bytes != null) {
      fileBytes = bytes!;
    } else {
      final file = File(path!);
      fileBytes = await file.readAsBytes();
    }

    if (fileExtension == '.cbr') {
      throw UnsupportedError('CBR format is not yet supported');
    } else {
      _archive = ZipDecoder().decodeBytes(fileBytes);
    }

    return _archive!;
  }

  bool _isSupportedFormat(String extension) =>
      extension == '.zip' || extension == '.cbz' || extension == '.cbr';

  Future<List<String>> listPageNames() async {
    final archive = await _getArchive();

    final imageFiles = archive.files
        .where((file) => file.isFile && _isImageFile(file.name))
        .toList()
      ..sort((a, b) => naturalCompare(a.name, b.name));

    return imageFiles.map((file) => file.name).toList();
  }

  Future<List<Uint8List>> listPages() async {
    final archive = await _getArchive();

    final imageFiles = archive.files
        .where((file) => file.isFile && _isImageFile(file.name))
        .toList()
      ..sort((a, b) => naturalCompare(a.name, b.name));

    final imageDataList = <Uint8List>[];
    for (final file in imageFiles) {
      imageDataList.add(Uint8List.fromList(file.content as List<int>));
    }

    return imageDataList;
  }

  bool _isImageFile(String filename) {
    final extension = filename.toLowerCase();
    return extension.endsWith('.jpg') ||
        extension.endsWith('.jpeg') ||
        extension.endsWith('.png') ||
        extension.endsWith('.gif') ||
        extension.endsWith('.webp');
  }

  /// 获取漫画封面图像
  Future<Uint8List?> getCoverImage() async {
    final archive = await _getArchive();

    // 查找第一个图像文件作为封面
    final imageFiles = archive.files
        .where((file) => file.isFile && _isImageFile(file.name))
        .toList()
      ..sort((a, b) => naturalCompare(a.name, b.name));

    if (imageFiles.isEmpty) {
      return null;
    }

    return Uint8List.fromList(imageFiles.first.content as List<int>);
  }
}
