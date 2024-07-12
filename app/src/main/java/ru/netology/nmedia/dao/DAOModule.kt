package ru.netology.nmedia.dao

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.db.AppDB

@InstallIn(SingletonComponent::class)
@Module
object DAOModule {
    @Provides
    fun providePostDao(db: AppDB): PostDAO = db.postDAO()

    @Provides
    fun providePostRemoteKeyDao(db: AppDB): PostRemoteKeyDAO = db.postRemoteKeyDAO()
}