import 'package:flutter/material.dart';

class FavoriteComicsScreen extends StatelessWidget {
  final int favoriteId;
  final String favoriteName;

  const FavoriteComicsScreen(
      {super.key, required this.favoriteId, required this.favoriteName});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(favoriteName),
      ),
      body: const Center(
        child: Text('该收藏夹中的漫画列表'),
      ),
    );
  }
}