package com.supersonic.walletwatcher.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _walletUiState = MutableStateFlow(WalletUiState())
    val walletUiState = _walletUiState.asStateFlow()

    private fun updateRefreshState(newState: RefreshUiSate){
        _walletUiState.update { it.copy(refreshUiSate = newState) }
    }

    fun loadTokensList(tokensList: List<TokenBalance>){
        _walletUiState.update { it.copy(tokensList = tokensList) }
    }

    fun loadWalletAddress(address: String){
        _walletUiState.update { it.copy(walletAddress = address) }
    }

    fun updateTokensList(){
        viewModelScope.launch {
            updateRefreshState(RefreshUiSate.InProgress)
            when(val result = repository.getWalletTokenBalances(_walletUiState.value.walletAddress)){
                is ResultWrapper.Success -> onRefreshSuccess(result.data)
                is ResultWrapper.Error -> onRefreshError(result.message)
            }
        }
    }

    private fun onRefreshSuccess(result: List<TokenBalance>){
        _walletUiState.update { it.copy(tokensList = result) }
        updateRefreshState(RefreshUiSate.Success)
        viewModelScope.launch {
            delay(1000)
            updateRefreshState(RefreshUiSate.Idle)
        }
    }

    private fun onRefreshError(message: String){
        updateRefreshState(RefreshUiSate.Error(message))
        viewModelScope.launch {
            delay(1000)
            updateRefreshState(RefreshUiSate.Idle)
        }
    }
}