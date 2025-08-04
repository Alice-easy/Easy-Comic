import 'dart:io';

import 'package:easy_comic/core/di/injection_container.dart';
import 'package:easy_comic/domain/entities/manga.dart';
import 'package:easy_comic/presentation/features/library/bloc/library_bloc.dart';
import 'package:easy_comic/presentation/widgets/loading_overlay.dart';
import 'package:easy_comic/presentation/features/reader/reader_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LibraryScreen extends StatefulWidget {
  const LibraryScreen({super.key});

  @override
  State<LibraryScreen> createState() => _LibraryScreenState();
}

class _LibraryScreenState extends State<LibraryScreen> {
  bool _isSearch = false;
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<LibraryBloc>()..add(LoadLibrary()),
      child: Builder(builder: (context) {
        return Scaffold(
          body: BlocConsumer<LibraryBloc, LibraryState>(
            listener: (context, state) {
              if (state is LibraryError) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Error: ${state.message}'),
                    backgroundColor: Colors.red,
                  ),
                );
              }
            },
            builder: (context, state) {
              final isLoading = state is LibraryLoading;
              Widget body;
              if (state is LibraryLoaded) {
                body = GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    childAspectRatio: 0.7,
                    crossAxisSpacing: 8,
                    mainAxisSpacing: 8,
                  ),
                  itemCount: state.displayedMangas.length,
                  itemBuilder: (context, index) {
                    final manga = state.displayedMangas[index];
                    return _buildMangaCard(context, manga);
                  },
                  padding: const EdgeInsets.all(8),
                );
              } else if (state is LibraryError) {
                body = Center(child: Text(state.message));
              } else {
                body = const Center(child: Text('Welcome to your library!'));
              }

              return LoadingOverlay(
                isLoading: isLoading,
                message: 'Importing manga...',
                child: body,
              );
            },
          ),
        );
      }),
    );
  }


  Widget _buildMangaCard(BuildContext context, Manga manga) {
    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => BlocProvider.value(
              value: BlocProvider.of<LibraryBloc>(context),
              child: ReaderScreen(mangaId: manga.id),
            ),
          ),
        );
      },
      onLongPress: () {
        _showContextMenu(context, manga);
      },
      child: Card(
        clipBehavior: Clip.antiAlias,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Expanded(
              child: manga.coverPath.isNotEmpty
                  ? Image.file(
                      File(manga.coverPath),
                      fit: BoxFit.cover,
                      cacheWidth: 200, // Optimize image caching
                    )
                  : Container(
                      color: Colors.grey,
                      child: const Icon(Icons.image_not_supported),
                    ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Text(
                manga.title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                textAlign: TextAlign.center,
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showContextMenu(BuildContext context, Manga manga) {
    showModalBottomSheet(
      context: context,
      builder: (_) {
        return Wrap(
          children: [
            ListTile(
              leading: Icon(manga.isFavorite ? Icons.favorite : Icons.favorite_border),
              title: Text(manga.isFavorite ? 'Unfavorite' : 'Favorite'),
              onTap: () {
                context.read<LibraryBloc>().add(ToggleFavorite(manga.id));
                Navigator.pop(context);
              },
            ),
            ListTile(
              leading: const Icon(Icons.delete),
              title: const Text('Delete'),
              onTap: () {
                Navigator.pop(context);
                _showDeleteConfirmation(context, manga);
              },
            ),
          ],
        );
      },
    );
  }

  void _showDeleteConfirmation(BuildContext context, Manga manga) {
    showDialog(
      context: context,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text('Delete Manga'),
          content: Text('Are you sure you want to delete "${manga.title}"?'),
          actions: [
            TextButton(
              child: const Text('Cancel'),
              onPressed: () {
                Navigator.of(dialogContext).pop();
              },
            ),
            TextButton(
              child: const Text('Delete'),
              onPressed: () {
                context.read<LibraryBloc>().add(DeleteManga(manga.id));
                Navigator.of(dialogContext).pop();
              },
            ),
          ],
        );
      },
    );
  }
}