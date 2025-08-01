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
      child: BlocListener<FavoritesBloc, FavoritesState>(
        listener: (context, state) {
          if (state is FavoriteOperationSuccess) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(state.message)),
            );
          }
        },
        child: Scaffold(
          appBar: _buildAppBar(context),
          body: BlocBuilder<FavoritesBloc, FavoritesState>(
            builder: (context, state) {
              if (state is FavoritesLoading) {
                return const Center(child: CircularProgressIndicator());
              } else if (state is FavoritesLoaded) {
                if (state.favorites.isEmpty) {
                  return _buildEmptyState();
                }
                return _buildFavoritesList(context, state);
              } else if (state is FavoritesError) {
                return _buildErrorState(state.message);
              }
              return _buildInitialState();
            },
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _showCreateFavoriteDialog(context),
            child: const Icon(Icons.add),
            tooltip: '创建收藏夹',
          ),
        ),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar(BuildContext context) {
    return AppBar(
      title: const Text('收藏夹'),
      actions: [
        PopupMenuButton<FavoriteSortType>(
          icon: const Icon(Icons.sort),
          tooltip: '排序',
          onSelected: (sortType) {
            context.read<FavoritesBloc>().add(SortFavorites(sortType));
          },
          itemBuilder: (context) => [
            const PopupMenuItem(
              value: FavoriteSortType.nameAsc,
              child: Row(
                children: [
                  Icon(Icons.sort_by_alpha),
                  SizedBox(width: 8),
                  Text('按名称升序'),
                ],
              ),
            ),
            const PopupMenuItem(
              value: FavoriteSortType.nameDesc,
              child: Row(
                children: [
                  Icon(Icons.sort_by_alpha),
                  SizedBox(width: 8),
                  Text('按名称降序'),
                ],
              ),
            ),
            const PopupMenuItem(
              value: FavoriteSortType.createTimeDesc,
              child: Row(
                children: [
                  Icon(Icons.access_time),
                  SizedBox(width: 8),
                  Text('按创建时间降序'),
                ],
              ),
            ),
            const PopupMenuItem(
              value: FavoriteSortType.createTimeAsc,
              child: Row(
                children: [
                  Icon(Icons.access_time),
                  SizedBox(width: 8),
                  Text('按创建时间升序'),
                ],
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildEmptyState() {
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
            '还没有收藏夹',
            style: TextStyle(
              fontSize: 18,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '点击右下角的 + 按钮创建一个收藏夹',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[500],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(String message) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(
            Icons.error_outline,
            size: 64,
            color: Colors.red,
          ),
          const SizedBox(height: 16),
          Text(
            message,
            style: const TextStyle(fontSize: 16),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildInitialState() {
    return const Center(
      child: Text('收藏夹'),
    );
  }

  Widget _buildFavoritesList(BuildContext context, FavoritesLoaded state) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: state.favorites.length,
      itemBuilder: (context, index) {
        final favorite = state.favorites[index];
        final comicCount = state.comicCounts[favorite.id] ?? 0;
        
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: _FavoriteCard(
            favorite: favorite,
            comicCount: comicCount,
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
            onLongPress: () => _showFavoriteOptionsBottomSheet(context, favorite, comicCount),
            onDelete: () => _showDeleteConfirmDialog(context, favorite, comicCount),
            onRename: () => _showRenameFavoriteDialog(context, favorite),
          ),
        );
      },
    );
  }

  void _showCreateFavoriteDialog(BuildContext blocContext) {
    final nameController = TextEditingController();
    String? errorMessage;

    showDialog(
      context: blocContext,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('创建新收藏夹'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: nameController,
                    decoration: InputDecoration(
                      hintText: '收藏夹名称',
                      errorText: errorMessage,
                    ),
                    onChanged: (value) {
                      if (errorMessage != null) {
                        setState(() {
                          errorMessage = null;
                        });
                      }
                    },
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () {
                    Navigator.of(context).pop();
                  },
                  child: const Text('取消'),
                ),
                TextButton(
                  onPressed: () async {
                    final name = nameController.text.trim();
                    if (name.isEmpty) {
                      setState(() {
                        errorMessage = '收藏夹名称不能为空';
                      });
                      return;
                    }

                    if (name.length > 50) {
                      setState(() {
                        errorMessage = '收藏夹名称不能超过50个字符';
                      });
                      return;
                    }

                    // 验证名称
                    blocContext.read<FavoritesBloc>().add(ValidateFavoriteName(name));
                    
                    Navigator.of(context).pop();
                    _showCreateConfirmDialog(blocContext, name);
                  },
                  child: const Text('创建'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showCreateConfirmDialog(BuildContext blocContext, String name) {
    showDialog(
      context: blocContext,
      builder: (context) {
        return BlocListener<FavoritesBloc, FavoritesState>(
          listener: (context, state) {
            if (state is FavoriteNameValidation) {
              Navigator.of(context).pop();
              if (state.isValid) {
                blocContext.read<FavoritesBloc>().add(CreateFavorite(name));
              } else {
                _showErrorDialog(blocContext, state.errorMessage ?? '名称验证失败');
              }
            }
          },
          child: const AlertDialog(
            content: Row(
              children: [
                CircularProgressIndicator(),
                SizedBox(width: 16),
                Text('正在验证收藏夹名称...'),
              ],
            ),
          ),
        );
      },
    );
  }

  void _showErrorDialog(BuildContext context, String message) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('错误'),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('确定'),
            ),
          ],
        );
      },
    );
  }

  void _showDeleteConfirmDialog(BuildContext context, favorite, int comicCount) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('删除收藏夹'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('确定要删除收藏夹 "${favorite.name}" 吗？'),
              const SizedBox(height: 8),
              if (comicCount > 0)
                Text(
                  '该收藏夹包含 $comicCount 个漫画，删除后漫画将从收藏夹中移除。',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
            ],
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
                context.read<FavoritesBloc>().add(DeleteFavorite(favorite.id));
                Navigator.of(context).pop();
              },
              style: TextButton.styleFrom(
                foregroundColor: Colors.red,
              ),
              child: const Text('删除'),
            ),
          ],
        );
      },
    );
  }

  void _showRenameFavoriteDialog(BuildContext context, favorite) {
    final nameController = TextEditingController(text: favorite.name);
    String? errorMessage;

    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('重命名收藏夹'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: nameController,
                    decoration: InputDecoration(
                      hintText: '收藏夹名称',
                      errorText: errorMessage,
                    ),
                    onChanged: (value) {
                      if (errorMessage != null) {
                        setState(() {
                          errorMessage = null;
                        });
                      }
                    },
                  ),
                ],
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
                    final newName = nameController.text.trim();
                    if (newName.isEmpty) {
                      setState(() {
                        errorMessage = '收藏夹名称不能为空';
                      });
                      return;
                    }

                    if (newName.length > 50) {
                      setState(() {
                        errorMessage = '收藏夹名称不能超过50个字符';
                      });
                      return;
                    }

                    if (newName == favorite.name) {
                      Navigator.of(context).pop();
                      return;
                    }

                    context.read<FavoritesBloc>().add(RenameFavorite(favorite.id, newName));
                    Navigator.of(context).pop();
                  },
                  child: const Text('确定'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showFavoriteOptionsBottomSheet(BuildContext context, favorite, int comicCount) {
    showModalBottomSheet(
      context: context,
      builder: (context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    const Icon(Icons.folder, size: 24),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            favorite.name,
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            '$comicCount 个漫画',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[600],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const Divider(height: 1),
              ListTile(
                leading: const Icon(Icons.edit),
                title: const Text('重命名'),
                onTap: () {
                  Navigator.of(context).pop();
                  _showRenameFavoriteDialog(context, favorite);
                },
              ),
              ListTile(
                leading: const Icon(Icons.info_outline),
                title: const Text('详细信息'),
                onTap: () {
                  Navigator.of(context).pop();
                  _showFavoriteInfoDialog(context, favorite, comicCount);
                },
              ),
              ListTile(
                leading: const Icon(Icons.delete, color: Colors.red),
                title: const Text('删除', style: TextStyle(color: Colors.red)),
                onTap: () {
                  Navigator.of(context).pop();
                  _showDeleteConfirmDialog(context, favorite, comicCount);
                },
              ),
            ],
          ),
        );
      },
    );
  }

  void _showFavoriteInfoDialog(BuildContext context, favorite, int comicCount) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(favorite.name),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _InfoRow(label: '漫画数量', value: '$comicCount 个'),
              _InfoRow(
                label: '创建时间',
                value: _formatDateTime(favorite.createTime),
              ),
              if (favorite.description?.isNotEmpty == true)
                _InfoRow(label: '描述', value: favorite.description!),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: const Text('确定'),
            ),
          ],
        );
      },
    );
  }

  String _formatDateTime(DateTime dateTime) {
    return '${dateTime.year}-${dateTime.month.toString().padLeft(2, '0')}-${dateTime.day.toString().padLeft(2, '0')} '
        '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}';
  }
}

