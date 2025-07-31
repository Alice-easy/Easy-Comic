import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../features/reader/bloc/reader_bloc.dart';
import '../features/reader/bloc/reader_event.dart';
import '../pages/settings_screen.dart';

class ReaderAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String comicTitle;
  final String pageIndicatorText;
  final int comicId;

  const ReaderAppBar({
    super.key,
    required this.comicTitle,
    required this.pageIndicatorText,
    required this.comicId,
  });

  @override
  Widget build(BuildContext context) {
    return AppBar(
      title: Text(comicTitle),
      actions: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: Center(child: Text(pageIndicatorText)),
        ),
        IconButton(
          icon: const Icon(Icons.settings),
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const SettingsScreen()),
            ).then((_) {
              // Reload the comic to apply new settings
              context.read<ReaderBloc>().add(LoadComic(comicId: comicId));
            });
          },
        ),
      ],
    );
  }

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);
}