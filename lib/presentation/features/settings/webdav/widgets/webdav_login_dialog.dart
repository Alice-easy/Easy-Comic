import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_event.dart';

class WebDavLoginDialog extends StatefulWidget {
  const WebDavLoginDialog({super.key});

  @override
  State<WebDavLoginDialog> createState() => _WebDavLoginDialogState();
}

class _WebDavLoginDialogState extends State<WebDavLoginDialog> {
  final _formKey = GlobalKey<FormState>();
  final _serverController = TextEditingController();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void dispose() {
    _serverController.dispose();
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _login() {
    if (_formKey.currentState!.validate()) {
      context.read<WebDAVBloc>().add(
            LoginEvent(
              serverUrl: _serverController.text,
              username: _usernameController.text,
              password: _passwordController.text,
            ),
          );
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('WebDAV 登录'),
      content: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextFormField(
              controller: _serverController,
              decoration: const InputDecoration(
                labelText: '服务器地址',
                hintText: 'https://dav.example.com',
              ),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return '请输入服务器地址';
                }
                if (!Uri.tryParse(value)!.isAbsolute) {
                  return '请输入有效的 URL';
                }
                return null;
              },
            ),
            TextFormField(
              controller: _usernameController,
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
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('取消'),
        ),
        ElevatedButton(
          onPressed: _login,
          child: const Text('登录'),
        ),
      ],
    );
  }
}