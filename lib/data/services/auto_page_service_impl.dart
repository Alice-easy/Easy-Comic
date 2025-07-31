import 'dart:async';
import '../../domain/services/auto_page_service.dart';

class AutoPageServiceImpl implements AutoPageService {
  Timer? _autoPageTimer;
  StreamController<AutoPageEvent>? _eventController;
  Duration _interval = const Duration(seconds: 5);
  bool _isActive = false;
  int _currentPage = 0;
  int _totalPages = 0;

  @override
  Future<void> startAutoPage(int currentPage, int totalPages, Duration interval) async {
    await stopAutoPage();
    
    _currentPage = currentPage;
    _totalPages = totalPages;
    _interval = interval;
    _isActive = true;
    
    _eventController ??= StreamController<AutoPageEvent>.broadcast();
    
    _autoPageTimer = Timer.periodic(_interval, (timer) {
      if (_currentPage < _totalPages - 1) {
        _currentPage++;
        _eventController?.add(AutoPageEvent.pageChanged(_currentPage));
      } else {
        // Reached the end, stop auto page
        stopAutoPage();
        _eventController?.add(AutoPageEvent.completed());
      }
    });
    
    _eventController?.add(AutoPageEvent.started());
  }

  @override
  Future<void> stopAutoPage() async {
    if (_autoPageTimer != null) {
      _autoPageTimer!.cancel();
      _autoPageTimer = null;
      _isActive = false;
      _eventController?.add(AutoPageEvent.stopped());
    }
  }

  @override
  Future<void> pauseAutoPage() async {
    if (_isActive && _autoPageTimer != null) {
      _autoPageTimer!.cancel();
      _autoPageTimer = null;
      _eventController?.add(AutoPageEvent.paused());
    }
  }

  @override
  Future<void> resumeAutoPage() async {
    if (_isActive && _autoPageTimer == null) {
      _autoPageTimer = Timer.periodic(_interval, (timer) {
        if (_currentPage < _totalPages - 1) {
          _currentPage++;
          _eventController?.add(AutoPageEvent.pageChanged(_currentPage));
        } else {
          stopAutoPage();
          _eventController?.add(AutoPageEvent.completed());
        }
      });
      _eventController?.add(AutoPageEvent.resumed());
    }
  }

  @override
  Future<bool> isActive() async {
    return _isActive;
  }

  @override
  Future<bool> isPaused() async {
    return _isActive && _autoPageTimer == null;
  }

  @override
  Future<Duration> getInterval() async {
    return _interval;
  }

  @override
  Future<void> setInterval(Duration interval) async {
    if (_interval != interval) {
      _interval = interval;
      
      // If currently running, restart with new interval
      if (_isActive && _autoPageTimer != null) {
        _autoPageTimer!.cancel();
        _autoPageTimer = Timer.periodic(_interval, (timer) {
          if (_currentPage < _totalPages - 1) {
            _currentPage++;
            _eventController?.add(AutoPageEvent.pageChanged(_currentPage));
          } else {
            stopAutoPage();
            _eventController?.add(AutoPageEvent.completed());
          }
        });
      }
      
      _eventController?.add(AutoPageEvent.intervalChanged(interval));
    }
  }

  @override
  Stream<AutoPageEvent> watchAutoPageEvents() {
    _eventController ??= StreamController<AutoPageEvent>.broadcast();
    return _eventController!.stream;
  }

  @override
  Future<void> updateCurrentPage(int page) async {
    _currentPage = page;
  }

  @override
  void dispose() {
    stopAutoPage();
    _eventController?.close();
    _eventController = null;
  }
}