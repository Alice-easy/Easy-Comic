# Easy Comic - Android Manga Reader

A feature-rich Android manga reader application built with modern Android development practices and clean architecture.

## Features

- **Comic File Support**: Read ZIP, RAR, CBZ, and CBR comic files
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Reading Experience**: 
  - Zoom, pan, and page navigation
  - Left-to-right and right-to-left reading directions
  - Multiple zoom modes (fit to screen, fill screen, original size)
  - Bookmark support
  - Reading progress tracking
- **Library Management**:
  - Grid layout with covers
  - Search functionality
  - Favorites system
  - Reading statistics
- **Cloud Sync**: WebDAV support for syncing your library across devices
- **Settings**: Customizable themes, reader preferences, and sync options

## Architecture

This application follows clean architecture principles with:

- **Clean Architecture**: Separation of concerns with Domain, Data, and Presentation layers
- **MVVM Pattern**: Model-View-ViewModel with Jetpack Compose
- **Dependency Injection**: Hilt for modern DI
- **Database**: Room for local storage with entities for Manga, Bookmarks, and Reading History
- **Coroutines**: Asynchronous programming with Kotlin coroutines
- **Flow**: Reactive programming with Kotlin Flow

## Project Structure

```
app/src/main/java/com/easycomic/
├── data/
│   ├── local/          # Local data sources
│   ├── remote/         # Remote data sources (WebDAV)
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   └── usecase/        # Business logic use cases
├── presentation/
│   ├── nav/            # Navigation components
│   ├── ui/
│   │   ├── components/  # Reusable UI components
│   │   ├── screens/     # Screen composables
│   │   └── theme/       # Theme and styling
│   └── viewmodel/      # ViewModels for screens
├── core/
│   ├── database/       # Room database entities and DAOs
│   ├── network/        # Network utilities
│   └── util/           # Utility classes
├── di/                 # Dependency injection modules
└── EasyComicApplication.kt  # Application class
```

## Dependencies

### Core
- **Kotlin**: Programming language
- **AndroidX**: Core Android libraries
- **Material Design 3**: UI components and theming

### Architecture
- **Hilt**: Dependency injection
- **Room**: Database ORM
- **DataStore**: Preferences storage
- **Navigation Component**: Navigation between screens

### UI
- **Jetpack Compose**: Modern UI toolkit
- **Coil**: Image loading and caching
- **Material Icons**: Icon library

### Networking
- **Retrofit**: HTTP client
- **OkHttp**: HTTP client with logging

### Testing
- **JUnit**: Unit testing
- **Espresso**: UI testing
- **Mockito**: Mocking framework
- **Truth**: Assertion library

## Screens

### Bookshelf Screen
- Grid layout displaying all manga
- Search functionality
- Add new manga files
- Reading progress indicators
- Favorite status

### Reader Screen
- Full-screen reading experience
- Zoom and pan gestures
- Page navigation
- Bookmark creation
- Reading direction support
- Multiple zoom modes

### Favorites Screen
- Display favorite manga
- Sort options (title, date added, last read, rating)
- Search functionality
- Progress indicators

### Settings Screen
- Theme selection (system, light, dark)
- Reader preferences (direction, zoom mode)
- WebDAV sync configuration
- Library statistics
- Connection testing

## Data Models

### Manga
Represents a comic book with metadata:
- Basic info (title, author, description)
- File information (path, format, size)
- Reading state (current page, progress, favorite status)
- Cover image and page count

### Bookmark
Bookmarks for specific pages:
- Page number and name
- Timestamp and optional notes
- Associated manga reference

### ReadingHistory
Tracks reading progress and statistics:
- Last page read and total pages
- Reading time and sessions
- Completion status
- Average reading speed

## Features Implementation

### Comic File Parsing
- **ZIP/CBZ Support**: Extract images from ZIP archives
- **RAR/CBR Support**: Framework for RAR extraction
- **Image Sorting**: Natural sorting for page order
- **Cover Detection**: Automatic cover image identification
- **File Validation**: Format verification and error handling

### Image Processing
- **Memory Optimization**: Sample size calculation for large images
- **EXIF Rotation**: Automatic image orientation correction
- **Thumbnail Generation**: Optimized thumbnails for covers
- **Caching**: Efficient image caching with Coil

### Reading Experience
- **Gesture Support**: Pinch-to-zoom and pan
- **Page Navigation**: Previous/next and jump to page
- **Bookmark System**: Create and manage bookmarks
- **Progress Tracking**: Automatic progress saving
- **Reading Statistics**: Time tracking and session counting

### Cloud Sync
- **WebDAV Protocol**: Standard web-based file sharing
- **Conflict Resolution**: Timestamp-based conflict handling
- **Offline Support**: Full offline functionality with sync on demand
- **Error Handling**: Comprehensive sync error management

## Setup and Build

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.7.0 or later
- Android Gradle Plugin 7.3.0 or later

### Build Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on emulator or device

### Key Configuration
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 33 (Android 13.0)
- Build Tools: 33.0.0

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Future Enhancements

- PDF comic support
- Cloud backup services (Google Drive, Dropbox)
- Reading statistics and analytics
- Custom reading modes and themes
- Series management and organization
- Annotation and highlighting features
- Multi-language support
- Performance optimizations for large libraries