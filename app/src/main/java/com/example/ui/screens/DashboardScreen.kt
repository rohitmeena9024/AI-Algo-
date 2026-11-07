package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPair
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.SignalAction
import com.example.data.model.Ticker24h
import com.example.data.model.TradeSignal
import com.example.ui.components.MarketTickerHeader
import com.example.ui.components.SignalCard
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoElectricBlue
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextMuted
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold

@Composable
fun DashboardScreen(
    tickers: Map<String, Ticker24h>,
    bestLongSetup: TradeSignal?,
    bestShortSetup: TradeSignal?,
    selectedPair: CryptoPair,
    onSelectPair: (CryptoPair) -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToRisk: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val btcTicker = tickers["BTCUSDT"]
    val ethTicker = tickers["ETHUSDT"]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .testTag("dashboard_screen")
    ) {
        // Top Live Tickers Bar
        item {
            MarketTickerHeader(
                tickers = tickers,
                selectedPair = selectedPair,
                onSelectPair = onSelectPair
            )
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with App Title & Refresh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Crypto AI Friend",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoTextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Live Trading Signals & AI Companion",
                            fontSize = 12.sp,
                            color = CryptoNeonCyan
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .background(CryptoDarkSurfaceVariant, CircleShape)
                            .testTag("dashboard_btn_refresh")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Market Data",
                            tint = CryptoTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Cards: BTC and ETH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroCryptoCard(
                        symbol = "BTC",
                        displayName = "Bitcoin",
                        ticker = btcTicker,
                        accentColor = CryptoWarningGold,
                        modifier = Modifier.weight(1f).clickable {
                            DEFAULT_CRYPTO_PAIRS.find { it.baseAsset == "BTC" }?.let(onSelectPair)
                            onNavigateToChart("BTC/USDT")
                        }
                    )
                    HeroCryptoCard(
                        symbol = "ETH",
                        displayName = "Ethereum",
                        ticker = ethTicker,
                        accentColor = CryptoElectricBlue,
                        modifier = Modifier.weight(1f).clickable {
                            DEFAULT_CRYPTO_PAIRS.find { it.baseAsset == "ETH" }?.let(onSelectPair)
                            onNavigateToChart("ETH/USDT")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Market Sentiment & AI Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CryptoNeonCyan.copy(alpha = 0.5f), CryptoBorder)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CryptoNeonCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Market Friend Briefing",
                                    color = CryptoTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(CryptoBullishGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Trend: MILD BULLISH",
                                    color = CryptoBullishGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Bhai, market abhi healthy volume ke saath consolidate ho raha hai. BTC $${btcTicker?.lastPrice?.toInt() ?: 76400} ke paas higher lows maintain kar raha hai aur ETH mein EMA bounce active hai. High leverage lene se bacho, aur sirf confirmed 1:2 setups par hi disciplined entry lo!",
                            color = CryptoTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Navigation Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionChip(
                                icon = Icons.Default.Chat,
                                label = "Ask AI Friend",
                                onClick = onNavigateToChat,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionChip(
                                icon = Icons.Default.Radar,
                                label = "Market Scanner",
                                onClick = onNavigateToScanner,
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionChip(
                                icon = Icons.Default.Calculate,
                                label = "Risk Calc",
                                onClick = onNavigateToRisk,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Best Available LONG Setup
                Text(
                    text = "Featured LONG Opportunity",
                    color = CryptoTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                bestLongSetup?.let { signal ->
                    SignalCard(
                        signal = signal,
                        onViewChart = onNavigateToChart,
                        onCalculateRisk = { onNavigateToRisk() },
                        onAskAi = { onNavigateToChat() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Best Available SHORT Setup
                Text(
                    text = "Featured SHORT / Breakdown Setup",
                    color = CryptoTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                bestShortSetup?.let { signal ->
                    SignalCard(
                        signal = signal,
                        onViewChart = onNavigateToChart,
                        onCalculateRisk = { onNavigateToRisk() },
                        onAskAi = { onNavigateToChat() }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun HeroCryptoCard(
    symbol: String,
    displayName: String,
    ticker: Ticker24h?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isPositive = (ticker?.priceChangePercent ?: 0.0) >= 0

    Card(
        modifier = modifier
            .border(1.dp, CryptoBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol.take(1),
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = symbol, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoTextPrimary)
                        Text(text = displayName, fontSize = 10.sp, color = CryptoTextMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPositive) CryptoBullishGreen.copy(alpha = 0.15f) else CryptoBearishRed.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = ticker?.let { String.format("%+.2f%%", it.priceChangePercent) } ?: "--",
                        color = if (isPositive) CryptoBullishGreen else CryptoBearishRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ticker?.let { "$${formatPrice(it.lastPrice)}" } ?: "--",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CryptoTextPrimary,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "24h H: $${ticker?.highPrice?.let { formatPrice(it) } ?: "--"}",
                    fontSize = 9.sp,
                    color = CryptoTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "L: $${ticker?.lowPrice?.let { formatPrice(it) } ?: "--"}",
                    fontSize = 9.sp,
                    color = CryptoTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CryptoDarkSurfaceVariant)
            .border(1.dp, CryptoBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CryptoNeonCyan, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = CryptoTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
}
