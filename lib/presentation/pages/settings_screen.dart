import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/settings/theme/bloc/theme_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_event.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(create: (context) => sl<WebDAVBloc>()),
        // ThemeBloc is already provided by MyApp
      ],
      child: Scaffold(
        appBar: AppBar(
          title: const Text('设置'),
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              _buildAppearanceSettings(context),
              const SizedBox(height: 24),
              _buildWebDAVSettings(context),
              const SizedBox(height: 24),
              _buildAboutSettings(context),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAppearanceSettings(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('外观', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 16),
            BlocBuilder<ThemeBloc, ThemeState>(
              builder: (context, state) {
                return ListTile(
                  title: const Text('主题模式'),
                  subtitle: Text(_themeModeToString(state.themeMode)),
                  trailing: PopupMenuButton<ThemeMode>(
                    onSelected: (ThemeMode mode) {
                      context.read<ThemeBloc>().add(ThemeModeChanged(mode));
                    },
                    itemBuilder: (BuildContext context) =>
                        <PopupMenuEntry<ThemeMode>>[
                      const PopupMenuItem<ThemeMode>(
                        value: ThemeMode.system,
                        child: Text('跟随系统'),
                      ),
                      const PopupMenuItem<ThemeMode>(
                        value: ThemeMode.light,
                        child: Text('亮色模式'),
                      ),
                      const PopupMenuItem<ThemeMode>(
                        value: ThemeMode.dark,
                        child: Text('暗色模式'),
                      ),
                    ],
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  String _themeModeToString(ThemeMode mode) {
    switch (mode) {
      case ThemeMode.system:
        return '跟随系统';
      case ThemeMode.light:
        return '亮色模式';
      case ThemeMode.dark:
        return '暗色模式';
    }
  }

  Widget _buildWebDAVSettings(BuildContext context) {
    return BlocProvider<WebDAVBloc>.value(
      value: BlocProvider.of<WebDAVBloc>(context),
      child: const WebDAVSettingsForm(),
    );
  }

  Widget _buildAboutSettings(BuildContext context) {
    // In a real app, you'd get this from package_info
    const appVersion = '1.0.0';
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('关于', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 16),
            const ListTile(
              title: Text('版本'),
              subtitle: Text(appVersion),
            ),
          ],
        ),
      ),
    );
  }
}

class WebDAVSettingsForm extends StatefulWidget {
  const WebDAVSettingsForm({super.key});

  @override
  _WebDAVSettingsFormState createState() => _WebDAVSettingsFormState();
}

class _WebDAVSettingsFormState extends State<WebDAVSettingsForm> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _uriController;
  late final TextEditingController _usernameController;
  late final TextEditingController _passwordController;

  @override
  void initState() {
    super.initState();
    _uriController = TextEditingController();
    _usernameController = TextEditingController();
    _passwordController = TextEditingController();
    _loadInitialConfig();
  }

  Future<void> _loadInitialConfig() async {
    final settingsRepo = sl<SettingsRepository>();
    final config = await settingsRepo.getWebDAVConfig();
    if (mounted) {
      _uriController.text = config.uri;
      _usernameController.text = config.username;
      _passwordController.text = config.password;
    }
  }

  @override
  void dispose() {
    _uriController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: BlocConsumer<WebDAVBloc, WebDAVState>(
          listener: (context, state) {
            if (state is WebDAVSuccess) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                    content: Text(state.message),
                    backgroundColor: Colors.green),
              );
            } else if (state is WebDAVFailure) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                    content: Text(state.message), backgroundColor: Colors.red),
              );
            } else if (state is WebDAVInProgress) {
              // Optional: show a loading indicator
            }
          },
          builder: (context, state) {
            final isInProgress = state is WebDAVInProgress;
            return Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Text('备份与同步',
                      style: Theme.of(context).textTheme.headlineSmall),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _uriController,
                    decoration:
                        const InputDecoration(labelText: '服务器地址 (URI)'),
                    validator: (value) {
                      if (value == null || value.isEmpty) {
                        return '请输入服务器地址';
                      }
                      return null;
                    },
                  ),
                  TextFormField(
                    controller: _usernameController,
                    decoration: const InputDecoration(labelText: '用户名'),
                  ),
                  TextFormField(
                    controller: _passwordController,
                    decoration: const InputDecoration(labelText: '密码'),
                    obscureText: true,
                  ),
                  const SizedBox(height: 24),
                  ElevatedButton(
                    onPressed:
                        isInProgress ? null : () => _saveConfig(context),
                    child: const Text('保存配置'),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: ElevatedButton(
                          onPressed: isInProgress ? null : () => _backup(context),
                          child: isInProgress && state.operation == WebDAVOperation.backup
                              ? const SizedBox(
                                  height: 20,
                                  width: 20,
                                  child: CircularProgressIndicator(strokeWidth: 2))
                              : const Text('立即备份'),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: ElevatedButton(
                          onPressed: isInProgress ? null : () => _restore(context),
                          child: isInProgress && state.operation == WebDAVOperation.restore
                              ? const SizedBox(
                                  height: 20,
                                  width: 20,
                                  child: CircularProgressIndicator(strokeWidth: 2))
                              : const Text('立即恢复'),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }

  void _saveConfig(BuildContext context) async {
    if (_formKey.currentState!.validate()) {
      final config = WebDAVConfig(
        uri: _uriController.text,
        username: _usernameController.text,
        password: _passwordController.text,
      );
      final settingsRepo = sl<SettingsRepository>();
      await settingsRepo.saveWebDAVConfig(config);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('配置已保存'), backgroundColor: Colors.green),
        );
      }
    }
  }

  void _backup(BuildContext context) {
    if (_formKey.currentState!.validate()) {
      _saveConfig(context); // Save before backup
      context.read<WebDAVBloc>().add(BackupDataEvent());
    }
  }

  void _restore(BuildContext context) {
    if (_formKey.currentState!.validate()) {
      _saveConfig(context); // Save before restore
      context.read<WebDAVBloc>().add(RestoreDataEvent());
    }
  }
}