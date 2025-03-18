package com.supersonic.walletwatcher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.local.entities.FavoriteWalletEntity
import com.supersonic.walletwatcher.utils.abbreviateWalletAddress

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedWalletsList(
    modifier: Modifier = Modifier,
    savedWallets: List<FavoriteWalletEntity>,
    onItemClick: (String) -> Unit,
    onItemLongClick: (FavoriteWalletEntity) -> Unit
) {
    if (savedWallets.isNotEmpty()) {
        LazyColumn(
            modifier = modifier
        ) {
            items(savedWallets) { item ->
                SavedWalletItem(
                    savedWallet = item,
                    onMoreClick = { onItemLongClick(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .combinedClickable(onClick = { onItemClick(item.address) },
                            onLongClick = { onItemLongClick(item) })
                        .animateItem()
                )
            }
        }
    } else {
        EmptyContent(
            icon = Icons.Outlined.Bookmarks,
            title = stringResource(R.string.saved_wallets_empty_title),
            text = stringResource(R.string.saved_wallets_empty_text)
        )
    }
}

@Composable
fun SavedWalletItem(
    modifier: Modifier = Modifier, savedWallet: FavoriteWalletEntity, onMoreClick: () -> Unit
) {
    Row(
        modifier = modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = savedWallet.name ?: savedWallet.address.abbreviateWalletAddress(8, 8),
            style = typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F)
        )

        Icon(imageVector = Icons.Default.MoreVert,
            tint = colorScheme.outline,
            contentDescription = null,
            modifier = Modifier
                .clickable { onMoreClick() }
                .padding(4.dp))


    }
}