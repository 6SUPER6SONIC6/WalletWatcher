package com.supersonic.walletwatcher.data.remote.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.ui.graphics.vector.ImageVector
import com.supersonic.walletwatcher.utils.formatBalance
import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistoryResponse(
    val result: List<TransactionRaw>
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
    val transaction_fee: String,
    val timestamp: String,
    val summary: String?,
)

@Serializable
data class TransactionRaw(
    val hash: String,
    val nonce: String,
    val transaction_index: String,
    val from_address: String,
    val from_address_entity: String? = null,
    val from_address_entity_logo: String? = null,
    val from_address_label: String? = null,
    val to_address: String,
    val to_address_entity: String? = null,
    val to_address_entity_logo: String? = null,
    val to_address_label: String? = null,
    val value: String,
    val gas: String,
    val gas_price: String,
    val receipt_cumulative_gas_used: String,
    val receipt_gas_used: String,
    val receipt_contract_address: String? = null,
    val receipt_status: String,
    val block_timestamp: String,
    val block_number: String,
    val block_hash: String,
    val transaction_fee: String,
    val method_label: String? = null,
    val category: String,
    val summary: String? = null,
    val possible_spam: Boolean? = null,
    val erc20_transfers: List<Erc20Transaction> = emptyList(),
    val native_transfers: List<NativeTransaction> = emptyList()
)

@Serializable
data class Erc20Transaction(
    val token_name: String,
    val token_symbol: String,
    val token_logo: String? = null,
    val token_decimals: String,
    val from_address: String,
    val from_address_entity: String? = null,
    val from_address_entity_logo: String? = null,
    val from_address_label: String? = null,
    val to_address: String,
    val to_address_entity: String? = null,
    val to_address_entity_logo: String? = null,
    val to_address_label: String? = null,
    val address: String,  // Contract address of the token
    val log_index: Int,
    val value: String,  // Raw value
    val value_formatted: String,  // Formatted value
    val possible_spam: Boolean? = null,
    val verified_contract: Boolean? = null,
    val security_score: Int? = null,
    val direction: String? = null  // "send" or "receive"
)
@Serializable
data class NativeTransaction(
    val from_address: String,
    val from_address_entity: String? = null,
    val from_address_entity_logo: String? = null,
    val from_address_label: String? = null,
    val to_address: String,
    val to_address_entity: String? = null,
    val to_address_entity_logo: String? = null,
    val to_address_label: String? = null,
    val value: String,  // Raw value
    val value_formatted: String,  // Formatted value
    val direction: String? = null,  // "send" or "receive"
    val internal_transaction: Boolean? = null,
    val token_symbol: String? = "ETH",  // Default to ETH if missing
    val token_logo: String? = "https://cdn.moralis.io/eth/0x.png"
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
    TOKEN_SWAP("Swapped", Icons.Default.CurrencyExchange),
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

fun TransactionRaw.toTransaction(): List<Transaction> {
    val transactions = mutableListOf<Transaction>()

    val sentTransfers = erc20_transfers.filter { it.direction == "send" }
    val receivedTransfers = erc20_transfers.filter { it.direction == "receive" }

    if (sentTransfers.isNotEmpty() && receivedTransfers.isNotEmpty()){
        transactions.add(
            Transaction(
                hash = this.hash,
                type = TransactionType.TOKEN_SWAP,
                from = this.from_address,
                to = this.to_address,
                amount = "${sentTransfers.first().value_formatted.formatBalance()} ${sentTransfers.first().token_symbol} -> ${receivedTransfers.first().value_formatted.formatBalance()} ${receivedTransfers.first().token_symbol}",
                tokenSymbol = null,
                tokenName = null,
                tokenLogo = null,
                transaction_fee = this.transaction_fee,
                timestamp = this.block_timestamp,
                summary = this.summary,
            )
        )
    } else {
        erc20_transfers.forEach { transfer ->
            transactions.add(
                Transaction(
                    hash = this.hash,
                    type = TransactionType.fromCategory(this.category),
                    from = transfer.from_address,
                    to = transfer.to_address,
                    amount = transfer.value_formatted,
                    tokenSymbol = transfer.token_symbol,
                    tokenName = transfer.token_name,
                    tokenLogo = transfer.token_logo,
                    transaction_fee = this.transaction_fee,
                    timestamp = this.block_timestamp,
                    summary = this.summary,
                )
            )
        }
    }

    native_transfers.forEach { native ->
        transactions.add(
            Transaction(
                hash = this.hash,
                type = TransactionType.fromCategory(this.category),
                from = native.from_address,
                to = native.to_address,
                amount = native.value_formatted,
                tokenSymbol = native.token_symbol,
                tokenName = null,
                tokenLogo = native.token_logo,
                transaction_fee = this.transaction_fee,
                timestamp = this.block_timestamp,
                summary = this.summary,
            )
        )
    }

    if (transactions.isEmpty()){
        transactions.add(
            Transaction(
                hash = this.hash,
                type = TransactionType.fromCategory(this.category),
                from = this.from_address,
                to = this.to_address,
                amount = this.value,
                tokenSymbol = null,
                tokenName = null,
                tokenLogo = null,
                transaction_fee = this.transaction_fee,
                timestamp = this.block_timestamp,
                summary = this.summary,
            )
        )
    }

    return transactions
}
