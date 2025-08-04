import 'package:easy_comic/domain/usecases/webdav/sync_data_usecase.dart';
import 'package:easy_comic/presentation/features/home/home_screen.dart';
import 'package:easy_comic/presentation/features/settings/bloc/theme/theme_bloc.dart';
import 'package:flutter/material.dart';
import 'package:easy_comic/core/di/injection_container.dart' as di;
import 'package:flutter_bloc/flutter_bloc.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await di.init();
  // Don't await this, let it run in the background
  // di.sl<SyncDataUseCase>().call(SyncDataParams());
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => di.sl<ThemeBloc>()..add(ThemeLoadStarted()),
      child: BlocBuilder<ThemeBloc, ThemeState>(
        builder: (context, state) {
          return MaterialApp(
            title: 'Easy Comic',
            theme: ThemeData.light(useMaterial3: true),
            darkTheme: ThemeData.dark(useMaterial3: true),
            themeMode: state.themeMode,
            home: const HomeScreen(),
          );
        },
      ),
    );
  }
}