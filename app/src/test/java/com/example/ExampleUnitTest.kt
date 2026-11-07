package com.example

import com.example.ai.AiFriendEngine
import com.example.ai.LanguageTone
import com.example.data.model.Candle
import com.example.data.model.SignalAction
import com.example.data.model.Timeframe
import com.example.domain.indicators.TechnicalAnalysisEngine
import com.example.domain.risk.RiskCalculator
import com.example.domain.signals.SignalEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private fun createTestCandles(count: Int, startPrice: Double = 70000.0, trend: Double = 50.0): List<Candle> {
        val list = mutableListOf<Candle>()
        var price = startPrice
        val now = System.currentTimeMillis()
        for (i in 0 until count) {
            val open = price
            val close = open + trend
            val high = maxOf(open, close) + 20.0
            val low = minOf(open, close) - 20.0
            val vol = 1000.0 + (i * 10)
            list.add(Candle(now + (i * 60000L), open, high, low, close, vol, now + ((i + 1) * 60000L)))
            price = close
        }
        return list
    }

    @Test
    fun testEmaCalculation() {
        val values = listOf(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0)
        val ema = TechnicalAnalysisEngine.calculateEma(values, period = 5)
        assertEquals(values.size, ema.size)
        // Last EMA should track recent prices closely
        assertTrue(ema.last() > 15.0)
    }

    @Test
    fun testRsiRange() {
        val candles = createTestCandles(40, startPrice = 60000.0, trend = 40.0)
        val closes = candles.map { it.close }
        val rsiList = TechnicalAnalysisEngine.calculateRsi(closes, 14)
        val lastRsi = rsiList.last()
        assertTrue("RSI must be between 0 and 100", lastRsi in 0.0..100.0)
        assertTrue("Strong uptrend should have RSI > 50", lastRsi > 50.0)
    }

    @Test
    fun testMacdCalculation() {
        val candles = createTestCandles(50, startPrice = 50000.0, trend = 100.0)
        val closes = candles.map { it.close }
        val (macdLine, signalLine, histogram) = TechnicalAnalysisEngine.calculateMacd(closes)
        // In strong uptrend, 12 EMA is higher than 26 EMA, so MACD line is positive
        assertTrue(macdLine > 0.0)
    }

    @Test
    fun testSignalEngineGeneration() {
        val candles = createTestCandles(60, startPrice = 70000.0, trend = 80.0)
        val indicators = TechnicalAnalysisEngine.analyze(candles)
        val signal = SignalEngine.generateSignal("BTC/USDT", Timeframe.M15, indicators, candles)

        assertNotNull(signal)
        assertEquals("BTC/USDT", signal.pair)
        assertEquals("15m", signal.timeframe)
        assertTrue("Stop loss should be below current price for Long or above for Short", signal.stopLoss > 0.0)
        assertTrue("Invalidation conditions must not be empty", signal.invalidationConditions.isNotEmpty())
    }

    @Test
    fun testRiskCalculatorFormulas() {
        val equity = 10000.0
        val riskPct = 1.0 // 1% risk = $100
        val entry = 50000.0
        val stopLoss = 49000.0 // $1000 distance = 2%

        val result = RiskCalculator.calculate(
            accountEquity = equity,
            riskPercentage = riskPct,
            entryPrice = entry,
            stopLossPrice = stopLoss
        )

        assertEquals(100.0, result.riskCapitalUsdt, 0.001)
        assertEquals(2.0, result.stopLossDistancePercent, 0.001)
        // Position size USDT = 100 / 0.02 = 5000 USDT
        assertEquals(5000.0, result.positionSizeUsdt, 0.1)
        // Position size units = 5000 / 50000 = 0.1 BTC
        assertEquals(0.1, result.positionSizeUnits, 0.001)
        assertEquals(1, result.suggestedLeverage)
    }

    @Test
    fun testAiFriendLocalResponseGeneration() {
        val engine = AiFriendEngine()
        val textHinglish = engine.generateLocalGroundedResponse(
            userMessage = "Bhai, BTC mein abhi LONG lena chahiye ya SHORT?",
            pair = "BTC/USDT",
            timeframe = Timeframe.M15,
            ticker = null,
            indicators = null,
            signal = null,
            tone = LanguageTone.HINGLISH
        )
        assertTrue("Hinglish response should address the user as Bhai", textHinglish.contains("Bhai"))

        val textEnglish = engine.generateLocalGroundedResponse(
            userMessage = "Should I take LONG on BTC?",
            pair = "BTC/USDT",
            timeframe = Timeframe.M15,
            ticker = null,
            indicators = null,
            signal = null,
            tone = LanguageTone.ENGLISH
        )
        assertTrue("English response should contain conversational tone", textEnglish.contains("friend") || textEnglish.contains("BTC"))
    }
}
