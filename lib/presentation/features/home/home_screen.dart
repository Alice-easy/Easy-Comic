import 'package:easy_comic/presentation/features/favorites/favorites_screen.dart';
import 'package:easy_comic/presentation/features/library/bloc/library_bloc.dart';
import 'package:easy_comic/presentation/features/library/library_screen.dart';
import 'package:easy_comic/presentation/features/settings/settings_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;
  final PageController _pageController = PageController();
  bool _isSearch = false;
  final _searchController = TextEditingController();

  final List<String> _titles = ['Library', 'Favorites', 'Settings'];

  final List<Widget> _screens = [
    const LibraryScreen(),
    const FavoritesScreen(),
    const SettingsScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
      _pageController.jumpToPage(index);
    });
  }

  @override
  void dispose() {
    _pageController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _buildAppBar(),
      body: PageView(
        controller: _pageController,
        children: _screens,
        onPageChanged: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.book),
            label: 'Library',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.favorite),
            label: 'Favorites',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: 'Settings',
          ),
        ],
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
      ),
      floatingActionButton: _selectedIndex == 0
          ? FloatingActionButton(
              onPressed: () => context.read<LibraryBloc>().add(ImportManga()),
              child: const Icon(Icons.add),
            )
          : null,
    );
  }

  AppBar _buildAppBar() {
    if (_selectedIndex == 0) {
      // Library Screen AppBar
      return AppBar(
        title: _isSearch
            ? TextField(
                controller: _searchController,
                autofocus: true,
                decoration: const InputDecoration(
                  hintText: 'Search...',
                  border: InputBorder.none,
                ),
                onChanged: (query) {
                  context.read<LibraryBloc>().add(SearchQueryChanged(query));
                },
              )
            : Text(_titles[_selectedIndex]),
        actions: [
          IconButton(
            icon: Icon(_isSearch ? Icons.close : Icons.search),
            onPressed: () {
              setState(() {
                _isSearch = !_isSearch;
                if (!_isSearch) {
                  _searchController.clear();
                  context.read<LibraryBloc>().add(const SearchQueryChanged(''));
                }
              });
            },
          ),
          PopupMenuButton<SortType>(
            onSelected: (sortType) {
              context.read<LibraryBloc>().add(SortOrderChanged(sortType));
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: SortType.dateAdded,
                child: Text('Sort by Date Added'),
              ),
              const PopupMenuItem(
                value: SortType.title,
                child: Text('Sort by Title'),
              ),
            ],
          ),
        ],
      );
    }
    return AppBar(
      title: Text(_titles[_selectedIndex]),
    );
  }
}