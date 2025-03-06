package com.supersonic.walletwatcher.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteWalletDao {

    @Query("SELECT * FROM favorite_wallets ORDER BY addedAt DESC")
    fun getFavoriteWallets(): Flow<List<FavoriteWalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteWallet(wallet: FavoriteWalletEntity)

    @Query("DELETE FROM favorite_wallets WHERE address = :walletAddress")
    suspend fun removeFavoriteWallet(walletAddress: String)

    @Query("SELECT EXISTS(SELECT 1  FROM favorite_wallets WHERE address = :walletAddress)")
    suspend fun isWalletFavorite(walletAddress: String): Boolean
}