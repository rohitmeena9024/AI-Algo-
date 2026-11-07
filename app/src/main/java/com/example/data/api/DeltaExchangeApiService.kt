package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class DeltaCandleItem(
    @Json(name = "time") val time: Long?,
    @Json(name = "open") val open: Double?,
    @Json(name = "high") val high: Double?,
    @Json(name = "low") val low: Double?,
    @Json(name = "close") val close: Double?,
    @Json(name = "volume") val volume: Double?
)

@JsonClass(generateAdapter = true)
data class DeltaCandlesResponse(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "result") val result: List<DeltaCandleItem>?
)

@JsonClass(generateAdapter = true)
data class DeltaTickerItem(
    @Json(name = "symbol") val symbol: String?,
    @Json(name = "close") val close: Double?,
    @Json(name = "open") val open: Double?,
    @Json(name = "high") val high: Double?,
    @Json(name = "low") val low: Double?,
    @Json(name = "volume") val volume: Double?,
    @Json(name = "turnover") val turnover: Double?,
    @Json(name = "mark_price") val markPrice: String?,
    @Json(name = "spot_price") val spotPrice: String?
)

@JsonClass(generateAdapter = true)
data class DeltaTickersResponse(
    @Json(name = "success") val success: Boolean?,
    @Json(name = "result") val result: List<DeltaTickerItem>?
)

interface DeltaExchangeApiService {

    @GET("v2/tickers")
    suspend fun getTickers(): DeltaTickersResponse

    @GET("v2/history/candles")
    suspend fun getHistoryCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("start") start: Long,
        @Query("end") end: Long
    ): DeltaCandlesResponse

    companion object {
        const val BASE_URL = "https://api.delta.exchange/"
        const val INDIA_URL = "https://india.delta.exchange/"

        fun create(baseUrl: String = BASE_URL, apiKey: String? = null): DeltaExchangeApiService {
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
                    val reqBuilder = original.newBuilder()
                        .header("api-key", apiKey)
                    chain.proceed(reqBuilder.build())
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
                .create(DeltaExchangeApiService::class.java)
        }
    }
}
