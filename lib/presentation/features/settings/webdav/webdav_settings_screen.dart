import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/widgets/loading_overlay.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class WebdavSettingsScreen extends StatelessWidget {
  const WebdavSettingsScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final _formKey = GlobalKey<FormState>();
    final _serverUrlController = TextEditingController();
    final _usernameController = TextEditingController();
    final _passwordController = TextEditingController();

    return Scaffold(
      appBar: AppBar(
        title: const Text('WebDAV Settings'),
      ),
      body: BlocConsumer<WebdavBloc, WebdavState>(
        listener: (context, state) {
          if (state.status == WebdavStatus.failure) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Error: ${state.failureMessage ?? 'An unknown error occurred'}'),
                backgroundColor: Colors.red,
              ),
            );
          } else if (state.status == WebdavStatus.success) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Operation successful!'),
                backgroundColor: Colors.green,
              ),
            );
          } else if (state.status == WebdavStatus.syncing) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Syncing...')),
            );
          }
        },
        builder: (context, state) {
          final isLoading = state.status == WebdavStatus.loading || state.status == WebdavStatus.syncing;
          String message = 'Loading...';
          if (state.status == WebdavStatus.syncing) {
            message = 'Syncing...';
          }

          Widget body;
          if (state.config != null) {
            body = Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('Connected to ${state.config!.serverUrl}'),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: () {
                      context.read<WebdavBloc>().add(SyncNow());
                    },
                    child: const Text('Sync Now'),
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: () {
                      context.read<WebdavBloc>().add(LogoutButtonPressed());
                    },
                    child: const Text('Logout'),
                  ),
                ],
              ),
            );
          } else {
            body = Padding(
              padding: const EdgeInsets.all(16.0),
              child: Form(
                key: _formKey,
                child: Column(
                  children: [
                    TextFormField(
                      controller: _serverUrlController,
                      decoration: const InputDecoration(labelText: 'Server URL'),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a server URL';
                        }
                        return null;
                      },
                    ),
                    TextFormField(
                      controller: _usernameController,
                      decoration: const InputDecoration(labelText: 'Username'),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a username';
                        }
                        return null;
                      },
                    ),
                    TextFormField(
                      controller: _passwordController,
                      decoration: const InputDecoration(labelText: 'Password'),
                      obscureText: true,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a password';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 20),
                    ElevatedButton(
                      onPressed: () {
                        if (_formKey.currentState!.validate()) {
                          context.read<WebdavBloc>().add(LoginButtonPressed(
                                serverUrl: _serverUrlController.text,
                                username: _usernameController.text,
                                password: _passwordController.text,
                              ));
                        }
                      },
                      child: const Text('Login'),
                    ),
                  ],
                ),
              ),
            );
          }

          return LoadingOverlay(
            isLoading: isLoading,
            message: message,
            child: body,
          );
        },
      ),
    );
  }
}