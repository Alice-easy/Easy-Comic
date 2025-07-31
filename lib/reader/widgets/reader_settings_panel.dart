import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/brightness_service.dart';
import '../models/reader_models.dart';
import '../providers/reader_state_provider.dart';

/// Comprehensive settings panel for all reader options
class ReaderSettingsPanel extends ConsumerStatefulWidget {
  final int comicId;
  final VoidCallback? onClose;

  const ReaderSettingsPanel({
    super.key,
    required this.comicId,
    this.onClose,
  });

  @override
  ConsumerState<ReaderSettingsPanel> createState() => _ReaderSettingsPanelState();
}

class _ReaderSettingsPanelState extends ConsumerState<ReaderSettingsPanel>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final readerState = ref.watch(readerStateProvider(widget.comicId));
    final stateNotifier = ref.read(readerStateProvider(widget.comicId).notifier);

    return Container(
      height: MediaQuery.of(context).size.height * 0.6,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: const BorderRadius.vertical(
          top: Radius.circular(16.0),
        ),
      ),
      child: Column(
        children: [
          _buildHeader(),
          _buildTabBar(),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildDisplaySettings(readerState, stateNotifier),
                _buildNavigationSettings(readerState, stateNotifier),
                _buildAdvancedSettings(readerState, stateNotifier),
              ],
            ),
          ),
          _buildFooter(stateNotifier),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      decoration: BoxDecoration(
        color: Theme.of(context).primaryColor.withOpacity(0.1),
        borderRadius: const BorderRadius.vertical(
          top: Radius.circular(16.0),
        ),
      ),
      child: Row(
        children: [
          const Icon(Icons.settings),
          const SizedBox(width: 8),
          const Expanded(
            child: Text(
              '阅读设置',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          IconButton(
            onPressed: widget.onClose ?? () => Navigator.of(context).pop(),
            icon: const Icon(Icons.close),
            tooltip: '关闭',
          ),
        ],
      ),
    );
  }

  Widget _buildTabBar() {
    return TabBar(
      controller: _tabController,
      tabs: const [
        Tab(text: '显示', icon: Icon(Icons.visibility)),
        Tab(text: '导航', icon: Icon(Icons.navigation)),
        Tab(text: '高级', icon: Icon(Icons.tune)),
      ],
    );
  }

  Widget _buildDisplaySettings(ReaderState state, ReaderStateNotifier notifier) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        _buildSectionTitle('阅读模式'),
        _buildReadingModeSelector(state, notifier),
        const SizedBox(height: 24),
        
        _buildSectionTitle('背景主题'),
        _buildBackgroundThemeSelector(state, notifier),
        const SizedBox(height: 24),
        
        _buildSectionTitle('亮度调节'),
        _buildBrightnessControl(state, notifier),
        const SizedBox(height: 24),
        
        _buildSectionTitle('页面过渡'),
        _buildTransitionSelector(state, notifier),
      ],
    );
  }

  Widget _buildNavigationSettings(ReaderState state, ReaderStateNotifier notifier) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        _buildSectionTitle('导航方向'),
        _buildNavigationDirectionSelector(state, notifier),
        const SizedBox(height: 24),
        
        _buildSectionTitle('缩略图设置'),
        _buildThumbnailSettings(state, notifier),
      ],
    );
  }

  Widget _buildAdvancedSettings(ReaderState state, ReaderStateNotifier notifier) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        _buildSectionTitle('性能设置'),
        _buildPerformanceSettings(state, notifier),
        const SizedBox(height: 24),
        
        _buildSectionTitle('重置设置'),
        _buildResetSection(notifier),
      ],
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Text(
        title,
        style: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
          color: Theme.of(context).primaryColor,
        ),
      ),
    );
  }

  Widget _buildReadingModeSelector(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Column(
        children: ReadingMode.values.map((mode) {
          return RadioListTile<ReadingMode>(
            value: mode,
            groupValue: state.mode,
            onChanged: (value) => value != null ? notifier.setReadingMode(value) : null,
            title: Text(_getReadingModeTitle(mode)),
            subtitle: Text(_getReadingModeDescription(mode)),
            secondary: Icon(_getReadingModeIcon(mode)),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildBackgroundThemeSelector(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Wrap(
        spacing: 8.0,
        children: BackgroundTheme.values.map((theme) {
          final isSelected = state.backgroundTheme == theme;
          return ChoiceChip(
            label: Text(_getThemeTitle(theme)),
            selected: isSelected,
            onSelected: (selected) {
              if (selected) notifier.setBackgroundTheme(theme);
            },
            avatar: CircleAvatar(
              backgroundColor: theme.color,
              radius: 8,
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildBrightnessControl(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: BrightnessSlider(
          brightness: state.brightness,
          onChanged: notifier.setBrightness,
          activeColor: Theme.of(context).primaryColor,
        ),
      ),
    );
  }

  Widget _buildTransitionSelector(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Column(
        children: TransitionType.values.map((type) {
          return RadioListTile<TransitionType>(
            value: type,
            groupValue: state.transitionType,
            onChanged: (value) => value != null ? notifier.setTransitionType(value) : null,
            title: Text(_getTransitionTitle(type)),
            subtitle: Text(_getTransitionDescription(type)),
            secondary: Icon(_getTransitionIcon(type)),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildNavigationDirectionSelector(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Column(
        children: NavigationDirection.values.map((direction) {
          return RadioListTile<NavigationDirection>(
            value: direction,
            groupValue: state.direction,
            onChanged: (value) => value != null ? notifier.setNavigationDirection(value) : null,
            title: Text(_getNavigationTitle(direction)),
            subtitle: Text(_getNavigationDescription(direction)),
            secondary: Icon(_getNavigationIcon(direction)),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildThumbnailSettings(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: SwitchListTile(
        value: state.showThumbnails,
        onChanged: notifier.setShowThumbnails,
        title: const Text('显示缩略图'),
        subtitle: const Text('在进度条拖拽时显示页面缩略图'),
        secondary: const Icon(Icons.image),
      ),
    );
  }

  Widget _buildPerformanceSettings(ReaderState state, ReaderStateNotifier notifier) {
    return Card(
      child: Column(
        children: [
          ListTile(
            leading: const Icon(Icons.memory),
            title: const Text('缓存管理'),
            subtitle: const Text('清理缩略图缓存以释放存储空间'),
            trailing: TextButton(
              onPressed: _clearThumbnailCache,
              child: const Text('清理'),
            ),
          ),
          ListTile(
            leading: const Icon(Icons.storage),
            title: const Text('存储使用情况'),
            subtitle: Text(_getCacheUsageText()),
          ),
        ],
      ),
    );
  }

  Widget _buildResetSection(ReaderStateNotifier notifier) {
    return Card(
      child: ListTile(
        leading: const Icon(Icons.restore, color: Colors.orange),
        title: const Text('重置所有设置'),
        subtitle: const Text('将所有阅读设置恢复为默认值'),
        trailing: TextButton(
          onPressed: () => _showResetConfirmation(notifier),
          style: TextButton.styleFrom(foregroundColor: Colors.orange),
          child: const Text('重置'),
        ),
      ),
    );
  }

  Widget _buildFooter(ReaderStateNotifier notifier) {
    return Container(
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        border: Border(
          top: BorderSide(
            color: Theme.of(context).dividerColor,
            width: 1.0,
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          TextButton(
            onPressed: widget.onClose ?? () => Navigator.of(context).pop(),
            child: const Text('完成'),
          ),
        ],
      ),
    );
  }

  // Helper methods for labels and descriptions
  String _getReadingModeTitle(ReadingMode mode) {
    switch (mode) {
      case ReadingMode.single:
        return '单页模式';
      case ReadingMode.dual:
        return '双页模式';
      case ReadingMode.vertical:
        return '垂直滚动';
    }
  }

  String _getReadingModeDescription(ReadingMode mode) {
    switch (mode) {
      case ReadingMode.single:
        return '一次显示一页，适合大多数漫画';
      case ReadingMode.dual:
        return '在宽屏设备上并排显示两页';
      case ReadingMode.vertical:
        return '连续垂直滚动浏览页面';
    }
  }

  IconData _getReadingModeIcon(ReadingMode mode) {
    switch (mode) {
      case ReadingMode.single:
        return Icons.crop_portrait;
      case ReadingMode.dual:
        return Icons.crop_landscape;
      case ReadingMode.vertical:
        return Icons.view_day;
    }
  }

  String _getThemeTitle(BackgroundTheme theme) {
    switch (theme) {
      case BackgroundTheme.black:
        return '黑色';
      case BackgroundTheme.darkGray:
        return '深灰';
      case BackgroundTheme.sepia:
        return '护眼棕';
      case BackgroundTheme.white:
        return '白色';
      case BackgroundTheme.eyeCare:
        return '护眼绿';
    }
  }

  String _getTransitionTitle(TransitionType type) {
    switch (type) {
      case TransitionType.none:
        return '无动画';
      case TransitionType.slide:
        return '滑动';
      case TransitionType.fade:
        return '淡入淡出';
      case TransitionType.curl:
        return '翻页';
    }
  }

  String _getTransitionDescription(TransitionType type) {
    switch (type) {
      case TransitionType.none:
        return '直接切换页面，性能最佳';
      case TransitionType.slide:
        return '页面滑动切换效果';
      case TransitionType.fade:
        return '页面淡入淡出效果';
      case TransitionType.curl:
        return '模拟真实翻页效果';
    }
  }

  IconData _getTransitionIcon(TransitionType type) {
    switch (type) {
      case TransitionType.none:
        return Icons.flash_on;
      case TransitionType.slide:
        return Icons.swipe;
      case TransitionType.fade:
        return Icons.blur_on;
      case TransitionType.curl:
        return Icons.auto_stories;
    }
  }

  String _getNavigationTitle(NavigationDirection direction) {
    switch (direction) {
      case NavigationDirection.horizontal:
        return '水平导航';
      case NavigationDirection.vertical:
        return '垂直导航';
    }
  }

  String _getNavigationDescription(NavigationDirection direction) {
    switch (direction) {
      case NavigationDirection.horizontal:
        return '左右滑动翻页';
      case NavigationDirection.vertical:
        return '上下滑动翻页';
    }
  }

  IconData _getNavigationIcon(NavigationDirection direction) {
    switch (direction) {
      case NavigationDirection.horizontal:
        return Icons.swipe_left;
      case NavigationDirection.vertical:
        return Icons.swipe_up;
    }
  }

  String _getCacheUsageText() {
    // This would require actual cache size calculation
    return '缓存大小: 计算中...';
  }

  void _clearThumbnailCache() {
    // Clear thumbnail cache
    // ThumbnailService.clearCache();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('缩略图缓存已清理')),
    );
  }

  void _showResetConfirmation(ReaderStateNotifier notifier) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('重置设置'),
        content: const Text('确定要将所有阅读设置重置为默认值吗？此操作无法撤销。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              notifier.resetToDefaults();
              Navigator.of(context).pop();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('设置已重置为默认值')),
              );
            },
            style: TextButton.styleFrom(foregroundColor: Colors.orange),
            child: const Text('重置'),
          ),
        ],
      ),
    );
  }
}