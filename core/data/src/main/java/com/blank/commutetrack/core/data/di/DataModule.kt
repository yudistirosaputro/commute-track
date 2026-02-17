package com.blank.commutetrack.core.data.di

import com.blank.commutetrack.core.data.repository.CommuteRepositoryImpl
import com.blank.commutetrack.core.data.repository.SettingsRepositoryImpl
import com.blank.commutetrack.core.domain.repository.CommuteRepository
import com.blank.commutetrack.core.domain.repository.SettingsRepository
import com.blank.commutetrack.core.domain.usecase.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCommuteRepository(impl: CommuteRepositoryImpl): CommuteRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        fun provideGetActiveSessionUseCase(repository: CommuteRepository) =
            GetActiveSessionUseCase(repository)

        @Provides
        fun provideStartSessionUseCase(repository: CommuteRepository) =
            StartSessionUseCase(repository)

        @Provides
        fun provideEndSessionUseCase(repository: CommuteRepository) =
            EndSessionUseCase(repository)

        @Provides
        fun provideGetSessionHistoryUseCase(repository: CommuteRepository) =
            GetSessionHistoryUseCase(repository)

        @Provides
        fun provideGetStatisticsUseCase(repository: CommuteRepository) =
            GetStatisticsUseCase(repository)

        @Provides
        fun provideGetSettingsUseCase(repository: SettingsRepository) =
            GetSettingsUseCase(repository)

        @Provides
        fun provideUpdateSettingsUseCase(repository: SettingsRepository) =
            UpdateSettingsUseCase(repository)
    }
}
