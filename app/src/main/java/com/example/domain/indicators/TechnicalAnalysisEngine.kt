package com.example.domain.indicators

import com.example.data.model.Candle
import com.example.data.model.SwingPoint
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Trendline
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TechnicalAnalysisEngine {

    /**
     * Computes all technical indicators from historical candles.
     */
    fun analyze(candles: List<Candle>): TechnicalIndicators {
        if (candles.isEmpty()) {
            return TechnicalIndicators(
                currentPrice = 0.0,
                rsi = 50.0,
                ema9 = 0.0,
                ema20 = 0.0,
                ema200 = 0.0,
                macdLine = 0.0,
                macdSignal = 0.0,
                macdHistogram = 0.0
            )
        }

        val closes = candles.map { it.close }
        val currentPrice = closes.last()

        val ema9Values = calculateEma(closes, 9)
        val ema20Values = calculateEma(closes, 20)
        val ema200Values = calculateEma(closes, min(200, closes.size))

        val ema9 = ema9Values.lastOrNull() ?: currentPrice
        val ema20 = ema20Values.lastOrNull() ?: currentPrice
        val ema200 = ema200Values.lastOrNull() ?: currentPrice

        val rsiValues = calculateRsi(closes, 14)
        val rsi = rsiValues.lastOrNull() ?: 50.0

        val (macdLine, macdSignal, macdHist) = calculateMacd(closes)

        val swingLows = findSwingPoints(candles, isHigh = false, radius = 3)
        val swingHighs = findSwingPoints(candles, isHigh = true, radius = 3)

        val activeTrendline = calculateBestTrendline(candles, swingLows, swingHighs)

        // Support & Resistance from recent pivots
        val recentLows = swingLows.takeLast(4).map { it.price }
        val recentHighs = swingHighs.takeLast(4).map { it.price }
        val support = if (recentLows.isNotEmpty()) recentLows.minOrNull() ?: (currentPrice * 0.98) else (currentPrice * 0.98)
        val resistance = if (recentHighs.isNotEmpty()) recentHighs.maxOrNull() ?: (currentPrice * 1.02) else (currentPrice * 1.02)

        // Volume analysis
        val volumes = candles.map { it.volume }
        val avgVolume20 = if (volumes.size >= 20) volumes.takeLast(20).average() else volumes.average()
        val currentVolume = volumes.last()
        val volumeRatio = if (avgVolume20 > 0) currentVolume / avgVolume20 else 1.0
        val isVolumeSurging = volumeRatio >= 1.4

        // Overall trend bias
        val trendBias = when {
            ema9 > ema20 && ema20 > ema200 && currentPrice > ema20 -> "BULLISH"
            ema9 < ema20 && ema20 < ema200 && currentPrice < ema20 -> "BEARISH"
            ema9 > ema20 -> "MILD_BULLISH"
            ema9 < ema20 -> "MILD_BEARISH"
            else -> "NEUTRAL"
        }

        return TechnicalIndicators(
            currentPrice = currentPrice,
            rsi = rsi,
            ema9 = ema9,
            ema20 = ema20,
            ema200 = ema200,
            macdLine = macdLine,
            macdSignal = macdSignal,
            macdHistogram = macdHist,
            swingLows = swingLows,
            swingHighs = swingHighs,
            activeTrendline = activeTrendline,
            supportLevel = support,
            resistanceLevel = resistance,
            isVolumeSurging = isVolumeSurging,
            volumeRatio = volumeRatio,
            trendBias = trendBias
        )
    }

    /**
     * Calculates Exponential Moving Average (EMA) series.
     */
    fun calculateEma(values: List<Double>, period: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        if (values.size < period) {
            val sma = values.average()
            return values.map { sma }
        }

        val multiplier = 2.0 / (period + 1.0)
        val result = mutableListOf<Double>()

        // Seed with SMA of the first `period` items
        var ema = values.take(period).average()
        for (i in 0 until period) {
            result.add(ema)
        }

        for (i in period until values.size) {
            ema = ((values[i] - ema) * multiplier) + ema
            result.add(ema)
        }
        return result
    }

    /**
     * Relative Strength Index (RSI) using standard Wilder's smoothing.
     */
    fun calculateRsi(closes: List<Double>, period: Int = 14): List<Double> {
        if (closes.size <= period) return closes.map { 50.0 }

        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()

        for (i in 1 until closes.size) {
            val change = closes[i] - closes[i - 1]
            if (change > 0) {
                gains.add(change)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(abs(change))
            }
        }

        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()

        val rsiList = mutableListOf<Double>()
        // Pad the first period items
        for (i in 0 until period) {
            rsiList.add(50.0)
        }

        for (i in period until gains.size) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period

            val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList.add(rsi)
        }

        return rsiList
    }

    /**
     * Moving Average Convergence Divergence (MACD) (12, 26, 9).
     */
    fun calculateMacd(closes: List<Double>): Triple<Double, Double, Double> {
        if (closes.size < 26) return Triple(0.0, 0.0, 0.0)

        val ema12 = calculateEma(closes, 12)
        val ema26 = calculateEma(closes, 26)

        val macdLineSeries = ema12.indices.map { i -> ema12[i] - ema26[i] }
        val signalSeries = calculateEma(macdLineSeries, 9)

        val macdLine = macdLineSeries.last()
        val signalLine = signalSeries.last()
        val histogram = macdLine - signalLine

        return Triple(macdLine, signalLine, histogram)
    }

    /**
     * Detects Swing Highs and Swing Lows using fractal radius.
     */
    fun findSwingPoints(candles: List<Candle>, isHigh: Boolean, radius: Int = 3): List<SwingPoint> {
        val points = mutableListOf<SwingPoint>()
        if (candles.size < (radius * 2 + 1)) return points

        for (i in radius until (candles.size - radius)) {
            val center = candles[i]
            var isPivot = true

            for (k in 1..radius) {
                if (isHigh) {
                    if (candles[i - k].high >= center.high || candles[i + k].high >= center.high) {
                        isPivot = false
                        break
                    }
                } else {
                    if (candles[i - k].low <= center.low || candles[i + k].low <= center.low) {
                        isPivot = false
                        break
                    }
                }
            }

            if (isPivot) {
                points.add(
                    SwingPoint(
                        index = i,
                        time = center.openTime,
                        price = if (isHigh) center.high else center.low,
                        isHigh = isHigh
                    )
                )
            }
        }
        return points
    }

    /**
     * Automatic Trendline system:
     * Identifies Higher Lows (Support Trendline) or Lower Highs (Resistance Trendline).
     */
    fun calculateBestTrendline(
        candles: List<Candle>,
        swingLows: List<SwingPoint>,
        swingHighs: List<SwingPoint>
    ): Trendline? {
        if (candles.size < 10) return null

        // 1. Look for Higher Lows (Bullish Support Trendline)
        if (swingLows.size >= 2) {
            val lastLows = swingLows.takeLast(4)
            for (i in 0 until (lastLows.size - 1)) {
                val p1 = lastLows[i]
                val p2 = lastLows[i + 1]
                // Higher low: second low is higher than first low
                if (p2.price > p1.price && (p2.index - p1.index) >= 4) {
                    val slope = (p2.price - p1.price) / (p2.index - p1.index)
                    // Verify that intermediate candles don't significantly breach trendline
                    var breaches = 0
                    for (c in p1.index..p2.index) {
                        val projected = p1.price + slope * (c - p1.index)
                        if (candles[c].low < projected * 0.995) {
                            breaches++
                        }
                    }
                    if (breaches <= 1) {
                        return Trendline(
                            startIndex = p1.index,
                            startPrice = p1.price,
                            endIndex = p2.index,
                            endPrice = p2.price,
                            isSupport = true,
                            slope = slope,
                            touchesCount = 2
                        )
                    }
                }
            }
        }

        // 2. Look for Lower Highs (Bearish Resistance Trendline)
        if (swingHighs.size >= 2) {
            val lastHighs = swingHighs.takeLast(4)
            for (i in 0 until (lastHighs.size - 1)) {
                val p1 = lastHighs[i]
                val p2 = lastHighs[i + 1]
                // Lower high: second high is lower than first
                if (p2.price < p1.price && (p2.index - p1.index) >= 4) {
                    val slope = (p2.price - p1.price) / (p2.index - p1.index)
                    var breaches = 0
                    for (c in p1.index..p2.index) {
                        val projected = p1.price + slope * (c - p1.index)
                        if (candles[c].high > projected * 1.005) {
                            breaches++
                        }
                    }
                    if (breaches <= 1) {
                        return Trendline(
                            startIndex = p1.index,
                            startPrice = p1.price,
                            endIndex = p2.index,
                            endPrice = p2.price,
                            isSupport = false,
                            slope = slope,
                            touchesCount = 2
                        )
                    }
                }
            }
        }

        return null
    }
}
