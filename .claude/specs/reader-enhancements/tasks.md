# Reader Enhancements Implementation Tasks

## Database and Models Implementation

### 1. Extend Database Schema

- [ ] Add ReaderSettings table to drift_db.dart with columns for reading mode, background color, transition type, brightness, and sync metadata
  - References requirements: 1, 7, 8, 11
  - Create table definition with proper constraints and defaults
  - Add migration logic to handle schema version increment
  - Include ETag column for WebDAV sync compatibility

- [ ] Add PageCustomOrder table to drift_db.dart for storing custom page arrangements
  - References requirements: 10, 11
  - Include foreign key relationship to Comics table
  - Add unique constraints for originalIndex per comic
  - Create indexes for efficient querying

- [ ] Add ReadingHistory table to drift_db.dart for automatic history tracking
  - References requirements: 6, 11
  - Include session grouping and time tracking fields
  - Add cleanup logic for history size limits
  - Create compound indexes for date-based queries

- [ ] Add BookmarkThumbnails table to drift_db.dart for enhanced bookmark visualization
  - References requirements: 5, 11
  - Link to existing Bookmarks table via foreign key
  - Include local file path for thumbnail storage
  - Add cascade delete rules

### 2. Create Data Models and Enums

- [ ] Create reader_models.dart with ReaderSettings, ReadingMode, TransitionType, and NavigationDirection enums
  - References requirements: 1, 2, 7, 8
  - Define serialization methods for settings persistence
  - Include validation methods for enum values
  - Add default value constants

- [ ] Create enhanced bookmark models with thumbnail support in reader_models.dart
  - References requirements: 5
  - Extend existing bookmark functionality
  - Include thumbnail path and metadata
  - Add helper methods for thumbnail operations

- [ ] Create page ordering models in reader_models.dart for custom page arrangement
  - References requirements: 10
  - Include original and custom index mappings
  - Add validation for order integrity
  - Create helper methods for order transformations

## Core Service Layer Implementation

### 3. Implement Reading Strategy Pattern

- [ ] Create abstract ReadingStrategy class in lib/reader/strategies/reading_strategy.dart
  - References requirements: 1, 2
  - Define interface for different reading modes
  - Include methods for widget building and gesture handling
  - Add abstract methods for page navigation logic

- [ ] Implement SinglePageStrategy class for single-page reading mode
  - References requirements: 1, 3
  - Build PhotoViewGallery for single page display
  - Handle horizontal navigation gestures
  - Implement zoom and pan functionality

- [ ] Implement DualPageStrategy class for dual-page reading mode
  - References requirements: 1, 3
  - Create side-by-side page layout for wide screens
  - Handle page pairing logic for odd page counts
  - Implement synchronized zoom across both pages

- [ ] Implement VerticalScrollStrategy class for continuous scrolling
  - References requirements: 2, 3
  - Create vertically scrollable page layout
  - Implement scroll position to page index mapping
  - Add smooth scrolling animations

### 4. Create Reader State Management

- [ ] Create ReaderStateNotifier class in lib/reader/providers/reader_state_provider.dart
  - References requirements: 1, 2, 7, 8, 11
  - Manage all reader-specific state using Riverpod StateNotifier
  - Implement methods for settings persistence and restoration
  - Add debounced saving to prevent excessive database writes

- [ ] Create ReaderSettingsProvider in lib/reader/providers/settings_provider.dart
  - References requirements: 11
  - Provide reactive access to reader settings
  - Handle settings synchronization with database
  - Implement settings conflict resolution for sync

- [ ] Create ThumbnailCacheProvider in lib/reader/providers/thumbnail_provider.dart
  - References requirements: 5, 9, 10
  - Manage thumbnail generation and caching
  - Implement LRU cache with memory limits
  - Add background thumbnail generation

### 5. Implement Core Services

- [ ] Create BrightnessService class in lib/core/brightness_service.dart
  - References requirements: 4
  - Implement app-specific brightness control
  - Create brightness overlay widget for non-intrusive adjustment
  - Add brightness persistence and restoration

- [ ] Create ThumbnailService class in lib/core/thumbnail_service.dart
  - References requirements: 5, 9, 10
  - Implement efficient thumbnail generation from comic pages
  - Add file system caching with automatic cleanup
  - Include memory pressure handling and cache size management

- [ ] Create PageOrderService class in lib/core/page_order_service.dart
  - References requirements: 10
  - Handle custom page ordering logic
  - Implement drag-and-drop reordering algorithms
  - Add validation and integrity checks for custom orders

## UI Component Development

### 6. Enhance ReaderPage with New Architecture

- [ ] Refactor ReaderPage to use strategy pattern in lib/reader/reader_page.dart
  - References requirements: 1, 2, 3
  - Integrate ReadingStrategy implementations
  - Update gesture handling to work with different strategies
  - Maintain backward compatibility with existing functionality

- [ ] Add reading mode switcher to ReaderPage UI
  - References requirements: 1, 2
  - Create mode selection buttons in reader toolbar
  - Implement smooth transitions between reading modes
  - Add visual indicators for current reading mode

- [ ] Implement brightness control overlay in ReaderPage
  - References requirements: 4
  - Add brightness slider to reader controls
  - Create semi-transparent overlay for brightness adjustment
  - Implement smooth brightness transition animations

### 7. Create Enhanced Progress Bar Component

- [ ] Create ThumbnailProgressBar widget in lib/reader/widgets/thumbnail_progress_bar.dart
  - References requirements: 9
  - Implement custom slider with thumbnail preview on drag
  - Add real-time page number feedback during interaction
  - Include loading states for thumbnail generation

