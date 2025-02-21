package com.supersonic.walletwatcher.data.repository

import com.supersonic.walletwatcher.data.remote.ApiService
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getWalletTokenBalances(walletAddress: String): ResultWrapper<List<TokenBalance>> {
        return apiService.getWalletTokenBalances(walletAddress)
    }

}