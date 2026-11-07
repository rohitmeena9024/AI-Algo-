package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CryptoDatabase
import com.example.data.local.PriceAlert
import com.example.data.local.WatchlistItem
import com.example.data.model.Candle
import com.example.data.model.CryptoPair
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.PositionRiskResult
import com.example.data.model.SignalAction
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Ticker24h
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import com.example.data.repository.CryptoMarketRepository
import com.example.domain.indicators.TechnicalAnalysisEngine
import com.example.domain.risk.RiskCalculator
import com.example.domain.signals.SignalEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ScannerItem(
    val pair: CryptoPair,
    val ticker: Ticker24h,
    val signal: TradeSignal,
    val rsi: Double,
    val trend: String
)

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CryptoMarketRepository()
    private val database = CryptoDatabase.getDatabase(application)
    private val dao = database.cryptoDao()

    // 1. Tickers and Market Pulse
    private val _tickers = MutableStateFlow<Map<String, Ticker24h>>(emptyMap())
    val tickers: StateFlow<Map<String, Ticker24h>> = _tickers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 2. Active Chart State
    private val _selectedPair = MutableStateFlow(DEFAULT_CRYPTO_PAIRS.first())
    val selectedPair: StateFlow<CryptoPair> = _selectedPair.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.M15)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _activeCandles = MutableStateFlow<List<Candle>>(emptyList())
    val activeCandles: StateFlow<List<Candle>> = _activeCandles.asStateFlow()

    private val _activeIndicators = MutableStateFlow<TechnicalIndicators?>(null)
    val activeIndicators: StateFlow<TechnicalIndicators?> = _activeIndicators.asStateFlow()

    private val _activeSignal = MutableStateFlow<TradeSignal?>(null)
    val activeSignal: StateFlow<TradeSignal?> = _activeSignal.asStateFlow()

    // 3. Multi-Pair Scanner State
    private val _scannerItems = MutableStateFlow<List<ScannerItem>>(emptyList())
    val scannerItems: StateFlow<List<ScannerItem>> = _scannerItems.asStateFlow()

    // 4. Best Available Setups for Dashboard
    private val _bestLongSetup = MutableStateFlow<TradeSignal?>(null)
    val bestLongSetup: StateFlow<TradeSignal?> = _bestLongSetup.asStateFlow()

    private val _bestShortSetup = MutableStateFlow<TradeSignal?>(null)
    val bestShortSetup: StateFlow<TradeSignal?> = _bestShortSetup.asStateFlow()

    // 5. Watchlist & Alerts
    val watchlist = dao.getWatchlist()
    val priceAlerts = dao.getPriceAlerts()

    // 6. Risk Calculator State
    private val _riskResult = MutableStateFlow<PositionRiskResult?>(null)
    val riskResult: StateFlow<PositionRiskResult?> = _riskResult.asStateFlow()

    private var pollingJob: Job? = null

    init {
        loadInitialData()
        startPeriodicPolling()
        calculateRisk(equity = 5000.0, riskPct = 1.5, entry = 76400.0, stopLoss = 75200.0)
    }

    fun selectPair(pair: CryptoPair) {
        _selectedPair.value = pair
        refreshActiveChartAndSignal()
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
        refreshActiveChartAndSignal()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchTickers()
            refreshActiveChartAndSignal()
            scanAllPairs()
            _isLoading.value = false
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchTickers()
            refreshActiveChartAndSignal()
            scanAllPairs()
            _isLoading.value = false
        }
    }

    private fun startPeriodicPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(12_000L) // Poll market pulse every 12 seconds
                fetchTickers()
            }
        }
    }

    private suspend fun fetchTickers() {
        val result = repository.fetch24hTickers()
        result.onSuccess { list ->
            _tickers.value = list.associateBy { it.symbol }
            _errorMessage.value = null
        }.onFailure { err ->
            _errorMessage.value = err.message
        }
    }

    fun refreshActiveChartAndSignal() {
        val pair = _selectedPair.value
        val tf = _selectedTimeframe.value
        viewModelScope.launch {
            val res = repository.fetchCandles(pair.symbol, tf, limit = 120)
            res.onSuccess { candles ->
                _activeCandles.value = candles
                val indicators = TechnicalAnalysisEngine.analyze(candles)
                _activeIndicators.value = indicators
                val signal = SignalEngine.generateSignal(pair.displayPair, tf, indicators, candles)
                _activeSignal.value = signal
            }.onFailure { err ->
                _errorMessage.value = "Failed to load candles: ${err.message}"
            }
        }
    }

    private suspend fun scanAllPairs() {
        val currentTf = _selectedTimeframe.value
        val items = mutableListOf<ScannerItem>()

        var foundLong: TradeSignal? = null
        var foundShort: TradeSignal? = null

        DEFAULT_CRYPTO_PAIRS.forEach { pair ->
            val ticker = _tickers.value[pair.symbol] ?: repository.getCachedTicker(pair.symbol)
            val candleRes = repository.fetchCandles(pair.symbol, currentTf, limit = 60)
            val candles = candleRes.getOrNull() ?: emptyList()
            if (candles.isNotEmpty() && ticker != null) {
                val indicators = TechnicalAnalysisEngine.analyze(candles)
                val signal = SignalEngine.generateSignal(pair.displayPair, currentTf, indicators, candles)

                items.add(
                    ScannerItem(
                        pair = pair,
                        ticker = ticker,
                        signal = signal,
                        rsi = indicators.rsi,
                        trend = indicators.trendBias
                    )
                )

                if (foundLong == null && signal.action == SignalAction.LONG) {
                    foundLong = signal
                }
                if (foundShort == null && signal.action == SignalAction.SHORT) {
                    foundShort = signal
                }
            }
        }

        _scannerItems.value = items
        _bestLongSetup.value = foundLong ?: items.firstOrNull()?.signal
        _bestShortSetup.value = foundShort ?: items.lastOrNull()?.signal
    }

    fun calculateRisk(equity: Double, riskPct: Double, entry: Double, stopLoss: Double) {
        val result = RiskCalculator.calculate(
            accountEquity = equity,
            riskPercentage = riskPct,
            entryPrice = entry,
            stopLossPrice = stopLoss
        )
        _riskResult.value = result
    }

    fun toggleWatchlist(pair: CryptoPair) {
        viewModelScope.launch {
            val exists = dao.isInWatchlist(pair.symbol)
            if (exists) {
                dao.removeBySymbol(pair.symbol)
            } else {
                dao.addToWatchlist(WatchlistItem(pair.symbol, pair.displayName))
            }
        }
    }

    fun addPriceAlert(symbol: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            dao.insertAlert(
                PriceAlert(
                    symbol = symbol,
                    targetPrice = targetPrice,
                    isAbove = isAbove
                )
            )
        }
    }

    fun deletePriceAlert(alert: PriceAlert) {
        viewModelScope.launch {
            dao.deleteAlert(alert)
        }
    }

    private val _selectedProvider = MutableStateFlow(repository.currentProvider)
    val selectedProvider: StateFlow<com.example.data.repository.ExchangeProvider> = _selectedProvider.asStateFlow()

    fun setExchangeProvider(provider: com.example.data.repository.ExchangeProvider) {
        repository.setProvider(provider)
        _selectedProvider.value = provider
        refreshAll()
    }

    fun updateDeltaApiKey(newKey: String) {
        repository.updateDeltaApiKey(newKey)
        refreshAll()
    }

    fun updateApiKey(newKey: String) {
        repository.updateDeltaApiKey(newKey)
        repository.updateBinanceApiKey(newKey)
        refreshAll()
    }

    fun updateBaseUrl(newUrl: String) {
        repository.updateBaseUrl(newUrl)
        refreshAll()
    }

    fun getRepository(): CryptoMarketRepository = repository

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
