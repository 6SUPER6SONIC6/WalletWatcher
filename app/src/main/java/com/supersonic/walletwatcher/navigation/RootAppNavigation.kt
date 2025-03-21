package com.supersonic.walletwatcher.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.supersonic.walletwatcher.ui.screens.main.MainScreen
import com.supersonic.walletwatcher.ui.screens.main.MainViewModel
import com.supersonic.walletwatcher.ui.screens.wallet.WalletScreen
import com.supersonic.walletwatcher.ui.screens.wallet.WalletViewModel

@Composable
fun RootAppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = MainScreen
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 500)
            )
        }) {
        composable<MainScreen> {
            MainScreen(
                viewModel = hiltViewModel<MainViewModel>(),
                onNavigationToWalletScreen = { walletAddress ->
                    navController.navigate(
                        WalletScreen(walletAddress)
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        composable<WalletScreen> {
            val args = it.toRoute<WalletScreen>()

            val mainViewModel =
                hiltViewModel<MainViewModel>(navController.getBackStackEntry(MainScreen))
            val walletViewModel = hiltViewModel<WalletViewModel>()

            val tokensList by remember { derivedStateOf { mainViewModel.mainUiState.value.tokensList } }
            val transactionsList by remember { derivedStateOf { mainViewModel.mainUiState.value.transactionHistoryList } }

            LaunchedEffect(args.walletAddress, tokensList, transactionsList) {
                walletViewModel.loadWalletData(args.walletAddress, tokensList, transactionsList)
            }

            WalletScreen(
                viewModel = walletViewModel, onNavigateBack = {
                    navController.navigateUp()
                })
        }
    }
}