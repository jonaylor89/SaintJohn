# Claude Code Development Guide for Saint John

## Project Overview
Saint John is a minimalist Android launcher built with Kotlin and Jetpack Compose. It features a three-page horizontal pager interface with app drawer, widget cards, and an LLM chat interface.

## Quick Start Commands

### Building and Running
```bash
# Set Java environment (required)
export JAVA_HOME="/Users/johannes/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build debug APK
./gradlew assembleDebug

# Install on device (Pixel 10 connected via WiFi ADB)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.jonaylor.saintjohn/.MainActivity

# Combined build, install, and launch
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.jonaylor.saintjohn/.MainActivity
```

### Checking Device Connection
```bash
adb devices
```

## Architecture

### Clean Architecture Layers
```
presentation/     → Compose UI, ViewModels (Hilt)
domain/          → Models, Repository interfaces, UseCases
data/            → Repository implementations, Room DB, API services
  ├─ local/      → Room database, DAOs, Entities, PreferencesManager
  ├─ remote/     → API services (Weather, LLM providers)
  └─ repository/ → Repository implementations
di/              → Hilt dependency injection modules
util/            → Utility classes and theme
```

### Key Technologies
- **UI**: Jetpack Compose, Material3
- **DI**: Hilt/Dagger
- **Database**: Room (version 4)
- **Async**: Kotlin Coroutines, Flow
- **Networking**: Retrofit, OkHttp
- **Navigation**: HorizontalPager (3 pages)

## Three-Page Structure

1. **Page 0 (Left) - Root/Cards**: Weather, Calendar, Quick Notes widgets
2. **Page 1 (Center) - Drawer**: App launcher with categories
3. **Page 2 (Right) - Home**: LLM chat interface

## Database Schema (Version 4)

### Tables
- `app_preferences` - App customization settings
- `notes` - Quick notes
- `weather` - Cached weather data
- `conversations` - LLM chat conversations
- `messages` - Chat messages with role (USER/ASSISTANT/SYSTEM)

### Migrations
- v1→v2: Added `isPinned` to app_preferences
- v2→v3: Added weather table
- v3→v4: Added conversations and messages tables

## Code Style Guidelines

### Compose UI Patterns
```kotlin
@Composable
fun FeatureCard(
    data: DataType,
    isLoading: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Content
    }
}
```

### ViewModel Pattern
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {

    val uiState: StateFlow<FeatureUiState> = repository.getData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeatureUiState()
        )

    fun performAction() {
        viewModelScope.launch {
            repository.doSomething()
        }
    }
}
```

### Repository Pattern
```kotlin
@Singleton
class FeatureRepositoryImpl @Inject constructor(
    private val dao: FeatureDao,
    private val api: FeatureApi
) : FeatureRepository {

    override fun getData(): Flow<List<DomainModel>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun refresh() {
        val response = api.fetch()
        dao.insert(response.toEntity())
    }
}
```

## Important Conventions

### Permissions
All permission requests are handled in `MainActivity.kt` using Accompanist Permissions library:
```kotlin
val permissionState = rememberMultiplePermissionsState(
    listOf(Manifest.permission.PERMISSION_NAME)
)

LaunchedEffect(Unit) {
    if (!permissionState.allPermissionsGranted) {
        permissionState.launchMultiplePermissionRequest()
    }
}
```

### System Bars
- Use `.statusBarsPadding()` and `.navigationBarsPadding()` in root composables
- Status bar appearance is configured in MainActivity SideEffect

### Color Scheme
- Greyscale app icons using `ColorMatrix().setToSaturation(0f)`
- Primary colors for accents
- Surface/background for cards

### File References
When referencing code locations, use format: `filename.kt:line_number`

## Current Features

### Root Page (Page 0)
- **Weather Widget**: OpenWeatherMap API, location-based, tap to refresh
- **Calendar Widget**: Reads device calendars, shows 5 upcoming events
- **Notes Widget**: CRUD operations, dialogs for add/edit, long-press to delete

### Drawer (Page 1)
- Categorized apps (automatically categorized)
- Collapsible categories (tap header to expand)
- Search bar at bottom
- Pinned apps bar (up to 5 apps)
- 3-column grid with greyscale icons
- Usage stats integration

### Home/Chat (Page 2)
- **In Progress**: LLM chat interface
- Database ready for conversations and messages
- Provider selection (OpenAI, Anthropic, Google)
- API key storage in DataStore

## Dependency Injection

### Adding a New Repository
1. Create interface in `domain/repository/`
2. Create implementation in `data/repository/`
3. Add binding in `di/RepositoryModule.kt`:
```kotlin
@Binds
@Singleton
abstract fun bindYourRepository(impl: YourRepositoryImpl): YourRepository
```

### Adding a New DAO
1. Create DAO interface in `data/local/dao/`
2. Add provider in `di/DatabaseModule.kt`:
```kotlin
@Provides
fun provideYourDao(database: LauncherDatabase): YourDao {
    return database.yourDao()
}
```

## Common Issues

### Java Version
- **Must use Java 21** from Android Studio bundle
- Error "Gradle doesn't support Java 25" → Set JAVA_HOME correctly

### Lint Errors
- ChromeOS telephony permission requires `<uses-feature>` with `required="false"`
- Deprecated APIs: Use `replaceFirstChar` instead of `capitalize`

### Database Migrations
- Always increment version number
- Create new `MIGRATION_X_Y` object
- Add to `.addMigrations()` in DatabaseModule
- Test migration with fresh install

## Testing Workflow

1. Make changes
2. Build: `./gradlew assembleDebug`
3. Check for errors (lint runs automatically)
4. Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. Launch: `adb shell am start -n com.jonaylor.saintjohn/.MainActivity`
6. Test feature on device

## PreferencesManager (DataStore)

Stores:
- Theme mode (BLLOC/SUN/COLOR)
- Monochrome settings
- LLM provider selection
- API keys (OpenAI, Anthropic, Google)

Access pattern:
```kotlin
// Read
val value: Flow<String> = preferencesManager.someValue

// Write
suspend fun setValue(value: String) {
    preferencesManager.setSomeValue(value)
}
```

## Next Steps for Chat Interface

1. Create chat UI with message bubbles (`presentation/home/components/ChatBubble.kt`)
2. Create settings dialog for API keys
3. Create model selector dropdown
4. Update HomeScreen to use ChatViewModel
5. Implement actual API integrations (OpenAI, Anthropic, Google)

## Useful File Locations

- Main Activity: `app/src/main/java/com/jonaylor/saintjohn/MainActivity.kt`
- Theme: `app/src/main/java/com/jonaylor/saintjohn/util/theme/`
- Database: `app/src/main/java/com/jonaylor/saintjohn/data/local/LauncherDatabase.kt`
- Dependencies: `gradle/libs.versions.toml`
- Build config: `app/build.gradle.kts`
- Manifest: `app/src/main/AndroidManifest.xml`
