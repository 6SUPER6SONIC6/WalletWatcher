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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.ui.components.Dialog
import com.supersonic.walletwatcher.ui.components.IconTextButton
import com.supersonic.walletwatcher.ui.components.SavedWalletsList
import com.supersonic.walletwatcher.ui.components.SearchHistoryList
import com.supersonic.walletwatcher.ui.components.TextFieldWithActionButton
import com.supersonic.walletwatcher.utils.abbreviateWalletAddress
import com.supersonic.walletwatcher.utils.formatMillisToRelativeTime

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen: (List<Token>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val fetchingUiState = mainUiState.fetchingState
    val bottomSheetState = mainUiState.bottomSheetState
    val dialogState = mainUiState.dialogState

    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current

    DisposableEffect(Unit) {
        onDispose { viewModel.resetState() }
    }

    LaunchedEffect(fetchingUiState) {
        when (fetchingUiState) {
            FetchingUiState.Success -> view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
            FetchingUiState.NavigateToWallet -> {
                if (mainUiState.tokensList.isNotEmpty()) {
                    onNavigationToWalletScreen(mainUiState.tokensList, mainUiState.walletAddress)
                }
            }

            is FetchingUiState.Error -> {
                val errorMessage = fetchingUiState.message
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            else -> {}
        }

    }

    when (bottomSheetState) {
        is MainBottomSheetUiState.ShowFavoriteBottomSheet -> SavedWalletsBottomSheet(favoriteWallet = bottomSheetState.wallet,
            onNavigateToWallet = {
                viewModel.updateWalletAddress(bottomSheetState.wallet.address)
                viewModel.navigateToWallet()
                viewModel.dismissBottomSheet()
            },
            onRenameWallet = viewModel::renameFavoriteWallet,
            onCopyAddress = { clipboardManager.setText(AnnotatedString(bottomSheetState.wallet.address)) },
            onRemoveWallet = { viewModel.showRemoveSavedDialog(bottomSheetState.wallet) },
            onDismiss = viewModel::dismissBottomSheet
        )

        is MainBottomSheetUiState.ShowHistoryBottomSheet -> HistoryBottomSheet(
            historyEntity = bottomSheetState.wallet,
            isFavorite = viewModel.isFavorite(bottomSheetState.wallet.address),
            onNavigateToWallet = {
                viewModel.updateWalletAddress(bottomSheetState.wallet.address)
                viewModel.navigateToWallet()
                viewModel.dismissBottomSheet()
            },
            onSaveWallet = {
                viewModel.addFavorite(it)
                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                Toast.makeText(
                    context, context.getString(R.string.toast_wallet_saved), Toast.LENGTH_SHORT
                ).show()
            },
            onCopyAddress = { clipboardManager.setText(AnnotatedString(bottomSheetState.wallet.address)) },
            onRemoveSearch = {
                viewModel.dismissBottomSheet()
                viewModel.removeHistoryWallet(it)
                view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_removed_from_resent_searches),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = viewModel::dismissBottomSheet
        )

        else -> {}
    }

    when (dialogState) {
        MainDialogUiState.ShowClearHistoryDialog -> Dialog(
            title = stringResource(R.string.clear_history_dialog_title),
            text = stringResource(R.string.clear_history_dialog_text),
            confirmButtonText = stringResource(R.string.clear_history_dialog_confirm),
            dismissButtonText = stringResource(R.string.clear_history_dialog_dismiss),
            onConfirm = {
                viewModel.clearSearchHistory()
                viewModel.dismissDialog()
            },
            onDismiss = viewModel::dismissDialog
        )

        MainDialogUiState.ShowClearSavedDialog -> Dialog(
            title = stringResource(R.string.clear_saved_wallets_dialog_title),
            text = stringResource(R.string.clear_saved_wallets_dialog_text),
            confirmButtonText = stringResource(R.string.clear_history_dialog_confirm),
            dismissButtonText = stringResource(R.string.clear_history_dialog_dismiss),
            onConfirm = {
                viewModel.clearSavedWallets()
                viewModel.dismissDialog()
            },
            onDismiss = viewModel::dismissDialog
        )

        is MainDialogUiState.ShowRemoveSavedDialog -> Dialog(
            title = stringResource(R.string.remove_saved_wallet_dialog_title),
            text = stringResource(
                R.string.remove_saved_wallet_dialog_text,
                dialogState.favoriteWalletEntity.name
                    ?: dialogState.favoriteWalletEntity.address.abbreviateWalletAddress()
            ),
            confirmButtonText = stringResource(R.string.remove_saved_wallet_dialog_confirm),
            dismissButtonText = stringResource(R.string.clear_history_dialog_dismiss),
            onConfirm = {
                viewModel.removeFavorite(dialogState.favoriteWalletEntity.address)
                viewModel.dismissDialog()
                viewModel.dismissBottomSheet()
            },
            onDismiss = viewModel::dismissDialog
        )

        else -> {}
    }

    Scaffold(modifier = modifier,
        topBar = { MainTopBar(stringResource(R.string.app_name)) },
        content = { paddingValues ->
            MainScreenContent(
                state = mainUiState,
                onTabSelected = viewModel::onTabSelected,
                onWalletAddress = { viewModel.updateWalletAddress(it) },
                onSearchButtonClick = viewModel::navigateToWallet,
                onClearHistoryClick = viewModel::showClearHistoryDialog,
                onClearSavedClick = viewModel::showClearSavedDialog,
                onFavoriteItemLongClick = viewModel::showFavoriteWalletBottomSheet,
                onHistoryItemLongClick = viewModel::showHistoryBottomSheet,
                modifier = Modifier.padding(paddingValues)
            )
        })

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String, modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) }, modifier = modifier
    )
}

