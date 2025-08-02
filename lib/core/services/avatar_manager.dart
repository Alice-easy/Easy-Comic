import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:image_cropper/image_cropper.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:uuid/uuid.dart';

class AvatarManager {
  final ImagePicker _picker = ImagePicker();
  final ImageCropper _cropper = ImageCropper();

  Future<File?> pickAndCropImage() async {
    final XFile? pickedFile = await _picker.pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      final CroppedFile? croppedFile = await _cropper.cropImage(
        sourcePath: pickedFile.path,
        aspectRatio: const CropAspectRatio(ratioX: 1, ratioY: 1),
        uiSettings: [
          AndroidUiSettings(
              toolbarTitle: '裁剪头像',
              toolbarColor: const Color(0xFF212121),
              toolbarWidgetColor: Colors.white,
              initAspectRatio: CropAspectRatioPreset.original,
              lockAspectRatio: false),
          IOSUiSettings(
            title: '裁剪头像',
          ),
        ],
      );

      if (croppedFile != null) {
        return await _saveImageLocally(File(croppedFile.path));
      }
    }
    return null;
  }

  Future<File> _saveImageLocally(File image) async {
    final directory = await getApplicationDocumentsDirectory();
    final String fileExtension = p.extension(image.path);
    final String newFileName = '${const Uuid().v4()}$fileExtension';
    final String newPath = p.join(directory.path, newFileName);
    return await image.copy(newPath);
  }
}