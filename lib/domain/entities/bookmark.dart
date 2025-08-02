import 'package:equatable/equatable.dart';

class Bookmark extends Equatable {
  final String comicId;
  final int pageIndex;
  final String? note;
  final DateTime createdAt;

  const Bookmark({
    required this.comicId,
    required this.pageIndex,
    this.note,
    required this.createdAt,
  });

  @override
  List<Object?> get props => [comicId, pageIndex, note, createdAt];

  factory Bookmark.fromJson(Map<String, dynamic> json) {
    return Bookmark(
      comicId: json['comicId'] as String,
      pageIndex: json['pageIndex'] as int,
      note: json['note'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'comicId': comicId,
      'pageIndex': pageIndex,
      'note': note,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}