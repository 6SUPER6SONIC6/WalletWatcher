package com.supersonic.walletwatcher.ui.screens.wallet

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction
import com.supersonic.walletwatcher.data.remote.models.TransactionType
import com.supersonic.walletwatcher.ui.components.Dialog
import com.supersonic.walletwatcher.ui.components.GoUpButton
import com.supersonic.walletwatcher.ui.components.TextFieldWithActionButton
import com.supersonic.walletwatcher.ui.components.TokenBalancesList
import com.supersonic.walletwatcher.ui.components.TransactionsHistoryList
import com.supersonic.walletwatcher.ui.theme.WalletWatcherTheme
import com.supersonic.walletwatcher.utils.abbreviateWalletAddress
import com.supersonic.walletwatcher.utils.formatTimestampToDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val walletUiState by viewModel.walletUiState.collectAsStateWithLifecycle()
    val refreshState = walletUiState.refreshUiSate
    val favoriteState = walletUiState.favoriteUiState
    val transactionBottomSheetState = walletUiState.transactionBottomSheetState

    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(refreshState) {
        when (refreshState) {
            RefreshUiSate.Success -> view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            is RefreshUiSate.Error -> {
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                Toast.makeText(
                    context, refreshState.message, Toast.LENGTH_LONG
                ).show()
            }

            else -> {}
        }
    }

    when (favoriteState) {
        FavoriteUiState.ShowSaveWalletBottomSheet -> SaveWalletModalBottomSheet(
            onDismiss = viewModel::dismissFavoriteDialog, onSaveWallet = viewModel::saveWallet
        )

        FavoriteUiState.ShowRemoveSavedDialog -> Dialog(
            title = stringResource(R.string.remove_saved_wallet_dialog_title),
            text = stringResource(
                R.string.remove_saved_wallet_dialog_text,
                walletUiState.walletName ?: walletUiState.walletAddress.abbreviateWalletAddress()
            ),
            confirmButtonText = stringResource(R.string.remove_saved_wallet_dialog_confirm),
            dismissButtonText = stringResource(R.string.clear_history_dialog_dismiss),
            onConfirm = viewModel::removeWallet,
            onDismiss = viewModel::dismissFavoriteDialog
        )

        else -> {}
    }

    when (transactionBottomSheetState) {
        is TransactionBottomSheetUiState.ShowTransactionInfoBottomSheet -> TransactionInfoModalBottomSheet(
            transaction = transactionBottomSheetState.transaction,
            onDismiss = viewModel::dismissTransactionInfoBottomSheet
        )

        else -> {}
    }

    Scaffold(topBar = {
        WalletTopBar(
            address = walletUiState.walletAddress,
            name = walletUiState.walletName,
            refreshUiSate = walletUiState.refreshUiSate,
            isWalletFavorite = walletUiState.isWalletFavorite,
            onCloseClick = onNavigateBack,
            onRefreshClick = viewModel::refreshWallet,
            onFavoriteClick = viewModel::onFavoriteClick
        )
    }, content = {
        WalletScreenContent(
            tokensList = walletUiState.tokensList,
            transactionsList = walletUiState.transactionHistoryList,
            tabsList = walletUiState.tabs,
            selectedTab = walletUiState.selectedTab,
            onTabSelected = viewModel::onTabSelected,
            onTransactionClick = viewModel::showTransactionInfoBottomSheet,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        )
    }, modifier = modifier
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WalletTopBar(
    address: String,
    name: String?,
    refreshUiSate: RefreshUiSate,
    isWalletFavorite: Boolean,
    onCloseClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    var isTopBarExpended by remember { mutableStateOf(false) }

    val topBarHeight by animateDpAsState(
        targetValue = if (isTopBarExpended) TopAppBarDefaults.MediumAppBarExpandedHeight
        else TopAppBarDefaults.TopAppBarExpandedHeight,
    )

    val infiniteTransition = rememberInfiniteTransition()
    val refreshIconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (refreshUiSate is RefreshUiSate.InProgress) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200), repeatMode = RepeatMode.Restart
        )
    )
    val errorIconRotation by infiniteTransition.animateFloat(
        initialValue = -30f, targetValue = 30f, animationSpec = infiniteRepeatable(
            animation = tween(230), repeatMode = RepeatMode.Reverse
        )
    )

    val favoriteIcon =
        if (isWalletFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = if (isTopBarExpended) address else name ?: address.abbreviateWalletAddress(),
                maxLines = if (isTopBarExpended) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            isTopBarExpended = !isTopBarExpended
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                        },
                        onLongClick = {
                            clipboardManager.setText(AnnotatedString(address))
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
            IconButton(onClick = {
                onCloseClick()
                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close WalletScreen icon"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onRefreshClick
            ) {
                AnimatedContent(targetState = refreshUiSate,
                    contentAlignment = Alignment.Center,
                    transitionSpec = { (scaleIn() + fadeIn()).togetherWith(fadeOut() + scaleOut()) }) { state ->
                    when (state) {
                        RefreshUiSate.Success -> Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Refresh successful"
                        )

                        is RefreshUiSate.Error -> Icon(
                            imageVector = Icons.Default.Close,
                            modifier = Modifier.rotate(errorIconRotation),
                            contentDescription = "Refresh error"
                        )

                        else -> Icon(
                            imageVector = Icons.Default.Refresh,
                            modifier = Modifier.rotate(refreshIconRotation),
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
            IconButton(
                onClick = onFavoriteClick
            ) {
                Icon(imageVector = favoriteIcon, contentDescription = "Favorite")
            }
        },
    )

}

@Composable
private fun WalletScreenContent(
    tokensList: List<Token>,
    transactionsList: List<Transaction>,
    tabsList: List<WalletScreenTab>,
    selectedTab: WalletScreenTab,
    onTabSelected: (WalletScreenTab) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {

    val pagerState = rememberPagerState { tabsList.size }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(tabsList.indexOf(selectedTab))
    }

    LaunchedEffect(pagerState.targetPage) {
        onTabSelected(tabsList[pagerState.targetPage])
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = tabsList.indexOf(selectedTab),
        ) {
            tabsList.forEachIndexed { index, item ->
                val tabTitle = when (item) {
                    is WalletScreenTab.Portfolio -> item.title
                    is WalletScreenTab.TransactionsHistory -> item.title
                }
                Tab(selected = index == tabsList.indexOf(selectedTab),
                    onClick = { onTabSelected(tabsList[index]) },
                    text = {
                        Text(
                            text = tabTitle
                        )
                    })
            }
        }
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) { pageIndex ->
            when (tabsList[pageIndex]) {
                is WalletScreenTab.Portfolio -> PortfolioContent(
                    tokensList = tokensList, modifier = Modifier.fillMaxWidth()
                )

                is WalletScreenTab.TransactionsHistory -> HistoryContent(
                    modifier = Modifier.fillMaxSize(),
                    transactionsList = transactionsList,
                    onTransactionClick = onTransactionClick
                )
            }

        }
    }
}

