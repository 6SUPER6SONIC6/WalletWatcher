package com.supersonic.walletwatcher.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun Float.formatToCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this).replace(","," ")
}

fun String.formatBalance(decimals: Int = 4): String {
    return BigDecimal(this)
        .setScale(decimals, RoundingMode.DOWN)
        .toPlainString()
}

fun String.abbreviate(): String {
    return if (length > 10) "${take(6)}...${takeLast(6)}" else this
}