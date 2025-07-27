import 'dart:io';
import 'dart:typed_data';
import 'package:archive/archive.dart';
import 'package:path/path.dart' as p;

class ComicArchive {
  ComicArchive(this.path);

  final String path;
  Archive? _archive;

  Future<Archive> _getArchive() async {
    if (_archive != null) {
      return _archive!;
    }

    final fileExtension = p.extension(path).toLowerCase();
    if (!_isSupportedFormat(fileExtension)) {
      throw UnsupportedError('Unsupported file type: $fileExtension');
    }

    final file = File(path);
    final bytes = await file.readAsBytes();

    if (fileExtension == '.cbr') {
      throw UnsupportedError('CBR format is not yet supported');
    } else {
      _archive = ZipDecoder().decodeBytes(bytes);
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
      ..sort((a, b) => a.name.compareTo(b.name));

    return imageFiles.map((file) => file.name).toList();
  }

  Future<List<Uint8List>> listPages() async {
    final archive = await _getArchive();

    final imageFiles = archive.files
        .where((file) => file.isFile && _isImageFile(file.name))
        .toList()
      ..sort((a, b) => a.name.compareTo(b.name));

    final imageDataList = <Uint8List>[];
    for (final file in imageFiles) {
      if (file.content != null) {
        imageDataList.add(Uint8List.fromList(file.content as List<int>));
      }
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
}
