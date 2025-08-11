# Easy Comic Phase 2 Integration Diagram

## Component Integration Overview

### Data Flow Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
├─────────────────────────────────────────────────────────────────┤
│  BookshelfScreen  ←→  BookshelfViewModel  ←→  ReaderViewModel  │
│        ↓                    ↓                      ↓           │
│  FileImportScreen  ←→  ImportViewModel       ReaderScreen      │
│        ↓                    ↓                      ↓           │
│  Navigation Controller (AppNavigation)                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Domain Layer                             │
├─────────────────────────────────────────────────────────────────┤
│  ImportComicFileUseCase     SaveReadingProgressUseCase           │
│        ↓                              ↓                        │
│  ParseAndSaveComicUseCase   GetMangaByIdUseCase                  │
│        ↓                              ↓                        │
│  GetAllMangaUseCase        UpdateReadingProgressUseCase           │
│        ↓                              ↓                        │
│  SearchMangaUseCase        ToggleFavoriteUseCase                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                          Data Layer                              │
├─────────────────────────────────────────────────────────────────┤
│  MangaRepositoryImpl       ComicParser                         │
│        ↓                              ↓                        │
│  MangaDao                ImageLoader                         │
│        ↓                              ↓                        │
│  MangaEntity (Database)   File System                        │
│        ↓                              ↓                        │
│  BookmarkEntity           CoverImageExtractor                 │
│        ↓                              ↓                        │
│  ReadingHistoryEntity     FileImportHelper                   │
└─────────────────────────────────────────────────────────────────┘
```

## Key Integration Points

### 1. File Import Workflow

```
User Action → File Picker → FileImportScreen → ImportViewModel → ImportComicFileUseCase → ParseAndSaveComicUseCase → ComicParser → MangaRepository → Database
```

**Step-by-Step Integration:**

1. **User Interface Layer**
   - `FileImportScreen`: Handles file picker UI and import progress display
   - `ImportViewModel`: Manages import state, error handling, and user feedback
   - `BookshelfScreen`: Displays imported comics and triggers import flow

2. **Domain Layer Processing**
   - `ImportComicFileUseCase`: Orchestrates the entire import workflow
   - `ParseAndSaveComicUseCase`: Connects ComicParser with database operations
   - File validation and error processing

3. **Data Layer Operations**
   - `ComicParser`: Extracts comic metadata and page data from files
   - `MangaRepositoryImpl`: Saves parsed data to database with proper mapping
   - `CoverImageExtractor`: Processes cover images for display and storage

### 2. Reading Experience Flow

```
Bookshelf Comic Click → Navigation → ReaderScreen → ReaderViewModel → GetMangaByIdUseCase → MangaRepository → Database → ComicParser → ImageLoader → Display
```

**Step-by-Step Integration:**

1. **Navigation and Initialization**
   - `AppNavigation`: Routes user from bookshelf to reader with manga ID
   - `ReaderViewModel`: Loads comic data from database using manga ID
   - `ReaderScreen`: Displays comic pages and handles user interactions

2. **Data Loading and Processing**
   - `GetMangaByIdUseCase`: Retrieves comic metadata from database
   - `ComicParser`: Loads specific page data on demand
   - `ImageLoader`: Processes and optimizes images for display

3. **Progress Management**
   - `SaveReadingProgressUseCase`: Automatically saves reading progress
   - `MangaRepository`: Updates database with current page and timestamp
   - `ReadingHistoryRepository`: Tracks reading sessions for analytics

### 3. Data Synchronization Flow

```
Database Changes → Repository Flow → ViewModel State → UI Updates
```

**Real-time Data Flow:**

1. **Database to UI Flow**
   - Room database emits Flow updates when data changes
   - Repository implementations transform entity data to domain models
   - ViewModels collect Flow and update UI state accordingly
   - Compose UI recomposes with new data automatically

2. **User Actions to Database Flow**
   - User interactions trigger ViewModel methods
   - ViewModels call appropriate Use Cases
   - Use Cases interact with Repositories
   - Repositories update database through DAOs
   - Database changes propagate back through Flow

## Critical Integration Challenges

### 1. Model Transformation

**Problem:** Different layers use different model representations
- `Comic` (parser) ↔ `Manga` (domain) ↔ `MangaEntity` (database)

**Solution:** Implement proper mapping functions in repository layer:
```kotlin
// In MangaRepositoryImpl.kt
private fun Comic.toDomainManga(): Manga {
    return Manga(
        title = this.title,
        filePath = this.filePath,
        pageCount = this.pageCount,
        currentPage = this.currentPage,
        coverImagePath = this.coverImagePath,
        // Map other fields appropriately
    )
}

