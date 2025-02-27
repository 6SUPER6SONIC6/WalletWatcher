package com.supersonic.walletwatcher.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

    fun loadWalletData(walletAddress: String, tokensList: List<TokenBalance>){
        _walletUiState.update { it.copy(walletAddress = walletAddress) }
        _walletUiState.update { it.copy(tokensList = tokensList) }
        loadTransactionsHistory(walletAddress)
    }

    private fun loadTransactionsHistory(walletAddress: String){
        viewModelScope.launch {
            when(val transactions = repository.getWalletTransactionHistory(walletAddress)){
                is ResultWrapper.Success -> _walletUiState.update { it.copy(transactionHistoryList = transactions.data) }
                is ResultWrapper.Error -> {
                    onRefreshError(transactions.message)
                    return@launch
                }
            }
        }
    }

    fun refreshWallet(){
        viewModelScope.launch {
            updateRefreshState(RefreshUiSate.InProgress)

            val walletAddress = _walletUiState.value.walletAddress

            val balancesResult = async { repository.getWalletTokenBalances(walletAddress) }
            val transactionsResult = async { repository.getWalletTransactionHistory(walletAddress) }

            when(val balances = balancesResult.await()) {
                is ResultWrapper.Success -> _walletUiState.update { it.copy(tokensList = balances.data) }
                is ResultWrapper.Error -> {
                    onRefreshError(balances.message)
                    return@launch
                }
            }
            when(val transactions = transactionsResult.await()){
                is ResultWrapper.Success -> _walletUiState.update { it.copy(transactionHistoryList = transactions.data) }
                is ResultWrapper.Error -> {
                    onRefreshError(transactions.message)
                    return@launch
                }
            }
            onRefreshSuccess()
        }
    }

    private fun onRefreshSuccess(){
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

    fun onTabSelected(index: Int){
        _walletUiState.update { it.copy(selectedTabIndex = index) }
    }
}