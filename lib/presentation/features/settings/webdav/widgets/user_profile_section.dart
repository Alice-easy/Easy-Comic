import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/core/services/avatar_manager.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:intl/intl.dart' as intl;
import '../bloc/webdav_bloc.dart';
import '../bloc/webdav_event.dart';
import '../bloc/webdav_state.dart';
import 'webdav_login_dialog.dart';

class UserProfileSection extends StatelessWidget {
  const UserProfileSection({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<WebDAVBloc, WebDAVState>(
      listener: (context, state) {
        if (state is WebDAVFailure) {
          ScaffoldMessenger.of(context)
            ..hideCurrentSnackBar()
            ..showSnackBar(
              SnackBar(content: Text(state.errorMessage ?? '发生未知错误')),
            );
        }
      },
      builder: (context, state) {
        if (state.status == WebDavStatus.loggedIn || state.status == WebDavStatus.syncing || state.status == WebDavStatus.syncFailure) {
          return _buildLoggedInUI(context, state);
        } else {
          return _buildLoggedOutUI(context, state);
        }
      },
    );
  }

  Widget _buildLoggedInUI(BuildContext context, WebDAVState state) {
    final avatarManager = sl<AvatarManager>();

    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        children: [
          GestureDetector(
            onTap: () async {
              final newAvatar = await avatarManager.pickAndCropImage();
              if (newAvatar != null && context.mounted) {
                context.read<WebDAVBloc>().add(UpdateAvatarEvent(newAvatar.path));
              }
            },
            child: CircleAvatar(
              radius: 40,
              backgroundImage: state.avatarPath != null ? FileImage(File(state.avatarPath!)) : null,
              child: state.avatarPath == null ? const Icon(Icons.person, size: 40) : null,
            ),
          ),
          const SizedBox(height: 8),
          Text(state.username ?? '未登录', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          _buildSyncStatus(context, state),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: state.status == WebDavStatus.syncing ? null : () {
                  context.read<WebDAVBloc>().add(SyncDataEvent());
                },
                child: state.status == WebDavStatus.syncing
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2))
                    : const Text('手动同步'),
              ),
              const SizedBox(width: 16),
              ElevatedButton(
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                onPressed: () {
                  context.read<WebDAVBloc>().add(LogoutEvent());
                },
                child: const Text('登出'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSyncStatus(BuildContext context, WebDAVState state) {
    if (state.status == WebDavStatus.syncing) {
      return const Text('同步中...');
    }
    if (state.lastSyncTime != null) {
      final formattedTime = intl.DateFormat('yyyy-MM-dd HH:mm').format(state.lastSyncTime!);
      return Text('上次同步: $formattedTime', style: Theme.of(context).textTheme.bodySmall);
    }
    return const SizedBox.shrink();
  }

  Widget _buildLoggedOutUI(BuildContext context, WebDAVState state) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            if (state is WebDAVAuthInProgress)
              const CircularProgressIndicator()
            else
              ElevatedButton(
                onPressed: () {
                  showDialog(
                    context: context,
                    builder: (_) => BlocProvider.value(
                      value: BlocProvider.of<WebDAVBloc>(context),
                      child: const WebDavLoginDialog(),
                    ),
                  );
                },
                child: const Text('登录 WebDAV 账户'),
              ),
          ],
        ),
      ),
    );
  }
}