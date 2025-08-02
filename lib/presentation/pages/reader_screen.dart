import 'package:flutter/material.dart';
import 'dart:io';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:wakelock_plus/wakelock_plus.dart';
import 'dart:developer' as developer;
import '../../injection_container.dart';
import '../features/reader/bloc/reader_bloc.dart';
import '../features/reader/bloc/reader_event.dart';
import '../features/reader/bloc/reader_state.dart';
import '../widgets/reader_app_bar.dart';
import '../widgets/reader_bottom_bar.dart';
import '../widgets/error_boundary_widget.dart';
import '../widgets/loading_indicator_widget.dart';

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
                        '${state.currentPageIndex + 1} / ${state.comic.pages?.length ?? 0}',
                    comicId: state.comic.id,
                  )
                : null,
            body: _buildBody(context, state),
            bottomNavigationBar: (state is ReaderLoaded && state.isUIVisible && state.comic.pages != null)
                ? ReaderBottomBar(
                    currentPage: state.currentPageIndex,
                    totalPages: state.comic.pages!.length,
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
    developer.log('Building reader body', name: 'ReaderScreen', error: {
      'stateType': state.runtimeType.toString(),
    });
    
    if (state is ReaderLoading) {
      return _buildLoadingState(context, state);
    } else if (state is ReaderLoaded) {
      return _buildLoadedState(context, state);
    } else if (state is ReaderError) {
      return _buildErrorState(context, state);
    } else {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.library_books, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'Ready to read',
              style: TextStyle(fontSize: 18, color: Colors.grey),
            ),
            SizedBox(height: 8),
            Text(
              'Please select a comic to begin reading',
              style: TextStyle(color: Colors.grey),
            ),
          ],
        ),
      );
    }
  }
  
  Widget _buildLoadingState(BuildContext context, ReaderLoading state) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Progress indicator
          if (state.progress != null)
            Column(
              children: [
                CircularProgressIndicator(
                  value: state.progress! / 100,
                ),
                const SizedBox(height: 16),
                Text(
                  '${state.progress!.toStringAsFixed(0)}%',
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ],
            )
          else
            const CircularProgressIndicator(),
            
          const SizedBox(height: 24),
          
          // Operation text
          if (state.operation != null)
            Text(
              state.operation!,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
            ),
            
          const SizedBox(height: 8),
          
          // Current file/message
          if (state.message != null)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: Text(
                state.message!,
                style: const TextStyle(color: Colors.grey),
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            
          // Debug info (only in development)
          if (state.diagnostics != null && state.diagnostics!.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(top: 16),
              child: ExpansionTile(
                title: const Text('Debug Info', style: TextStyle(fontSize: 12)),
                children: [
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Text(
                      state.diagnostics.toString(),
                      style: const TextStyle(fontSize: 10, fontFamily: 'monospace'),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
  
  Widget _buildLoadedState(BuildContext context, ReaderLoaded state) {
    developer.log('Building loaded state', name: 'ReaderScreen', error: {
      'comicTitle': state.comic.title,
      'pageCount': state.comic.pages?.length ?? 0,
      'currentPage': state.currentPageIndex,
    });
    
    // Validate comic data before building UI
    if (state.comic.pages == null || state.comic.pages!.isEmpty) {
      developer.log('Comic has no pages', name: 'ReaderScreen', level: 1000);
      return _buildErrorState(context, const ReaderError(
        message: 'Comic has no readable pages',
        errorType: ReaderErrorType.noValidImages,
        canRetry: true,
      ));
    }
    
    return ErrorBoundaryWidget(
      child: GestureDetector(
        onTapUp: (details) => _handleTapGesture(context, details, state),
        child: PageView.builder(
          controller: _pageController,
          reverse: state.isReversed,
          itemCount: state.comic.pages!.length,
          onPageChanged: (index) {
            developer.log('Page changed', name: 'ReaderScreen', error: {
              'newIndex': index,
              'totalPages': state.comic.pages!.length,
            });
            context.read<ReaderBloc>().add(PageChanged(index));
          },
          itemBuilder: (context, index) => _buildPageItem(context, state, index),
        ),
      ),
      onError: (error, stackTrace) {
        developer.log('Error in loaded state', name: 'ReaderScreen', level: 1000, error: error, stackTrace: stackTrace);
        return _buildErrorState(context, ReaderError(
          message: 'Error displaying comic: ${error.toString()}',
          errorType: ReaderErrorType.unknown,
          canRetry: true,
          originalError: error,
        ));
      },
    );
  }
  
  Widget _buildPageItem(BuildContext context, ReaderLoaded state, int index) {
    if (index < 0 || index >= state.comic.pages!.length) {
      return const Center(child: Icon(Icons.error, color: Colors.red, size: 48));
    }

    final page = state.comic.pages![index];
    final imagePath = page.path;

    // Validate page path
    if (imagePath.isEmpty) {
      developer.log('Empty page path', name: 'ReaderScreen', level: 900, error: {'pageIndex': index});
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.broken_image, color: Colors.grey, size: 48),
            SizedBox(height: 8),
            Text('Image path is missing', style: TextStyle(color: Colors.grey)),
          ],
        ),
      );
    }

    return InteractiveViewer(
      minScale: 0.5,
      maxScale: 4.0,
      child: Image.file(
        File(imagePath),
        fit: BoxFit.contain,
        // Add a loading builder to show progress while the image loads from disk
        frameBuilder: (BuildContext context, Widget child, int? frame, bool wasSynchronouslyLoaded) {
          if (wasSynchronouslyLoaded) {
            return child;
          }
          return AnimatedOpacity(
            opacity: frame == null ? 0 : 1,
            duration: const Duration(seconds: 1),
            curve: Curves.easeOut,
            child: child,
          );
        },
        errorBuilder: (context, error, stackTrace) {
          developer.log('Image display error', name: 'ReaderScreen', level: 1000, error: error, stackTrace: stackTrace);
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.broken_image, color: Colors.red, size: 48),
                const SizedBox(height: 8),
                const Text('Failed to display page', style: TextStyle(color: Colors.red)),
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Text(
                    'Path: $imagePath',
                    style: const TextStyle(color: Colors.grey, fontSize: 10),
                    textAlign: TextAlign.center,
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
  
  Widget _buildErrorState(BuildContext context, ReaderError state) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              _getErrorIcon(state.errorType),
              size: 64,
              color: Colors.red,
            ),
            const SizedBox(height: 16),
            Text(
              state.message,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            if (state.details != null) ...
              [
                const SizedBox(height: 8),
                Text(
                  state.details!,
                  style: const TextStyle(color: Colors.grey),
                  textAlign: TextAlign.center,
                ),
              ],
            const SizedBox(height: 24),
            if (state.canRetry)
              ElevatedButton.icon(
                onPressed: () => _retryOperation(context),
                icon: const Icon(Icons.refresh),
                label: const Text('Retry'),
              ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Go Back'),
            ),
            if (state.suggestedActions != null && state.suggestedActions!.isNotEmpty) ...
              [
                const SizedBox(height: 16),
                const Text('Suggestions:', style: TextStyle(fontWeight: FontWeight.bold)),
                const SizedBox(height: 8),
                ...state.suggestedActions!.map((action) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 2),
                  child: Text('• $action', style: const TextStyle(color: Colors.grey)),
                )),
              ],
            // Debug information (expandable)
            if (state.diagnostics != null)
              ExpansionTile(
                title: const Text('Debug Information'),
                children: [
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: Text(
                      state.diagnostics.toString(),
                      style: const TextStyle(fontSize: 10, fontFamily: 'monospace'),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
  
  void _handleTapGesture(BuildContext context, TapUpDetails details, ReaderLoaded state) {
    final screenWidth = MediaQuery.of(context).size.width;
    final tapPosition = details.globalPosition.dx;

    if (tapPosition < screenWidth * 0.25) {
      context.read<ReaderBloc>().add(PreviousPage());
    } else if (tapPosition > screenWidth * 0.75) {
      context.read<ReaderBloc>().add(NextPage());
    } else {
      context.read<ReaderBloc>().add(ToggleUIVisibility());
    }
  }
  
  void _retryOperation(BuildContext context) {
    if (widget.filePath != null) {
      context.read<ReaderBloc>().add(LoadComic(filePath: widget.filePath!));
    }
  }
  
  IconData _getErrorIcon(ReaderErrorType errorType) {
    switch (errorType) {
      case ReaderErrorType.fileError:
        return Icons.insert_drive_file;
      case ReaderErrorType.unsupportedFormat:
        return Icons.file_present;
      case ReaderErrorType.corruptedFile:
        return Icons.error;
      case ReaderErrorType.noValidImages:
        return Icons.image_not_supported;
      case ReaderErrorType.permissionDenied:
        return Icons.lock;
      case ReaderErrorType.fileTooLarge:
        return Icons.storage;
      case ReaderErrorType.networkError:
        return Icons.wifi_off;
      case ReaderErrorType.memoryError:
        return Icons.memory;
      default:
        return Icons.error_outline;
    }
  }
}