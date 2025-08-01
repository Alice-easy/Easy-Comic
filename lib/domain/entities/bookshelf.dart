import 'package:equatable/equatable.dart';

class Bookshelf extends Equatable {
  final int id;
  final String name;
  final String? coverImage;
  final DateTime createTime;

  const Bookshelf({
    required this.id,
    required this.name,
    this.coverImage,
    required this.createTime,
  });

  @override
  List<Object?> get props => [id, name, coverImage, createTime];
}