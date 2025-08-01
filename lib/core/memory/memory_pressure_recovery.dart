import 'dart:async';
import 'dart:io';
import 'package:flutter/foundation.dart';
import '../services/cache_service.dart';
import '../error/retry_mechanism.dart';

/// Memory pressure levels with thresholds
enum MemoryPressureLevel {
  normal,    // < 60MB
  warning,   // 60-80MB
  critical,  // 80-100MB
  emergency, // > 100MB
}

/// Recovery strategies for different pressure levels
enum RecoveryStrategy {
  none,
  lightCleanup,
  aggressiveCleanup,
  qualityDegradation,
  emergencyCleanup,
}

/// Image quality levels for degradation
enum ImageQuality {
  original,
  high,
  medium,
  thumbnail,
}

/// Memory usage statistics
class MemoryStats {
  final int totalMemoryMB;
  final int usedMemoryMB;
  final int cacheMemoryMB;
  final double usagePercentage;
  final MemoryPressureLevel pressureLevel;
  final DateTime timestamp;
  
  const MemoryStats({
    required this.totalMemoryMB,
    required this.usedMemoryMB,
    required this.cacheMemoryMB,
    required this.usagePercentage,
    required this.pressureLevel,
    required this.timestamp,
  });
  
  @override
  String toString() {
    return 'MemoryStats(used: ${usedMemoryMB}MB, cache: ${cacheMemoryMB}MB, '
        'pressure: $pressureLevel, usage: ${usagePercentage.toStringAsFixed(1)}%)';
  }
}

/// Recovery action result
class RecoveryResult {
  final int memoryFreedMB;
  final RecoveryStrategy strategy;
  final Duration duration;
  final bool succeeded;
  final String? error;
  
  const RecoveryResult({
    required this.memoryFreedMB,
    required this.strategy,
    required this.duration,
    required this.succeeded,
    this.error,
  });
}

/// Memory pressure recovery service with automatic cleanup and quality degradation
class MemoryPressureRecovery {
  final ICacheService _cacheService;
  final ExponentialBackoffRetry _retry = ExponentialBackoffRetry();
  
  // Monitoring configuration
  final Duration monitoringInterval;
  final int maxMemoryMB;
  final int warningThresholdMB;
  final int criticalThresholdMB;
  
  // State tracking
  Timer? _monitoringTimer;
  final StreamController<MemoryStats> _memoryStatsController = 
      StreamController<MemoryStats>.broadcast();
  final StreamController<RecoveryResult> _recoveryResultController = 
      StreamController<RecoveryResult>.broadcast();
  
  MemoryStats? _lastStats;
  bool _isRecovering = false;
  ImageQuality _currentQuality = ImageQuality.high;
  
  // Recovery statistics
  int _totalRecoveries = 0;
  int _totalMemoryFreed = 0;
  DateTime? _lastRecoveryTime;
  
  MemoryPressureRecovery({
    required ICacheService cacheService,
    this.monitoringInterval = const Duration(seconds: 5),
    this.maxMemoryMB = 100,
    this.warningThresholdMB = 60,
    this.criticalThresholdMB = 80,
  }) : _cacheService = cacheService {
    _startMonitoring();
  }
  
  /// Stream of memory statistics
  Stream<MemoryStats> get memoryStatsStream => _memoryStatsController.stream;
  
  /// Stream of recovery results
  Stream<RecoveryResult> get recoveryResultStream => _recoveryResultController.stream;
  
  /// Current memory statistics
  MemoryStats? get currentStats => _lastStats;
  
  /// Current image quality level
  ImageQuality get currentQuality => _currentQuality;
  
  /// Recovery statistics
  Map<String, dynamic> get recoveryStats => {
    'totalRecoveries': _totalRecoveries,
    'totalMemoryFreed': _totalMemoryFreed,
    'lastRecoveryTime': _lastRecoveryTime?.toIso8601String(),
    'currentQuality': _currentQuality.name,
  };
  
