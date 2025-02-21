package com.supersonic.walletwatcher.ui.screens.wallet

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.ui.components.TokenBalancesList
import com.supersonic.walletwatcher.utils.abbreviate
import com.supersonic.walletwatcher.utils.formatToCurrency

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
//    tokensList: List<TokenBalance>,
    walletAddress: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    ) {

    val tokensList by viewModel.tokensList.collectAsState()

    Scaffold(
        topBar = {
            WalletTopBar(
                title = walletAddress,
                onCloseClick = onNavigateBack,
                onRefreshClick = { viewModel.updateTokensList(walletAddress) },
                onFavoriteClick = { TODO() }
            )
        },
        content = {
            WalletScreenContent(
                tokensList = tokensList,
                modifier = Modifier.padding(it)
            )
        },
        modifier = modifier
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WalletTopBar(
    title: String,
    onCloseClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    var isTopBarExpended by remember { mutableStateOf(false) }

    val topBarHeight by animateDpAsState(
        targetValue = if (isTopBarExpended) TopAppBarDefaults.MediumAppBarExpandedHeight
                        else TopAppBarDefaults.TopAppBarExpandedHeight,
        )

        TopAppBar(
            modifier = modifier,
            title = {
                Text(
                    text = if (isTopBarExpended) title else title.abbreviate(),
                    maxLines = if (isTopBarExpended) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                isTopBarExpended = !isTopBarExpended
//                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                            },
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(title))
//                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .animateContentSize()
                )
            },
            expandedHeight = topBarHeight,
            navigationIcon = {
                IconButton(
                    onClick = {
                        onCloseClick()
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close WalletScreen icon"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onRefreshClick
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(
                    onClick = onFavoriteClick
                ) {
                    Icon(imageVector = Icons.TwoTone.Star, contentDescription = "Favorite")
                }
            },
        )

}

@Composable
private fun WalletScreenContent(
    tokensList: List<TokenBalance>,
    modifier: Modifier = Modifier
) {

    val totalUsdValue = tokensList.mapNotNull { it.usd_value }.sum()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
    ) {
        Text(
            text = "Total balance",
            style = typography.titleLarge
        )
        Text(
            text = totalUsdValue.formatToCurrency(),
            style = typography.displayMedium
        )
//        Spacer(Modifier.height(48.dp))
        TokenBalancesList(
            tokensList = tokensList.filter { it.usd_value != null },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}