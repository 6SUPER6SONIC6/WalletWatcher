package com.supersonic.walletwatcher.ui.screens.main

import androidx.lifecycle.ViewModel
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    suspend fun fetchWalletBalance(walletAddress: String) : List<TokenBalance> {
        return repository.getWalletTokenBalances(walletAddress)
    }
}