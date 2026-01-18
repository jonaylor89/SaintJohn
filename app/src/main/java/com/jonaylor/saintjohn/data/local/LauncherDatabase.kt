package com.jonaylor.saintjohn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jonaylor.saintjohn.data.local.dao.AppPreferenceDao
import com.jonaylor.saintjohn.data.local.dao.ConversationDao
import com.jonaylor.saintjohn.data.local.dao.MessageDao
import com.jonaylor.saintjohn.data.local.dao.NoteDao
import com.jonaylor.saintjohn.data.local.dao.WeatherDao
import com.jonaylor.saintjohn.data.local.entity.AppPreferenceEntity
import com.jonaylor.saintjohn.data.local.entity.ConversationEntity
import com.jonaylor.saintjohn.data.local.entity.MessageEntity
import com.jonaylor.saintjohn.data.local.entity.NoteEntity
import com.jonaylor.saintjohn.data.local.entity.WeatherEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE app_preferences ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
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
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                provider TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """.trimIndent())

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                conversationId INTEGER NOT NULL,
                content TEXT NOT NULL,
                role TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isError INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add title column to notes table with default empty string
        database.execSQL("ALTER TABLE notes ADD COLUMN title TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add model column to conversations table with default empty string
        database.execSQL("ALTER TABLE conversations ADD COLUMN model TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop old weather table and create new one with id as primary key
        database.execSQL("DROP TABLE IF EXISTS weather")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS weather (
                id INTEGER PRIMARY KEY NOT NULL,
                location TEXT NOT NULL,
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
        """.trimIndent())
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add thinking columns to messages table for reasoning model support
        database.execSQL("ALTER TABLE messages ADD COLUMN thinking TEXT")
        database.execSQL("ALTER TABLE messages ADD COLUMN thinkingSummary TEXT")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add imagesJson column to messages table for image generation support
        database.execSQL("ALTER TABLE messages ADD COLUMN imagesJson TEXT")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE messages ADD COLUMN toolCallsJson TEXT")
        database.execSQL("ALTER TABLE messages ADD COLUMN toolResultJson TEXT")
    }
}

@Database(
    entities = [
        AppPreferenceEntity::class,
        NoteEntity::class,
        WeatherEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun noteDao(): NoteDao
    abstract fun weatherDao(): WeatherDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
