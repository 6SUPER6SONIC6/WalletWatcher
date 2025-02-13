package com.supersonic.walletwatcher.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.ui.components.WalletTextField
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen:(List<TokenBalance>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {

        },
        content = {
            MainScreenContent(
                onSearchButtonClick = {
                   scope.launch {
                       onNavigationToWalletScreen(viewModel.fetchWalletBalance(it), it)
                   }
                },
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            )
        }
    )

}

@Composable
private fun MainScreenContent(
    onSearchButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var walletAddress by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            WalletTextField(
                value = walletAddress,
                onValueChange = { walletAddress = it },
                hint = "Enter a wallet address",
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    IconButton(
                        onClick = { onSearchButtonClick(walletAddress) }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )


        }
    }

}