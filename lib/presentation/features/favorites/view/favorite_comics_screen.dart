import 'dart:io';

import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorite_comics_bloc.dart';
import 'package:easy_comic/presentation/pages/reader_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class FavoriteComicsScreen extends StatefulWidget {
  final int favoriteId;
  final String favoriteName;

  const FavoriteComicsScreen({
    super.key,
    required this.favoriteId,
    required this.favoriteName,
  });

  @override
  State<FavoriteComicsScreen> createState() => _FavoriteComicsScreenState();
}

class _FavoriteComicsScreenState extends State<FavoriteComicsScreen> {
  final _searchController = TextEditingController();
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    _searchController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => sl<FavoriteComicsBloc>()
        ..add(LoadFavoriteComics(widget.favoriteId)),
      child: Scaffold(
        appBar: _buildAppBar(context),
        body: _FavoriteComicsBody(
          scrollController: _scrollController,
          searchController: _searchController,
        ),
        floatingActionButton: _buildFloatingActionButton(),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(BuildContext context) {
    return BlocBuilder<FavoriteComicsBloc, FavoriteComicsState>(
      builder: (context, state) {
        if (state is FavoriteComicsLoaded && state.isSelectionMode) {
          return AppBar(
            title: Text('已选择 ${state.selectedComics.length} 个'),
            leading: IconButton(
              icon: const Icon(Icons.close),
              onPressed: () {
                context.read<FavoriteComicsBloc>().add(ToggleSelectionMode());
              },
            ),
            actions: [
              if (state.selectedComics.isNotEmpty)
                IconButton(
                  icon: const Icon(Icons.select_all),
                  onPressed: () {
                    context.read<FavoriteComicsBloc>().add(SelectAllComics());
                  },
                ),
              IconButton(
                icon: const Icon(Icons.delete, color: Colors.red),
                onPressed: state.selectedComics.isNotEmpty
                    ? () => _showRemoveSelectedDialog(context, state.selectedComics.length)
                    : null,
              ),
            ],
          );
        }

        return AppBar(
          title: Text(widget.favoriteName),
          actions: [
            IconButton(
              icon: const Icon(Icons.search),
              onPressed: () => _showSearchDialog(context),
            ),
            PopupMenuButton<FavoriteComicsSortType>(
              icon: const Icon(Icons.sort),
              tooltip: '排序',
              onSelected: (sortType) {
                context.read<FavoriteComicsBloc>().add(SortFavoriteComics(sortType));
              },
              itemBuilder: (context) => [
                const PopupMenuItem(
                  value: FavoriteComicsSortType.title,
                  child: Row(
                    children: [
                      Icon(Icons.sort_by_alpha),
                      SizedBox(width: 8),
                      Text('按标题排序'),
                    ],
                  ),
                ),
                const PopupMenuItem(
                  value: FavoriteComicsSortType.addTime,
                  child: Row(
                    children: [
                      Icon(Icons.access_time),
                      SizedBox(width: 8),
                      Text('按添加时间排序'),
                    ],
                  ),
                ),
                const PopupMenuItem(
                  value: FavoriteComicsSortType.lastRead,
                  child: Row(
                    children: [
                      Icon(Icons.history),
                      SizedBox(width: 8),
                      Text('按最后阅读排序'),
                    ],
                  ),
                ),
                const PopupMenuItem(
                  value: FavoriteComicsSortType.progress,
                  child: Row(
                    children: [
                      Icon(Icons.timeline),
                      SizedBox(width: 8),
                      Text('按阅读进度排序'),
                    ],
                  ),
                ),
              ],
            ),
            PopupMenuButton<String>(
              onSelected: (value) {
                switch (value) {
                  case 'select':
                    context.read<FavoriteComicsBloc>().add(ToggleSelectionMode());
                    break;
                }
              },
              itemBuilder: (context) => [
                const PopupMenuItem(
                  value: 'select',
                  child: Row(
                    children: [
                      Icon(Icons.checklist),
                      SizedBox(width: 8),
                      Text('批量选择'),
                    ],
                  ),
                ),
              ],
            ),
          ],
        );
      },
    ) as PreferredSizeWidget;
  }

  Widget? _buildFloatingActionButton() {
    return BlocBuilder<FavoriteComicsBloc, FavoriteComicsState>(
      builder: (context, state) {
        if (state is FavoriteComicsLoaded && state.isSelectionMode && state.selectedComics.isNotEmpty) {
          return FloatingActionButton.extended(
            onPressed: () => _showRemoveSelectedDialog(context, state.selectedComics.length),
            backgroundColor: Colors.red,
            icon: const Icon(Icons.delete),
            label: Text('移除 ${state.selectedComics.length} 个'),
          );
        }
        return const SizedBox.shrink();
      },
    );
  }

  void _showSearchDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        String searchQuery = '';
        return AlertDialog(
          title: const Text('搜索漫画'),
          content: TextField(
            autofocus: true,
            decoration: const InputDecoration(
              hintText: '输入漫画标题或文件名...',
              border: OutlineInputBorder(),
            ),
            onChanged: (value) => searchQuery = value,
            onSubmitted: (value) {
              Navigator.of(dialogContext).pop();
              context.read<FavoriteComicsBloc>().add(SearchFavoriteComics(value.trim()));
            },
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop();
                context.read<FavoriteComicsBloc>().add(SearchFavoriteComics(searchQuery.trim()));
              },
              child: const Text('搜索'),
            ),
          ],
        );
      },
    );
  }

  void _showRemoveSelectedDialog(BuildContext context, int count) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('确认移除'),
          content: Text('确定要从收藏夹中移除 $count 个漫画吗？'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop();
                context.read<FavoriteComicsBloc>().add(RemoveSelectedComics());
              },
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('移除'),
            ),
          ],
        );
      },
    );
  }
}

