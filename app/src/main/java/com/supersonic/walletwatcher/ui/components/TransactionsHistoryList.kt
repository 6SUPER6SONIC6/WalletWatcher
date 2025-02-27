package com.supersonic.walletwatcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.supersonic.walletwatcher.data.remote.models.Transaction
import com.supersonic.walletwatcher.data.remote.models.TransactionType
import com.supersonic.walletwatcher.utils.abbreviate
import com.supersonic.walletwatcher.utils.formatBalance

@Composable
fun TransactionsHistoryList(
    transactionsList: List<Transaction>,
    modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(transactionsList){ transaction ->
            TransactionItem(transaction, Modifier.animateItem())
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier
) {

    val itemTitle = when(transaction.type){
        TransactionType.RECEIVE, TransactionType.TOKEN_RECEIVE -> transaction.type.typeName
        TransactionType.SEND, TransactionType.TOKEN_SEND -> transaction.type.typeName
        TransactionType.TOKEN_SWAP -> transaction.type.typeName

        else -> transaction.summary
    }
    val itemBody = when(transaction.type){
        TransactionType.RECEIVE, TransactionType.TOKEN_RECEIVE -> "From ${transaction.from.abbreviate()}"
        TransactionType.SEND, TransactionType.TOKEN_SEND -> "To ${transaction.to.abbreviate()}"
        TransactionType.TOKEN_SWAP -> transaction.amount

        else -> null
    }
    val itemAmount = when(transaction.type){
        TransactionType.RECEIVE, TransactionType.TOKEN_RECEIVE -> "+${transaction.amount.formatBalance()} ${transaction.tokenSymbol}"
        TransactionType.SEND, TransactionType.TOKEN_SEND -> "-${transaction.amount.formatBalance()} ${transaction.tokenSymbol}"

        else -> null

    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .defaultMinSize(minHeight = 80.dp)
//            .height(120.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .weight(1F)
                .padding(16.dp)
        ) {
           Row(
               modifier = Modifier
                   .fillMaxSize()
                   .weight(1F),
               verticalAlignment = Alignment.CenterVertically
           ) {
                    Icon(
                        imageVector = transaction.type.icon,
                        modifier = Modifier
                            .background(colorScheme.surface, CircleShape)
                            .padding(8.dp),
                        tint = colorScheme.onSurfaceVariant,
                        contentDescription = "Transaction icon"
                    )

                Spacer(Modifier.width(16.dp))

                Column(
//                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (itemTitle != null) {
                        Text(
                            text = itemTitle,
                            style = typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    if (itemBody != null) {
                        Text(
                            text = itemBody,
                            style = typography.bodyMedium,
                            color = colorScheme.outline
                        )
                    }
                }
            }

            if (itemAmount != null) {
                Text(
                    text = itemAmount,
                    style = typography.bodyMedium,
                )
            }
        }
    }
}