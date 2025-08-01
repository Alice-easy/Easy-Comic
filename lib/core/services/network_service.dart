import 'dart:async';
import 'dart:io';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/foundation.dart';
import 'message_service.dart';
import 'logging_service.dart';

/// 网络连接类型
enum NetworkType {
  none,
  wifi,
  mobile,
  ethernet,
  bluetooth,
  vpn,
  other,
}

/// 网络状态
class NetworkStatus {
  final bool isConnected;
  final NetworkType type;
  final DateTime timestamp;
  final String? ssid;
  final int? signalStrength;

  const NetworkStatus({
    required this.isConnected,
    required this.type,
    required this.timestamp,
    this.ssid,
    this.signalStrength,
  });

  /// 获取网络描述
  String get description {
    if (!isConnected) return '无网络连接';
    
    switch (type) {
      case NetworkType.wifi:
        return ssid != null ? 'WiFi ($ssid)' : 'WiFi';
      case NetworkType.mobile:
        return '移动数据';
      case NetworkType.ethernet:
        return '有线网络';
      case NetworkType.bluetooth:
        return '蓝牙';
      case NetworkType.vpn:
        return 'VPN';
      case NetworkType.other:
        return '其他网络';
      case NetworkType.none:
        return '无网络';
    }
  }

  /// 是否为高速网络
  bool get isHighSpeed {
    return type == NetworkType.wifi || type == NetworkType.ethernet;
  }

  /// 是否为计费网络
  bool get isMetered {
    return type == NetworkType.mobile;
  }

  @override
  String toString() => 'NetworkStatus(connected: $isConnected, type: $type)';
}

/// 网络监控服务
class NetworkService {
  final Connectivity _connectivity = Connectivity();
  final MessageService _messageService;
  final LoggingService _loggingService;

  StreamSubscription<List<ConnectivityResult>>? _connectivitySubscription;
  Timer? _internetCheckTimer;
  NetworkStatus _currentStatus = NetworkStatus(
    isConnected: false,
    type: NetworkType.none,
    timestamp: DateTime.now(),
  );

  bool _hasShownOfflineMessage = false;
  bool _isInitialized = false;
  final Duration _internetCheckInterval = const Duration(seconds: 30);
  final Duration _offlineRetryInterval = const Duration(seconds: 10);

  final StreamController<NetworkStatus> _statusController = 
      StreamController<NetworkStatus>.broadcast();

  NetworkService({
    required MessageService messageService,
    required LoggingService loggingService,
  }) : _messageService = messageService,
       _loggingService = loggingService;

  /// 当前网络状态
  NetworkStatus get currentStatus => _currentStatus;

  /// 网络状态流
  Stream<NetworkStatus> get statusStream => _statusController.stream;

  /// 是否已连接到互联网
  bool get isConnected => _currentStatus.isConnected;

  /// 是否为离线模式
  bool get isOffline => !_currentStatus.isConnected;

  /// 初始化网络监控
  Future<void> initialize() async {
    if (_isInitialized) return;

    try {
      // 检查初始连接状态
      await _checkInitialConnectivity();

      // 开始监听网络状态变化
      _startConnectivityListener();

      // 开始定期检查互联网连接
      _startInternetConnectivityCheck();

      _isInitialized = true;
      _loggingService.info('Network service initialized', _currentStatus);
    } catch (e, stackTrace) {
      _loggingService.error('Failed to initialize network service', e, stackTrace);
    }
  }

  /// 停止网络监控
  void dispose() {
    _connectivitySubscription?.cancel();
    _internetCheckTimer?.cancel();
    _statusController.close();
    _isInitialized = false;
    _loggingService.info('Network service disposed');
  }

  /// 手动检查网络状态
  Future<NetworkStatus> checkNetworkStatus() async {
    try {
      final connectivityResults = await _connectivity.checkConnectivity();
      final hasInternet = await _checkInternetConnectivity();
      
      _updateNetworkStatus(connectivityResults, hasInternet);
      return _currentStatus;
    } catch (e, stackTrace) {
      _loggingService.error('Failed to check network status', e, stackTrace);
      return _currentStatus;
    }
  }

