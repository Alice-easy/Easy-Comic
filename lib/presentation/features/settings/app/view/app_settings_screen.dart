import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class AppSettingsScreen extends StatelessWidget {
  const AppSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('APP设置'),
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
              _buildCacheManagementSection(context, settings),
              const Divider(height: 32),
              _buildPermissionsSection(context),
              const Divider(height: 32),
              _buildDebugSection(context, settings),
            ],
          );
        },
      ),
    );
  }

  Widget _buildCacheManagementSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '缓存管理',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.cached),
          title: const Text('图像缓存大小'),
          subtitle: Text('${settings.imageCacheSize} MB'),
          trailing: SizedBox(
            width: 150,
            child: Slider(
              value: settings.imageCacheSize.toDouble(),
              min: 50,
              max: 1000,
              divisions: 19,
              label: '${settings.imageCacheSize.toInt()} MB',
              onChanged: (value) {
                context.read<SettingsBloc>().add(UpdateImageCacheSize(value.toInt()));
              },
            ),
          ),
        ),
        ListTile(
          leading: const Icon(Icons.storage),
          title: const Text('磁盘缓存大小'),
          subtitle: Text('${settings.diskCacheSize} MB'),
          trailing: SizedBox(
            width: 150,
            child: Slider(
              value: settings.diskCacheSize.toDouble(),
              min: 100,
              max: 5000,
              divisions: 49,
              label: '${settings.diskCacheSize.toInt()} MB',
              onChanged: (value) {
                context.read<SettingsBloc>().add(UpdateDiskCacheSize(value.toInt()));
              },
            ),
          ),
        ),
        const SizedBox(height: 8),
        OutlinedButton.icon(
          onPressed: () => _showClearCacheDialog(context),
          icon: const Icon(Icons.clear_all),
          label: const Text('清理缓存'),
        ),
      ],
    );
  }

  Widget _buildPermissionsSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '权限管理',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.folder_open_outlined),
          title: const Text('存储权限'),
          subtitle: const Text('用于访问本地漫画文件'),
          trailing: const Text('已授予'),
          onTap: () {
            // TODO: Open app settings
          },
        ),
      ],
    );
  }

  Widget _buildDebugSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '调试选项',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          title: const Text('开发者模式'),
          subtitle: const Text('启用详细日志和调试信息'),
          value: settings.debugMode,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateDebugMode(value));
          },
        ),
      ],
    );
  }

  void _showClearCacheDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('清理缓存'),
        content: const Text('确定要清理所有缓存文件吗？此操作不可撤销。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              context.read<SettingsBloc>().add(ClearCache());
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('缓存清理完成')),
              );
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}