class _FavoriteCard extends StatelessWidget {
  final dynamic favorite;
  final int comicCount;
  final VoidCallback onTap;
  final VoidCallback onLongPress;
  final VoidCallback onDelete;
  final VoidCallback onRename;

  const _FavoriteCard({
    required this.favorite,
    required this.comicCount,
    required this.onTap,
    required this.onLongPress,
    required this.onDelete,
    required this.onRename,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: onTap,
        onLongPress: onLongPress,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              // 收藏夹图标或封面预览
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: Theme.of(context).primaryColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  Icons.folder,
                  size: 32,
                  color: Theme.of(context).primaryColor,
                ),
              ),
              const SizedBox(width: 16),
              // 收藏夹信息
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      favorite.name,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '$comicCount 个漫画',
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '创建于 ${_formatDate(favorite.createTime)}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[500],
                      ),
                    ),
                  ],
                ),
              ),
              // 操作按钮
              PopupMenuButton<String>(
                onSelected: (value) {
                  switch (value) {
                    case 'rename':
                      onRename();
                      break;
                    case 'delete':
                      onDelete();
                      break;
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(
                    value: 'rename',
                    child: Row(
                      children: [
                        Icon(Icons.edit, size: 20),
                        SizedBox(width: 8),
                        Text('重命名'),
                      ],
                    ),
                  ),
                  const PopupMenuItem(
                    value: 'delete',
                    child: Row(
                      children: [
                        Icon(Icons.delete, size: 20, color: Colors.red),
                        SizedBox(width: 8),
                        Text('删除', style: TextStyle(color: Colors.red)),
                      ],
                    ),
                  ),
                ],
                child: const Icon(Icons.more_vert),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime dateTime) {
    return '${dateTime.year}-${dateTime.month.toString().padLeft(2, '0')}-${dateTime.day.toString().padLeft(2, '0')}';
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;

  const _InfoRow({
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
              label,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
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