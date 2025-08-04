import 'package:easy_comic/presentation/features/settings/appearance_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/webdav/webdav_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/webdav/widgets/user_profile_section.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/core/di/injection_container.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView(
        children: [
          const UserProfileSection(),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.palette_outlined),
            title: const Text('Appearance'),
            onTap: () {
              Navigator.of(context).push(MaterialPageRoute(
                builder: (context) => const AppearanceSettingsScreen(),
              ));
            },
          ),
          ListTile(
            leading: const Icon(Icons.cloud_outlined),
            title: const Text('WebDAV'),
            onTap: () {
              Navigator.of(context).push(MaterialPageRoute(
                builder: (context) => BlocProvider.value(
                  value: sl<WebdavBloc>(),
                  child: const WebdavSettingsScreen(),
                ),
              ));
            },
          ),
        ],
      ),
    );
  }
}