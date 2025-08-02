import 'dart:io';

import 'package:bloc_test/bloc_test.dart';
import 'package:easy_comic/core/services/avatar_manager.dart';
import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_bloc.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_event.dart';
import 'package:easy_comic/presentation/features/settings/webdav/bloc/webdav_state.dart';
import 'package:easy_comic/presentation/features/settings/webdav/widgets/user_profile_section.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';

import 'user_profile_section_test.mocks.dart';

class MockWebDAVBloc extends MockBloc<WebDAVEvent, WebDAVState> implements WebDAVBloc {}

@GenerateMocks([AvatarManager])
void main() {
  late MockWebDAVBloc mockWebDAVBloc;
  late MockAvatarManager mockAvatarManager;

  setUp(() {
    mockWebDAVBloc = MockWebDAVBloc();
    mockAvatarManager = MockAvatarManager();
    // Use reset and register for each test to ensure isolation
    sl.reset();
    sl.registerSingleton<AvatarManager>(mockAvatarManager);
  });

  Widget createWidgetUnderTest() {
    return MaterialApp(
      home: Scaffold(
        body: BlocProvider<WebDAVBloc>.value(
          value: mockWebDAVBloc,
          child: const UserProfileSection(),
        ),
      ),
    );
  }

  group('UserProfileSection', () {
    testWidgets('renders login button when state is WebDAVLoggedOut', (tester) async {
      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(const WebDAVLoggedOut()));
      when(mockWebDAVBloc.state).thenReturn(const WebDAVLoggedOut());

      await tester.pumpWidget(createWidgetUnderTest());

      expect(find.text('登录 WebDAV 账户'), findsOneWidget);
      expect(find.byType(CircleAvatar), findsNothing);
    });

    testWidgets('renders user info and logout button when state is WebDAVLoggedIn', (tester) async {
      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(const WebDAVLoggedIn(username: 'testuser')));
      when(mockWebDAVBloc.state).thenReturn(const WebDAVLoggedIn(username: 'testuser'));

      await tester.pumpWidget(createWidgetUnderTest());

      expect(find.text('testuser'), findsOneWidget);
      expect(find.byType(CircleAvatar), findsOneWidget);
      expect(find.text('登出'), findsOneWidget);
    });

    testWidgets('renders avatar image when avatarPath is provided', (tester) async {
      final avatarFile = File('test/assets/avatar.png');
      await avatarFile.create(recursive: true);
      
      final loggedInState = WebDAVLoggedIn(username: 'testuser', avatarPath: avatarFile.path);
      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(loggedInState));
      when(mockWebDAVBloc.state).thenReturn(loggedInState);

      await tester.pumpWidget(createWidgetUnderTest());

      expect(find.byType(CircleAvatar), findsOneWidget);
      final avatar = tester.widget<CircleAvatar>(find.byType(CircleAvatar));
      expect(avatar.backgroundImage, isA<FileImage>());

      await avatarFile.delete();
    });

    testWidgets('tapping login button dispatches LoginEvent', (tester) async {
      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(const WebDAVLoggedOut()));
      when(mockWebDAVBloc.state).thenReturn(const WebDAVLoggedOut());

      await tester.pumpWidget(createWidgetUnderTest());

      await tester.tap(find.text('登录 WebDAV 账户'));
      await tester.pump();

      verify(() => mockWebDAVBloc.add(any(that: isA<LoginEvent>()))).called(1);
    });

    testWidgets('tapping logout button dispatches LogoutEvent', (tester) async {
      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(const WebDAVLoggedIn(username: 'testuser')));
      when(mockWebDAVBloc.state).thenReturn(const WebDAVLoggedIn(username: 'testuser'));

      await tester.pumpWidget(createWidgetUnderTest());

      await tester.tap(find.text('登出'));
      await tester.pump();

      verify(() => mockWebDAVBloc.add(any(that: isA<LogoutEvent>()))).called(1);
    });

    testWidgets('tapping avatar triggers pickAndCropImage and dispatches UpdateAvatarEvent', (tester) async {
      final newAvatarFile = File('test/assets/new_avatar.png');
      await newAvatarFile.create(recursive: true);

      when(mockWebDAVBloc.stream).thenAnswer((_) => Stream.value(const WebDAVLoggedIn(username: 'testuser')));
      when(mockWebDAVBloc.state).thenReturn(const WebDAVLoggedIn(username: 'testuser'));
      when(() => mockAvatarManager.pickAndCropImage()).thenAnswer((_) async => newAvatarFile);

      await tester.pumpWidget(createWidgetUnderTest());

      await tester.tap(find.byType(CircleAvatar));
      await tester.pumpAndSettle();

      verify(() => mockAvatarManager.pickAndCropImage()).called(1);
      verify(() => mockWebDAVBloc.add(UpdateAvatarEvent(newAvatarFile.path))).called(1);

      await newAvatarFile.delete();
    });
  });
}