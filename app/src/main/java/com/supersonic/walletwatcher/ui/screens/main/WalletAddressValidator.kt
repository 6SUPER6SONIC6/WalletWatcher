package com.supersonic.walletwatcher.ui.screens.main

import com.supersonic.walletwatcher.R

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

enum class WalletAddressValidationResult(val errorMessageId: Int){
    EMPTY(R.string.wallet_address_input_error_empty),
    INCORRECT(R.string.wallet_address_input_error_incorrect),
    CORRECT(0)
}