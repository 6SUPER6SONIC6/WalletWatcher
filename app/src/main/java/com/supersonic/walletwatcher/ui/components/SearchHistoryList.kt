package com.supersonic.walletwatcher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.supersonic.walletwatcher.R
import com.supersonic.walletwatcher.data.local.entities.SearchHistoryEntity
import com.supersonic.walletwatcher.ui.theme.WalletWatcherTheme
import com.supersonic.walletwatcher.utils.formatMillisToRelativeTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchHistoryList(
    modifier: Modifier = Modifier,
    resentSearches: List<SearchHistoryEntity>,
    onItemClick: (String) -> Unit,
    onItemLongClick: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
         if (resentSearches.isEmpty()){
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            modifier = Modifier.size(92.dp),
                            contentDescription = null
                        )
                        Text(
                            text = stringResource(R.string.recent_searches_empty_text_title),
                            style = typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.recent_searches_empty_text_body),
                            style = typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(resentSearches) { item ->
                        SearchHistoryItem(
                            historyItem = item,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .combinedClickable(
                                    onClick = { onItemClick(item.address) },
                                    onLongClick = { onItemLongClick(item.address) }
                                )
                        )
                    }
                }
            }

    }
}

@Composable
private fun SearchHistoryItem(
    modifier: Modifier,
    historyItem: SearchHistoryEntity,
) {
    var isItemExpanded by remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier

        ) {
            Text(
                text = historyItem.address,
                style = typography.bodyLarge,
                maxLines = if (isItemExpanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1F)
            )
            Spacer(Modifier.width(32.dp))
            Text(
                text = historyItem.searchedAt.formatMillisToRelativeTime(),
                style = typography.bodySmall,
                color = colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

}

@Preview(showBackground = true)
@Composable
private fun SearchHistoryListPreview() {
    val history = listOf(
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
        SearchHistoryEntity("0x112532B200980Ddee8226023bEbBE2E6884C31e2"),
        SearchHistoryEntity("0x95222290DD7278Aa3Ddd389Cc1E1d165CC4BAfe5"),
        SearchHistoryEntity("0xcA8Fa8f0b631EcdB18Cda619C4Fc9d197c8aFfCa"),
        SearchHistoryEntity("0x3b64216AD1a58f61538b4fA1B27327675Ab7ED67"),
        SearchHistoryEntity("0x001858857703f44148076e1B4C66a3E08522587B"),
    )
    WalletWatcherTheme(
        dynamicColor = false
    ) {
        SearchHistoryList(
            modifier = Modifier.fillMaxSize(),
            resentSearches = history,
            onItemClick = {},
            onItemLongClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchHistoryListEmptyPreview() {
    val history = listOf<SearchHistoryEntity>()
    WalletWatcherTheme(
        dynamicColor = false
    ) {
        SearchHistoryList(
            modifier = Modifier.fillMaxSize(),
            resentSearches = history,
            onItemClick = {},
            onItemLongClick = {},
        )
    }
}

