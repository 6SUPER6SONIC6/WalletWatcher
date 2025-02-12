package com.supersonic.walletwatcher.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class TokenBalance(
    val token_address: String,
    val symbol: String,
    val name: String,
    val logo: String? = null,
    val thumbnail: String? = null,
    val decimals: Int? = null,
    val balance: String,
    val possible_spam: Boolean? = null,
    val verified_contract: Boolean? = null,
    val security_score: Int? = null,
    val balance_formatted: String,
    val usd_price: Float?,
    val usd_price_24hr_percent_change: Float?,
    val usd_price_24hr_usd_change: Float?,
    val usd_value: Float?,
    val usd_value_24hr_usd_change: Float?,
    val native_token: Boolean?,
    val portfolio_percentage: Float?
)

@Serializable
data class TokenBalancesResponse(
    val result: List<TokenBalance>
)