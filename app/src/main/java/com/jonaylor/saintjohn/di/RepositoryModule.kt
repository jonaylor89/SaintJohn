package com.jonaylor.saintjohn.di

import com.jonaylor.saintjohn.data.repository.AppRepositoryImpl
import com.jonaylor.saintjohn.domain.repository.AppRepository
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
