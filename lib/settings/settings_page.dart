import 'package:easy_comic/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsPage extends ConsumerStatefulWidget {
  const SettingsPage({super.key});

  @override
  ConsumerState<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends ConsumerState<SettingsPage> {
  final _formKey = GlobalKey<FormState>();
  final _hostController = TextEditingController();
  final _userController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    _hostController.text = prefs.getString('webdav_host') ?? '';
    _userController.text = prefs.getString('webdav_user') ?? '';
    _passwordController.text = prefs.getString('webdav_password') ?? '';
  }

  Future<void> _saveSettings() async {
    if (_formKey.currentState!.validate()) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('webdav_host', _hostController.text);
      await prefs.setString('webdav_user', _userController.text);
      await prefs.setString('webdav_password', _passwordController.text);
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('设置已保存')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final settings = ref.watch(settingsStoreProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('设置')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              DropdownButtonFormField<ThemeMode>(
                value: settings.themeMode,
                decoration: const InputDecoration(labelText: '主题'),
                items: const [
                  DropdownMenuItem(
                    value: ThemeMode.system,
                    child: Text('跟随系统'),
                  ),
                  DropdownMenuItem(value: ThemeMode.light, child: Text('亮色模式')),
                  DropdownMenuItem(value: ThemeMode.dark, child: Text('暗色模式')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    ref
                        .read(settingsStoreProvider.notifier)
                        .setThemeMode(value);
                  }
                },
              ),
              TextFormField(
                controller: _hostController,
                decoration: const InputDecoration(labelText: '主机'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入主机地址';
                  }
                  return null;
                },
              ),
              TextFormField(
                controller: _userController,
                decoration: const InputDecoration(labelText: '用户名'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入用户名';
                  }
                  return null;
                },
              ),
              TextFormField(
                controller: _passwordController,
                decoration: const InputDecoration(labelText: '密码'),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '请输入密码';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 20),
              ElevatedButton(onPressed: _saveSettings, child: const Text('保存')),
            ],
          ),
        ),
      ),
    );
  }
}
