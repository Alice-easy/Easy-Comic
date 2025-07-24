import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:easy_comic/backup/webdav_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ComicReaderPage extends StatefulWidget {
  const ComicReaderPage({super.key});

  @override
  State<ComicReaderPage> createState() => _ComicReaderPageState();
}

class _ComicReaderPageState extends State<ComicReaderPage> {
  List<File> _images = [];

  Future<void> _pickImages() async {
    final pickedFiles = await ImagePicker().pickMultiImage();

    if (pickedFiles.isNotEmpty) {
      setState(() {
        _images = pickedFiles.map((file) => File(file.path)).toList();
      });
    }
  }

  Future<void> _uploadComic() async {
    final prefs = await SharedPreferences.getInstance();
    final host = prefs.getString('webdav_host');
    final user = prefs.getString('webdav_user');
    final password = prefs.getString('webdav_password');

    if (host == null || user == null || password == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please configure WebDAV settings first')),
      );
      return;
    }

    final webdavService = WebdavService(host, user, password);
    for (final image in _images) {
      try {
        await webdavService.uploadFile(
          image.path,
          '/comics/${image.path.split('/').last}',
        );
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to upload ${image.path}: $e')),
        );
      }
    }

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Comic uploaded successfully')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Comic Reader'),
        actions: [
          IconButton(icon: const Icon(Icons.backup), onPressed: _uploadComic),
        ],
      ),
      body: _images.isEmpty
          ? const Center(child: Text('No images selected.'))
          : PageView.builder(
              itemCount: _images.length,
              itemBuilder: (context, index) {
                return Image.file(_images[index], fit: BoxFit.contain);
              },
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _pickImages,
        tooltip: 'Pick Images',
        child: const Icon(Icons.add_a_photo),
      ),
    );
  }
}
