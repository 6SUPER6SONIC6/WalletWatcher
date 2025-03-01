package com.supersonic.walletwatcher.ui.screens.wallet

import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction

data class WalletUiState(
    val walletAddress: String = "",
    val tokensList: List<Token> = listOf(),
    val transactionHistoryList: List<Transaction> = listOf(),
    val tabs: List<WalletTabItem> = listOf(WalletTabItem("Portfolio"),WalletTabItem("Transactions")),
    val selectedTabIndex: Int = 0,
    val refreshUiSate: RefreshUiSate = RefreshUiSate.Idle
)

sealed class RefreshUiSate {
    data object Idle: RefreshUiSate()
    data object InProgress: RefreshUiSate()
    data object Success: RefreshUiSate()
    data class Error(val message: String): RefreshUiSate()
}

data class WalletTabItem(
    val title: String
)