import 'package:equatable/equatable.dart';

class Favorite extends Equatable {
  final int id;
  final String name;
  final int? parentId;
  final String? description;
  final DateTime createTime;

  const Favorite({
    required this.id,
    required this.name,
    this.parentId,
    this.description,
    required this.createTime,
  });

  @override
  List<Object?> get props => [id, name, parentId, description, createTime];

  factory Favorite.fromJson(Map<String, dynamic> json) {
    return Favorite(
      id: json['id'],
      name: json['name'],
      parentId: json['parentId'],
      description: json['description'],
      createTime: DateTime.parse(json['createTime']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'parentId': parentId,
      'description': description,
      'createTime': createTime.toIso8601String(),
    };
  }
}