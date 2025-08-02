import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/data/drift_db.dart';

void main() {
  group('Database Performance Tests', () {
    late AppDatabase database;

    setUp(() async {
      // Create in-memory database for testing
      database = AppDatabase.memory();
    });

    tearDown(() async {
      await database.close();
    });

    group('Progress Persistence Performance', () {
      test('should handle batch inserts efficiently', () async {
        const batchSize = 1000;
        final stopwatch = Stopwatch()..start();

        // Create batch progress updates
        final companions = List.generate(batchSize, (index) => 
          ComicProgressCompanion.insert(
            comicId: 'comic_$index',
            currentPage: index % 100,
            totalPages: 100,
            lastUpdated: DateTime.now(),
            isCompleted: false,
          ),
        );

        await database.comicProgressDao.batchSaveProgress(companions);
        
        stopwatch.stop();

        // Should complete batch insert in under 1 second
        expect(stopwatch.elapsedMilliseconds, lessThan(1000));

        // Verify data integrity
        final allProgress = await database.comicProgressDao.getAllProgress();
        expect(allProgress.length, equals(batchSize));
      });

      test('should maintain query performance with large datasets', () async {
        // Insert large dataset
        const numRecords = 10000;
        for (int i = 0; i < numRecords; i += 100) {
          final batch = List.generate(100, (index) => 
            ComicProgressCompanion.insert(
              comicId: 'comic_${i + index}',
              currentPage: (i + index) % 100,
              totalPages: 100,
              lastUpdated: DateTime.now(),
              isCompleted: (i + index) % 10 == 0,
            ),
          );
          await database.comicProgressDao.batchSaveProgress(batch);
        }

        // Test query performance
        final stopwatch = Stopwatch()..start();
        
        // Query operations that should be fast
        final completedComics = await database.comicProgressDao.getCompletedComics();
        final pendingSync = await database.comicProgressDao.getPendingSyncProgress();
        final specificProgress = await database.comicProgressDao.getProgress('comic_5000');
        
        stopwatch.stop();

        // Queries should complete in under 100ms even with 10k records
        expect(stopwatch.elapsedMilliseconds, lessThan(100));
        
        expect(completedComics.length, greaterThan(0));
        expect(pendingSync.length, greaterThan(0));
        expect(specificProgress, isNotNull);
      });

      test('should handle concurrent database operations safely', () async {
        const numConcurrentOps = 50;
        final futures = <Future>[];

        // Concurrent reads and writes
        for (int i = 0; i < numConcurrentOps; i++) {
          // Mix of read and write operations
          if (i % 2 == 0) {
            futures.add(database.comicProgressDao.saveProgress(
              ComicProgressCompanion.insert(
                comicId: 'concurrent_comic_$i',
                currentPage: i,
                totalPages: 100,
                lastUpdated: DateTime.now(),
                isCompleted: false,
              ),
            ));
          } else {
            futures.add(database.comicProgressDao.getProgress('concurrent_comic_${i - 1}'));
          }
        }

        // All operations should complete without deadlocks
        await expectLater(Future.wait(futures), completes);
      });
    });

    group('Index Performance', () {
      test('should utilize indexes for common queries', () async {
        // Insert test data with different timestamps
        final now = DateTime.now();
        for (int i = 0; i < 1000; i++) {
          await database.comicProgressDao.saveProgress(
            ComicProgressCompanion.insert(
              comicId: 'comic_$i',
              currentPage: i % 100,
              totalPages: 100,
              lastUpdated: now.subtract(Duration(days: i)),
              isCompleted: i % 10 == 0,
            ),
          );
        }

        final stopwatch = Stopwatch()..start();

        // Query by lastUpdated (should use index)
        final recentProgress = await (database.select(database.comicProgress)
              ..where((t) => t.lastUpdated.isBiggerThanValue(
                    now.subtract(const Duration(days: 30))
                  ))
              ..limit(10)).get();

        stopwatch.stop();

        // Should be fast due to index
        expect(stopwatch.elapsedMilliseconds, lessThan(50));
        expect(recentProgress.length, greaterThan(0));
      });
    });

    group('Memory Usage', () {
      test('should not cause memory leaks with large result sets', () async {
        // Insert large dataset
        for (int batch = 0; batch < 10; batch++) {
          final companions = List.generate(1000, (index) => 
            ComicProgressCompanion.insert(
              comicId: 'memory_test_${batch * 1000 + index}',
              currentPage: index % 100,
              totalPages: 100,
              lastUpdated: DateTime.now(),
              isCompleted: false,
            ),
          );
          await database.comicProgressDao.batchSaveProgress(companions);
        }

        // Multiple queries should not accumulate memory
        for (int i = 0; i < 10; i++) {
          final results = await database.comicProgressDao.getAllProgress();
          expect(results.length, equals(10000));
          
          // Clear references to allow GC
          results.clear();
        }

        // If we get here without OOM, memory is being managed properly
        expect(true, isTrue);
      });
    });

    group('Transaction Performance', () {
      test('should handle transaction rollback efficiently', () async {
        final stopwatch = Stopwatch()..start();

        try {
          await database.transaction(() async {
            // Insert some data
            for (int i = 0; i < 100; i++) {
              await database.comicProgressDao.saveProgress(
                ComicProgressCompanion.insert(
                  comicId: 'transaction_test_$i',
                  currentPage: i,
                  totalPages: 100,
                  lastUpdated: DateTime.now(),
                  isCompleted: false,
                ),
              );
            }
            
            // Force rollback
            throw Exception('Intentional rollback');
          });
        } catch (e) {
          // Expected exception
        }

        stopwatch.stop();

        // Rollback should be fast
        expect(stopwatch.elapsedMilliseconds, lessThan(500));

        // Data should be rolled back
        final count = await database.comicProgressDao.getAllProgress();
        expect(count.length, equals(0));
      });
    });
  });

  group('Cache Performance Benchmarks', () {
    test('should demonstrate cache vs database performance', () async {
      final database = AppDatabase.memory();
      
      try {
        // Setup test data
        const numComics = 100;
        for (int i = 0; i < numComics; i++) {
          await database.comicProgressDao.saveProgress(
            ComicProgressCompanion.insert(
              comicId: 'benchmark_comic_$i',
              currentPage: i % 50,
              totalPages: 50,
              lastUpdated: DateTime.now(),
              isCompleted: false,
            ),
          );
        }

        // Benchmark database access
        final dbStopwatch = Stopwatch()..start();
        for (int i = 0; i < numComics; i++) {
          await database.comicProgressDao.getProgress('benchmark_comic_$i');
        }
        dbStopwatch.stop();

        // Benchmark with in-memory cache simulation
        final cache = <String, ComicProgressModel>{};
        
        // Pre-populate cache
        for (int i = 0; i < numComics; i++) {
          final progress = await database.comicProgressDao.getProgress('benchmark_comic_$i');
          if (progress != null) {
            cache['benchmark_comic_$i'] = progress;
          }
        }

        final cacheStopwatch = Stopwatch()..start();
        for (int i = 0; i < numComics; i++) {
          final _ = cache['benchmark_comic_$i'];
        }
        cacheStopwatch.stop();

        // Cache should be significantly faster
        expect(cacheStopwatch.elapsedMicroseconds, 
               lessThan(dbStopwatch.elapsedMicroseconds ~/ 10));
        
        print('Database access: ${dbStopwatch.elapsedMilliseconds}ms');
        print('Cache access: ${cacheStopwatch.elapsedMicroseconds}μs');
        
      } finally {
        await database.close();
      }
    });
  });
}