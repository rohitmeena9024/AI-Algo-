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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PriceAlert
import com.example.data.local.WatchlistItem
import com.example.data.model.CryptoPair
import com.example.data.model.DEFAULT_CRYPTO_PAIRS
import com.example.data.model.Ticker24h
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

import com.example.data.repository.ExchangeProvider

@Composable
fun WatchlistAlertsScreen(
    watchlistItems: List<WatchlistItem>,
    priceAlerts: List<PriceAlert>,
    tickers: Map<String, Ticker24h>,
    selectedProvider: ExchangeProvider = ExchangeProvider.DELTA_EXCHANGE,
    onSelectProvider: (ExchangeProvider) -> Unit = {},
    onSelectPair: (CryptoPair) -> Unit,
    onNavigateToChart: (String) -> Unit,
    onRemoveFromWatchlist: (CryptoPair) -> Unit,
    onAddAlert: (symbol: String, targetPrice: Double, isAbove: Boolean) -> Unit,
    onDeleteAlert: (PriceAlert) -> Unit,
    onUpdateApiUrl: (String) -> Unit,
    onUpdateApiKey: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var alertSymbol by remember { mutableStateOf("BTCUSDT") }
    var targetPriceText by remember { mutableStateOf("") }
    var isAbove by remember { mutableStateOf(true) }
    var customApiUrl by remember { mutableStateOf("https://api.delta.exchange/") }
    var customApiKey by remember { mutableStateOf("") }
    var customApiSecret by remember { mutableStateOf("") }
    var showApiSettings by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .padding(16.dp)
            .testTag("watchlist_alerts_screen")
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CryptoWarningGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Watchlist & Alerts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CryptoTextPrimary
                    )
                }

                IconButton(onClick = { showApiSettings = !showApiSettings }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = CryptoTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showApiSettings) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoBorder))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Exchange Provider", color = CryptoNeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExchangeProvider.entries.forEach { provider ->
                                val isSelected = provider == selectedProvider
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CryptoNeonCyan else CryptoDarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) CryptoNeonCyan else CryptoBorder, RoundedCornerShape(8.dp))
                                        .clickable { onSelectProvider(provider) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isSelected) "${provider.displayName} ✓" else provider.displayName,
                                        color = if (isSelected) Color.Black else CryptoTextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (selectedProvider == ExchangeProvider.DELTA_EXCHANGE) "Delta Exchange API Key" else "Binance API Key",
                            color = CryptoTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customApiKey,
                            onValueChange = { customApiKey = it },
                            placeholder = { Text("Enter API Key", fontSize = 11.sp, color = CryptoTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoNeonCyan,
                                unfocusedBorderColor = CryptoBorder,
                                focusedTextColor = CryptoTextPrimary,
                                unfocusedTextColor = CryptoTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (selectedProvider == ExchangeProvider.DELTA_EXCHANGE) "Delta Exchange API Secret" else "API Secret",
                            color = CryptoTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customApiSecret,
                            onValueChange = { customApiSecret = it },
                            placeholder = { Text("Enter API Secret", fontSize = 11.sp, color = CryptoTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoNeonCyan,
                                unfocusedBorderColor = CryptoBorder,
                                focusedTextColor = CryptoTextPrimary,
                                unfocusedTextColor = CryptoTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Custom Base URL", color = CryptoTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customApiUrl,
                            onValueChange = { customApiUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoNeonCyan,
                                unfocusedBorderColor = CryptoBorder,
                                focusedTextColor = CryptoTextPrimary,
                                unfocusedTextColor = CryptoTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onUpdateApiUrl(customApiUrl)
                                if (customApiKey.isNotBlank()) {
                                    onUpdateApiKey(customApiKey)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CryptoNeonCyan, contentColor = Color.Black)
                        ) {
                            Text("Save Exchange Configuration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Pinned Watchlist Section
        item {
            Text(
                text = "Pinned Favorites",
                color = CryptoTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (watchlistItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pinned coins yet. Tap the star icon on any chart screen to add to your watchlist!",
                        color = CryptoTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(watchlistItems) { item ->
                val pair = DEFAULT_CRYPTO_PAIRS.find { it.symbol == item.symbol }
                val ticker = tickers[item.symbol]
                val isPositive = (ticker?.priceChangePercent ?: 0.0) >= 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, CryptoBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            pair?.let {
                                onSelectPair(it)
                                onNavigateToChart(it.displayPair)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = pair?.displayPair ?: item.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CryptoTextPrimary)
                            Text(text = pair?.displayName ?: "", fontSize = 11.sp, color = CryptoTextSecondary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = ticker?.let { "$${formatPrice(it.lastPrice)}" } ?: "--",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoTextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = ticker?.let { String.format("%+.2f%%", it.priceChangePercent) } ?: "--",
                                    fontSize = 11.sp,
                                    color = if (isPositive) CryptoBullishGreen else CryptoBearishRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(onClick = { pair?.let(onRemoveFromWatchlist) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = CryptoTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Price Alert Creation Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Set Price Alert",
                color = CryptoTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Target Price Trigger ($)", fontSize = 11.sp, color = CryptoTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = targetPriceText,
                        onValueChange = { targetPriceText = it },
                        placeholder = { Text("e.g. 78000", color = CryptoTextSecondary, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CryptoNeonCyan,
                            unfocusedBorderColor = CryptoBorder,
                            focusedTextColor = CryptoTextPrimary,
                            unfocusedTextColor = CryptoTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAbove) CryptoBullishGreen.copy(alpha = 0.2f) else CryptoDarkSurfaceVariant)
                                .border(1.dp, if (isAbove) CryptoBullishGreen else CryptoBorder, RoundedCornerShape(8.dp))
                                .clickable { isAbove = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Crosses Above ↗", color = if (isAbove) CryptoBullishGreen else CryptoTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isAbove) CryptoBearishRed.copy(alpha = 0.2f) else CryptoDarkSurfaceVariant)
                                .border(1.dp, if (!isAbove) CryptoBearishRed else CryptoBorder, RoundedCornerShape(8.dp))
                                .clickable { isAbove = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Crosses Below ↘", color = if (!isAbove) CryptoBearishRed else CryptoTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val price = targetPriceText.toDoubleOrNull()
                            if (price != null && price > 0) {
                                onAddAlert(alertSymbol, price, isAbove)
                                targetPriceText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoNeonCyan, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Price Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Alerts List
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Active Notifications & Triggers",
                color = CryptoTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (priceAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No active price alerts set.", color = CryptoTextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            items(priceAlerts) { alert ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (alert.isAbove) CryptoBullishGreen else CryptoBearishRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = alert.symbol,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoTextPrimary
                                )
                                Text(
                                    text = if (alert.isAbove) "Alert when price ≥ $${formatPrice(alert.targetPrice)}" else "Alert when price ≤ $${formatPrice(alert.targetPrice)}",
                                    fontSize = 11.sp,
                                    color = CryptoTextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteAlert(alert) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CryptoBearishRed)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
}
