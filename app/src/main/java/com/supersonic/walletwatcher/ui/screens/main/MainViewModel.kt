package com.supersonic.walletwatcher.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
        loadFavoriteWallets()
        loadSearchHistory()
    }

    private fun updateFetchingSate(fetchingUiState: FetchingUiState){
        _mainUiState.update { it.copy(fetchingUiState = fetchingUiState) }
    }

    fun updateWalletAddress(input: String){
        _mainUiState.update { it.copy(walletAddress = input.trim())}
    }

    fun navigateToWallet(){
        viewModelScope.launch {
            if (validateWalletAddress()){
                fetchWalletData(_mainUiState.value.walletAddress)
            }
        }
    }

    fun clearSearchHistory(){
        viewModelScope.launch{
            repository.clearSearchHistory()
        }
    }

    private fun fetchWalletData(address: String){
        viewModelScope.launch {
            updateFetchingSate(FetchingUiState.InProgress)
            when(val result = repository.getWalletTokenBalances(address)){
                is ResultWrapper.Success -> { onSuccess(result.data) }
                is ResultWrapper.Error -> { onError(result.message) }
            }
        }
    }

    private fun loadFavoriteWallets(){
        viewModelScope.launch {
            repository.getFavoriteWallets().collectLatest { favorites ->
                _mainUiState.update { it.copy(favoriteWallets = favorites) }
            }
        }
    }

    private fun loadSearchHistory(){
        viewModelScope.launch {
            repository.getSearchHistory().collectLatest { history ->
                _mainUiState.update { it.copy(searchHistory = history) }
            }
        }
    }

    private fun onSuccess(result: List<Token>){
        _mainUiState.update { it.copy(tokensList = result) }
        updateFetchingSate(FetchingUiState.Success)
        viewModelScope.launch {
            delay(500)
            updateFetchingSate(FetchingUiState.NavigateToWallet)
            repository.addSearchHistory(SearchHistoryEntity(address = _mainUiState.value.walletAddress))
        }
    }

    private fun onError(message: String){
        updateFetchingSate(FetchingUiState.Error(message))
        viewModelScope.launch {
            delay(1000)
            updateFetchingSate(FetchingUiState.Idle)
        }
    }

    private fun validateWalletAddress(): Boolean{
            val validationResult = walletAddressValidator(_mainUiState.value.walletAddress)
            return if (validationResult != WalletAddressValidationResult.CORRECT) {
                _mainUiState.update { it.copy(validationResult = validationResult) }
                onError("")
                false
            } else{
                _mainUiState.update { it.copy(validationResult = WalletAddressValidationResult.CORRECT) }
                true
            }
    }

    fun onTabSelected(tab: MainScreenTab){
        _mainUiState.update { it.copy(selectedTab = tab) }
    }


    fun resetState(){
        _mainUiState.update { it.copy(
            walletAddress = "",
            tokensList = listOf(),
            validationResult = WalletAddressValidationResult.CORRECT,
            fetchingUiState = FetchingUiState.Idle
        ) }
    }

}