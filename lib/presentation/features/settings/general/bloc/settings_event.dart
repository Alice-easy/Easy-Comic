// lib/presentation/features/settings/general/bloc/settings_event.dart
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';

abstract class SettingsEvent extends Equatable {
  const SettingsEvent();

  @override
  List<Object?> get props => [];
}

// 加载设置
class LoadSettings extends SettingsEvent {}

// 重置为默认设置
class ResetToDefaults extends SettingsEvent {}

// 导入设置
class ImportSettings extends SettingsEvent {
  final Map<String, dynamic> settingsData;

  const ImportSettings(this.settingsData);

  @override
  List<Object> get props => [settingsData];
}

// 导出设置
class ExportSettings extends SettingsEvent {}

// 阅读偏好设置
class UpdateReadingDirection extends SettingsEvent {
  final ReadingDirection direction;

  const UpdateReadingDirection(this.direction);

  @override
  List<Object> get props => [direction];
}

class UpdateReadingMode extends SettingsEvent {
  final ReadingMode mode;

  const UpdateReadingMode(this.mode);

  @override
  List<Object> get props => [mode];
}

class UpdatePageTransition extends SettingsEvent {
  final PageTransition transition;

  const UpdatePageTransition(this.transition);

  @override
  List<Object> get props => [transition];
}

class UpdateAutoPageInterval extends SettingsEvent {
  final int interval;

  const UpdateAutoPageInterval(this.interval);

  @override
  List<Object> get props => [interval];
}

class UpdateAutoPageEnabled extends SettingsEvent {
  final bool enabled;

