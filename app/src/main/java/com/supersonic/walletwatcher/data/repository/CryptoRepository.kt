package com.supersonic.walletwatcher.data.repository

import com.supersonic.walletwatcher.data.remote.models.TokenBalance
import com.supersonic.walletwatcher.data.remote.models.TokenBalancesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class CryptoRepository @Inject constructor(private val client: HttpClient) {

    suspend fun getWalletTokenBalances(walletAddress: String) : List<TokenBalance> {
        val endPoint = "wallets/$walletAddress/tokens"

//        val chains = listOf("eth","polygon","base","bsc")
//        val tokens = mutableListOf<TokenBalance>()
//        return coroutineScope {
//            val requests = chains.map { chain ->
//                async {
//                    try {
//                        val response: TokenBalancesResponse = client.get(endPoint){
//                            parameter("chain", chain)
//                            parameter("exclude_spam", true)
//                        }.body<TokenBalancesResponse>()
//                        tokens.addAll(response.result)
//                    } catch (e:Exception){
//                        TokenBalancesResponse(emptyList()).result
//                    }
//                }
//            }
//            requests.awaitAll()
//            tokens.filter { it.usd_value != null }
//        }


        val response : TokenBalancesResponse = client.get(endPoint){
            parameter("chain","eth")
            parameter("exclude_spam", true)
        }.body()
        return response.result
    }
}