package com.irys.app.data.di

import com.irys.app.data.repo.AppSettingsRepositoryImpl
import com.irys.app.data.repo.ConversationRepositoryImpl
import com.irys.app.data.repo.MessageRepositoryImpl
import com.irys.app.data.repo.PeerRepositoryImpl
import com.irys.app.data.repo.PermissionRepositoryImpl
import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import com.irys.app.domain.repo.PeerRepository
import com.irys.app.domain.repo.PermissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        impl: PermissionRepositoryImpl
    ): PermissionRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindPeerRepository(
        impl: PeerRepositoryImpl
    ): PeerRepository

    @Binds
    @Singleton
    abstract fun bindBleDiscoveryRepository(
        impl: com.irys.app.data.repo.BleDiscoveryRepositoryImpl
    ): com.irys.app.domain.repo.BleDiscoveryRepository
}
