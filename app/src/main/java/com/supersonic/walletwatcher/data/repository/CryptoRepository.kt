package com.supersonic.walletwatcher.data.repository

import com.supersonic.walletwatcher.data.local.dao.FavoriteWalletDao
import com.supersonic.walletwatcher.data.local.dao.SearchHistoryDao
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.ApiService
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CryptoRepository @Inject constructor(
    private val apiService: ApiService,
    private val favoriteWalletDao: FavoriteWalletDao,
    private val searchHistoryDao: SearchHistoryDao
) {

    suspend fun getWalletTokenBalances(walletAddress: String): ResultWrapper<List<Token>> {
        return apiService.getWalletTokenBalances(walletAddress)
    }

    suspend fun getWalletTransactionHistory(walletAddress: String): ResultWrapper<List<Transaction>> {
        return apiService.getWalletTransactionHistory(walletAddress)
    }

    fun getAllFavoriteWallets(): Flow<List<FavoriteWalletEntity>> = favoriteWalletDao.getFavoriteWallets()
    suspend fun getFavoriteWallet(address: String): FavoriteWalletEntity? = favoriteWalletDao.getFavoriteWallet(address)
    suspend fun addFavorite(wallet: FavoriteWalletEntity) = favoriteWalletDao.addFavoriteWallet(wallet)
    suspend fun removeFavorite(address: String) = favoriteWalletDao.removeFavoriteWallet(address)
    suspend fun isWalletFavorite(address: String): Boolean = favoriteWalletDao.isWalletFavorite(address)
    suspend fun updateFavorite(wallet: FavoriteWalletEntity) = favoriteWalletDao.updateFavoriteWallet(wallet)
    suspend fun clearFavorites() = favoriteWalletDao.clearFavorites()

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getSearchHistory()
    suspend fun addSearchHistory(entry: SearchHistoryEntity) = searchHistoryDao.insertSearchHistory(entry)
    suspend fun deleteSearchHistory(address: String) = searchHistoryDao.deleteFromSearchHistory(address)
    suspend fun clearSearchHistory() = searchHistoryDao.clearSearchHistory()

}