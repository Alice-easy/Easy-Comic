# Phase 1 Technical Specification - Easy Comic Android App

## Problem Statement
- **Business Issue**: The Easy Comic Android app currently has basic file parsing functionality and a simple UI, but lacks proper Clean Architecture implementation, database persistence, dependency injection, and structured navigation.
- **Current State**: The app has comic parsing (ZIP/CBZ, RAR/CBR) and basic Compose UI, but all architectural components are in `*_disabled/` folders. The app uses a simplified application class without DI, and navigation is commented out.
- **Expected Outcome**: Complete Clean Architecture implementation with Room database, Hilt dependency injection, proper navigation, repository pattern, domain layer with use cases, and Android permission handling, resulting in a fully functional comic reader app with proper architecture.

## Solution Overview
- **Approach**: Enable and integrate the existing Clean Architecture components from disabled folders, implement Hilt dependency injection, set up Room database, establish proper navigation flow, and add permission handling for file access.
- **Core Changes**: Move components from `data_disabled/` and `domain_disabled/` to active packages, configure Hilt modules, set up Room database, implement navigation architecture, create file permission handlers, and integrate all components with the UI layer.
- **Success Criteria**: App starts with proper DI, database operations work, navigation flows between screens, file permissions are handled correctly, and all architectural layers are properly connected.

## Technical Implementation

### Database Changes
- **Tables to Modify**: Enable existing database schema from `data_disabled/database/AppDatabase.kt`
- **New Tables**: No new tables needed - existing schema supports Manga, Bookmark, and ReadingHistory entities
- **Migration Scripts**: Use existing migration structure in AppDatabase

#### Database Schema Details
```kotlin
// Enable from data_disabled/database/AppDatabase.kt
@Database(
    entities = [
        MangaEntity::class,
        BookmarkEntity::class,
        ReadingHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
}
```

#### Entity Structure
```kotlin
// Move from data_disabled/entity/MangaEntity.kt
@Entity(
    tableName = "manga",
    indices = [
        Index("title"),
        Index("author"),
        Index("last_read"),
        Index("date_added"),
        Index("is_favorite")
    ]
)
data class MangaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val description: String = "",
    val filePath: String,
    val fileUri: String? = null,
    val fileFormat: String = "",
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val coverImagePath: String? = null,
    val thumbnailPath: String? = null,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    val tags: String = "",
    val lastRead: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
)
```

### Code Changes

#### 1. Build Configuration
- **Files to Modify**: `C:\001\Comic\Easy-Comic\app\build.gradle.kts`
- **Changes Required**: Add Hilt dependencies, Room dependencies, KAPT plugin

```kotlin
// Add to plugins in app/build.gradle.kts
plugins {
    // ... existing plugins
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
}

// Add to dependencies in app/build.gradle.kts
dependencies {
    // ... existing dependencies
    
    // Hilt for dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // For Hilt testing
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
}
```

#### 2. Application Class
- **Files to Modify**: `C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\EasyComicApplication.kt`
- **Changes Required**: Enable Hilt annotation and initialize DI

```kotlin
@HiltAndroidApp
class EasyComicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("EasyComic Application started with Hilt")
    }
}
```

#### 3. Data Layer Implementation
- **Files to Create**: Move from `data_disabled/` to `app/src/main/java/com/easycomic/data/`
- **Package Structure**:
  - `com.easycomic.data.database` - Room database
  - `com.easycomic.data.dao` - Data access objects
  - `com.easycomic.data.entity` - Database entities
  - `com.easycomic.data.repository` - Repository implementations
  - `com.easycomic.data.di` - Hilt data module

```kotlin
// Create com.easycomic.data.di.DataModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): AppDatabase {
        return AppDatabase.getDatabase(context, applicationScope)
    }
    
    @Provides
    @Singleton
    fun provideMangaDao(database: AppDatabase): MangaDao {
        return database.mangaDao()
    }
    
    @Provides
    @Singleton
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
    
    @Provides
    @Singleton
    fun provideReadingHistoryDao(database: AppDatabase): ReadingHistoryDao {
        return database.readingHistoryDao()
    }
    
    @Provides
    @Singleton
    fun provideMangaRepository(mangaDao: MangaDao): MangaRepository {
        return MangaRepositoryImpl(mangaDao)
    }
    
    @Provides
    @Singleton
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository {
        return BookmarkRepositoryImpl(bookmarkDao)
    }
    
    @Provides
    @Singleton
    fun provideReadingHistoryRepository(readingHistoryDao: ReadingHistoryDao): ReadingHistoryRepository {
        return ReadingHistoryRepositoryImpl(readingHistoryDao)
    }
    
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
```

#### 4. Domain Layer Implementation
- **Files to Create**: Move from `domain_disabled/` to `app/src/main/java/com/easycomic/domain/`
- **Package Structure**:
  - `com.easycomic.domain.model` - Domain models
  - `com.easycomic.domain.repository` - Repository interfaces
  - `com.easycomic.domain.usecase` - Use cases
  - `com.easycomic.domain.di` - Hilt domain module

