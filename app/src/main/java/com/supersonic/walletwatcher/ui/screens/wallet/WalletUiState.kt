package com.supersonic.walletwatcher.ui.screens.wallet

import com.supersonic.walletwatcher.data.remote.models.TokenBalance

data class WalletUiState(
    val walletAddress: String = "",
    val tokensList: List<TokenBalance> = listOf(),
    val refreshUiSate: RefreshUiSate = RefreshUiSate.Idle
)

sealed class RefreshUiSate {
    data object Idle: RefreshUiSate()
    data object InProgress: RefreshUiSate()
    data object Success: RefreshUiSate()
    data class Error(val message: String): RefreshUiSate()
}