package com.blank.commutetrack.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blank.commutetrack.core.database.dao.CommuteSessionDao
import com.blank.commutetrack.core.database.entity.CommuteSessionEntity

@Database(
    entities = [CommuteSessionEntity::class],
    version = 2,
    exportSchema = true
)
abstract class CommuteTrackDatabase : RoomDatabase() {
    abstract fun commuteSessionDao(): CommuteSessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE commute_sessions ADD COLUMN pausedMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE commute_sessions ADD COLUMN pauseCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE commute_sessions ADD COLUMN averageSpeedKmh REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}
