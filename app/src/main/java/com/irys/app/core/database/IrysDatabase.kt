package com.irys.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.irys.app.core.database.dao.AppSettingDao
import com.irys.app.core.database.entity.AppSettingEntity

@Database(
    entities = [
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IrysDatabase : RoomDatabase() {
    abstract fun appSettingDao(): AppSettingDao
}
