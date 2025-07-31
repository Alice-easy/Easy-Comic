import 'dart:typed_data';
import '../data/drift_db.dart';
import '../reader/models/reader_models.dart';

/// Service for managing custom page ordering
/// Provides drag-and-drop reordering with database persistence
class PageOrderService {
  final DriftDb _db;

  PageOrderService(this._db);

  /// Get custom page order for a comic
  /// Returns empty list if no custom order exists
  Future<List<int>> getCustomOrder(int comicId) async {
    try {
      final customOrder = await _db.getCustomPageOrder(comicId);
      if (customOrder.isEmpty) return [];
      
      // Sort by custom index and return original indices
      customOrder.sort((a, b) => a.customIndex.compareTo(b.customIndex));
      return customOrder.map((order) => order.originalIndex).toList();
    } catch (e) {
      return [];
    }
  }

  /// Check if comic has custom page order
  Future<bool> hasCustomOrder(int comicId) async {
    try {
      final customOrder = await _db.getCustomPageOrder(comicId);
      return customOrder.isNotEmpty;
    } catch (e) {
      return false;
    }
  }

  /// Set custom page order for a comic
  Future<void> setCustomOrder(int comicId, List<int> newOrder) async {
    try {
      // Validate the new order
      if (!_isValidOrder(newOrder)) {
        throw ArgumentError('Invalid page order: contains duplicates or invalid indices');
      }
      
      await _db.setCustomPageOrder(comicId, newOrder);
    } catch (e) {
      throw Exception('Failed to set custom page order: $e');
    }
  }

  /// Clear custom page order for a comic (revert to default)
  Future<void> clearCustomOrder(int comicId) async {
    try {
      await _db.clearCustomPageOrder(comicId);
    } catch (e) {
      throw Exception('Failed to clear custom page order: $e');
    }
  }

  /// Apply custom page order to a list of pages
  List<T> applyCustomOrder<T>(List<T> pages, List<int> customOrder) {
    if (customOrder.isEmpty || customOrder.length != pages.length) {
      return pages;
    }
    
    // Validate that custom order is valid for the page count
    if (!_isValidOrderForPageCount(customOrder, pages.length)) {
      return pages;
    }
    
    final reorderedPages = <T>[];
    for (final originalIndex in customOrder) {
      if (originalIndex >= 0 && originalIndex < pages.length) {
        reorderedPages.add(pages[originalIndex]);
      }
    }
    
    return reorderedPages.length == pages.length ? reorderedPages : pages;
  }

  /// Get default page order (sequential indices)
  List<int> getDefaultOrder(int pageCount) {
    return List.generate(pageCount, (index) => index);
  }

  /// Reorder pages by moving an item from one position to another
  List<int> reorderPages(List<int> currentOrder, int oldIndex, int newIndex) {
    if (oldIndex < 0 || oldIndex >= currentOrder.length || 
        newIndex < 0 || newIndex >= currentOrder.length) {
      return currentOrder;
    }
    
    final newOrder = List<int>.from(currentOrder);
    final item = newOrder.removeAt(oldIndex);
    newOrder.insert(newIndex, item);
    
    return newOrder;
  }

  /// Validate that all pages are present in the custom order
  bool _isValidOrder(List<int> order) {
    if (order.isEmpty) return true;
    
    // Check for duplicates
    final uniqueIndices = Set<int>.from(order);
    if (uniqueIndices.length != order.length) return false;
    
    // Check that all indices are valid (non-negative)
    return order.every((index) => index >= 0);
  }

  /// Validate that custom order is valid for the given page count
  bool _isValidOrderForPageCount(List<int> order, int pageCount) {
    if (order.length != pageCount) return false;
    
    // Check that all indices are within range
    return order.every((index) => index >= 0 && index < pageCount);
  }

  /// Generate page order entries for UI display
  List<PageOrderEntry> generateOrderEntries(
    List<int> customOrder,
    Map<int, String> thumbnailCache,
  ) {
    final entries = <PageOrderEntry>[];
    
    for (int i = 0; i < customOrder.length; i++) {
      final originalIndex = customOrder[i];
      entries.add(PageOrderEntry(
        originalIndex: originalIndex,
        customIndex: i,
        thumbnailKey: thumbnailCache[originalIndex] ?? 'page_$originalIndex',
      ));
    }
    
    return entries;
  }

  /// Batch update custom orders for multiple comics
  Future<void> batchUpdateCustomOrders(
    Map<int, List<int>> comicOrders,
  ) async {
    try {
      for (final entry in comicOrders.entries) {
        await setCustomOrder(entry.key, entry.value);
      }
    } catch (e) {
      throw Exception('Failed to batch update custom orders: $e');
    }
  }

  /// Get statistics about custom page orders
  Future<PageOrderStats> getOrderStats() async {
    try {
      // This would require additional database queries
      // For now, return basic stats
      return PageOrderStats(
        totalComicsWithCustomOrder: 0,
        totalCustomOrderEntries: 0,
        averageOrderChanges: 0.0,
      );
    } catch (e) {
      return PageOrderStats(
        totalComicsWithCustomOrder: 0,
        totalCustomOrderEntries: 0,
        averageOrderChanges: 0.0,
      );
    }
  }

  /// Suggest optimal page order based on image dimensions or content
  Future<List<int>> suggestOptimalOrder(
    List<Uint8List> pages, {
    PageOrderStrategy strategy = PageOrderStrategy.preserveOriginal,
  }) async {
    switch (strategy) {
      case PageOrderStrategy.preserveOriginal:
        return getDefaultOrder(pages.length);
      
      case PageOrderStrategy.sortBySize:
        // Sort by image size (largest first)
        final sizeMap = <int, int>{};
        for (int i = 0; i < pages.length; i++) {
          sizeMap[i] = pages[i].length;
        }
        
        final sortedIndices = sizeMap.entries
            .toList()
            ..sort((a, b) => b.value.compareTo(a.value));
        
        return sortedIndices.map((e) => e.key).toList();
      
      case PageOrderStrategy.reverseOrder:
        return getDefaultOrder(pages.length).reversed.toList();
    }
  }
}

/// Statistics about page ordering
class PageOrderStats {
  final int totalComicsWithCustomOrder;
  final int totalCustomOrderEntries;
  final double averageOrderChanges;

  const PageOrderStats({
    required this.totalComicsWithCustomOrder,
    required this.totalCustomOrderEntries,
    required this.averageOrderChanges,
  });
}

/// Strategies for suggesting page order
enum PageOrderStrategy {
  preserveOriginal,
  sortBySize,
  reverseOrder,
}