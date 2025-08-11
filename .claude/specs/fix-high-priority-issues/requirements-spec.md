## Problem Statement
- **Business Issue**: KAPT compilation errors prevent successful APK generation due to Hilt dependency injection configuration conflicts with Room KSP
- **Current State**: Project uses Hilt with KAPT for DI while Room uses KSP, causing compilation failures and preventing app deployment
- **Expected Outcome**: Clean compilation without Hilt KAPT errors, Room database generation working correctly, app builds and runs successfully with manual dependency injection

## Solution Overview
- **Approach**: Remove all Hilt annotations and dependencies, implement manual dependency injection using a simple singleton DI container, preserve Room KSP for database code generation
- **Core Changes**: Convert Hilt-based DI to manual DI, remove KAPT dependencies, create dependency container, refactor ViewModels to use manual injection
- **Success Criteria**: Project compiles without errors, Room database generates correctly, ViewModels work with manual DI, app launches and functions normally

## Technical Implementation

### Database Changes
- **Tables to Modify**: No changes needed - Room entities and DAOs remain unchanged
- **New Tables**: None required
- **Migration Scripts**: None required - Room schema stays intact

### Code Changes

#### Files to Modify:

1. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\EasyComicApplication.kt**
   - Remove @HiltAndroidApp annotation
   - Add dependency container initialization
   - Initialize Timber logging

2. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\MainActivity.kt**
   - Remove @AndroidEntryPoint annotation
   - Remove Hilt imports
   - Keep existing functionality as-is

3. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\ui\bookshelf\BookshelfViewModel.kt**
   - Remove @HiltViewModel and @Inject annotations
   - Convert to manual constructor injection
   - Add ViewModelFactory support

4. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\ui\reader\ReaderViewModel.kt**
   - Remove @HiltViewModel and @Inject annotations
   - Convert to manual constructor injection
   - Add ViewModelFactory support

5. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\data\di\DataModule.kt**
   - Remove Hilt annotations (@Module, @InstallIn, @Provides, @Singleton)
   - Convert to manual dependency container class
   - Implement lazy initialization

6. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\domain\di\DomainModule.kt**
   - Remove Hilt annotations (@Module, @InstallIn, @Provides, @JavaxSingleton)
   - Convert to manual dependency container class
   - Implement lazy initialization

#### New Files:

1. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\DependencyContainer.kt**
   - Create singleton DI container
   - Initialize all dependencies
   - Provide access to all repositories and use cases

2. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\ViewModelFactory.kt**
   - Create ViewModelFactory for manual DI
   - Support ViewModel creation with dependencies

3. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\AppModule.kt**
   - Convert DataModule to plain dependency provider
   - Remove all Hilt annotations
   - Implement lazy initialization

4. **C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\DomainModule.kt**
   - Convert DomainModule to plain dependency provider
   - Remove all Hilt annotations
   - Implement lazy initialization

### API Changes
- **Endpoints**: No API endpoints involved
- **Request/Response**: No changes needed
- **Validation Rules**: No validation changes needed

### Configuration Changes

#### Build Configuration (C:\001\Comic\Easy-Comic\app\build.gradle.kts):
- **Remove Hilt plugins**: Remove `alias(libs.plugins.hilt.android)` and `alias(libs.plugins.kotlin.kapt)`
- **Remove Hilt dependencies**: 
  - Remove `implementation(libs.hilt.android)`
  - Remove `kapt(libs.hilt.android.compiler)`
  - Remove `implementation(libs.androidx.hilt.navigation.compose)`
  - Remove `androidTestImplementation("androidx.hilt:hilt-testing:1.2.0")`
  - Remove `kaptAndroidTest("androidx.hilt:hilt-compiler:1.2.0")`
- **Keep Room KSP**: Preserve Room KSP configuration for database generation
- **Add Koin**: Add simple DI library for manual injection support
  - `implementation("io.insert-koin:koin-android:3.5.0")`
  - `implementation("io.insert-koin:koin-androidx-compose:3.5.0")`

#### AndroidManifest.xml:
- **No changes needed**: Application class reference remains the same

## Implementation Sequence

### Phase 1: Remove Hilt Dependencies and Build Configuration
1. Update app/build.gradle.kts to remove Hilt dependencies and KAPT
2. Add Koin dependencies for manual DI support
3. Build project to verify Hilt removal doesn't break existing code

### Phase 2: Create Manual Dependency Injection Infrastructure
1. Create C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\DependencyContainer.kt
2. Create C:\001\Comic\Easy-Comic\app\src\main\java\com\easycomic\di\ViewModelFactory.kt
3. Convert DataModule to manual dependency provider
4. Convert DomainModule to manual dependency provider

### Phase 3: Update Application and Activities
1. Update EasyComicApplication.kt to remove @HiltAndroidApp and initialize DI container
2. Update MainActivity.kt to remove @AndroidEntryPoint
3. Update ViewModel initialization in UI components

### Phase 4: Update ViewModels
1. Update BookshelfViewModel.kt to remove Hilt annotations and use manual injection
2. Update ReaderViewModel.kt to remove Hilt annotations and use manual injection
3. Update ViewModel creation in UI screens

### Phase 5: Testing and Validation
1. Build project to ensure no compilation errors
2. Test Room database generation
3. Verify ViewModels work correctly
4. Test app launch and basic functionality

## Validation Plan

### Unit Tests
- **DependencyContainerTest**: Verify all dependencies initialize correctly
- **ViewModelFactoryTest**: Test ViewModel creation with dependencies
- **RoomDatabaseTest**: Verify Room database generation works without Hilt

### Integration Tests
- **BookshelfViewModelTest**: Test ViewModel with manual DI
- **ReaderViewModelTest**: Test ViewModel with manual DI
- **ApplicationStartupTest**: Verify app launches without Hilt errors

### Business Logic Verification
- **Comic Import Test**: Verify comic import functionality works
- **Database Operations Test**: Verify all CRUD operations work
- **Navigation Test**: Verify app navigation flows correctly
- **Performance Test**: Verify app performance with manual DI

### Build Verification
- **Clean Build**: `./gradlew clean build` should succeed without errors
- **APK Generation**: `./gradlew assembleDebug` should produce working APK
- **Room Schema Generation**: Verify schema files are generated in schemas directory
- **KSP Processing**: Verify KSP generates Room database code correctly

## Risk Mitigation

### Known Issues
- **Room KSP Compatibility**: Room KSP should work independently of Hilt
- **ViewModel Initialization**: Manual DI requires proper ViewModelFactory setup
- **Dependency Lifecycle**: Singletons must be properly managed in manual DI

### Rollback Strategy
- Keep git commits small and reversible
- Maintain backup of Hilt configuration
- Test each phase thoroughly before proceeding

### Success Metrics
- Project builds without errors
- Room database generates correctly
- ViewModels work with manual DI
- App launches and functions normally
- All existing features work as expected