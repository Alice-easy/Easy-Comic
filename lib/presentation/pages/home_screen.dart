import 'package:easy_comic/injection_container.dart';
import 'package:easy_comic/presentation/features/bookshelf/view/bookshelf_screen.dart';
import 'package:easy_comic/presentation/features/bookshelf/bloc/bookshelf_bloc.dart';
import 'package:easy_comic/presentation/features/bookshelf/bloc/bookshelf_event.dart';
import 'package:easy_comic/presentation/features/favorites/view/favorites_screen.dart';
import 'package:easy_comic/presentation/features/favorites/bloc/favorites_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/view/enhanced_settings_screen.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_bloc.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_event.dart';
import 'package:easy_comic/presentation/features/settings/general/bloc/settings_state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with TickerProviderStateMixin {
  int _selectedIndex = 0;
  late final PageController _pageController;
  late final AnimationController _animationController;
  late final Animation<double> _fadeAnimation;

  // BLoC instances that persist across page switches
  late final BookshelfBloc _bookshelfBloc;
  late final FavoritesBloc _favoritesBloc;
  late final SettingsBloc _settingsBloc;

  @override
  void initState() {
    super.initState();
    _pageController = PageController(initialPage: _selectedIndex);
    _animationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _fadeAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInOut,
    ));

    // Initialize persistent BLoC instances
    _bookshelfBloc = sl<BookshelfBloc>();
    _favoritesBloc = sl<FavoritesBloc>();
    _settingsBloc = sl<SettingsBloc>();
    
    // Start animation
    _animationController.forward();
    
    // Load initial data
    _loadInitialData();
  }

  void _loadInitialData() {
    _bookshelfBloc.add(const LoadBookshelf('default'));
    _favoritesBloc.add(LoadFavorites());
    _settingsBloc.add(LoadSettings());
  }

  @override
  void dispose() {
    _pageController.dispose();
    _animationController.dispose();
    _bookshelfBloc.close();
    _favoritesBloc.close();
    _settingsBloc.close();
    super.dispose();
  }

  List<Widget> get _pages => [
    _KeepAlivePage(child: BookshelfScreen()),
    _KeepAlivePage(child: FavoritesScreen()),
    _KeepAlivePage(child: EnhancedSettingsScreen()),
  ];

  void _onItemTapped(int index) {
    if (_selectedIndex != index) {
      _pageController.animateToPage(
        index,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  void _onPageChanged(int index) {
    setState(() {
      _selectedIndex = index;
    });
    
    // Trigger data refresh when switching to certain pages
    if (index == 0) {
      // Refresh bookshelf when switching to it
      _bookshelfBloc.add(const LoadBookshelf('default'));
    } else if (index == 1) {
      // Refresh favorites when switching to it
      _favoritesBloc.add(LoadFavorites());
    }
  }

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider<BookshelfBloc>.value(value: _bookshelfBloc),
        BlocProvider<FavoritesBloc>.value(value: _favoritesBloc),
        BlocProvider<SettingsBloc>.value(value: _settingsBloc),
      ],
      child: MultiBlocListener(
        listeners: [
          BlocListener<SettingsBloc, SettingsState>(
            listener: (context, state) {
              // Handle settings changes that affect other pages
              if (state is SettingsLoaded) {
                // Notify other BLoCs about settings changes if needed
                _bookshelfBloc.add(const LoadBookshelf('default'));
              }
            },
          ),
          BlocListener<FavoritesBloc, FavoritesState>(
            listener: (context, state) {
              // Handle favorite operations that might affect bookshelf
              if (state is FavoritesLoaded) {
                _bookshelfBloc.add(const LoadBookshelf('default'));
              }
            },
          ),
        ],
        child: FadeTransition(
          opacity: _fadeAnimation,
          child: Scaffold(
            body: PageView(
              controller: _pageController,
              onPageChanged: _onPageChanged,
              children: _pages,
            ),
            bottomNavigationBar: _buildBottomNavigationBar(),
          ),
        ),
      ),
    );
  }

  Widget _buildBottomNavigationBar() {
    return Container(
      decoration: BoxDecoration(
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.book),
            activeIcon: Icon(Icons.book),
            label: '书架',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.favorite_outline),
            activeIcon: Icon(Icons.favorite),
            label: '收藏',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings_outlined),
            activeIcon: Icon(Icons.settings),
            label: '设置',
          ),
        ],
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
        elevation: 0,
        selectedItemColor: Theme.of(context).primaryColor,
        unselectedItemColor: Colors.grey,
        selectedFontSize: 12,
        unselectedFontSize: 12,
      ),
    );
  }
}

/// Widget that keeps its state alive across page switches
class _KeepAlivePage extends StatefulWidget {
  final Widget child;

  const _KeepAlivePage({required this.child});

  @override
  State<_KeepAlivePage> createState() => _KeepAlivePageState();
}

class _KeepAlivePageState extends State<_KeepAlivePage>
    with AutomaticKeepAliveClientMixin {
  @override
  bool get wantKeepAlive => true;

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return widget.child;
  }
}