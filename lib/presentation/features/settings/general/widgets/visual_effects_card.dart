// lib/presentation/features/settings/general/widgets/visual_effects_card.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';

class VisualEffectsCard extends StatelessWidget {
  const VisualEffectsCard({super.key});

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
                    Icon(Icons.palette, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '视觉效果',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // 阅读背景色
                _buildBackgroundColorSection(context, settings),
                const Divider(height: 32),
                
                // 图像亮度调节
                _buildImageBrightnessSection(context, settings),
                const Divider(height: 32),
                
                // 图像对比度
                _buildContrastSection(context, settings),
                const Divider(height: 32),
                
                // 图像饱和度
                _buildSaturationSection(context, settings),
                const Divider(height: 32),
                
                // 夜间模式优化
                _buildNightModeSection(context, settings),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildBackgroundColorSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '阅读背景色',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          children: BackgroundColor.values.map((color) {
            return ChoiceChip(
              label: Text(_getBackgroundColorLabel(color)),
              selected: settings.backgroundColor == color,
              onSelected: (selected) {
                if (selected) {
                  context.read<SettingsBloc>().add(UpdateBackgroundColor(color));
                }
              },
            );
          }).toList(),
        ),
        if (settings.backgroundColor == BackgroundColor.Custom) ...[
          const SizedBox(height: 16),
          Row(
            children: [
              const Text('自定义颜色: '),
              GestureDetector(
                onTap: () => _showColorPicker(context, settings),
                child: Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: settings.customBackgroundColor ?? Colors.grey,
                    border: Border.all(color: Colors.grey),
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
              ),
            ],
          ),
        ],
        Text(
          '选择阅读时的背景颜色，影响阅读舒适度',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildImageBrightnessSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '图像亮度',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Text(
          '亮度: ${(settings.imageBrightness * 100).round()}%',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        Slider(
          value: settings.imageBrightness,
          min: 0.3,
          max: 2.0,
          divisions: 17,
          label: '${(settings.imageBrightness * 100).round()}%',
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateImageBrightness(value));
          },
        ),
        Text(
          '调整漫画图像的亮度，适应不同的阅读环境',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildContrastSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '图像对比度',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Text(
          '对比度: ${(settings.contrast * 100).round()}%',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        Slider(
          value: settings.contrast,
          min: 0.5,
          max: 2.0,
          divisions: 15,
          label: '${(settings.contrast * 100).round()}%',
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateContrast(value));
          },
        ),
        Text(
          '调整图像对比度，增强图像清晰度',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildSaturationSection(BuildContext context, ReaderSettings settings) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          '图像饱和度',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        Text(
          '饱和度: ${(settings.saturation * 100).round()}%',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        Slider(
          value: settings.saturation,
          min: 0.0,
          max: 2.0,
          divisions: 20,
          label: '${(settings.saturation * 100).round()}%',
          onChanged: (value) {
            context.read<SettingsBloc>().add(UpdateSaturation(value));
          },
        ),
        Text(
          '调整图像色彩饱和度，控制色彩鲜艳程度',
          style: Theme.of(context).textTheme.bodySmall?.copyWith(
            color: Theme.of(context).textTheme.bodySmall?.color?.withOpacity(0.7),
          ),
        ),
      ],
    );
  }

  Widget _buildNightModeSection(BuildContext context, ReaderSettings settings) {
    return SwitchListTile(
      title: const Text('夜间模式优化'),
      subtitle: const Text('在夜间模式下自动优化图像显示效果'),
      value: settings.nightModeOptimization,
      onChanged: (value) {
        context.read<SettingsBloc>().add(UpdateNightModeOptimization(value));
      },
      contentPadding: EdgeInsets.zero,
    );
  }

  String _getBackgroundColorLabel(BackgroundColor color) {
    switch (color) {
      case BackgroundColor.White:
        return '白色';
      case BackgroundColor.Black:
        return '黑色';
      case BackgroundColor.Sepia:
        return '护眼色';
      case BackgroundColor.Gray:
        return '灰色';
      case BackgroundColor.Custom:
        return '自定义';
    }
  }

  void _showColorPicker(BuildContext context, ReaderSettings settings) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('选择背景颜色'),
        content: SizedBox(
          width: 300,
          child: GridView.builder(
            shrinkWrap: true,
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 6,
              crossAxisSpacing: 8,
              mainAxisSpacing: 8,
            ),
            itemCount: _predefinedColors.length,
            itemBuilder: (context, index) {
              final color = _predefinedColors[index];
              return GestureDetector(
                onTap: () {
                  context.read<SettingsBloc>().add(UpdateCustomBackgroundColor(color));
                  Navigator.of(context).pop();
                },
                child: Container(
                  decoration: BoxDecoration(
                    color: color,
                    border: Border.all(
                      color: settings.customBackgroundColor == color 
                        ? Theme.of(context).primaryColor 
                        : Colors.grey,
                      width: settings.customBackgroundColor == color ? 3 : 1,
                    ),
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
              );
            },
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
        ],
      ),
    );
  }

  static const List<Color> _predefinedColors = [
    Colors.white,
    Colors.black,
    Colors.grey,
    Color(0xFFF5F5DC), // 米色
    Color(0xFFF0E68C), // 卡其色
    Color(0xFFDEB887), // 浅棕色
    Color(0xFF2E2E2E), // 深灰色
    Color(0xFF1A1A1A), // 炭黑色
    Color(0xFF0D1117), // GitHub 深色背景
    Color(0xFF161B22), // GitHub 深色卡片
    Color(0xFFF6F8FA), // GitHub 浅色背景
    Color(0xFFFFFFFF), // 纯白
  ];
}