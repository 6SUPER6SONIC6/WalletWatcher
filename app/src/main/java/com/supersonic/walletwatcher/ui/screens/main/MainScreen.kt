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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.remote.models.Token

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigationToWalletScreen:(List<Token>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainUiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current

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

    Scaffold(
        modifier = modifier,
        topBar = { MainTopBar(stringResource(R.string.app_name)) },
        content = { paddingValues ->
            MainScreenContent(
                state = mainUiState,
                onWalletAddress = { viewModel.updateWalletAddress(it) },
                onSearchButtonClick = viewModel::fetchWalletBalance,
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
    onWalletAddress: (String) -> Unit,
    onSearchButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {



    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter wallet address",
            style = typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        WalletAddressInput(
            text = state.walletAddress,
            fetchingUiState = state.fetchingUiState,
            isError = state.validationResult != WalletAddressValidationResult.CORRECT,
            onTextChange = onWalletAddress,
            onSearchClick = onSearchButtonClick,
            onClearTextClick = {onWalletAddress("")},
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedContent(
            targetState = state.validationResult,
            modifier = Modifier.padding(top = 4.dp),
        ) { targetState ->
            val errorMessage = when(targetState){
                WalletAddressValidationResult.EMPTY -> "This field must be filled in"
                WalletAddressValidationResult.INCORRECT -> "Wallet address is invalid"
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
private fun WalletAddressInput(
    text: String,
    fetchingUiState: FetchingUiState,
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
                        text = "0x112532B200980Ddee8226023bEbBE2E6884C31e2",
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
                onClick = {if (isWalletAddressInputEnabled) onSearchClick() },
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonContainerColor),
                contentPadding = PaddingValues(0.dp)
            ) {
                AnimatedContent(
                    targetState = fetchingUiState,
                    contentAlignment = Alignment.Center,
                    transitionSpec = { (scaleIn() + fadeIn()).togetherWith(fadeOut() + scaleOut())}
                ) { state ->
                    when(state){
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
                            imageVector = Icons.Default.Done,  contentDescription = null
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


}