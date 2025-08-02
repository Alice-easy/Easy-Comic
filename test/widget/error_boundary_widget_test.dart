import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:easy_comic/presentation/widgets/error_boundary_widget.dart';

void main() {
  group('ErrorBoundaryWidget', () {
    testWidgets('should display child widget when no error occurs', (tester) async {
      const testWidget = Text('Test Content');
      
      await tester.pumpWidget(
        const MaterialApp(
          home: ErrorBoundaryWidget(
            child: testWidget,
          ),
        ),
      );

      expect(find.text('Test Content'), findsOneWidget);
      expect(find.byType(ErrorDisplay), findsNothing);
    });

    testWidgets('should catch and display errors from child widgets', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: ErrorBoundaryWidget(
            child: Builder(
              builder: (context) {
                throw Exception('Test error');
              },
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.byType(ErrorDisplay), findsOneWidget);
      expect(find.textContaining('something went wrong'), findsOneWidget);
    });

    testWidgets('should show retry button and allow error recovery', (tester) async {
      int buildCount = 0;
      
      await tester.pumpWidget(
        MaterialApp(
          home: ErrorBoundaryWidget(
            child: Builder(
              builder: (context) {
                buildCount++;
                if (buildCount == 1) {
                  throw Exception('Test error');
                }
                return const Text('Recovered');
              },
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      // Should show error
      expect(find.byType(ErrorDisplay), findsOneWidget);
      expect(find.textContaining('Retry'), findsOneWidget);

      // Tap retry button
      await tester.tap(find.textContaining('Retry'));
      await tester.pumpAndSettle();

      // Should show recovered content
      expect(find.text('Recovered'), findsOneWidget);
      expect(find.byType(ErrorDisplay), findsNothing);
    });

    testWidgets('should handle custom error handler', (tester) async {
      String? capturedError;
      
      await tester.pumpWidget(
        MaterialApp(
          home: ErrorBoundaryWidget(
            onError: (error, stackTrace) {
              capturedError = error.toString();
            },
            child: Builder(
              builder: (context) {
                throw Exception('Custom error');
              },
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(capturedError, contains('Custom error'));
      expect(find.byType(ErrorDisplay), findsOneWidget);
    });

    testWidgets('should display custom error message', (tester) async {
      const customMessage = 'Custom error message for users';
      
      await tester.pumpWidget(
        MaterialApp(
          home: ErrorBoundaryWidget(
            errorMessage: customMessage,
            child: Builder(
              builder: (context) {
                throw Exception('Internal error');
              },
            ),
          ),
        ),
      );

      await tester.pumpAndSettle();

      expect(find.text(customMessage), findsOneWidget);
    });

    testWidgets('should handle async errors in child widgets', (tester) async {
      final completer = Completer<void>();
      
      await tester.pumpWidget(
        MaterialApp(
          home: ErrorBoundaryWidget(
            child: FutureBuilder<void>(
              future: completer.future,
              builder: (context, snapshot) {
                if (snapshot.hasError) {
                  throw snapshot.error!;
                }
                return const Text('Loading...');
              },
            ),
          ),
        ),
      );

      expect(find.text('Loading...'), findsOneWidget);

      // Trigger async error
      completer.completeError(Exception('Async error'));
      await tester.pumpAndSettle();

      expect(find.byType(ErrorDisplay), findsOneWidget);
    });

    group('Error Display Component', () {
      testWidgets('should show error icon and message', (tester) async {
        await tester.pumpWidget(
          const MaterialApp(
            home: ErrorDisplay(
              message: 'Test error message',
            ),
          ),
        );

        expect(find.byIcon(Icons.error_outline), findsOneWidget);
        expect(find.text('Test error message'), findsOneWidget);
      });

      testWidgets('should show retry button when onRetry is provided', (tester) async {
        bool retryPressed = false;
        
        await tester.pumpWidget(
          MaterialApp(
            home: ErrorDisplay(
              message: 'Test error',
              onRetry: () {
                retryPressed = true;
              },
            ),
          ),
        );

        expect(find.textContaining('Retry'), findsOneWidget);
        
        await tester.tap(find.textContaining('Retry'));
        expect(retryPressed, isTrue);
      });

      testWidgets('should not show retry button when onRetry is null', (tester) async {
        await tester.pumpWidget(
          const MaterialApp(
            home: ErrorDisplay(
              message: 'Test error',
            ),
          ),
        );

        expect(find.textContaining('Retry'), findsNothing);
      });

      testWidgets('should be accessible', (tester) async {
        await tester.pumpWidget(
          MaterialApp(
            home: ErrorDisplay(
              message: 'Test error message',
              onRetry: () {},
            ),
          ),
        );

        // Check for semantic labels
        expect(find.bySemanticsLabel('Error'), findsOneWidget);
        expect(find.bySemanticsLabel('Retry'), findsOneWidget);
      });
    });

    group('Error Recovery', () {
      testWidgets('should reset error state when child changes', (tester) async {
        bool showError = true;
        
        await tester.pumpWidget(
          MaterialApp(
            home: StatefulBuilder(
              builder: (context, setState) {
                return Column(
                  children: [
                    ErrorBoundaryWidget(
                      child: showError
                          ? Builder(
                              builder: (context) => throw Exception('Error'),
                            )
                          : const Text('No Error'),
                    ),
                    ElevatedButton(
                      onPressed: () {
                        setState(() {
                          showError = false;
                        });
                      },
                      child: const Text('Fix Error'),
                    ),
                  ],
                );
              },
            ),
          ),
        );

        await tester.pumpAndSettle();

        // Should show error initially
        expect(find.byType(ErrorDisplay), findsOneWidget);

        // Fix the error
        await tester.tap(find.text('Fix Error'));
        await tester.pumpAndSettle();

        // Should show fixed content
        expect(find.text('No Error'), findsOneWidget);
        expect(find.byType(ErrorDisplay), findsNothing);
      });

      testWidgets('should handle multiple error/recovery cycles', (tester) async {
        int errorCount = 0;
        
        await tester.pumpWidget(
          MaterialApp(
            home: StatefulBuilder(
              builder: (context, setState) {
                return ErrorBoundaryWidget(
                  child: Column(
                    children: [
                      Builder(
                        builder: (context) {
                          if (errorCount % 2 == 1) {
                            throw Exception('Error $errorCount');
                          }
                          return Text('Success $errorCount');
                        },
                      ),
                      ElevatedButton(
                        onPressed: () {
                          setState(() {
                            errorCount++;
                          });
                        },
                        child: const Text('Toggle'),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
        );

        // Initial success state
        expect(find.text('Success 0'), findsOneWidget);

        // Trigger error
        await tester.tap(find.text('Toggle'));
        await tester.pumpAndSettle();
        expect(find.byType(ErrorDisplay), findsOneWidget);

        // Recover
        await tester.tap(find.text('Toggle'));
        await tester.pumpAndSettle();
        expect(find.text('Success 2'), findsOneWidget);

        // Error again
        await tester.tap(find.text('Toggle'));
        await tester.pumpAndSettle();
        expect(find.byType(ErrorDisplay), findsOneWidget);
      });
    });

    group('Performance', () {
      testWidgets('should not impact performance when no errors occur', (tester) async {
        const numWidgets = 100;
        
        await tester.pumpWidget(
          MaterialApp(
            home: Column(
              children: List.generate(
                numWidgets,
                (index) => ErrorBoundaryWidget(
                  child: Text('Widget $index'),
                ),
              ),
            ),
          ),
        );

        // Should render all widgets without performance issues
        for (int i = 0; i < numWidgets; i++) {
          expect(find.text('Widget $i'), findsOneWidget);
        }
      });

      testWidgets('should isolate errors to affected boundaries only', (tester) async {
        await tester.pumpWidget(
          MaterialApp(
            home: Column(
              children: [
                ErrorBoundaryWidget(
                  child: Builder(
                    builder: (context) => throw Exception('Error in first'),
                  ),
                ),
                ErrorBoundaryWidget(
                  child: const Text('Second widget OK'),
                ),
                ErrorBoundaryWidget(
                  child: const Text('Third widget OK'),
                ),
              ],
            ),
          ),
        );

        await tester.pumpAndSettle();

        // Only first widget should show error
        expect(find.byType(ErrorDisplay), findsOneWidget);
        expect(find.text('Second widget OK'), findsOneWidget);
        expect(find.text('Third widget OK'), findsOneWidget);
      });
    });

    group('Edge Cases', () {
      testWidgets('should handle errors during error display', (tester) async {
        await tester.pumpWidget(
          MaterialApp(
            home: ErrorBoundaryWidget(
              child: Builder(
                builder: (context) => throw Exception('Primary error'),
              ),
              errorBuilder: (context, error, onRetry) {
                throw Exception('Error in error display');
              },
            ),
          ),
        );

        await tester.pumpAndSettle();

        // Should fall back to default error display
        expect(find.byType(ErrorDisplay), findsOneWidget);
      });

      testWidgets('should handle null or empty error messages', (tester) async {
        await tester.pumpWidget(
          MaterialApp(
            home: ErrorBoundaryWidget(
              child: Builder(
                builder: (context) => throw Exception(''),
              ),
            ),
          ),
        );

        await tester.pumpAndSettle();

        // Should show default error message
        expect(find.byType(ErrorDisplay), findsOneWidget);
        expect(find.textContaining('something went wrong'), findsOneWidget);
      });
    });
  });
}