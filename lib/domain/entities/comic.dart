import 'dart:convert';

import 'package:equatable/equatable.dart';
import 'package:easy_comic/domain/entities/comic_page.dart';

class Comic extends Equatable {
  final String id;
  final String title;
  final String path;
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
  
  // 新增属性以支持构建要求
  final String author;
  final DateTime? addedAt;
  final DateTime? lastReadAt;
  final int? currentPage;
  final int? totalPages;
  final List<ComicPage> pages;

  const Comic({
    required this.id,
    required this.title,
    required this.path,
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
    required this.author,
    this.addedAt,
    this.lastReadAt,
    this.currentPage,
    this.totalPages,
    this.pages = const [],
  });

  @override
  List<Object?> get props => [
        id,
        title,
        path,
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
        author,
        addedAt,
        lastReadAt,
        currentPage,
        totalPages,
        pages,
      ];

  factory Comic.fromJson(Map<String, dynamic> json) {
    return Comic(
      id: json['id'],
      title: json['title'],
      path: json['path'],
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
      author: json['author'] ?? '',
      addedAt: json['addedAt'] != null ? DateTime.parse(json['addedAt']) : null,
      lastReadAt: json['lastReadAt'] != null ? DateTime.parse(json['lastReadAt']) : null,
      currentPage: json['currentPage'],
      totalPages: json['totalPages'],
      pages: json['pages'] != null
          ? (jsonDecode(json['pages']) as List).map((p) => ComicPage.fromJson(p)).toList()
          : const [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'path': path,
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
      'author': author,
      'addedAt': addedAt?.toIso8601String(),
      'lastReadAt': lastReadAt?.toIso8601String(),
      'currentPage': currentPage,
      'totalPages': totalPages,
      'pages': jsonEncode(pages.map((p) => p.toJson()).toList()),
    };
  }
}