  void _startMonitoring() {
    _monitoringTimer = Timer.periodic(monitoringInterval, (_) {
      _checkMemoryPressure();
    });
  }
  
  Future<void> _checkMemoryPressure() async {
    try {
      final stats = await _getCurrentMemoryStats();
      _lastStats = stats;
      _memoryStatsController.add(stats);
      
      // Trigger recovery if needed
      if (stats.pressureLevel != MemoryPressureLevel.normal && !_isRecovering) {
        await _triggerRecovery(stats);
      }
      
    } catch (e) {
      debugPrint('Error checking memory pressure: $e');
    }
  }
  
  Future<MemoryStats> _getCurrentMemoryStats() async {
    // Get cache statistics
    final cacheStats = await _cacheService.getStats();
    final cacheMemoryMB = cacheStats.memoryUsage.round();
    
    // Estimate total memory usage (cache + overhead)
    final estimatedTotalMB = (cacheMemoryMB * 1.5).round(); // 50% overhead estimate
    
    // Determine pressure level
    MemoryPressureLevel pressureLevel;
    if (estimatedTotalMB < warningThresholdMB) {
      pressureLevel = MemoryPressureLevel.normal;
    } else if (estimatedTotalMB < criticalThresholdMB) {
      pressureLevel = MemoryPressureLevel.warning;
    } else if (estimatedTotalMB < maxMemoryMB) {
      pressureLevel = MemoryPressureLevel.critical;
    } else {
      pressureLevel = MemoryPressureLevel.emergency;
    }
    
    return MemoryStats(
      totalMemoryMB: maxMemoryMB,
      usedMemoryMB: estimatedTotalMB,
      cacheMemoryMB: cacheMemoryMB,
      usagePercentage: (estimatedTotalMB / maxMemoryMB) * 100,
      pressureLevel: pressureLevel,
      timestamp: DateTime.now(),
    );
  }
  
  Future<void> _triggerRecovery(MemoryStats stats) async {
    if (_isRecovering) return;
    
    _isRecovering = true;
    final stopwatch = Stopwatch()..start();
    
    try {
      final strategy = _selectRecoveryStrategy(stats.pressureLevel);
      final initialMemory = stats.usedMemoryMB;
      
      await _executeRecoveryStrategy(strategy, stats);
      
      // Check results
      final newStats = await _getCurrentMemoryStats();
      final memoryFreed = initialMemory - newStats.usedMemoryMB;
      
      stopwatch.stop();
      
      final result = RecoveryResult(
        memoryFreedMB: memoryFreed,
        strategy: strategy,
        duration: stopwatch.elapsed,
        succeeded: memoryFreed > 0,
      );
      
      _recoveryResultController.add(result);
      _updateRecoveryStats(result);
      
      debugPrint('Memory recovery completed: freed ${memoryFreed}MB using $strategy');
      
    } catch (e) {
      stopwatch.stop();
      
      final result = RecoveryResult(
        memoryFreedMB: 0,
        strategy: RecoveryStrategy.none,
        duration: stopwatch.elapsed,
        succeeded: false,
        error: e.toString(),
      );
      
      _recoveryResultController.add(result);
      debugPrint('Memory recovery failed: $e');
      
    } finally {
      _isRecovering = false;
    }
  }
  
  RecoveryStrategy _selectRecoveryStrategy(MemoryPressureLevel level) {
    switch (level) {
      case MemoryPressureLevel.normal:
        return RecoveryStrategy.none;
      case MemoryPressureLevel.warning:
        return RecoveryStrategy.lightCleanup;
      case MemoryPressureLevel.critical:
        return RecoveryStrategy.aggressiveCleanup;
      case MemoryPressureLevel.emergency:
        return RecoveryStrategy.emergencyCleanup;
    }
  }
  
