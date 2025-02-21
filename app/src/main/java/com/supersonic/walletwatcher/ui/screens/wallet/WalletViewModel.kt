package com.supersonic.walletwatcher.ui.screens.wallet

import androidx.lifecycle.ViewModel
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _tokensList = MutableStateFlow<List<TokenBalance>>(listOf())
    val tokensList = _tokensList.asStateFlow()

    fun loadTokensList(tokensList: List<TokenBalance>){
        _tokensList.value = tokensList
    }

    fun updateTokensList(walletAddress: String){
//        viewModelScope.launch {
//            val updatedList = repository.getWalletTokenBalances(walletAddress)
//            _tokensList.value = updatedList
//        }
    }
}