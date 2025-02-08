package com.supersonic.walletwatcher.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun RootAppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = MainScreen
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = MainScreen
    ){
        composable<MainScreen> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Button(
                    onClick = {
                        navController.navigate(WalletScreen("0x112532B200980Ddee8226023bEbBE2E6884C31e2"))
                    }
                ) {
                    Text("Search")
                }
            }
        }

        composable<WalletScreen> {
            val args = it.toRoute<WalletScreen>()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = args.walletAddress)
            }

        }

    }
}