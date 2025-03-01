package com.supersonic.walletwatcher.ui.screens.main

import com.supersonic.walletwatcher.data.remote.models.Token

data class MainUiState(
    val walletAddress: String = "",
    val tokensList: List<Token> = listOf(),
    val validationResult: WalletAddressValidationResult = WalletAddressValidationResult.CORRECT,
    val fetchingUiState: FetchingUiState = FetchingUiState.Idle
)

sealed class FetchingUiState {
    data object Idle: FetchingUiState()
    data object InProgress: FetchingUiState()
    data object Success : FetchingUiState()
    data object NavigateToWallet : FetchingUiState()
    data class Error(val message: String) : FetchingUiState()
}