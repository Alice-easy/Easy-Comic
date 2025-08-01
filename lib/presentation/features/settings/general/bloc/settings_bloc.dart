// lib/presentation/features/settings/general/bloc/settings_bloc.dart
import 'dart:convert';
import 'dart:io';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/core/services/cache_service.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:path_provider/path_provider.dart';
import 'settings_event.dart';
import 'settings_state.dart';

class SettingsBloc extends Bloc<SettingsEvent, SettingsState> {
  final SettingsRepository _settingsRepository;
  final CacheService _cacheService;
  final LoggingService _loggingService;

  SettingsBloc({
    required SettingsRepository settingsRepository,
    required CacheService cacheService,
    required LoggingService loggingService,
  })  : _settingsRepository = settingsRepository,
        _cacheService = cacheService,
        _loggingService = loggingService,
        super(SettingsInitial()) {
    on<LoadSettings>(_onLoadSettings);
    on<ResetToDefaults>(_onResetToDefaults);
    on<ImportSettings>(_onImportSettings);
    on<ExportSettings>(_onExportSettings);
    
    // 阅读偏好设置
    on<UpdateReadingDirection>(_onUpdateReadingDirection);
    on<UpdateReadingMode>(_onUpdateReadingMode);
    on<UpdatePageTransition>(_onUpdatePageTransition);
    on<UpdateAutoPageInterval>(_onUpdateAutoPageInterval);
    on<UpdateAutoPageEnabled>(_onUpdateAutoPageEnabled);
    on<UpdateTapAreaSensitivity>(_onUpdateTapAreaSensitivity);
    
    // 显示设置
    on<UpdateDefaultZoomLevel>(_onUpdateDefaultZoomLevel);
    on<UpdateScreenOrientation>(_onUpdateScreenOrientation);
    on<UpdateHideStatusBar>(_onUpdateHideStatusBar);
    on<UpdateHideNavigationBar>(_onUpdateHideNavigationBar);
    on<UpdateKeepScreenAwake>(_onUpdateKeepScreenAwake);
    on<UpdateFullScreenMode>(_onUpdateFullScreenMode);
    
    // 视觉效果
    on<UpdateBackgroundColor>(_onUpdateBackgroundColor);
    on<UpdateCustomBackgroundColor>(_onUpdateCustomBackgroundColor);
    on<UpdateImageBrightness>(_onUpdateImageBrightness);
    on<UpdateNightModeOptimization>(_onUpdateNightModeOptimization);
    on<UpdateContrast>(_onUpdateContrast);
    on<UpdateSaturation>(_onUpdateSaturation);
    
    // 界面布局
    on<UpdateBookshelfLayout>(_onUpdateBookshelfLayout);
    on<UpdateComicCardStyle>(_onUpdateComicCardStyle);
    on<UpdateFavoritesLayout>(_onUpdateFavoritesLayout);
    on<UpdateFontSize>(_onUpdateFontSize);
    on<UpdateShowComicProgress>(_onUpdateShowComicProgress);
    on<UpdateShowLastReadTime>(_onUpdateShowLastReadTime);
    
    // 存储配置
    on<UpdateDefaultStorageLocation>(_onUpdateDefaultStorageLocation);
    on<UpdateCustomStoragePath>(_onUpdateCustomStoragePath);
    on<UpdateCacheMaxSize>(_onUpdateCacheMaxSize);
    on<UpdateCacheStrategy>(_onUpdateCacheStrategy);
    on<UpdateAutoImportDetection>(_onUpdateAutoImportDetection);
    on<UpdateAutoCleanupDays>(_onUpdateAutoCleanupDays);
    
    // 文件处理
    on<UpdateSupportedFormats>(_onUpdateSupportedFormats);
    on<UpdateImageQuality>(_onUpdateImageQuality);
    on<UpdateCoverQuality>(_onUpdateCoverQuality);
    on<UpdateDuplicateStrategy>(_onUpdateDuplicateStrategy);
    on<UpdateEnableImageCompression>(_onUpdateEnableImageCompression);
    on<UpdateMaxImageSize>(_onUpdateMaxImageSize);
    
    // 同步配置
    on<UpdateAutoSyncEnabled>(_onUpdateAutoSyncEnabled);
    on<UpdateSyncFrequency>(_onUpdateSyncFrequency);
    on<UpdateSyncScope>(_onUpdateSyncScope);
    on<UpdateConflictResolution>(_onUpdateConflictResolution);
    on<UpdateSyncOnWifiOnly>(_onUpdateSyncOnWifiOnly);
    
    // 备份恢复
    on<UpdateAutoBackupEnabled>(_onUpdateAutoBackupEnabled);
    on<UpdateBackupRetentionDays>(_onUpdateBackupRetentionDays);
    on<UpdateBackupFormat>(_onUpdateBackupFormat);
    on<UpdateSelectiveRestore>(_onUpdateSelectiveRestore);
    
    // 应用信息
    on<UpdateShowDeveloperInfo>(_onUpdateShowDeveloperInfo);
    on<UpdateEnableUpdateCheck>(_onUpdateEnableUpdateCheck);
    on<UpdateShowUsageHelp>(_onUpdateShowUsageHelp);
    on<UpdateAcceptPrivacyPolicy>(_onUpdateAcceptPrivacyPolicy);
    
    // 调试和诊断
    on<UpdateLogLevel>(_onUpdateLogLevel);
    on<UpdatePerformanceMonitoring>(_onUpdatePerformanceMonitoring);
    on<UpdateShowCacheStatus>(_onUpdateShowCacheStatus);
    on<UpdateEnableCrashReporting>(_onUpdateEnableCrashReporting);
    
    // 清理操作
    on<ClearCache>(_onClearCache);
    on<ResetAppData>(_onResetAppData);
  }

