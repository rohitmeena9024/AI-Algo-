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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoPair
import com.example.data.model.SignalAction
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
import com.example.ui.viewmodel.ScannerItem

enum class ScannerFilter(val label: String) {
    ALL("All Pairs"),
    BULLISH("Bullish / LONG"),
    BEARISH("Bearish / SHORT"),
    BREAKOUTS("Breakouts"),
    WAIT("Consolidation / WAIT")
}

@Composable
fun MarketScannerScreen(
    scannerItems: List<ScannerItem>,
    onSelectPair: (CryptoPair) -> Unit,
    onNavigateToChart: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ScannerFilter.ALL) }

    val filteredItems = remember(scannerItems, selectedFilter) {
        when (selectedFilter) {
            ScannerFilter.ALL -> scannerItems
            ScannerFilter.BULLISH -> scannerItems.filter { it.signal.action == SignalAction.LONG }
            ScannerFilter.BEARISH -> scannerItems.filter { it.signal.action == SignalAction.SHORT }
            ScannerFilter.BREAKOUTS -> scannerItems.filter { it.trend.contains("BULLISH") || it.rsi > 60.0 }
            ScannerFilter.WAIT -> scannerItems.filter { it.signal.action == SignalAction.WAIT }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .testTag("market_scanner_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CryptoDarkSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Market Scanner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CryptoTextPrimary
                    )
                    Text(
                        text = "Real-time evaluation across 8 top cryptocurrencies",
                        fontSize = 11.sp,
                        color = CryptoNeonCyan
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.background(CryptoDarkSurfaceVariant, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CryptoTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ScannerFilter.entries) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CryptoNeonCyan else CryptoDarkSurfaceVariant)
                            .border(1.dp, if (isSelected) CryptoNeonCyan else CryptoBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) Color.Black else CryptoTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Scanner List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems) { item ->
                ScannerCard(
                    item = item,
                    onOpenChart = {
                        onSelectPair(item.pair)
                        onNavigateToChart(item.pair.displayPair)
                    },
                    onAskAi = {
                        onSelectPair(item.pair)
                        onNavigateToChat("Analyze ${item.pair.baseAsset} setup")
                    }
                )
            }
        }
    }
}

@Composable
private fun ScannerCard(
    item: ScannerItem,
    onOpenChart: () -> Unit,
    onAskAi: () -> Unit
) {
    val actionColor = when (item.signal.action) {
        SignalAction.LONG -> CryptoBullishGreen
        SignalAction.SHORT -> CryptoBearishRed
        SignalAction.WAIT -> CryptoWarningGold
    }

    val isPositive = item.ticker.priceChangePercent >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CryptoBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.pair.displayPair,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.pair.displayName,
                        fontSize = 11.sp,
                        color = CryptoTextSecondary
                    )
                }

                // Action Pill
                Box(
                    modifier = Modifier
                        .background(actionColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, actionColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (item.signal.action) {
                            SignalAction.LONG -> "LONG"
                            SignalAction.SHORT -> "SHORT"
                            SignalAction.WAIT -> "WAIT"
                        },
                        color = actionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${formatPrice(item.ticker.lastPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CryptoTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%+.2f%% (24h)", item.ticker.priceChangePercent),
                        fontSize = 11.sp,
                        color = if (isPositive) CryptoBullishGreen else CryptoBearishRed,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RSI: ${String.format("%.1f", item.rsi)}",
                        fontSize = 11.sp,
                        color = CryptoTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Trend: ${item.trend}",
                        fontSize = 10.sp,
                        color = CryptoNeonCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoDarkSurfaceVariant)
                        .clickable(onClick = onOpenChart)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = CryptoNeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "View Chart", fontSize = 11.sp, color = CryptoTextPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CryptoNeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, CryptoNeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onAskAi)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = CryptoNeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ask AI Setup", fontSize = 11.sp, color = CryptoNeonCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
}
