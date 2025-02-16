package com.supersonic.walletwatcher.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.ui.screens.main.MainScreen
import com.supersonic.walletwatcher.ui.screens.main.MainViewModel
import com.supersonic.walletwatcher.ui.screens.wallet.WalletScreen
import com.supersonic.walletwatcher.ui.screens.wallet.WalletViewModel
import kotlinx.serialization.json.Json

@Composable
fun RootAppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = MainScreen
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ){
        composable<MainScreen> {

            MainScreen(
                viewModel = hiltViewModel<MainViewModel>(),
                onNavigationToWalletScreen = { tokenBalances, walletAddress ->
                    navController.navigate(
                        WalletScreen(
                            tokenBalances.map { item ->
                        Json.encodeToString(TokenBalance.serializer(), item)
                    }, walletAddress)
                    )
                },
                modifier = Modifier.fillMaxSize()
            )

        }

        composable<WalletScreen> {
            val args = it.toRoute<WalletScreen>()
            val tokenBalances = if (args.walletBalances.isEmpty()){
                emptyList()
            } else {
                args.walletBalances.map { tokenBalance ->
                    Json.decodeFromString(TokenBalance.serializer(), tokenBalance)
                }
            }

            WalletScreen(
                viewModel = hiltViewModel<WalletViewModel>(),
                tokensList = tokenBalances,
                walletAddress = args.walletAddress,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

    }
}