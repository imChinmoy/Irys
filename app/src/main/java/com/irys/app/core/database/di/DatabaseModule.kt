package com.irys.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.irys.app.core.common.AppConstants
import com.irys.app.core.database.IrysDatabase
import com.irys.app.core.database.dao.AppSettingDao
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
    fun provideIrysDatabase(
        @ApplicationContext context: Context
    ): IrysDatabase {
        return Room.databaseBuilder(
            context,
            IrysDatabase::class.java,
            AppConstants.DATABASE_NAME
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideAppSettingDao(database: IrysDatabase): AppSettingDao {
        return database.appSettingDao()
    }
}
