package com.supersonic.walletwatcher.ui.screens.main

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.ui.components.SearchHistoryList

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen:(List<Token>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    LaunchedEffect(mainUiState.fetchingUiState) {
        when(mainUiState.fetchingUiState){
            FetchingUiState.Success -> view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            FetchingUiState.NavigateToWallet -> {
                if (mainUiState.tokensList.isNotEmpty()){
                    onNavigationToWalletScreen(mainUiState.tokensList, mainUiState.walletAddress)
                }
            }
            is FetchingUiState.Error -> {
                val errorMessage = (mainUiState.fetchingUiState as FetchingUiState.Error).message
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                if (errorMessage.isNotEmpty()){
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            else -> {}
        }

    }

    if (showClearHistoryDialog){
        ClearHistoryDialog(
            onConfirm = {
                viewModel.clearSearchHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false}
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { MainTopBar(stringResource(R.string.app_name)) },
        content = { paddingValues ->
            MainScreenContent(
                state = mainUiState,
                onTabSelected = viewModel::onTabSelected,
                onWalletAddress = { viewModel.updateWalletAddress(it) },
                onSearchButtonClick = viewModel::navigateToWallet,
                onClearHistoryClick = { showClearHistoryDialog = true },
                modifier = Modifier
                    .padding(paddingValues)
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
    state: MainUiState,
    onTabSelected: (MainScreenTab) -> Unit,
    onWalletAddress: (String) -> Unit,
    onSearchButtonClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        WalletAddressInput(
            text = state.walletAddress,
            fetchingUiState = state.fetchingUiState,
            validationResult = state.validationResult,
            isError = state.validationResult != WalletAddressValidationResult.CORRECT,
            onTextChange = onWalletAddress,
            onSearchClick = onSearchButtonClick,
            onClearTextClick = { onWalletAddress("") },
            modifier = Modifier.fillMaxWidth()
        )
        HistoryFavoritesContent(
            modifier = Modifier.weight(1F),
            tabsList = state.tabs,
            selectedTab = state.selectedTab,
            historyList = state.searchHistory,
            favoritesList = emptyList(),
            onTabSelected = onTabSelected,
            onClearHistoryClick = onClearHistoryClick,
            onClearFavoritesClick = {},
            onItemClick = {
                onWalletAddress(it)
                onSearchButtonClick()
            }
        )
    }
}

@Composable
private fun WalletAddressInput(
    text: String,
    fetchingUiState: FetchingUiState,
    validationResult: WalletAddressValidationResult,
    isError: Boolean,
    onTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearTextClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val isWalletAddressInputEnabled = when(fetchingUiState){
        FetchingUiState.Idle -> true
        else -> false
    }

    val buttonContainerColor = when(fetchingUiState){
        is FetchingUiState.Error -> colorScheme.error
        else -> ButtonDefaults.buttonColors().containerColor
    }

    val infiniteTransition = rememberInfiniteTransition()
    val errorIconRotation by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(230),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column {
        Text(
            text = stringResource(R.string.wallet_address_input_title),
            style = typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                readOnly = !isWalletAddressInputEnabled,
                //                enabled = isWalletAddressInputEnabled,
                isError = isError,
                placeholder = {
                    Text(
                        text = stringResource(R.string.wallet_address_input_placeholder),
                        color = colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                trailingIcon = {
                    AnimatedVisibility(
                        visible = text.isNotEmpty(),
                        enter = scaleIn() + fadeIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = onClearTextClick,
                            enabled = isWalletAddressInputEnabled
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )

            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { if (isWalletAddressInputEnabled) onSearchClick() },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonContainerColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                AnimatedContent(
                    targetState = fetchingUiState,
                    contentAlignment = Alignment.Center,
                    transitionSpec = { (scaleIn() + fadeIn()).togetherWith(fadeOut() + scaleOut()) }
                ) { state ->
                    when (state) {
                        FetchingUiState.Idle -> Icon(
                            imageVector = Icons.Default.Search, contentDescription = null
                        )

                        FetchingUiState.InProgress -> {
                            CircularProgressIndicator(color = colorScheme.onPrimary)
                        }

                        FetchingUiState.Success -> Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )

                        FetchingUiState.NavigateToWallet -> Icon(
                            imageVector = Icons.Default.Done, contentDescription = null
                        )

                        is FetchingUiState.Error -> Icon(
                            imageVector = Icons.Default.Close,
                            modifier = Modifier.rotate(errorIconRotation),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = validationResult,
            modifier = Modifier.padding(top = 4.dp),
        ) { validationResult ->
            val errorMessage = when (validationResult) {
                WalletAddressValidationResult.EMPTY -> stringResource(validationResult.errorMessageId)
                WalletAddressValidationResult.INCORRECT -> stringResource(validationResult.errorMessageId)
                WalletAddressValidationResult.CORRECT -> ""
            }
            Text(
                text = errorMessage,
                style = typography.labelMedium,
                color = colorScheme.error,
            )
        }
    }
}

@Composable
private fun HistoryFavoritesContent(
    modifier: Modifier = Modifier,
    tabsList: List<MainScreenTab>,
    selectedTab: MainScreenTab,
    historyList: List<SearchHistoryEntity>,
    favoritesList: List<FavoriteWalletEntity>,
    onTabSelected: (MainScreenTab) -> Unit,
    onClearHistoryClick: () -> Unit,
    onClearFavoritesClick:() -> Unit,
    onItemClick: (String) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val view = LocalView.current
    val pagerState = rememberPagerState { tabsList.size }

    val titleId = when(selectedTab){
        is MainScreenTab.Favorites -> selectedTab.titleId
        is MainScreenTab.History -> selectedTab.titleId
    }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(tabsList.indexOf(selectedTab))
    }

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(tabsList[pagerState.currentPage])
    }

    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabsList.forEachIndexed { index, tab ->

                val icon = when(tab){
                    is MainScreenTab.Favorites -> if (pagerState.currentPage == index) tab.selectedIcon else tab.icon
                    is MainScreenTab.History -> if (pagerState.currentPage == index) tab.selectedIcon else tab.icon
                }
                val iconColor  = if (pagerState.currentPage == index) colorScheme.primary else colorScheme.outline


                IconButton(
                    onClick = { onTabSelected(tab) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = iconColor
                    ),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(titleId),
                style = typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { if (selectedTab is MainScreenTab.History) onClearHistoryClick() else onClearFavoritesClick() },
                enabled = if (selectedTab is MainScreenTab.History) historyList.isNotEmpty() else favoritesList.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
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

        ) { pageIndex ->
            when (tabsList[pageIndex]) {
                is MainScreenTab.Favorites -> {
                    Box(Modifier.fillMaxSize()) {
                        Text("Favorites is empty")
                    }
                }

                is MainScreenTab.History -> SearchHistoryList(
                    resentSearches = historyList,
                    modifier = Modifier.fillMaxSize(),
                    onItemClick = onItemClick,
                    onItemLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        clipboardManager.setText(AnnotatedString(it))
                    }
                )
            }
        }
    }
}

@Composable
fun ClearHistoryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(R.string.clear_history_dialog_title)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.clear_history_dialog_text),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = stringResource(R.string.clear_history_dialog_confirm)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.clear_history_dialog_dismiss)
                )
            }
        }

    )
}