# Saint John Architecture Documentation

## Overview
Saint John follows Clean Architecture principles with clear separation 
between UI, business logic, and data layers.

## Layer Structure

### Presentation Layer (`presentation/`)
**Responsibility**: UI and user interaction

**Components**:
- **Screens**: Top-level composables for each page
  - `drawer/DrawerScreen.kt` - App launcher
  - `root/RootScreen.kt` - Widget cards
  - `home/HomeScreen.kt` - Chat interface
- **ViewModels**: State management and business logic coordination
  - Annotated with `@HiltViewModel`
  - Inject repositories via constructor
  - Expose `StateFlow<UiState>` for reactive UI
- **Components**: Reusable UI components
  - `root/components/WeatherCard.kt`
  - `root/components/CalendarCard.kt`
  - `root/components/NotesCard.kt`

**Pattern**:
```
Screen → ViewModel → Repository → Data Source
  ↓         ↓            ↓
 UI State  Flow    Domain Models
```

### Domain Layer (`domain/`)
**Responsibility**: Business logic and data contracts

**Components**:
- **Models**: Pure Kotlin data classes
  - `AppInfo`, `Note`, `WeatherData`, `CalendarEvent`
  - `Message`, `LLMProvider` (chat)
- **Repository Interfaces**: Data access contracts
  - No implementation details
  - Return domain models
  - Use Kotlin Flow for reactive streams
- **UseCases**: Complex business operations
  - `GetAppUsageStatsUseCase`
  - `GetLocationUseCase`
  - `AppCategorizationUseCase`

### Data Layer (`data/`)
**Responsibility**: Data management and external communication

**Structure**:
```
data/
├── local/           # Local data sources
│   ├── dao/         # Room DAOs
│   ├── entity/      # Room entities
│   ├── LauncherDatabase.kt
│   └── PreferencesManager.kt
├── remote/          # Network data sources
│   ├── dto/         # API response models
│   └── WeatherApi.kt
└── repository/      # Repository implementations
    └── *RepositoryImpl.kt
```

**Repository Implementation Pattern**:
- Implements domain repository interface
- Coordinates between local and remote sources
- Maps between entities/DTOs and domain models
- Handles caching strategies

## Dependency Injection (Hilt)

### Module Structure

**DatabaseModule** (`di/DatabaseModule.kt`):
- Provides Room database instance
- Provides all DAOs
- Manages database migrations

**NetworkModule** (`di/NetworkModule.kt`):
- Provides Retrofit instance
- Provides OkHttpClient with logging
- Provides API interfaces

**RepositoryModule** (`di/RepositoryModule.kt`):
- Binds repository interfaces to implementations
- All repositories are Singletons

**UseCaseModule** (`di/UseCaseModule.kt`):
- Provides UseCase instances
- Typically stateless, can be Singleton

### Injection Pattern
```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository,
    private val useCase: FeatureUseCase
) : ViewModel()

@Singleton
class FeatureRepositoryImpl @Inject constructor(
    private val dao: FeatureDao,
    private val api: FeatureApi
) : FeatureRepository
```

## Data Flow

### Reactive Pattern (Preferred)
```
[Room DB] → Flow<Entity> → Repository maps to Flow<DomainModel>
    ↓
ViewModel collects and transforms to Flow<UiState>
    ↓
Screen collects as State and renders UI
```

**Example**:
```kotlin
// Repository
override fun getData(): Flow<List<DomainModel>> {
    return dao.getAll().map { entities ->
        entities.map { it.toDomainModel() }
    }
}

// ViewModel
val uiState: StateFlow<UiState> = repository.getData()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

// UI
val uiState by viewModel.uiState.collectAsState()
```

### Action Pattern
```
User Action → ViewModel function → Repository suspend function → Database/API
    ↓
Reactive flow automatically updates UI
```

## Database Architecture

### Room Database (Version 4)

**Entities**:
- `AppPreferenceEntity` - Per-app settings
- `NoteEntity` - Quick notes
- `WeatherEntity` - Cached weather
- `ConversationEntity` - Chat conversations
- `MessageEntity` - Chat messages

**DAOs**:
- Provide Flow for reactive queries
- Use suspend functions for write operations
- Queries are type-safe

**Migration Strategy**:
- Create `MIGRATION_X_Y` for each version bump
- SQL schema changes in migration
- Add to database builder
- `fallbackToDestructiveMigration()` for development

### PreferencesManager (DataStore)

**Pattern**:
```kotlin
// Keys
companion object {
    val KEY = stringPreferencesKey("key_name")
}

// Read
val value: Flow<String> = context.dataStore.data.map {
    preferences -> preferences[KEY] ?: "default"
}

// Write
suspend fun setValue(value: String) {
    context.dataStore.edit { preferences ->
        preferences[KEY] = value
    }
}
```

