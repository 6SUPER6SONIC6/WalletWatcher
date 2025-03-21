package com.supersonic.walletwatcher.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    private val walletAddressValidator = WalletAddressValidator()

    init {
        loadSearchHistory()
        loadFavoriteWallets()
    }

    private fun updateFetchingSate(fetchingUiState: FetchingUiState) {
        _mainUiState.update { it.copy(fetchingState = fetchingUiState) }
    }

    private fun updateBottomSheetState(bottomSheetUiState: MainBottomSheetUiState) {
        _mainUiState.update { it.copy(bottomSheetState = bottomSheetUiState) }
    }

    private fun updateDialogState(dialogUiState: MainDialogUiState) {
        _mainUiState.update { it.copy(dialogState = dialogUiState) }
    }

    fun updateWalletAddress(input: String) {
        _mainUiState.update { it.copy(walletAddress = input.trim()) }
    }

    fun navigateToWallet() {
        viewModelScope.launch {
            if (validateWalletAddress()) {
                fetchWalletData(_mainUiState.value.walletAddress)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun clearSavedWallets() {
        viewModelScope.launch {
            repository.clearFavorites()
        }
    }

    fun showFavoriteWalletBottomSheet(favoriteEntity: FavoriteWalletEntity) {
        updateBottomSheetState(MainBottomSheetUiState.ShowFavoriteBottomSheet(favoriteEntity))
    }

    fun showHistoryBottomSheet(historyEntity: SearchHistoryEntity) {
        updateBottomSheetState(MainBottomSheetUiState.ShowHistoryBottomSheet(historyEntity))
    }

    fun dismissBottomSheet() {
        updateBottomSheetState(MainBottomSheetUiState.NotShown)
    }

    fun showClearHistoryDialog() {
        updateDialogState(MainDialogUiState.ShowClearHistoryDialog)
    }

    fun showClearSavedDialog() {
        updateDialogState(MainDialogUiState.ShowClearSavedDialog)
    }

    fun showRemoveSavedDialog(savedWallet: FavoriteWalletEntity) {
        updateDialogState(MainDialogUiState.ShowRemoveSavedDialog(savedWallet))
    }

    fun dismissDialog() {
        updateDialogState(MainDialogUiState.NotShown)
    }

    private fun fetchWalletData(address: String) {
        viewModelScope.launch {
            updateFetchingSate(FetchingUiState.InProgress)

            val balancesResult = async { repository.getWalletTokenBalances(address) }
            val transactionsResult = async { repository.getWalletTransactionHistory(address) }

            when (val balances = balancesResult.await()) {
                is ResultWrapper.Success -> _mainUiState.update { it.copy(tokensList = balances.data) }
                is ResultWrapper.Error -> {
                    onError(balances.message)
                    return@launch
                }
            }

            when (val transactions = transactionsResult.await()) {
                is ResultWrapper.Success -> _mainUiState.update { it.copy(transactionHistoryList = transactions.data) }
                is ResultWrapper.Error -> {
                    onError(transactions.message)
                    return@launch
                }
            }

            onSuccess()
        }
    }

    private fun loadFavoriteWallets() {
        viewModelScope.launch {
            repository.getAllFavoriteWallets().collectLatest { favorites ->
                _mainUiState.update { it.copy(favoriteWallets = favorites) }
            }
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            repository.getSearchHistory().collectLatest { history ->
                _mainUiState.update { it.copy(searchHistory = history) }
            }
        }
    }

    fun removeFavorite(address: String) {
        viewModelScope.launch {
            repository.removeFavorite(address)
        }
    }

    fun addFavorite(favoriteEntity: FavoriteWalletEntity) {
        viewModelScope.launch {
            repository.addFavorite(favoriteEntity)
        }
    }

    fun renameFavoriteWallet(favoriteEntity: FavoriteWalletEntity) {
        viewModelScope.launch {
            repository.updateFavorite(favoriteEntity)
        }
    }

    fun isFavorite(address: String): Boolean {
        return _mainUiState.value.favoriteWallets.any { it.address == address }

    }

    fun removeHistoryWallet(address: String) {
        viewModelScope.launch {
            repository.deleteSearchHistory(address)
        }
    }

    private fun onSuccess() {
        updateFetchingSate(FetchingUiState.Success)
        viewModelScope.launch {
            delay(500)
            updateFetchingSate(FetchingUiState.NavigateToWallet)
            repository.addSearchHistory(SearchHistoryEntity(address = _mainUiState.value.walletAddress))
        }
    }

    private fun onError(message: String) {
        updateFetchingSate(FetchingUiState.Error(message))
        viewModelScope.launch {
            delay(1000)
            updateFetchingSate(FetchingUiState.Idle)
        }
    }

    private fun validateWalletAddress(): Boolean {
        val validationResult = walletAddressValidator(_mainUiState.value.walletAddress)
        return if (validationResult != WalletAddressValidationResult.CORRECT) {
            _mainUiState.update { it.copy(validationResult = validationResult) }
            onError("")
            false
        } else {
            _mainUiState.update { it.copy(validationResult = WalletAddressValidationResult.CORRECT) }
            true
        }
    }

    fun onTabSelected(tab: MainScreenTab) {
        _mainUiState.update { it.copy(selectedTab = tab) }
    }


    fun resetState() {
        _mainUiState.update {
            it.copy(
                walletAddress = "",
                tokensList = listOf(),
                validationResult = WalletAddressValidationResult.CORRECT,
                fetchingState = FetchingUiState.Idle,
                bottomSheetState = MainBottomSheetUiState.NotShown
            )
        }
    }

}