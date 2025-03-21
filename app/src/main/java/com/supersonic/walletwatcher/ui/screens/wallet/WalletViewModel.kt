package com.supersonic.walletwatcher.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction
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
    private val repository: CryptoRepository,
) : ViewModel() {

    private val _walletUiState = MutableStateFlow(WalletUiState())
    val walletUiState = _walletUiState.asStateFlow()

    private fun updateRefreshState(refreshUiSate: RefreshUiSate) {
        _walletUiState.update { it.copy(refreshUiSate = refreshUiSate) }
    }

    private fun updateFavoriteState(favoriteUiState: FavoriteUiState) {
        _walletUiState.update { it.copy(favoriteUiState = favoriteUiState) }
    }

    private fun updateTransactionBottomSheetState(transactionBottomSheetUiState: TransactionBottomSheetUiState) {
        _walletUiState.update { it.copy(transactionBottomSheetState = transactionBottomSheetUiState) }
    }

    fun loadWalletData(
        walletAddress: String, tokensList: List<Token>, transactionsList: List<Transaction>
    ) {
        _walletUiState.update {
            it.copy(
                walletAddress = walletAddress,
                tokensList = tokensList,
                transactionHistoryList = transactionsList
            )
        }
        loadWalletName()
        isWalletFavorite(walletAddress)
    }

    private fun loadWalletName() {
        viewModelScope.launch {
            val favoriteWallet = repository.getFavoriteWallet(_walletUiState.value.walletAddress)
            if (favoriteWallet != null) {
                _walletUiState.update { it.copy(walletName = favoriteWallet.name) }
            } else {
                _walletUiState.update { it.copy(walletName = null) }
            }
        }
    }

    fun onFavoriteClick() {
        if (_walletUiState.value.isWalletFavorite) {
            updateFavoriteState(FavoriteUiState.ShowRemoveSavedDialog)
        } else updateFavoriteState(FavoriteUiState.ShowSaveWalletBottomSheet)
    }

    fun dismissFavoriteDialog() {
        updateFavoriteState(FavoriteUiState.Idle)
    }

    fun showTransactionInfoBottomSheet(transaction: Transaction) {
        updateTransactionBottomSheetState(
            TransactionBottomSheetUiState.ShowTransactionInfoBottomSheet(
                transaction
            )
        )
    }

    fun dismissTransactionInfoBottomSheet() {
        updateTransactionBottomSheetState(TransactionBottomSheetUiState.NotShown)
    }

    private fun isWalletFavorite(address: String) {
        viewModelScope.launch {
            _walletUiState.update { it.copy(isWalletFavorite = repository.isWalletFavorite(address)) }
        }
    }

    fun saveWallet(walletName: String?) {
        viewModelScope.launch {
            repository.addFavorite(
                FavoriteWalletEntity(
                    address = _walletUiState.value.walletAddress, name = walletName
                )
            )
            loadWalletName()
        }
        updateFavoriteState(FavoriteUiState.Idle)
        _walletUiState.update { it.copy(isWalletFavorite = true) }
    }

    fun removeWallet() {
        viewModelScope.launch {
            repository.removeFavorite(_walletUiState.value.walletAddress)
            loadWalletName()
        }
        updateFavoriteState(FavoriteUiState.Idle)
        _walletUiState.update { it.copy(isWalletFavorite = false) }
    }

    fun refreshWallet() {
        viewModelScope.launch {
            updateRefreshState(RefreshUiSate.InProgress)

            val walletAddress = _walletUiState.value.walletAddress

            val balancesResult = async { repository.getWalletTokenBalances(walletAddress) }
            val transactionsResult = async { repository.getWalletTransactionHistory(walletAddress) }

            when (val balances = balancesResult.await()) {
                is ResultWrapper.Success -> _walletUiState.update { it.copy(tokensList = balances.data) }
                is ResultWrapper.Error -> {
                    onRefreshError(balances.message)
                    return@launch
                }
            }
            when (val transactions = transactionsResult.await()) {
                is ResultWrapper.Success -> _walletUiState.update { it.copy(transactionHistoryList = transactions.data) }
                is ResultWrapper.Error -> {
                    onRefreshError(transactions.message)
                    return@launch
                }
            }
            onRefreshSuccess()
        }
    }

    private fun onRefreshSuccess() {
        updateRefreshState(RefreshUiSate.Success)
        viewModelScope.launch {
            delay(1000)
            updateRefreshState(RefreshUiSate.Idle)
        }
    }

    private fun onRefreshError(message: String) {
        updateRefreshState(RefreshUiSate.Error(message))
        viewModelScope.launch {
            delay(1000)
            updateRefreshState(RefreshUiSate.Idle)
        }
    }

    fun onTabSelected(tab: WalletScreenTab) {
        _walletUiState.update { it.copy(selectedTab = tab) }
    }
}