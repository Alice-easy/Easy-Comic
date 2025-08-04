import 'dart:io';

import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:image_picker/image_picker.dart';

class UserProfileSection extends StatelessWidget {
  const UserProfileSection({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<WebdavBloc, WebdavState>(
      builder: (context, state) {
        return Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            children: [
              GestureDetector(
                onTap: () async {
                  final picker = ImagePicker();
                  final pickedFile = await picker.pickImage(source: ImageSource.gallery);
                  if (pickedFile != null) {
                    context.read<WebdavBloc>().add(UploadAvatarButtonPressed(avatarFile: File(pickedFile.path)));
                  }
                },
                child: CircleAvatar(
                  radius: 40,
                  backgroundImage: state.avatarPath != null ? FileImage(File(state.avatarPath!)) : null,
                  child: state.avatarPath == null ? const Icon(Icons.person, size: 40) : null,
                ),
              ),
              const SizedBox(width: 16),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    state.config?.username ?? 'Not logged in',
                    style: Theme.of(context).textTheme.headline6,
                  ),
                  Text(
                    state.config != null ? 'Connected to WebDAV' : 'Not connected',
                    style: Theme.of(context).textTheme.caption,
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }
}