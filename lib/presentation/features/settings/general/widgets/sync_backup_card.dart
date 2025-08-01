import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';
import '../../../../../../domain/entities/reader_settings.dart';

class SyncBackupCard extends StatelessWidget {
  const SyncBackupCard({super.key});

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
                    Icon(Icons.sync_outlined, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '同步与备份',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // WebDAV 设置
                ListTile(
                  leading: const Icon(Icons.cloud_outlined),
                  title: const Text('WebDAV 服务器'),
                  subtitle: Text(
                    settings.webdavUrl.isNotEmpty 
                        ? settings.webdavUrl 
                        : '未配置',
                  ),
                  trailing: IconButton(
                    icon: const Icon(Icons.edit_outlined),
                    onPressed: () => _showWebdavConfigDialog(context, settings),
                  ),
                ),
                
                // 自动同步设置
                SwitchListTile(
                  secondary: const Icon(Icons.sync),
                  title: const Text('自动同步'),
                  subtitle: const Text('启动时自动同步阅读进度'),
                  value: settings.autoSync,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateAutoSync(value),
                    );
                  },
                ),
                
                // 同步频率
                if (settings.autoSync) ...[
                  ListTile(
                    leading: const Icon(Icons.schedule_outlined),
                    title: const Text('同步频率'),
                    subtitle: Text(_getSyncFrequencyText(settings.syncFrequency)),
                    trailing: DropdownButton<SyncFrequency>(
                      value: settings.syncFrequency,
                      onChanged: (value) {
                        if (value != null) {
                          context.read<SettingsBloc>().add(
                            UpdateSyncFrequency(value),
                          );
                        }
                      },
                      items: SyncFrequency.values.map((frequency) {
                        return DropdownMenuItem(
                          value: frequency,
                          child: Text(_getSyncFrequencyText(frequency)),
                        );
                      }).toList(),
                    ),
                  ),
                ],
                
                const Divider(),
                
                // 同步选项
                SwitchListTile(
                  secondary: const Icon(Icons.bookmark_outlined),
                  title: const Text('同步书签'),
                  subtitle: const Text('在设备间同步书签数据'),
                  value: settings.syncBookmarks,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateSyncBookmarks(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.settings_outlined),
                  title: const Text('同步设置'),
                  subtitle: const Text('在设备间同步应用设置'),
                  value: settings.syncSettings,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateSyncSettings(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.history_outlined),
                  title: const Text('同步阅读历史'),
                  subtitle: const Text('在设备间同步阅读记录'),
                  value: settings.syncReadingHistory,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateSyncReadingHistory(value),
                    );
                  },
                ),
                
                const Divider(),
                
                // 备份设置
                SwitchListTile(
                  secondary: const Icon(Icons.backup_outlined),
                  title: const Text('自动备份'),
                  subtitle: const Text('定期创建数据备份'),
                  value: settings.autoBackup,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateAutoBackup(value),
                    );
                  },
                ),
                
                if (settings.autoBackup) ...[
                  ListTile(
                    leading: const Icon(Icons.access_time_outlined),
                    title: const Text('备份间隔'),
                    subtitle: Text('${settings.backupInterval} 天'),
                    trailing: SizedBox(
                      width: 120,
                      child: Slider(
                        value: settings.backupInterval.toDouble(),
                        min: 1,
                        max: 30,
                        divisions: 29,
                        label: '${settings.backupInterval} 天',
                        onChanged: (value) {
                          context.read<SettingsBloc>().add(
                            UpdateBackupInterval(value.toInt()),
                          );
                        },
                      ),
                    ),
                  ),
                ],
                
                const Divider(),
                
                // 操作按钮
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: settings.webdavUrl.isNotEmpty
                            ? () => _performBackup(context)
                            : null,
                        icon: const Icon(Icons.backup),
                        label: const Text('立即备份'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: settings.webdavUrl.isNotEmpty
                            ? () => _performRestore(context)
                            : null,
                        icon: const Icon(Icons.restore),
                        label: const Text('恢复数据'),
                      ),
                    ),
                  ],
                ),
                
                const SizedBox(height: 8),
                
                // 同步状态
                if (settings.webdavUrl.isNotEmpty) ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Theme.of(context).primaryColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      children: [
                        Icon(
                          Icons.cloud_done_outlined,
                          color: Theme.of(context).primaryColor,
                          size: 20,
                        ),
                        const SizedBox(width: 8),
                        const Expanded(
                          child: Text(
                            '最后同步：2024-08-01 12:30',
                            style: TextStyle(fontSize: 12),
                          ),
                        ),
                      ],
                    ),
                  ),
                ] else ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.orange.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: const Row(
                      children: [
                        Icon(
                          Icons.cloud_off_outlined,
                          color: Colors.orange,
                          size: 20,
                        ),
                        SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            '未配置 WebDAV，点击上方配置服务器',
                            style: TextStyle(fontSize: 12),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          ),
        );
      },
    );
  }

  String _getSyncFrequencyText(SyncFrequency frequency) {
    switch (frequency) {
      case SyncFrequency.Manual:
        return '手动';
      case SyncFrequency.Hourly:
        return '每小时';
      case SyncFrequency.Daily:
        return '每日';
      case SyncFrequency.Weekly:
        return '每周';
    }
  }

  void _showWebdavConfigDialog(BuildContext context, ReaderSettings settings) {
    final urlController = TextEditingController(text: settings.webdavUrl);
    final usernameController = TextEditingController(text: settings.webdavUsername);
    final passwordController = TextEditingController(text: settings.webdavPassword);

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('WebDAV 配置'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: urlController,
                decoration: const InputDecoration(
                  labelText: '服务器地址',
                  hintText: 'https://example.com/webdav',
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: usernameController,
                decoration: const InputDecoration(
                  labelText: '用户名',
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: passwordController,
                decoration: const InputDecoration(
                  labelText: '密码',
                ),
                obscureText: true,
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              context.read<SettingsBloc>().add(
                UpdateWebdavSettings(
                  url: urlController.text,
                  username: usernameController.text,
                  password: passwordController.text,
                ),
              );
            },
            child: const Text('保存'),
          ),
        ],
      ),
    );
  }

  void _performBackup(BuildContext context) {
    context.read<SettingsBloc>().add(PerformBackup());
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('备份已开始...')),
    );
  }

  void _performRestore(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('恢复数据'),
        content: const Text('确定要从 WebDAV 服务器恢复数据吗？此操作将覆盖本地数据。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              context.read<SettingsBloc>().add(PerformRestore());
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('数据恢复已开始...')),
              );
            },
            child: const Text('确定'),
          ),
        ],
      ),
    );
  }
}