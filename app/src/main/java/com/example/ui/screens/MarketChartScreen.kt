package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.CryptoPair
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.TechnicalIndicators
import com.example.data.model.Ticker24h
import com.example.data.model.Timeframe
import com.example.data.model.TradeSignal
import com.example.ui.components.CandlestickChart
import com.example.ui.components.MacdIndicatorPanel
import com.example.ui.components.RsiIndicatorPanel
import com.example.ui.components.SignalCard
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoDarkBackground
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold

@Composable
fun MarketChartScreen(
    pair: CryptoPair,
    timeframe: Timeframe,
    candles: List<Candle>,
    indicators: TechnicalIndicators?,
    ticker: Ticker24h?,
    signal: TradeSignal?,
    isWatchlisted: Boolean,
    onSelectPair: (CryptoPair) -> Unit,
    onSelectTimeframe: (Timeframe) -> Unit,
    onToggleWatchlist: () -> Unit,
    onAskAiAboutPair: () -> Unit,
    onCalculateRisk: (TradeSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmas by remember { mutableStateOf(true) }
    var showTrendline by remember { mutableStateOf(true) }
    var showRsi by remember { mutableStateOf(true) }
    var showMacd by remember { mutableStateOf(true) }

    val isPositive = (ticker?.priceChangePercent ?: 0.0) >= 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .testTag("market_chart_screen")
    ) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Pair Selector and Watchlist Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pair.displayPair,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CryptoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPositive) CryptoBullishGreen.copy(alpha = 0.15f) else CryptoBearishRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ticker?.let { String.format("%+.2f%%", it.priceChangePercent) } ?: "--",
                                    color = if (isPositive) CryptoBullishGreen else CryptoBearishRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = ticker?.let { "$${formatPrice(it.lastPrice)}" } ?: "--",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = CryptoTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onToggleWatchlist,
                            modifier = Modifier
                                .background(CryptoDarkSurfaceVariant, CircleShape)
                                .testTag("btn_toggle_watchlist")
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Watchlist",
                                tint = if (isWatchlisted) CryptoWarningGold else CryptoTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pair Horizontal Switcher
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DEFAULT_CRYPTO_PAIRS) { p ->
                        val isSelected = p.symbol == pair.symbol
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CryptoNeonCyan.copy(alpha = 0.2f) else CryptoDarkSurfaceVariant)
                                .border(1.dp, if (isSelected) CryptoNeonCyan else CryptoBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectPair(p) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = p.baseAsset,
                                color = if (isSelected) CryptoNeonCyan else CryptoTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Timeframe.entries.forEach { tf ->
                        val isSelected = tf == timeframe
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CryptoNeonCyan else CryptoDarkSurface)
                                .clickable { onSelectTimeframe(tf) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tf.label,
                                color = if (isSelected) androidx.compose.ui.graphics.Color.Black else CryptoTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Indicator Toggles Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IndicatorTogglePill(label = "EMAs (9/20/200)", active = showEmas, onToggle = { showEmas = !showEmas })
                    IndicatorTogglePill(label = "Trendline", active = showTrendline, onToggle = { showTrendline = !showTrendline })
                    IndicatorTogglePill(label = "RSI", active = showRsi, onToggle = { showRsi = !showRsi })
                    IndicatorTogglePill(label = "MACD", active = showMacd, onToggle = { showMacd = !showMacd })
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The Master Candlestick Chart
                CandlestickChart(
                    candles = candles,
                    indicators = indicators,
                    activeSignal = signal,
                    showEmas = showEmas,
                    showTrendline = showTrendline,
                    showTradeLevels = true
                )

                // Sub-panels
                if (showRsi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RsiIndicatorPanel(candles = candles)
                }

                if (showMacd) {
                    Spacer(modifier = Modifier.height(8.dp))
                    MacdIndicatorPanel(candles = candles)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Evaluated Trade Signal Card for this Pair & Timeframe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Trading Signal & Setup",
                        color = CryptoTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onAskAiAboutPair,
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoDarkSurfaceVariant, contentColor = CryptoNeonCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ask AI Friend", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                signal?.let { sig ->
                    SignalCard(
                        signal = sig,
                        onViewChart = null, // already on chart
                        onCalculateRisk = onCalculateRisk,
                        onAskAi = { onAskAiAboutPair() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IndicatorTogglePill(
    label: String,
    active: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) CryptoDarkSurfaceVariant else CryptoDarkSurface)
            .border(1.dp, if (active) CryptoNeonCyan.copy(alpha = 0.6f) else CryptoBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (active) CryptoNeonCyan else CryptoTextSecondary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
}
