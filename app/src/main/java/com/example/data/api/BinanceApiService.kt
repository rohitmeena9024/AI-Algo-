package com.example.data.api

import com.example.data.model.Ticker24hResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface BinanceApiService {

    @GET("api/v3/ticker/24hr")
    suspend fun get24hTicker(
        @Query("symbol") symbol: String
    ): Ticker24hResponse

    @GET("api/v3/ticker/24hr")
    suspend fun getAll24hTickers(): List<Ticker24hResponse>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>

    companion object {
        const val BASE_URL = "https://api.binance.us/"
        const val FALLBACK_URL = "https://api.binance.com/"

        fun create(baseUrl: String = BASE_URL, apiKey: String? = null): BinanceApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)

            if (!apiKey.isNullOrBlank()) {
                clientBuilder.addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("X-MBX-APIKEY", apiKey)
                        .build()
                    chain.proceed(request)
                }
            }

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(BinanceApiService::class.java)
        }
    }
}
