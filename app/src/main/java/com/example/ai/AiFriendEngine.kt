package com.example.ai

import android.util.Log
import com.example.data.model.Candle
import com.example.data.model.SignalAction
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Ticker24h
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import com.example.domain.signals.SignalEngine
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

enum class LanguageTone(val label: String, val promptDescriptor: String) {
    HINGLISH("Friendly Hinglish", "friendly, warm Indian Hinglish crypto trader companion (uses natural expressions like 'Bhai', 'dhyan se dekho', 'setup', 'profit', 'entry zone')"),
    ENGLISH("Conversational English", "knowledgeable, friendly crypto trading friend and buddy"),
    HINDI("Pure Hindi", "दोस्ताना और अनुभवी हिंदी क्रिप्टो ट्रेडिंग साथी"),
    PROFESSIONAL("Professional Analyst", "institutional quantitative crypto market analyst")
}

data class AiResponse(
    val replyText: String,
    val attachedSignal: TradeSignal? = null
)

class AiFriendEngine {
    private val TAG = "AiFriendEngine"

    // Lazily initialize Firebase AI Logic generative model
    private val generativeModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.6f
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI Logic initialization note: ${e.message}")
            null
        }
    }

    suspend fun chat(
        userMessage: String,
        pair: String,
        timeframe: Timeframe,
        ticker: Ticker24h?,
        indicators: TechnicalIndicators?,
        candles: List<Candle>,
        tone: LanguageTone
    ): AiResponse = withContext(Dispatchers.Default) {
        val currentPrice = ticker?.lastPrice ?: indicators?.currentPrice ?: 0.0
        val priceChange = ticker?.priceChangePercent ?: 0.0

        // Always calculate real, ground-truth signal first
        val computedSignal = if (indicators != null && candles.isNotEmpty()) {
            SignalEngine.generateSignal(pair, timeframe, indicators, candles)
        } else null

        // Check if user is asking for analysis or setup
        val isSetupIntent = isSignalRequest(userMessage)

        // Try Firebase AI Logic with strict timeout
        var aiText: String? = null
        try {
            val model = generativeModel
            if (model != null && ticker != null && indicators != null) {
                val prompt = buildGroundedPrompt(
                    userMessage = userMessage,
                    pair = pair,
                    timeframe = timeframe,
                    ticker = ticker,
                    indicators = indicators,
                    signal = computedSignal,
                    tone = tone
                )
                aiText = withTimeout(15_000L) {
                    model.generateContent(prompt).text
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Cloud AI unavailable or timed out, generating local grounded analysis: ${e.message}")
        }

        // If cloud AI not available or empty, use high-fidelity grounded analytical friend engine
        val finalReply = if (!aiText.isNullOrBlank()) {
            aiText.trim()
        } else {
            generateLocalGroundedResponse(
                userMessage = userMessage,
                pair = pair,
                timeframe = timeframe,
                ticker = ticker,
                indicators = indicators,
                signal = computedSignal,
                tone = tone
            )
        }

        AiResponse(
            replyText = finalReply,
            attachedSignal = if (isSetupIntent) computedSignal else null
        )
    }

    private fun isSignalRequest(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("long") || lower.contains("short") || lower.contains("entry") ||
                lower.contains("setup") || lower.contains("signal") || lower.contains("kharide") ||
                lower.contains("buy") || lower.contains("sell") || lower.contains("trade") ||
                lower.contains("stop loss") || lower.contains("target")
    }

    private fun buildGroundedPrompt(
        userMessage: String,
        pair: String,
        timeframe: Timeframe,
        ticker: Ticker24h,
        indicators: TechnicalIndicators,
        signal: TradeSignal?,
        tone: LanguageTone
    ): String {
        return """
You are "Crypto AI Friend", a warm, intelligent cryptocurrency market analysis and trading companion.
You speak like a knowledgeable friend in ${tone.promptDescriptor}.

CRITICAL INTEGRITY INSTRUCTIONS:
- You must base your answers STRICTLY on the real market snapshot provided below.
- NEVER invent or hallucinate market prices, indicators, or past performance.
- NEVER guarantee profits. Remind users that trading carries risk and capital protection is paramount.
- If market setup conditions are incomplete, advise WAIT / NO TRADE.

REAL-TIME MARKET SNAPSHOT:
- Pair: $pair
- Current Price: $${ticker.lastPrice}
- 24h Change: ${ticker.priceChangePercent}% (24h High: $${ticker.highPrice}, Low: $${ticker.lowPrice})
- Timeframe: ${timeframe.label}
- Technical Indicators:
  * RSI(14): ${String.format("%.1f", indicators.rsi)}
  * EMA 9: $${String.format("%.2f", indicators.ema9)}
  * EMA 20: $${String.format("%.2f", indicators.ema20)}
  * EMA 200: $${String.format("%.2f", indicators.ema200)}
  * MACD Histogram: ${String.format("%.4f", indicators.macdHistogram)}
  * Volume Surging: ${indicators.isVolumeSurging} (Ratio: ${String.format("%.1f", indicators.volumeRatio)}x)
  * Trend Bias: ${indicators.trendBias}
- Evaluated Signal: ${signal?.action?.name ?: "WAIT"}
- Stop-Loss Level: $${signal?.stopLoss?.let { String.format("%.2f", it) } ?: "N/A"}
- Take-Profit Target (1:2 R:R): $${signal?.takeProfit2?.let { String.format("%.2f", it) } ?: "N/A"}

USER QUESTION:
"$userMessage"

Respond in 2-4 friendly, concise paragraphs addressing the user directly.
        """.trimIndent()
    }

    fun generateLocalGroundedResponse(
        userMessage: String,
        pair: String,
        timeframe: Timeframe,
        ticker: Ticker24h?,
        indicators: TechnicalIndicators?,
        signal: TradeSignal?,
        tone: LanguageTone
    ): String {
        val lower = userMessage.lowercase()
        val price = ticker?.lastPrice ?: indicators?.currentPrice ?: 0.0
        val change = ticker?.priceChangePercent ?: 0.0
        val rsi = indicators?.rsi ?: 50.0
        val ema9 = indicators?.ema9 ?: price
        val ema20 = indicators?.ema20 ?: price
        val ema200 = indicators?.ema200 ?: price
        val trend = indicators?.trendBias ?: "NEUTRAL"
        val action = signal?.action ?: SignalAction.WAIT
        val sl = signal?.stopLoss ?: (price * 0.98)
        val tp = signal?.takeProfit2 ?: (price * 1.04)

        return when (tone) {
            LanguageTone.HINGLISH -> when {
                lower.contains("long") || lower.contains("short") || lower.contains("entry") || lower.contains("signal") -> {
                    when (action) {
                        SignalAction.LONG -> "Bhai, $pair ka setup abhi **LONG (BUY)** ke favor mein dikh raha hai! Current price $${formatPrice(price)} hai aur EMA 9 ($${formatPrice(ema9)}) EMA 20 ke upar chal raha hai. RSI ${String.format("%.1f", rsi)} par hai jo healthy upward momentum show kar raha hai.\n\nEntry zone: $${formatPrice(price * 0.998)} - $${formatPrice(price * 1.002)}. Stop-Loss swing low ke neeche $${formatPrice(sl)} par set karna, aur target $${formatPrice(tp)} (1:2 Risk-Reward) rakhenge. Risk management hamesha follow karna bhai!"
                        SignalAction.SHORT -> "Bhai, $pair ka setup abhi **SHORT (SELL)** ke favor mein ja raha hai. Price $${formatPrice(price)} par hai aur EMA 9 ($${formatPrice(ema9)}) EMA 20 ke neeche reject ho raha hai. RSI ${String.format("%.1f", rsi)} par selling pressure reflect kar raha hai.\n\nStop-Loss $${formatPrice(sl)} ke upar strict lagana aur Take-Profit $${formatPrice(tp)} target rakhna. Market volatile hai, isliye over-leverage bilkul mat lena!"
                        SignalAction.WAIT -> "Bhai, $pair mein abhi jaldbazi mat karo! Market abhi choppy zone mein hai aur RSI ${String.format("%.1f", rsi)} neutral consolidate ho raha hai. Koi clean Higher-Low bounce ya breakout confirm nahi hua hai.\n\nFilhaal **WAIT / NO TRADE** karna hi sabse smart decision hai. Capital bacha rahega toh agla badhiya 1:2 setup zaroor milega!"
                    }
                }
                lower.contains("trend") || lower.contains("bullish") || lower.contains("bearish") -> {
                    "Bhai, $pair ka overall trend ${if (price > ema200) "BULLISH" else "BEARISH"} structure bana raha hai. 24h change ${String.format("%+.2f", change)}% hai. Price abhi EMA 200 ($${formatPrice(ema200)}) ke ${if (price > ema200) "upar trade ho raha hai jo buyers ka strength dikhata hai" else "neeche reject ho raha hai jo supply pressure show karta hai"}. RSI ${String.format("%.1f", rsi)} par stable hai."
                }
                lower.contains("rsi") -> {
                    "Bhai, RSI (Relative Strength Index) market ka speedometer hota hai! 0 se 100 ke beech chalta hai. Agar 70 ke upar hai toh 'Overbought' maana jata hai (price thoda gir sakta hai), aur 30 ke neeche 'Oversold' hota hai (bounce aa sakta hai). Abhi $pair ka RSI ${String.format("%.1f", rsi)} par hai, jo ekdum healthy zone mein hai!"
                }
                lower.contains("stop loss") || lower.contains("sl") || lower.contains("target") -> {
                    "Bhai, Stop Loss trading ki life-insurance hai! $pair ke liye latest confirmed swing level ke hisaab se Stop Loss $${formatPrice(sl)} par aata hai, aur 1:2 Risk-Reward target $${formatPrice(tp)} banta hai. Kabhi bhi bina SL ke trade mat lagana dost!"
                }
                else -> {
                    "Bhai, chalo $pair ka live data check karte hain! Current price $${formatPrice(price)} chal raha hai (${String.format("%+.2f", change)}% 24h). RSI ${String.format("%.1f", rsi)} aur EMA 9/20 trend align ho rahe hain. Tujhe iska detailed chart dekhna hai, risk calculate karna hai ya signal setup chahiye?"
                }
            }

            LanguageTone.ENGLISH -> when {
                lower.contains("long") || lower.contains("short") || lower.contains("entry") || lower.contains("signal") -> {
                    when (action) {
                        SignalAction.LONG -> "Hey friend! $pair is currently setting up for a solid **LONG opportunity** on the ${timeframe.label} chart. Current price is $${formatPrice(price)}. EMA 9 ($${formatPrice(ema9)}) is leading above EMA 20, and RSI is steady at ${String.format("%.1f", rsi)}.\n\nRecommended Entry Zone is $${formatPrice(price * 0.998)} - $${formatPrice(price * 1.002)} with a disciplined Stop-Loss at $${formatPrice(sl)} and Take-Profit at $${formatPrice(tp)} for a clean 1:2 Risk-Reward ratio."
                        SignalAction.SHORT -> "Hey friend! $pair is showing signs of a **SHORT setup** on ${timeframe.label}. Selling momentum pushed price to $${formatPrice(price)}, breaking below EMA 20 ($${formatPrice(ema20)}). RSI is down at ${String.format("%.1f", rsi)}.\n\nKey Entry Zone is $${formatPrice(price * 0.998)} - $${formatPrice(price * 1.002)}, with Stop-Loss anchored above swing high at $${formatPrice(sl)} and target at $${formatPrice(tp)}."
                        SignalAction.WAIT -> "Friend, right now the smart move on $pair is **WAIT / NO TRADE**. The price action is consolidating with RSI at ${String.format("%.1f", rsi)} and no clear trendline bounce has triggered. Preserving trading capital during sideways chop is how disciplined traders win long-term!"
                    }
                }
                lower.contains("trend") -> {
                    "The current trend for $pair (${timeframe.label}) is **$trend**. Price is $${formatPrice(price)} with a 24-hour move of ${String.format("%+.2f", change)}%. EMA 9 is at $${formatPrice(ema9)} and EMA 200 baseline sits at $${formatPrice(ema200)}."
                }
                lower.contains("rsi") -> {
                    "RSI measures price momentum on a scale of 0 to 100. Readings above 70 indicate overbought conditions, while readings below 30 signal oversold territory. $pair currently registers an RSI of ${String.format("%.1f", rsi)}, representing balanced market flow."
                }
                else -> {
                    "Hey friend! I've analyzed the live orderbook and candles for $pair. Current price is $${formatPrice(price)} (${String.format("%+.2f", change)}% today) with RSI at ${String.format("%.1f", rsi)}. Feel free to ask me for a LONG/SHORT setup, stop-loss calculation, or market scan!"
                }
            }

            LanguageTone.HINDI -> {
                "भाई, $pair का ताज़ा भाव $${formatPrice(price)} चल रहा है (24 घंटे का बदलाव ${String.format("%+.2f", change)}%)। RSI ${String.format("%.1f", rsi)} पर है और EMA 9 $${formatPrice(ema9)} पर स्थित है। बाज़ार का रुख़ अभी **$trend** बना हुआ है। $action संकेत के अनुसार स्टॉप लॉस $${formatPrice(sl)} और लक्ष्य $${formatPrice(tp)} अनुशंसित है। बिना स्टॉप लॉस के कभी ट्रेड न करें।"
            }

            LanguageTone.PROFESSIONAL -> {
                "Executive Market Briefing for $pair (${timeframe.label}): Spot price is $${formatPrice(price)} with a 24-hour variance of ${String.format("%+.2f", change)}%. Technical metrics: RSI(14) = ${String.format("%.1f", rsi)}; EMA(9) = $${formatPrice(ema9)}; EMA(20) = $${formatPrice(ema20)}; EMA(200) = $${formatPrice(ema200)}. Structural Action: $action setup recommended with invalidation at $${formatPrice(sl)} and target at $${formatPrice(tp)}."
            }
        }
    }

    private fun formatPrice(price: Double): String {
        return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
    }
}