  Future<void> _onLoadSettings(
    LoadSettings event,
    Emitter<SettingsState> emit,
  ) async {
    emit(SettingsLoading());
    try {
      final result = await _settingsRepository.getReaderSettings();
      result.fold(
        (failure) => emit(SettingsError(failure.message)),
        (settings) => emit(SettingsLoaded(settings: settings)),
      );
    } catch (e) {
      _loggingService.logError('Failed to load settings', e);
      emit(const SettingsError('加载设置失败'));
    }
  }

  Future<void> _onResetToDefaults(
    ResetToDefaults event,
    Emitter<SettingsState> emit,
  ) async {
    emit(SettingsResetting());
    try {
      const defaultSettings = ReaderSettings();
      final result = await _settingsRepository.saveReaderSettings(defaultSettings);
      result.fold(
        (failure) => emit(SettingsError(failure.message)),
        (_) => emit(SettingsReset(defaultSettings)),
      );
    } catch (e) {
      _loggingService.logError('Failed to reset settings', e);
      emit(const SettingsError('重置设置失败'));
    }
  }

  Future<void> _onImportSettings(
    ImportSettings event,
    Emitter<SettingsState> emit,
  ) async {
    emit(SettingsImporting());
    try {
      final settings = ReaderSettings.fromJson(event.settingsData);
      final result = await _settingsRepository.saveReaderSettings(settings);
      result.fold(
        (failure) => emit(SettingsError(failure.message)),
        (_) => emit(SettingsImported(settings)),
      );
    } catch (e) {
      _loggingService.logError('Failed to import settings', e);
      emit(const SettingsError('导入设置失败'));
    }
  }

  Future<void> _onExportSettings(
    ExportSettings event,
    Emitter<SettingsState> emit,
  ) async {
    if (state is! SettingsLoaded) return;
    
    final currentState = state as SettingsLoaded;
    emit(SettingsExporting(currentState.settings));
    
    try {
      final directory = await getApplicationDocumentsDirectory();
      final timestamp = DateTime.now().millisecondsSinceEpoch;
      final filePath = '${directory.path}/easy_comic_settings_$timestamp.json';
      
      final settingsJson = currentState.settings.toJson();
      final jsonString = const JsonEncoder.withIndent('  ').convert(settingsJson);
      
      await File(filePath).writeAsString(jsonString);
      
      emit(SettingsExported(
        exportPath: filePath,
        settings: currentState.settings,
      ));
    } catch (e) {
      _loggingService.logError('Failed to export settings', e);
      emit(const SettingsError('导出设置失败'));
    }
  }

  Future<void> _onClearCache(
    ClearCache event,
    Emitter<SettingsState> emit,
  ) async {
    emit(CacheClearing());
    try {
      await _cacheService.clearAll();
      emit(const CacheCleared('缓存已清理'));
    } catch (e) {
      _loggingService.logError('Failed to clear cache', e);
      emit(const SettingsError('清理缓存失败'));
    }
  }

