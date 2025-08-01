import 'dart:io';

import 'package:easy_comic/domain/entities/comic.dart';
import 'package:easy_comic/domain/services/cache_service.dart';
import 'package:easy_comic/domain/repositories/comic_repository.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorites_bloc.dart';
import 'package:easy_comic/presentation/pages/reader_screen.dart';
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
  final _searchController = TextEditingController();

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
          title: BlocBuilder<BookshelfBloc, BookshelfState>(
            builder: (context, state) {
              if (state is BookshelfLoaded && state.isSearching) {
                return TextField(
                  controller: _searchController,
                  autofocus: true,
                  style: const TextStyle(color: Colors.white),
                  decoration: const InputDecoration(
                    hintText: '搜索漫画...',
                    hintStyle: TextStyle(color: Colors.white70),
                    border: InputBorder.none,
                  ),
                  onSubmitted: (query) {
                    if (query.trim().isEmpty) {
                      context.read<BookshelfBloc>().add(const ClearSearch());
                    } else {
                      context.read<BookshelfBloc>().add(SearchComics(query.trim()));
                    }
                  },
                );
              }
              return Row(
                children: [
                  const Text('书架'),
                  if (state is BookshelfLoaded && state.searchQuery.isNotEmpty) ...[
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        '搜索: ${state.searchQuery}',
                        style: const TextStyle(fontSize: 12, color: Colors.blue),
                      ),
                    ),
                  ],
                ],
              );
            },
          ),
          actions: [
            BlocBuilder<BookshelfBloc, BookshelfState>(
              builder: (context, state) {
                if (state is BookshelfLoaded) {
                  if (state.isSearching) {
                    return Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.search),
                          onPressed: () {
                            final query = _searchController.text.trim();
                            if (query.isEmpty) {
                              context.read<BookshelfBloc>().add(const ClearSearch());
                            } else {
                              context.read<BookshelfBloc>().add(SearchComics(query));
                            }
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.close),
                          onPressed: () {
                            _searchController.clear();
                            context.read<BookshelfBloc>().add(const ClearSearch());
                          },
                        ),
                      ],
                    );
                  } else if (state.searchQuery.isNotEmpty) {
                    return Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.search),
                          onPressed: () => _showSearchDialog(context),
                        ),
                        IconButton(
                          icon: const Icon(Icons.clear),
                          onPressed: () {
                            context.read<BookshelfBloc>().add(const ClearSearch());
                          },
                        ),
                        IconButton(
                          icon: const Icon(Icons.sort),
                          onPressed: () => _showSortDialog(context),
                        ),
                      ],
                    );
                  }
                }
                return Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.search),
                      onPressed: () => _showSearchDialog(context),
                    ),
                    IconButton(
                      icon: const Icon(Icons.sort),
                      onPressed: () => _showSortDialog(context),
                    ),
                  ],
                );
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
    _searchController.dispose();
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
              if (value.trim().isNotEmpty) {
                context.read<BookshelfBloc>().add(SearchComics(value.trim()));
              }
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
                if (searchQuery.trim().isNotEmpty) {
                  context.read<BookshelfBloc>().add(SearchComics(searchQuery.trim()));
                }
              },
              child: const Text('搜索'),
            ),
          ],
        );
      },
    );
  }

  void _showSortDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return BlocBuilder<BookshelfBloc, BookshelfState>(
          builder: (context, state) {
            final currentSortType = state is BookshelfLoaded ? state.currentSortType : null;
            return AlertDialog(
              title: const Text('排序选项'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _SortOption(
                    title: '按添加时间',
                    subtitle: '最新添加的漫画在前',
                    sortType: SortType.dateAdded,
                    currentSortType: currentSortType,
                    onTap: () {
                      Navigator.of(dialogContext).pop();
                      context.read<BookshelfBloc>().add(const SortComics(SortType.dateAdded));
                    },
                  ),
                  _SortOption(
                    title: '按标题',
                    subtitle: '按字母顺序排列',
                    sortType: SortType.title,
                    currentSortType: currentSortType,
                    onTap: () {
                      Navigator.of(dialogContext).pop();
                      context.read<BookshelfBloc>().add(const SortComics(SortType.title));
                    },
                  ),
                  _SortOption(
                    title: '按作者',
                    subtitle: '按作者名称排列',
                    sortType: SortType.author,
                    currentSortType: currentSortType,
                    onTap: () {
                      Navigator.of(dialogContext).pop();
                      context.read<BookshelfBloc>().add(const SortComics(SortType.author));
                    },
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(dialogContext).pop(),
                  child: const Text('取消'),
                ),
              ],
            );
          },
        );
      },
    );
  }
}

class _SortOption extends StatelessWidget {
  final String title;
  final String subtitle;
  final SortType sortType;
  final SortType? currentSortType;
  final VoidCallback onTap;

  const _SortOption({
    required this.title,
    required this.subtitle,
    required this.sortType,
    required this.currentSortType,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final isSelected = currentSortType == sortType;
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
        decoration: BoxDecoration(
          color: isSelected ? Theme.of(context).primaryColor.withOpacity(0.1) : null,
          borderRadius: BorderRadius.circular(8),
          border: isSelected ? Border.all(color: Theme.of(context).primaryColor) : null,
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                      color: isSelected ? Theme.of(context).primaryColor : null,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            ),
            if (isSelected)
              Icon(
                Icons.check_circle,
                color: Theme.of(context).primaryColor,
              ),
          ],
        ),
      ),
    );
  }
}

