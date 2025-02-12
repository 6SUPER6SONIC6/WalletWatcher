package com.supersonic.walletwatcher.di

import com.supersonic.walletwatcher.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KtorModule {

    private const val API_KEY = BuildConfig.MORALIS_API_KEY
    private const val BASE_URL = "https://deep-index.moralis.io/api/v2.2/"


    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true }
                )
            }
            install(DefaultRequest){
                url(BASE_URL)
                headers.append("Accept", "application/json")
                headers.append("ContentType", "text/html; charset=utf-8")
                headers.append("X-API-Key", API_KEY)
            }
        }
    }
}