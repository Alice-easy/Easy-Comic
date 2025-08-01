import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:device_info_plus/device_info_plus.dart';
import '../bloc/settings_bloc.dart';
import '../bloc/settings_event.dart';
import '../bloc/settings_state.dart';
import '../../../../../../domain/entities/reader_settings.dart';

class DebugCard extends StatefulWidget {
  const DebugCard({super.key});

  @override
  State<DebugCard> createState() => _DebugCardState();
}

class _DebugCardState extends State<DebugCard> {
  Map<String, dynamic>? _deviceInfo;
  String _logs = '';

  @override
  void initState() {
    super.initState();
    _loadDeviceInfo();
    _loadLogs();
  }

  Future<void> _loadDeviceInfo() async {
    final deviceInfoPlugin = DeviceInfoPlugin();
    try {
      if (Theme.of(context).platform == TargetPlatform.android) {
        final androidInfo = await deviceInfoPlugin.androidInfo;
        setState(() {
          _deviceInfo = {
            'Platform': 'Android',
            'Model': androidInfo.model,
            'Manufacturer': androidInfo.manufacturer,
            'Version': androidInfo.version.release,
            'SDK': androidInfo.version.sdkInt.toString(),
            'Brand': androidInfo.brand,
            'Product': androidInfo.product,
          };
        });
      } else if (Theme.of(context).platform == TargetPlatform.iOS) {
        final iosInfo = await deviceInfoPlugin.iosInfo;
        setState(() {
          _deviceInfo = {
            'Platform': 'iOS',
            'Model': iosInfo.model,
            'Name': iosInfo.name,
            'System Version': iosInfo.systemVersion,
            'Machine': iosInfo.utsname.machine,
          };
        });
      }
    } catch (e) {
      setState(() {
        _deviceInfo = {'Error': e.toString()};
      });
    }
  }

