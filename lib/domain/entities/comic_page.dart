// lib/domain/entities/comic_page.dart
import 'dart:typed_data';

import 'package:equatable/equatable.dart';

class ComicPage extends Equatable {
  final int pageIndex;
  final Uint8List imageData;

  const ComicPage({required this.pageIndex, required this.imageData});

  @override
  List<Object> get props => [pageIndex];
}