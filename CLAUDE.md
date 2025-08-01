# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy Comic is a Flutter-based cross-platform comic reader application that provides smooth reading experience, intelligent caching, and WebDAV cloud synchronization. The app supports local comic files (.cbz, .zip) with features like bookmarks, reading progress tracking, and customizable reading settings.

## Development Commands

### Build and Development
```bash
# Development mode (Windows)
scripts/dev.bat                              # Start development on Windows

# Code generation (required after database changes)
flutter packages pub run build_runner build --delete-conflicting-outputs

# Standard Flutter commands
flutter run                                  # Run on default device
flutter run -d windows                       # Run on Windows desktop
flutter run -d android                       # Run on Android device
flutter build apk                           # Build Android APK
flutter build ios                           # Build iOS app
flutter build windows                       # Build Windows desktop app

# Code quality
flutter analyze                             # Static analysis
flutter test                               # Run tests

# Project cleanup
scripts/clean_and_build.bat                # Clean project and rebuild (Windows)
flutter clean                              # Clean build cache
flutter pub get                            # Get dependencies
```

### Code Generation
The project uses code generation for database models and freezed classes. Always run after modifying:
- Database tables in `lib/data/drift_db.dart`
- Any `.freezed.dart` annotated models

## Architecture Overview

### Clean Architecture Implementation
The project follows Clean Architecture with three main layers:

**Data Layer** (`lib/data/`):
- `drift_db.dart` - SQLite database using Drift ORM with 8 tables
- `repositories/` - Repository implementations  
- `datasources/local/` - Local data sources

**Domain Layer** (`lib/domain/`):
- `entities/` - Business entities (Comic, Bookmark, ReaderSettings)
- `repositories/` - Repository interfaces
- `services/` - Service interfaces

**Presentation Layer** (`lib/presentation/`):
- `bloc/` - BLoC state management
- `pages/` - Screen widgets
- `widgets/` - Reusable UI components

**Core Layer** (`lib/core/`):
- `sync_engine.dart` - WebDAV synchronization engine
- `webdav_service.dart` - WebDAV client wrapper
- `comic_archive.dart` - Archive file processing
- `brightness_service.dart` - Platform-specific brightness control
- `services/` - Core services (cache, settings, etc.)

### Key Dependencies
- **State Management**: `flutter_bloc ^8.1.3` - Event-driven state management
- **Database**: `drift ^2.18.0` - Type-safe SQLite ORM
- **Dependency Injection**: `get_it ^7.6.4` - Service locator pattern
- **Image Handling**: `photo_view ^0.15.0` - Zoomable image viewer
- **Archive Processing**: `archive ^4.0.7` - ZIP file support
- **WebDAV Sync**: `webdav_client ^1.2.2` - Cloud synchronization
- **Background Tasks**: `workmanager ^0.8.0` - Background sync
- **Firebase**: Analytics and Crashlytics integration

### Database Schema
8 main tables with relationships:
- `Comics` - Comic metadata and favorites
- `ComicProgress` - Reading progress with WebDAV sync (ETag support)
- `Bookmarks` - Page bookmarks with thumbnails
- `ReadingSessions` - Reading time tracking
- `ReaderSettings` - User preferences with sync support
- `PageCustomOrder` - Drag-and-drop page reordering
- `ReadingHistory` - Enhanced reading history with sessions
- `BookmarkThumbnails` - Visual bookmark navigation

### WebDAV Synchronization
Intelligent sync engine (`lib/core/sync_engine.dart`) with:
- ETag-based conflict detection
- Local-first design with background sync
- Configurable conflict resolution strategies
- Progress tracking and error handling
- Automatic retry mechanisms

## Code Style Guidelines

### Dart/Flutter Conventions
- **Single quotes**: Always use single quotes for strings
- **Code generation**: Exclude generated files (`*.g.dart`, `*.freezed.dart`)
- **Strict analysis**: Enabled strict-casts, strict-inference, strict-raw-types
- **Import ordering**: Core imports first, then package imports, then relative imports

### State Management Patterns
- Use BLoC pattern for complex state management
- Repository pattern for data access abstraction
- Service locator (GetIt) for dependency injection
- Stream-based reactive programming for real-time updates

### Performance Considerations
- **Memory Management**: Multi-level caching (memory + disk)
- **Image Loading**: Progressive loading with preview/high-res strategy
- **Preloading**: Priority-based page preloading (next 3 high, next 5 medium)
- **Background Tasks**: Use WorkManager for sync operations

## Testing Strategy

### Unit Tests
Focus on business logic in:
- Repository implementations
- Services (sync engine, cache service)
- BLoC event/state handling

### Integration Tests  
Test complete workflows:
- Comic loading and reading
- WebDAV synchronization
- Background task execution

### Performance Tests
Monitor:
- Memory usage during reading sessions
- Image loading performance
- Cache hit rates

## Common Development Tasks

### Adding New Database Tables
1. Define table in `lib/data/drift_db.dart`
2. Update schema version and migration logic
3. Run code generation: `flutter packages pub run build_runner build --delete-conflicting-outputs`
4. Implement DAO methods in the database class

### Implementing New Sync Features
1. Add data model in `lib/models/sync_models.dart`
2. Extend sync engine operations in `lib/core/sync_engine.dart`
3. Update WebDAV service methods if needed
4. Test sync scenarios (local-first, conflicts, errors)

### Creating New UI Components
1. Follow existing widget patterns in `lib/presentation/widgets/`
2. Use BLoC for state management integration
3. Implement responsive design for different screen sizes
4. Add proper accessibility support

## Platform-Specific Notes

### Android
- Native brightness control via Kotlin (`android/app/src/main/kotlin/`)
- Home widget support for reading statistics
- WorkManager background sync integration

### iOS
- Swift brightness control implementation
- Document picker for file access
- Background app refresh for sync

### Windows
- Desktop-specific UI adaptations
- File system access patterns
- Window management integration

## Debugging and Troubleshooting

### Common Issues
- **Database migrations**: Always increment schema version and test migration paths
- **Code generation**: Clean and regenerate after model changes
- **WebDAV sync**: Check network connectivity and credentials
- **Memory pressure**: Monitor cache size limits and cleanup triggers

### Logging
Firebase Crashlytics is integrated for crash reporting. Local logging should focus on:
- Sync operation details
- Cache performance metrics
- Reading session tracking

## Development Workflow

1. **Setup**: Run `flutter pub get` and code generation
2. **Development**: Use `scripts/dev.bat` for rapid iteration
3. **Testing**: Run `flutter analyze` and `flutter test` regularly
4. **Database Changes**: Always run code generation after schema modifications
5. **Performance**: Monitor memory usage during comic reading sessions