  void _loadLogs() {
    // 模拟日志数据
    setState(() {
      _logs = '''[2024-08-01 19:30:15] INFO: Application started
[2024-08-01 19:30:16] DEBUG: Settings loaded successfully
[2024-08-01 19:30:17] INFO: Cache initialized: 256MB
[2024-08-01 19:30:18] DEBUG: WebDAV connection established
[2024-08-01 19:30:20] INFO: Comic library scan completed: 42 files
[2024-08-01 19:30:21] DEBUG: Memory usage: 128MB/512MB
[2024-08-01 19:30:22] INFO: Auto-backup scheduled for tomorrow 02:00''';
    });
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<SettingsBloc, SettingsState>(
      builder: (context, state) {
        if (state is! SettingsLoaded) {
          return const Card(
            child: Padding(
              padding: EdgeInsets.all(16.0),
              child: Center(child: CircularProgressIndicator()),
            ),
          );
        }

        final settings = state.settings;
        return Card(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.bug_report_outlined, color: Theme.of(context).primaryColor),
                    const SizedBox(width: 8),
                    Text(
                      '调试与诊断',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                
                // 调试选项
                SwitchListTile(
                  secondary: const Icon(Icons.code_outlined),
                  title: const Text('开发者模式'),
                  subtitle: const Text('启用详细日志和调试信息'),
                  value: settings.debugMode,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateDebugMode(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.analytics_outlined),
                  title: const Text('性能监控'),
                  subtitle: const Text('监控应用性能和内存使用'),
                  value: settings.performanceMonitoring,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdatePerformanceMonitoring(value),
                    );
                  },
                ),
                
                SwitchListTile(
                  secondary: const Icon(Icons.description_outlined),
                  title: const Text('详细日志'),
                  subtitle: const Text('记录详细的应用操作日志'),
                  value: settings.verboseLogging,
                  onChanged: (value) {
                    context.read<SettingsBloc>().add(
                      UpdateVerboseLogging(value),
                    );
                  },
                ),
                
                const Divider(),
                
                // 日志级别选择
                ListTile(
                  leading: const Icon(Icons.filter_list_outlined),
                  title: const Text('日志级别'),
                  subtitle: Text(_getLogLevelText(settings.logLevel)),
                  trailing: DropdownButton<LogLevel>(
                    value: settings.logLevel,
                    onChanged: (value) {
                      if (value != null) {
                        context.read<SettingsBloc>().add(
                          UpdateLogLevel(value),
                        );
                      }
                    },
                    items: LogLevel.values.map((level) {
                      return DropdownMenuItem(
                        value: level,
                        child: Text(_getLogLevelText(level)),
                      );
                    }).toList(),
                  ),
                ),
                
                const Divider(),
                
                // 系统信息
                ExpansionTile(
                  leading: const Icon(Icons.info_outlined),
                  title: const Text('设备信息'),
                  children: [
                    if (_deviceInfo != null) ...[
                      ..._deviceInfo!.entries.map((entry) => 
                        _buildInfoRow(entry.key, entry.value.toString())
                      ),
                    ] else ...[
                      const Padding(
                        padding: EdgeInsets.all(16),
                        child: CircularProgressIndicator(),
                      ),
                    ],
                  ],
                ),
                
                // 日志查看
                ExpansionTile(
                  leading: const Icon(Icons.notes_outlined),
                  title: const Text('应用日志'),
                  children: [
                    Container(
                      height: 200,
                      width: double.infinity,
                      margin: const EdgeInsets.all(8),
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: Colors.grey[300]!),
                      ),
                      child: SingleChildScrollView(
                        child: Text(
                          _logs,
                          style: const TextStyle(
                            fontFamily: 'monospace',
                            fontSize: 12,
                          ),
                        ),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(8),
                      child: Row(
                        children: [
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: _copyLogs,
                              icon: const Icon(Icons.copy),
                              label: const Text('复制日志'),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: _clearLogs,
                              icon: const Icon(Icons.clear),
                              label: const Text('清除日志'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                
                const Divider(),
                
                // 诊断工具
                Text(
                  '诊断工具',
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _runMemoryTest,
                        icon: const Icon(Icons.memory),
                        label: const Text('内存测试'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _runCacheTest,
                        icon: const Icon(Icons.cached),
                        label: const Text('缓存测试'),
                      ),
                    ),
                  ],
                ),
                
                const SizedBox(height: 8),
                
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _exportDebugInfo,
                        icon: const Icon(Icons.file_download),
                        label: const Text('导出诊断'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: _resetAppData,
                        icon: const Icon(Icons.restore),
                        label: const Text('重置应用'),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.red,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          SizedBox(
            width: 120,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }

  String _getLogLevelText(LogLevel level) {
    switch (level) {
      case LogLevel.Debug:
        return '调试';
      case LogLevel.Info:
        return '信息';
      case LogLevel.Warning:
        return '警告';
      case LogLevel.Error:
        return '错误';
      case LogLevel.None:
        return '关闭';
      case LogLevel.Verbose:
        return '详细';
    }
  }

  void _copyLogs() {
    Clipboard.setData(ClipboardData(text: _logs));
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('日志已复制到剪贴板')),
    );
  }

  void _clearLogs() {
    setState(() {
      _logs = '';
    });
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('日志已清除')),
    );
  }

  void _runMemoryTest() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('内存测试已开始...')),
    );
    // 实际实现中会运行内存诊断
  }

  void _runCacheTest() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('缓存测试已开始...')),
    );
    // 实际实现中会运行缓存诊断
  }

  void _exportDebugInfo() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('诊断信息导出功能将在后续版本中实现')),
    );
  }

  void _resetAppData() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('重置应用数据'),
        content: const Text(
          '此操作将删除所有应用数据，包括设置、阅读记录和缓存。'
          '此操作不可撤销，确定要继续吗？'
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              context.read<SettingsBloc>().add(ResetAppData());
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('应用数据重置完成，请重启应用')),
              );
            },
            child: const Text(
              '确定重置',
              style: TextStyle(color: Colors.red),
            ),
          ),
        ],
      ),
    );
  }
}