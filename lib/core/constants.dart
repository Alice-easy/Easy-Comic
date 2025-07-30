/// 应用常量定义
class AppConstants {
  // 私有构造函数，防止实例化
  AppConstants._();

  // 应用信息
  static const String appName = 'Easy Comic';
  static const String appVersion = '1.0.0';
  static const String appLegalese = '© 2024 The Easy Comic Authors';

  // 用户信息
  static const String defaultUserId = 'user-12345';

  // 同步相关
  static const String syncProgressPath = '/comic_progress/';
  static const String jsonExtension = '.json';

  // Widget 相关
  static const String comicsWidgetProvider = 'ComicsWidgetProvider';

  // 时间间隔
  static const Duration progressUpdateInterval = Duration(seconds: 2);

  // 文件扩展名
  static const List<String> supportedComicExtensions = ['cbz', 'zip'];

  // UI 常量
  static const double cardElevation = 4.0;
  static const double iconSizeLarge = 80.0;
  static const double iconSizeSmall = 16.0;
  static const double iconSizeMedium = 20.0;
  static const int progressIndicatorStrokeWidth = 2;

  // 网格布局
  static const int gridCrossAxisCount = 2;
  static const double gridChildAspectRatio = 0.7;
  static const double gridCrossAxisSpacing = 16.0;
  static const double gridMainAxisSpacing = 16.0;

  // 边距和间距
  static const double paddingSmall = 8.0;
  static const double paddingMedium = 16.0;
  static const double paddingLarge = 24.0;

  // 字体大小
  static const double fontSizeSmall = 12.0;
  static const double fontSizeMedium = 14.0;
  static const double fontSizeLarge = 18.0;

  // PhotoView 缩放
  static const double photoViewMinScale = 0.5;
  static const double photoViewMaxScale = 3.0;

  // 双页模式阈值
  static const double dualPageModeWidth = 600.0;

  // Firebase Analytics 事件
  static const String eventReadStart = 'read_start';
  static const String eventPageFlipped = 'page_flipped';

  // Firebase Crashlytics 自定义键
  static const String crashlyticsKeyFileName = 'fileName';

  // 错误信息
  static const String errorComicNotFound = 'Comic not found';
  static const String errorNoPages = '没有找到漫画页面';
  static const String errorLoadingFailed = '加载失败';
  static const String errorSyncFailed = '同步失败';
  static const String errorNetworkConnection = '网络连接错误，请检查网络设置';

  // 用户界面文本
  static const String textAddComic = '添加漫画';
  static const String textSettings = '设置';
  static const String textAbout = '关于';
  static const String textCancel = '取消';
  static const String textConfirm = '确认';
  static const String textDelete = '删除';
  static const String textRetry = '重试';
  static const String textClose = '关闭';
  static const String textAdd = '添加';
  static const String textLoading = '正在加载...';
  static const String textNoComics = '暂无漫画';
  static const String textAddComicHint = '点击右上角的 + 按钮添加漫画文件';
  static const String textSortByName = '按名称';
  static const String textSortByDate = '按最近阅读';
  static const String textSortByProgress = '按进度';
  static const String textShowFavoritesOnly = '仅显示收藏';
  static const String textSyncData = '同步数据';
  static const String textComicReader = '漫画阅读器';
  static const String textAddBookmark = '添加书签';
  static const String textBookmarkList = '书签列表';
  static const String textComicInfo = '漫画信息';
  static const String textBookmarkDescription = '书签描述 (可选)';

  // Toast 消息
  static const String toastSyncCompleted = '同步完成';
  static const String toastSyncFailed = '同步失败';

  // 排序提示
  static const String tooltipSort = '排序方式';
}