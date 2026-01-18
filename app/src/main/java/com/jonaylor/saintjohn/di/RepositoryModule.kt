package com.jonaylor.saintjohn.di

import com.jonaylor.saintjohn.data.repository.AppRepositoryImpl
import com.jonaylor.saintjohn.data.repository.CalendarRepositoryImpl
import com.jonaylor.saintjohn.data.repository.ChatRepositoryImpl
import com.jonaylor.saintjohn.data.repository.NoteRepositoryImpl
import com.jonaylor.saintjohn.data.repository.WeatherRepositoryImpl
import com.jonaylor.saintjohn.domain.repository.AppRepository
import com.jonaylor.saintjohn.domain.repository.CalendarRepository
import com.jonaylor.saintjohn.domain.repository.ChatRepository
import com.jonaylor.saintjohn.domain.repository.NoteRepository
import com.jonaylor.saintjohn.domain.repository.WeatherRepository
import com.jonaylor.saintjohn.domain.usecase.AppCategorizationUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindAppLauncher(impl: com.jonaylor.saintjohn.data.repository.AppLauncherImpl): com.jonaylor.saintjohn.domain.repository.AppLauncher
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAppCategorizationUseCase(): AppCategorizationUseCase {
        return AppCategorizationUseCase()
    }
}
