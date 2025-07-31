import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:easy_comic/presentation/pages/reader_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Easy Comic'),
      ),
      body: Center(
        child: ElevatedButton(
          onPressed: () async {
            final result = await FilePicker.platform.pickFiles(
              type: FileType.custom,
              allowedExtensions: ['zip', 'cbz'],
            );

            if (result != null && result.files.single.path != null) {
              final filePath = result.files.single.path!;
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ReaderScreen(filePath: filePath),
                ),
              );
            }
          },
          child: const Text('选择漫画'),
        ),
      ),
    );
  }
}