- [ ] Integrate ThumbnailProgressBar into ReaderPage bottom navigation
  - References requirements: 9
  - Replace existing basic slider with enhanced version
  - Add thumbnail cache integration
  - Implement smooth scrolling to selected pages

### 8. Develop Page Management Interface

- [ ] Create PageManagerDialog widget in lib/reader/widgets/page_manager_dialog.dart
  - References requirements: 10
  - Build reorderable grid view for page thumbnails
  - Implement drag-and-drop reordering functionality
  - Add visual feedback during reordering operations

- [ ] Add page manager access button to ReaderPage toolbar
  - References requirements: 10
  - Include page management icon in reader controls
  - Implement modal dialog for page reordering
  - Add confirmation dialog for order changes

### 9. Create Reader Settings Panel

- [ ] Create ReaderSettingsPanel widget in lib/reader/widgets/reader_settings_panel.dart
  - References requirements: 1, 2, 4, 7, 8
  - Build comprehensive settings interface for all reader options
  - Implement real-time preview of setting changes
  - Add reset to defaults functionality

- [ ] Integrate ReaderSettingsPanel into ReaderPage menu
  - References requirements: 1, 2, 4, 7, 8
  - Add settings access button to reader toolbar
  - Implement slide-out or modal settings panel
  - Add settings persistence on panel close

## Enhanced Features Implementation

### 10. Implement Background Theme System

- [ ] Create BackgroundThemeService in lib/core/background_theme_service.dart
  - References requirements: 7
  - Define preset background color themes
  - Implement theme application logic
  - Add contrast validation for UI elements

- [ ] Add background theme selector to reader settings
  - References requirements: 7
  - Create color picker or preset selection interface
  - Implement live preview of background changes
  - Add custom color support with color picker

### 11. Develop Page Transition Animations

- [ ] Create TransitionService class in lib/core/transition_service.dart
  - References requirements: 8
  - Implement different page transition animations
  - Create slide, fade, and curl transition effects
  - Add transition duration and easing configuration

- [ ] Integrate page transitions into reading strategies
  - References requirements: 8
  - Apply selected transitions during page navigation
  - Ensure transitions work with all reading modes
  - Add transition performance optimization

### 12. Enhance Bookmark System

- [ ] Extend bookmark functionality in ReaderPage for thumbnail support
  - References requirements: 5
  - Update bookmark creation to generate thumbnails
  - Modify bookmark list to display thumbnail previews
  - Add batch bookmark management operations

- [ ] Create BookmarkThumbnailWidget in lib/reader/widgets/bookmark_thumbnail.dart
  - References requirements: 5
  - Display bookmark thumbnails with fallback handling
  - Implement lazy loading for bookmark thumbnails
  - Add thumbnail regeneration on demand

### 13. Implement Reading History System

- [ ] Create ReadingHistoryService in lib/core/reading_history_service.dart
  - References requirements: 6
  - Implement automatic history recording
  - Add history size management and cleanup
  - Create history search and filtering functionality

- [ ] Add reading history access to home page navigation
  - References requirements: 6
  - Create history view interface
  - Implement history-based comic resumption
  - Add history clearing and management options

## Testing and Quality Assurance

### 14. Create Unit Tests

- [ ] Write unit tests for all service classes in test/services/
  - References all requirements
  - Test strategy pattern implementations
  - Validate state management behavior
  - Test database operations and migrations

- [ ] Write unit tests for new data models in test/models/
  - References requirements: 1, 2, 5, 6, 7, 8, 10, 11
  - Test serialization and deserialization
  - Validate enum value handling
  - Test model validation methods

### 15. Create Integration Tests

- [ ] Write integration tests for reader functionality in test/integration/
  - References all requirements
  - Test end-to-end reader workflows
  - Validate settings persistence and sync
  - Test performance under various conditions

- [ ] Write widget tests for new UI components in test/widgets/
  - References requirements: 1, 2, 4, 5, 7, 8, 9, 10
  - Test widget interactions and state changes
  - Validate gesture handling
  - Test error states and recovery

## Performance Optimization and Polish

### 16. Optimize Memory Usage and Performance

- [ ] Implement memory-efficient thumbnail caching
  - References requirements: 5, 9, 10
  - Add memory pressure monitoring
  - Implement intelligent cache eviction
  - Optimize thumbnail generation performance

- [ ] Optimize database queries for new tables
  - References requirements: 5, 6, 10, 11
  - Add proper indexes for frequent queries
  - Implement query result caching where appropriate
  - Optimize batch operations for better performance

### 17. Add Error Handling and Recovery

- [ ] Implement comprehensive error handling for all new features
  - References all requirements
  - Add graceful fallbacks for failed operations
  - Implement retry mechanisms for transient failures
  - Add user-friendly error messages and recovery options

- [ ] Add logging and crash reporting for new functionality
  - References all requirements
  - Integrate with existing Firebase Crashlytics
  - Add custom event tracking for new features
  - Implement debug logging for troubleshooting

### 18. Final Integration and Testing

- [ ] Integrate all new features with existing WebDAV sync system
  - References requirements: 11
  - Ensure settings sync compatibility
  - Test conflict resolution mechanisms
  - Validate sync performance impact

- [ ] Perform comprehensive testing across different devices and screen sizes
  - References all requirements
  - Test on various Android and iOS devices
  - Validate performance on different screen sizes
  - Test memory usage with large comic collections