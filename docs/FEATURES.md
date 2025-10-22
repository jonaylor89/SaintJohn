# Features Documentation

## App Structure

Saint John uses a 3-page horizontal swipe interface:

```
┌─────────────┬─────────────┬─────────────┐
│   Cards     │    Apps     │    Chat     │
│  (Page 0)   │  (Page 1)   │  (Page 2)   │
│             │             │             │
│  Weather    │  Search bar │  LLM Chat   │
│  Calendar   │  ─────────  │  Interface  │
│  Notes      │  Categories │  with API   │
│             │  App Grid   │  settings   │
│             │  Pinned bar │             │
└─────────────┴─────────────┴─────────────┘
   Swipe ←→      Default        Swipe ←→
```

## Page 0: Cards/Root

### Weather Widget
**Status**: ✅ Complete

**Features**:
- Current weather with location services
- Temperature, feels like, condition
- High/low, humidity, pressure
- Last updated timestamp
- Tap to refresh
- 1-hour cache

**Data Source**: OpenWeatherMap API
**Permissions**: ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION

**API Key**: Hardcoded demo key (free tier)

**Implementation**:
- `WeatherCard.kt` - UI component
- `WeatherViewModel.kt` - State management
- `WeatherRepositoryImpl.kt` - API + caching
- `GetLocationUseCase.kt` - GPS location

### Calendar Widget
**Status**: ✅ Complete

**Features**:
- Next 5 upcoming events
- Event details: time, date, location, calendar name
- Smart date formatting (Today, Tomorrow, day name, date)
- Color indicators from calendar
- All-day event support
- Tap to refresh

**Data Source**: Android CalendarContract
**Permissions**: READ_CALENDAR

**Implementation**:
- `CalendarCard.kt` - Event list UI
- `CalendarViewModel.kt` - State management
- `CalendarRepositoryImpl.kt` - ContentProvider queries

### Quick Notes Widget
**Status**: ✅ Complete

**Features**:
- Create notes with + button
- Edit notes by tapping
- Delete notes with long-press (confirmation)
- Shows up to 5 recent notes
- Relative timestamps (just now, Xm ago, Xh ago)
- Persistent storage
- Multi-line support

**Data Source**: Room database
**Permissions**: None

**Implementation**:
- `NotesCard.kt` - List + dialogs
- `NotesViewModel.kt` - CRUD operations
- `NoteRepositoryImpl.kt` - Database access

## Page 1: Apps/Drawer

### App Launcher
**Status**: ✅ Complete

**Features**:
- All installed apps
- Automatic categorization (12 categories)
- Collapsible categories (tap header to expand/collapse)
- 3-column grid layout
- 3:4 aspect ratio cards
- Greyscale app icons
- App labels (2 lines max)
- Usage time display per app
- Category usage time totals

**Categories**:
1. Social Media
2. Communication
3. Productivity
4. Entertainment
5. Shopping
6. Travel
7. Finance
8. Health & Fitness
9. News & Reading
10. Education
11. Utilities
12. Other

**Implementation**:
- `DrawerScreen.kt` - Main UI
- `DrawerViewModel.kt` - Search and filter
- `AppRepositoryImpl.kt` - Package manager integration
- `AppCategorizationUseCase.kt` - Category assignment

### Search
**Status**: ✅ Complete

**Features**:
- Pill-shaped search bar at bottom
- Real-time filtering
- Searches app name and package name
- Grid view for results
- Same card style as categories

**Implementation**: Reactive Flow with `combine()` operator

### Pinned Apps
**Status**: ✅ Complete

**Features**:
- Up to 5 pinned apps
- Semi-transparent bar above search
- Greyscale icons
- Quick access to favorites
- Long-press app to toggle pin (TODO: UI for this)

**Data Source**: Room database (isPinned flag)

### Usage Stats
**Status**: ✅ Complete

**Features**:
- Per-app usage time (last 24h)
- Category total usage time
- Formatted display (Xh Ym, Ym, <1m)
- Lock indicator for restricted apps

