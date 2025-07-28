# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy Comic is a Flutter-based comic reader app that focuses on privacy and data sovereignty. It reads local comic archive files (CBZ/ZIP) and syncs reading progress to user-controlled WebDAV servers.

## Core Architecture

### Data Layer
- **Drift Database** (`lib/data/drift_db.dart`): Local SQLite database with three main tables:
  - `Comics`: Stores comic file metadata
  - `ComicProgress`: Tracks reading progress with sync information (etag for WebDAV sync)
  - `ReadingSessions`: Records reading time for analytics
- **Sync Models** (`lib/models/sync_models.dart`): Data transfer objects for sync operations

### Core Services
- **SyncEngine** (`lib/core/sync_engine.dart`): Implements bidirectional sync with WebDAV using ETags for conflict detection
- **WebDAVService** (`lib/core/webdav_service.dart`): Handles all WebDAV operations (upload/download/list)
- **ComicArchive** (`lib/core/comic_archive.dart`): Processes ZIP/CBZ files to extract comic pages
- **BackgroundTaskManager** (`lib/core/background_task_manager.dart`): Manages periodic sync tasks

### UI Layer
- **HomePage** (`lib/home/home_page.dart`): Main interface with comic library and file picking
- **ReaderPage** (`lib/reader/reader_page.dart`): Comic reading interface using PhotoView for image display
- **SettingsPage** (`lib/settings/settings_page.dart`): WebDAV configuration and app settings

### State Management
- Uses **Riverpod** for state management
- Key providers defined in `lib/main.dart`:
  - `dbProvider`: Database instance
  - `settingsStoreProvider`: App settings
  - `seedColorProvider`: Theme color

## Development Commands

### Setup & Dependencies
```bash
flutter pub get
```

### Code Generation (Required for Drift)
```bash
flutter packages pub run build_runner build --delete-conflicting-outputs
```

### Development (Windows)
```bash
scripts/dev.bat
# Runs: flutter run -d windows
```

### Clean & Rebuild (Windows)
```bash
scripts/clean_and_build.bat
# Runs: flutter clean && flutter pub get && build_runner build
```

### Standard Flutter Commands
```bash
flutter run                    # Run on default device
flutter build apk             # Build Android APK
flutter analyze               # Static analysis
flutter test                  # Run tests
```

## Code Style & Linting

- Strict linting rules defined in `analysis_options.yaml`
- Uses `package:flutter_lints/flutter.yaml` as base
- Key rules:
  - `prefer_single_quotes: true`
  - Strict type analysis enabled
  - Generated files (`**/*.g.dart`) excluded from analysis

## Sync Architecture

The app implements a sophisticated WebDAV sync system:

1. **Local-First**: All data stored locally in SQLite
2. **Progress-Only Sync**: Only reading progress synced, not comic files
3. **ETag-Based Conflict Detection**: Uses WebDAV ETags to detect changes
4. **Conflict Resolution**: Configurable (local-first by default)

The sync flow compares local and remote data using timestamps and ETags to determine upload/download/conflict operations.

## Key Dependencies

- `flutter_riverpod`: State management
- `drift`: Local database ORM with code generation
- `webdav_client`: WebDAV protocol implementation
- `photo_view`: Image viewer with zoom/pan support
- `archive`: ZIP/CBZ file processing
- `workmanager`: Background task scheduling
- `firebase_core`, `firebase_crashlytics`, `firebase_analytics`: Firebase integration

## Testing Notes

Currently no test framework is set up. When adding tests, check the existing project structure and add appropriate test configurations to `pubspec.yaml`.