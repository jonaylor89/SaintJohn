# Database Schema Documentation

## Overview
Saint John uses Room (SQLite) for local data persistence. Current version: 
**4**

## Schema Diagram

```
┌─────────────────────────┐
│   app_preferences       │
├─────────────────────────┤
│ packageName (PK)        │
│ category                │
│ isHidden                │
│ isLocked                │
│ forceColor              │
│ isPinned                │
└─────────────────────────┘

┌─────────────────────────┐
│   notes                 │
├─────────────────────────┤
│ id (PK, auto)           │
│ content                 │
│ createdAt               │
│ updatedAt               │
└─────────────────────────┘

┌─────────────────────────┐
│   weather               │
├─────────────────────────┤
│ location (PK)           │
│ temp                    │
│ feelsLike               │
│ condition               │
│ description             │
│ tempMin                 │
│ tempMax                 │
│ humidity                │
│ pressure                │
│ iconCode                │
│ timestamp               │
└─────────────────────────┘

┌─────────────────────────┐
│   conversations         │
├─────────────────────────┤
│ id (PK, auto)           │
│ title                   │
│ provider                │
│ createdAt               │
│ updatedAt               │
└─────────────────────────┘
         │
         │ 1:N
         ↓
┌─────────────────────────┐
│   messages              │
├─────────────────────────┤
│ id (PK, auto)           │
│ conversationId (FK)     │
│ content                 │
│ role                    │
│ timestamp               │
│ isError                 │
└─────────────────────────┘
```

## Table Definitions

### app_preferences
Stores per-app customization settings.

| Column       | Type    | Constraints | Description                       |
|--------------|---------|-------------|-----------------------------------|
| packageName  | TEXT    | PRIMARY KEY | Android package name              |
| category     | TEXT    | NOT NULL    | App category (enum)               |
| isHidden     | INTEGER | NOT NULL    | 0/1 boolean, hide from drawer     |
| isLocked     | INTEGER | NOT NULL    | 0/1 boolean, require auth to open |
| forceColor   | INTEGER | NOT NULL    | 0/1 boolean, show in color        |
| isPinned     | INTEGER | NOT NULL    | 0/1 boolean, show in pinned bar   |

**Indices**: None (packageName is unique identifier)

### notes
Quick notes feature.

| Column    | Type    | Constraints | Description                      |
|-----------|---------|-------------|----------------------------------|
| id        | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique note ID |
| content   | TEXT    | NOT NULL    | Note text                        |
| createdAt | INTEGER | NOT NULL    | Unix timestamp (milliseconds)    |
| updatedAt | INTEGER | NOT NULL    | Unix timestamp (milliseconds)    |

**Indices**: None currently
**Recommended Index**: CREATE INDEX idx_notes_updatedAt ON notes(updatedAt DESC)

### weather
Cached weather data (1 hour TTL).

| Column      | Type    | Constraints | Description                    |
|-------------|---------|-------------|--------------------------------|
| location    | TEXT    | PRIMARY KEY | City name                      |
| temp        | INTEGER | NOT NULL    | Temperature (Celsius)          |
| feelsLike   | INTEGER | NOT NULL    | Feels like temperature         |
| condition   | TEXT    | NOT NULL    | Main condition (Rain, Clear)   |
| description | TEXT    | NOT NULL    | Detailed description           |
| tempMin     | INTEGER | NOT NULL    | Daily low temperature          |
| tempMax     | INTEGER | NOT NULL    | Daily high temperature         |
| humidity    | INTEGER | NOT NULL    | Humidity percentage            |
| pressure    | INTEGER | NOT NULL    | Atmospheric pressure (hPa)     |
| iconCode    | TEXT    | NOT NULL    | OpenWeatherMap icon code       |
| timestamp   | INTEGER | NOT NULL    | Unix timestamp (milliseconds)  |

**Cleanup**: Old entries (>1 hour) are automatically deleted on refresh

### conversations
LLM chat conversations.

| Column    | Type    | Constraints | Description                           |
|-----------|---------|-------------|---------------------------------------|
| id        | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique conversation ID |
| title     | TEXT    | NOT NULL    | Conversation title                    |
| provider  | TEXT    | NOT NULL    | LLM provider (OPENAI/ANTHROPIC/GOOGLE)|
| createdAt | INTEGER | NOT NULL    | Unix timestamp (milliseconds)         |
| updatedAt | INTEGER | NOT NULL    | Unix timestamp (milliseconds)         |

**Indices**: None currently
**Recommended Index**: CREATE INDEX idx_conversations_updatedAt ON conversations(updatedAt DESC)

### messages
Chat messages within conversations.

