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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.Transaction
import com.supersonic.walletwatcher.ui.components.GoUpButton
import com.supersonic.walletwatcher.ui.components.TokenBalancesList
import com.supersonic.walletwatcher.ui.components.TransactionsHistoryList
import com.supersonic.walletwatcher.utils.abbreviate
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    ) {

    val walletUiState by viewModel.walletUiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(walletUiState.refreshUiSate) {
        when(walletUiState.refreshUiSate){
            RefreshUiSate.Success -> view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            is RefreshUiSate.Error -> {
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                Toast.makeText(context, (walletUiState.refreshUiSate as RefreshUiSate.Error).message, Toast.LENGTH_LONG).show()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            WalletTopBar(
                title = walletUiState.walletAddress,
                refreshUiSate = walletUiState.refreshUiSate,
                onCloseClick = onNavigateBack,
                onRefreshClick = viewModel::refreshWallet,
                onFavoriteClick = { TODO() }
            )
        },
        content = {
            WalletScreenContent(
                tokensList = walletUiState.tokensList,
                transactionsList = walletUiState.transactionHistoryList,
                tabsList = walletUiState.tabs,
                selectedTabIndex = walletUiState.selectedTabIndex,
                onTabSelected = viewModel::onTabSelected,
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            )
        },
        modifier = modifier
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WalletTopBar(
    title: String,
    refreshUiSate: RefreshUiSate,
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
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        )
    )
    val errorIconRotation by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(230),
            repeatMode = RepeatMode.Reverse
        )
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close WalletScreen icon"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onRefreshClick
                ) {
                    AnimatedContent(
                        targetState = refreshUiSate,
                        contentAlignment = Alignment.Center,
                        transitionSpec = { (scaleIn() + fadeIn()).togetherWith(fadeOut() + scaleOut())}
                    ){ state ->
                        when(state){
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
                                contentDescription = "Refresh")
                        }
                    }
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
    tokensList: List<Token>,
    transactionsList: List<Transaction>,
    tabsList: List<WalletTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val pagerState = rememberPagerState { tabsList.size }

    LaunchedEffect(selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
    }

    LaunchedEffect(pagerState.targetPage) {
        onTabSelected(pagerState.targetPage)
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabsList.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {onTabSelected(index)},
                    text = {
                        Text(
                            text = item.title
                        )
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) { index ->
            when(index){
                0 -> PortfolioContent(tokensList = tokensList, modifier = Modifier.fillMaxWidth())
                1 -> HistoryContent(transactionsList = transactionsList, modifier = Modifier.fillMaxSize())
            }

        }
    }
}

@Composable
private fun PortfolioContent(
    tokensList: List<Token>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val tokensListState = rememberLazyListState()
    val showUpButton by remember {
        derivedStateOf { tokensListState.firstVisibleItemIndex > 3 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TokenBalancesList(
            tokensList = tokensList,
            listState = tokensListState,
            modifier = modifier
        )

        GoUpButton(
            visible = showUpButton,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            onClick = {
                scope.launch {
                    tokensListState.scrollToItem(0)
                }
            }
        )
    }
}

@Composable
private fun HistoryContent(
    transactionsList: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val transactionsListState = rememberLazyListState()
    val showUpButton by remember {
        derivedStateOf { transactionsListState.firstVisibleItemIndex > 5 }
    }

    Box(modifier = Modifier.fillMaxSize()){
        TransactionsHistoryList(
            transactionsList = transactionsList,
            listState = transactionsListState,
            modifier = modifier
        )

        GoUpButton(
            visible = showUpButton,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(8.dp),
            onClick = {
                scope.launch {
                    transactionsListState.scrollToItem(0)
                }
            }
        )
    }
}


