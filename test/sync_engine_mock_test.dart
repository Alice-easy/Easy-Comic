import 'package:drift/native.dart';
import 'package:easy_comic/core/sync_engine.dart';
import 'package:easy_comic/core/webdav_service.dart';
import 'package:easy_comic/data/drift_db.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';

// 生成 Mock 类
@GenerateMocks([WebDAVService])
import 'sync_engine_mock_test.mocks.dart';

void main() {
  late DriftDb database;
  late MockWebDAVService mockWebDAVService;
  late SyncEngine syncEngine;

  setUp(() async {
    // 创建内存数据库用于测试
    database = DriftDb.withExecutor(NativeDatabase.memory());

    // 创建 Mock WebDAV 服务
    mockWebDAVService = MockWebDAVService();

    // 初始化同步引擎
    syncEngine = SyncEngine(
      db: database,
      webdavService: mockWebDAVService,
    );
  });

  tearDown(() async {
    await database.close();
  });

  group('SyncEngine Tests', () {
    test('should upload local data when remote is empty', () async {
      // 准备测试数据
      await database.upsertProgress('test_file_hash', 5, 10);

      // Mock WebDAV 行为
      when(
        mockWebDAVService.mkdir('/comic_progress/'),
      ).thenAnswer((_) async {});
      when(
        mockWebDAVService.listDir('/comic_progress/'),
      ).thenAnswer((_) async => []);
      when(mockWebDAVService.upload(any, any)).thenAnswer((_) async {});
      when(mockWebDAVService.getFileInfo(any)).thenAnswer(
        (_) async => WebDAVFileInfo(
          name: 'test_file_hash.json',
          path: '/comic_progress/test_file_hash.json',
          isDirectory: false,
          etag: 'mock_etag_123',
        ),
      );

      // 执行同步
      final result = await syncEngine.sync();

      // 验证结果
      expect(result.uploaded, equals(1));
      expect(result.downloaded, equals(0));
      expect(result.conflicts, equals(0));
      expect(result.errors, isEmpty);

      // 验证 upload 被调用了一次
      verify(mockWebDAVService.upload(any, any)).called(1);
    });
  });
}
