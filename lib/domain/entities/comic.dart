// lib/domain/entities/comic.dart
import 'package:easy_comic/domain/entities/comic_page.dart';
import 'package:equatable/equatable.dart';

class Comic extends Equatable {
  final int id;
  final String filePath;
  final String title;
  final List<ComicPage> pages;

  const Comic({required this.id, required this.filePath, required this.title, required this.pages});

  @override
  List<Object> get props => [id, filePath, title, pages];
}