import 'package:json_annotation/json_annotation.dart';

part 'brightness_errors.g.dart';

/// Sealed class hierarchy for brightness-related errors
@JsonSerializable()
sealed class BrightnessError implements Exception {
  const BrightnessError();
  
  String get message;

  factory BrightnessError.fromJson(Map<String, dynamic> json) =>
      _$BrightnessErrorFromJson(json);

  Map<String, dynamic> toJson() => _$BrightnessErrorToJson(this);
}

/// Permission denied error for brightness control
@JsonSerializable()
class BrightnessPermissionError extends BrightnessError {
  @override
  final String message;
  
  const BrightnessPermissionError(this.message);
  
  @override
  String toString() => 'BrightnessPermissionError: $message';
}

/// Platform-specific error for brightness control
@JsonSerializable()
class BrightnessPlatformError extends BrightnessError {
  @override
  final String message;
  final String? platformCode;
  
  const BrightnessPlatformError(this.message, {this.platformCode});
  
  @override
  String toString() => 'BrightnessPlatformError: $message${platformCode != null ? ' (code: $platformCode)' : ''}';
}

/// Unsupported operation error for brightness control
@JsonSerializable()
class BrightnessUnsupportedError extends BrightnessError {
  @override
  final String message;
  
  const BrightnessUnsupportedError(this.message);
  
  @override
  String toString() => 'BrightnessUnsupportedError: $message';
}

/// Generic brightness error
@JsonSerializable()
class BrightnessGenericError extends BrightnessError {
  @override
  final String message;
  @JsonKey(includeToJson: false, includeFromJson: false)
  final dynamic originalError;
  
  const BrightnessGenericError(this.message, {this.originalError});
  
  @override
  String toString() => 'BrightnessGenericError: $message${originalError != null ? ' (caused by: $originalError)' : ''}';
}
class BrightnessErrorConverter
    implements JsonConverter<BrightnessError, Map<String, dynamic>> {
  const BrightnessErrorConverter();

  @override
  BrightnessError fromJson(Map<String, dynamic> json) {
    final type = json['type'] as String;
    switch (type) {
      case 'permission':
        return BrightnessPermissionError.fromJson(json);
      case 'platform':
        return BrightnessPlatformError.fromJson(json);
      case 'unsupported':
        return BrightnessUnsupportedError.fromJson(json);
      case 'generic':
        return BrightnessGenericError.fromJson(json);
      default:
        throw Exception('Unknown BrightnessError type: $type');
    }
  }

  @override
  Map<String, dynamic> toJson(BrightnessError object) {
    return object.toJson();
  }
}