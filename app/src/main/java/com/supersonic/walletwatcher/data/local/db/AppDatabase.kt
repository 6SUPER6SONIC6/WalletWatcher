package com.supersonic.walletwatcher.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.supersonic.walletwatcher.data.local.dao.FavoriteWalletDao
import com.supersonic.walletwatcher.data.local.dao.SearchHistoryDao
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity


@Database(entities = [FavoriteWalletEntity::class, SearchHistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun favoriteWalletDao(): FavoriteWalletDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object{
        @Volatile
        private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this){
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "wallet_watcher.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}