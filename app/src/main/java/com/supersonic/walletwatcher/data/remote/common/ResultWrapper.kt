package com.supersonic.walletwatcher.data.remote.common

sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T): ResultWrapper<T>()
    data class Error(val message: String): ResultWrapper<Nothing>()
}