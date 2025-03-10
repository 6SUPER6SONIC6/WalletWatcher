package com.supersonic.walletwatcher.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.supersonic.walletwatcher.data.remote.models.Token
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
        }
    ){
        composable<MainScreen> {

            MainScreen(
                viewModel = hiltViewModel<MainViewModel>(),
                onNavigationToWalletScreen = { tokenBalances, walletAddress ->
                    navController.navigate(
                        WalletScreen(
                            walletBalances = tokenBalances.map { item ->
                                Json.encodeToString(Token.serializer(), item) },
                            walletAddress = walletAddress
                        )
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
                    Json.decodeFromString(Token.serializer(), tokenBalance)
                }
            }

            val viewModel = hiltViewModel<WalletViewModel>()
            viewModel.loadWalletData(args.walletAddress, tokenBalances)
            WalletScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

    }
}