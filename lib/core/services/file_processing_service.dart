import 'dart:io';
import 'dart:typed_data';

import 'package:archive/archive_io.dart';
import 'package:image/image.dart' as img;
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

abstract class FileProcessingService {
  Future<ProcessedMangaData> processMangaFile(File file);
  Future<List<String>> getPageImagePaths(String archivePath);
}

class ProcessedMangaData {
  final int pageCount;
  final String coverImagePath;

  ProcessedMangaData({required this.pageCount, required this.coverImagePath});
}

class FileProcessingServiceImpl implements FileProcessingService {
  @override
  Future<ProcessedMangaData> processMangaFile(File file) async {
    final bytes = await file.readAsBytes();
    final archive = ZipDecoder().decodeBytes(bytes);

    // Filter for image files and sort them
    final imageFiles = archive.files
        .where((file) =>
            !file.isDirectory &&
            (file.name.endsWith('.jpg') ||
                file.name.endsWith('.jpeg') ||
                file.name.endsWith('.png')))
        .toList();
    imageFiles.sort((a, b) => a.name.compareTo(b.name));

    if (imageFiles.isEmpty) {
      throw Exception('No images found in the archive.');
    }

    // Generate thumbnail from the first image
    final firstImageFile = imageFiles.first;
    final image = img.decodeImage(firstImageFile.content as Uint8List);
    if (image == null) {
      throw Exception('Could not decode the first image.');
    }

    final thumbnail = img.copyResize(image, width: 200);
    final thumbnailBytes = img.encodeJpg(thumbnail);

    // Save thumbnail to a permanent location
    final documentsDir = await getApplicationDocumentsDirectory();
    final coverDir = Directory(p.join(documentsDir.path, 'covers'));
    if (!await coverDir.exists()) {
      await coverDir.create(recursive: true);
    }
    final coverPath = p.join(coverDir.path, '${p.basenameWithoutExtension(file.path)}.jpg');
    await File(coverPath).writeAsBytes(thumbnailBytes);

    return ProcessedMangaData(
      pageCount: imageFiles.length,
      coverImagePath: coverPath,
    );
  }

  @override
  Future<List<String>> getPageImagePaths(String archivePath) async {
    final file = File(archivePath);
    final bytes = await file.readAsBytes();
    final archive = ZipDecoder().decodeBytes(bytes);

    final imageFiles = archive.files
        .where((file) =>
            !file.isDirectory &&
            (file.name.endsWith('.jpg') ||
                file.name.endsWith('.jpeg') ||
                file.name.endsWith('.png')))
        .toList();
    imageFiles.sort((a, b) => a.name.compareTo(b.name));

    final tempDir = await getTemporaryDirectory();
    final mangaId = p.basenameWithoutExtension(archivePath);
    final pagesDir = Directory(p.join(tempDir.path, 'manga_pages', mangaId));

    if (await pagesDir.exists()) {
      // Assuming if the directory exists, the pages are already extracted
      // A more robust implementation would check file integrity
      return imageFiles.map((f) => p.join(pagesDir.path, f.name)).toList();
    }
    
    await pagesDir.create(recursive: true);

    final extractedPagePaths = <String>[];
    for (var imageFile in imageFiles) {
      final imagePath = p.join(pagesDir.path, imageFile.name);
      await File(imagePath).writeAsBytes(imageFile.content as Uint8List);
      extractedPagePaths.add(imagePath);
    }

    return extractedPagePaths;
  }
}