  Future<void> _executeRecoveryStrategy(RecoveryStrategy strategy, MemoryStats stats) async {
    switch (strategy) {
      case RecoveryStrategy.none:
        break;
        
      case RecoveryStrategy.lightCleanup:
        await _performLightCleanup();
        break;
        
      case RecoveryStrategy.aggressiveCleanup:
        await _performAggressiveCleanup();
        break;
        
      case RecoveryStrategy.qualityDegradation:
        await _degradeImageQuality();
        await _performAggressiveCleanup();
        break;
        
      case RecoveryStrategy.emergencyCleanup:
        await _performEmergencyCleanup();
        break;
    }
  }
  
  Future<void> _performLightCleanup() async {
    await _retry.execute(
      () async {
        // Clean up expired cache entries
        await _cacheService.cleanupCacheAsync();
        
        // Trigger garbage collection
        if (!kReleaseMode) {
          await Future.delayed(const Duration(milliseconds: 100));
        }
      },
      config: RetryConfig.database,
    );
  }
  
  Future<void> _performAggressiveCleanup() async {
    await _retry.execute(
      () async {
        // Clear low priority cache entries
        await _cacheService.cleanupCacheAsync();
        
        // Force garbage collection
        await _forceGarbageCollection();
      },
      config: RetryConfig.database,
    );
  }
  
  Future<void> _performEmergencyCleanup() async {
    await _retry.execute(
      () async {
        // Clear all cache
        await _cacheService.clearCache();
        
        // Degrade quality to minimum
        _currentQuality = ImageQuality.thumbnail;
        
        // Force garbage collection
        await _forceGarbageCollection();
      },
      config: RetryConfig.database,
    );
  }
  
  Future<void> _degradeImageQuality() async {
    // Step down quality level
    switch (_currentQuality) {
      case ImageQuality.original:
        _currentQuality = ImageQuality.high;
        break;
      case ImageQuality.high:
        _currentQuality = ImageQuality.medium;
        break;
      case ImageQuality.medium:
        _currentQuality = ImageQuality.thumbnail;
        break;
      case ImageQuality.thumbnail:
        // Already at minimum quality
        break;
    }
    
    debugPrint('Degraded image quality to: $_currentQuality');
  }
  
  Future<void> _forceGarbageCollection() async {
    // Platform-specific garbage collection hints
    if (Platform.isAndroid || Platform.isIOS) {
      // On mobile platforms, suggest garbage collection
      await Future.delayed(const Duration(milliseconds: 50));
    }
  }
  
  void _updateRecoveryStats(RecoveryResult result) {
    if (result.succeeded) {
      _totalRecoveries++;
      _totalMemoryFreed += result.memoryFreedMB;
      _lastRecoveryTime = DateTime.now();
    }
  }
  
  /// Manually trigger memory recovery
  Future<RecoveryResult> triggerManualRecovery({RecoveryStrategy? strategy}) async {
    final stats = await _getCurrentMemoryStats();
    final selectedStrategy = strategy ?? _selectRecoveryStrategy(stats.pressureLevel);
    
    final stopwatch = Stopwatch()..start();
    final initialMemory = stats.usedMemoryMB;
    
    try {
      await _executeRecoveryStrategy(selectedStrategy, stats);
      
      final newStats = await _getCurrentMemoryStats();
      final memoryFreed = initialMemory - newStats.usedMemoryMB;
      
      stopwatch.stop();
      
      final result = RecoveryResult(
        memoryFreedMB: memoryFreed,
        strategy: selectedStrategy,
        duration: stopwatch.elapsed,
        succeeded: memoryFreed > 0,
      );
      
      _updateRecoveryStats(result);
      return result;
      
    } catch (e) {
      stopwatch.stop();
      
      return RecoveryResult(
        memoryFreedMB: 0,
        strategy: selectedStrategy,
        duration: stopwatch.elapsed,
        succeeded: false,
        error: e.toString(),
      );
    }
  }
  
  /// Reset image quality to default
  void resetImageQuality() {
    _currentQuality = ImageQuality.high;
  }
  
  /// Stop monitoring and cleanup
  void dispose() {
    _monitoringTimer?.cancel();
    _memoryStatsController.close();
    _recoveryResultController.close();
  }
}