class _FavoriteComicsBody extends StatelessWidget {
  final ScrollController scrollController;
  final TextEditingController searchController;

  const _FavoriteComicsBody({
    required this.scrollController,
    required this.searchController,
  });

  @override
  Widget build(BuildContext context) {
    return BlocListener<FavoriteComicsBloc, FavoriteComicsState>(
      listener: (context, state) {
        if (state is FavoriteComicsError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: Colors.red,
            ),
          );
        }
      },
      child: BlocBuilder<FavoriteComicsBloc, FavoriteComicsState>(
        builder: (context, state) {
          if (state is FavoriteComicsLoading) {
            return const Center(child: CircularProgressIndicator());
          } else if (state is FavoriteComicsLoaded) {
            if (state.displayedComics.isEmpty) {
              return _buildEmptyState(state);
            }
            return _buildComicsGrid(state);
          }
          return const Center(child: Text('无法加载漫画'));
        },
      ),
    );
  }

  Widget _buildEmptyState(FavoriteComicsLoaded state) {
    if (state.searchQuery.isNotEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.search_off, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text(
              '没有找到匹配 "${state.searchQuery}" 的漫画',
              style: const TextStyle(fontSize: 16, color: Colors.grey),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            BlocBuilder<FavoriteComicsBloc, FavoriteComicsState>(
              builder: (context, state) {
                return ElevatedButton(
                  onPressed: () {
                    context.read<FavoriteComicsBloc>().add(SearchFavoriteComics(''));
                  },
                  child: const Text('清除搜索'),
                );
              },
            ),
          ],
        ),
      );
    }

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.favorite_outline,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            '收藏夹是空的',
            style: TextStyle(
              fontSize: 18,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '从书架中添加漫画到这个收藏夹',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[500],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildComicsGrid(FavoriteComicsLoaded state) {
    return GridView.builder(
      controller: scrollController,
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        childAspectRatio: 2 / 3,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
      ),
      itemCount: state.displayedComics.length,
      itemBuilder: (context, index) {
        final comic = state.displayedComics[index];
        final isSelected = state.selectedComics.contains(comic.id);
        
        return _ComicCard(
          comic: comic,
          isSelectionMode: state.isSelectionMode,
          isSelected: isSelected,
          searchQuery: state.searchQuery,
          onTap: () {
            if (state.isSelectionMode) {
              context.read<FavoriteComicsBloc>().add(ToggleComicSelection(comic.id));
            } else {
              _navigateToReader(context, comic);
            }
          },
          onLongPress: () {
            if (!state.isSelectionMode) {
              context.read<FavoriteComicsBloc>().add(ToggleSelectionMode());
              context.read<FavoriteComicsBloc>().add(ToggleComicSelection(comic.id));
            }
          },
        );
      },
    );
  }

  void _navigateToReader(BuildContext context, Comic comic) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => ReaderScreen(filePath: comic.filePath),
      ),
    );
  }
}

