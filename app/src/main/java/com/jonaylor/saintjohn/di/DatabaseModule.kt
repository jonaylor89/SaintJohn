package com.jonaylor.saintjohn.di

import android.content.Context
import androidx.room.Room
import com.jonaylor.saintjohn.data.local.LauncherDatabase
import com.jonaylor.saintjohn.data.local.MIGRATION_1_2
import com.jonaylor.saintjohn.data.local.MIGRATION_2_3
import com.jonaylor.saintjohn.data.local.MIGRATION_3_4
import com.jonaylor.saintjohn.data.local.MIGRATION_4_5
import com.jonaylor.saintjohn.data.local.MIGRATION_5_6
import com.jonaylor.saintjohn.data.local.MIGRATION_6_7
import com.jonaylor.saintjohn.data.local.MIGRATION_7_8
import com.jonaylor.saintjohn.data.local.MIGRATION_8_9
import com.jonaylor.saintjohn.data.local.MIGRATION_9_10
import com.jonaylor.saintjohn.data.local.MIGRATION_10_11
import com.jonaylor.saintjohn.data.local.PreferencesManager
import com.jonaylor.saintjohn.data.local.dao.AppPreferenceDao
import com.jonaylor.saintjohn.data.local.dao.ConversationDao
import com.jonaylor.saintjohn.data.local.dao.MessageDao
import com.jonaylor.saintjohn.data.local.dao.NoteDao
import com.jonaylor.saintjohn.data.local.dao.WeatherDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
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

    @Provides
    fun provideWeatherDao(database: LauncherDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    fun provideConversationDao(database: LauncherDatabase): ConversationDao {
        return database.conversationDao()
    }

    @Provides
    fun provideMessageDao(database: LauncherDatabase): MessageDao {
        return database.messageDao()
    }
}
