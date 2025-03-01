package com.supersonic.walletwatcher.data.remote

import android.util.Log
import com.supersonic.walletwatcher.BuildConfig
import com.supersonic.walletwatcher.data.remote.common.ResultWrapper
import com.supersonic.walletwatcher.data.remote.models.EthTransactionRaw
import com.supersonic.walletwatcher.data.remote.models.Token
import com.supersonic.walletwatcher.data.remote.models.TokenRaw
import com.supersonic.walletwatcher.data.remote.models.Transaction
import com.supersonic.walletwatcher.data.remote.models.TransactionHistoryResponse
import com.supersonic.walletwatcher.data.remote.models.TransactionRaw
import com.supersonic.walletwatcher.data.remote.models.detectSwaps
import com.supersonic.walletwatcher.data.remote.models.isSpamTransaction
import com.supersonic.walletwatcher.data.remote.models.toToken
import com.supersonic.walletwatcher.data.remote.models.toTransaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject


private const val API_KEY = BuildConfig.ETHPLORER_API_KEY

class ApiService @Inject constructor(private val client: HttpClient) {


    suspend fun getWalletTokenBalances(walletAddress: String) : ResultWrapper<List<Token>> {
       return safeApiCall {
           val endPoint = "/getAddressInfo/$walletAddress"
           val response: TokenRaw = client.get(endPoint){
                parameter("apiKey", API_KEY)
            }.body()

           response.toToken()
        }
    }

    suspend fun getWalletTransactionHistory(walletAddress: String) : ResultWrapper<List<Transaction>> {
        return safeApiCall {
            val tokenTransactions = fetchTokenTransactions(walletAddress).detectSwaps(walletAddress)
            val ethTransactions = fetchEthTransactions(walletAddress)

            (tokenTransactions + ethTransactions).sortedByDescending { it.timestamp }
        }
    }

    private suspend fun fetchTokenTransactions(walletAddress: String): List<TransactionRaw>{
        val endPoint = "/getAddressHistory/$walletAddress"
        val response: TransactionHistoryResponse = client.get(endPoint) {
            parameter("apiKey", API_KEY)
            parameter("limit", 1000)
        }.body()

        return response.operations
            .filter { it.tokenInfo != null }
            .filterNot { isSpamTransaction(it) }

    }

    private suspend fun fetchEthTransactions(walletAddress: String): List<Transaction>{
        val endPoint = "/getAddressTransactions/$walletAddress"
        val response: List<EthTransactionRaw> = client.get(endPoint){
            parameter("apiKey", API_KEY)
            parameter("limit", 1000)
        }.body()

        return response.map { it.toTransaction(walletAddress) }
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
            Log.e("Api Call", e.localizedMessage.orEmpty(), e)
            ResultWrapper.Error("Unexpected error. Try again.")
        }
    }
}