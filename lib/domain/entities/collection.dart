import 'package:equatable/equatable.dart';

class Collection extends Equatable {
  final String id;
  final String name;
  final List<String> mangaIds;

  const Collection({
    required this.id,
    required this.name,
    this.mangaIds = const [],
  });

  @override
  List<Object?> get props => [id, name, mangaIds];
}