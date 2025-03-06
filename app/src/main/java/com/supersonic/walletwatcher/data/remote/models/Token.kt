package com.supersonic.walletwatcher.data.remote.models

import com.supersonic.walletwatcher.utils.PriceSerializer
import com.supersonic.walletwatcher.utils.formatFromWei
import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val address: String,
    val symbol: String,
    val name: String,
    val logo: String? = null,
    val balance: String,
    val price: Float,
)

@Serializable
data class TokenRaw(
    val address: String,
    val ETH: EthToken? = null,
    val countTxs: Int? = null,
    val tokens: List<TokenBalance> = emptyList()
)

@Serializable
data class EthToken(
    val price: Price,
    val balance: Float,
    val rawBalance: String,
    val totalIn: Double? = null,
    val totalOut: Double? = null
)

@Serializable
data class Price(
    val rate: Float,
    val diff: Double? = null,
    val diff7d: Double? = null,
    val diff30d: Double? = null,
    val ts: Long,
    val marketCapUsd: Double? = null,
    val availableSupply: Double? = null,
    val volume24h: Double? = null,
    val volDiff1: Double? = null,
    val volDiff7: Double? = null,
    val volDiff30: Double? = null
)

@Serializable
data class TokenBalance(
    val tokenInfo: TokenInfo,
    val balance: Double,
    val rawBalance: String
)

@Serializable
data class TokenInfo(
    val address: String,
    val decimals: String,
    val lastUpdated: Long,
    val name: String? = null,
    val owner: String? = null,
    @Serializable(with = PriceSerializer::class)
    val price: Price? = null,
    val symbol: String? = null,
    val totalSupply: String,
    val issuancesCount: Int,
    val holdersCount: Int,
    val website: String? = null,
    val image: String? = null,
    val ethTransfersCount: Int? = null
)

fun TokenRaw.toToken(): List<Token>{

    val ethToken = ETH?.let {
        Token(
            address = "",
            symbol = "ETH",
            name = "Ethereum",
            logo = "https://ethplorer.io/images/eth.png",
            balance = ETH.balance.toString(),
            price = (ETH.balance * ETH.price.rate)
        )
    }

    val tokenList = tokens.mapNotNull { token ->
        val info = token.tokenInfo
        // Check for spam conditions
        if (isSpamToken(info)) return@mapNotNull null
        if (info.price == null) return@mapNotNull null
        if (info.totalSupply == "0") return@mapNotNull null


        val decimals = info.decimals.toIntOrNull() ?: 18
        val formattedBalance = token.rawBalance.formatFromWei(decimals)
        val tokenPrice = (formattedBalance * (info.price.rate).toBigDecimal()).toFloat()
        Token(
            address = info.address,
            symbol = info.symbol.orEmpty(),
            name = info.name.orEmpty(),
            logo = "https://ethplorer.io${info.image}",
            balance = formattedBalance.toPlainString(),
            price = if (tokenPrice.isFinite()) tokenPrice else 0F
        )
    }
    return listOfNotNull(ethToken) + tokenList
}

fun isSpamToken(tokenInfo: TokenInfo): Boolean {
    val spamKeywords = listOf("http", ".com", "claim", "gift", "visit", "earn", "free")
    return spamKeywords.any { keyword ->
        tokenInfo.name?.contains(keyword, ignoreCase = true) ?: false  ||
        tokenInfo.symbol?.contains(keyword, ignoreCase = true) ?: false
    }
}

