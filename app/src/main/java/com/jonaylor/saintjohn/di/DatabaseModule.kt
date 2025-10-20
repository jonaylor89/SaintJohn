package com.jonaylor.saintjohn.di

import android.content.Context
import androidx.room.Room
import com.jonaylor.saintjohn.data.local.LauncherDatabase
import com.jonaylor.saintjohn.data.local.MIGRATION_1_2
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.local.dao.AppPreferenceDao
import com.jonaylor.saintjohn.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LauncherDatabase {
        return Room.databaseBuilder(
            context,
            LauncherDatabase::class.java,
            "launcher_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAppPreferenceDao(database: LauncherDatabase): AppPreferenceDao {
        return database.appPreferenceDao()
    }

    @Provides
    fun provideNoteDao(database: LauncherDatabase): NoteDao {
        return database.noteDao()
    }
}
