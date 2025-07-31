// lib/domain/entities/bookmark.dart

class Bookmark {
  final String comicId;
  final int pageIndex;
  final DateTime createdAt;

  Bookmark({required this.comicId, required this.pageIndex, required this.createdAt});
}