package com.supersonic.walletwatcher.utils

import android.icu.util.Calendar
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Float.formatToCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this).replace(",", " ")
}

fun String.formatFromWei(decimals: Int): BigDecimal {
    val weiBigDecimal = this.toBigDecimal()
    return weiBigDecimal.movePointLeft(decimals)
}

fun String.formatBalance(): String {
    val bigDecimal = BigDecimal(this)
    return when {
        bigDecimal < BigDecimal.ONE -> bigDecimal.setScale(4, RoundingMode.UP).stripTrailingZeros()
            .toPlainString()

        bigDecimal < BigDecimal("5") -> bigDecimal.setScale(3, RoundingMode.DOWN)
            .stripTrailingZeros().toPlainString()

        bigDecimal < BigDecimal("10") -> bigDecimal.setScale(2, RoundingMode.DOWN)
            .stripTrailingZeros().toPlainString()

        bigDecimal < BigDecimal("100") -> bigDecimal.setScale(1, RoundingMode.DOWN)
            .stripTrailingZeros().toPlainString()

        bigDecimal < BigDecimal("5000") -> bigDecimal.setScale(0, RoundingMode.DOWN)
            .stripTrailingZeros().toPlainString()

        bigDecimal < BigDecimal("1000000") -> (bigDecimal / BigDecimal("1000")).setScale(
            1, RoundingMode.DOWN
        ).stripTrailingZeros().toPlainString() + "k"

        bigDecimal < BigDecimal("1000000000") -> (bigDecimal / BigDecimal("1000000")).setScale(
            1, RoundingMode.DOWN
        ).stripTrailingZeros().toPlainString() + "M"

        else -> (bigDecimal / BigDecimal("1000000000")).setScale(1, RoundingMode.DOWN)
            .stripTrailingZeros().toPlainString() + "B"
    }
}

fun String.abbreviateWalletAddress(firs: Int = 6, last: Int = 4): String {
    return if (length > 10) "${take(firs)}...${takeLast(last)}" else this
}

fun Long.formatTimestampToDate(fullDate: Boolean = false): String {
    return if (fullDate) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(this * 1000))
    } else {
        SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(this * 1000))
    }
}

fun Long.formatMillisToRelativeTime(fullDate: Boolean = false): String {
    val now = System.currentTimeMillis()

    val nowCalendar = Calendar.getInstance().apply { timeInMillis = now }
    val searchCalendar =
        Calendar.getInstance().apply { timeInMillis = this@formatMillisToRelativeTime }

    return if (fullDate) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(this))
    } else {
        if (nowCalendar.get(Calendar.YEAR) == searchCalendar.get(Calendar.YEAR) && nowCalendar.get(
                Calendar.DAY_OF_YEAR
            ) == searchCalendar.get(Calendar.DAY_OF_YEAR)
        ) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
        } else {
            SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(this))
        }
    }
}