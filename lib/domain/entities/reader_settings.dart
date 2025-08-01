import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

// --- Enums ---

enum AppTheme { Light, Dark, System }

enum ReadingDirection { LTR, RTL, Vertical }

enum ReadingMode { SinglePage, DoublePage, Webtoon }

enum PageTransition { Slide, Fade, Scale, None }

enum ScreenOrientation { Portrait, Landscape, Auto }

enum BackgroundColor { White, Black, Sepia, Gray, Custom }

enum BookshelfLayout { Grid2, Grid3, Grid4, Grid5, List }

enum ComicCardStyle { Standard, Compact, Detailed }

enum FontSize { Small, Medium, Large, ExtraLarge }

enum StorageLocation { Internal, External, Custom }

enum CacheStrategy { Conservative, Balanced, Aggressive, Manual }

enum FileFormat { CBZ, CBR, ZIP, RAR, PDF, EPUB }

enum ImageQuality { Low, Medium, High, Original }

enum SyncFrequency { Manual, Hourly, Daily, Weekly }

enum SyncScope { ReadingProgress, Bookmarks, Settings, All }

enum ConflictResolution { KeepLocal, KeepRemote, Prompt }

enum BackupFormat { JSON, XML, CSV }

enum LogLevel { None, Error, Warning, Info, Debug, Verbose }

enum DuplicateFileStrategy { Skip, Replace, Rename, Prompt }

// --- Entity ---

class ReaderSettings extends Equatable {
  // 基础设置
  final AppTheme appTheme;
  final ReadingDirection readingDirection;
  final ReadingMode readingMode;
  final int autoPageInterval;
  final WebDAVConfig? webDavConfig;

  // 阅读偏好设置
  final PageTransition pageTransition;
  final bool autoPageEnabled;
  final double tapAreaSensitivity;

  // 显示设置
  final double defaultZoomLevel;
  final ScreenOrientation screenOrientation;
  final bool hideStatusBar;
  final bool hideNavigationBar;
  final bool keepScreenAwake;
  final bool fullScreenMode;

  // 视觉效果
  final BackgroundColor backgroundColor;
  final Color? customBackgroundColor;
  final double imageBrightness;
  final bool nightModeOptimization;
  final double contrast;
  final double saturation;

  // 界面布局
  final BookshelfLayout bookshelfLayout;
  final ComicCardStyle comicCardStyle;
  final BookshelfLayout favoritesLayout;
  final FontSize fontSize;
  final bool showComicProgress;
  final bool showLastReadTime;

  // 存储配置
  final StorageLocation defaultStorageLocation;
  final String? customStoragePath;
  final int cacheMaxSize; // MB
  final CacheStrategy cacheStrategy;
  final bool autoImportDetection;
  final int autoCleanupDays;

  // 文件处理
  final Set<FileFormat> supportedFormats;
  final ImageQuality imageQuality;
  final ImageQuality coverQuality;
  final DuplicateFileStrategy duplicateStrategy;
  final bool enableImageCompression;
  final int maxImageSize; // pixels

  // 同步配置
  final bool autoSyncEnabled;
  final SyncFrequency syncFrequency;
  final SyncScope syncScope;
  final ConflictResolution conflictResolution;
  final bool syncOnWifiOnly;

  // 备份恢复
  final bool autoBackupEnabled;
  final int backupRetentionDays;
  final BackupFormat backupFormat;
  final bool selectiveRestore;

  // 应用信息
  final bool showDeveloperInfo;
  final bool enableUpdateCheck;
  final bool showUsageHelp;
  final bool acceptPrivacyPolicy;

  // 调试和诊断
  final LogLevel logLevel;
  final bool performanceMonitoring;
  final bool showCacheStatus;
  final bool enableCrashReporting;

  // 额外的UI相关属性
  final bool enableFullscreen;
  final double brightness;
  final String defaultDownloadPath;
  final String cacheDirectory;
  final int imageCacheSize; // MB
  final int diskCacheSize; // MB
  final bool autoCleanTempFiles;
  final bool clearCacheOnExit;
  final bool smartMemoryManagement;

  // WebDAV 连接设置
  final String webdavUrl;
  final String webdavUsername; 
  final String webdavPassword;
  final bool autoSync;
  final bool syncBookmarks;
  final bool syncSettings;
  final bool syncReadingHistory;
  final bool autoBackup;
  final int backupInterval; // hours

