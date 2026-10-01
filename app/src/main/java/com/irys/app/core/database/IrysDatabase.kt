package com.irys.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.irys.app.core.database.dao.AppSettingDao
import com.irys.app.core.database.dao.ConversationDao
import com.irys.app.core.database.dao.MessageDao
import com.irys.app.core.database.dao.PeerDao
import com.irys.app.core.database.entity.AppSettingEntity
import com.irys.app.core.database.entity.ConversationEntity
import com.irys.app.core.database.entity.MessageEntity
import com.irys.app.core.database.entity.PeerEntity

@Database(
    entities = [
        AppSettingEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        PeerEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class IrysDatabase : RoomDatabase() {
    abstract fun appSettingDao(): AppSettingDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun peerDao(): PeerDao
}