  Future<void> _onResetAppData(
    ResetAppData event,
    Emitter<SettingsState> emit,
  ) async {
    emit(AppDataResetting());
    try {
      // 清理缓存
      await _cacheService.clearAll();
      
      // 重置设置为默认值
      const defaultSettings = ReaderSettings();
      await _settingsRepository.saveReaderSettings(defaultSettings);
      
      emit(const AppDataReset('应用数据已重置'));
    } catch (e) {
      _loggingService.logError('Failed to reset app data', e);
      emit(const SettingsError('重置应用数据失败'));
    }
  }

  // 通用的设置更新方法
  Future<void> _updateSetting<T>(
    T Function(ReaderSettings) getValue,
    ReaderSettings Function(ReaderSettings, T) updateFunction,
    T newValue,
    Emitter<SettingsState> emit,
  ) async {
    if (state is! SettingsLoaded) return;
    
    final currentState = state as SettingsLoaded;
    final currentValue = getValue(currentState.settings);
    
    // 如果值没有变化，不需要更新
    if (currentValue == newValue) return;
    
    final updatedSettings = updateFunction(currentState.settings, newValue);
    
    emit(SettingsSaving(updatedSettings));
    
    try {
      final result = await _settingsRepository.saveReaderSettings(updatedSettings);
      result.fold(
        (failure) => emit(SettingsError(failure.message)),
        (_) => emit(currentState.copyWith(settings: updatedSettings)),
      );
    } catch (e) {
      _loggingService.logError('Failed to save setting', e);
      emit(const SettingsError('保存设置失败'));
    }
  }

