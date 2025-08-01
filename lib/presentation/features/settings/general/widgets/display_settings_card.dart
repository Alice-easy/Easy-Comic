// lib/presentation/features/settings/general/widgets/display_settings_card.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';

class DisplaySettingsCard extends StatelessWidget {
  const DisplaySettingsCard({super.key});

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
                    Icon(Icons.display_settings, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '显示设置',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // 默认缩放级别
                _buildZoomLevelSection(context, settings),
                const Divider(height: 32),
                
                // 屏幕方向
                _buildScreenOrientationSection(context, settings),
                const Divider(height: 32),
                
                // 系统UI控制
                _buildSystemUISection(context, settings),
                const Divider(height: 32),
                
                // 屏幕常亮
                _buildScreenAwakeSection(context, settings),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildZoomLevelSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '默认缩放级别',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Text(
          '缩放: ${(settings.defaultZoomLevel * 100).round()}%',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        Slider(
          value: settings.defaultZoomLevel,
          min: 0.5,
          max: 3.0,
          divisions: 25,
          label: '${(settings.defaultZoomLevel * 100).round()}%',
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateDefaultZoomLevel(value));
          },
        ),
        Text(
          '设置打开漫画时的默认缩放级别',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildScreenOrientationSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '屏幕方向',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: ScreenOrientation.values.map((orientation) {
            return ChoiceChip(
              label: Text(_getScreenOrientationLabel(orientation)),
              selected: settings.screenOrientation == orientation,
              onSelected: (selected) {
                if (selected) {
                  context.read<SettingsBloc>().add(UpdateScreenOrientation(orientation));
                }
              },
            );
          }).toList(),
        ),
        Text(
          '控制阅读时的屏幕方向锁定设置',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildSystemUISection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '系统界面控制',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 16),
        
        // 隐藏状态栏
        SwitchListTile(
          title: const Text('隐藏状态栏'),
          subtitle: const Text('阅读时隐藏顶部状态栏'),
          value: settings.hideStatusBar,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateHideStatusBar(value));
          },
          contentPadding: EdgeInsets.zero,
        ),
        
        // 隐藏导航栏
        SwitchListTile(
          title: const Text('隐藏导航栏'),
          subtitle: const Text('阅读时隐藏底部导航栏'),
          value: settings.hideNavigationBar,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateHideNavigationBar(value));
          },
          contentPadding: EdgeInsets.zero,
        ),
        
        // 全屏模式
        SwitchListTile(
          title: const Text('沉浸式全屏'),
          subtitle: const Text('启用完全沉浸式阅读体验'),
          value: settings.fullScreenMode,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateFullScreenMode(value));
          },
          contentPadding: EdgeInsets.zero,
        ),
      ],
    );
  }

  Widget _buildScreenAwakeSection(BuildContext context, ReaderSettings settings) {
    return SwitchListTile(
      title: const Text('屏幕常亮'),
      subtitle: const Text('阅读时保持屏幕常亮，防止自动锁屏'),
      value: settings.keepScreenAwake,
      onChanged: (value) {
        context.read<SettingsBloc>().add(UpdateKeepScreenAwake(value));
      },
      contentPadding: EdgeInsets.zero,
    );
  }

  String _getScreenOrientationLabel(ScreenOrientation orientation) {
    switch (orientation) {
      case ScreenOrientation.Portrait:
        return '竖屏';
      case ScreenOrientation.Landscape:
        return '横屏';
      case ScreenOrientation.Auto:
        return '自动';
    }
  }
}