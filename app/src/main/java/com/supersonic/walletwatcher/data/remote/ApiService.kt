package com.supersonic.walletwatcher.data.remote

import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.remote.models.TokenBalancesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class ApiService @Inject constructor(private val client: HttpClient) {

    suspend fun getWalletTokenBalances(walletAddress: String) : ResultWrapper<List<TokenBalance>> {
       return safeApiCall {
            val endPoint = "wallets/$walletAddress/tokens"

            client.get(endPoint){
                parameter("chain","eth")
                parameter("exclude_spam", true)
            }.body<TokenBalancesResponse>().result
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultWrapper<T> {
        return try {
            ResultWrapper.Success(apiCall())
        } catch (e: ClientRequestException) { // HTTP 4xx Errors
            ResultWrapper.Error("Wallet not found. Please check the address.")

        } catch (e: ServerResponseException) { // HTTP 5xx Errors
            ResultWrapper.Error("Server is currently unavailable. Try again later.")

        } catch (e: IOException) { // Network Issues
            ResultWrapper.Error("Network error. Please check your internet connection.")

        } catch (e: SerializationException) { // JSON Parsing Errors
            ResultWrapper.Error("Data error. Unable to process response.")

        } catch (e: Exception) { // Fallback for unexpected errors
            ResultWrapper.Error("Unexpected error. ${e.localizedMessage.orEmpty()}")
        }
    }
}