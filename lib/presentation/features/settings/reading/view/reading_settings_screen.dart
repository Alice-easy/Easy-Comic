import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class ReadingSettingsScreen extends StatelessWidget {
  const ReadingSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('阅读设置'),
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
              _buildPageTransitionSection(context, settings),
              const Divider(height: 32),
              _buildAutoPageSection(context, settings),
              const Divider(height: 32),
              _buildTapAreaSensitivitySection(context, settings),
            ],
          );
        },
      ),
    );
  }

  Widget _buildPageTransitionSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '页面过渡动画',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Text(
          '设置翻页时的过渡动画效果',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: PageTransition.values.map((transition) {
            return ChoiceChip(
              label: Text(_getPageTransitionLabel(transition)),
              selected: settings.pageTransition == transition,
              onSelected: (selected) {
                if (selected) {
                  context.read<SettingsBloc>().add(UpdatePageTransition(transition));
                }
              },
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildAutoPageSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '自动翻页',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        SwitchListTile(
          title: const Text('启用自动翻页'),
          subtitle: const Text('启用后将按设定间隔自动翻页'),
          value: settings.autoPageEnabled,
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateAutoPageEnabled(value));
          },
          contentPadding: EdgeInsets.zero,
        ),
        if (settings.autoPageEnabled) ...[
          const SizedBox(height: 16),
          Text(
            '翻页间隔: ${settings.autoPageInterval}秒',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          Slider(
            value: settings.autoPageInterval.toDouble(),
            min: 1,
            max: 30,
            divisions: 29,
            label: '${settings.autoPageInterval}秒',
            onChanged: (value) {
              context.read<SettingsBloc>().add(UpdateAutoPageInterval(value.round()));
            },
          ),
        ],
      ],
    );
  }

  Widget _buildTapAreaSensitivitySection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '手势灵敏度',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Text(
          '调整屏幕边缘点击区域的敏感度，影响翻页手势识别',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: 8),
        Text(
          '敏感度: ${(settings.tapAreaSensitivity * 100).round()}%',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        Slider(
          value: settings.tapAreaSensitivity,
          min: 0.1,
          max: 0.5,
          divisions: 40,
          label: '${(settings.tapAreaSensitivity * 100).round()}%',
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateTapAreaSensitivity(value));
          },
        ),
      ],
    );
  }

  String _getPageTransitionLabel(PageTransition transition) {
    switch (transition) {
      case PageTransition.Slide:
        return '滑动';
      case PageTransition.Fade:
        return '淡入淡出';
      case PageTransition.Scale:
        return '缩放';
      case PageTransition.None:
        return '无动画';
    }
  }
}