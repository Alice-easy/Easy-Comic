import 'dart:io';

import 'package:file_picker/file_picker.dart';

abstract class FilePickerService {
  Future<File?>pickFile();
}

class FilePickerServiceImpl implements FilePickerService {
  @override
  Future<File?> pickFile() async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['zip', 'cbz'],
    );

    if (result != null && result.files.single.path != null) {
      return File(result.files.single.path!);
    }
    return null;
  }
}