  // 调试选项
  final bool debugMode;
  final bool verboseLogging;

  const ReaderSettings({
    // 基础设置
    this.appTheme = AppTheme.System,
    this.readingDirection = ReadingDirection.LTR,
    this.readingMode = ReadingMode.SinglePage,
    this.autoPageInterval = 5,
    this.webDavConfig,

    // 阅读偏好设置
    this.pageTransition = PageTransition.Slide,
    this.autoPageEnabled = false,
    this.tapAreaSensitivity = 0.25,

    // 显示设置
    this.defaultZoomLevel = 1.0,
    this.screenOrientation = ScreenOrientation.Auto,
    this.hideStatusBar = true,
    this.hideNavigationBar = true,
    this.keepScreenAwake = true,
    this.fullScreenMode = true,

    // 视觉效果
    this.backgroundColor = BackgroundColor.Black,
    this.customBackgroundColor,
    this.imageBrightness = 1.0,
    this.nightModeOptimization = true,
    this.contrast = 1.0,
    this.saturation = 1.0,

    // 界面布局
    this.bookshelfLayout = BookshelfLayout.Grid3,
    this.comicCardStyle = ComicCardStyle.Standard,
    this.favoritesLayout = BookshelfLayout.Grid3,
    this.fontSize = FontSize.Medium,
    this.showComicProgress = true,
    this.showLastReadTime = true,

    // 存储配置
    this.defaultStorageLocation = StorageLocation.Internal,
    this.customStoragePath,
    this.cacheMaxSize = 500, // 500MB
    this.cacheStrategy = CacheStrategy.Balanced,
    this.autoImportDetection = true,
    this.autoCleanupDays = 30,

    // 文件处理
    this.supportedFormats = const {
      FileFormat.CBZ,
      FileFormat.CBR,
      FileFormat.ZIP,
      FileFormat.RAR
    },
    this.imageQuality = ImageQuality.High,
    this.coverQuality = ImageQuality.Medium,
    this.duplicateStrategy = DuplicateFileStrategy.Prompt,
    this.enableImageCompression = false,
    this.maxImageSize = 2048,

    // 同步配置
    this.autoSyncEnabled = false,
    this.syncFrequency = SyncFrequency.Daily,
    this.syncScope = SyncScope.All,
    this.conflictResolution = ConflictResolution.Prompt,
    this.syncOnWifiOnly = true,

    // 备份恢复
    this.autoBackupEnabled = false,
    this.backupRetentionDays = 30,
    this.backupFormat = BackupFormat.JSON,
    this.selectiveRestore = true,

    // 应用信息
    this.showDeveloperInfo = false,
    this.enableUpdateCheck = true,
    this.showUsageHelp = true,
    this.acceptPrivacyPolicy = false,

    // 调试和诊断
    this.logLevel = LogLevel.Warning,
    this.performanceMonitoring = false,
    this.showCacheStatus = false,
    this.enableCrashReporting = true,

    // 额外的UI相关属性
    this.enableFullscreen = true,
    this.brightness = 1.0,
    this.defaultDownloadPath = '/storage/emulated/0/Download',
    this.cacheDirectory = '/storage/emulated/0/Android/data/com.example.easy_comic/cache',
    this.imageCacheSize = 100, // 100MB
    this.diskCacheSize = 500, // 500MB
    this.autoCleanTempFiles = true,
    this.clearCacheOnExit = false,
    this.smartMemoryManagement = true,

    // WebDAV 连接设置
    this.webdavUrl = '',
    this.webdavUsername = '',
    this.webdavPassword = '',
    this.autoSync = false,
    this.syncBookmarks = true,
    this.syncSettings = true,
    this.syncReadingHistory = true,
    this.autoBackup = false,
    this.backupInterval = 24, // 24 hours

    // 调试选项
    this.debugMode = false,
    this.verboseLogging = false,
  });

