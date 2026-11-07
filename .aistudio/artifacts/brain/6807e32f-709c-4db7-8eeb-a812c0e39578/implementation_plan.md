# Crypto AI Friend — Intelligent Cryptocurrency Trading Assistant

Crypto AI Friend is a modern, high-performance Android trading companion that analyzes live cryptocurrency market feeds, computes institutional-grade technical indicators, generates disciplined LONG/SHORT/WAIT trading setups with transparent risk-reward ratios, and interacts naturally with traders through a friendly, voice-enabled AI companion in Hinglish, English, or Hindi.

***

### User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Configuration & User Preferences**
> - **Market Data Source**: Free public crypto exchange REST API (Binance public market data for live tickers, 24-hour statistics, and candlestick history `klines` in 5m, 15m, 1h, 4h, 1d timeframes with zero API key required, plus custom endpoint configuration).
> - **AI Companion Personality**: Bilingual friendly companion featuring **Friendly Hinglish** ("Bhai, market ko dhyan se dekhte hain...") and **Natural Conversational English**, switchable on-the-fly to Hindi or Professional Analyst mode.
> - **Trading Strategy**: Multi-factor Trendline Trading System combining Higher Low / Lower High swing identification, EMA trend alignment (EMA 9, 20, 200), RSI momentum, MACD divergence/crossover, and automatic 1:2 Risk-Reward Stop-Loss/Take-Profit calculation.

***

## 1. Overview & Core Concept

Crypto AI Friend bridges the gap between raw, overwhelming market data and actionable trading decisions. Rather than presenting static numbers or speculative predictions, it operates as an honest, disciplined trading companion that:
1. **Never guesses or fabricates prices**: Pulls real candlestick history and tick data directly from exchange orderbook/ticker endpoints.
2. **Calculates real mathematical indicators**: Pure client-side calculations for RSI(14), MACD(12,26,9), triple EMAs (9, 20, 200), Swing Highs/Lows, and volume surge.
3. **Generates disciplined signals**: Strictly triggers LONG or SHORT setups only when mathematical criteria align; otherwise defaults to **WAIT / NO TRADE** to preserve capital.
4. **Speaks like a trusted friend**: Grounded AI companion that analyzes the exact live state before speaking, answering questions via voice and text without trading jargon gatekeeping.

***

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             APP LAUNCH & HOME                               │
│  - Live BTC/ETH/SOL Ticker Bar with 24h change color coding                │
│  - Overall Market Sentiment & Trend Direction (Bullish / Neutral / Bearish) │
│  - Best Active LONG & SHORT Setup Cards with 1:2 Risk-Reward Badges         │
│  - Quick AI Action Chips: "Analyze BTC", "Scan Market", "Find Long Setup"   │
└───────────────────────┬───────────────────────────────┬─────────────────────┘
                        │                               │
                        ▼                               ▼
┌──────────────────────────────────────┐  ┌───────────────────────────────────┐
│     MARKET & CANDLESTICK CHARTS      │  │        AI FRIEND COMPANION        │
│  - Interactive Custom Candlestick    │  │  - Real-time Voice & Text chat    │
│    Canvas with Volume Bars & EMAs    │  │  - Hinglish / English / Hindi     │
│  - Timeframes: 5m, 15m, 1h, 4h, 1d   │  │  - Grounded live market context   │
│  - Swing High/Low & Trendlines       │  │  - Dedicated One-Tap Signal Cards │
│  - RSI & MACD Indicator Panels       │  │  - Position size & Risk checks    │
└───────────────────────┬──────────────┘  └───────────────────┬───────────────┘
                        │                                     │
                        ▼                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    MARKET SCANNER & RISK CALCULATOR                         │
