import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class CollectionSettingsScreen extends StatelessWidget {
  const CollectionSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('本地收藏设置'),
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
              _buildSyncOptionsSection(context, settings),
              const Divider(height: 32),
              _buildManagementSection(context),
            ],
          );
        },
      ),
    );
  }

  Widget _buildSyncOptionsSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '同步选项',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          title: const Text('同步书签'),
          subtitle: const Text('在设备间同步书签数据'),
          value: settings.syncBookmarks,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateSyncBookmarks(value));
          },
        ),
        SwitchListTile(
          title: const Text('同步阅读历史'),
          subtitle: const Text('在设备间同步阅读记录'),
          value: settings.syncReadingHistory,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateSyncReadingHistory(value));
          },
        ),
      ],
    );
  }

  Widget _buildManagementSection(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '数据管理',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        ListTile(
          leading: const Icon(Icons.bookmarks_outlined),
          title: const Text('管理书签'),
          trailing: const Icon(Icons.chevron_right),
          onTap: () {
            // TODO: Navigate to bookmarks management screen
          },
        ),
        ListTile(
          leading: const Icon(Icons.favorite_border),
          title: const Text('管理收藏夹'),
          trailing: const Icon(Icons.chevron_right),
          onTap: () {
            // TODO: Navigate to favorites management screen
          },
        ),
        ListTile(
          leading: const Icon(Icons.history),
          title: const Text('清除阅读历史'),
          onTap: () {
            // TODO: Show confirmation dialog and clear history
          },
        ),
      ],
    );
  }
}