  @override
  List<Object?> get props => [
        // 基础设置
        appTheme,
        readingDirection,
        readingMode,
        autoPageInterval,
        webDavConfig,

        // 阅读偏好设置
        pageTransition,
        autoPageEnabled,
        tapAreaSensitivity,

        // 显示设置
        defaultZoomLevel,
        screenOrientation,
        hideStatusBar,
        hideNavigationBar,
        keepScreenAwake,
        fullScreenMode,

        // 视觉效果
        backgroundColor,
        customBackgroundColor,
        imageBrightness,
        nightModeOptimization,
        contrast,
        saturation,

        // 界面布局
        bookshelfLayout,
        comicCardStyle,
        favoritesLayout,
        fontSize,
        showComicProgress,
        showLastReadTime,

        // 存储配置
        defaultStorageLocation,
        customStoragePath,
        cacheMaxSize,
        cacheStrategy,
        autoImportDetection,
        autoCleanupDays,

        // 文件处理
        supportedFormats,
        imageQuality,
        coverQuality,
        duplicateStrategy,
        enableImageCompression,
        maxImageSize,

        // 同步配置
        autoSyncEnabled,
        syncFrequency,
        syncScope,
        conflictResolution,
        syncOnWifiOnly,

        // 备份恢复
        autoBackupEnabled,
        backupRetentionDays,
        backupFormat,
        selectiveRestore,

        // 应用信息
        showDeveloperInfo,
        enableUpdateCheck,
        showUsageHelp,
        acceptPrivacyPolicy,

        // 调试和诊断
        logLevel,
        performanceMonitoring,
        showCacheStatus,
        enableCrashReporting,

        // 额外的UI相关属性
        enableFullscreen,
        brightness,
        defaultDownloadPath,
        cacheDirectory,
        imageCacheSize,
        diskCacheSize,
        autoCleanTempFiles,
        clearCacheOnExit,
        smartMemoryManagement,

        // WebDAV 连接设置
        webdavUrl,
        webdavUsername,
        webdavPassword,
        autoSync,
        syncBookmarks,
        syncSettings,
        syncReadingHistory,
        autoBackup,
        backupInterval,

        // 调试选项
        debugMode,
        verboseLogging,
      ];

