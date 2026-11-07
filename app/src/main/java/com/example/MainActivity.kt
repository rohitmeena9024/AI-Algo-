package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.CryptoDarkSurfaceVariant
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiChatFriendScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketChartScreen
import com.example.ui.screens.MarketScannerScreen
import com.example.ui.screens.RiskCalculatorScreen
import com.example.ui.screens.WatchlistAlertsScreen
import com.example.ui.theme.CryptoAiFriendTheme
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.MarketViewModel

enum class NavigationTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    CHARTS("Markets", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "tab_charts"),
    AI_FRIEND("AI Friend", Icons.Filled.Chat, Icons.Outlined.Chat, "tab_ai_friend"),
    SCANNER("Scanner", Icons.Filled.Radar, Icons.Outlined.Radar, "tab_scanner"),
    RISK("Risk/Watch", Icons.Filled.Shield, Icons.Outlined.Shield, "tab_risk")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CryptoAiFriendTheme {
                CryptoAiFriendApp()
            }
        }
    }
}

@Composable
fun CryptoAiFriendApp(
    marketViewModel: MarketViewModel = viewModel()
) {
    val context = LocalContext.current
    val aiViewModel: AiChatViewModel = remember {
        AiChatViewModel(
            application = context.applicationContext as android.app.Application,
            repository = marketViewModel.getRepository()
        )
    }

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Audio Permission Launcher
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    // Collect Reactive State
    val tickers by marketViewModel.tickers.collectAsState()
    val selectedPair by marketViewModel.selectedPair.collectAsState()
    val selectedTimeframe by marketViewModel.selectedTimeframe.collectAsState()
    val activeCandles by marketViewModel.activeCandles.collectAsState()
    val activeIndicators by marketViewModel.activeIndicators.collectAsState()
    val activeSignal by marketViewModel.activeSignal.collectAsState()
    val scannerItems by marketViewModel.scannerItems.collectAsState()
    val bestLongSetup by marketViewModel.bestLongSetup.collectAsState()
    val bestShortSetup by marketViewModel.bestShortSetup.collectAsState()
    val watchlistItems by marketViewModel.watchlist.collectAsState(initial = emptyList())
    val priceAlerts by marketViewModel.priceAlerts.collectAsState(initial = emptyList())
    val riskResult by marketViewModel.riskResult.collectAsState()
    val selectedProvider by marketViewModel.selectedProvider.collectAsState()

    val chatMessages by aiViewModel.messages.collectAsState()
    val isGenerating by aiViewModel.isGenerating.collectAsState()
    val isVoiceSpeaking by aiViewModel.isVoiceSpeaking.collectAsState()
    val isVoiceListening by aiViewModel.isVoiceListening.collectAsState()
    val selectedTone by aiViewModel.selectedTone.collectAsState()

    val isWatchlisted = remember(watchlistItems, selectedPair) {
        watchlistItems.any { it.symbol == selectedPair.symbol }
    }

    // BackHandler: Return to Dashboard if on any secondary tab
    if (currentTab != NavigationTab.DASHBOARD) {
        BackHandler {
            currentTab = NavigationTab.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CryptoDarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CryptoDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("crypto_bottom_navigation")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CryptoNeonCyan,
                            indicatorColor = CryptoNeonCyan,
                            unselectedIconColor = CryptoTextSecondary,
                            unselectedTextColor = CryptoTextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        tickers = tickers,
                        bestLongSetup = bestLongSetup,
                        bestShortSetup = bestShortSetup,
                        selectedPair = selectedPair,
                        onSelectPair = { pair ->
                            marketViewModel.selectPair(pair)
                        },
                        onNavigateToChart = { pairStr ->
                            currentTab = NavigationTab.CHARTS
                        },
                        onNavigateToChat = {
                            currentTab = NavigationTab.AI_FRIEND
                        },
                        onNavigateToScanner = {
                            currentTab = NavigationTab.SCANNER
                        },
                        onNavigateToRisk = {
                            currentTab = NavigationTab.RISK
                        },
                        onRefresh = {
                            marketViewModel.refreshAll()
                        }
                    )
                }

                NavigationTab.CHARTS -> {
                    MarketChartScreen(
                        pair = selectedPair,
                        timeframe = selectedTimeframe,
                        candles = activeCandles,
                        indicators = activeIndicators,
                        ticker = tickers[selectedPair.symbol],
                        signal = activeSignal,
                        isWatchlisted = isWatchlisted,
                        onSelectPair = { pair ->
                            marketViewModel.selectPair(pair)
                        },
                        onSelectTimeframe = { tf ->
                            marketViewModel.selectTimeframe(tf)
                        },
                        onToggleWatchlist = {
                            marketViewModel.toggleWatchlist(selectedPair)
                        },
                        onAskAiAboutPair = {
                            aiViewModel.sendMessage(
                                query = "Bhai, analyze ${selectedPair.displayPair} on ${selectedTimeframe.label} timeframe. What is the trend, RSI, and entry setup?",
                                activePair = selectedPair,
                                activeTf = selectedTimeframe
                            )
                            currentTab = NavigationTab.AI_FRIEND
                        },
                        onCalculateRisk = { sig ->
                            marketViewModel.calculateRisk(
                                equity = 5000.0,
                                riskPct = 1.5,
                                entry = sig.currentPrice,
                                stopLoss = sig.stopLoss
                            )
                            currentTab = NavigationTab.RISK
                        }
                    )
                }

                NavigationTab.AI_FRIEND -> {
                    AiChatFriendScreen(
                        messages = chatMessages,
                        isGenerating = isGenerating,
                        isVoiceSpeaking = isVoiceSpeaking,
                        isVoiceListening = isVoiceListening,
                        selectedTone = selectedTone,
                        activePair = selectedPair,
                        activeTimeframe = selectedTimeframe,
                        onSendMessage = { q ->
                            aiViewModel.sendMessage(q, selectedPair, selectedTimeframe)
                        },
                        onSpeakMessage = { msg ->
                            aiViewModel.speakMessage(msg)
                        },
                        onStartVoice = {
                            if (hasAudioPermission) {
                                aiViewModel.startVoiceInput(selectedPair, selectedTimeframe)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopVoice = {
                            aiViewModel.stopVoiceInput()
                        },
                        onSelectTone = { tone ->
                            aiViewModel.setTone(tone)
                        },
                        onViewChart = { pairStr ->
                            currentTab = NavigationTab.CHARTS
                        },
                        onCalculateRisk = { sig ->
                            marketViewModel.calculateRisk(
                                equity = 5000.0,
                                riskPct = 1.5,
                                entry = sig.currentPrice,
                                stopLoss = sig.stopLoss
                            )
                            currentTab = NavigationTab.RISK
                        }
                    )
                }

                NavigationTab.SCANNER -> {
                    MarketScannerScreen(
                        scannerItems = scannerItems,
                        onSelectPair = { p ->
                            marketViewModel.selectPair(p)
                        },
                        onNavigateToChart = { pairStr ->
                            currentTab = NavigationTab.CHARTS
                        },
                        onNavigateToChat = { prompt ->
                            aiViewModel.sendMessage(prompt, selectedPair, selectedTimeframe)
                            currentTab = NavigationTab.AI_FRIEND
                        },
                        onRefresh = {
                            marketViewModel.refreshAll()
                        }
                    )
                }

                NavigationTab.RISK -> {
                    var riskSubTab by remember { mutableStateOf(0) }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CryptoDarkSurface)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Risk Calculator", "Watchlist & Alerts").forEachIndexed { index, title ->
                                val isSubSelected = riskSubTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSubSelected) CryptoNeonCyan else CryptoDarkSurfaceVariant,
                                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                        )
                                        .clickable { riskSubTab = index }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSubSelected) Color.Black else CryptoTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (riskSubTab == 0) {
                            RiskCalculatorScreen(
                                riskResult = riskResult,
                                onCalculate = { eq, pct, en, sl ->
                                    marketViewModel.calculateRisk(eq, pct, en, sl)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            WatchlistAlertsScreen(
                                watchlistItems = watchlistItems,
                                priceAlerts = priceAlerts,
                                tickers = tickers,
                                selectedProvider = selectedProvider,
                                onSelectProvider = { p -> marketViewModel.setExchangeProvider(p) },
                                onSelectPair = { p -> marketViewModel.selectPair(p) },
                                onNavigateToChart = { currentTab = NavigationTab.CHARTS },
                                onRemoveFromWatchlist = { p -> marketViewModel.toggleWatchlist(p) },
                                onAddAlert = { sym, price, above -> marketViewModel.addPriceAlert(sym, price, above) },
                                onDeleteAlert = { a -> marketViewModel.deletePriceAlert(a) },
                                onUpdateApiUrl = { url -> marketViewModel.updateBaseUrl(url) },
                                onUpdateApiKey = { key -> marketViewModel.updateApiKey(key) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
