import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../models/reader_models.dart' as models;
import '../../main.dart';

/// Mixin that provides reader settings management UI and logic
/// 
/// This mixin provides:
/// - Settings UI widgets (buttons, sliders, overlays)
/// - Theme and appearance management
/// - Brightness control integration
/// - Settings persistence through providers
mixin SettingsHandler<T extends ConsumerStatefulWidget> 
    on ConsumerState<T> {

  /// Get foreground color based on background theme
  Color getForegroundColor(models.BackgroundTheme theme) {
    switch (theme) {
      case models.BackgroundTheme.black:
      case models.BackgroundTheme.grey:
        return Colors.white;
      case models.BackgroundTheme.white:
      case models.BackgroundTheme.sepia:
        return Colors.black87;
    }
  }

  /// Get icon for reading mode
  IconData getModeIcon(models.ReadingMode mode) {
    switch (mode) {
      case models.ReadingMode.single:
        return Icons.view_agenda;
      case models.ReadingMode.dual:
        return Icons.view_column;
      case models.ReadingMode.continuous:
      case models.ReadingMode.vertical:
        return Icons.view_day;
    }
  }

  /// Build reading mode selection button
  Widget buildModeButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.ReadingMode>(
      icon: Icon(getModeIcon(settings.readingMode)),
      tooltip: '阅读模式',
      onSelected: (mode) => ref.read(readerSettingsProvider.notifier).updateReadingMode(mode),
      itemBuilder: (context) => [
        PopupMenuItem(
          value: models.ReadingMode.single,
          child: Row(
            children: [
              Icon(getModeIcon(models.ReadingMode.single)),
              const SizedBox(width: 8),
              const Text('单页'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.ReadingMode.dual,
          child: Row(
            children: [
              Icon(getModeIcon(models.ReadingMode.dual)),
              const SizedBox(width: 8),
              const Text('双页'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.ReadingMode.continuous,
          child: Row(
            children: [
              Icon(getModeIcon(models.ReadingMode.continuous)),
              const SizedBox(width: 8),
              const Text('连续'),
            ],
          ),
        ),
      ],
    );
  }

  /// Build navigation direction selection button
  Widget buildDirectionButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.NavigationDirection>(
      icon: Icon(getDirectionIcon(settings.navigationDirection)),
      tooltip: '阅读方向',
      onSelected: (direction) => ref.read(readerSettingsProvider.notifier).updateNavigationDirection(direction),
      itemBuilder: (context) => [
        PopupMenuItem(
          value: models.NavigationDirection.horizontal,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.horizontal)),
              const SizedBox(width: 8),
              const Text('从左到右'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.NavigationDirection.rtl,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.rtl)),
              const SizedBox(width: 8),
              const Text('从右到左'),
            ],
          ),
        ),
        PopupMenuItem(
          value: models.NavigationDirection.vertical,
          child: Row(
            children: [
              Icon(getDirectionIcon(models.NavigationDirection.vertical)),
              const SizedBox(width: 8),
              const Text('从上到下'),
            ],
          ),
        ),
      ],
    );
  }

  /// Build brightness control button with slider
  Widget buildBrightnessButton(models.ReaderSettings settings, models.ReaderUIState uiState) {
    return PopupMenuButton(
      icon: const Icon(Icons.brightness_6),
      tooltip: '亮度调节',
      itemBuilder: (context) => [
        PopupMenuItem(
          enabled: false,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('亮度: ${(settings.brightness * 100).round()}%'),
              Slider(
                value: settings.brightness,
                onChanged: (value) => ref.read(readerSettingsProvider.notifier).updateBrightness(value),
                min: 0.1,
                max: 1.0,
                divisions: 18,
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  TextButton(
                    onPressed: () => ref.read(brightnessProvider.notifier).autoAdjustBrightness(),
                    child: const Text('自动'),
                  ),
                  TextButton(
                    onPressed: () => ref.read(readerUIStateProvider.notifier).toggleBrightnessOverlay(),
                    child: Text(uiState.brightnessOverlayEnabled ? '关闭覆盖' : '启用覆盖'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }

  /// Build background theme selection button
  Widget buildBackgroundButton(models.ReaderSettings settings) {
    return PopupMenuButton<models.BackgroundTheme>(
      icon: const Icon(Icons.palette),
      tooltip: '背景主题',
      onSelected: (theme) => ref.read(readerSettingsProvider.notifier).updateBackgroundTheme(theme),
      itemBuilder: (context) => models.BackgroundTheme.values.map((theme) => PopupMenuItem(
        value: theme,
        child: Row(
          children: [
            Container(
              width: 20,
              height: 20,
              decoration: BoxDecoration(
                color: theme.color,
                shape: BoxShape.circle,
                border: Border.all(color: Colors.grey),
              ),
            ),
            const SizedBox(width: 8),
            Text(theme.displayName),
            if (settings.backgroundTheme == theme)
              const Padding(
                padding: EdgeInsets.only(left: 8),
                child: Icon(Icons.check, size: 16),
              ),
          ],
        ),
      )).toList(),
    );
  }

  /// Build brightness overlay widget
  Widget buildBrightnessOverlay({required Widget child, required double brightness}) {
    if (brightness >= 1.0) {
      return child;
    }

    return Stack(
      children: [
        child,
        IgnorePointer(
          child: Container(
            color: Colors.black.withOpacity(1.0 - brightness),
          ),
        ),
      ],
    );
  }

  /// Build settings panel with all controls
  Widget buildSettingsPanel(models.ReaderSettings settings, models.ReaderUIState uiState) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.2),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              buildModeButton(settings),
              buildDirectionButton(settings),
              buildBrightnessButton(settings, uiState),
              buildBackgroundButton(settings),
            ],
          ),
          const SizedBox(height: 16),
          // Additional settings can be added here
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              TextButton.icon(
                onPressed: () => ref.read(readerSettingsProvider.notifier).toggleThumbnails(),
                icon: Icon(settings.showThumbnails ? Icons.visibility : Icons.visibility_off),
                label: Text(settings.showThumbnails ? '隐藏缩略图' : '显示缩略图'),
              ),
              TextButton.icon(
                onPressed: () => ref.read(readerUIStateProvider.notifier).toggleFullscreen(),
                icon: Icon(uiState.fullscreen ? Icons.fullscreen_exit : Icons.fullscreen),
                label: Text(uiState.fullscreen ? '退出全屏' : '全屏'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  /// Get background decoration for the current theme
  BoxDecoration getBackgroundDecoration(models.BackgroundTheme theme) {
    return BoxDecoration(
      color: theme.color,
    );
  }

  /// Check if theme is dark (for automatic adjustments)
  bool isThemeDark(models.BackgroundTheme theme) {
    return theme == models.BackgroundTheme.black || 
           theme == models.BackgroundTheme.grey;
  }

  /// Get appropriate text style for current theme
  TextStyle getThemeTextStyle(models.BackgroundTheme theme, {
    double? fontSize,
    FontWeight? fontWeight,
  }) {
    return TextStyle(
      color: getForegroundColor(theme),
      fontSize: fontSize,
      fontWeight: fontWeight,
    );
  }

  /// Abstract method to get direction icon (should be provided by NavigationHandler)
  IconData getDirectionIcon(models.NavigationDirection direction);
}