  ReaderSettings copyWith({
    // 基础设置
    AppTheme? appTheme,
    ReadingDirection? readingDirection,
    ReadingMode? readingMode,
    int? autoPageInterval,
    WebDAVConfig? webDavConfig,

    // 阅读偏好设置
    PageTransition? pageTransition,
    bool? autoPageEnabled,
    double? tapAreaSensitivity,

    // 显示设置
    double? defaultZoomLevel,
    ScreenOrientation? screenOrientation,
    bool? hideStatusBar,
    bool? hideNavigationBar,
    bool? keepScreenAwake,
    bool? fullScreenMode,

    // 视觉效果
    BackgroundColor? backgroundColor,
    Color? customBackgroundColor,
    double? imageBrightness,
    bool? nightModeOptimization,
    double? contrast,
    double? saturation,

    // 界面布局
    BookshelfLayout? bookshelfLayout,
    ComicCardStyle? comicCardStyle,
    BookshelfLayout? favoritesLayout,
    FontSize? fontSize,
    bool? showComicProgress,
    bool? showLastReadTime,

    // 存储配置
    StorageLocation? defaultStorageLocation,
    String? customStoragePath,
    int? cacheMaxSize,
    CacheStrategy? cacheStrategy,
    bool? autoImportDetection,
    int? autoCleanupDays,

    // 文件处理
    Set<FileFormat>? supportedFormats,
    ImageQuality? imageQuality,
    ImageQuality? coverQuality,
    DuplicateFileStrategy? duplicateStrategy,
    bool? enableImageCompression,
    int? maxImageSize,

    // 同步配置
    bool? autoSyncEnabled,
    SyncFrequency? syncFrequency,
    SyncScope? syncScope,
    ConflictResolution? conflictResolution,
    bool? syncOnWifiOnly,

    // 备份恢复
    bool? autoBackupEnabled,
    int? backupRetentionDays,
    BackupFormat? backupFormat,
    bool? selectiveRestore,

    // 应用信息
    bool? showDeveloperInfo,
    bool? enableUpdateCheck,
    bool? showUsageHelp,
    bool? acceptPrivacyPolicy,

    // 调试和诊断
    LogLevel? logLevel,
    bool? performanceMonitoring,
    bool? showCacheStatus,
    bool? enableCrashReporting,

    // 额外的UI相关属性
    bool? enableFullscreen,
    double? brightness,
    String? defaultDownloadPath,
    String? cacheDirectory,
    int? imageCacheSize,
    int? diskCacheSize,
    bool? autoCleanTempFiles,
    bool? clearCacheOnExit,
    bool? smartMemoryManagement,

    // WebDAV 连接设置
    String? webdavUrl,
    String? webdavUsername,
    String? webdavPassword,
    bool? autoSync,
    bool? syncBookmarks,
    bool? syncSettings,
    bool? syncReadingHistory,
    bool? autoBackup,
    int? backupInterval,

    // 调试选项
    bool? debugMode,
    bool? verboseLogging,
  }) {
    return ReaderSettings(
      // 基础设置
      appTheme: appTheme ?? this.appTheme,
      readingDirection: readingDirection ?? this.readingDirection,
      readingMode: readingMode ?? this.readingMode,
      autoPageInterval: autoPageInterval ?? this.autoPageInterval,
      webDavConfig: webDavConfig ?? this.webDavConfig,

      // 阅读偏好设置
      pageTransition: pageTransition ?? this.pageTransition,
      autoPageEnabled: autoPageEnabled ?? this.autoPageEnabled,
      tapAreaSensitivity: tapAreaSensitivity ?? this.tapAreaSensitivity,

      // 显示设置
      defaultZoomLevel: defaultZoomLevel ?? this.defaultZoomLevel,
      screenOrientation: screenOrientation ?? this.screenOrientation,
      hideStatusBar: hideStatusBar ?? this.hideStatusBar,
      hideNavigationBar: hideNavigationBar ?? this.hideNavigationBar,
      keepScreenAwake: keepScreenAwake ?? this.keepScreenAwake,
      fullScreenMode: fullScreenMode ?? this.fullScreenMode,

      // 视觉效果
      backgroundColor: backgroundColor ?? this.backgroundColor,
      customBackgroundColor: customBackgroundColor ?? this.customBackgroundColor,
      imageBrightness: imageBrightness ?? this.imageBrightness,
      nightModeOptimization: nightModeOptimization ?? this.nightModeOptimization,
      contrast: contrast ?? this.contrast,
      saturation: saturation ?? this.saturation,

      // 界面布局
      bookshelfLayout: bookshelfLayout ?? this.bookshelfLayout,
      comicCardStyle: comicCardStyle ?? this.comicCardStyle,
      favoritesLayout: favoritesLayout ?? this.favoritesLayout,
      fontSize: fontSize ?? this.fontSize,
      showComicProgress: showComicProgress ?? this.showComicProgress,
      showLastReadTime: showLastReadTime ?? this.showLastReadTime,

      // 存储配置
      defaultStorageLocation: defaultStorageLocation ?? this.defaultStorageLocation,
      customStoragePath: customStoragePath ?? this.customStoragePath,
      cacheMaxSize: cacheMaxSize ?? this.cacheMaxSize,
      cacheStrategy: cacheStrategy ?? this.cacheStrategy,
      autoImportDetection: autoImportDetection ?? this.autoImportDetection,
      autoCleanupDays: autoCleanupDays ?? this.autoCleanupDays,

      // 文件处理
      supportedFormats: supportedFormats ?? this.supportedFormats,
      imageQuality: imageQuality ?? this.imageQuality,
      coverQuality: coverQuality ?? this.coverQuality,
      duplicateStrategy: duplicateStrategy ?? this.duplicateStrategy,
      enableImageCompression: enableImageCompression ?? this.enableImageCompression,
      maxImageSize: maxImageSize ?? this.maxImageSize,

      // 同步配置
      autoSyncEnabled: autoSyncEnabled ?? this.autoSyncEnabled,
      syncFrequency: syncFrequency ?? this.syncFrequency,
      syncScope: syncScope ?? this.syncScope,
      conflictResolution: conflictResolution ?? this.conflictResolution,
      syncOnWifiOnly: syncOnWifiOnly ?? this.syncOnWifiOnly,

      // 备份恢复
      autoBackupEnabled: autoBackupEnabled ?? this.autoBackupEnabled,
      backupRetentionDays: backupRetentionDays ?? this.backupRetentionDays,
      backupFormat: backupFormat ?? this.backupFormat,
      selectiveRestore: selectiveRestore ?? this.selectiveRestore,

      // 应用信息
      showDeveloperInfo: showDeveloperInfo ?? this.showDeveloperInfo,
      enableUpdateCheck: enableUpdateCheck ?? this.enableUpdateCheck,
      showUsageHelp: showUsageHelp ?? this.showUsageHelp,
      acceptPrivacyPolicy: acceptPrivacyPolicy ?? this.acceptPrivacyPolicy,

      // 调试和诊断
      logLevel: logLevel ?? this.logLevel,
      performanceMonitoring: performanceMonitoring ?? this.performanceMonitoring,
      showCacheStatus: showCacheStatus ?? this.showCacheStatus,
      enableCrashReporting: enableCrashReporting ?? this.enableCrashReporting,

      // 额外的UI相关属性
      enableFullscreen: enableFullscreen ?? this.enableFullscreen,
      brightness: brightness ?? this.brightness,
      defaultDownloadPath: defaultDownloadPath ?? this.defaultDownloadPath,
      cacheDirectory: cacheDirectory ?? this.cacheDirectory,
      imageCacheSize: imageCacheSize ?? this.imageCacheSize,
      diskCacheSize: diskCacheSize ?? this.diskCacheSize,
      autoCleanTempFiles: autoCleanTempFiles ?? this.autoCleanTempFiles,
      clearCacheOnExit: clearCacheOnExit ?? this.clearCacheOnExit,
      smartMemoryManagement: smartMemoryManagement ?? this.smartMemoryManagement,

      // WebDAV 连接设置
      webdavUrl: webdavUrl ?? this.webdavUrl,
      webdavUsername: webdavUsername ?? this.webdavUsername,
      webdavPassword: webdavPassword ?? this.webdavPassword,
      autoSync: autoSync ?? this.autoSync,
      syncBookmarks: syncBookmarks ?? this.syncBookmarks,
      syncSettings: syncSettings ?? this.syncSettings,
      syncReadingHistory: syncReadingHistory ?? this.syncReadingHistory,
      autoBackup: autoBackup ?? this.autoBackup,
      backupInterval: backupInterval ?? this.backupInterval,

      // 调试选项
      debugMode: debugMode ?? this.debugMode,
      verboseLogging: verboseLogging ?? this.verboseLogging,
    );
  }

