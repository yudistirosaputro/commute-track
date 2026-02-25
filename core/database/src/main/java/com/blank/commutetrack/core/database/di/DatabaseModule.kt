package com.blank.commutetrack.core.database.di

import android.content.Context
import androidx.room.Room
import com.blank.commutetrack.core.database.CommuteTrackDatabase
import com.blank.commutetrack.core.database.dao.CommuteSessionDao
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
    fun provideDatabase(@ApplicationContext context: Context): CommuteTrackDatabase =
        Room.databaseBuilder(
            context,
            CommuteTrackDatabase::class.java,
            "commute_track.db"
        )
        .addMigrations(CommuteTrackDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideCommuteSessionDao(database: CommuteTrackDatabase): CommuteSessionDao =
        database.commuteSessionDao()
}