**Stored Data**:
- UI preferences (theme, monochrome)
- API keys (encrypted in production)
- User settings

## UI Architecture

### Compose Patterns

**Screen Structure**:
```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Layout
    Column {
        // UI components that read from uiState
        // Pass viewModel functions as callbacks
    }
}
```

**Card Component Pattern**:
```kotlin
@Composable
fun FeatureCard(
    data: DataType,
    isLoading: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        when {
            isLoading -> LoadingIndicator()
            data == null -> EmptyState()
            else -> ContentView(data, onAction)
        }
    }
}
```

### Navigation

**HorizontalPager** (3 pages):
```kotlin
val pagerState = rememberPagerState(
    initialPage = 1,  // Start at center (Drawer)
    pageCount = { 3 }
)

HorizontalPager(state = pagerState) { page ->
    when (page) {
        0 -> RootScreen()      // Cards
        1 -> DrawerScreen()    // App Drawer
        2 -> HomeScreen()      // Chat
    }
}
```

**Back Button Handling**:
- Always returns to center page (Drawer)
- Doesn't close the launcher

## Feature Modules

### Weather Widget
**Flow**: Location → Coordinates → OpenWeatherMap API → Room Cache → UI

**Components**:
- `GetLocationUseCase` - FusedLocationProvider
- `WeatherApi` - Retrofit interface
- `WeatherRepositoryImpl` - Offline-first caching
- `WeatherViewModel` - Reactive state
- `WeatherCard` - Compose UI

### Calendar Widget
**Flow**: Android CalendarContract → Room (no cache) → UI

**Components**:
- `CalendarRepositoryImpl` - ContentProvider queries
- `CalendarViewModel` - State management
- `CalendarCard` - Event list UI

### Notes Widget
**Flow**: UI Dialog → Room DB → Reactive UI update

**Components**:
- `NoteRepositoryImpl` - CRUD operations
- `NotesViewModel` - Add/Edit/Delete actions
- `NotesCard` - List with dialogs

### App Drawer
**Flow**: PackageManager → Room Preferences → Categorization → UI

**Components**:
- `AppRepositoryImpl` - System app integration
- `AppCategorizationUseCase` - ML-based categories
- `GetAppUsageStatsUseCase` - Usage tracking
- `DrawerViewModel` - Search and filter
- `DrawerScreen` - Grid with categories

### Chat Interface (In Progress)
**Flow**: User Message → ChatRepository → LLM API → Response → Room → UI

**Components**:
- `ChatRepositoryImpl` - Message persistence
- `ChatViewModel` - Conversation management
- Provider interfaces for OpenAI/Anthropic/Google APIs

## Error Handling

### Repository Level
```kotlin
override suspend fun fetchData(): Result<Data> {
    return try {
        val response = api.fetch()
        dao.insert(response.toEntity())
        Result.success(response.toDomainModel())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### ViewModel Level
```kotlin
fun performAction() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        repository.fetchData()
            .onSuccess { data ->
                _uiState.value = _uiState.value.copy(data = data, error = null)
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
```

### UI Level
```kotlin
when {
    uiState.isLoading -> LoadingIndicator()
    uiState.error != null -> ErrorMessage(uiState.error)
    else -> Content(uiState.data)
}
```

## Testing Strategy

### Unit Tests
- Domain models (data classes)
- UseCases (business logic)
- Repository implementations (mock dependencies)

### Integration Tests
- Database DAOs with Room in-memory DB
- Repository + DAO integration

### UI Tests
- Compose UI testing
- Navigation flow
- User interactions

## Performance Considerations

### Database
- Use Flow for reactive queries (automatic updates)
- Index frequently queried columns
- Batch insert operations

### UI
- Use `remember` for expensive calculations
- `LazyColumn`/`LazyVerticalGrid` for lists
- Avoid recomposition with `derivedStateOf`

### Network
- Cache API responses in Room
- Offline-first for better UX
- Background refresh with WorkManager (if needed)

## Security Considerations

### API Keys
- Currently stored in DataStore (plaintext)
- **Production**: Use EncryptedSharedPreferences or Android Keystore
- Never commit keys to version control

### Permissions
- Request at runtime with Accompanist Permissions
- Explain permission purpose to user
- Graceful degradation if denied

### Data Privacy
- Calendar and contacts data stays on device
- No analytics or tracking
- User conversations stored locally only

## Future Architectural Improvements

1. **Modularization**: Split into feature modules
2. **Offline Sync**: WorkManager for background tasks
3. **Testing**: Increase test coverage
4. **Security**: Encrypt sensitive data
5. **Performance**: Add performance monitoring
6. **Analytics**: Optional privacy-respecting analytics
7. **Multi-language**: Localization support
8. **Accessibility**: Screen reader optimization
9. **Widgets**: Home screen widgets
10. **Backup**: Export/import user data
