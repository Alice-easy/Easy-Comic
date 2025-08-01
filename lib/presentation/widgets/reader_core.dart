import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../domain/entities/comic.dart';
import '../../domain/entities/comic_page.dart';
import '../../domain/entities/reader_settings.dart';
import '../../domain/entities/bookmark.dart';
import '../../domain/entities/gesture_types.dart';
import '../../domain/services/auto_page_service.dart';
import '../../domain/services/cache_service.dart';
import '../../domain/services/volume_key_service.dart';
import '../../domain/services/gesture_config_service.dart';
import '../../core/services/cache_service.dart';
import '../renderers/reading_mode_renderer.dart';
import '../renderers/horizontal_mode_renderer.dart';
import '../renderers/vertical_mode_renderer.dart';
import '../renderers/webtoon_mode_renderer.dart';
import 'auto_page/auto_page_progress_indicator.dart';

class ReaderCore extends StatefulWidget {
  final Comic comic;
  final List<ComicPage> pages;
  final int currentPageIndex;
  final ReaderSettings settings;
  
  // Interface dependencies - NO concrete classes
  final AutoPageService autoPageService;
  final ICacheService cacheService;
  final IVolumeKeyService volumeKeyService;
  final IGestureConfigService gestureConfigService;
  
  // Callbacks
  final ValueChanged<int> onPageChanged;
  final ValueChanged<GestureEvent> onGesture;
  final ValueChanged<double> onZoomChanged;
  final VoidCallback? onAutoPageToggle;
  final Function(Bookmark)? onBookmarkCreate;

  const ReaderCore({
    Key? key,
    required this.comic,
    required this.pages,
    required this.currentPageIndex,
    required this.settings,
    required this.autoPageService,
    required this.cacheService,
    required this.volumeKeyService,
    required this.gestureConfigService,
    required this.onPageChanged,
    required this.onGesture,
    required this.onZoomChanged,
    this.onAutoPageToggle,
    this.onBookmarkCreate,
  }) : super(key: key);

  @override
  State<ReaderCore> createState() => _ReaderCoreState();
}

class _ReaderCoreState extends State<ReaderCore> with TickerProviderStateMixin {
  late ReadingModeRenderer _modeRenderer;
  late PageController _pageController;
  late ScrollController _scrollController;
  final FocusNode _focusNode = FocusNode();
  
