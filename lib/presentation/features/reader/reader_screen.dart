import 'dart:io';

import 'package:easy_comic/core/di/injection_container.dart';
import 'package:easy_comic/presentation/features/reader/bloc/reader_bloc.dart';
import 'package:easy_comic/presentation/widgets/loading_overlay.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class ReaderScreen extends StatefulWidget {
  final String mangaId;

  const ReaderScreen({Key? key, required this.mangaId}) : super(key: key);

  @override
  State<ReaderScreen> createState() => _ReaderScreenState();
}

class _ReaderScreenState extends State<ReaderScreen> {
  late PageController _pageController;

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) => sl<ReaderBloc>()..add(LoadMangaEvent(widget.mangaId)),
      child: Scaffold(
        body: BlocConsumer<ReaderBloc, ReaderState>(
          listener: (context, state) {
            if (state is ReaderError) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('Error: ${state.message}'),
                  backgroundColor: Colors.red,
                ),
              );
            } else if (state is ReaderLoaded) {
              if (_pageController.hasClients &&
                  _pageController.page?.round() != state.currentPage) {
                _pageController.jumpToPage(state.currentPage);
              }
            }
          },
          builder: (context, state) {
            final isLoading = state is ReaderLoading;
            Widget body;
            if (state is ReaderLoaded) {
              body = GestureDetector(
                onTap: () => context.read<ReaderBloc>().add(ToggleOverlayEvent()),
                child: Stack(
                  children: [
                    _buildReaderView(context, state),
                    if (state.isOverlayVisible) _buildOverlay(context, state),
                  ],
                ),
              );
            } else if (state is ReaderError) {
              body = Center(child: Text('Error: ${state.message}'));
            } else {
              body = const Center(child: Text('Welcome to the reader!'));
            }

            return LoadingOverlay(
              isLoading: isLoading,
              message: 'Loading comic...',
              child: body,
            );
          },
        ),
      ),
    );
  }

  Widget _buildReaderView(BuildContext context, ReaderLoaded state) {
    switch (state.readingMode) {
      case ReadingMode.LongStrip:
        return ListView.builder(
          itemCount: state.manga.pagePaths.length,
          itemBuilder: (context, index) {
            final pagePath = state.manga.pagePaths[index];
            return InteractiveViewer(
              minScale: 1.0,
              maxScale: 4.0,
              child: Image.file(File(pagePath), cacheWidth: 1080), // Optimize image caching
            );
          },
        );
      case ReadingMode.DoublePage:
        return const Center(child: Text("Double Page Mode (Not Implemented)"));
      case ReadingMode.SinglePage:
      default:
        return PageView.builder(
          controller: _pageController,
          itemCount: state.manga.pagePaths.length,
          onPageChanged: (index) {
            context.read<ReaderBloc>().add(PageChangedEvent(index));
          },
          itemBuilder: (context, index) {
            final pagePath = state.manga.pagePaths[index];
            return InteractiveViewer(
              minScale: 1.0,
              maxScale: 4.0,
              child: Image.file(File(pagePath), cacheWidth: 1080), // Optimize image caching
            );
          },
        );
    }
  }

  Widget _buildOverlay(BuildContext context, ReaderLoaded state) {
    return Column(
      children: [
        AppBar(
          title: Text(state.manga.title),
          backgroundColor: Colors.black.withOpacity(0.5),
          actions: [
            PopupMenuButton<ReadingMode>(
              onSelected: (mode) {
                context.read<ReaderBloc>().add(ReadingModeChangedEvent(mode));
              },
              itemBuilder: (context) => [
                const PopupMenuItem(
                  value: ReadingMode.SinglePage,
                  child: Text('Single Page'),
                ),
                const PopupMenuItem(
                  value: ReadingMode.LongStrip,
                  child: Text('Long Strip'),
                ),
                const PopupMenuItem(
                  value: ReadingMode.DoublePage,
                  child: Text('Double Page'),
                ),
              ],
            ),
          ],
        ),
        const Spacer(),
        Container(
          color: Colors.black.withOpacity(0.5),
          padding: const EdgeInsets.all(16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '${state.currentPage + 1} / ${state.manga.totalPages}',
                style: const TextStyle(color: Colors.white),
              ),
            ],
          ),
        ),
      ],
    );
  }
}