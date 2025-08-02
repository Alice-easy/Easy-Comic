import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';

class BrowseSettingsScreen extends StatelessWidget {
  const BrowseSettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('浏览设置'),
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
              _buildReadingDirectionSection(context, settings),
              const Divider(height: 32),
              _buildReadingModeSection(context, settings),
              const Divider(height: 32),
              _buildZoomLevelSection(context, settings),
            ],
          );
        },
      ),
    );
  }

  Widget _buildReadingDirectionSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '阅读方向',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Text(
          '选择漫画的阅读方向，影响翻页手势和界面布局',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: ReadingDirection.values.map((direction) {
            return ChoiceChip(
              label: Text(_getReadingDirectionLabel(direction)),
              selected: settings.readingDirection == direction,
              onSelected: (selected) {
                if (selected) {
                  context.read<SettingsBloc>().add(UpdateReadingDirection(direction));
                }
              },
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildReadingModeSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '页面显示模式',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Text(
          '选择页面显示方式，单页、双页或长条漫画模式',
          style: Theme.of(context).textTheme.bodySmall,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: ReadingMode.values.map((mode) {
            return ChoiceChip(
              label: Text(_getReadingModeLabel(mode)),
              selected: settings.readingMode == mode,
              onSelected: (selected) {
                if (selected) {
                  context.read<SettingsBloc>().add(UpdateReadingMode(mode));
                }
              },
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildZoomLevelSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '默认缩放级别',
          style: Theme.of(context).textTheme.titleLarge,
        ),
        const SizedBox(height: 8),
        Text(
          '设置打开漫画时的默认缩放级别',
          style: Theme.of(context).textTheme.bodySmall,
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
      ],
    );
  }

  String _getReadingDirectionLabel(ReadingDirection direction) {
    switch (direction) {
      case ReadingDirection.LTR:
        return '左到右';
      case ReadingDirection.RTL:
        return '右到左';
      case ReadingDirection.Vertical:
        return '垂直滚动';
    }
  }

  String _getReadingModeLabel(ReadingMode mode) {
    switch (mode) {
      case ReadingMode.SinglePage:
        return '单页模式';
      case ReadingMode.DoublePage:
        return '双页模式';
      case ReadingMode.Webtoon:
        return '长条漫画';
    }
  }
}