  @override
  void initState() {
    super.initState();
    _initializeControllers();
    _initializeModeRenderer();
    _setupVolumeKeyListener();
    
    // Request focus for volume key handling
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _focusNode.requestFocus();
    });
  }

  void _initializeControllers() {
    _pageController = PageController(initialPage: widget.currentPageIndex);
    _scrollController = ScrollController();
  }

  void _initializeModeRenderer() {
    switch (widget.settings.readingMode) {
      case ReadingMode.leftToRight:
      case ReadingMode.rightToLeft:
        _modeRenderer = HorizontalModeRenderer(
          settings: widget.settings,
          navigationService: NavigationService(), // TODO: Inject this via constructor
          pageController: _pageController,
        );
        break;
      case ReadingMode.vertical:
        _modeRenderer = VerticalModeRenderer(
          settings: widget.settings,
          navigationService: NavigationService(), // TODO: Inject this via constructor
          pageController: _pageController,
        );
        break;
      case ReadingMode.webtoon:
        _modeRenderer = WebtoonModeRenderer(
          settings: widget.settings,
          navigationService: NavigationService(), // TODO: Inject this via constructor
          scrollController: _scrollController,
        );
        break;
    }
  }

  void _setupVolumeKeyListener() {
    widget.volumeKeyService.keyEventStream.listen((event) {
      _handleVolumeKeyEvent(event);
    });
  }

  void _handleVolumeKeyEvent(VolumeKeyEvent event) {
    if (!widget.settings.enableVolumeKeys) return;
    
    final gestureType = event.type == VolumeKeyType.volumeUp 
        ? GestureType.volumeUp 
        : GestureType.volumeDown;
        
    final gestureEvent = GestureEvent.now(type: gestureType);
    widget.onGesture(gestureEvent);
    _pauseAutoPageOnUserInteraction();
  }

  @override
  void didUpdateWidget(ReaderCore oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.currentPageIndex != widget.currentPageIndex) {
      _updatePageController();
    }
    
    // Reinitialize renderer if settings changed
    if (oldWidget.settings.readingMode != widget.settings.readingMode) {
      _initializeModeRenderer();
    }
  }

  void _updatePageController() {
    if (widget.settings.readingMode != ReadingMode.webtoon) {
      _pageController.animateToPage(
        widget.currentPageIndex,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  @override
  void dispose() {
    _pageController.dispose();
    _scrollController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _pauseAutoPageOnUserInteraction() {
    if (widget.autoPageService.isAutoPageActive && 
        widget.settings.autoPageConfig?.pauseOnUserInteraction == true) {
      widget.autoPageService.pauseForUserInteraction();
    }
  }

  @override
  Widget build(BuildContext context) {
    if (widget.pages.isEmpty) {
      return const Center(
        child: Text(
          '没有可显示的页面',
          style: TextStyle(color: Colors.white, fontSize: 16),
        ),
      );
    }

    return KeyboardListener(
      focusNode: _focusNode,
      onKeyEvent: _handleKeyEvent,
      child: GestureDetector(
        onTap: _handleTap,
        onScaleStart: (_) => _pauseAutoPageOnUserInteraction(),
        onScaleUpdate: (_) => _pauseAutoPageOnUserInteraction(),
        child: Stack(
          children: [
            // Main reading view using renderer
            _modeRenderer.buildView(widget.pages, widget.currentPageIndex),
            
            // Auto-page progress indicator
            if (widget.settings.autoPageConfig?.showProgressIndicator == true)
              StreamBuilder<AutoPageState>(
                stream: widget.autoPageService.watchAutoPageState(),
                builder: (context, snapshot) {
                  if (!snapshot.hasData) return const SizedBox.shrink();
                  
                  return AutoPageProgressIndicator(
                    state: snapshot.data!,
                    onTap: _onAutoPageIndicatorTapped,
                    showProgressIndicator: widget.settings.autoPageConfig?.showProgressIndicator ?? false,
                  );
                },
              ),
          ],
        ),
      ),
    );
  }

  void _handleTap() {
    // For center tap, we'll use the center of the screen as default position
    final screenSize = MediaQuery.of(context).size;
    final centerPosition = Offset(screenSize.width / 2, screenSize.height / 2);
    
    final gestureEvent = GestureEvent.now(
      type: GestureType.tapCenter,
      position: centerPosition,
    );
    
    widget.onGesture(gestureEvent);
    _pauseAutoPageOnUserInteraction();
  }

  void _handleKeyEvent(KeyEvent event) {
    if (!widget.settings.enableVolumeKeys) return;
    
    if (event is KeyDownEvent) {
      GestureType? gestureType;
      
      if (event.logicalKey == LogicalKeyboardKey.audioVolumeUp) {
        gestureType = GestureType.volumeUp;
      } else if (event.logicalKey == LogicalKeyboardKey.audioVolumeDown) {
        gestureType = GestureType.volumeDown;
      }
      
      if (gestureType != null) {
        final gestureEvent = GestureEvent.now(type: gestureType);
        widget.onGesture(gestureEvent);
        _pauseAutoPageOnUserInteraction();
      }
    }
  }

  void _onAutoPageIndicatorTapped() {
    if (widget.autoPageService.isAutoPageActive) {
      if (widget.autoPageService.isAutoPagePaused) {
        widget.autoPageService.resumeAutoPage();
      } else {
        widget.autoPageService.pauseAutoPage();
      }
    } else {
      widget.onAutoPageToggle?.call();
    }
  }
}
}