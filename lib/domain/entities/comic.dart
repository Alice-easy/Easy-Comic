import 'dart:convert';

import 'package:equatable/equatable.dart';

class Comic extends Equatable {
  final String id;
  final String filePath;
  final String fileName;
  final String coverPath;
  final int pageCount;
  final DateTime addTime;
  final DateTime lastReadTime;
  final int progress;
  final int bookshelfId;
  final bool isFavorite;
  final List<String> tags;
  final Map<String, dynamic> metadata;

  const Comic({
    required this.id,
    required this.filePath,
    required this.fileName,
    required this.coverPath,
    required this.pageCount,
    required this.addTime,
    required this.lastReadTime,
    required this.progress,
    required this.bookshelfId,
    required this.isFavorite,
    required this.tags,
    required this.metadata,
  });

  @override
  List<Object?> get props => [
        id,
        filePath,
        fileName,
        coverPath,
        pageCount,
        addTime,
        lastReadTime,
        progress,
        bookshelfId,
        isFavorite,
        tags,
        metadata,
      ];

  factory Comic.fromJson(Map<String, dynamic> json) {
    return Comic(
      id: json['id'],
      filePath: json['filePath'],
      fileName: json['fileName'],
      coverPath: json['coverPath'],
      pageCount: json['pageCount'],
      addTime: DateTime.parse(json['addTime']),
      lastReadTime: DateTime.parse(json['lastReadTime']),
      progress: json['progress'],
      bookshelfId: json['bookshelfId'],
      isFavorite: json['isFavorite'],
      tags: List<String>.from(jsonDecode(json['tags'])),
      metadata: jsonDecode(json['metadata']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'filePath': filePath,
      'fileName': fileName,
      'coverPath': coverPath,
      'pageCount': pageCount,
      'addTime': addTime.toIso8601String(),
      'lastReadTime': lastReadTime.toIso8601String(),
      'progress': progress,
      'bookshelfId': bookshelfId,
      'isFavorite': isFavorite,
      'tags': jsonEncode(tags),
      'metadata': jsonEncode(metadata),
    };
  }
}