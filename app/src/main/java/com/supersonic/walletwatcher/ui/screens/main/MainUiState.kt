package com.supersonic.walletwatcher.ui.screens.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.History
import androidx.compose.ui.graphics.vector.ImageVector
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.models.Token

data class MainUiState(
    val walletAddress: String = "",
    val tokensList: List<Token> = listOf(),
    val favoriteWallets: List<FavoriteWalletEntity> = listOf(),
    val searchHistory: List<SearchHistoryEntity> = listOf(),
    val tabs: List<MainScreenTab> = listOf(
        MainScreenTab.History(),
        MainScreenTab.Favorites(),
    ),
    val selectedTab: MainScreenTab = MainScreenTab.History(),
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

sealed class MainScreenTab {
    data class Favorites(
        val icon: ImageVector = Icons.Outlined.Bookmarks,
        val selectedIcon: ImageVector = Icons.Filled.Bookmarks,
        val titleId: Int = R.string.saved_wallets_title
    ): MainScreenTab()
    data class History(
        val icon: ImageVector = Icons.Outlined.History,
        val selectedIcon: ImageVector = Icons.Filled.History,
        val titleId: Int = R.string.recent_searches_title
    ): MainScreenTab()
}