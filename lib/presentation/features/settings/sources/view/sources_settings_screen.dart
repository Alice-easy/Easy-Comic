import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class SourcesSettingsScreen extends StatelessWidget {
  const SourcesSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('漫画源设置'),
      ),
      body: BlocBuilder<SettingsBloc, SettingsState>(
        builder: (context, state) {
          if (state is! SettingsLoaded) {
            return const Center(child: CircularProgressIndicator());
          }

          final settings = state.settings;

          return ListView(
            padding: const EdgeInsets.all(16.0),
            children: [
              _buildStorageLocationSection(context, settings),
              const Divider(height: 32),
              _buildSupportedFormatsSection(context),
            ],
          );
        },
      ),
    );
  }

  Widget _buildStorageLocationSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '存储位置',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.download_outlined),
          title: const Text('默认下载路径'),
          subtitle: Text(settings.defaultDownloadPath),
          trailing: IconButton(
            icon: const Icon(Icons.folder_open),
            onPressed: () => _showPathSelector(context, 'download'),
          ),
          contentPadding: EdgeInsets.zero,
        ),
        ListTile(
          leading: const Icon(Icons.storage_outlined),
          title: const Text('缓存路径'),
          subtitle: Text(settings.cacheDirectory),
          trailing: IconButton(
            icon: const Icon(Icons.folder_open),
            onPressed: () => _showPathSelector(context, 'cache'),
          ),
          contentPadding: EdgeInsets.zero,
        ),
      ],
    );
  }

  Widget _buildSupportedFormatsSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '支持的格式',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        const Text(
          '应用当前支持以下漫画格式：',
          style: TextStyle(fontSize: 14),
        ),
        const SizedBox(height: 8),
        const Wrap(
          spacing: 8.0,
          runSpacing: 4.0,
          children: [
            Chip(label: Text('CBZ')),
            Chip(label: Text('CBR')),
            Chip(label: Text('ZIP')),
            Chip(label: Text('RAR')),
            Chip(label: Text('文件夹')),
          ],
        ),
      ],
    );
  }

  void _showPathSelector(BuildContext context, String type) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('选择${type == 'download' ? '下载' : '缓存'}路径'),
        content: const Text('路径选择功能将在后续版本中实现'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}