| Column         | Type    | Constraints | Description                          |
|----------------|---------|-------------|--------------------------------------|
| id             | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique message ID |
| conversationId | INTEGER | NOT NULL    | Foreign key to conversations.id      |
| content        | TEXT    | NOT NULL    | Message text                         |
| role           | TEXT    | NOT NULL    | USER/ASSISTANT/SYSTEM                |
| timestamp      | INTEGER | NOT NULL    | Unix timestamp (milliseconds)        |
| isError        | INTEGER | NOT NULL    | 0/1 boolean, API error occurred      |

**Indices**: None currently
**Recommended Indices**:
- CREATE INDEX idx_messages_conversationId ON messages(conversationId, timestamp ASC)
- CREATE INDEX idx_messages_timestamp ON messages(timestamp DESC)

**Foreign Key**: conversationId → conversations.id (no CASCADE defined)

## Migration History

### Version 1 → 2
**Added**: `isPinned` column to app_preferences

```sql
ALTER TABLE app_preferences ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0
```

**Use Case**: Pinned apps bar feature

### Version 2 → 3
**Added**: `weather` table

```sql
CREATE TABLE IF NOT EXISTS weather (
    location TEXT PRIMARY KEY NOT NULL,
    temp INTEGER NOT NULL,
    feelsLike INTEGER NOT NULL,
    condition TEXT NOT NULL,
    description TEXT NOT NULL,
    tempMin INTEGER NOT NULL,
    tempMax INTEGER NOT NULL,
    humidity INTEGER NOT NULL,
    pressure INTEGER NOT NULL,
    iconCode TEXT NOT NULL,
    timestamp INTEGER NOT NULL
)
```

**Use Case**: Weather widget with caching

### Version 3 → 4
**Added**: `conversations` and `messages` tables

```sql
CREATE TABLE IF NOT EXISTS conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    provider TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
)

CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    conversationId INTEGER NOT NULL,
    content TEXT NOT NULL,
    role TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    isError INTEGER NOT NULL DEFAULT 0
)
```

**Use Case**: LLM chat interface

## DAO Patterns

### Query with Flow (Reactive)
```kotlin
@Query("SELECT * FROM notes ORDER BY updatedAt DESC")
fun getAllNotes(): Flow<List<NoteEntity>>
```

### Insert/Update (Suspend)
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertNote(note: NoteEntity): Long

@Update
suspend fun updateNote(note: NoteEntity)
```

### Delete Operations
```kotlin
@Delete
suspend fun deleteNote(note: NoteEntity)

@Query("DELETE FROM notes WHERE id = :id")
suspend fun deleteNoteById(id: Long)
```

### Filtering
```kotlin
@Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
fun getMessagesByConversation(conversationId: Long): Flow<List<MessageEntity>>
```

## Data Mapping

### Entity → Domain Model
```kotlin
private fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

### Domain Model → Entity
```kotlin
private fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

## Best Practices

### Timestamps
- Always use `System.currentTimeMillis()` for consistency
- Store as INTEGER (Long in Kotlin)
- Consider timezone implications for display

### Boolean Values
- Store as INTEGER (0 = false, 1 = true)
- Room doesn't support boolean types directly

### Enums
- Store as TEXT (enum.name)
- Validate on read with try-catch:
```kotlin
role = try {
    MessageRole.valueOf(entity.role)
} catch (e: Exception) {
    MessageRole.USER  // fallback
}
```

### Foreign Keys
- Not enforced at database level currently
- Consider adding CASCADE for deletions:
```kotlin
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
```

### Indices
- Add for columns used in WHERE clauses
- Add for ORDER BY columns
- Composite index for (conversationId, timestamp)

## Backup Strategy

### Export Database
```bash
adb exec-out run-as com.jonaylor.saintjohn cat /data/data/com.jonaylor.saintjohn/databases/launcher_database > backup.db
```

### Import Database
```bash
adb push backup.db /sdcard/
adb shell run-as com.jonaylor.saintjohn cp /sdcard/backup.db /data/data/com.jonaylor.saintjohn/databases/launcher_database
```

### Clear Database (Testing)
```bash
adb shell pm clear com.jonaylor.saintjohn
```

## Performance Tips

1. **Use Transactions** for bulk operations
2. **Avoid N+1 queries** - join tables when possible
3. **Limit result sets** - paginate large queries
4. **Cache expensive queries** in ViewModel
5. **Use Flow** for automatic UI updates
6. **Index wisely** - improves reads, slows writes

## Future Schema Changes

### Planned
- Add foreign key constraints
- Add indices for performance
- Add full-text search for notes
- Add conversation folders/tags
- Add message reactions/favorites

### Considerations
- Encryption for sensitive data
- Sync mechanism for multi-device
- Archive old conversations
- Export/import functionality
