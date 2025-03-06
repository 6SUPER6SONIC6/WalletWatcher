package com.supersonic.walletwatcher.di

import android.content.Context
import com.supersonic.walletwatcher.data.local.dao.FavoriteWalletDao
import com.supersonic.walletwatcher.data.local.dao.SearchHistoryDao
import com.supersonic.walletwatcher.data.local.db.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFavoriteWalletDao(database: AppDatabase): FavoriteWalletDao = database.favoriteWalletDao()

    @Provides
    fun provideSearchHistoryDao(database: AppDatabase): SearchHistoryDao = database.searchHistoryDao()
}