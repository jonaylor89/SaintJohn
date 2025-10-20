package com.jonaylor.saintjohn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jonaylor.saintjohn.data.local.dao.AppPreferenceDao
import com.jonaylor.saintjohn.data.local.dao.NoteDao
import com.jonaylor.saintjohn.data.local.entity.AppPreferenceEntity
import com.jonaylor.saintjohn.data.local.entity.NoteEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE app_preferences ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [AppPreferenceEntity::class, NoteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun noteDao(): NoteDao
}
