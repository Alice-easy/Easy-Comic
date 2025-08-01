// lib/presentation/features/settings/general/widgets/reading_preferences_card.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';

class ReadingPreferencesCard extends StatelessWidget {
  const ReadingPreferencesCard({super.key});

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
                    Icon(Icons.menu_book, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '阅读偏好',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // 阅读方向
                _buildReadingDirectionSection(context, settings),
                const Divider(height: 32),
                
                // 阅读模式
                _buildReadingModeSection(context, settings),
                const Divider(height: 32),
                
                // 页面过渡动画
                _buildPageTransitionSection(context, settings),
                const Divider(height: 32),
                
                // 自动翻页设置
                _buildAutoPageSection(context, settings),
                const Divider(height: 32),
                
                // 点击区域敏感度
                _buildTapAreaSensitivitySection(context, settings),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildReadingDirectionSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '阅读方向',
          style: Theme.of(context).textTheme.titleMedium,
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
        Text(
          '选择漫画的阅读方向，影响翻页手势和界面布局',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildReadingModeSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '阅读模式',
          style: Theme.of(context).textTheme.titleMedium,
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
        Text(
          '选择页面显示方式，单页、双页或长条漫画模式',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildPageTransitionSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '页面过渡动画',
          style: Theme.of(context).textTheme.titleMedium,
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
        Text(
          '设置翻页时的过渡动画效果',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildAutoPageSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              '自动翻页',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            Switch(
              value: settings.autoPageEnabled,
              onChanged: (value) {
                context.read<SettingsBloc>().add(UpdateAutoPageEnabled(value));
              },
            ),
          ],
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
        Text(
          settings.autoPageEnabled 
            ? '启用自动翻页功能，设置翻页时间间隔'
            : '启用自动翻页功能，可以设置自动翻页间隔',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildTapAreaSensitivitySection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '点击区域敏感度',
          style: Theme.of(context).textTheme.titleMedium,
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
        Text(
          '调整屏幕边缘点击区域的敏感度，影响翻页手势识别',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
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