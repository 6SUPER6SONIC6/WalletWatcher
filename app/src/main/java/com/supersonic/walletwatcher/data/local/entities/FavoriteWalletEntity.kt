package com.supersonic.walletwatcher.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_wallets")
data class FavoriteWalletEntity(
    @PrimaryKey val address: String,
    val name: String?,
    val addedAt: Long = System.currentTimeMillis()
)
