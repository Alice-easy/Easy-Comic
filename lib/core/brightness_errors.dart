/// Sealed class hierarchy for brightness-related errors
sealed class BrightnessError implements Exception {
  const BrightnessError();
  
  String get message;
}

/// Permission denied error for brightness control
class BrightnessPermissionError extends BrightnessError {
  final String _message;
  
  const BrightnessPermissionError(this._message);
  
  @override
  String get message => _message;
  
  @override
  String toString() => 'BrightnessPermissionError: $_message';
}

/// Platform-specific error for brightness control
class BrightnessPlatformError extends BrightnessError {
  final String _message;
  final String? platformCode;
  
  const BrightnessPlatformError(this._message, {this.platformCode});
  
  @override
  String get message => _message;
  
  @override
  String toString() => 'BrightnessPlatformError: $_message${platformCode != null ? ' (code: $platformCode)' : ''}';
}

/// Unsupported operation error for brightness control
class BrightnessUnsupportedError extends BrightnessError {
  final String _message;
  
  const BrightnessUnsupportedError(this._message);
  
  @override
  String get message => _message;
  
  @override
  String toString() => 'BrightnessUnsupportedError: $_message';
}

/// Generic brightness error
class BrightnessGenericError extends BrightnessError {
  final String _message;
  final dynamic originalError;
  
  const BrightnessGenericError(this._message, {this.originalError});
  
  @override
  String get message => _message;
  
  @override
  String toString() => 'BrightnessGenericError: $_message${originalError != null ? ' (caused by: $originalError)' : ''}';
}