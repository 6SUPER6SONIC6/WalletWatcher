package com.supersonic.walletwatcher.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.ui.screens.main.MainScreen
import com.supersonic.walletwatcher.ui.screens.main.MainViewModel
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

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = args.walletAddress, modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp))
                LazyColumn {
                    items(tokenBalances){ token: TokenBalance ->
                        Text("${token.symbol} - ${token.usd_value}$")
                    }
                }
            }

        }

    }
}