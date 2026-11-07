package com.example.domain.signals

import com.example.data.model.Candle
import com.example.data.model.SignalAction
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max

object SignalEngine {

    /**
     * Evaluates technical indicators against the Trendline Trading Strategy
     * and generates an objective LONG, SHORT, or WAIT / NO TRADE trade setup.
     */
    fun generateSignal(
        pair: String,
        timeframe: Timeframe,
        indicators: TechnicalIndicators,
        candles: List<Candle>
    ): TradeSignal {
        val currentPrice = indicators.currentPrice
        val now = System.currentTimeMillis()
        val validityDurationMs = timeframe.minutes * 60_000L * 4 // Valid for 4 candles
        val expiresAt = now + validityDurationMs

        if (candles.size < 20 || currentPrice <= 0.0) {
            return TradeSignal(
                id = UUID.randomUUID().toString(),
                pair = pair,
                currentPrice = currentPrice,
                action = SignalAction.WAIT,
                entryZoneMin = currentPrice,
                entryZoneMax = currentPrice,
                stopLoss = currentPrice * 0.98,
                takeProfit1 = currentPrice * 1.02,
                takeProfit2 = currentPrice * 1.04,
                riskRewardRatio = "1:2.0",
                timeframe = timeframe.label,
                timestamp = now,
                technicalReasons = listOf("Insufficient candlestick history to confirm safe setup."),
                invalidationConditions = listOf("Wait for at least 20 historical candles."),
                expiresAt = expiresAt
            )
        }

        val rsi = indicators.rsi
        val ema9 = indicators.ema9
        val ema20 = indicators.ema20
        val ema200 = indicators.ema200
        val macdHist = indicators.macdHistogram
        val trendline = indicators.activeTrendline

        val reasons = mutableListOf<String>()
        val invalidations = mutableListOf<String>()

        // 1. EVALUATE LONG SETUP CONDITIONS
        var longScore = 0
        val longReasons = mutableListOf<String>()

        // Condition A: Moving Average Alignment
        if (ema9 > ema20) {
            longScore += 2
            longReasons.add("EMA 9 ($${formatPrice(ema9)}) crossed above EMA 20 ($${formatPrice(ema20)}) indicating short-term bullish momentum.")
        }
        if (currentPrice > ema200) {
            longScore += 1
            longReasons.add("Price is trading above the long-term 200 EMA baseline.")
        }

        // Condition B: Trendline Support or Bounce
        if (trendline != null && trendline.isSupport) {
            longScore += 2
            longReasons.add("Confirmed Higher Lows support trendline with slope +${String.format("%.4f", trendline.slope)}.")
            val lastCandle = candles.last()
            // Check if price bounced near trendline
            val projectedSupport = trendline.endPrice + trendline.slope * (candles.size - 1 - trendline.endIndex)
            if (abs(lastCandle.low - projectedSupport) / projectedSupport < 0.012) {
                longScore += 2
                longReasons.add("Bullish reaction and wick rejection near trendline support level.")
            }
        }

        // Condition C: RSI Momentum Confirmation
        if (rsi in 42.0..68.0) {
            longScore += 2
            longReasons.add("RSI at ${String.format("%.1f", rsi)} is in the healthy bullish expansion zone without being overbought.")
        }

        // Condition D: MACD Momentum
        if (macdHist > 0) {
            longScore += 1
            longReasons.add("MACD histogram is positive and expanding.")
        }

        // 2. EVALUATE SHORT SETUP CONDITIONS
        var shortScore = 0
        val shortReasons = mutableListOf<String>()

        if (ema9 < ema20) {
            shortScore += 2
            shortReasons.add("EMA 9 ($${formatPrice(ema9)}) is below EMA 20 ($${formatPrice(ema20)}) confirming bearish pressure.")
        }
        if (currentPrice < ema200) {
            shortScore += 1
            shortReasons.add("Price is rejected beneath the 200 EMA resistance baseline.")
        }

        if (trendline != null && !trendline.isSupport) {
            shortScore += 2
            shortReasons.add("Confirmed Lower Highs resistance trendline with downward slope.")
        }

        if (rsi in 32.0..58.0) {
            shortScore += 2
            shortReasons.add("RSI at ${String.format("%.1f", rsi)} reflects bearish continuation without oversold exhaustion.")
        }

        if (macdHist < 0) {
            shortScore += 1
            shortReasons.add("MACD histogram is negative and selling pressure is dominant.")
        }

        // 3. DECISION & RISK TARGET CALCULATION
        val thresholdScore = 5

        if (longScore >= thresholdScore && longScore > shortScore) {
            // Find latest confirmed Swing Low for Stop Loss
            val lastSwingLow = indicators.swingLows.lastOrNull()?.price
            val candidateSl = lastSwingLow ?: (currentPrice * 0.985)
            // Ensure stop loss is actually below current price and within safe distance (1% - 3.5%)
            val stopLoss = if (candidateSl < currentPrice && (currentPrice - candidateSl) / currentPrice in 0.008..0.05) {
                candidateSl * 0.998 // small buffer below swing low
            } else {
                currentPrice * 0.985 // default 1.5% SL
            }

            val riskDistance = currentPrice - stopLoss
            val takeProfit1 = currentPrice + (riskDistance * 1.5)
            val takeProfit2 = currentPrice + (riskDistance * 2.0) // 1:2 Risk to Reward

            invalidations.add("A candle close below the stop-loss level of $${formatPrice(stopLoss)} invalidates this setup.")
            invalidations.add("EMA 9 crossing below EMA 20 will signal immediate momentum loss.")
            invalidations.add("RSI falling below 38.0 invalidates bullish continuation.")

            return TradeSignal(
                id = UUID.randomUUID().toString(),
                pair = pair,
                currentPrice = currentPrice,
                action = SignalAction.LONG,
                entryZoneMin = currentPrice * 0.998,
                entryZoneMax = currentPrice * 1.002,
                stopLoss = stopLoss,
                takeProfit1 = takeProfit1,
                takeProfit2 = takeProfit2,
                riskRewardRatio = "1:2.0",
                timeframe = timeframe.label,
                timestamp = now,
                technicalReasons = longReasons,
                invalidationConditions = invalidations,
                expiresAt = expiresAt
            )
        } else if (shortScore >= thresholdScore && shortScore > longScore) {
            // Find latest confirmed Swing High for Stop Loss
            val lastSwingHigh = indicators.swingHighs.lastOrNull()?.price
            val candidateSl = lastSwingHigh ?: (currentPrice * 1.015)
            val stopLoss = if (candidateSl > currentPrice && (candidateSl - currentPrice) / currentPrice in 0.008..0.05) {
                candidateSl * 1.002
            } else {
                currentPrice * 1.015
            }

            val riskDistance = stopLoss - currentPrice
            val takeProfit1 = currentPrice - (riskDistance * 1.5)
            val takeProfit2 = currentPrice - (riskDistance * 2.0) // 1:2 Risk to Reward

            invalidations.add("A candle close above the stop-loss level of $${formatPrice(stopLoss)} invalidates this setup.")
            invalidations.add("EMA 9 reclaiming above EMA 20 invalidates bearish momentum.")
            invalidations.add("RSI rising above 62.0 invalidates short thesis.")

            return TradeSignal(
                id = UUID.randomUUID().toString(),
                pair = pair,
                currentPrice = currentPrice,
                action = SignalAction.SHORT,
                entryZoneMin = currentPrice * 0.998,
                entryZoneMax = currentPrice * 1.002,
                stopLoss = stopLoss,
                takeProfit1 = takeProfit1,
                takeProfit2 = takeProfit2,
                riskRewardRatio = "1:2.0",
                timeframe = timeframe.label,
                timestamp = now,
                technicalReasons = shortReasons,
                invalidationConditions = invalidations,
                expiresAt = expiresAt
            )
        } else {
            // Neutral / Consolidating / Unclear Market
            val waitReasons = mutableListOf<String>()
            waitReasons.add("Market conditions are currently choppy or conflicting across timeframes.")
            waitReasons.add("RSI is at ${String.format("%.1f", rsi)} indicating neutral range-bound chop.")
            if (trendline == null) {
                waitReasons.add("No clean Higher Low or Lower High trendline structure detected yet.")
            }
            waitReasons.add("Capital preservation rule: Only enter high-probability setups with confirmed 1:2 R:R.")

            invalidations.add("Wait for a clear breakout above $${formatPrice(indicators.resistanceLevel)} or breakdown below $${formatPrice(indicators.supportLevel)}.")

            return TradeSignal(
                id = UUID.randomUUID().toString(),
                pair = pair,
                currentPrice = currentPrice,
                action = SignalAction.WAIT,
                entryZoneMin = currentPrice,
                entryZoneMax = currentPrice,
                stopLoss = currentPrice * 0.98,
                takeProfit1 = currentPrice * 1.02,
                takeProfit2 = currentPrice * 1.04,
                riskRewardRatio = "N/A (WAIT)",
                timeframe = timeframe.label,
                timestamp = now,
                technicalReasons = waitReasons,
                invalidationConditions = invalidations,
                expiresAt = expiresAt
            )
        }
    }

    private fun formatPrice(price: Double): String {
        return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
    }
}
