import 'package:freezed_annotation/freezed_annotation.dart';

part 'webdav_config.freezed.dart';
part 'webdav_config.g.dart';

@freezed
class WebDAVConfig with _$WebDAVConfig {
  const factory WebDAVConfig({
    required String serverUrl,
    required String username,
    required String password,
    required bool autoSync,
    required String avatarPath,
  }) = _WebDAVConfig;

  factory WebDAVConfig.fromJson(Map<String, dynamic> json) => _$WebDAVConfigFromJson(json);
}