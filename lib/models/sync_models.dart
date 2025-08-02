import 'package:easy_comic/domain/entities/bookmark.dart';
import 'package:easy_comic/domain/entities/comic_progress.dart';
import 'package:easy_comic/domain/entities/favorite.dart';
import 'package:easy_comic/domain/entities/reader_settings.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'sync_models.freezed.dart';
part 'sync_models.g.dart';

@freezed
class SyncPackage with _$SyncPackage {
  const factory SyncPackage({
    required DateTime lastModified,
    required ReaderSettings settings,
    required List<ComicProgress> progress,
    required List<Favorite> favorites,
    required List<Bookmark> bookmarks,
  }) = _SyncPackage;

  factory SyncPackage.fromJson(Map<String, dynamic> json) =>
      _$SyncPackageFromJson(json);
}


class SyncResult {
  SyncResult({
    required this.uploaded,
    required this.downloaded,
    required this.conflicts,
    required this.errors,
  });

  final int uploaded;
  final int downloaded;
  final int conflicts;
  final List<String> errors;

  @override
  String toString() =>
      'SyncResult(uploaded: $uploaded, downloaded: $downloaded, conflicts: $conflicts, errors: ${errors.length})';
}

class SyncDataItem {
  SyncDataItem({
    required this.fileHash,
    required this.currentPage,
    required this.totalPages,
    required this.updatedAt,
    this.localEtag,
    this.remoteEtag,
  });

  factory SyncDataItem.fromJson(Map<String, dynamic> json) => SyncDataItem(
    fileHash: json['fileHash'] as String,
    currentPage: json['currentPage'] as int,
    totalPages: json['totalPages'] as int,
    updatedAt: DateTime.parse(json['updatedAt'] as String),
  );

  final String fileHash;
  final int currentPage;
  final int totalPages;
  final DateTime updatedAt;
  final String? localEtag;
  final String? remoteEtag;

  Map<String, dynamic> toJson() => {
    'fileHash': fileHash,
    'currentPage': currentPage,
    'totalPages': totalPages,
    'updatedAt': updatedAt.toIso8601String(),
  };
}