│  - Multi-pair scanner (BTC, ETH, SOL, BNB, DOGE, XRP, ADA, AVAX)           │
│  - Filter by Strong Bullish, Bearish, Breakouts, Breakdowns, WAIT           │
│  - Interactive Risk Calculator (Account Equity, Risk %, Entry, SL -> Size)   │
│  - Watchlist & Custom Price Alert management                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Visual Identity & Theme
- **Aesthetic Direction**: Premium High-Tech Crypto Terminal (sleek dark aesthetic inspired by modern trading platforms like TradingView and Bloomberg Terminal).
- **Color Palette & Tokens**:
  - `Background`: Deep Midnight Obsidian (`#0B0E14`)
  - `Surface / Card`: Dark Slate Navy (`#151A24` and `#1B2230`)
  - `Surface Variant / Borders`: Subtle Border Stroke (`#262F40`)
  - `Bullish Green`: Vibrant Emerald (`#0ECB81`) with soft glow accents
  - `Bearish Red`: Vivid Coral Crimson (`#F6465D`) with soft glow accents
  - `Accent / Brand`: Electric Cyan / Neon Blue (`#00F0FF` and `#3861FB`)
  - `Text Primary`: Pure Ice White (`#F5F7FA`)
  - `Text Secondary`: Muted Blue Silver (`#848E9C`)
- **Typography & Hierarchy**:
  - Tabular Numbers: Clean monospace styling for prices and percentages to eliminate visual jitter.
  - Hierarchy: Large bold display figures for primary prices, crisp medium badges for signal directions, and readable body text for AI explanations.
- **Interactive Feedback & Motion**:
  - Spring-based chip selection for timeframes (5m, 15m, 1h, 4h, 1d).
  - Pulsing status dots for live market data connectivity.
  - Animated audio wave visualizer when the AI Friend is speaking.
  - Smooth card expansion for signal invalidation criteria and technical breakdown.

***

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Free Public Exchange Market Feed with Zero-Setup Dependency**
  - *Chosen Approach*: Use public REST endpoints (`api.binance.com/api/v3/ticker/24hr` and `klines`) with OkHttp/Retrofit, supported by client-side indicator calculation and auto-refresh intervals. Provide custom endpoint and API key settings for power users.
  - *Why*: Gives instant, live, real-world data without blocking users with API key registration barriers, while keeping market data authentic and real-time.
