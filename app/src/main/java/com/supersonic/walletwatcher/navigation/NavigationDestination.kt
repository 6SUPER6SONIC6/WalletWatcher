package com.supersonic.walletwatcher.navigation

import kotlinx.serialization.Serializable

@Serializable
object MainScreen

@Serializable
data class WalletScreen(
    val walletAddress: String
)