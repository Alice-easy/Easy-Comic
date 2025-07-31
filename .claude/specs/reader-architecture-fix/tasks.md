# Reader Architecture Consolidation Tasks

## 1. Model Consolidation
- [ ] **Merge duplicate reader_models.dart files**
  - Analyze differences between `lib/models/reader_models.dart` and `lib/reader/models/reader_models.dart`
  - Consolidate enums with consistent values (ReadingMode, NavigationDirection, TransitionType)
  - Merge model classes, keeping the best features from both
  - Update all import statements across the codebase
  - *References: Requirements 1.1, 1.2*

- [ ] **Create unified ReaderSettings model**
  - Add comprehensive settings class with all reader preferences
  - Implement JSON serialization for settings persistence
  - Add validation methods and default values
  - Create migration logic for existing settings
  - *References: Requirements 6.1, 6.2, 6.3*

## 2. Service Unification
- [ ] **Consolidate brightness services**
  - Enhance `lib/core/brightness_service.dart` with features from `lib/services/brightness_service.dart`
  - Implement hybrid brightness control (overlay + system)
  - Remove duplicate `lib/services/brightness_service.dart`
  - Update all imports to use unified service
  - Add comprehensive error handling
  - *References: Requirements 1.2, 5.2*

- [ ] **Create unified error handling system**
  - Design consistent error types for reader operations
  - Implement error recovery mechanisms
  - Add user-friendly error messages
  - Create error boundary components
  - *References: Requirements 5.1, 5.2, 5.4*

## 3. ReaderPage Architecture Refactoring
- [ ] **Extract ReaderCore widget**
  - Create standalone widget for core image display logic (~150 lines)
  - Implement PhotoView integration with gesture handling
  - Add image caching and memory management
  - Include page transition animations
  - *References: Requirements 2.1, 2.2, 4.1*

- [ ] **Create NavigationHandler mixin**
  - Extract navigation logic into reusable mixin (~100 lines)
  - Implement tap zones, volume keys, swipe gestures
  - Add keyboard shortcut support
  - Ensure consistent behavior across reading modes
  - *References: Requirements 2.3, 6.3*

- [ ] **Create SettingsHandler mixin**
  - Extract settings management logic (~80 lines)
  - Implement brightness overlay integration
  - Add reading mode switching logic
  - Handle settings persistence and restoration
  - *References: Requirements 2.3, 6.1, 6.2*

- [ ] **Create BookmarkManager mixin**
  - Extract bookmark operations (~70 lines)
  - Implement add/remove/navigate bookmark functionality
  - Add thumbnail generation for bookmarks
  - Create bookmark UI integration
  - *References: Requirements 2.3, 6.4*

- [ ] **Create ProgressTracker mixin**
  - Extract progress tracking logic (~60 lines)
  - Implement reading time tracking
  - Add Firebase analytics integration
  - Handle sync coordination for progress
  - *References: Requirements 2.3, 4.3*

- [ ] **Refactor main ReaderPage class**
  - Reduce ReaderPage to orchestration logic (<150 lines)
  - Integrate all extracted components and mixins
  - Ensure proper state management flow
  - Add comprehensive documentation
  - *References: Requirements 2.1, 2.2*

## 4. Dead Code Elimination
- [ ] **Remove unused strategy pattern files**
  - Delete all files in `lib/reader/strategies/` directory
  - Remove strategy-related imports from ReaderPage
  - Clean up any references in provider definitions
  - Update documentation to reflect simplified architecture
  - *References: Requirements 3.1, 3.3*

- [ ] **Clean up redundant imports and dependencies**
  - Scan all files for unused imports
  - Remove redundant dependencies from pubspec.yaml
  - Clean up unused provider definitions
  - Remove any orphaned widget files
  - *References: Requirements 3.2, 3.4*

## 5. Performance Optimization Implementation
- [ ] **Implement image caching system**
  - Create ImageCacheManager with LRU eviction
  - Add preloading for adjacent pages (±3 pages buffer)
  - Implement memory usage monitoring
  - Add cache size limits and cleanup logic
  - *References: Requirements 4.1, 4.3*

- [ ] **Optimize database operations**
  - Identify and eliminate redundant queries in ReaderPage
  - Implement proper state management to prevent unnecessary DB calls
  - Add database query optimization for bookmarks and progress
  - Create efficient batch operations for bulk updates
  - *References: Requirements 4.3*

- [ ] **Add memory management for page images**
  - Implement automatic disposal of off-screen images
  - Add memory pressure detection
  - Create efficient image loading pipeline
  - Add memory usage debugging tools
  - *References: Requirements 4.2, 4.4*

## 6. Type Safety and Provider Fixes
- [ ] **Fix provider type mismatches**
  - Audit all provider definitions in ReaderPage
  - Ensure provider return types match consumer expectations
  - Remove any runtime type casting
  - Add type-safe provider factories where needed
  - *References: Requirements 1.4, 5.1*

- [ ] **Enhance error handling in providers**
  - Add proper error states to all reader providers
  - Implement graceful error recovery
  - Create consistent error UI components
  - Add retry mechanisms for failed operations
  - *References: Requirements 5.2, 5.4*

## 7. Testing Implementation
- [ ] **Create unit tests for consolidated models**
  - Test model serialization/deserialization
  - Verify enum consistency and conversion
  - Test model validation methods
  - Add edge case testing for model operations
  - *References: Requirements 2.4*

- [ ] **Create widget tests for extracted components**
  - Test ReaderCore widget rendering and gestures
  - Test navigation handler functionality
  - Test settings panel integration
  - Test bookmark UI components
  - *References: Requirements 2.4*

- [ ] **Create integration tests for reading workflow**
  - Test complete page navigation flow
  - Test settings persistence and restoration
  - Test bookmark operations end-to-end
  - Test error recovery scenarios
  - *References: Requirements 2.4*

## 8. Documentation and Code Quality
- [ ] **Update code documentation**
  - Add comprehensive documentation for all new components
  - Update existing documentation to reflect architectural changes
  - Create architecture decision records (ADR) for major changes
  - Add code examples for common usage patterns
  - *References: All requirements*

- [ ] **Run quality validation**
  - Execute linting and static analysis
  - Verify all tests pass
  - Check code coverage metrics
  - Run performance benchmarks
  - Validate against quality assessment criteria
  - *References: All requirements*