  const UpdateAutoPageEnabled(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateTapAreaSensitivity extends SettingsEvent {
  final double sensitivity;

  const UpdateTapAreaSensitivity(this.sensitivity);

  @override
  List<Object> get props => [sensitivity];
}

// 显示设置
class UpdateDefaultZoomLevel extends SettingsEvent {
  final double zoomLevel;

  const UpdateDefaultZoomLevel(this.zoomLevel);

  @override
  List<Object> get props => [zoomLevel];
}

class UpdateScreenOrientation extends SettingsEvent {
  final ScreenOrientation orientation;

  const UpdateScreenOrientation(this.orientation);

  @override
  List<Object> get props => [orientation];
}

class UpdateHideStatusBar extends SettingsEvent {
  final bool hide;

  const UpdateHideStatusBar(this.hide);

  @override
  List<Object> get props => [hide];
}

class UpdateHideNavigationBar extends SettingsEvent {
  final bool hide;

  const UpdateHideNavigationBar(this.hide);

  @override
  List<Object> get props => [hide];
}

class UpdateKeepScreenAwake extends SettingsEvent {
  final bool awake;

  const UpdateKeepScreenAwake(this.awake);

  @override
  List<Object> get props => [awake];
}

class UpdateFullScreenMode extends SettingsEvent {
  final bool fullScreen;

  const UpdateFullScreenMode(this.fullScreen);

  @override
  List<Object> get props => [fullScreen];
}

// 视觉效果
class UpdateBackgroundColor extends SettingsEvent {
  final BackgroundColor color;

  const UpdateBackgroundColor(this.color);

  @override
  List<Object> get props => [color];
}

class UpdateCustomBackgroundColor extends SettingsEvent {
  final Color? color;

  const UpdateCustomBackgroundColor(this.color);

  @override
  List<Object?> get props => [color];
}

class UpdateImageBrightness extends SettingsEvent {
  final double brightness;

  const UpdateImageBrightness(this.brightness);

  @override
  List<Object> get props => [brightness];
}

class UpdateNightModeOptimization extends SettingsEvent {
  final bool enabled;

  const UpdateNightModeOptimization(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateContrast extends SettingsEvent {
  final double contrast;

  const UpdateContrast(this.contrast);

  @override
  List<Object> get props => [contrast];
}

class UpdateSaturation extends SettingsEvent {
  final double saturation;

  const UpdateSaturation(this.saturation);

  @override
  List<Object> get props => [saturation];
}

// 界面布局
class UpdateBookshelfLayout extends SettingsEvent {
  final BookshelfLayout layout;

  const UpdateBookshelfLayout(this.layout);

  @override
  List<Object> get props => [layout];
}

class UpdateComicCardStyle extends SettingsEvent {
  final ComicCardStyle style;

  const UpdateComicCardStyle(this.style);

  @override
  List<Object> get props => [style];
}

class UpdateFavoritesLayout extends SettingsEvent {
  final BookshelfLayout layout;

  const UpdateFavoritesLayout(this.layout);

  @override
  List<Object> get props => [layout];
}

class UpdateFontSize extends SettingsEvent {
  final FontSize size;

  const UpdateFontSize(this.size);

  @override
  List<Object> get props => [size];
}

class UpdateShowComicProgress extends SettingsEvent {
  final bool show;

  const UpdateShowComicProgress(this.show);

  @override
  List<Object> get props => [show];
}

class UpdateShowLastReadTime extends SettingsEvent {
  final bool show;

  const UpdateShowLastReadTime(this.show);

  @override
  List<Object> get props => [show];
}

// 存储配置
class UpdateDefaultStorageLocation extends SettingsEvent {
  final StorageLocation location;

  const UpdateDefaultStorageLocation(this.location);

  @override
  List<Object> get props => [location];
}

class UpdateCustomStoragePath extends SettingsEvent {
  final String? path;

  const UpdateCustomStoragePath(this.path);

  @override
  List<Object?> get props => [path];
}

class UpdateCacheMaxSize extends SettingsEvent {
  final int sizeInMB;

  const UpdateCacheMaxSize(this.sizeInMB);

  @override
  List<Object> get props => [sizeInMB];
}

class UpdateCacheStrategy extends SettingsEvent {
  final CacheStrategy strategy;

  const UpdateCacheStrategy(this.strategy);

  @override
  List<Object> get props => [strategy];
}

class UpdateAutoImportDetection extends SettingsEvent {
  final bool enabled;

  const UpdateAutoImportDetection(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateAutoCleanupDays extends SettingsEvent {
  final int days;

  const UpdateAutoCleanupDays(this.days);

  @override
  List<Object> get props => [days];
}

// 文件处理
class UpdateSupportedFormats extends SettingsEvent {
  final Set<FileFormat> formats;

  const UpdateSupportedFormats(this.formats);

  @override
  List<Object> get props => [formats];
}

class UpdateImageQuality extends SettingsEvent {
  final ImageQuality quality;

  const UpdateImageQuality(this.quality);

  @override
  List<Object> get props => [quality];
}

class UpdateCoverQuality extends SettingsEvent {
  final ImageQuality quality;

  const UpdateCoverQuality(this.quality);

  @override
  List<Object> get props => [quality];
}

class UpdateDuplicateStrategy extends SettingsEvent {
  final DuplicateFileStrategy strategy;

  const UpdateDuplicateStrategy(this.strategy);

  @override
  List<Object> get props => [strategy];
}

class UpdateEnableImageCompression extends SettingsEvent {
  final bool enabled;

  const UpdateEnableImageCompression(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateMaxImageSize extends SettingsEvent {
  final int sizeInPixels;

  const UpdateMaxImageSize(this.sizeInPixels);

  @override
  List<Object> get props => [sizeInPixels];
}

// 同步配置
class UpdateAutoSyncEnabled extends SettingsEvent {
  final bool enabled;

  const UpdateAutoSyncEnabled(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateSyncFrequency extends SettingsEvent {
  final SyncFrequency frequency;

  const UpdateSyncFrequency(this.frequency);

  @override
  List<Object> get props => [frequency];
}

class UpdateSyncScope extends SettingsEvent {
  final SyncScope scope;

  const UpdateSyncScope(this.scope);

  @override
  List<Object> get props => [scope];
}

class UpdateConflictResolution extends SettingsEvent {
  final ConflictResolution resolution;

  const UpdateConflictResolution(this.resolution);

  @override
  List<Object> get props => [resolution];
}

class UpdateSyncOnWifiOnly extends SettingsEvent {
  final bool wifiOnly;

  const UpdateSyncOnWifiOnly(this.wifiOnly);

  @override
  List<Object> get props => [wifiOnly];
}

// 备份恢复
class UpdateAutoBackupEnabled extends SettingsEvent {
  final bool enabled;

  const UpdateAutoBackupEnabled(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateBackupRetentionDays extends SettingsEvent {
  final int days;

  const UpdateBackupRetentionDays(this.days);

  @override
  List<Object> get props => [days];
}

class UpdateBackupFormat extends SettingsEvent {
  final BackupFormat format;

  const UpdateBackupFormat(this.format);

  @override
  List<Object> get props => [format];
}

class UpdateSelectiveRestore extends SettingsEvent {
  final bool enabled;

  const UpdateSelectiveRestore(this.enabled);

  @override
  List<Object> get props => [enabled];
}

// 应用信息
class UpdateShowDeveloperInfo extends SettingsEvent {
  final bool show;

  const UpdateShowDeveloperInfo(this.show);

  @override
  List<Object> get props => [show];
}

class UpdateEnableUpdateCheck extends SettingsEvent {
  final bool enabled;

  const UpdateEnableUpdateCheck(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateShowUsageHelp extends SettingsEvent {
  final bool show;

  const UpdateShowUsageHelp(this.show);

  @override
  List<Object> get props => [show];
}

class UpdateAcceptPrivacyPolicy extends SettingsEvent {
  final bool accepted;

  const UpdateAcceptPrivacyPolicy(this.accepted);

  @override
  List<Object> get props => [accepted];
}

// 调试和诊断
class UpdateLogLevel extends SettingsEvent {
  final LogLevel level;

  const UpdateLogLevel(this.level);

  @override
  List<Object> get props => [level];
}

class UpdatePerformanceMonitoring extends SettingsEvent {
  final bool enabled;

  const UpdatePerformanceMonitoring(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateShowCacheStatus extends SettingsEvent {
  final bool show;

  const UpdateShowCacheStatus(this.show);

  @override
  List<Object> get props => [show];
}

class UpdateEnableCrashReporting extends SettingsEvent {
  final bool enabled;

  const UpdateEnableCrashReporting(this.enabled);

  @override
  List<Object> get props => [enabled];
}

// 文件管理缺失的事件
class UpdateImageCacheSize extends SettingsEvent {
  final int sizeInMB;

  const UpdateImageCacheSize(this.sizeInMB);

  @override
  List<Object> get props => [sizeInMB];
}

class UpdateDiskCacheSize extends SettingsEvent {
  final int sizeInMB;

  const UpdateDiskCacheSize(this.sizeInMB);

  @override
  List<Object> get props => [sizeInMB];
}

class UpdateAutoCleanTempFiles extends SettingsEvent {
  final bool enabled;

  const UpdateAutoCleanTempFiles(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateClearCacheOnExit extends SettingsEvent {
  final bool enabled;

  const UpdateClearCacheOnExit(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateSmartMemoryManagement extends SettingsEvent {
  final bool enabled;

  const UpdateSmartMemoryManagement(this.enabled);

  @override
  List<Object> get props => [enabled];
}

// 同步备份缺失的事件
class UpdateAutoSync extends SettingsEvent {
  final bool enabled;

  const UpdateAutoSync(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateSyncBookmarks extends SettingsEvent {
  final bool enabled;

  const UpdateSyncBookmarks(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateSyncSettings extends SettingsEvent {
  final bool enabled;

  const UpdateSyncSettings(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateSyncReadingHistory extends SettingsEvent {
  final bool enabled;

  const UpdateSyncReadingHistory(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateAutoBackup extends SettingsEvent {
  final bool enabled;

  const UpdateAutoBackup(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateBackupInterval extends SettingsEvent {
  final int intervalInDays;

  const UpdateBackupInterval(this.intervalInDays);

  @override
  List<Object> get props => [intervalInDays];
}

class UpdateWebdavSettings extends SettingsEvent {
  final String url;
  final String username;
  final String password;

  const UpdateWebdavSettings({
    required this.url,
    required this.username,
    required this.password,
  });

  @override
  List<Object> get props => [url, username, password];
}

class PerformBackup extends SettingsEvent {}

class PerformRestore extends SettingsEvent {}

// 调试缺失的事件
class UpdateDebugMode extends SettingsEvent {
  final bool enabled;

  const UpdateDebugMode(this.enabled);

  @override
  List<Object> get props => [enabled];
}

class UpdateVerboseLogging extends SettingsEvent {
  final bool enabled;

  const UpdateVerboseLogging(this.enabled);

  @override
  List<Object> get props => [enabled];
}

// 清理缓存
class ClearCache extends SettingsEvent {}

// 重置应用数据
class ResetAppData extends SettingsEvent {}