import 'package:flutter/material.dart';
import 'package:easy_comic/reader/comic_reader_page.dart';
import 'package:easy_comic/settings/settings_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '轻松看漫画',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('轻松看漫画'),
        elevation: 4,
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'settings') {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SettingsPage()),
                );
              } else if (value == 'about') {
                showAboutDialog(
                  context: context,
                  applicationName: '轻松看漫画',
                  applicationVersion: '1.0.0',
                  applicationLegalese: '© 2024 The Easy Comic Authors',
                );
              }
            },
            itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
              const PopupMenuItem<String>(
                value: 'settings',
                child: Text('设置'),
              ),
              const PopupMenuItem<String>(
                value: 'about',
                child: Text('关于'),
              ),
            ],
          ),
        ],
      ),
      body: Center(
        child: FilledButton(
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const ComicReaderPage()),
            );
          },
          child: const Text('打开漫画'),
        ),
      ),
    );
  }
}
