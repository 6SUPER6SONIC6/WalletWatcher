package com.supersonic.walletwatcher.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen:(List<TokenBalance>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var wallet by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column {
            TextField(
                value = wallet,
                onValueChange = {wallet = it}
            )

            IconButton(
                onClick = {
                    scope.launch {
                        onNavigationToWalletScreen(viewModel.fetchWalletBalance(wallet), wallet)
                         }
                }
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
            }
        }
    }
}