@Composable
private fun MainScreenContent(
    state: MainUiState,
    onTabSelected: (MainScreenTab) -> Unit,
    onWalletAddress: (String) -> Unit,
    onSearchButtonClick: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onClearSavedClick: () -> Unit,
    onFavoriteItemLongClick: (FavoriteWalletEntity) -> Unit,
    onHistoryItemLongClick: (SearchHistoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp), verticalArrangement = Arrangement.Center
    ) {
        WalletAddressInput(
            text = state.walletAddress,
            fetchingUiState = state.fetchingState,
            validationResult = state.validationResult,
            isError = state.validationResult != WalletAddressValidationResult.CORRECT,
            onTextChange = onWalletAddress,
            onSearchClick = onSearchButtonClick,
            modifier = Modifier.fillMaxWidth()
        )
        HistoryFavoritesContent(modifier = Modifier.weight(1F),
            tabsList = state.tabs,
            selectedTab = state.selectedTab,
            historyList = state.searchHistory,
            favoritesList = state.favoriteWallets,
            onTabSelected = onTabSelected,
            onClearHistoryClick = onClearHistoryClick,
            onClearSavedClick = onClearSavedClick,
            onFavoriteItemLongClick = onFavoriteItemLongClick,
            onHistoryItemLongClick = onHistoryItemLongClick,
            onItemClick = {
                onWalletAddress(it)
                onSearchButtonClick()
            })
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
    modifier: Modifier = Modifier
) {
    val isWalletAddressInputEnabled = fetchingUiState is FetchingUiState.Idle
    val buttonContainerColor = when (fetchingUiState) {
        is FetchingUiState.Error -> colorScheme.error
        else -> ButtonDefaults.buttonColors().containerColor
    }
    val infiniteTransition = rememberInfiniteTransition()
    val errorIconRotation by infiniteTransition.animateFloat(
        initialValue = -30f, targetValue = 30f, animationSpec = infiniteRepeatable(
            animation = tween(230), repeatMode = RepeatMode.Reverse
        )
    )
    TextFieldWithActionButton(modifier = modifier,
        title = stringResource(R.string.wallet_address_input_title),
        text = text,
        onTextChange = onTextChange,
        placeholder = stringResource(R.string.wallet_address_input_placeholder),
        isError = isError,
        actionContainerColor = buttonContainerColor,
        imeAction = ImeAction.Search,
        keyboardType = KeyboardType.Ascii,
        errorMessage = {
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
        },
        actionIcon = {
            AnimatedContent(targetState = fetchingUiState,
                contentAlignment = Alignment.Center,
                transitionSpec = { (scaleIn() + fadeIn()).togetherWith(fadeOut() + scaleOut()) }) { state ->
                when (state) {
                    FetchingUiState.Idle -> Icon(
                        imageVector = Icons.Default.Search, contentDescription = null
                    )

                    FetchingUiState.InProgress -> {
                        CircularProgressIndicator(color = colorScheme.onPrimary)
                    }

                    FetchingUiState.Success -> Icon(
                        imageVector = Icons.Default.Done, contentDescription = null
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
        },
        onActionClick = { if (isWalletAddressInputEnabled) onSearchClick() })
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
    onClearSavedClick: () -> Unit,
    onFavoriteItemLongClick: (FavoriteWalletEntity) -> Unit,
    onHistoryItemLongClick: (SearchHistoryEntity) -> Unit,
    onItemClick: (String) -> Unit,
) {
    val view = LocalView.current
    val pagerState = rememberPagerState { tabsList.size }

    val titleId = when (selectedTab) {
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

                val icon = when (tab) {
                    is MainScreenTab.Favorites -> if (pagerState.currentPage == index) tab.selectedIcon else tab.icon
                    is MainScreenTab.History -> if (pagerState.currentPage == index) tab.selectedIcon else tab.icon
                }
                val iconColor =
                    if (pagerState.currentPage == index) colorScheme.primary else colorScheme.outline


                IconButton(
                    onClick = { onTabSelected(tab) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = iconColor
                    ),
                ) {
                    Icon(
                        imageVector = icon, contentDescription = null
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
                onClick = { if (selectedTab is MainScreenTab.History) onClearHistoryClick() else onClearSavedClick() },
                enabled = if (selectedTab is MainScreenTab.History) historyList.isNotEmpty() else favoritesList.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete, contentDescription = null
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
                is MainScreenTab.History -> SearchHistoryList(resentSearches = historyList,
                    modifier = Modifier.fillMaxSize(),
                    onItemClick = onItemClick,
                    onItemLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                        onHistoryItemLongClick(it)
                    })

                is MainScreenTab.Favorites -> SavedWalletsList(modifier = Modifier.fillMaxSize(),
                    savedWallets = favoritesList,
                    onItemClick = onItemClick,
                    onItemLongClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.CONFIRM)
                        onFavoriteItemLongClick(it)
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    modifier: Modifier = Modifier,
    historyEntity: SearchHistoryEntity,
    isFavorite: Boolean,
    onNavigateToWallet: () -> Unit,
    onSaveWallet: (FavoriteWalletEntity) -> Unit,
    onCopyAddress: () -> Unit,
    onRemoveSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val optionModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)

    var isSaveWallet by remember { mutableStateOf(false) }
    var walletName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = historyEntity.address, style = typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    modifier = Modifier.size(16.dp),
                    tint = colorScheme.outline,
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = historyEntity.searchedAt.formatMillisToRelativeTime(true),
                    style = typography.labelMedium,
                    color = colorScheme.outline
                )
            }
            HorizontalDivider(Modifier.padding(top = 8.dp))
            IconTextButton(
                modifier = optionModifier,
                text = stringResource(R.string.saved_wallets_bottom_sheet_option_go_to_wallet),
                icon = Icons.AutoMirrored.Default.ArrowForward,
                onClick = onNavigateToWallet
            )
            IconTextButton(
                modifier = optionModifier,
                text = stringResource(R.string.saved_wallets_bottom_sheet_option_copy_wallet_address),
                icon = Icons.Default.ContentCopy,
                onClick = onCopyAddress
            )
            AnimatedVisibility(!isFavorite) {
                NameInputBottomSheetSection(modifier = optionModifier,
                    isNameInput = isSaveWallet,
                    sectionTitle = stringResource(R.string.history_bottom_sheet_option_save_wallet),
                    sectionIcon = Icons.Default.BookmarkBorder,
                    textFieldTitle = stringResource(R.string.save_wallet_bottom_sheet_textfield_title),
                    name = walletName,
                    onNameChange = { walletName = it },
                    onConfirm = {
                        onSaveWallet(
                            FavoriteWalletEntity(address = historyEntity.address,
                                name = walletName.trim().ifEmpty { null })
                        )
                        isSaveWallet = false
                    },
                    onToggleNameInput = { isSaveWallet = !isSaveWallet })
            }
            IconTextButton(modifier = optionModifier,
                text = stringResource(R.string.history_bottom_sheet_option_remove_from_recent),
                icon = Icons.Default.Close,
                isDanger = true,
                onClick = { onRemoveSearch(historyEntity.address) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedWalletsBottomSheet(
    modifier: Modifier = Modifier,
    favoriteWallet: FavoriteWalletEntity,
    onNavigateToWallet: () -> Unit,
    onRenameWallet: (FavoriteWalletEntity) -> Unit,
    onCopyAddress: () -> Unit,
    onRemoveWallet: () -> Unit,
    onDismiss: () -> Unit
) {
    val optionModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)

    var isRenameWallet by remember { mutableStateOf(false) }
    var walletName by remember { mutableStateOf(favoriteWallet.name ?: "") }

    var walletDisplayName by remember { mutableStateOf(favoriteWallet.name) }

    val onRenameConfirm = {
        val newName = walletName.trim().ifEmpty { null }
        isRenameWallet = false
        onRenameWallet(favoriteWallet.copy(name = newName))
        walletDisplayName = newName
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TitleSavedBottomSheetSection(
                walletName = walletDisplayName, walletAddress = favoriteWallet.address
            )
            HorizontalDivider(Modifier.padding(top = 8.dp))
            IconTextButton(
                modifier = optionModifier,
                text = stringResource(R.string.saved_wallets_bottom_sheet_option_go_to_wallet),
                icon = Icons.AutoMirrored.Default.ArrowForward,
                onClick = onNavigateToWallet
            )
            IconTextButton(
                modifier = optionModifier,
                text = stringResource(R.string.saved_wallets_bottom_sheet_option_copy_wallet_address),
                icon = Icons.Default.ContentCopy,
                onClick = onCopyAddress
            )
            NameInputBottomSheetSection(
                isNameInput = isRenameWallet,
                sectionTitle = stringResource(R.string.saved_wallets_bottom_sheet_option_change_wallet_name),
                sectionIcon = Icons.Default.DriveFileRenameOutline,
                textFieldTitle = stringResource(R.string.saved_wallets_bottom_sheet_textfield_title),
                name = walletName,
                onNameChange = { walletName = it },
                onConfirm = onRenameConfirm,
                onToggleNameInput = { isRenameWallet = !isRenameWallet },
                modifier = optionModifier,
            )
            IconTextButton(
                modifier = optionModifier,
                text = stringResource(R.string.saved_wallets_bottom_sheet_option_remove_from_saved),
                icon = Icons.Default.BookmarkRemove,
                isDanger = true,
                onClick = onRemoveWallet
            )
        }
    }
}

@Composable
private fun TitleSavedBottomSheetSection(
    modifier: Modifier = Modifier, walletName: String?, walletAddress: String
) {
    AnimatedContent(
        targetState = walletName, modifier = modifier
    ) { name ->
        Column {
            Text(
                text = name ?: walletAddress,
                style = if (name == null) typography.titleMedium else typography.titleLarge,
                overflow = TextOverflow.Ellipsis
            )
            if (name != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = walletAddress,
                    style = typography.labelMedium,
                    color = colorScheme.outline,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NameInputBottomSheetSection(
    modifier: Modifier = Modifier,
    isNameInput: Boolean,
    sectionTitle: String,
    sectionIcon: ImageVector,
    textFieldTitle: String,
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onToggleNameInput: () -> Unit,
) {
    AnimatedContent(isNameInput) { state ->
        if (!state) {
            IconTextButton(
                modifier = modifier,
                text = sectionTitle,
                icon = sectionIcon,
                onClick = onToggleNameInput
            )
        } else {
            TextFieldWithActionButton(
                text = name,
                onTextChange = onNameChange,
                title = textFieldTitle,
                modifier = modifier,
                actionIcon = { Icon(Icons.Default.Done, contentDescription = null) },
                onActionClick = onConfirm
            )
        }
    }
}