```kotlin
// Create com.easycomic.domain.di.DomainModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    
    @Binds
    abstract fun bindGetAllMangaUseCase(
        getAllMangaUseCase: GetAllMangaUseCase
    ): GetAllMangaUseCase
    
    @Binds
    abstract fun bindSearchMangaUseCase(
        searchMangaUseCase: SearchMangaUseCase
    ): SearchMangaUseCase
    
    @Binds
    abstract fun bindGetFavoriteMangaUseCase(
        getFavoriteMangaUseCase: GetFavoriteMangaUseCase
    ): GetFavoriteMangaUseCase
    
    @Binds
    abstract fun bindGetRecentMangaUseCase(
        getRecentMangaUseCase: GetRecentMangaUseCase
    ): GetRecentMangaUseCase
    
    @Binds
    abstract fun bindDeleteMangaUseCase(
        deleteMangaUseCase: DeleteMangaUseCase
    ): DeleteMangaUseCase
    
    @Binds
    abstract fun bindDeleteAllMangaUseCase(
        deleteAllMangaUseCase: DeleteAllMangaUseCase
    ): DeleteAllMangaUseCase
    
    @Binds
    abstract fun bindToggleFavoriteUseCase(
        toggleFavoriteUseCase: ToggleFavoriteUseCase
    ): ToggleFavoriteUseCase
    
    @Binds
    abstract fun bindUpdateReadingProgressUseCase(
        updateReadingProgressUseCase: UpdateReadingProgressUseCase
    ): UpdateReadingProgressUseCase
    
    @Binds
    abstract fun bindInsertOrUpdateMangaUseCase(
        insertOrUpdateMangaUseCase: InsertOrUpdateMangaUseCase
    ): InsertOrUpdateMangaUseCase
}
```

#### 5. Presentation Layer Implementation
- **Files to Create**: Move from `ui_*_disabled/` to `app/src/main/java/com/easycomic/ui/`
- **Package Structure**:
  - `com.easycomic.ui.bookshelf` - Bookshelf screen components
  - `com.easycomic.ui.reader` - Reader screen components
  - `com.easycomic.ui.navigation` - Navigation components
  - `com.easycomic.ui.di` - Hilt UI module

```kotlin
// Create com.easycomic.ui.di.UiModule.kt
@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class UiModule {
    
    @Binds
    abstract fun bindBookshelfViewModel(
        bookshelfViewModel: BookshelfViewModel
    ): BookshelfViewModel
    
    @Binds
    abstract fun bindReaderViewModel(
        readerViewModel: ReaderViewModel
    ): ReaderViewModel
}
```

