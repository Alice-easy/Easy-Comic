import 'dart:async';
import '../../domain/services/navigation_service.dart';

class NavigationServiceImpl implements NavigationService {
  Timer? _autoPageTimer;
  StreamController<int>? _pageChangeController;
  
  @override
  Future<int> getNextPage(int currentPage, int totalPages, NavigationDirection direction) async {
    switch (direction) {
      case NavigationDirection.next:
        return currentPage < totalPages - 1 ? currentPage + 1 : currentPage;
      case NavigationDirection.previous:
        return currentPage > 0 ? currentPage - 1 : currentPage;
      case NavigationDirection.first:
        return 0;
      case NavigationDirection.last:
        return totalPages - 1;
    }
  }

  @override
  Future<bool> canNavigate(int currentPage, int totalPages, NavigationDirection direction) async {
    switch (direction) {
      case NavigationDirection.next:
        return currentPage < totalPages - 1;
      case NavigationDirection.previous:
        return currentPage > 0;
      case NavigationDirection.first:
        return currentPage > 0;
      case NavigationDirection.last:
        return currentPage < totalPages - 1;
    }
  }

  @override
  Future<int> getPageFromGesture(GestureType gesture, int currentPage, int totalPages) async {
    switch (gesture) {
      case GestureType.swipeLeft:
        return await getNextPage(currentPage, totalPages, NavigationDirection.next);
      case GestureType.swipeRight:
        return await getNextPage(currentPage, totalPages, NavigationDirection.previous);
      case GestureType.tapLeft:
        return await getNextPage(currentPage, totalPages, NavigationDirection.previous);
      case GestureType.tapRight:
        return await getNextPage(currentPage, totalPages, NavigationDirection.next);
      case GestureType.tapCenter:
        // Tap center usually toggles UI visibility, return current page
        return currentPage;
      case GestureType.doubleTap:
        // Double tap usually handles zoom, return current page
        return currentPage;
    }
  }

  @override
  Stream<int> startAutoNavigation(int startPage, int totalPages, Duration interval) {
    _pageChangeController?.close();
    _pageChangeController = StreamController<int>.broadcast();
    
    int currentPage = startPage;
    
    _autoPageTimer = Timer.periodic(interval, (timer) {
      if (currentPage < totalPages - 1) {
        currentPage++;
        _pageChangeController?.add(currentPage);
      } else {
        // Stop auto navigation when reaching the end
        stopAutoNavigation();
      }
    });
    
    return _pageChangeController!.stream;
  }

  @override
  Future<void> stopAutoNavigation() async {
    _autoPageTimer?.cancel();
    _autoPageTimer = null;
    await _pageChangeController?.close();
    _pageChangeController = null;
  }

  @override
  Future<bool> isAutoNavigating() async {
    return _autoPageTimer != null && _autoPageTimer!.isActive;
  }

  @override
  Future<int> jumpToPage(int targetPage, int totalPages) async {
    if (targetPage < 0) return 0;
    if (targetPage >= totalPages) return totalPages - 1;
    return targetPage;
  }

  @override
  Future<List<int>> getPreloadPages(int currentPage, int totalPages, {int ahead = 3, int behind = 1}) async {
    final List<int> preloadPages = [];
    
    // Add pages behind current page
    for (int i = 1; i <= behind; i++) {
      final page = currentPage - i;
      if (page >= 0) {
        preloadPages.add(page);
      }
    }
    
    // Add pages ahead of current page
    for (int i = 1; i <= ahead; i++) {
      final page = currentPage + i;
      if (page < totalPages) {
        preloadPages.add(page);
      }
    }
    
    return preloadPages;
  }

  @override
  void dispose() {
    stopAutoNavigation();
  }
}