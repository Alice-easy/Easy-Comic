import 'dart:async';
import '../../domain/services/auto_page_service.dart';
import '../../domain/entities/reader_settings.dart';

class AutoPageServiceImpl implements AutoPageService {
  Timer? _autoPageTimer;
  final StreamController<AutoPageEvent> _eventController = StreamController<AutoPageEvent>.broadcast();
  final StreamController<AutoPageState> _stateController = StreamController<AutoPageState>.broadcast();
  
  AutoPageState _state = const AutoPageState(
    isActive: false,
    isPaused: false,
    intervalSeconds: 5,
    remainingSeconds: 5,
  );

  @override
  Stream<AutoPageEvent> get autoPageEventStream => _eventController.stream;

  @override
  int get currentInterval => _state.intervalSeconds;

  @override
  bool get isAutoPageActive => _state.isActive;

  @override
  bool get isAutoPagePaused => _state.isPaused;

  @override
  Future<void> pauseForUserInteraction() {
    // TODO: implement pauseForUserInteraction
    throw UnimplementedError();
  }

  @override
  Future<void> resetTimer() {
    // TODO: implement resetTimer
    throw UnimplementedError();
  }

  @override
  Future<void> setAutoPageConfig(AutoPageConfig config) {
    // TODO: implement setAutoPageConfig
    throw UnimplementedError();
  }

  @override
  Stream<AutoPageState> watchAutoPageState() => _stateController.stream;

  @override
  Future<void> startAutoPage(int intervalSeconds) async {
    await stopAutoPage();
    _updateState(_state.copyWith(
      isActive: true,
      isPaused: false,
      intervalSeconds: intervalSeconds,
      remainingSeconds: intervalSeconds,
    ));
    _startTimer();
    _eventController.add(AutoPageEvent.started());
  }

  @override
  Future<void> stopAutoPage() async {
    _autoPageTimer?.cancel();
    _updateState(_state.copyWith(isActive: false, isPaused: false));
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
    _updateState(_state.copyWith(intervalSeconds: intervalSeconds));
    if (_state.isActive && !_state.isPaused) {
      _autoPageTimer?.cancel();
      _startTimer();
    }
  }

  void _startTimer() {
    _autoPageTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_state.remainingSeconds <= 1) {
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
  }

  void dispose() {
    _autoPageTimer?.cancel();
    _eventController.close();
    _stateController.close();
  }
}

extension on AutoPageState {
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