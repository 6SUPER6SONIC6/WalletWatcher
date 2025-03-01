package com.supersonic.walletwatcher.data.repository

import com.supersonic.walletwatcher.data.remote.ApiService
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getWalletTokenBalances(walletAddress: String): ResultWrapper<List<Token>> {
        return apiService.getWalletTokenBalances(walletAddress)
    }

    suspend fun getWalletTransactionHistory(walletAddress: String): ResultWrapper<List<Transaction>> {
        return apiService.getWalletTransactionHistory(walletAddress)
    }

}