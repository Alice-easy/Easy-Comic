import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';
import '../../../../../../domain/entities/reader_settings.dart';

class FileManagementCard extends StatelessWidget {
  const FileManagementCard({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SettingsBloc, SettingsState>(
      builder: (context, state) {
        if (state is! SettingsLoaded) {
          return const Card(
            child: Padding(
              padding: EdgeInsets.all(16.0),
              child: Center(child: CircularProgressIndicator()),
            ),
          );
        }

        final settings = state.settings;
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.folder_outlined, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '文件管理',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // 默认下载路径
                ListTile(
                  leading: const Icon(Icons.download_outlined),
                  title: const Text('默认下载路径'),
                  subtitle: Text(settings.defaultDownloadPath),
                  trailing: IconButton(
                    icon: const Icon(Icons.folder_open),
                    onPressed: () => _showPathSelector(context, 'download'),
                  ),
                ),
                
                // 缓存路径
                ListTile(
                  leading: const Icon(Icons.storage_outlined),
                  title: const Text('缓存路径'),
                  subtitle: Text(settings.cacheDirectory),
                  trailing: IconButton(
                    icon: const Icon(Icons.folder_open),
                    onPressed: () => _showPathSelector(context, 'cache'),
                  ),
                ),
                
                const Divider(),
                
                // 缓存设置
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
                        context.read<SettingsBloc>().add(
                          UpdateImageCacheSize(value.toInt()),
                        );
                      },
                    ),
                  ),
                ),
                
                // 磁盘缓存大小
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
                        context.read<SettingsBloc>().add(
                          UpdateDiskCacheSize(value.toInt()),
                        );
                      },
                    ),
                  ),
                ),
                
                const Divider(),
                
                // 文件处理选项
                SwitchListTile(
                  secondary: const Icon(Icons.auto_delete_outlined),
                  title: const Text('自动清理临时文件'),
                  subtitle: const Text('定期清理解压缓存'),
                  value: settings.autoCleanTempFiles,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateAutoCleanTempFiles(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.delete_sweep_outlined),
                  title: const Text('退出时清理缓存'),
                  subtitle: const Text('应用关闭时自动清理图像缓存'),
                  value: settings.clearCacheOnExit,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateClearCacheOnExit(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.memory_outlined),
                  title: const Text('智能内存管理'),
                  subtitle: const Text('根据系统内存自动调整缓存'),
                  value: settings.smartMemoryManagement,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateSmartMemoryManagement(value),
                    );
                  },
                ),
                
                const Divider(),
                
                // 操作按钮
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _showClearCacheDialog(context),
                        icon: const Icon(Icons.clear_all),
                        label: const Text('清理缓存'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => _showStorageInfoDialog(context, settings),
                        icon: const Icon(Icons.info_outline),
                        label: const Text('存储信息'),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
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

  void _showStorageInfoDialog(BuildContext context, ReaderSettings settings) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('存储信息'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildStorageRow('图像缓存', '${settings.imageCacheSize} MB'),
            _buildStorageRow('磁盘缓存', '${settings.diskCacheSize} MB'),
            _buildStorageRow('下载路径', settings.defaultDownloadPath),
            _buildStorageRow('缓存路径', settings.cacheDirectory),
            const Divider(),
            const Text(
              '注：实际使用量可能与设置值有差异',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }

  Widget _buildStorageRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          const Text(': '),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }
}