- **Decision 2: Dedicated Mathematical Indicator Engine on Device**
  - *Chosen Approach*: Implement pure Kotlin indicator math algorithms (Wilder's RSI, Exponential Moving Averages 9/20/200, MACD with signal line and histogram, ZigZag swing high/low detection, and trendline linear regression).
  - *Why*: Eliminates latency, guarantees deterministic and reproducible signal logic, and ensures calculations never drift or suffer hallucination.
- **Decision 3: Grounded AI Companion with Structured Context Injection**
  - *Chosen Approach*: Pre-compute indicators and market statistics into a concise, structured snapshot that is injected into the AI system prompt before answering user questions.
  - *Why*: Prevents the AI from guessing prices, inventing trends, or claiming it checked data when it did not.
- **Decision 4: Integrated Voice Interaction (SpeechRecognizer + TextToSpeech)**
  - *Chosen Approach*: Use Android's native `SpeechRecognizer` for voice dictation and `TextToSpeech` with language selection (Hinglish/English/Hindi) for spoken replies.
  - *Why*: Low latency, works seamlessly on Android, requires zero cloud audio billing, and provides immediate voice-to-voice feedback.

***

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           UI LAYER (Jetpack Compose)                        │
│                                                                             │
│  ┌────────────────────┐ ┌────────────────────┐ ┌─────────────────────────┐  │
│  │   DashboardScreen  │ │  MarketChartScreen │ │    AiChatFriendScreen   │  │
│  └─────────┬──────────┘ └──────────┬─────────┘ └────────────┬────────────┘  │
│            │                       │                        │               │
│  ┌─────────┴──────────┐ ┌──────────┴─────────┐ ┌────────────┴────────────┐  │
│  │ ScannerScreen      │ │ RiskCalculatorCard │ │ WatchlistScreen         │  │
│  └────────────────────┘ └────────────────────┘ └─────────────────────────┘  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ StateFlows / Events
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       VIEWMODEL & DOMAIN LOGIC LAYER                        │
│                                                                             │
│  ┌────────────────────────────────┐    ┌─────────────────────────────────┐  │
│  │       MarketViewModel          │    │          AiChatViewModel        │  │
│  │  - Live Polling & Refresh      │    │  - Speech-to-Text & TTS Voice   │  │
│  │  - Active Timeframe & Pair     │    │  - Multilingual Tone Switcher   │  │
│  └────────────────┬───────────────┘    └────────────────┬────────────────┘  │
│                   │                                     │                   │
│                   ▼                                     ▼                   │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                     TechnicalAnalysisEngine                           │  │
│  │  - RSI(14), MACD(12,26,9), EMA(9, 20, 200)                            │  │
│  │  - Swing High/Low Detection & Automatic Trendline Fitting             │  │
│  │  - Signal Generator: LONG, SHORT, or WAIT / NO TRADE                  │  │
│  │  - 1:2 Risk-Reward Target & Invalidation Calculator                   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DATA & PERSISTENCE LAYER                           │
│                                                                             │
│  ┌───────────────────────────────┐     ┌─────────────────────────────────┐  │
│  │     CryptoMarketRepository    │     │      Local Preferences / Room   │  │
│  │  - Binance Public REST Client │     │  - Watchlist & Price Alerts     │  │
│  │  - Retrofit + Moshi Models    │     │  - User Equity & Risk Settings  │  │
│  │  - In-memory Candle Caches    │     │  - Historical Saved Setups      │  │
│  └───────────────────────────────┘     └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Models
- `Candle`: `openTime`, `open`, `high`, `low`, `close`, `volume`, `closeTime`
- `Ticker24h`: `symbol`, `lastPrice`, `priceChange`, `priceChangePercent`, `highPrice`, `lowPrice`, `volume`, `quoteVolume`
- `TechnicalIndicators`: `rsi`, `macd`, `macdSignal`, `macdHist`, `ema9`, `ema20`, `ema200`, `swingHighs`, `swingLows`, `trendlineSupport`, `trendlineResistance`
- `TradeSignal`: `pair`, `currentPrice`, `action` (LONG / SHORT / WAIT), `entryZoneMin`, `entryZoneMax`, `stopLoss`, `takeProfit1`, `takeProfit2`, `riskRewardRatio`, `timeframe`, `timestamp`, `reasons`, `invalidations`, `isExpired`
- `RiskCalculation`: `accountEquity`, `riskPercentage`, `riskCapital`, `entryPrice`, `stopLossPrice`, `positionSizeUnits`, `positionSizeUsdt`, `estimatedLoss`, `leverageWarning`
- `ChatMessage`: `id`, `sender` (USER / AI), `text`, `timestamp`, `attachedSignal`, `isSpeaking`

### Implementation Phasing & Milestones
1. **Foundation & Data Engine**: Network models, Retrofit service for live 24h tickers and klines, and room/preferences storage.
2. **Technical Indicator & Signal Logic**: Deterministic calculations for EMA, RSI, MACD, Swing Lows/Highs, automatic trendline bounce/breakout, and Stop Loss / Take Profit 1:2 targets.
3. **Interactive Charting & Market Dashboard**: Custom Jetpack Compose Canvas candlestick chart with volume bars, moving average overlays, crosshair inspect, timeframe picker, and dashboard overview.
4. **AI Companion & Voice Chat**: Multi-turn conversation UI with quick-action chips, speech-to-text dictation, spoken voice feedback, Hindi/Hinglish/English personality modes, and dedicated signal cards.
5. **Scanner, Risk Calculator & Watchlist**: Multi-pair scanner, position size risk manager, and custom price alerts.
6. **Polishing & Verification**: App icons, strings, edge-to-edge system insets, and Robolectric indicator unit tests.