  /// 检查特定主机的连接性
  Future<bool> checkHostConnectivity(String host, {int port = 80, Duration timeout = const Duration(seconds: 5)}) async {
    try {
      final result = await InternetAddress.lookup(host)
          .timeout(timeout);
      
      if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
        final socket = await Socket.connect(result[0], port)
            .timeout(timeout);
        socket.destroy();
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  /// 等待网络连接
  Future<bool> waitForConnection({Duration timeout = const Duration(seconds: 30)}) async {
    if (isConnected) return true;

    final completer = Completer<bool>();
    late StreamSubscription subscription;

    subscription = statusStream.listen((status) {
      if (status.isConnected) {
        subscription.cancel();
        if (!completer.isCompleted) {
          completer.complete(true);
        }
      }
    });

    // 设置超时
    Timer(timeout, () {
      subscription.cancel();
      if (!completer.isCompleted) {
        completer.complete(false);
      }
    });

    return completer.future;
  }

  /// 显示网络状态消息
  void showNetworkStatusMessage() {
    if (isConnected) {
      _messageService.showSuccess('网络连接已恢复: ${_currentStatus.description}');
    } else {
      _messageService.showError('网络连接中断，部分功能可能不可用');
    }
  }

  /// 检查初始连接状态
  Future<void> _checkInitialConnectivity() async {
    final connectivityResults = await _connectivity.checkConnectivity();
    final hasInternet = await _checkInternetConnectivity();
    _updateNetworkStatus(connectivityResults, hasInternet);
  }

  /// 开始监听网络状态变化
  void _startConnectivityListener() {
    _connectivitySubscription = _connectivity.onConnectivityChanged.listen(
      (List<ConnectivityResult> results) async {
        _loggingService.info('Connectivity changed to: $results');
        
        // 检查实际的互联网连接
        final hasInternet = await _checkInternetConnectivity();
        _updateNetworkStatus(results, hasInternet);
      },
      onError: (error, stackTrace) {
        _loggingService.error('Connectivity stream error', error, stackTrace);
      },
    );
  }

  /// 开始定期检查互联网连接
  void _startInternetConnectivityCheck() {
    _internetCheckTimer = Timer.periodic(_internetCheckInterval, (_) async {
      if (_currentStatus.type != NetworkType.none) {
        final hasInternet = await _checkInternetConnectivity();
        if (hasInternet != _currentStatus.isConnected) {
          _updateNetworkStatus(await _connectivity.checkConnectivity(), hasInternet);
        }
      }
    });
  }

  /// 检查实际的互联网连接
  Future<bool> _checkInternetConnectivity() async {
    try {
      // 尝试连接多个可靠的服务器
      final hosts = ['8.8.8.8', '1.1.1.1', 'google.com'];
      
      for (final host in hosts) {
        try {
          final result = await InternetAddress.lookup(host)
              .timeout(const Duration(seconds: 3));
          
          if (result.isNotEmpty && result[0].rawAddress.isNotEmpty) {
            return true;
          }
        } catch (e) {
          continue;
        }
      }
      
      return false;
    } catch (e) {
      return false;
    }
  }

  /// 更新网络状态
  void _updateNetworkStatus(List<ConnectivityResult> connectivityResults, bool hasInternet) {
    // 取第一个非none的结果，如果都是none则取第一个
    final connectivityResult = connectivityResults.firstWhere(
      (result) => result != ConnectivityResult.none,
      orElse: () => connectivityResults.first,
    );
    
    final networkType = _convertConnectivityResult(connectivityResult);
    final wasConnected = _currentStatus.isConnected;
    
    final newStatus = NetworkStatus(
      isConnected: hasInternet,
      type: networkType,
      timestamp: DateTime.now(),
    );

    if (_currentStatus.isConnected != newStatus.isConnected ||
        _currentStatus.type != newStatus.type) {
      
      _currentStatus = newStatus;
      _statusController.add(_currentStatus);

      // 记录状态变化
      _loggingService.info('Network status changed', _currentStatus);

      // 处理状态变化的用户反馈
      _handleNetworkStatusChange(wasConnected, newStatus);
    }
  }

  /// 处理网络状态变化的用户反馈
  void _handleNetworkStatusChange(bool wasConnected, NetworkStatus newStatus) {
    if (!wasConnected && newStatus.isConnected) {
      // 网络恢复
      _hasShownOfflineMessage = false;
      _messageService.showSuccess('网络连接已恢复: ${newStatus.description}');
    } else if (wasConnected && !newStatus.isConnected) {
      // 网络中断
      if (!_hasShownOfflineMessage) {
        _hasShownOfflineMessage = true;
        _messageService.showError(
          '网络连接中断',
          actionLabel: '重试',
          onAction: () => checkNetworkStatus(),
        );
      }
    } else if (newStatus.isConnected && newStatus.type != _currentStatus.type) {
      // 网络类型变化（但仍有连接）
      _messageService.showInfo('网络已切换至: ${newStatus.description}');
    }
  }

  /// 转换连接类型
  NetworkType _convertConnectivityResult(ConnectivityResult result) {
    switch (result) {
      case ConnectivityResult.wifi:
        return NetworkType.wifi;
      case ConnectivityResult.mobile:
        return NetworkType.mobile;
      case ConnectivityResult.ethernet:
        return NetworkType.ethernet;
      case ConnectivityResult.bluetooth:
        return NetworkType.bluetooth;
      case ConnectivityResult.vpn:
        return NetworkType.vpn;
      case ConnectivityResult.other:
        return NetworkType.other;
      case ConnectivityResult.none:
        return NetworkType.none;
    }
  }

  /// 获取网络质量评估
  Future<String> getNetworkQuality() async {
    if (!isConnected) return '无网络连接';

    try {
      final stopwatch = Stopwatch()..start();
      final hasConnection = await checkHostConnectivity('8.8.8.8');
      stopwatch.stop();

      if (!hasConnection) return '网络不稳定';

      final latency = stopwatch.elapsedMilliseconds;
      
      if (latency < 50) return '网络质量：优秀';
      if (latency < 100) return '网络质量：良好';
      if (latency < 200) return '网络质量：一般';
      return '网络质量：较差';
    } catch (e) {
      return '网络质量：未知';
    }
  }

  /// 获取网络使用建议
  String getNetworkUsageAdvice() {
    if (!isConnected) {
      return '当前无网络连接，部分功能不可用。请检查网络设置。';
    }

    if (_currentStatus.isMetered) {
      return '当前使用移动数据，建议在WiFi环境下进行大文件操作。';
    }

    if (!_currentStatus.isHighSpeed) {
      return '当前网络速度较慢，大文件操作可能需要更长时间。';
    }

    return '网络连接良好，可以正常使用所有功能。';
  }
}