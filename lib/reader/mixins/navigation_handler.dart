import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:firebase_analytics/firebase_analytics.dart';

import '../../models/reader_models.dart' as models;

/// Mixin that provides navigation handling logic for comic readers
/// 
/// This mixin provides:
/// - Page change handling and state updates
/// - Navigation direction utilities
/// - Keyboard and volume key navigation support
/// - Progress tracking integration
/// - Analytics event logging
mixin NavigationHandler<T extends ConsumerStatefulWidget> 
    on ConsumerState<T> {

  /// Current page index
  int get currentPage;
  
  /// Total number of pages
  int get totalPages;
  
  /// Page controller for navigation
  PageController get pageController;
  
  /// Update current page state (should be implemented by mixing class)
  void updateCurrentPage(int page);
  
  /// Update progress (should be implemented by mixing class)
  void updateProgress();

  /// Handle page change events
  /// 
  /// Updates current page state, triggers progress update, 
  /// and logs Firebase Analytics events
  void onPageChanged(int index) {
    // Ensure the index is within valid bounds
    if (index < 0 || index >= totalPages) return;
    
    updateCurrentPage(index);
    updateProgress();
    
    // Log analytics event
    FirebaseAnalytics.instance.logEvent(
      name: 'page_flipped',
      parameters: {
        'page': index,
        'total_pages': totalPages,
        'progress_percent': ((index + 1) / totalPages * 100).round(),
      },
    );
  }

  /// Navigate to specific page
  void goToPage(int page, {bool animated = true}) {
    if (page < 0 || page >= totalPages) return;
    
    if (animated) {
      pageController.animateToPage(
        page,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      pageController.jumpToPage(page);
    }
  }

  /// Navigate to next page
  void goToNextPage({bool animated = true}) {
    if (currentPage < totalPages - 1) {
      goToPage(currentPage + 1, animated: animated);
    }
  }

  /// Navigate to previous page
  void goToPreviousPage({bool animated = true}) {
    if (currentPage > 0) {
      goToPage(currentPage - 1, animated: animated);
    }
  }

  /// Jump to first page
  void goToFirstPage({bool animated = true}) {
    goToPage(0, animated: animated);
  }

  /// Jump to last page
  void goToLastPage({bool animated = true}) {
    goToPage(totalPages - 1, animated: animated);
  }

  /// Calculate progress percentage
  double get progressPercentage {
    if (totalPages <= 0) return 0.0;
    return (currentPage + 1) / totalPages;
  }

  /// Check if we can go to next page
  bool get canGoNext => currentPage < totalPages - 1;

  /// Check if we can go to previous page
  bool get canGoPrevious => currentPage > 0;

  /// Get icon for navigation direction
  IconData getDirectionIcon(models.NavigationDirection direction) {
    switch (direction) {
      case models.NavigationDirection.horizontal:
        return Icons.arrow_forward;
      case models.NavigationDirection.rtl:
        return Icons.arrow_back;
      case models.NavigationDirection.vertical:
        return Icons.arrow_downward;
    }
  }

  /// Get navigation direction name for display
  String getDirectionName(models.NavigationDirection direction) {
    switch (direction) {
      case models.NavigationDirection.horizontal:
        return 'Left to Right';
      case models.NavigationDirection.rtl:
        return 'Right to Left';
      case models.NavigationDirection.vertical:
        return 'Top to Bottom';
    }
  }

  /// Handle keyboard navigation (for desktop/web)
  bool handleKeyNavigation(KeyEvent event) {
    if (event is KeyDownEvent) {
      switch (event.logicalKey.keyLabel) {
        case 'Arrow Right':
        case 'Space':
          goToNextPage();
          return true;
        case 'Arrow Left':
        case 'Backspace':
          goToPreviousPage();
          return true;
        case 'Home':
          goToFirstPage();
          return true;
        case 'End':
          goToLastPage();
          return true;
      }
    }
    return false;
  }

  /// Handle volume key navigation (Android)
  bool handleVolumeKeyNavigation(String key) {
    switch (key) {
      case 'volumeUp':
        goToPreviousPage();
        return true;
      case 'volumeDown':
        goToNextPage();
        return true;
    }
    return false;
  }

  /// Handle tap navigation zones
  bool handleTapNavigation(
    Offset position, 
    Size screenSize, 
    models.NavigationDirection direction
  ) {
    // Define navigation zones based on direction
    switch (direction) {
      case models.NavigationDirection.horizontal:
        // Left third = previous, right third = next, middle third = toggle UI
        final threshold = screenSize.width / 3;
        if (position.dx < threshold) {
          goToPreviousPage();
          return true;
        } else if (position.dx > screenSize.width - threshold) {
          goToNextPage();
          return true;
        }
        break;
        
      case models.NavigationDirection.rtl:
        // Right third = previous, left third = next (RTL reversed)
        final threshold = screenSize.width / 3;
        if (position.dx > screenSize.width - threshold) {
          goToPreviousPage();
          return true;
        } else if (position.dx < threshold) {
          goToNextPage();
          return true;
        }
        break;
        
      case models.NavigationDirection.vertical:
        // Top third = previous, bottom third = next
        final threshold = screenSize.height / 3;
        if (position.dy < threshold) {
          goToPreviousPage();
          return true;
        } else if (position.dy > screenSize.height - threshold) {
          goToNextPage();
          return true;
        }
        break;
    }
    
    return false; // Let UI toggle handle middle zone
  }

  /// Get navigation summary for analytics
  Map<String, dynamic> getNavigationAnalytics() {
    return {
      'current_page': currentPage,
      'total_pages': totalPages,
      'progress_percent': (progressPercentage * 100).round(),
      'pages_remaining': totalPages - currentPage - 1,
    };
  }
}