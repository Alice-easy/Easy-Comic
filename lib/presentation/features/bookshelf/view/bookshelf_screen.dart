import 'dart:io';

import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorites_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/bookshelf_bloc.dart';
import '../bloc/bookshelf_event.dart';
import '../bloc/bookshelf_state.dart';

class BookshelfScreen extends StatefulWidget {
  const BookshelfScreen({super.key});

  @override
  State<BookshelfScreen> createState() => _BookshelfScreenState();
}

class _BookshelfScreenState extends State<BookshelfScreen> {
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (_) =>
              sl<BookshelfBloc>()..add(const LoadBookshelf('default')),
        ),
        BlocProvider(
          create: (_) => sl<FavoritesBloc>()..add(LoadFavorites()),
        ),
      ],
      child: Scaffold(
        appBar: AppBar(
          title: const Text('书架'),
          actions: [
            IconButton(
              icon: const Icon(Icons.search),
              onPressed: () {
                // TODO: Implement search functionality
              },
            ),
            IconButton(
              icon: const Icon(Icons.sort),
              onPressed: () {
                // TODO: Implement sort functionality
              },
            ),
          ],
        ),
        body: _BookshelfBody(scrollController: _scrollController),
        floatingActionButton: BlocBuilder<BookshelfBloc, BookshelfState>(
          builder: (context, state) {
            return FloatingActionButton(
              onPressed: () {
                context
                    .read<BookshelfBloc>()
                    .add(const ImportComicEvent('default'));
              },
              child: const Icon(Icons.add),
            );
          },
        ),
      ),
    );
  }
  @override
  void dispose() {
    _scrollController
      ..removeListener(_onScroll)
      ..dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_isBottom) {
      context.read<BookshelfBloc>().add(const LoadMoreComics());
    }
  }

  bool get _isBottom {
    if (!_scrollController.hasClients) return false;
    final maxScroll = _scrollController.position.maxScrollExtent;
    final currentScroll = _scrollController.offset;
    return currentScroll >= (maxScroll * 0.9);
  }
}

class _BookshelfBody extends StatelessWidget {
  final ScrollController scrollController;

  const _BookshelfBody({required this.scrollController});

  @override
  Widget build(BuildContext context) {
    return BlocListener<BookshelfBloc, BookshelfState>(
      listener: (context, state) {
        if (state is BookshelfError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: Colors.red,
            ),
          );
        }
      },
      child: BlocBuilder<BookshelfBloc, BookshelfState>(
        builder: (context, state) {
          if (state is BookshelfLoading || state is BookshelfInitial) {
            return const Center(child: CircularProgressIndicator());
          } else if (state is BookshelfLoaded) {
            if (state.comics.isEmpty) {
              return const Center(child: Text('书架是空的。'));
            }
            return GridView.builder(
              controller: scrollController,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                childAspectRatio: 2 / 3,
              ),
              itemCount: state.hasReachedMax
                  ? state.comics.length
                  : state.comics.length + 1,
              itemBuilder: (context, index) {
                if (index >= state.comics.length) {
                  return const Center(
                    child: SizedBox(
                      height: 24,
                      width: 24,
                      child: CircularProgressIndicator(strokeWidth: 1.5),
                    ),
                  );
                }
                final comic = state.comics[index];
                return GestureDetector(
                  onLongPress: () {
                    _showAddToFavoriteDialog(context, comic);
                  },
                  child: Card(
                    child: Column(
                      children: [
                        Expanded(child: _ComicCover(comic: comic)),
                        Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: Text(
                            comic.fileName,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            );
          }
          // When there is an error, we now show an empty center view
          // because the error message is handled by the BlocListener.
          return const Center(child: Text('无法加载漫画。'));
        },
      ),
    );
  }

  void _showAddToFavoriteDialog(BuildContext context, Comic comic) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return BlocBuilder<FavoritesBloc, FavoritesState>(
          builder: (context, state) {
            if (state is FavoritesLoaded) {
              return AlertDialog(
                title: const Text('添加到收藏夹'),
                content: SizedBox(
                  width: double.maxFinite,
                  child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: state.favorites.length,
                    itemBuilder: (context, index) {
                      final favorite = state.favorites[index];
                      return ListTile(
                        title: Text(favorite.name),
                        onTap: () {
                          context
                              .read<FavoritesBloc>()
                              .add(AddComicToFavorite(favorite.id, comic.id));
                          Navigator.of(dialogContext).pop();
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                                content: Text(
                                    '已将 "${comic.title}" 添加到 "${favorite.name}"')),
                          );
                        },
                      );
                    },
                  ),
                ),
              );
            }
            return const AlertDialog(
              title: Text('添加到收藏夹'),
              content: Text('正在加载收藏夹...'),
            );
          },
        );
      },
    );
  }
}

class _ComicCover extends StatelessWidget {
  final Comic comic;

  const _ComicCover({required this.comic});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<File>(
      future: sl<CacheService>().getCoverImage(comic.path),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Container(
            color: Colors.grey[300],
            child: const Center(child: CircularProgressIndicator()),
          );
        } else if (snapshot.hasError) {
          return Container(
            color: Colors.grey[300],
            child: const Icon(Icons.error, size: 50, color: Colors.red),
          );
        } else if (snapshot.hasData) {
          return Image.file(
            snapshot.data!,
            fit: BoxFit.cover,
            width: double.infinity,
          );
        } else {
          return Container(
            color: Colors.grey[300],
            child: const Icon(Icons.book, size: 50, color: Colors.grey),
          );
        }
      },
    );
  }
}