#### 6. Navigation Setup
- **Files to Modify**: `C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\ui\navigation\AppNavigation.kt`
- **Changes Required**: Enable navigation components and integrate ViewModels

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Bookshelf.route
    ) {
        composable(Screen.Bookshelf.route) {
            val viewModel: BookshelfViewModel = hiltViewModel()
            BookshelfScreen(
                viewModel = viewModel,
                navController = navController,
                onMangaClick = { manga ->
                    navController.navigate(Screen.Reader.createRoute(manga.id))
                },
                onAddMangaClick = {
                    // Handle file picker intent
                }
            )
        }
        
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("mangaId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val viewModel: ReaderViewModel = hiltViewModel()
            val mangaId = backStackEntry.arguments?.getLong("mangaId") ?: 0L
            
            LaunchedEffect(mangaId) {
                viewModel.setMangaId(mangaId)
            }
            
            ReaderScreen(
                viewModel = viewModel,
                navController = navController,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

### API Changes
- **Endpoints**: No REST API endpoints needed for Phase 1 (local database only)
- **Request/Response**: Database queries via Room DAOs
- **Validation Rules**: Input validation in ViewModels and use cases

### Configuration Changes
- **Settings**: Create `res/xml/file_paths.xml` for FileProvider
- **Environment Variables**: None needed for local database
- **Feature Flags**: None needed for Phase 1

#### FileProvider Configuration
```xml
<!-- Create res/xml/file_paths.xml -->
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="my_images" path="Pictures/" />
    <external-cache-path name="my_cache" path="." />
    <files-path name="my_files" path="." />
</paths>
```

## Implementation Sequence

### Phase 1: Infrastructure Setup
1. **Enable Gradle Dependencies** - Add Hilt and Room to `app/build.gradle.kts`
2. **Update Application Class** - Add `@HiltAndroidApp` annotation to `EasyComicApplication.kt`
3. **Update AndroidManifest** - Ensure application class reference is correct

### Phase 2: Data Layer Implementation
1. **Move Database Components** - Copy from `data_disabled/` to `app/src/main/java/com/easycomic/data/`
2. **Create Data Hilt Module** - Create `com.easycomic.data.di.DataModule.kt`
3. **Set up Room Converters** - Move `Converters.kt` for type conversion

### Phase 3: Domain Layer Implementation
1. **Move Domain Components** - Copy from `domain_disabled/` to `app/src/main/java/com/easycomic/domain/`
2. **Create Domain Hilt Module** - Create `com.easycomic.domain.di.DomainModule.kt`
3. **Update Import Statements** - Fix domain layer imports after moving

### Phase 4: Presentation Layer Implementation
1. **Move UI Components** - Copy from `ui_*_disabled/` to `app/src/main/java/com/easycomic/ui/`
2. **Create UI Hilt Module** - Create `com.easycomic.ui.di.UiModule.kt`
3. **Enable Navigation** - Uncomment and update `AppNavigation.kt`
4. **Update MainActivity** - Replace basic UI with navigation and proper theme setup

### Phase 5: Permission Handling Implementation
1. **Create Permission Handler** - Implement file permission handling utility
2. **Update MainActivity** - Add permission request logic
3. **Update BookshelfScreen** - Add file picker integration with permission checks

### Phase 6: Integration and Testing
1. **Hilt Integration** - Ensure all components are properly injected
2. **Database Testing** - Verify Room database operations
3. **Navigation Testing** - Test navigation flows between screens
4. **Permission Testing** - Test file access permission handling

## Validation Plan

### Unit Tests
- **Database Tests**: Verify DAO operations in `MangaDaoTest.kt`
- **Repository Tests**: Test repository implementations with mock data
- **Use Case Tests**: Verify business logic in use cases
- **ViewModel Tests**: Test UI state management and user interactions

### Integration Tests
- **Database Integration**: Test Room database with real data
- **DI Integration**: Verify Hilt provides all dependencies correctly
- **Navigation Integration**: Test navigation between Bookshelf and Reader screens
- **File Integration**: Test comic file parsing and display

### Business Logic Verification
- **Comic Loading**: Verify comics can be loaded and stored in database
- **Reading Progress**: Test progress tracking and persistence
- **Search Functionality**: Verify search works across comic metadata
- **Favorite System**: Test favorite toggle and filtering
- **Navigation Flow**: Verify user can navigate from bookshelf to reader and back

### Performance Testing
- **Database Performance**: Test database queries with large comic collections
- **Image Loading**: Verify image loading performance with large comic files
- **Memory Usage**: Monitor memory usage during comic reading

## File Structure After Implementation

```
app/src/main/java/com/easycomic/
├── EasyComicApplication.kt
├── MainActivity.kt
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   └── Converters.kt
│   ├── dao/
│   │   ├── MangaDao.kt
│   │   ├── BookmarkDao.kt
│   │   └── ReadingHistoryDao.kt
│   ├── entity/
│   │   ├── MangaEntity.kt
│   │   ├── BookmarkEntity.kt
│   │   └── ReadingHistoryEntity.kt
│   ├── repository/
│   │   ├── MangaRepositoryImpl.kt
│   │   ├── BookmarkRepositoryImpl.kt
│   │   └── ReadingHistoryRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt
├── domain/
│   ├── model/
│   │   ├── Manga.kt
│   │   ├── Bookmark.kt
│   │   └── ReadingHistory.kt
│   ├── repository/
│   │   ├── MangaRepository.kt
│   │   ├── BookmarkRepository.kt
│   │   └── ReadingHistoryRepository.kt
│   ├── usecase/
│   │   ├── BaseUseCase.kt
│   │   └── manga/
│   │       └── MangaUseCases.kt
│   └── di/
│       └── DomainModule.kt
├── ui/
│   ├── bookshelf/
│   │   ├── BookshelfScreen.kt
│   │   ├── BookshelfViewModel.kt
│   │   └── ComicCard.kt
│   ├── reader/
│   │   ├── ReaderScreen.kt
│   │   └── ReaderViewModel.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Shape.kt
│   │   └── Type.kt
│   ├── main/
│   │   └── MainScreen.kt
│   └── di/
│       └── UiModule.kt
├── model/
│   └── ComicModel.kt
├── parser/
│   └── ComicParser.kt
└── image/
    └── ImageLoader.kt
```

## Integration Points

### Data Domain Integration
- **Repository Pattern**: Domain models map to data entities via repository implementations
- **Use Case Layer**: Business logic encapsulated in use cases with single responsibility
- **Flow-based Communication**: Reactive data flow from database to UI

### Domain Presentation Integration
- **ViewModel Dependencies**: ViewModels receive use cases via Hilt injection
- **State Management**: UI state managed through StateFlow and MutableStateFlow
- **Navigation Events**: Navigation handled through NavController in ViewModels

### UI System Integration
- **Compose Navigation**: Screen transitions managed by Jetpack Navigation
- **Theme System**: Consistent theming across all screens
- **Permission Handling**: File access permissions integrated with file picker

### File System Integration
- **Comic Parsing**: Existing ComicParser integrated with database storage
- **Image Loading**: Coil image loading integrated with reader UI
- **File Management**: FileProvider for secure file access

This specification provides a complete blueprint for implementing Phase 1 of the Easy Comic Android app with Clean Architecture, dependency injection, proper database handling, and file permission management.