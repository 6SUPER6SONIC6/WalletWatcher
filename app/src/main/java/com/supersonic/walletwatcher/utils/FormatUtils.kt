package com.supersonic.walletwatcher.utils

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun Float.formatToCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this).replace(","," ")
}

fun String.formatFromWei(decimals: Int): BigDecimal {
    val weiBigDecimal = this.toBigDecimal()
    return weiBigDecimal.movePointLeft(decimals)
}

fun String.formatBalance(): String {
    val bigDecimal = BigDecimal(this)
    return when {
        bigDecimal < BigDecimal("0") -> bigDecimal.setScale(3, RoundingMode.UP).toPlainString()
        bigDecimal < BigDecimal("5") -> bigDecimal.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        bigDecimal < BigDecimal("10") -> bigDecimal.setScale(2, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        bigDecimal < BigDecimal("100") -> bigDecimal.setScale(1, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        else -> bigDecimal.setScale(0, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
    }
}

fun String.abbreviate(): String {
    return if (length > 10) "${take(6)}...${takeLast(4)}" else this
}