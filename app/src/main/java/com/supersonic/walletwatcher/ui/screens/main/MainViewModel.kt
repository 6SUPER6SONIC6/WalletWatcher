package com.supersonic.walletwatcher.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.walletwatcher.data.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private fun updateFetchingSate(fetchingUiState: FetchingUiState){
        _mainUiState.update { it.copy(fetchingUiState = fetchingUiState) }
    }

    init {
        resetState()
    }

    fun onWalletAddress(walletAddress: String){
        _mainUiState.update { it.copy(walletAddress = walletAddress)}
    }

    fun fetchWalletBalance(){
        viewModelScope.launch {
            val validationResult = walletAddressValidator(_mainUiState.value.walletAddress)
            if (validationResult != WalletAddressValidationResult.CORRECT) {
                _mainUiState.update { it.copy(validationResult = validationResult) }
                updateFetchingSate(FetchingUiState.Error)
                delay(1000)
                updateFetchingSate(FetchingUiState.Idle)
                return@launch
            }
            updateFetchingSate(FetchingUiState.InProgress)
            _mainUiState.update { it.copy(validationResult = WalletAddressValidationResult.CORRECT) }
            try {
                val tokensList = repository.getWalletTokenBalances(_mainUiState.value.walletAddress)
                _mainUiState.update { it.copy(tokensList = tokensList) }
                updateFetchingSate(FetchingUiState.Success)
                delay(500)
                updateFetchingSate(FetchingUiState.NavigateToWallet)
            } catch (e: Exception){
                resetState()
            }

        }

    }

    fun resetState(){
        _mainUiState.value = MainUiState()
    }
}