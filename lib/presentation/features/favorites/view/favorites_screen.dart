import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorites_bloc.dart';
import 'package:easy_comic/presentation/features/favorites/view/favorite_comics_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class FavoritesScreen extends StatelessWidget {
  const FavoritesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<FavoritesBloc>()..add(LoadFavorites()),
      child: Scaffold(
        appBar: AppBar(
          title: const Text('收藏夹'),
        ),
        body: BlocBuilder<FavoritesBloc, FavoritesState>(
          builder: (context, state) {
            if (state is FavoritesLoading) {
              return const Center(child: CircularProgressIndicator());
            } else if (state is FavoritesLoaded) {
              if (state.favorites.isEmpty) {
                return const Center(child: Text('还没有收藏夹，快去创建一个吧'));
              }
              return ListView.builder(
                itemCount: state.favorites.length,
                itemBuilder: (context, index) {
                  final favorite = state.favorites[index];
                  return ListTile(
                    title: Text(favorite.name),
                    trailing: IconButton(
                      icon: const Icon(Icons.delete),
                      onPressed: () {
                        context
                            .read<FavoritesBloc>()
                            .add(DeleteFavorite(favorite.id));
                      },
                    ),
                    onTap: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => FavoriteComicsScreen(
                            favoriteId: favorite.id,
                            favoriteName: favorite.name,
                          ),
                        ),
                      );
                    },
                  );
                },
              );
            } else if (state is FavoritesError) {
              return Center(child: Text(state.message));
            }
            return const Center(child: Text('收藏夹'));
          },
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () {
            _showCreateFavoriteDialog(context);
          },
          child: const Icon(Icons.add),
        ),
      ),
    );
  }

  void _showCreateFavoriteDialog(BuildContext blocContext) {
    final nameController = TextEditingController();
    showDialog(
      context: blocContext,
      builder: (context) {
        return AlertDialog(
          title: const Text('创建新收藏夹'),
          content: TextField(
            controller: nameController,
            decoration: const InputDecoration(hintText: '收藏夹名称'),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                if (nameController.text.isNotEmpty) {
                  blocContext
                      .read<FavoritesBloc>()
                      .add(CreateFavorite(nameController.text));
                  Navigator.of(context).pop();
                }
              },
              child: const Text('创建'),
            ),
          ],
        );
      },
    );
  }
}