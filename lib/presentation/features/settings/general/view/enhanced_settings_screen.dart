// lib/presentation/features/settings/general/view/enhanced_settings_screen.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/injection_container.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';
import '../widgets/reading_preferences_card.dart';
import '../widgets/display_settings_card.dart';
import '../widgets/visual_effects_card.dart';
import '../widgets/file_management_card.dart';
import '../widgets/sync_backup_card.dart';
import '../widgets/about_card.dart';
import '../widgets/debug_card.dart';

class EnhancedSettingsScreen extends StatefulWidget {
  const EnhancedSettingsScreen({super.key});

  @override
  State<EnhancedSettingsScreen> createState() => _EnhancedSettingsScreenState();
}

class _EnhancedSettingsScreenState extends State<EnhancedSettingsScreen>
    with TickerProviderStateMixin {
  late TabController _tabController;
  final TextEditingController _searchController = TextEditingController();
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 6, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => sl<SettingsBloc>()..add(LoadSettings()),
      child: Scaffold(
        appBar: _buildAppBar(),
        body: BlocListener<SettingsBloc, SettingsState>(
          listener: (context, state) {
            if (state is SettingsError) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(state.message),
                  backgroundColor: Colors.red,
                ),
              );
            } else if (state is SettingsSaved) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('设置已保存'),
                  backgroundColor: Colors.green,
                  duration: Duration(seconds: 1),
                ),
              );
            } else if (state is CacheCleared) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text(state.message),
                  backgroundColor: Colors.green,
                ),
              );
            } else if (state is SettingsExported) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('设置已导出到: ${state.exportPath}'),
                  backgroundColor: Colors.green,
                ),
              );
            }
          },
          child: Column(
            children: [
              if (_isSearching) _buildSearchBar(),
              _buildTabBar(),
              Expanded(
                child: TabBarView(
                  controller: _tabController,
                  children: const [
                    _ReadingTab(),
                    _DisplayTab(),
                    _FileManagementTab(),
                    _SyncBackupTab(),
                    _AboutTab(),
                    _DebugTab(),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() {
    return AppBar(
      title: const Text('设置中心'),
      elevation: 0,
      actions: [
        IconButton(
          icon: Icon(_isSearching ? Icons.close : Icons.search),
          onPressed: () {
            setState(() {
              _isSearching = !_isSearching;
              if (!_isSearching) {
                _searchController.clear();
              }
            });
          },
        ),
        PopupMenuButton<String>(
          onSelected: (value) {
            final bloc = context.read<SettingsBloc>();
            switch (value) {
              case 'export':
                bloc.add(ExportSettings());
                break;
              case 'import':
                _showImportDialog();
                break;
              case 'reset':
                _showResetDialog();
                break;
            }
          },
          itemBuilder: (context) => [
            const PopupMenuItem(
              value: 'export',
              child: ListTile(
                leading: Icon(Icons.file_download),
                title: Text('导出设置'),
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const PopupMenuItem(
              value: 'import',
              child: ListTile(
                leading: Icon(Icons.file_upload),
                title: Text('导入设置'),
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const PopupMenuItem(
              value: 'reset',
              child: ListTile(
                leading: Icon(Icons.restore),
                title: Text('重置为默认'),
                contentPadding: EdgeInsets.zero,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildSearchBar() {
    return Container(
      padding: const EdgeInsets.all(16),
      child: TextField(
        controller: _searchController,
        decoration: const InputDecoration(
          hintText: '搜索设置项...',
          prefixIcon: Icon(Icons.search),
          border: OutlineInputBorder(),
        ),
        onChanged: (query) {
          // 实现搜索逻辑
        },
      ),
    );
  }

  Widget _buildTabBar() {
    return TabBar(
      controller: _tabController,
      isScrollable: true,
      tabs: const [
        Tab(icon: Icon(Icons.menu_book), text: '阅读'),
        Tab(icon: Icon(Icons.display_settings), text: '显示'),
        Tab(icon: Icon(Icons.folder), text: '文件'),
        Tab(icon: Icon(Icons.sync), text: '同步'),
        Tab(icon: Icon(Icons.info), text: '关于'),
        Tab(icon: Icon(Icons.bug_report), text: '调试'),
      ],
    );
  }

  void _showImportDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('导入设置'),
        content: const Text('请选择要导入的设置文件'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () {
              // 实现文件选择和导入逻辑
              Navigator.of(context).pop();
            },
            child: const Text('选择文件'),
          ),
        ],
      ),
    );
  }

  void _showResetDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('重置设置'),
        content: const Text('确定要将所有设置重置为默认值吗？此操作不可撤销。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () {
              context.read<SettingsBloc>().add(ResetToDefaults());
              Navigator.of(context).pop();
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text('重置', style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }
}

// 阅读设置标签页
class _ReadingTab extends StatelessWidget {
  const _ReadingTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        children: [
          ReadingPreferencesCard(),
          SizedBox(height: 16),
          DisplaySettingsCard(),
        ],
      ),
    );
  }
}

// 显示设置标签页
class _DisplayTab extends StatelessWidget {
  const _DisplayTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: Column(
        children: [
          VisualEffectsCard(),
          SizedBox(height: 16),
          // 这里可以添加界面布局设置卡片
          _InterfaceLayoutCard(),
        ],
      ),
    );
  }
}

// 文件管理标签页
class _FileManagementTab extends StatelessWidget {
  const _FileManagementTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: FileManagementCard(),
    );
  }
}

// 同步备份标签页
class _SyncBackupTab extends StatelessWidget {
  const _SyncBackupTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: SyncBackupCard(),
    );
  }
}

// 关于标签页
class _AboutTab extends StatelessWidget {
  const _AboutTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: AboutCard(),
    );
  }
}

// 调试标签页
class _DebugTab extends StatelessWidget {
  const _DebugTab();

  @override
  Widget build(BuildContext context) {
    return const SingleChildScrollView(
      padding: EdgeInsets.all(16),
      child: DebugCard(),
    );
  }
}

// 简化的卡片组件（实际实现中这些应该是完整的组件）
class _InterfaceLayoutCard extends StatelessWidget {
  const _InterfaceLayoutCard();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.view_module, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('界面布局', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('书架网格、卡片样式等设置即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _StorageConfigCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.storage, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('存储配置', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('存储位置、缓存大小等设置即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _FileHandlingCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.insert_drive_file, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('文件处理', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('支持格式、图像质量等设置即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _BackupRestoreCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.backup, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('备份恢复', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('自动备份、备份历史等功能即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _AppInfoCard extends StatelessWidget {
  const _AppInfoCard();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.info_outline, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('应用信息', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const ListTile(
              title: Text('版本'),
              subtitle: Text('1.0.0'),
              contentPadding: EdgeInsets.zero,
            ),
            const ListTile(
              title: Text('开发者'),
              subtitle: Text('Easy Comic Team'),
              contentPadding: EdgeInsets.zero,
            ),
          ],
        ),
      ),
    );
  }
}

class _HelpSupportCard extends StatelessWidget {
  const _HelpSupportCard();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.help_outline, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('帮助支持', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('使用帮助、FAQ、反馈等功能即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _DiagnosticsCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.bug_report, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('诊断信息', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            const Text('日志级别、性能监控等设置即将推出...'),
          ],
        ),
      ),
    );
  }
}

class _CacheManagementCard extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.cleaning_services, color: Theme.of(context).primaryColor),
                const SizedBox(width: 8),
                Text('缓存管理', style: Theme.of(context).textTheme.headlineSmall),
              ],
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () {
                context.read<SettingsBloc>().add(ClearCache());
              },
              icon: const Icon(Icons.delete_sweep),
              label: const Text('清理缓存'),
            ),
            const SizedBox(height: 16),
            ElevatedButton.icon(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('重置应用数据'),
                    content: const Text('确定要重置所有应用数据吗？此操作不可撤销。'),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.of(context).pop(),
                        child: const Text('取消'),
                      ),
                      ElevatedButton(
                        onPressed: () {
                          context.read<SettingsBloc>().add(ResetAppData());
                          Navigator.of(context).pop();
                        },
                        style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                        child: const Text('重置', style: TextStyle(color: Colors.white)),
                      ),
                    ],
                  ),
                );
              },
              icon: const Icon(Icons.restore),
              label: const Text('重置应用数据'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            ),
          ],
        ),
      ),
    );
  }
}