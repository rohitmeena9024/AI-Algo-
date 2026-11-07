package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.SignalAction
import com.example.data.model.TradeSignal
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBearishRedGlow
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoBullishGreenGlow
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextMuted
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignalCard(
    signal: TradeSignal,
    onViewChart: ((String) -> Unit)? = null,
    onCalculateRisk: ((TradeSignal) -> Unit)? = null,
    onAskAi: ((TradeSignal) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val actionColor = when (signal.action) {
        SignalAction.LONG -> CryptoBullishGreen
        SignalAction.SHORT -> CryptoBearishRed
        SignalAction.WAIT -> CryptoWarningGold
    }

    val actionGlow = when (signal.action) {
        SignalAction.LONG -> CryptoBullishGreenGlow
        SignalAction.SHORT -> CryptoBearishRedGlow
        SignalAction.WAIT -> CryptoWarningGold.copy(alpha = 0.2f)
    }

    val timeFormatted = remember(signal.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(signal.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, actionColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("signal_card_${signal.pair}"),
        colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Pair, Action Badge, and Timeframe
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
                            .background(actionColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = signal.pair,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(CryptoDarkSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = signal.timeframe,
                            fontSize = 11.sp,
                            color = CryptoNeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action Pill Badge
                Box(
                    modifier = Modifier
                        .background(actionGlow, RoundedCornerShape(20.dp))
                        .border(1.dp, actionColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (signal.action) {
                            SignalAction.LONG -> "LONG ↗"
                            SignalAction.SHORT -> "SHORT ↘"
                            SignalAction.WAIT -> "WAIT / NO TRADE"
                        },
                        color = actionColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price & Targets Grid
            if (signal.action != SignalAction.WAIT) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LevelItem(
                        label = "Entry Zone",
                        value = "$${formatPrice(signal.entryZoneMin)} - $${formatPrice(signal.entryZoneMax)}",
                        valueColor = CryptoNeonCyan
                    )
                    LevelItem(
                        label = "Stop-Loss",
                        value = "$${formatPrice(signal.stopLoss)}",
                        valueColor = CryptoBearishRed
                    )
                    LevelItem(
                        label = "Target (1:2)",
                        value = "$${formatPrice(signal.takeProfit2)}",
                        valueColor = CryptoBullishGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Take Profit 1: $${formatPrice(signal.takeProfit1)} (1:1.5)",
                        fontSize = 11.sp,
                        color = CryptoTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Generated at $timeFormatted",
                        fontSize = 11.sp,
                        color = CryptoTextMuted
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Preserve Capital: Conditions Not Fully Met",
                            color = CryptoWarningGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The market is currently consolidating. Strategy requires confirmed Higher Low bounce or breakout with volume confirmation before taking risk.",
                            color = CryptoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Technical Reasons & Invalidation section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Hide Strategy Analysis" else "View Technical Reasons & Invalidation",
                    color = CryptoNeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CryptoNeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Technical Confirmations:",
                        color = CryptoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    signal.technicalReasons.forEach { reason ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CryptoBullishGreen,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason, color = CryptoTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Invalidation Conditions:",
                        color = CryptoBearishRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    signal.invalidationConditions.forEach { inv ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CryptoBearishRed,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = inv, color = CryptoTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onViewChart?.let { viewChart ->
                    OutlinedButton(
                        onClick = { viewChart(signal.pair) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("signal_btn_chart_${signal.pair}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoNeonCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoBorder))
                    ) {
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chart", fontSize = 11.sp)
                    }
                }

                if (signal.action != SignalAction.WAIT && onCalculateRisk != null) {
                    Button(
                        onClick = { onCalculateRisk(signal) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("signal_btn_risk_${signal.pair}"),
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoDarkSurfaceVariant, contentColor = CryptoTextPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Risk Calc", fontSize = 11.sp)
                    }
                }

                onAskAi?.let { askAi ->
                    Button(
                        onClick = { askAi(signal) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("signal_btn_ask_ai_${signal.pair}"),
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor, contentColor = Color.Black)
                    ) {
                        Text("Ask AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = CryptoTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 10.0) String.format("%.2f", price) else String.format("%.4f", price)
}