class _HighlightedText extends StatelessWidget {
  final String text;
  final String query;
  final int? maxLines;
  final TextOverflow? overflow;
  final TextAlign? textAlign;

  const _HighlightedText({
    required this.text,
    required this.query,
    this.maxLines,
    this.overflow,
    this.textAlign,
  });

  @override
  Widget build(BuildContext context) {
    if (query.isEmpty) {
      return Text(
        text,
        maxLines: maxLines,
        overflow: overflow,
        textAlign: textAlign,
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
        style: DefaultTextStyle.of(context).style,
      ),
      maxLines: maxLines,
      overflow: overflow ?? TextOverflow.clip,
      textAlign: textAlign ?? TextAlign.start,
    );
  }
}

class _DetailRow extends StatelessWidget {
  final String label;
  final String value;

  const _DetailRow({
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              '$label:',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                color: Colors.grey,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontSize: 14),
            ),
          ),
        ],
      ),
    );
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
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: () {
                          context.read<BookshelfBloc>().add(const ClearSearch());
                        },
                        child: const Text('清除搜索'),
                      ),
                    ],
                  ),
                );
              }
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
                  onTap: () {
                    _navigateToReader(context, comic);
                  },
                  onLongPress: () {
                    _showComicOptionsDialog(context, comic);
                  },
                  child: Card(
                    child: Column(
                      children: [
                        Expanded(child: _ComicCover(comic: comic)),
                        Padding(
                          padding: const EdgeInsets.all(8.0),
                          child: BlocBuilder<BookshelfBloc, BookshelfState>(
                            builder: (context, state) {
                              if (state is BookshelfLoaded && state.searchQuery.isNotEmpty) {
                                return _HighlightedText(
                                  text: comic.fileName,
                                  query: state.searchQuery,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  textAlign: TextAlign.center,
                                );
                              }
                              return Text(
                                comic.fileName,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                textAlign: TextAlign.center,
                              );
                            },
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

  void _navigateToReader(BuildContext context, Comic comic) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => ReaderScreen(filePath: comic.filePath),
      ),
    );
  }

  void _showComicOptionsDialog(BuildContext context, Comic comic) {
    showModalBottomSheet(
      context: context,
      builder: (bottomSheetContext) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: SizedBox(
                        width: 60,
                        height: 80,
                        child: _ComicCover(comic: comic),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            comic.title,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 4),
                          Text(
                            comic.fileName,
                            style: const TextStyle(
                              fontSize: 12,
                              color: Colors.grey,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const Divider(),
              ListTile(
                leading: const Icon(Icons.favorite_border),
                title: const Text('添加到收藏夹'),
                onTap: () {
                  Navigator.of(bottomSheetContext).pop();
                  _showAddToFavoriteDialog(context, comic);
                },
              ),
              ListTile(
                leading: const Icon(Icons.info_outline),
                title: const Text('查看详情'),
                onTap: () {
                  Navigator.of(bottomSheetContext).pop();
                  _showComicDetailsDialog(context, comic);
                },
              ),
              ListTile(
                leading: const Icon(Icons.edit_outlined),
                title: const Text('编辑标题'),
                onTap: () {
                  Navigator.of(bottomSheetContext).pop();
                  _showEditTitleDialog(context, comic);
                },
              ),
              ListTile(
                leading: const Icon(Icons.delete_outline, color: Colors.red),
                title: const Text('删除漫画', style: TextStyle(color: Colors.red)),
                onTap: () {
                  Navigator.of(bottomSheetContext).pop();
                  _showDeleteConfirmDialog(context, comic);
                },
              ),
              const SizedBox(height: 16),
            ],
          ),
        );
      },
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
                actions: [
                  TextButton(
                    onPressed: () => Navigator.of(dialogContext).pop(),
                    child: const Text('取消'),
                  ),
                ],
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

  void _showComicDetailsDialog(BuildContext context, Comic comic) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('漫画详情'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _DetailRow(label: '标题', value: comic.title),
              _DetailRow(label: '文件名', value: comic.fileName),
              _DetailRow(label: '路径', value: comic.path),
              if (comic.author.isNotEmpty) _DetailRow(label: '作者', value: comic.author),
              _DetailRow(label: '添加时间', value: comic.addedAt.toString().split('.')[0]),
              if (comic.lastReadAt != null)
                _DetailRow(label: '最后阅读', value: comic.lastReadAt.toString().split('.')[0]),
              _DetailRow(label: '阅读进度', value: '${comic.currentPage}/${comic.totalPages}'),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: const Text('关闭'),
            ),
          ],
        );
      },
    );
  }

  void _showEditTitleDialog(BuildContext context, Comic comic) {
    final titleController = TextEditingController(text: comic.title);
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('编辑标题'),
          content: TextField(
            controller: titleController,
            autofocus: true,
            decoration: const InputDecoration(
              labelText: '漫画标题',
              border: OutlineInputBorder(),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop();
                // TODO: 实现更新漫画标题的功能
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('标题编辑功能暂未实现')),
                );
              },
              child: const Text('保存'),
            ),
          ],
        );
      },
    );
  }

  void _showDeleteConfirmDialog(BuildContext context, Comic comic) {
    showDialog(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('确认删除'),
          content: Text('确定要删除漫画 "${comic.title}" 吗？此操作无法撤销。'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogContext).pop(),
              child: const Text('取消'),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(dialogContext).pop();
                context.read<BookshelfBloc>().add(DeleteComic(comic.id));
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('已删除 "${comic.title}"')),
                );
              },
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('删除'),
            ),
          ],
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