import 'package:json_annotation/json_annotation.dart';

part 'webdav_config.g.dart';

@JsonSerializable()
class WebDAVConfig {
  final String uri;
  final String username;
  final String password;

  const WebDAVConfig({
    required this.uri,
    required this.username,
    required this.password,
  });

  factory WebDAVConfig.fromJson(Map<String, dynamic> json) =>
      _$WebDAVConfigFromJson(json);

  Map<String, dynamic> toJson() => _$WebDAVConfigToJson(this);
}