private fun Manga.toEntity(): MangaEntity {
    return MangaEntity(
        title = this.title,
        filePath = this.filePath,
        pageCount = this.pageCount,
        currentPage = this.currentPage,
        readingStatus = this.readingStatus,
        // Map other fields appropriately
    )
}
```

### 2. Error Handling Propagation

**Problem:** Errors can occur at any layer and need to be handled gracefully

**Solution:** Implement sealed class error hierarchies:
```kotlin
sealed class ImportResult {
    data class Loading(val progress: Float) : ImportResult()
    data class Success(val mangaId: Long) : ImportResult()
    data class Error(val message: String, val type: ErrorType) : ImportResult()
}

enum class ErrorType {
    FILE_NOT_FOUND,
    UNSUPPORTED_FORMAT,
    CORRUPT_FILE,
    DATABASE_ERROR,
    STORAGE_FULL
}
```

### 3. Memory Management

**Problem:** Large comic files can cause memory issues

**Solution:** Implement streaming and pagination:
```kotlin
class ComicParser {
    suspend fun parseComicFile(filePath: String): Flow<ParseResult> = flow {
        // Parse metadata first
        emit(ParseResult.Loading(0.1f))
        val metadata = extractMetadata(filePath)
        
        // Parse pages incrementally
        val totalPages = metadata.pageCount
        for (i in 0 until totalPages) {
            val progress = 0.3f + (i.toFloat() / totalPages) * 0.6f
            emit(ParseResult.Loading(progress))
            
            val page = parsePage(filePath, i)
            // Emit page data or store for later use
        }
        
        emit(ParseResult.Success(metadata))
    }
}
```

### 4. State Management

**Problem:** Multiple UI states need to be managed consistently

**Solution:** Use StateFlow for reactive state management:
```kotlin
class BookshelfViewModel {
    private val _uiState = MutableStateFlow(BookshelfUiState())
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()
    
    // Combine states for complex UI scenarios
    val combinedState: StateFlow<CombinedUiState> = combine(
        uiState,
        importState
    ) { ui, import -> CombinedUiState(ui, import) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), CombinedUiState())
}
```

## Integration Dependencies

### Required Dependencies Matrix

| Component | Depends On | Integration Point | Risk Level |
|-----------|------------|------------------|------------|
| FileImportScreen | ImportViewModel | State observation | Low |
| ImportViewModel | ImportComicFileUseCase | Use case execution | Medium |
| ImportComicFileUseCase | ParseAndSaveComicUseCase | Data flow | Medium |
| ParseAndSaveComicUseCase | ComicParser, MangaRepository | File parsing and storage | High |
| ReaderViewModel | GetMangaByIdUseCase | Data loading | Medium |
| GetMangaByIdUseCase | MangaRepository | Database access | Low |
| MangaRepositoryImpl | MangaDao, ComicParser | Data transformation | Medium |
| ImageLoader | MangaRepository | Page data retrieval | Low |

### Critical Path Analysis

1. **Highest Risk**: `ParseAndSaveComicUseCase` → `ComicParser` → `MangaRepository`
   - Complex file parsing operations
   - Database transactions
   - Memory intensive operations

2. **Medium Risk**: `ImportViewModel` → `ImportComicFileUseCase`
   - User interface responsiveness
   - Error handling complexity

3. **Low Risk**: Database operations through `MangaRepository`
   - Well-established Room database patterns
   - Proven technology stack

## Testing Integration Points

### Integration Test Strategy

1. **Repository Layer Tests**
   ```kotlin
   @Test
   fun `manga repository should save parsed comic correctly`() = runTest {
       // Given
       val comic = createTestComic()
       val repository = MangaRepositoryImpl(mockDao, mockParser)
       
       // When
       val mangaId = repository.saveParsedComic(comic, pages)
       
       // Then
       val savedManga = repository.getMangaById(mangaId)
       assertNotNull(savedManga)
       assertEquals(comic.title, savedManga?.title)
   }
   ```

2. **Use Case Integration Tests**
   ```kotlin
   @Test
   fun `import use case should handle complete workflow`() = runTest {
       // Given
       val useCase = ImportComicFileUseCase(mockRepository, mockParser)
       val testFile = createTestFile()
       
       // When
       useCase(ImportComicFileUseCase.Params(testFile.path)).collect { result ->
           // Then
           when (result) {
               is ImportResult.Success -> {
                   assertTrue(result.mangaId > 0)
               }
               is ImportResult.Error -> {
                   fail("Import should succeed")
               }
           }
       }
   }
   ```

3. **ViewModel Integration Tests**
   ```kotlin
   @Test
   fun `bookshelf view model should update ui on import`() = runTest {
       // Given
       val viewModel = BookshelfViewModel(mockUseCases)
       val testFile = createTestFile()
       
       // When
       viewModel.importComicFile(testFile.path)
       
       // Then
       val uiState = viewModel.uiState.first()
       assertFalse(uiState.mangaList.isEmpty())
       assertEquals(testFile.nameWithoutExtension, uiState.mangaList.first().title)
   }
   ```

This integration diagram provides a comprehensive view of how the existing components will be connected in Phase 2, highlighting the critical integration points, data flow patterns, and potential challenges that need to be addressed during implementation.