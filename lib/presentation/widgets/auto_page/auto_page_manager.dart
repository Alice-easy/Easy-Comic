import 'dart:async';
import 'package:flutter/material.dart';
import '../../../domain/entities/reader_settings.dart';
import '../../../domain/entities/auto_page_config.dart';
import '../../../domain/services/auto_page_service.dart';

class AutoPageManager extends ChangeNotifier implements AutoPageService {
  Timer? _autoPageTimer;
  Timer? _interactionPauseTimer;
  
  AutoPageState _state = const AutoPageState(
    isActive: false,
    isPaused: false,
    intervalSeconds: 5,
    remainingSeconds: 5,
  );
  
  final StreamController<AutoPageEvent> _eventController = StreamController<AutoPageEvent>.broadcast();
  final StreamController<AutoPageState> _stateController = StreamController<AutoPageState>.broadcast();
  
  AutoPageConfig _config = const AutoPageConfig();
  
  bool _userInteractionDetected = false;
  DateTime? _lastUserInteraction;
  
  @override
  Stream<AutoPageEvent> get autoPageEventStream => _eventController.stream;
  
  @override
  bool get isAutoPageActive => _state.isActive;
  
  @override
  bool get isAutoPagePaused => _state.isPaused;
  
  @override
  int get currentInterval => _state.intervalSeconds;
  
  AutoPageState get currentState => _state;
  
  @override
  Future<void> startAutoPage(int intervalSeconds) async {
    await stopAutoPage(); // Stop any existing timer
    
    _updateState(_state.copyWith(
      isActive: true,
      isPaused: false,
      intervalSeconds: intervalSeconds,
      remainingSeconds: intervalSeconds,
    ));
    
    _startTimer();
    _eventController.add(AutoPageEvent.resume());
  }
  
  @override
  Future<void> stopAutoPage() async {
    _autoPageTimer?.cancel();
    _interactionPauseTimer?.cancel();
    
    _updateState(_state.copyWith(
      isActive: false,
      isPaused: false,
      remainingSeconds: _state.intervalSeconds,
    ));
    
    _eventController.add(AutoPageEvent.stop());
  }
  
  @override
  Future<void> pauseAutoPage() async {
    if (!_state.isActive) return;
    
    _autoPageTimer?.cancel();
    
    _updateState(_state.copyWith(isPaused: true));
    _eventController.add(AutoPageEvent.pause());
  }
  
  @override
  Future<void> resumeAutoPage() async {
    if (!_state.isActive || !_state.isPaused) return;
    
    _updateState(_state.copyWith(isPaused: false));
    _startTimer();
    _eventController.add(AutoPageEvent.resume());
  }
  
  @override
  Future<void> setInterval(int intervalSeconds) async {
    final wasActive = _state.isActive;
    final wasPaused = _state.isPaused;
    
    _updateState(_state.copyWith(
      intervalSeconds: intervalSeconds,
      remainingSeconds: intervalSeconds,
    ));
    
    if (wasActive && !wasPaused) {
      await startAutoPage(intervalSeconds);
    }
  }
  
  @override
  Stream<AutoPageState> watchAutoPageState() => _stateController.stream;
  
  @override
  Future<void> resetTimer() async {
    if (!_state.isActive || _state.isPaused) return;
    
    _autoPageTimer?.cancel();
    _updateState(_state.copyWith(remainingSeconds: _state.intervalSeconds));
    _startTimer();
  }
  
  @override
  Future<void> pauseForUserInteraction() async {
    if (!_config.pauseOnUserInteraction || !_state.isActive) return;
    
    _userInteractionDetected = true;
    _lastUserInteraction = DateTime.now();
    
    await pauseAutoPage();
    
    // Start timer to resume after interaction delay
    _interactionPauseTimer?.cancel();
    _interactionPauseTimer = Timer(_config.interactionPauseDelay, () {
      if (_userInteractionDetected && _state.isPaused) {
        _userInteractionDetected = false;
        resumeAutoPage();
      }
    });
  }
  
  @override
  Future<void> setAutoPageConfig(AutoPageConfig config) async {
    _config = config;
    
    // Apply new interval if active
    if (_state.isActive && _state.intervalSeconds != config.defaultInterval) {
      await setInterval(config.defaultInterval);
    }
  }
  
  void _startTimer() {
    _autoPageTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_state.remainingSeconds <= 1) {
        // Time to turn page
        _eventController.add(AutoPageEvent.nextPage());
        _updateState(_state.copyWith(remainingSeconds: _state.intervalSeconds));
      } else {
        _updateState(_state.copyWith(remainingSeconds: _state.remainingSeconds - 1));
      }
    });
  }
  
  void _updateState(AutoPageState newState) {
    _state = newState;
    _stateController.add(_state);
    notifyListeners();
  }
  
  /// Handle reaching the last page
  Future<void> handleLastPageReached() async {
    if (_config.stopAtLastPage && _state.isActive) {
      await stopAutoPage();
      // Could emit a special event or show notification
    }
  }
  
  /// Handle app going to background
  Future<void> handleAppBackground() async {
    if (_config.pauseOnAppBackground && _state.isActive && !_state.isPaused) {
      await pauseAutoPage();
    }
  }
  
  /// Handle app returning to foreground
  Future<void> handleAppForeground() async {
    if (_config.pauseOnAppBackground && _state.isActive && _state.isPaused) {
      await resumeAutoPage();
    }
  }
  
  @override
  void dispose() {
    _autoPageTimer?.cancel();
    _interactionPauseTimer?.cancel();
    _eventController.close();
    _stateController.close();
    super.dispose();
  }
}

extension AutoPageStateExtension on AutoPageState {
  AutoPageState copyWith({
    bool? isActive,
    bool? isPaused,
    int? intervalSeconds,
    int? remainingSeconds,
  }) {
    return AutoPageState(
      isActive: isActive ?? this.isActive,
      isPaused: isPaused ?? this.isPaused,
      intervalSeconds: intervalSeconds ?? this.intervalSeconds,
      remainingSeconds: remainingSeconds ?? this.remainingSeconds,
    );
  }
}