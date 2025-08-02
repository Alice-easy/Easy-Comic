import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/browse/view/browse_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/sources/view/sources_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/reading/view/reading_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/appearance/view/appearance_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/collection/view/collection_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/app/view/app_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/network/view/network_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/about/view/about_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/widgets/user_profile_section.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (context) => sl<SettingsBloc>()..add(LoadSettings()),
        ),
        BlocProvider(
          create: (context) => sl<WebDAVBloc>(),
        ),
      ],
      child: Scaffold(
        appBar: AppBar(
          title: const Text('设置'),
        ),
        body: ListView(
          children: [
            const UserProfileSection(),
            const Divider(),
            _buildCategoryTile(
              context,
              icon: Icons.explore_outlined,
              title: '浏览',
              subtitle: '阅读方向, 页面显示, 缩放',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const BrowseSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.source_outlined,
              title: '漫画源',
              subtitle: '文件导入, 支持格式, 存储位置',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const SourcesSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.menu_book_outlined,
              title: '阅读',
              subtitle: '自动翻页, 手势, 阅读偏好',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const ReadingSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.palette_outlined,
              title: '外观',
              subtitle: '主题, UI自定义, 显示设置',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const AppearanceSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.collections_bookmark_outlined,
              title: '本地收藏',
              subtitle: '书签, 收藏夹, 阅读历史',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const CollectionSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.settings_applications_outlined,
              title: 'APP',
              subtitle: '应用设置, 权限, 缓存',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const AppSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
              context,
              icon: Icons.network_check_outlined,
              title: '网络',
              subtitle: '网络设置, 下载偏好',
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<SettingsBloc>(context),
                      child: const NetworkSettingsScreen(),
                    ),
                  ),
                );
              },
            ),
            _buildCategoryTile(
            context,
            icon: Icons.info_outline,
            title: '关于',
            subtitle: '版本信息, 帮助, 鸣谢',
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const AboutSettingsScreen(),
                ),
              );
            },
          ),
        ],
      ),
    ),
  );
}

  Widget _buildCategoryTile(
    BuildContext context, {
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return ListTile(
      leading: Icon(icon, color: Theme.of(context).primaryColor),
      title: Text(title),
      subtitle: Text(
        subtitle,
        style: Theme.of(context).textTheme.bodySmall,
      ),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }
}