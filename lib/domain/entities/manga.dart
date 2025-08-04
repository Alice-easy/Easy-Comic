import 'package:freezed_annotation/freezed_annotation.dart';

part 'manga.freezed.dart';
part 'manga.g.dart';

@freezed
class Manga with _$Manga {
  const factory Manga({
    required String id,
    required String title,
    required String filePath,
    required String coverPath,
    required int totalPages,
    required int currentPage,
    required DateTime lastRead,
    required DateTime dateAdded,
    required List<String> tags,
    required bool isFavorite,
    @Default([]) List<String> pagePaths,
  }) = _Manga;

  factory Manga.fromJson(Map<String, dynamic> json) => _$MangaFromJson(json);
}