  // 阅读偏好设置处理器
  Future<void> _onUpdateReadingDirection(
    UpdateReadingDirection event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.readingDirection,
      (settings, value) => settings.copyWith(readingDirection: value),
      event.direction,
      emit,
    );
  }

  Future<void> _onUpdateReadingMode(
    UpdateReadingMode event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.readingMode,
      (settings, value) => settings.copyWith(readingMode: value),
      event.mode,
      emit,
    );
  }

  Future<void> _onUpdatePageTransition(
    UpdatePageTransition event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.pageTransition,
      (settings, value) => settings.copyWith(pageTransition: value),
      event.transition,
      emit,
    );
  }

  Future<void> _onUpdateAutoPageInterval(
    UpdateAutoPageInterval event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoPageInterval,
      (settings, value) => settings.copyWith(autoPageInterval: value),
      event.interval,
      emit,
    );
  }

  Future<void> _onUpdateAutoPageEnabled(
    UpdateAutoPageEnabled event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoPageEnabled,
      (settings, value) => settings.copyWith(autoPageEnabled: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateTapAreaSensitivity(
    UpdateTapAreaSensitivity event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.tapAreaSensitivity,
      (settings, value) => settings.copyWith(tapAreaSensitivity: value),
      event.sensitivity,
      emit,
    );
  }

  // 显示设置处理器
  Future<void> _onUpdateDefaultZoomLevel(
    UpdateDefaultZoomLevel event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.defaultZoomLevel,
      (settings, value) => settings.copyWith(defaultZoomLevel: value),
      event.zoomLevel,
      emit,
    );
  }

  Future<void> _onUpdateScreenOrientation(
    UpdateScreenOrientation event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.screenOrientation,
      (settings, value) => settings.copyWith(screenOrientation: value),
      event.orientation,
      emit,
    );
  }

  Future<void> _onUpdateHideStatusBar(
    UpdateHideStatusBar event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.hideStatusBar,
      (settings, value) => settings.copyWith(hideStatusBar: value),
      event.hide,
      emit,
    );
  }

  Future<void> _onUpdateHideNavigationBar(
    UpdateHideNavigationBar event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.hideNavigationBar,
      (settings, value) => settings.copyWith(hideNavigationBar: value),
      event.hide,
      emit,
    );
  }

  Future<void> _onUpdateKeepScreenAwake(
    UpdateKeepScreenAwake event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.keepScreenAwake,
      (settings, value) => settings.copyWith(keepScreenAwake: value),
      event.awake,
      emit,
    );
  }

  Future<void> _onUpdateFullScreenMode(
    UpdateFullScreenMode event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.fullScreenMode,
      (settings, value) => settings.copyWith(fullScreenMode: value),
      event.fullScreen,
      emit,
    );
  }

  // 视觉效果处理器
  Future<void> _onUpdateBackgroundColor(
    UpdateBackgroundColor event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.backgroundColor,
      (settings, value) => settings.copyWith(backgroundColor: value),
      event.color,
      emit,
    );
  }

  Future<void> _onUpdateCustomBackgroundColor(
    UpdateCustomBackgroundColor event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.customBackgroundColor,
      (settings, value) => settings.copyWith(customBackgroundColor: value),
      event.color,
      emit,
    );
  }

  Future<void> _onUpdateImageBrightness(
    UpdateImageBrightness event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.imageBrightness,
      (settings, value) => settings.copyWith(imageBrightness: value),
      event.brightness,
      emit,
    );
  }

  Future<void> _onUpdateNightModeOptimization(
    UpdateNightModeOptimization event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.nightModeOptimization,
      (settings, value) => settings.copyWith(nightModeOptimization: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateContrast(
    UpdateContrast event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.contrast,
      (settings, value) => settings.copyWith(contrast: value),
      event.contrast,
      emit,
    );
  }

  Future<void> _onUpdateSaturation(
    UpdateSaturation event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.saturation,
      (settings, value) => settings.copyWith(saturation: value),
      event.saturation,
      emit,
    );
  }

  // 界面布局处理器
  Future<void> _onUpdateBookshelfLayout(
    UpdateBookshelfLayout event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.bookshelfLayout,
      (settings, value) => settings.copyWith(bookshelfLayout: value),
      event.layout,
      emit,
    );
  }

  Future<void> _onUpdateComicCardStyle(
    UpdateComicCardStyle event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.comicCardStyle,
      (settings, value) => settings.copyWith(comicCardStyle: value),
      event.style,
      emit,
    );
  }

  Future<void> _onUpdateFavoritesLayout(
    UpdateFavoritesLayout event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.favoritesLayout,
      (settings, value) => settings.copyWith(favoritesLayout: value),
      event.layout,
      emit,
    );
  }

  Future<void> _onUpdateFontSize(
    UpdateFontSize event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.fontSize,
      (settings, value) => settings.copyWith(fontSize: value),
      event.size,
      emit,
    );
  }

  Future<void> _onUpdateShowComicProgress(
    UpdateShowComicProgress event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.showComicProgress,
      (settings, value) => settings.copyWith(showComicProgress: value),
      event.show,
      emit,
    );
  }

  Future<void> _onUpdateShowLastReadTime(
    UpdateShowLastReadTime event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.showLastReadTime,
      (settings, value) => settings.copyWith(showLastReadTime: value),
      event.show,
      emit,
    );
  }

  // 存储配置处理器
  Future<void> _onUpdateDefaultStorageLocation(
    UpdateDefaultStorageLocation event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.defaultStorageLocation,
      (settings, value) => settings.copyWith(defaultStorageLocation: value),
      event.location,
      emit,
    );
  }

  Future<void> _onUpdateCustomStoragePath(
    UpdateCustomStoragePath event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.customStoragePath,
      (settings, value) => settings.copyWith(customStoragePath: value),
      event.path,
      emit,
    );
  }

  Future<void> _onUpdateCacheMaxSize(
    UpdateCacheMaxSize event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.cacheMaxSize,
      (settings, value) => settings.copyWith(cacheMaxSize: value),
      event.sizeInMB,
      emit,
    );
  }

  Future<void> _onUpdateCacheStrategy(
    UpdateCacheStrategy event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.cacheStrategy,
      (settings, value) => settings.copyWith(cacheStrategy: value),
      event.strategy,
      emit,
    );
  }

  Future<void> _onUpdateAutoImportDetection(
    UpdateAutoImportDetection event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoImportDetection,
      (settings, value) => settings.copyWith(autoImportDetection: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateAutoCleanupDays(
    UpdateAutoCleanupDays event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoCleanupDays,
      (settings, value) => settings.copyWith(autoCleanupDays: value),
      event.days,
      emit,
    );
  }

  // 文件处理处理器
  Future<void> _onUpdateSupportedFormats(
    UpdateSupportedFormats event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.supportedFormats,
      (settings, value) => settings.copyWith(supportedFormats: value),
      event.formats,
      emit,
    );
  }

  Future<void> _onUpdateImageQuality(
    UpdateImageQuality event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.imageQuality,
      (settings, value) => settings.copyWith(imageQuality: value),
      event.quality,
      emit,
    );
  }

  Future<void> _onUpdateCoverQuality(
    UpdateCoverQuality event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.coverQuality,
      (settings, value) => settings.copyWith(coverQuality: value),
      event.quality,
      emit,
    );
  }

  Future<void> _onUpdateDuplicateStrategy(
    UpdateDuplicateStrategy event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.duplicateStrategy,
      (settings, value) => settings.copyWith(duplicateStrategy: value),
      event.strategy,
      emit,
    );
  }

  Future<void> _onUpdateEnableImageCompression(
    UpdateEnableImageCompression event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.enableImageCompression,
      (settings, value) => settings.copyWith(enableImageCompression: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateMaxImageSize(
    UpdateMaxImageSize event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.maxImageSize,
      (settings, value) => settings.copyWith(maxImageSize: value),
      event.sizeInPixels,
      emit,
    );
  }

  // 同步配置处理器
  Future<void> _onUpdateAutoSyncEnabled(
    UpdateAutoSyncEnabled event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoSyncEnabled,
      (settings, value) => settings.copyWith(autoSyncEnabled: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateSyncFrequency(
    UpdateSyncFrequency event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.syncFrequency,
      (settings, value) => settings.copyWith(syncFrequency: value),
      event.frequency,
      emit,
    );
  }

  Future<void> _onUpdateSyncScope(
    UpdateSyncScope event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.syncScope,
      (settings, value) => settings.copyWith(syncScope: value),
      event.scope,
      emit,
    );
  }

  Future<void> _onUpdateConflictResolution(
    UpdateConflictResolution event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.conflictResolution,
      (settings, value) => settings.copyWith(conflictResolution: value),
      event.resolution,
      emit,
    );
  }

  Future<void> _onUpdateSyncOnWifiOnly(
    UpdateSyncOnWifiOnly event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.syncOnWifiOnly,
      (settings, value) => settings.copyWith(syncOnWifiOnly: value),
      event.wifiOnly,
      emit,
    );
  }

  // 备份恢复处理器
  Future<void> _onUpdateAutoBackupEnabled(
    UpdateAutoBackupEnabled event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.autoBackupEnabled,
      (settings, value) => settings.copyWith(autoBackupEnabled: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateBackupRetentionDays(
    UpdateBackupRetentionDays event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.backupRetentionDays,
      (settings, value) => settings.copyWith(backupRetentionDays: value),
      event.days,
      emit,
    );
  }

  Future<void> _onUpdateBackupFormat(
    UpdateBackupFormat event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.backupFormat,
      (settings, value) => settings.copyWith(backupFormat: value),
      event.format,
      emit,
    );
  }

  Future<void> _onUpdateSelectiveRestore(
    UpdateSelectiveRestore event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.selectiveRestore,
      (settings, value) => settings.copyWith(selectiveRestore: value),
      event.enabled,
      emit,
    );
  }

  // 应用信息处理器
  Future<void> _onUpdateShowDeveloperInfo(
    UpdateShowDeveloperInfo event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.showDeveloperInfo,
      (settings, value) => settings.copyWith(showDeveloperInfo: value),
      event.show,
      emit,
    );
  }

  Future<void> _onUpdateEnableUpdateCheck(
    UpdateEnableUpdateCheck event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.enableUpdateCheck,
      (settings, value) => settings.copyWith(enableUpdateCheck: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateShowUsageHelp(
    UpdateShowUsageHelp event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.showUsageHelp,
      (settings, value) => settings.copyWith(showUsageHelp: value),
      event.show,
      emit,
    );
  }

  Future<void> _onUpdateAcceptPrivacyPolicy(
    UpdateAcceptPrivacyPolicy event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.acceptPrivacyPolicy,
      (settings, value) => settings.copyWith(acceptPrivacyPolicy: value),
      event.accepted,
      emit,
    );
  }

  // 调试和诊断处理器
  Future<void> _onUpdateLogLevel(
    UpdateLogLevel event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.logLevel,
      (settings, value) => settings.copyWith(logLevel: value),
      event.level,
      emit,
    );
  }

  Future<void> _onUpdatePerformanceMonitoring(
    UpdatePerformanceMonitoring event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.performanceMonitoring,
      (settings, value) => settings.copyWith(performanceMonitoring: value),
      event.enabled,
      emit,
    );
  }

  Future<void> _onUpdateShowCacheStatus(
    UpdateShowCacheStatus event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.showCacheStatus,
      (settings, value) => settings.copyWith(showCacheStatus: value),
      event.show,
      emit,
    );
  }

  Future<void> _onUpdateEnableCrashReporting(
    UpdateEnableCrashReporting event,
    Emitter<SettingsState> emit,
  ) async {
    await _updateSetting(
      (settings) => settings.enableCrashReporting,
      (settings, value) => settings.copyWith(enableCrashReporting: value),
      event.enabled,
      emit,
    );
  }
}