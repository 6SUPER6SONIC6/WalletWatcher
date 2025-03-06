package com.supersonic.walletwatcher.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val address: String,
    val searchedAt: Long = System.currentTimeMillis()
)
