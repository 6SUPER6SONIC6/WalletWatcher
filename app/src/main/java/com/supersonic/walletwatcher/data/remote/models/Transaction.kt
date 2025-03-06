package com.supersonic.walletwatcher.data.remote.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.ui.graphics.vector.ImageVector
import com.supersonic.walletwatcher.utils.TokenInfoSerializer
import com.supersonic.walletwatcher.utils.formatBalance
import com.supersonic.walletwatcher.utils.formatFromWei
import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistoryResponse(
    val operations: List<TransactionRaw>
)

@Serializable
data class Transaction(
    val hash: String,
    val type: TransactionType,
    val from: String,
    val to: String,
    val amount: String,
    val tokenSymbol: String?,
    val tokenName: String?,
    val tokenLogo: String?,
    val usdValue: String?,
    val timestamp: Long,
)

@Serializable
data class TransactionRaw(
    val timestamp: Long,
    val transactionHash: String,
    @Serializable(with = TokenInfoSerializer::class)
    val tokenInfo: TokenInfo? = null,
    val type: String,
    val value: String,
    val priority: Int,
    val from: String? = null,
    val to: String? = null
)

@Serializable
data class EthTransactionRaw(
    val timestamp: Long,
    val hash: String,
    val value: Float,
    val rawValue: String,
    val usdPrice: Float,
    val usdValue: Float,
    val success: Boolean,
    val from: String? = null,
    val to: String? = null
)

private val sendIcon = Icons.AutoMirrored.Filled.CallMade
private val receiveIcon = Icons.AutoMirrored.Filled.CallReceived
enum class TransactionType(val typeName: String, val icon: ImageVector){
    SEND("Sent", sendIcon),
    RECEIVE("Received", receiveIcon),
    NFT_SEND("Sent", sendIcon),
    NFT_RECEIVE("Received", receiveIcon),
    TOKEN_SEND("Sent", sendIcon),
    TOKEN_RECEIVE("Received", receiveIcon),
    DEPOSIT("Deposited", Icons.Default.VerticalAlignBottom),
    WITHDRAW("Withdrawn", Icons.Default.VerticalAlignTop),
    TOKEN_SWAP("Swapped", Icons.Default.SwapHoriz),
    AIRDROP("Airdrop", receiveIcon),
    MINT("Minted" , Icons.Default.Add),
    BURN("Burned", Icons.Default.LocalFireDepartment),
    NFT_PURCHASE("Purchased", Icons.Default.VerticalAlignBottom),
    NFT_SALE("Sold", Icons.Default.VerticalAlignTop),
    BORROW("Borrowed", Icons.Default.CreditCard),
    APPROVE("Approved",Icons.Default.DownloadDone),
    REVOKE("Revoked", Icons.Default.Replay),
    CONTRACT_INTERACTION("Interacted with contract", Icons.Default.AssignmentTurnedIn),
    UNKNOWN("Unknown transaction", Icons.Default.QuestionMark);

    companion object {
        fun fromCategory(category: String?): TransactionType {
            return entries.find {
                it.name.replace("_", " ").equals(category, ignoreCase = true)
            } ?: UNKNOWN
        }
    }
}

fun TransactionRaw.toTransaction(currentWallet: String): Transaction {
    val transactionType = when {
        this.type == "mint" -> TransactionType.MINT
        this.type == "burn" -> TransactionType.BURN
        this.from?.equals(currentWallet, ignoreCase = true) == true -> TransactionType.SEND
        this.to?.equals(currentWallet, ignoreCase = true) == true -> TransactionType.RECEIVE
        else -> TransactionType.UNKNOWN
    }

    val decimals = tokenInfo?.decimals?.toIntOrNull()?.coerceIn(0,18) ?: 18
    return Transaction(
        hash = this.transactionHash,
        type = transactionType,
        from = this.from.orEmpty(),
        to = this.to.orEmpty(),
        amount = this.value.formatFromWei(decimals).toPlainString(),
        tokenSymbol = this.tokenInfo?.symbol,
        tokenName = this.tokenInfo?.name,
        tokenLogo = "https://ethplorer.io${this.tokenInfo?.image}",
        usdValue = null,
        timestamp = this.timestamp
    )
}

fun EthTransactionRaw.toTransaction(currentWallet: String): Transaction {
    val transactionType = when {
        this.from?.equals(currentWallet, ignoreCase = true) == true -> TransactionType.SEND
        this.to?.equals(currentWallet, ignoreCase = true) == true -> TransactionType.RECEIVE
        else -> TransactionType.UNKNOWN
    }
    return Transaction(
        hash = this.hash,
        type = transactionType,
        from = this.from.orEmpty(),
        to = this.to.orEmpty(),
        amount = this.value.toString(),
        tokenSymbol = "ETH",
        tokenName = "Ethereum",
        tokenLogo = "https://etherscan.io/images/ethereum-icon.png",
        usdValue = this.usdValue.toString(),
        timestamp = this.timestamp
    )
}

fun List<TransactionRaw>.detectSwaps(walletAddress: String): List<Transaction> {
    val groupedByHash = this.groupBy { it.transactionHash }

    val swapTransactions = mutableListOf<Transaction>()
    val processedHashes = mutableSetOf<String>()

    for ((hash, transactions) in groupedByHash) {
        // Ensure there are at least two transfers in this transaction
        if (transactions.size < 2) continue

        val sentTx = transactions.find { it.from?.equals(walletAddress, ignoreCase = true) == true }
        val receivedTx = transactions.find { it.to?.equals(walletAddress, ignoreCase = true) == true }

        val sentTokenInfo = sentTx?.tokenInfo
        val receivedTokenInfo = receivedTx?.tokenInfo

        if (sentTx != null && receivedTx != null) {
            // Both sending & receiving tokens exist in this transaction → Swap detected
            if (sentTokenInfo != null && receivedTokenInfo != null) {
                swapTransactions.add(
                    Transaction(
                        hash = hash,
                        type = TransactionType.TOKEN_SWAP,
                        from = sentTx.from.orEmpty(),
                        to = receivedTx.to.orEmpty(),
                        amount = "${sentTx.value.formatFromWei(sentTokenInfo.decimals.toIntOrNull() ?: 18).toPlainString().formatBalance()} ${sentTokenInfo.symbol} ➝ " +
                                "${receivedTx.value.formatFromWei(receivedTokenInfo.decimals.toIntOrNull() ?: 18).toPlainString().formatBalance()} ${receivedTokenInfo.symbol}",
                        tokenSymbol = "${sentTokenInfo.symbol} ➝ ${receivedTokenInfo.symbol}",
                        tokenName = "${sentTx.tokenInfo.name} ➝ ${receivedTokenInfo.name}",
                        tokenLogo = "https://ethplorer.io${sentTx.tokenInfo.image}",
                        timestamp = sentTx.timestamp,
                        usdValue = null
                    )
                )
            }

            // Mark transaction hash as processed
            processedHashes.add(hash)
        }
    }

    // Return swap transactions + other transactions that were NOT swaps
    return swapTransactions + this.filterNot { it.transactionHash in processedHashes }.map { it.toTransaction(walletAddress) }
}

fun isSpamTransaction(transaction: TransactionRaw): Boolean {
    val tokenInfo = transaction.tokenInfo

    if (tokenInfo != null){
        val isSpamName = isSpamToken(transaction.tokenInfo)
        val isZeroSupply = transaction.tokenInfo.totalSupply == "0"
        val isPriceMissing = transaction.tokenInfo.price == null

        return isSpamName || isPriceMissing || isZeroSupply
    } else return true
}
