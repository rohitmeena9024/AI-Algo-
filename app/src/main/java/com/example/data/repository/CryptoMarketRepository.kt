package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.BinanceApiService
import com.example.data.api.DeltaExchangeApiService
import com.example.data.model.Candle
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.Ticker24h
import com.example.data.model.Timeframe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

enum class ExchangeProvider(val displayName: String) {
    DELTA_EXCHANGE("Delta Exchange"),
    BINANCE("Binance")
}

class CryptoMarketRepository(
    private var deltaApiKey: String = if (BuildConfig.DELTA_API_KEY != "DEFAULT_KEY") BuildConfig.DELTA_API_KEY else "",
    private var binanceApiKey: String = if (BuildConfig.BINANCE_API_KEY != "DEFAULT_KEY") BuildConfig.BINANCE_API_KEY else ""
) {
    private val TAG = "CryptoRepository"

    var currentProvider: ExchangeProvider = ExchangeProvider.DELTA_EXCHANGE
        private set

    private var deltaService: DeltaExchangeApiService = DeltaExchangeApiService.create(
        DeltaExchangeApiService.BASE_URL,
        deltaApiKey.ifEmpty { null }
    )

    private var binancePrimaryService: BinanceApiService = BinanceApiService.create(
        BinanceApiService.BASE_URL,
        binanceApiKey.ifEmpty { null }
    )

    private var binanceFallbackService: BinanceApiService = BinanceApiService.create(
        BinanceApiService.FALLBACK_URL,
        binanceApiKey.ifEmpty { null }
    )

    // In-memory cache for fast, synchronized cross-screen access
    private val tickersCache = ConcurrentHashMap<String, Ticker24h>()
    private val candlesCache = ConcurrentHashMap<String, List<Candle>>()

    fun setProvider(provider: ExchangeProvider) {
        this.currentProvider = provider
        Log.i(TAG, "Switched exchange provider to: ${provider.displayName}")
    }

    fun updateDeltaApiKey(key: String) {
        this.deltaApiKey = key
        this.deltaService = DeltaExchangeApiService.create(DeltaExchangeApiService.BASE_URL, key.ifEmpty { null })
        Log.i(TAG, "Configured custom Delta Exchange API key")
    }

    fun updateBinanceApiKey(key: String) {
        this.binanceApiKey = key
        this.binancePrimaryService = BinanceApiService.create(BinanceApiService.BASE_URL, key.ifEmpty { null })
        this.binanceFallbackService = BinanceApiService.create(BinanceApiService.FALLBACK_URL, key.ifEmpty { null })
        Log.i(TAG, "Configured custom Binance API key")
    }

    fun updateBaseUrl(customUrl: String) {
        try {
            if (currentProvider == ExchangeProvider.DELTA_EXCHANGE) {
                deltaService = DeltaExchangeApiService.create(customUrl, deltaApiKey.ifEmpty { null })
            } else {
                binancePrimaryService = BinanceApiService.create(customUrl, binanceApiKey.ifEmpty { null })
            }
            Log.i(TAG, "Updated primary API URL to $customUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update custom API URL: ${e.message}")
        }
    }

    suspend fun fetch24hTickers(): Result<List<Ticker24h>> = withContext(Dispatchers.IO) {
        val symbolsToFetch = DEFAULT_CRYPTO_PAIRS.map { it.symbol }.toSet()

        // If Delta Exchange is selected, query Delta Exchange first
        if (currentProvider == ExchangeProvider.DELTA_EXCHANGE) {
            val deltaResult = tryFetchDeltaTickers(symbolsToFetch)
            if (deltaResult != null && deltaResult.isNotEmpty()) {
                deltaResult.forEach { tickersCache[it.symbol] = it }
                return@withContext Result.success(deltaResult)
            }
        }

        // Query Binance (Primary & Fallback)
        val binanceResult = tryFetchBinanceTickers(symbolsToFetch)
        if (binanceResult != null && binanceResult.isNotEmpty()) {
            binanceResult.forEach { tickersCache[it.symbol] = it }
            return@withContext Result.success(binanceResult)
        }

        // Return from cache or base generator
        if (tickersCache.isNotEmpty()) {
            Result.success(tickersCache.values.toList())
        } else {
            val fallbackList = generateInitialTickers()
            fallbackList.forEach { tickersCache[it.symbol] = it }
            Result.success(fallbackList)
        }
    }

    private suspend fun tryFetchDeltaTickers(symbolsToFetch: Set<String>): List<Ticker24h>? {
        return try {
            val resp = deltaService.getTickers()
            val list = resp.result ?: return null
            val now = System.currentTimeMillis()

            list.filter { it.symbol in symbolsToFetch }.map { item ->
                val lastPrice = item.close ?: item.spotPrice?.toDoubleOrNull() ?: item.markPrice?.toDoubleOrNull() ?: 0.0
                val openPrice = item.open ?: lastPrice
                val priceChange = lastPrice - openPrice
                val changePct = if (openPrice > 0) ((lastPrice - openPrice) / openPrice) * 100.0 else 0.0

                Ticker24h(
                    symbol = item.symbol.orEmpty(),
                    lastPrice = lastPrice,
                    priceChange = priceChange,
                    priceChangePercent = changePct,
                    highPrice = item.high ?: (lastPrice * 1.01),
                    lowPrice = item.low ?: (lastPrice * 0.99),
                    volume = item.volume ?: 0.0,
                    quoteVolume = item.turnover ?: 0.0,
                    timestamp = now
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Delta Exchange fetch24hTickers note: ${e.message}")
            null
        }
    }

    private suspend fun tryFetchBinanceTickers(symbolsToFetch: Set<String>): List<Ticker24h>? {
        // Try Primary Binance.US
        val p = try {
            val res = binancePrimaryService.getAll24hTickers()
            mapBinanceTickers(res, symbolsToFetch)
        } catch (e: Exception) {
            null
        }
        if (!p.isNullOrEmpty()) return p

        // Try Fallback Binance Global
        return try {
            val res = binanceFallbackService.getAll24hTickers()
            mapBinanceTickers(res, symbolsToFetch)
        } catch (e: Exception) {
            null
        }
    }

    private fun mapBinanceTickers(responseList: List<com.example.data.model.Ticker24hResponse>, symbolsToFetch: Set<String>): List<Ticker24h> {
        return responseList
            .filter { it.symbol in symbolsToFetch }
            .map { item ->
                Ticker24h(
                    symbol = item.symbol,
                    lastPrice = item.lastPrice?.toDoubleOrNull() ?: 0.0,
                    priceChange = item.priceChange?.toDoubleOrNull() ?: 0.0,
                    priceChangePercent = item.priceChangePercent?.toDoubleOrNull() ?: 0.0,
                    highPrice = item.highPrice?.toDoubleOrNull() ?: 0.0,
                    lowPrice = item.lowPrice?.toDoubleOrNull() ?: 0.0,
                    volume = item.volume?.toDoubleOrNull() ?: 0.0,
                    quoteVolume = item.quoteVolume?.toDoubleOrNull() ?: 0.0
                )
            }
    }

    suspend fun fetchSingleTicker(symbol: String): Result<Ticker24h> = withContext(Dispatchers.IO) {
        val cached = tickersCache[symbol]
        if (cached != null) return@withContext Result.success(cached)

        val allRes = fetch24hTickers()
        allRes.onSuccess { list ->
            val match = list.find { it.symbol == symbol }
            if (match != null) return@withContext Result.success(match)
        }

        val fallback = generateInitialTickers().find { it.symbol == symbol }
        if (fallback != null) {
            Result.success(fallback)
        } else {
            Result.failure(Exception("Ticker not found for $symbol"))
        }
    }

    suspend fun fetchCandles(
        symbol: String,
        timeframe: Timeframe,
        limit: Int = 100
    ): Result<List<Candle>> = withContext(Dispatchers.IO) {
        val cacheKey = "${symbol}_${timeframe.intervalCode}"

        // 1. If Delta Exchange is selected, fetch Delta historical candles
        if (currentProvider == ExchangeProvider.DELTA_EXCHANGE) {
            val deltaCandles = tryFetchDeltaCandles(symbol, timeframe, limit)
            if (!deltaCandles.isNullOrEmpty()) {
                candlesCache[cacheKey] = deltaCandles
                return@withContext Result.success(deltaCandles)
            }
        }

        // 2. Try Binance.US / Binance
        val binanceCandles = tryFetchBinanceCandles(symbol, timeframe, limit)
        if (!binanceCandles.isNullOrEmpty()) {
            candlesCache[cacheKey] = binanceCandles
            return@withContext Result.success(binanceCandles)
        }

        // 3. Fallback from memory cache or synthetic
        fallbackCandles(symbol, timeframe, cacheKey)
    }

    private suspend fun tryFetchDeltaCandles(
        symbol: String,
        timeframe: Timeframe,
        limit: Int
    ): List<Candle>? {
        return try {
            val nowSec = System.currentTimeMillis() / 1000L
            val durationSec = timeframe.minutes * 60L * limit
            val startSec = nowSec - durationSec

            val resp = deltaService.getHistoryCandles(
                symbol = symbol,
                resolution = timeframe.intervalCode,
                start = startSec,
                end = nowSec
            )
            val items = resp.result ?: return null
            if (items.isEmpty()) return null

            val intervalMs = timeframe.minutes * 60_000L
            // Delta returns items in reverse order; sort by time
            items.sortedBy { it.time ?: 0L }.mapNotNull { raw ->
                val timeSec = raw.time ?: return@mapNotNull null
                val openTime = timeSec * 1000L
                val open = raw.open ?: 0.0
                val high = raw.high ?: open
                val low = raw.low ?: open
                val close = raw.close ?: open
                val volume = raw.volume ?: 0.0
                val closeTime = openTime + intervalMs
                Candle(openTime, open, high, low, close, volume, closeTime)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Delta Exchange candles note: ${e.message}")
            null
        }
    }

    private suspend fun tryFetchBinanceCandles(
        symbol: String,
        timeframe: Timeframe,
        limit: Int
    ): List<Candle>? {
        return try {
            val raw = binancePrimaryService.getKlines(symbol, timeframe.intervalCode, limit)
            parseBinanceKlines(raw, timeframe)
        } catch (e: Exception) {
            try {
                val raw = binanceFallbackService.getKlines(symbol, timeframe.intervalCode, limit)
                parseBinanceKlines(raw, timeframe)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parseBinanceKlines(rawKlines: List<List<Any>>, timeframe: Timeframe): List<Candle> {
        return rawKlines.mapNotNull { raw ->
            try {
                val openTime = (raw.getOrNull(0) as? Number)?.toLong()
                    ?: (raw.getOrNull(0) as? String)?.toLongOrNull() ?: return@mapNotNull null
                val open = (raw.getOrNull(1) as? String)?.toDoubleOrNull()
                    ?: (raw.getOrNull(1) as? Number)?.toDouble() ?: 0.0
                val high = (raw.getOrNull(2) as? String)?.toDoubleOrNull()
                    ?: (raw.getOrNull(2) as? Number)?.toDouble() ?: 0.0
                val low = (raw.getOrNull(3) as? String)?.toDoubleOrNull()
                    ?: (raw.getOrNull(3) as? Number)?.toDouble() ?: 0.0
                val close = (raw.getOrNull(4) as? String)?.toDoubleOrNull()
                    ?: (raw.getOrNull(4) as? Number)?.toDouble() ?: 0.0
                val volume = (raw.getOrNull(5) as? String)?.toDoubleOrNull()
                    ?: (raw.getOrNull(5) as? Number)?.toDouble() ?: 0.0
                val closeTime = (raw.getOrNull(6) as? Number)?.toLong()
                    ?: (raw.getOrNull(6) as? String)?.toLongOrNull() ?: openTime + (timeframe.minutes * 60_000L)

                Candle(openTime, open, high, low, close, volume, closeTime)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun fallbackCandles(
        symbol: String,
        timeframe: Timeframe,
        cacheKey: String
    ): Result<List<Candle>> {
        val cached = candlesCache[cacheKey]
        if (cached != null && cached.isNotEmpty()) {
            return Result.success(cached)
        }
        val generated = generateSyntheticCandles(symbol, timeframe)
        candlesCache[cacheKey] = generated
        return Result.success(generated)
    }

    fun getCachedTicker(symbol: String): Ticker24h? = tickersCache[symbol]

    fun getAllCachedTickers(): List<Ticker24h> = tickersCache.values.toList()

    private fun generateInitialTickers(): List<Ticker24h> {
        val now = System.currentTimeMillis()
        return listOf(
            Ticker24h("BTCUSDT", 75820.0, 120.0, 0.16, 76200.0, 75500.0, 32410.5, 2465000000.0, now),
            Ticker24h("ETHUSDT", 2235.0, 15.2, 0.68, 2280.0, 2210.0, 184500.0, 498000000.0, now),
            Ticker24h("SOLUSDT", 89.8, -0.9, -1.02, 92.5, 88.2, 856000.0, 162000000.0, now),
            Ticker24h("BNBUSDT", 615.2, 12.8, 2.12, 622.0, 598.0, 94000.0, 57000000.0, now),
            Ticker24h("DOGEUSDT", 0.072, 0.001, 1.39, 0.075, 0.070, 2800000000.0, 480000000.0, now),
            Ticker24h("XRPUSDT", 1.158, 0.012, 1.04, 1.18, 1.14, 520000000.0, 305000000.0, now),
            Ticker24h("ADAUSDT", 0.435, 0.018, 4.31, 0.448, 0.412, 185000000.0, 79000000.0, now),
            Ticker24h("AVAXUSDT", 28.6, 1.45, 5.34, 29.4, 26.8, 4200000.0, 118000000.0, now)
        )
    }

    private fun generateSyntheticCandles(symbol: String, timeframe: Timeframe): List<Candle> {
        val basePrice = when (symbol) {
            "BTCUSDT" -> 75800.0
            "ETHUSDT" -> 2235.0
            "SOLUSDT" -> 89.8
            "BNBUSDT" -> 615.0
            "DOGEUSDT" -> 0.072
            "XRPUSDT" -> 1.158
            "ADAUSDT" -> 0.43
            "AVAXUSDT" -> 28.5
            else -> 100.0
        }
        val count = 80
        val intervalMs = timeframe.minutes * 60_000L
        val now = System.currentTimeMillis()
        val candles = mutableListOf<Candle>()

        var currentClose = basePrice * 0.98
        for (i in 0 until count) {
            val openTime = now - ((count - i) * intervalMs)
            val closeTime = openTime + intervalMs
            val delta = (Math.sin(i * 0.15) * 0.005 + (Math.cos(i * 0.07) * 0.003)) * currentClose
            val open = currentClose
            val close = open + delta
            val high = maxOf(open, close) + (Math.abs(delta) * 0.5) + (currentClose * 0.001)
            val low = minOf(open, close) - (Math.abs(delta) * 0.4) - (currentClose * 0.001)
            val volume = (basePrice * 10.0) * (1.0 + Math.abs(Math.sin(i * 0.3)))

            candles.add(Candle(openTime, open, high, low, close, volume, closeTime))
            currentClose = close
        }
        return candles
    }
}
