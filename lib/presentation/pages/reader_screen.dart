import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import '../../injection_container.dart';
import '../features/reader/bloc/reader_bloc.dart';
import '../features/reader/bloc/reader_event.dart';
import '../features/reader/bloc/reader_state.dart';
import '../widgets/reader_app_bar.dart';
import '../widgets/reader_bottom_bar.dart';

class ReaderScreen extends StatefulWidget {
  final String? filePath;

  const ReaderScreen({super.key, this.filePath});

  @override
  State<ReaderScreen> createState() => _ReaderScreenState();
}

class _ReaderScreenState extends State<ReaderScreen> {
  late final PageController _pageController;

  @override
  void initState() {
    super.initState();
    _pageController = PageController();
    WakelockPlus.enable();
  }

  @override
  void dispose() {
    _pageController.dispose();
    WakelockPlus.disable();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (context) {
        final bloc = sl<ReaderBloc>();
        if (widget.filePath != null) {
          bloc.add(LoadComic(filePath: widget.filePath!));
        }
        return bloc;
      },
      child: BlocConsumer<ReaderBloc, ReaderState>(
        listener: (context, state) {
          if (state is ReaderLoaded) {
            if (_pageController.page?.round() != state.currentPageIndex) {
              _pageController.jumpToPage(state.currentPageIndex);
            }
          }
        },
        builder: (context, state) {
          return Scaffold(
            appBar: (state is ReaderLoaded && state.isUIVisible)
                ? ReaderAppBar(
                    comicTitle: state.comic.title,
                    pageIndicatorText:
                        '${state.currentPageIndex + 1} / ${state.comic.pages.length}',
                    comicId: state.comic.id,
                  )
                : null,
            body: _buildBody(context, state),
            bottomNavigationBar: (state is ReaderLoaded && state.isUIVisible)
                ? ReaderBottomBar(
                    currentPage: state.currentPageIndex,
                    totalPages: state.comic.pages.length,
                    onSliderChanged: (newIndex) {
                      context.read<ReaderBloc>().add(PageChanged(newIndex));
                    },
                  )
                : null,
          );
        },
      ),
    );
  }

  Widget _buildBody(BuildContext context, ReaderState state) {
    if (state is ReaderLoading) {
      return const Center(child: CircularProgressIndicator());
    } else if (state is ReaderLoaded) {
      return GestureDetector(
        onTapUp: (details) {
          final screenWidth = MediaQuery.of(context).size.width;
          final tapPosition = details.globalPosition.dx;

          if (tapPosition < screenWidth * 0.25) {
            context.read<ReaderBloc>().add(PreviousPage());
          } else if (tapPosition > screenWidth * 0.75) {
            context.read<ReaderBloc>().add(NextPage());
          } else {
            context.read<ReaderBloc>().add(ToggleUIVisibility());
          }
        },
        child: PageView.builder(
          controller: _pageController,
          reverse: state.isReversed,
          itemCount: state.comic.pages.length,
          onPageChanged: (index) {
            context.read<ReaderBloc>().add(PageChanged(index));
          },
          itemBuilder: (context, index) {
            final page = state.comic.pages[index];
            return InteractiveViewer(
              child: Image.memory(
                page.imageData,
                fit: BoxFit.contain,
              ),
            );
          },
        ),
      );
    } else if (state is ReaderError) {
      return Center(child: Text(state.message));
    } else {
      return const Center(child: Text('Please load a comic.'));
    }
  }
}