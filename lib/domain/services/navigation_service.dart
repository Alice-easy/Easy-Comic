enum SwipeDirection {
  left,
  right,
  up,
  down,
}

abstract class NavigationService {
  /// 导航到指定页面
  Future<void> goToPage(int pageIndex);

  /// 导航到下一页
  Future<bool> goToNextPage();

  /// 导航到上一页
  Future<bool> goToPreviousPage();

  /// 检查是否可以导航到下一页
  bool canGoToNextPage(int currentPage, int totalPages);

  /// 检查是否可以导航到上一页
  bool canGoToPreviousPage(int currentPage);

  /// 处理手势导航
  Future<bool> handleSwipeGesture(SwipeDirection direction, int currentPage, int totalPages);

  /// 处理点击区域导航
  Future<bool> handleTapNavigation(double tapX, double screenWidth, int currentPage, int totalPages);

  /// 跳转到书签页面
  Future<void> jumpToBookmark(int pageIndex);

  /// 获取导航历史
  List<int> getNavigationHistory();

  /// 清空导航历史
  void clearNavigationHistory();

  /// 监听页面变化
  Stream<int> get currentPageStream;

  /// 获取当前页面
  int get currentPage;

  /// 设置总页数
  void setTotalPages(int totalPages);
}