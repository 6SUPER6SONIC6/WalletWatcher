package com.supersonic.walletwatcher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.utils.formatBalance
import com.supersonic.walletwatcher.utils.formatToCurrency

@Composable
fun TokenBalancesList(
    modifier: Modifier = Modifier,
    tokensList: List<Token>,
    listState: LazyListState = rememberLazyListState()
) {
    val totalUsdValue = tokensList.map { it.price }.sum().formatToCurrency()

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        item {
            Column {
                Text(
                    text = "Total balance",
                    style = typography.titleLarge
                )
                Text(
                    text = totalUsdValue,
                    style = typography.displayMedium
                )
            }
        }
        items(tokensList){ token ->
            TokenBalancesListItem(
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .animateItem()
            )
        }
    }
}

@Composable
private fun TokenBalancesListItem(
    token: Token,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1F)
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
//                border = BorderStroke(1.dp, colorScheme.primary)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(token.logo)
                        .crossfade(120)
                        .build(),
                    filterQuality = FilterQuality.None,
                    contentDescription = "Token Logo",
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = token.symbol,
                    style = typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
//                Spacer(Modifier.height(2.dp))
                Text(
                    text = token.name,
                    style = typography.bodyMedium,
                    color = colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(.8F)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = token.price.formatToCurrency(),
                style = typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
//            Spacer(Modifier.height(2.dp))
            Text(
                text = token.balance.formatBalance(),
                style = typography.bodyMedium,
                color = colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}