package com.blank.commutetrack.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blank.commutetrack.core.database.dao.CommuteSessionDao
import com.blank.commutetrack.core.database.entity.CommuteSessionEntity

@Database(
    entities = [CommuteSessionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CommuteTrackDatabase : RoomDatabase() {
    abstract fun commuteSessionDao(): CommuteSessionDao
}