@Composable
private fun PortfolioContent(
    tokensList: List<Token>, modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val tokensListState = rememberLazyListState()
    val showUpButton by remember {
        derivedStateOf { tokensListState.firstVisibleItemIndex > 3 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (tokensList.isEmpty()) {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        } else {
            TokenBalancesList(
                tokensList = tokensList, listState = tokensListState, modifier = modifier
            )
        }
        GoUpButton(visible = showUpButton,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            onClick = {
                scope.launch {
                    tokensListState.scrollToItem(0)
                }
            })
    }
}

@Composable
private fun HistoryContent(
    modifier: Modifier = Modifier,
    transactionsList: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    val scope = rememberCoroutineScope()
    val transactionsListState = rememberLazyListState()
    val showUpButton by remember {
        derivedStateOf { transactionsListState.firstVisibleItemIndex > 5 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TransactionsHistoryList(
            transactionsList = transactionsList,
            listState = transactionsListState,
            onTransactionClick = onTransactionClick,
            modifier = modifier
        )

        GoUpButton(visible = showUpButton,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            onClick = {
                scope.launch {
                    transactionsListState.scrollToItem(0)
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveWalletModalBottomSheet(
    modifier: Modifier = Modifier,
    state: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit,
    onSaveWallet: (String?) -> Unit
) {
    var walletName by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = state, modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.save_wallet_bottom_sheet_title),
                style = typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))
            TextFieldWithActionButton(
                title = stringResource(R.string.save_wallet_bottom_sheet_textfield_title),
                text = walletName,
                onTextChange = { walletName = it },
                placeholder = stringResource(R.string.save_wallet_bottom_sheet_textfield_placeholder),
                actionIcon = {
                    Icon(
                        imageVector = Icons.Default.Done, contentDescription = "Save wallet icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                onActionClick = {
                    onSaveWallet(walletName.trim().ifEmpty { null })
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionInfoModalBottomSheet(
    modifier: Modifier = Modifier, transaction: Transaction, onDismiss: () -> Unit
) {
    val optionModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)

    val title = when (transaction.type) {
        TransactionType.RECEIVE, TransactionType.TOKEN_RECEIVE -> stringResource(R.string.transaction_info_bottom_sheet_from)
        TransactionType.SEND, TransactionType.TOKEN_SEND -> stringResource(R.string.transaction_info_bottom_sheet_to)

        else -> ""
    }
    val text = when (transaction.type) {
        TransactionType.RECEIVE, TransactionType.TOKEN_RECEIVE -> transaction.from
        TransactionType.SEND, TransactionType.TOKEN_SEND -> transaction.to

        else -> ""
    }
    val tokenSymbol =
        if (transaction.type != TransactionType.TOKEN_SWAP) transaction.tokenSymbol else ""
    ModalBottomSheet(
        onDismissRequest = onDismiss, modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = transaction.type.typeName, style = typography.titleLarge
            )
            Spacer(Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    tint = colorScheme.outline,
                    modifier = Modifier.size(16.dp),
                    contentDescription = null,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = transaction.timestamp.formatTimestampToDate(true),
                    style = typography.labelMedium,
                    color = colorScheme.outline
                )
            }
            HorizontalDivider(Modifier.padding(top = 8.dp))
            TransactionInfoOption(
                title = stringResource(R.string.transaction_info_bottom_sheet_hash),
                text = transaction.hash,
                modifier = optionModifier
            )
            if (transaction.type != TransactionType.TOKEN_SWAP) {
                TransactionInfoOption(
                    title = title, text = text, modifier = optionModifier
                )
            }
            TransactionInfoOption(
                title = stringResource(R.string.transaction_info_bottom_sheet_amount),
                text = "${transaction.amount} $tokenSymbol",
                modifier = optionModifier
            )
            if (transaction.usdValue != null) {
                TransactionInfoOption(
                    title = stringResource(R.string.transaction_info_bottom_sheet_usd_value),
                    text = "${transaction.usdValue}$",
                    modifier = optionModifier
                )
            }
        }
    }
}

@Composable
fun TransactionInfoOption(
    modifier: Modifier = Modifier, title: String, text: String
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = title, style = typography.bodyMedium, color = colorScheme.outline
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text, style = typography.bodyMedium, modifier = Modifier.weight(1F)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun SaveWalletBottomSheetPreview() {
    WalletWatcherTheme {
        val state = rememberStandardBottomSheetState()
        SaveWalletModalBottomSheet(state = state, onSaveWallet = {}, onDismiss = {})
    }
}