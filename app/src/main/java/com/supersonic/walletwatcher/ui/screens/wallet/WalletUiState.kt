package com.supersonic.walletwatcher.ui.screens.wallet

import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction

data class WalletUiState(
    val walletAddress: String = "",
    val walletName: String? = null,
    val tokensList: List<Token> = listOf(),
    val isWalletFavorite: Boolean = false,
    val transactionHistoryList: List<Transaction> = listOf(),
    val tabs: List<WalletScreenTab> = listOf(
        WalletScreenTab.Portfolio(), WalletScreenTab.TransactionsHistory()
    ),
    val selectedTab: WalletScreenTab = WalletScreenTab.Portfolio(),
    val refreshUiSate: RefreshUiSate = RefreshUiSate.Idle,
    val favoriteUiState: FavoriteUiState = FavoriteUiState.Idle,
    val transactionBottomSheetState: TransactionBottomSheetUiState = TransactionBottomSheetUiState.NotShown
)

sealed class RefreshUiSate {
    data object Idle : RefreshUiSate()
    data object InProgress : RefreshUiSate()
    data object Success : RefreshUiSate()
    data class Error(val message: String) : RefreshUiSate()
}

sealed class FavoriteUiState {
    data object Idle : FavoriteUiState()
    data object ShowSaveWalletBottomSheet : FavoriteUiState()
    data object ShowRemoveSavedDialog : FavoriteUiState()
}

sealed class TransactionBottomSheetUiState {
    data object NotShown : TransactionBottomSheetUiState()
    data class ShowTransactionInfoBottomSheet(val transaction: Transaction) :
        TransactionBottomSheetUiState()
}

sealed class WalletScreenTab {
    data class Portfolio(val title: String = "Portfolio") : WalletScreenTab()
    data class TransactionsHistory(val title: String = "Transactions") : WalletScreenTab()
}