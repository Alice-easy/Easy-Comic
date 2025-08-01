import 'package:equatable/equatable.dart';

class WebDavConfig extends Equatable {
  final String uri;
  final String user;
  final String password;

  const WebDavConfig({
    required this.uri,
    required this.user,
    required this.password,
  });

  @override
  List<Object?> get props => [uri, user, password];

  factory WebDavConfig.fromJson(Map<String, dynamic> json) {
    return WebDavConfig(
      uri: json['uri'],
      user: json['user'],
      password: json['password'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'uri': uri,
      'user': user,
      'password': password,
    };
  }
}