import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class AppearanceSettingsScreen extends StatelessWidget {
  const AppearanceSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('外观设置'),
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
              _buildVisualEffectsSection(context, settings),
              const Divider(height: 32),
              _buildDisplaySection(context, settings),
            ],
          );
        },
      ),
    );
  }

  Widget _buildVisualEffectsSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '视觉效果',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 16),
        _buildBackgroundColorSection(context, settings),
        const SizedBox(height: 16),
        _buildImageBrightnessSection(context, settings),
        const SizedBox(height: 16),
        _buildContrastSection(context, settings),
        const SizedBox(height: 16),
        _buildSaturationSection(context, settings),
        const SizedBox(height: 16),
        _buildNightModeSection(context, settings),
      ],
    );
  }

  Widget _buildDisplaySection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '显示',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 16),
        _buildScreenOrientationSection(context, settings),
        const SizedBox(height: 16),
        _buildSystemUISection(context, settings),
        const SizedBox(height: 16),
        _buildScreenAwakeSection(context, settings),
      ],
    );
  }

  Widget _buildBackgroundColorSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from VisualEffectsCard for background color
    return const Text('背景色设置将在这里实现');
  }

  Widget _buildImageBrightnessSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from VisualEffectsCard for brightness
    return const Text('亮度设置将在这里实现');
  }

  Widget _buildContrastSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from VisualEffectsCard for contrast
    return const Text('对比度设置将在这里实现');
  }

  Widget _buildSaturationSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from VisualEffectsCard for saturation
    return const Text('饱和度设置将在这里实现');
  }

  Widget _buildNightModeSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from VisualEffectsCard for night mode
    return const Text('夜间模式设置将在这里实现');
  }

  Widget _buildScreenOrientationSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from DisplaySettingsCard for screen orientation
    return const Text('屏幕方向设置将在这里实现');
  }

  Widget _buildSystemUISection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from DisplaySettingsCard for system UI
    return const Text('系统UI设置将在这里实现');
  }

  Widget _buildScreenAwakeSection(BuildContext context, ReaderSettings settings) {
    // This would contain the logic from DisplaySettingsCard for screen awake
    return const Text('屏幕常亮设置将在这里实现');
  }
}