**Permissions**: PACKAGE_USAGE_STATS
**Implementation**: `GetAppUsageStatsUseCase.kt`

### App Settings (TODO)
**Features to Add**:
- Long-press → app dashboard
- Toggle: Hidden, Locked, Force Color, Pinned
- Uninstall shortcut
- App info shortcut

## Page 2: Home/Chat

### LLM Chat Interface
**Status**: 🚧 In Progress (70% complete)

**Completed**:
- Database schema (conversations, messages)
- Message persistence
- Provider selection (OpenAI, Anthropic, Google)
- API key storage (DataStore)
- ChatViewModel with state management
- Repository pattern ready for API calls

**TODO**:
- Chat UI with message bubbles
- Input field with send button
- Model selector dropdown
- Settings dialog for API keys
- Actual API integrations
- Streaming responses
- Code syntax highlighting
- Message copy/share

**Planned Features**:
- Multiple conversations
- Conversation history
- Export conversations
- System prompts
- Temperature/parameters settings
- Token usage tracking
- Cost estimation

**Providers**:
- **OpenAI**: GPT-4, GPT-3.5
- **Anthropic**: Claude 3 (Opus, Sonnet, Haiku)
- **Google**: Gemini Pro

## Cross-Page Features

### Theme System
**Status**: ✅ Complete

**Modes**:
- **BLLOC**: Dark theme (default)
- **SUN**: Light theme
- **COLOR**: System theme

**Features**:
- Greyscale app icons
- Monochrome mode toggle
- System-wide greyscale
- Smooth theme transitions

**Implementation**: `LauncherTheme.kt`, `PreferencesManager.kt`

### Permissions Management
**Status**: ✅ Complete

**Handled Permissions**:
- Location (weather)
- Calendar (events)
- Usage stats (app usage)
- Contacts (future)
- Phone (future)

**Pattern**: Accompanist Permissions in `MainActivity.kt`

### System UI
**Status**: ✅ Complete

**Features**:
- Edge-to-edge layout
- Status bar padding
- Navigation bar padding
- Keyboard (IME) padding
- Status bar icon color adapts to theme

### Back Button Behavior
**Status**: ✅ Complete

**Behavior**:
- Always returns to center page (Apps/Drawer)
- Never closes the launcher
- Maintains scroll position

## Settings (Future)

**Planned Features**:
- Theme selection
- Monochrome toggle
- API key management
- Default LLM provider
- Usage stats toggle
- Hidden apps management
- Locked apps (biometric)
- Backup/restore
- About/version info

## Performance Optimizations

### Implemented:
- LazyColumn/LazyVerticalGrid for lists
- Flow for reactive updates (no manual refresh)
- Image caching with Coil (TODO)
- Database indexing (partially)
- Offline-first architecture

### TODO:
- Pagination for large lists
- Virtual scrolling
- Image preloading
- Background sync with WorkManager
- Performance monitoring

## Accessibility (TODO)

**Planned**:
- Screen reader support
- Content descriptions
- Semantic roles
- Focus indicators
- Keyboard navigation
- Text scaling support
- High contrast mode

## Localization (TODO)

**Planned**:
- Multi-language support
- RTL layout support
- Date/time formatting per locale
- Number formatting per locale

## Known Limitations

1. **Weather**: Demo API key (rate limited)
2. **Calendar**: Read-only (no event creation)
3. **Notes**: No rich text formatting
4. **Chat**: API integration incomplete
5. **App Drawer**: No custom categories
6. **Search**: No fuzzy matching
7. **Backup**: Manual only
8. **Sync**: No multi-device support

## Roadmap

### Short Term
1. Complete chat UI
2. Implement LLM API calls
3. Add app dashboard (long-press)
4. Settings screen
5. Improve error handling

### Medium Term
1. Conversation management
2. Rich text in notes
3. Weather location picker
4. Calendar event creation
5. Image handling in chat

### Long Term
1. Home screen widgets
2. Gesture customization
3. Plugin system
4. Cloud sync
5. Desktop companion app