class _ComicCard extends StatelessWidget {
  final Comic comic;
  final bool isSelectionMode;
  final bool isSelected;
  final String searchQuery;
  final VoidCallback onTap;
  final VoidCallback onLongPress;

  const _ComicCard({
    required this.comic,
    required this.isSelectionMode,
    required this.isSelected,
    required this.searchQuery,
    required this.onTap,
    required this.onLongPress,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      onLongPress: onLongPress,
      child: Stack(
        children: [
          Card(
            elevation: isSelected ? 4 : 2,
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(8),
                border: isSelected
                    ? Border.all(color: Theme.of(context).primaryColor, width: 2)
                    : null,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Expanded(
                    child: ClipRRect(
                      borderRadius: const BorderRadius.vertical(top: Radius.circular(8)),
                      child: _ComicCover(comic: comic),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: _HighlightedText(
                      text: comic.title.isNotEmpty ? comic.title : comic.fileName,
                      query: searchQuery,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontSize: 12),
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (isSelectionMode)
            Positioned(
              top: 8,
              right: 8,
              child: Container(
                decoration: BoxDecoration(
                  color: isSelected 
                      ? Theme.of(context).primaryColor 
                      : Colors.white.withOpacity(0.8),
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: isSelected 
                        ? Theme.of(context).primaryColor 
                        : Colors.grey,
                    width: 2,
                  ),
                ),
                child: Icon(
                  isSelected ? Icons.check : null,
                  color: Colors.white,
                  size: 20,
                ),
              ),
            ),
        ],
      ),
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

class _HighlightedText extends StatelessWidget {
  final String text;
  final String query;
  final int? maxLines;
  final TextOverflow? overflow;
  final TextAlign? textAlign;
  final TextStyle? style;

  const _HighlightedText({
    required this.text,
    required this.query,
    this.maxLines,
    this.overflow,
    this.textAlign,
    this.style,
  });

  @override
  Widget build(BuildContext context) {
    if (query.isEmpty) {
      return Text(
        text,
        maxLines: maxLines,
        overflow: overflow,
        textAlign: textAlign,
        style: style,
      );
    }

    final spans = <TextSpan>[];
    final lowerText = text.toLowerCase();
    final lowerQuery = query.toLowerCase();
    
    int start = 0;
    int index = lowerText.indexOf(lowerQuery);
    
    while (index != -1) {
      // 添加匹配前的文本
      if (index > start) {
        spans.add(TextSpan(text: text.substring(start, index)));
      }
      
      // 添加高亮的匹配文本
      spans.add(TextSpan(
        text: text.substring(index, index + query.length),
        style: const TextStyle(
          backgroundColor: Colors.yellow,
          fontWeight: FontWeight.bold,
        ),
      ));
      
      start = index + query.length;
      index = lowerText.indexOf(lowerQuery, start);
    }
    
    // 添加剩余的文本
    if (start < text.length) {
      spans.add(TextSpan(text: text.substring(start)));
    }

    return RichText(
      text: TextSpan(
        children: spans,
        style: style ?? DefaultTextStyle.of(context).style,
      ),
      maxLines: maxLines,
      overflow: overflow ?? TextOverflow.clip,
      textAlign: textAlign ?? TextAlign.start,
    );
  }
}