import 'package:flutter/material.dart';
import '../../domain/entities/comic_page.dart';
import '../../domain/entities/reader_settings.dart';
import '../../domain/services/navigation_service.dart';

/// Navigation direction for reading mode rendering
enum NavigationDirection {
  forward,
  backward,
}

/// Abstract base class for different reading modes with consistent interface
abstract class ReadingModeRenderer {
  final ReaderSettings settings;
  final NavigationService navigationService;
  
  ReadingModeRenderer({
    required this.settings,
    required this.navigationService,
  });
  
  // Abstract methods that must be implemented by concrete renderers
  /// Builds the view for the specific reading mode
  Widget buildView(List<ComicPage> pages, int currentPageIndex);
  
  /// Handles page navigation in the context of the reading mode
  Future<void> handlePageNavigation(NavigationDirection direction);
  
  /// Preserves reading position for the current reading mode
  Future<void> preserveReadingPosition(int currentPage, double scrollOffset);
  
  // Shared functionality available to all renderers
  /// Called when page changes - updates navigation service
  void onPageChanged(int pageIndex) {
    navigationService.updateCurrentPage(pageIndex);
  }
  
  /// Determines if should navigate forward based on reading mode and direction
  bool shouldNavigateForward(NavigationDirection direction) {
    final isRTL = settings.readingMode == ReadingMode.rightToLeft;
    return (direction == NavigationDirection.forward && !isRTL) ||
           (direction == NavigationDirection.backward && isRTL);
  }
}