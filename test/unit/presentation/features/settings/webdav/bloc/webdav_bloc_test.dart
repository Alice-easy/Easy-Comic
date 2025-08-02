import 'dart:io';

import 'package:bloc_test/bloc_test.dart';
import 'package:easy_comic/core/services/avatar_manager.dart';
import 'package:easy_comic/core/services/logging_service.dart';
import 'package:easy_comic/domain/repositories/settings_repository.dart';
import 'package:easy_comic/domain/usecases/backup_data_to_webdav_usecase.dart';
import 'package:easy_comic/domain/usecases/restore_data_from_webdav_usecase.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_event.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_state.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';

import 'webdav_bloc_test.mocks.dart';

@GenerateMocks([
  BackupDataToWebdavUseCase,
  RestoreDataFromWebdavUseCase,
  SettingsRepository,
  LoggingService,
  AvatarManager,
])
void main() {
  late WebDAVBloc webDavBloc;
  late MockBackupDataToWebdavUseCase mockBackupDataToWebdavUseCase;
  late MockRestoreDataFromWebdavUseCase mockRestoreDataFromWebdavUseCase;
  late MockSettingsRepository mockSettingsRepository;
  late MockLoggingService mockLoggingService;
  late MockAvatarManager mockAvatarManager;

  setUp(() {
    mockBackupDataToWebdavUseCase = MockBackupDataToWebdavUseCase();
    mockRestoreDataFromWebdavUseCase = MockRestoreDataFromWebdavUseCase();
    mockSettingsRepository = MockSettingsRepository();
    mockLoggingService = MockLoggingService();
    mockAvatarManager = MockAvatarManager();
    webDavBloc = WebDAVBloc(
      backupDataToWebdavUseCase: mockBackupDataToWebdavUseCase,
      restoreDataFromWebdavUseCase: mockRestoreDataFromWebdavUseCase,
      settingsRepository: mockSettingsRepository,
      loggingService: mockLoggingService,
      avatarManager: mockAvatarManager,
    );
  });

  tearDown(() {
    webDavBloc.close();
  });

  test('initial state is WebDAVInitial', () {
    expect(webDavBloc.state, WebDAVInitial());
  });

  group('LoginEvent', () {
    blocTest<WebDAVBloc, WebDAVState>(
      'emits [WebDAVInProgress, WebDAVLoggedIn] when login is successful',
      build: () => webDavBloc,
      act: (bloc) => bloc.add(const LoginEvent(username: 'testuser', password: 'password')),
      wait: const Duration(seconds: 1),
      expect: () => [
        const WebDAVInProgress(WebDAVOperation.login),
        const WebDAVLoggedIn(username: 'testuser'),
      ],
    );
  });

  group('LogoutEvent', () {
    blocTest<WebDAVBloc, WebDAVState>(
      'emits [WebDAVInProgress, WebDAVLoggedOut] when logout is successful',
      build: () {
        // Start with a logged in state
        webDavBloc.emit(const WebDAVLoggedIn(username: 'testuser'));
        return webDavBloc;
      },
      act: (bloc) => bloc.add(LogoutEvent()),
      wait: const Duration(seconds: 1),
      expect: () => [
        const WebDAVInProgress(WebDAVOperation.logout),
        const WebDAVLoggedOut(),
      ],
    );
  });

  group('UpdateAvatarEvent', () {
    const newAvatarPath = '/path/to/new/avatar.jpg';
    final newAvatarFile = File(newAvatarPath);

    blocTest<WebDAVBloc, WebDAVState>(
      'emits [WebDAVLoggedIn] with new avatar path when update is successful',
      build: () {
        webDavBloc.emit(const WebDAVLoggedIn(username: 'testuser'));
        return webDavBloc;
      },
      act: (bloc) => bloc.add(const UpdateAvatarEvent(newAvatarPath)),
      expect: () => [
        const WebDAVLoggedIn(username: 'testuser', avatarPath: newAvatarPath),
      ],
    );

    blocTest<WebDAVBloc, WebDAVState>(
      'emits [WebDAVFailure] when update fails',
      build: () {
        webDavBloc.emit(const WebDAVLoggedIn(username: 'testuser', avatarPath: 'old/path'));
        // This is a simplified test. A more realistic scenario would involve mocking the avatarManager to throw an exception.
        // However, the current BLoC implementation doesn't actually call the manager, so we'll test the state logic.
        // To properly test this, the BLoC's _onUpdateAvatar should interact with avatarManager.
        // For now, we will simulate a failure by having the BLoC emit a failure state.
        // This test will fail initially, prompting us to fix the BLoC.
        
        // Let's assume the BLoC is modified to catch exceptions.
        // We can't directly cause an exception here without changing the BLoC,
        // so we'll add a test case that expects a failure state, which will drive
        // the change in the BLoC to handle errors.
        
        // The current BLoC implementation will pass the "successful" test but has no failure path.
        // Let's add a test that forces us to implement it.
        // Since we can't mock a method call that doesn't exist, we'll add the test and then modify the BLoC.
        return webDavBloc;
      },
      act: (bloc) {
        // To test the failure case, we need to modify the BLoC to actually have a failure path.
        // Let's assume we've modified the BLoC to look like this:
        /*
          Future<void> _onUpdateAvatar(UpdateAvatarEvent event, Emitter<WebDAVState> emit) async {
            if (state is WebDAVLoggedIn) {
              final currentState = state as WebDAVLoggedIn;
              try {
                // final newPath = await avatarManager.saveImage(event.newAvatarPath); // hypothetical
                // emit(WebDAVLoggedIn(username: currentState.username, avatarPath: newPath));
                if (event.newAvatarPath.isEmpty) { // Simulate a failure condition
                  throw Exception('Invalid path');
                }
                emit(WebDAVLoggedIn(username: currentState.username, avatarPath: event.newAvatarPath));
              } catch (e) {
                emit(WebDAVFailure("Failed to update avatar.", status: currentState.status, username: currentState.username, avatarPath: currentState.avatarPath));
              }
            }
          }
        */
        // With the above hypothetical change, this test would be valid.
        bloc.add(const UpdateAvatarEvent('')); // Simulate failure with an empty path
      },
      expect: () => [
        isA<WebDAVFailure>(),
      ],
      // This test will likely fail until the BLoC is updated to handle errors.
    );
  });
}