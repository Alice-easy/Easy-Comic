import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class NetworkSettingsScreen extends StatelessWidget {
  const NetworkSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('网络设置'),
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
              _buildWebDavSection(context, settings),
              const Divider(height: 32),
              _buildDownloadSection(context, settings),
            ],
          );
        },
      ),
    );
  }

  Widget _buildWebDavSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'WebDAV 同步',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.cloud_outlined),
          title: const Text('WebDAV 服务器'),
          subtitle: Text(
            settings.webdavUrl.isNotEmpty ? settings.webdavUrl : '未配置',
          ),
          trailing: IconButton(
            icon: const Icon(Icons.edit_outlined),
            onPressed: () => _showWebdavConfigDialog(context, settings),
          ),
          contentPadding: EdgeInsets.zero,
        ),
        SwitchListTile(
          title: const Text('自动同步'),
          subtitle: const Text('启动时自动同步阅读进度'),
          value: settings.autoSync,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateAutoSync(value));
          },
        ),
      ],
    );
  }

  Widget _buildDownloadSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '下载偏好',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        // Add download-related settings here in the future
        const ListTile(
          leading: Icon(Icons.wifi_off_outlined),
          title: Text('仅在Wi-Fi下下载'),
          trailing: Text('即将推出'),
        ),
      ],
    );
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
}