package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standard Candlestick data model.
 */
data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Long
)

/**
 * Supported timeframe intervals.
 */
enum class Timeframe(
    val label: String,
    val intervalCode: String,
    val minutes: Int
) {
    M5("5m", "5m", 5),
    M15("15m", "15m", 15),
    H1("1h", "1h", 60),
    H4("4h", "4h", 240),
    D1("1d", "1d", 1440);

    companion object {
        fun fromCode(code: String): Timeframe =
            entries.find { it.intervalCode.equals(code, ignoreCase = true) } ?: M15
    }
}

/**
 * Supported cryptocurrency trading pairs.
 */
data class CryptoPair(
    val symbol: String,        // e.g. "BTCUSDT"
    val displayPair: String,   // e.g. "BTC/USDT"
    val baseAsset: String,     // e.g. "BTC"
    val quoteAsset: String,    // e.g. "USDT"
    val displayName: String,   // e.g. "Bitcoin"
    val priceDecimals: Int = 2
)

val DEFAULT_CRYPTO_PAIRS = listOf(
    CryptoPair("BTCUSDT", "BTC/USDT", "BTC", "USDT", "Bitcoin", 2),
    CryptoPair("ETHUSDT", "ETH/USDT", "ETH", "USDT", "Ethereum", 2),
    CryptoPair("SOLUSDT", "SOL/USDT", "SOL", "USDT", "Solana", 2),
    CryptoPair("BNBUSDT", "BNB/USDT", "BNB", "USDT", "BNB", 2),
    CryptoPair("DOGEUSDT", "DOGE/USDT", "DOGE", "USDT", "Dogecoin", 4),
    CryptoPair("XRPUSDT", "XRP/USDT", "XRP", "USDT", "XRP", 4),
    CryptoPair("ADAUSDT", "ADA/USDT", "ADA", "USDT", "Cardano", 4),
    CryptoPair("AVAXUSDT", "AVAX/USDT", "AVAX", "USDT", "Avalanche", 2)
)

/**
 * 24-hour Ticker Price and Volume Statistics.
 */
@JsonClass(generateAdapter = true)
data class Ticker24hResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "lastPrice") val lastPrice: String?,
    @Json(name = "priceChange") val priceChange: String?,
    @Json(name = "priceChangePercent") val priceChangePercent: String?,
    @Json(name = "highPrice") val highPrice: String?,
    @Json(name = "lowPrice") val lowPrice: String?,
    @Json(name = "volume") val volume: String?,
    @Json(name = "quoteVolume") val quoteVolume: String?
)

data class Ticker24h(
    val symbol: String,
    val lastPrice: Double,
    val priceChange: Double,
    val priceChangePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val quoteVolume: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isPositive: Boolean get() = priceChangePercent >= 0
}

/**
 * Direction of trading setup.
 */
enum class SignalAction {
    LONG,
    SHORT,
    WAIT
}

/**
 * Swing point for market structure.
 */
data class SwingPoint(
    val index: Int,
    val time: Long,
    val price: Double,
    val isHigh: Boolean // true = swing high, false = swing low
)

/**
 * Trendline parameters.
 */
data class Trendline(
    val startIndex: Int,
    val startPrice: Double,
    val endIndex: Int,
    val endPrice: Double,
    val isSupport: Boolean, // true = support (Higher Lows), false = resistance (Lower Highs)
    val slope: Double,
    val touchesCount: Int
)

/**
 * Calculated technical indicator values.
 */
data class TechnicalIndicators(
    val currentPrice: Double,
    val rsi: Double,
    val ema9: Double,
    val ema20: Double,
    val ema200: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val swingLows: List<SwingPoint> = emptyList(),
    val swingHighs: List<SwingPoint> = emptyList(),
    val activeTrendline: Trendline? = null,
    val supportLevel: Double = 0.0,
    val resistanceLevel: Double = 0.0,
    val isVolumeSurging: Boolean = false,
    val volumeRatio: Double = 1.0,
    val trendBias: String = "NEUTRAL" // BULLISH, BEARISH, NEUTRAL
)

/**
 * AI-generated LONG/SHORT/WAIT trade setup.
 */
data class TradeSignal(
    val id: String,
    val pair: String,
    val currentPrice: Double,
    val action: SignalAction,
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val riskRewardRatio: String,
    val timeframe: String,
    val timestamp: Long,
    val technicalReasons: List<String>,
    val invalidationConditions: List<String>,
    val expiresAt: Long,
    val isExpired: Boolean = false,
    val strategyName: String = "Trendline & Multi-Factor EMA"
)

/**
 * Risk and Position Sizing computation.
 */
data class PositionRiskResult(
    val accountEquity: Double,
    val riskPercentage: Double,
    val riskCapitalUsdt: Double,
    val entryPrice: Double,
    val stopLossPrice: Double,
    val stopLossDistancePercent: Double,
    val positionSizeUnits: Double,
    val positionSizeUsdt: Double,
    val estimatedLossUsdt: Double,
    val suggestedLeverage: Int,
    val leverageWarning: String?
)
