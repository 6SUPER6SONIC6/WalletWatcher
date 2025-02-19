package com.supersonic.walletwatcher.ui.screens.main

class WalletAddressValidator {

    private val regex = Regex("^0x[a-fA-F0-9]{40}$")

    operator fun invoke(walletAddress: String): WalletAddressValidationResult{
        return when{

            walletAddress.isEmpty() -> WalletAddressValidationResult.EMPTY
            !regex.matches(walletAddress) -> WalletAddressValidationResult.INCORRECT

            else -> WalletAddressValidationResult.CORRECT
        }
    }
}

enum class WalletAddressValidationResult{
    EMPTY,
    INCORRECT,
    CORRECT
}