  // --- JSON Serialization ---

  factory ReaderSettings.fromJson(Map<String, dynamic> json) {
    return ReaderSettings(
      // 基础设置
      appTheme: AppTheme.values[json['appTheme'] ?? AppTheme.System.index],
      readingDirection: ReadingDirection.values[json['readingDirection'] ?? ReadingDirection.LTR.index],
      readingMode: ReadingMode.values[json['readingMode'] ?? ReadingMode.SinglePage.index],
      autoPageInterval: json['autoPageInterval'] ?? 5,
      webDavConfig: json['webDavConfig'] != null
          ? WebDAVConfig.fromJson(json['webDavConfig'])
          : null,

      // 阅读偏好设置
      pageTransition: PageTransition.values[json['pageTransition'] ?? PageTransition.Slide.index],
      autoPageEnabled: json['autoPageEnabled'] ?? false,
      tapAreaSensitivity: (json['tapAreaSensitivity'] ?? 0.25).toDouble(),

      // 显示设置
      defaultZoomLevel: (json['defaultZoomLevel'] ?? 1.0).toDouble(),
      screenOrientation: ScreenOrientation.values[json['screenOrientation'] ?? ScreenOrientation.Auto.index],
      hideStatusBar: json['hideStatusBar'] ?? true,
      hideNavigationBar: json['hideNavigationBar'] ?? true,
      keepScreenAwake: json['keepScreenAwake'] ?? true,
      fullScreenMode: json['fullScreenMode'] ?? true,

      // 视觉效果
      backgroundColor: BackgroundColor.values[json['backgroundColor'] ?? BackgroundColor.Black.index],
      customBackgroundColor: json['customBackgroundColor'] != null
          ? Color(json['customBackgroundColor'])
          : null,
      imageBrightness: (json['imageBrightness'] ?? 1.0).toDouble(),
      nightModeOptimization: json['nightModeOptimization'] ?? true,
      contrast: (json['contrast'] ?? 1.0).toDouble(),
      saturation: (json['saturation'] ?? 1.0).toDouble(),

      // 界面布局
      bookshelfLayout: BookshelfLayout.values[json['bookshelfLayout'] ?? BookshelfLayout.Grid3.index],
      comicCardStyle: ComicCardStyle.values[json['comicCardStyle'] ?? ComicCardStyle.Standard.index],
      favoritesLayout: BookshelfLayout.values[json['favoritesLayout'] ?? BookshelfLayout.Grid3.index],
      fontSize: FontSize.values[json['fontSize'] ?? FontSize.Medium.index],
      showComicProgress: json['showComicProgress'] ?? true,
      showLastReadTime: json['showLastReadTime'] ?? true,

      // 存储配置
      defaultStorageLocation: StorageLocation.values[json['defaultStorageLocation'] ?? StorageLocation.Internal.index],
      customStoragePath: json['customStoragePath'],
      cacheMaxSize: json['cacheMaxSize'] ?? 500,
      cacheStrategy: CacheStrategy.values[json['cacheStrategy'] ?? CacheStrategy.Balanced.index],
      autoImportDetection: json['autoImportDetection'] ?? true,
      autoCleanupDays: json['autoCleanupDays'] ?? 30,

      // 文件处理
      supportedFormats: (json['supportedFormats'] as List<dynamic>?)
              ?.map((e) => FileFormat.values[e])
              .toSet() ??
          {FileFormat.CBZ, FileFormat.CBR, FileFormat.ZIP, FileFormat.RAR},
      imageQuality: ImageQuality.values[json['imageQuality'] ?? ImageQuality.High.index],
      coverQuality: ImageQuality.values[json['coverQuality'] ?? ImageQuality.Medium.index],
      duplicateStrategy: DuplicateFileStrategy.values[json['duplicateStrategy'] ?? DuplicateFileStrategy.Prompt.index],
      enableImageCompression: json['enableImageCompression'] ?? false,
      maxImageSize: json['maxImageSize'] ?? 2048,

      // 同步配置
      autoSyncEnabled: json['autoSyncEnabled'] ?? false,
      syncFrequency: SyncFrequency.values[json['syncFrequency'] ?? SyncFrequency.Daily.index],
      syncScope: SyncScope.values[json['syncScope'] ?? SyncScope.All.index],
      conflictResolution: ConflictResolution.values[json['conflictResolution'] ?? ConflictResolution.Prompt.index],
      syncOnWifiOnly: json['syncOnWifiOnly'] ?? true,

      // 备份恢复
      autoBackupEnabled: json['autoBackupEnabled'] ?? false,
      backupRetentionDays: json['backupRetentionDays'] ?? 30,
      backupFormat: BackupFormat.values[json['backupFormat'] ?? BackupFormat.JSON.index],
      selectiveRestore: json['selectiveRestore'] ?? true,

      // 应用信息
      showDeveloperInfo: json['showDeveloperInfo'] ?? false,
      enableUpdateCheck: json['enableUpdateCheck'] ?? true,
      showUsageHelp: json['showUsageHelp'] ?? true,
      acceptPrivacyPolicy: json['acceptPrivacyPolicy'] ?? false,

      // 调试和诊断
      logLevel: LogLevel.values[json['logLevel'] ?? LogLevel.Warning.index],
      performanceMonitoring: json['performanceMonitoring'] ?? false,
      showCacheStatus: json['showCacheStatus'] ?? false,
      enableCrashReporting: json['enableCrashReporting'] ?? true,

      // 额外的UI相关属性
      enableFullscreen: json['enableFullscreen'] ?? true,
      brightness: (json['brightness'] ?? 1.0).toDouble(),
      defaultDownloadPath: json['defaultDownloadPath'] ?? '/storage/emulated/0/Download',
      cacheDirectory: json['cacheDirectory'] ?? '/storage/emulated/0/Android/data/com.example.easy_comic/cache',
      imageCacheSize: json['imageCacheSize'] ?? 100,
      diskCacheSize: json['diskCacheSize'] ?? 500,
      autoCleanTempFiles: json['autoCleanTempFiles'] ?? true,
      clearCacheOnExit: json['clearCacheOnExit'] ?? false,
      smartMemoryManagement: json['smartMemoryManagement'] ?? true,

      // WebDAV 连接设置
      webdavUrl: json['webdavUrl'] ?? '',
      webdavUsername: json['webdavUsername'] ?? '',
      webdavPassword: json['webdavPassword'] ?? '',
      autoSync: json['autoSync'] ?? false,
      syncBookmarks: json['syncBookmarks'] ?? true,
      syncSettings: json['syncSettings'] ?? true,
      syncReadingHistory: json['syncReadingHistory'] ?? true,
      autoBackup: json['autoBackup'] ?? false,
      backupInterval: json['backupInterval'] ?? 24,

      // 调试选项
      debugMode: json['debugMode'] ?? false,
      verboseLogging: json['verboseLogging'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      // 基础设置
      'appTheme': appTheme.index,
      'readingDirection': readingDirection.index,
      'readingMode': readingMode.index,
      'autoPageInterval': autoPageInterval,
      'webDavConfig': webDavConfig?.toJson(),

      // 阅读偏好设置
      'pageTransition': pageTransition.index,
      'autoPageEnabled': autoPageEnabled,
      'tapAreaSensitivity': tapAreaSensitivity,

      // 显示设置
      'defaultZoomLevel': defaultZoomLevel,
      'screenOrientation': screenOrientation.index,
      'hideStatusBar': hideStatusBar,
      'hideNavigationBar': hideNavigationBar,
      'keepScreenAwake': keepScreenAwake,
      'fullScreenMode': fullScreenMode,

      // 视觉效果
      'backgroundColor': backgroundColor.index,
      'customBackgroundColor': customBackgroundColor?.value,
      'imageBrightness': imageBrightness,
      'nightModeOptimization': nightModeOptimization,
      'contrast': contrast,
      'saturation': saturation,

      // 界面布局
      'bookshelfLayout': bookshelfLayout.index,
      'comicCardStyle': comicCardStyle.index,
      'favoritesLayout': favoritesLayout.index,
      'fontSize': fontSize.index,
      'showComicProgress': showComicProgress,
      'showLastReadTime': showLastReadTime,

      // 存储配置
      'defaultStorageLocation': defaultStorageLocation.index,
      'customStoragePath': customStoragePath,
      'cacheMaxSize': cacheMaxSize,
      'cacheStrategy': cacheStrategy.index,
      'autoImportDetection': autoImportDetection,
      'autoCleanupDays': autoCleanupDays,

      // 文件处理
      'supportedFormats': supportedFormats.map((e) => e.index).toList(),
      'imageQuality': imageQuality.index,
      'coverQuality': coverQuality.index,
      'duplicateStrategy': duplicateStrategy.index,
      'enableImageCompression': enableImageCompression,
      'maxImageSize': maxImageSize,

      // 同步配置
      'autoSyncEnabled': autoSyncEnabled,
      'syncFrequency': syncFrequency.index,
      'syncScope': syncScope.index,
      'conflictResolution': conflictResolution.index,
      'syncOnWifiOnly': syncOnWifiOnly,

      // 备份恢复
      'autoBackupEnabled': autoBackupEnabled,
      'backupRetentionDays': backupRetentionDays,
      'backupFormat': backupFormat.index,
      'selectiveRestore': selectiveRestore,

      // 应用信息
      'showDeveloperInfo': showDeveloperInfo,
      'enableUpdateCheck': enableUpdateCheck,
      'showUsageHelp': showUsageHelp,
      'acceptPrivacyPolicy': acceptPrivacyPolicy,

      // 调试和诊断
      'logLevel': logLevel.index,
      'performanceMonitoring': performanceMonitoring,
      'showCacheStatus': showCacheStatus,
      'enableCrashReporting': enableCrashReporting,

      // 额外的UI相关属性
      'enableFullscreen': enableFullscreen,
      'brightness': brightness,
      'defaultDownloadPath': defaultDownloadPath,
      'cacheDirectory': cacheDirectory,
      'imageCacheSize': imageCacheSize,
      'diskCacheSize': diskCacheSize,
      'autoCleanTempFiles': autoCleanTempFiles,
      'clearCacheOnExit': clearCacheOnExit,
      'smartMemoryManagement': smartMemoryManagement,

      // WebDAV 连接设置
      'webdavUrl': webdavUrl,
      'webdavUsername': webdavUsername,
      'webdavPassword': webdavPassword,
      'autoSync': autoSync,
      'syncBookmarks': syncBookmarks,
      'syncSettings': syncSettings,
      'syncReadingHistory': syncReadingHistory,
      'autoBackup': autoBackup,
      'backupInterval': backupInterval,

      // 调试选项
      'debugMode': debugMode,
      'verboseLogging': verboseLogging,
    };
  }
}