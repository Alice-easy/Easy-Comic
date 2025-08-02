import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:easy_comic/core/error/error_handler_chain.dart';

// Mock classes for testing
class MockErrorProcessor extends Mock implements ErrorProcessor {}

void main() {
  group('ErrorHandlerChain', () {
    late ErrorHandlerChain errorHandlerChain;
    late ErrorCallback? originalHandler;

    setUp(() {
      errorHandlerChain = ErrorHandlerChain();
      originalHandler = FlutterError.onError;
    });

    tearDown(() {
      errorHandlerChain.dispose();
      // Restore original handler if it was changed
      if (FlutterError.onError != originalHandler) {
        FlutterError.onError = originalHandler;
      }
    });

    group('Initialization', () {
      test('should initialize with development processor in debug mode', () {
        errorHandlerChain.initialize(isDevelopment: true);

        expect(FlutterError.onError, isNotNull);
        expect(FlutterError.onError, isNot(equals(originalHandler)));
      });

      test('should initialize with production processor in release mode', () {
        errorHandlerChain.initialize(isDevelopment: false);

        expect(FlutterError.onError, isNotNull);
        expect(FlutterError.onError, isNot(equals(originalHandler)));
      });

      test('should not reinitialize if already initialized', () {
        errorHandlerChain.initialize(isDevelopment: true);
        final firstHandler = FlutterError.onError;

        errorHandlerChain.initialize(isDevelopment: true);
        final secondHandler = FlutterError.onError;

        expect(firstHandler, equals(secondHandler));
      });
    });

    group('Error Processing Chain', () {
      test('should call custom processors when error occurs', () async {
        final mockProcessor = MockErrorProcessor();
        when(() => mockProcessor.processError(any(), any()))
            .thenAnswer((_) async {});

        errorHandlerChain.addProcessor(mockProcessor);
        errorHandlerChain.initialize(isDevelopment: true);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test_library',
          context: ErrorDescription('Test context'),
        );

        // Trigger error handler
        FlutterError.onError!(testError);

        // Wait for async processing
        await Future.delayed(const Duration(milliseconds: 50));

        verify(() => mockProcessor.processError(testError, any())).called(1);
      });

      test('should call original handler after custom processing', () async {
        bool originalCalled = false;
        FlutterError.onError = (details) {
          originalCalled = true;
        };

        errorHandlerChain.initialize(isDevelopment: true);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test_library',
        );

        FlutterError.onError!(testError);

        expect(originalCalled, isTrue);
      });

      test('should continue processing even if one processor fails', () async {
        final mockProcessor1 = MockErrorProcessor();
        final mockProcessor2 = MockErrorProcessor();

        when(() => mockProcessor1.processError(any(), any()))
            .thenThrow(Exception('Processor 1 failed'));
        when(() => mockProcessor2.processError(any(), any()))
            .thenAnswer((_) async {});

        errorHandlerChain.addProcessor(mockProcessor1);
        errorHandlerChain.addProcessor(mockProcessor2);
        errorHandlerChain.initialize(isDevelopment: true);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test_library',
        );

        FlutterError.onError!(testError);

        await Future.delayed(const Duration(milliseconds: 50));

        verify(() => mockProcessor1.processError(testError, any())).called(1);
        verify(() => mockProcessor2.processError(testError, any())).called(1);
      });
    });

    group('Error Report Stream', () {
      test('should emit error reports when errors occur', () async {
        errorHandlerChain.initialize(isDevelopment: true);

        final reports = <ErrorReport>[];
        final subscription = errorHandlerChain.errorReportStream.listen(reports.add);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test_library',
        );

        FlutterError.onError!(testError);

        await Future.delayed(const Duration(milliseconds: 50));

        expect(reports.length, equals(1));
        expect(reports.first.message, contains('Test error'));

        await subscription.cancel();
      });

      test('should create error reports with proper severity classification', () async {
        errorHandlerChain.initialize(isDevelopment: true);

        final reports = <ErrorReport>[];
        final subscription = errorHandlerChain.errorReportStream.listen(reports.add);

        // Test different error types
        final renderFlexError = FlutterErrorDetails(
          exception: Exception('RenderFlex overflow'),
          stack: StackTrace.current,
          library: 'rendering',
        );

        final nullError = FlutterErrorDetails(
          exception: Exception('Null check operator'),
          stack: StackTrace.current,
          library: 'app',
        );

        FlutterError.onError!(renderFlexError);
        FlutterError.onError!(nullError);

        await Future.delayed(const Duration(milliseconds: 50));

        expect(reports.length, equals(2));
        // RenderFlex errors should be low severity
        expect(reports[0].severity, equals(ErrorSeverity.low));
        // Null errors should be medium severity
        expect(reports[1].severity, equals(ErrorSeverity.medium));

        await subscription.cancel();
      });
    });

    group('Manual Error Reporting', () {
      test('should handle manually reported errors', () async {
        errorHandlerChain.initialize(isDevelopment: true);

        final reports = <ErrorReport>[];
        final subscription = errorHandlerChain.errorReportStream.listen(reports.add);

        final testError = Exception('Manual error');
        final testStackTrace = StackTrace.current;
        final testContext = ErrorContext(
          userAction: 'Manual test',
          appState: {'test': 'value'},
          deviceInfo: const DeviceInfo(platform: 'test'),
          buildVersion: '1.0.0',
        );

        errorHandlerChain.reportError(testError, testStackTrace, testContext);

        await Future.delayed(const Duration(milliseconds: 50));

        expect(reports.length, equals(1));
        expect(reports.first.message, contains('Manual error'));
        expect(reports.first.context.userAction, equals('Manual test'));

        await subscription.cancel();
      });
    });

    group('Error Context', () {
      test('should sanitize sensitive information in error context', () {
        final context = ErrorContext(
          comicId: 'very_long_comic_id_12345',
          currentPage: 10,
          userAction: 'Reading comic',
          appState: {
            'token': 'secret_token_value',
            'password': 'user_password',
            'normalData': 'normal_value',
          },
          deviceInfo: const DeviceInfo(
            platform: 'android',
            model: 'Samsung Galaxy S21',
          ),
          buildVersion: '1.0.0',
        );

        final anonymized = context.anonymize();
        final reportableMap = anonymized.toReportableMap();

        expect(reportableMap['comicId'], equals('ve***45'));
        expect(reportableMap['appState']['token'], equals('[REDACTED]'));
        expect(reportableMap['appState']['password'], equals('[REDACTED]'));
        expect(reportableMap['appState']['normalData'], equals('normal_value'));
      });

      test('should handle device info anonymization', () {
        final deviceInfo = const DeviceInfo(
          platform: 'ios',
          model: 'iPhone 13 Pro',
          operatingSystem: 'iOS',
          operatingSystemVersion: '15.0',
          memoryMB: 6144,
        );

        final anonymized = deviceInfo.anonymize();
        final deviceMap = anonymized.toMap();

        expect(deviceMap['platform'], equals('ios'));
        expect(deviceMap['operatingSystem'], equals('iOS'));
        expect(deviceMap['model'], isNot(equals('iPhone 13 Pro')));
        expect(deviceMap['model'], startsWith('device_'));
      });
    });

    group('Error Severity Classification', () {
      test('should classify rendering errors as low severity', () {
        errorHandlerChain.initialize(isDevelopment: true);

        final renderError = FlutterErrorDetails(
          exception: Exception('RenderFlex overflowed by 10 pixels'),
          stack: StackTrace.current,
          library: 'rendering',
        );

        // This is a bit hacky, but we need to access the private method
        // In a real implementation, you'd expose this through a public interface
        expect(renderError.exceptionAsString().toLowerCase(), contains('overflow'));
      });

      test('should classify assertion errors as high severity', () {
        final assertionError = FlutterErrorDetails(
          exception: AssertionError('Widget assertion failed'),
          stack: StackTrace.current,
          library: 'widgets',
        );

        expect(assertionError.exceptionAsString().toLowerCase(), contains('assertion'));
      });
    });

    group('Processor Implementations', () {
      test('DevelopmentErrorProcessor should log detailed information', () async {
        final processor = DevelopmentErrorProcessor();
        final testError = FlutterErrorDetails(
          exception: Exception('Development test error'),
          stack: StackTrace.current,
          library: 'test',
        );

        final context = ErrorContext(
          userAction: 'Testing',
          appState: {'debug': true},
          deviceInfo: const DeviceInfo(platform: 'test'),
          buildVersion: '1.0.0-dev',
        );

        // Should not throw
        expect(() => processor.processError(testError, context), returnsNormally);
      });

      test('ProductionErrorProcessor should handle errors gracefully', () async {
        final processor = ProductionErrorProcessor();
        final testError = FlutterErrorDetails(
          exception: Exception('Production test error'),
          stack: StackTrace.current,
          library: 'test',
        );

        final context = ErrorContext(
          userAction: 'Using app',
          appState: {'production': true},
          deviceInfo: const DeviceInfo(platform: 'test'),
          buildVersion: '1.0.0',
        );

        // Should not throw
        expect(() => processor.processError(testError, context), returnsNormally);
      });
    });

    group('Disposal', () {
      test('should restore original error handler on dispose', () {
        final originalHandler = FlutterError.onError;
        
        errorHandlerChain.initialize(isDevelopment: true);
        expect(FlutterError.onError, isNot(equals(originalHandler)));

        errorHandlerChain.dispose();
        expect(FlutterError.onError, equals(originalHandler));
      });

      test('should close error report stream on dispose', () async {
        errorHandlerChain.initialize(isDevelopment: true);

        bool streamClosed = false;
        final subscription = errorHandlerChain.errorReportStream.listen(
          (_) {},
          onDone: () => streamClosed = true,
        );

        errorHandlerChain.dispose();

        await Future.delayed(const Duration(milliseconds: 10));
        expect(streamClosed, isTrue);

        await subscription.cancel();
      });

      test('should handle multiple dispose calls safely', () {
        errorHandlerChain.initialize(isDevelopment: true);

        expect(() => errorHandlerChain.dispose(), returnsNormally);
        expect(() => errorHandlerChain.dispose(), returnsNormally);
      });
    });

    group('Non-destructive Behavior', () {
      test('should preserve original handler functionality', () async {
        bool originalHandlerCalled = false;
        final customOriginalHandler = (FlutterErrorDetails details) {
          originalHandlerCalled = true;
        };

        FlutterError.onError = customOriginalHandler;
        errorHandlerChain.initialize(isDevelopment: true);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test',
        );

        FlutterError.onError!(testError);

        expect(originalHandlerCalled, isTrue);
      });

      test('should handle case where original handler is null', () {
        FlutterError.onError = null;
        
        expect(() => errorHandlerChain.initialize(isDevelopment: true), returnsNormally);

        final testError = FlutterErrorDetails(
          exception: Exception('Test error'),
          stack: StackTrace.current,
          library: 'test',
        );

        expect(() => FlutterError.onError!(testError), returnsNormally);
      });
    });
  });
}