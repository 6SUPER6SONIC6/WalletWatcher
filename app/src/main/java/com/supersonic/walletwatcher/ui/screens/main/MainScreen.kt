package com.supersonic.walletwatcher.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen:(List<TokenBalance>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = { MainTopBar(stringResource(R.string.app_name)) },
        content = {
            MainScreenContent(
                onSearchButtonClick = {
                   scope.launch {
                       onNavigationToWalletScreen(viewModel.fetchWalletBalance(it), it)
                   }
                },
                modifier = Modifier
                    .padding(it)
            )
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier
    )
}

@Composable
private fun MainScreenContent(
    onSearchButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var walletAddress by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WalletAddressInput(
            text = walletAddress,
            onTextChange = { walletAddress = it},
            onSearchClick = { onSearchButtonClick(walletAddress)},
            onClearTextClick = {walletAddress = ""},
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@Composable
private fun WalletAddressInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text("Enter wallet address")
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = {
                AnimatedVisibility(
                    visible = text.isNotEmpty(),
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    IconButton(
                        onClick = onClearTextClick
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                    }
                }
            }
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onSearchClick,
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        }
    }

}