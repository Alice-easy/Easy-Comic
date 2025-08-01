// lib/domain/entities/comic_page.dart
import 'dart:typed_data';
import 'dart:convert';

import 'package:equatable/equatable.dart';

class ComicPage extends Equatable {
  final int pageIndex;
  final Uint8List imageData;
  final String path;

  const ComicPage({
    required this.pageIndex,
    required this.imageData,
    required this.path,
  });

  factory ComicPage.fromJson(Map<String, dynamic> json) {
    return ComicPage(
      pageIndex: json['pageIndex'] as int,
      imageData: base64Decode(json['imageData'] as String),
      path: json['path'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'pageIndex': pageIndex,
      'imageData': base64Encode(imageData),
      'path': path,
    };
  }

  @override
  List<Object> get props => [pageIndex, path];
}