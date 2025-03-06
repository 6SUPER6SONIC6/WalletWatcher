package com.supersonic.walletwatcher.ui.screens.wallet

import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction

data class WalletUiState(
    val walletAddress: String = "",
    val tokensList: List<Token> = listOf(),
    val transactionHistoryList: List<Transaction> = listOf(),
    val tabs: List<WalletScreenTab> = listOf(
        WalletScreenTab.Portfolio(),
        WalletScreenTab.TransactionsHistory()
                ),
    val selectedTab: WalletScreenTab = WalletScreenTab.Portfolio(),
    val refreshUiSate: RefreshUiSate = RefreshUiSate.Idle
)

sealed class RefreshUiSate {
    data object Idle: RefreshUiSate()
    data object InProgress: RefreshUiSate()
    data object Success: RefreshUiSate()
    data class Error(val message: String): RefreshUiSate()
}

sealed class WalletScreenTab {
    data class Portfolio(val title: String = "Portfolio"): WalletScreenTab()
    data class TransactionsHistory(val title: String = "Transactions"): WalletScreenTab()
}