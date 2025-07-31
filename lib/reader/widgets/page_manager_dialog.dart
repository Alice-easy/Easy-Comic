import 'dart:typed_data';
import 'package:flutter/material.dart';
import '../../core/thumbnail_service.dart';
import '../../core/page_order_service.dart';
import '../models/reader_models.dart';

/// Dialog for managing page order with drag-and-drop interface
class PageManagerDialog extends StatefulWidget {
  final List<Uint8List> pages;
  final List<int> currentOrder;
  final ValueChanged<List<int>>? onOrderChanged;
  final VoidCallback? onResetOrder;

  const PageManagerDialog({
    super.key,
    required this.pages,
    required this.currentOrder,
    this.onOrderChanged,
    this.onResetOrder,
  });

  @override
  State<PageManagerDialog> createState() => _PageManagerDialogState();
}

class _PageManagerDialogState extends State<PageManagerDialog> {
  late List<int> _workingOrder;
  bool _hasChanges = false;
  final Map<int, String?> _thumbnailCache = {};
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _workingOrder = List.from(widget.currentOrder.isEmpty 
        ? List.generate(widget.pages.length, (index) => index)
        : widget.currentOrder);
    _preloadThumbnails();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Container(
        width: MediaQuery.of(context).size.width * 0.9,
        height: MediaQuery.of(context).size.height * 0.8,
        child: Column(
          children: [
            _buildHeader(),
            Expanded(child: _buildPageGrid()),
            _buildFooter(),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: Theme.of(context).primaryColor.withOpacity(0.1),
        borderRadius: const BorderRadius.only(
          topLeft: Radius.circular(4.0),
          topRight: Radius.circular(4.0),
        ),
      ),
      child: Row(
        children: [
          const Icon(Icons.reorder),
          const SizedBox(width: 8),
          const Expanded(
            child: Text(
              '页面管理',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          if (_hasChanges)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: Colors.orange,
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text(
                '已修改',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 12,
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildPageGrid() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      child: ReorderableGridView.count(
        controller: _scrollController,
        crossAxisCount: _getCrossAxisCount(),
        mainAxisSpacing: 8.0,
        crossAxisSpacing: 8.0,
        childAspectRatio: 0.75,
        onReorder: _handleReorder,
        children: _workingOrder.asMap().entries.map((entry) {
          final displayIndex = entry.key;
          final originalIndex = entry.value;
          return _buildPageTile(displayIndex, originalIndex);
        }).toList(),
      ),
    );
  }

  Widget _buildPageTile(int displayIndex, int originalIndex) {
    return Card(
      key: ValueKey('page_$originalIndex'),
      elevation: 2,
      child: InkWell(
        onTap: () => _showPagePreview(originalIndex),
        child: Column(
          children: [
            Expanded(
              child: Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  borderRadius: const BorderRadius.vertical(
                    top: Radius.circular(4.0),
                  ),
                  color: Colors.grey[100],
                ),
                child: _buildThumbnail(originalIndex),
              ),
            ),
            Container(
              padding: const EdgeInsets.all(8.0),
              width: double.infinity,
              decoration: BoxDecoration(
                color: displayIndex != originalIndex 
                    ? Colors.orange.withOpacity(0.1)
                    : null,
                borderRadius: const BorderRadius.vertical(
                  bottom: Radius.circular(4.0),
                ),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    '第 ${displayIndex + 1} 页',
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                    textAlign: TextAlign.center,
                  ),
                  if (displayIndex != originalIndex)
                    Text(
                      '(原 ${originalIndex + 1})',
                      style: TextStyle(
                        fontSize: 10,
                        color: Colors.orange[700],
                      ),
                      textAlign: TextAlign.center,
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildThumbnail(int originalIndex) {
    if (_thumbnailCache.containsKey(originalIndex)) {
      final thumbnailPath = _thumbnailCache[originalIndex];
      if (thumbnailPath != null) {
        return ThumbnailImage(
          thumbnailKey: 'page_$originalIndex',
          width: double.infinity,
          height: double.infinity,
          fit: BoxFit.cover,
        );
      }
    }

    // Fallback to original image
    if (originalIndex >= 0 && originalIndex < widget.pages.length) {
      return Image.memory(
        widget.pages[originalIndex],
        width: double.infinity,
        height: double.infinity,
        fit: BoxFit.cover,
        errorBuilder: (context, error, stackTrace) {
          return const Center(
            child: Icon(
              Icons.image_not_supported,
              color: Colors.grey,
              size: 24,
            ),
          );
        },
      );
    }

    return const Center(
      child: CircularProgressIndicator(),
    );
  }

  Widget _buildFooter() {
    return Container(
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: const BorderRadius.only(
          bottomLeft: Radius.circular(4.0),
          bottomRight: Radius.circular(4.0),
        ),
      ),
      child: Row(
        children: [
          TextButton.icon(
            onPressed: _hasChanges ? _resetToOriginal : null,
            icon: const Icon(Icons.restore),
            label: const Text('重置'),
          ),
          TextButton.icon(
            onPressed: widget.onResetOrder,
            icon: const Icon(Icons.sort_by_alpha),
            label: const Text('默认排序'),
          ),
          const Spacer(),
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          const SizedBox(width: 8),
          ElevatedButton(
            onPressed: _hasChanges ? _applyChanges : null,
            child: const Text('应用'),
          ),
        ],
      ),
    );
  }

  int _getCrossAxisCount() {
    final screenWidth = MediaQuery.of(context).size.width;
    if (screenWidth > 800) return 6;
    if (screenWidth > 600) return 4;
    return 3;
  }

  void _handleReorder(int oldIndex, int newIndex) {
    if (oldIndex < newIndex) {
      newIndex -= 1;
    }
    
    setState(() {
      final item = _workingOrder.removeAt(oldIndex);
      _workingOrder.insert(newIndex, item);
      _hasChanges = !_listsEqual(_workingOrder, widget.currentOrder);
    });
  }

  void _resetToOriginal() {
    setState(() {
      _workingOrder = List.from(widget.currentOrder.isEmpty 
          ? List.generate(widget.pages.length, (index) => index)
          : widget.currentOrder);
      _hasChanges = false;
    });
  }

  void _applyChanges() {
    if (_hasChanges && widget.onOrderChanged != null) {
      widget.onOrderChanged!(_workingOrder);
    }
    Navigator.of(context).pop();
  }

  void _showPagePreview(int originalIndex) {
    showDialog(
      context: context,
      builder: (context) => Dialog(
        child: Container(
          constraints: BoxConstraints(
            maxWidth: MediaQuery.of(context).size.width * 0.8,
            maxHeight: MediaQuery.of(context).size.height * 0.8,
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  '第 ${originalIndex + 1} 页预览',
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              Flexible(
                child: InteractiveViewer(
                  child: Image.memory(
                    widget.pages[originalIndex],
                    fit: BoxFit.contain,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.all(16.0),
                child: TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('关闭'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _preloadThumbnails() {
    for (int i = 0; i < widget.pages.length; i++) {
      final thumbnailKey = 'page_$i';
      ThumbnailService.getCachedThumbnail(thumbnailKey).then((cachedPath) {
        if (cachedPath != null) {
          setState(() {
            _thumbnailCache[i] = cachedPath;
          });
        } else {
          // Generate thumbnail in background
          ThumbnailService.generateThumbnail(
            widget.pages[i],
            thumbnailKey,
          ).then((generatedPath) {
            if (mounted) {
              setState(() {
                _thumbnailCache[i] = generatedPath;
              });
            }
          });
        }
      });
    }
  }

  bool _listsEqual<T>(List<T> a, List<T> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }
}

/// Reorderable grid view widget
class ReorderableGridView extends StatefulWidget {
  final int crossAxisCount;
  final double mainAxisSpacing;
  final double crossAxisSpacing;
  final double childAspectRatio;
  final List<Widget> children;
  final ReorderCallback onReorder;
  final ScrollController? controller;

  const ReorderableGridView({
    super.key,
    required this.crossAxisCount,
    this.mainAxisSpacing = 0.0,
    this.crossAxisSpacing = 0.0,
    this.childAspectRatio = 1.0,
    required this.children,
    required this.onReorder,
    this.controller,
  });

  factory ReorderableGridView.count({
    Key? key,
    required int crossAxisCount,
    double mainAxisSpacing = 0.0,
    double crossAxisSpacing = 0.0,
    double childAspectRatio = 1.0,
    required List<Widget> children,
    required ReorderCallback onReorder,
    ScrollController? controller,
  }) {
    return ReorderableGridView(
      key: key,
      crossAxisCount: crossAxisCount,
      mainAxisSpacing: mainAxisSpacing,
      crossAxisSpacing: crossAxisSpacing,
      childAspectRatio: childAspectRatio,
      children: children,
      onReorder: onReorder,
      controller: controller,
    );
  }

  @override
  State<ReorderableGridView> createState() => _ReorderableGridViewState();
}

class _ReorderableGridViewState extends State<ReorderableGridView> {
  @override
  Widget build(BuildContext context) {
    return ReorderableListView(
      scrollController: widget.controller,
      onReorder: widget.onReorder,
      children: widget.children,
      // Convert to grid-like layout using custom list items
      buildDefaultDragHandles: false,
    );
  }
}