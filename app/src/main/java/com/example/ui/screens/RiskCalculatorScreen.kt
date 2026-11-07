package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PositionRiskResult
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
fun RiskCalculatorScreen(
    riskResult: PositionRiskResult?,
    onCalculate: (equity: Double, riskPct: Double, entry: Double, stopLoss: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var equityText by remember { mutableStateOf("5000") }
    var riskPct by remember { mutableDoubleStateOf(1.5) }
    var entryText by remember { mutableStateOf("76400") }
    var stopLossText by remember { mutableStateOf("75200") }

    val scrollState = rememberScrollState()

    fun triggerCalc() {
        val eq = equityText.toDoubleOrNull() ?: 5000.0
        val en = entryText.toDoubleOrNull() ?: 76400.0
        val sl = stopLossText.toDoubleOrNull() ?: 75200.0
        onCalculate(eq, riskPct, en, sl)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CryptoDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("risk_calculator_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CryptoNeonCyan, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Risk & Position Size Calculator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CryptoTextPrimary
                )
                Text(
                    text = "Capital Preservation & Leverage Safeguards",
                    fontSize = 11.sp,
                    color = CryptoTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Account Balance Input
                Text(text = "Total Account Equity ($ USDT)", fontSize = 12.sp, color = CryptoTextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = equityText,
                    onValueChange = {
                        equityText = it
                        triggerCalc()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_equity"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CryptoNeonCyan,
                        unfocusedBorderColor = CryptoBorder,
                        focusedTextColor = CryptoTextPrimary,
                        unfocusedTextColor = CryptoTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Risk Percentage Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Risk Capital Per Trade", fontSize = 12.sp, color = CryptoTextSecondary, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${String.format("%.1f", riskPct)}% of Equity",
                        fontSize = 12.sp,
                        color = CryptoNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = riskPct.toFloat(),
                    onValueChange = {
                        riskPct = it.toDouble()
                        triggerCalc()
                    },
                    valueRange = 0.5f..5.0f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = CryptoNeonCyan,
                        activeTrackColor = CryptoNeonCyan,
                        inactiveTrackColor = CryptoBorder
                    ),
                    modifier = Modifier.testTag("slider_risk_pct")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Entry and Stop Loss Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Entry Price ($)", fontSize = 11.sp, color = CryptoTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = entryText,
                            onValueChange = {
                                entryText = it
                                triggerCalc()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_entry"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoNeonCyan,
                                unfocusedBorderColor = CryptoBorder,
                                focusedTextColor = CryptoTextPrimary,
                                unfocusedTextColor = CryptoTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Stop-Loss Price ($)", fontSize = 11.sp, color = CryptoTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = stopLossText,
                            onValueChange = {
                                stopLossText = it
                                triggerCalc()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_stop_loss"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CryptoBearishRed,
                                unfocusedBorderColor = CryptoBorder,
                                focusedTextColor = CryptoTextPrimary,
                                unfocusedTextColor = CryptoTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculation Results Card
        riskResult?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CryptoDarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CryptoNeonCyan.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calculated Position Metrics",
                        color = CryptoNeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ResultRow(
                        label = "Max Capital at Risk (Loss if SL hits)",
                        value = "$${String.format("%.2f", res.riskCapitalUsdt)}",
                        valueColor = CryptoBearishRed
                    )

                    ResultRow(
                        label = "Distance to Stop-Loss",
                        value = "${String.format("%.2f", res.stopLossDistancePercent)}%",
                        valueColor = CryptoTextPrimary
                    )

                    ResultRow(
                        label = "Recommended Position Size ($ USDT)",
                        value = "$${String.format("%.2f", res.positionSizeUsdt)}",
                        valueColor = CryptoBullishGreen
                    )

                    ResultRow(
                        label = "Position Size (Crypto Units)",
                        value = "${String.format("%.4f", res.positionSizeUnits)}",
                        valueColor = CryptoTextPrimary
                    )

                    ResultRow(
                        label = "Required Leverage",
                        value = "${res.suggestedLeverage}x",
                        valueColor = if (res.suggestedLeverage > 5) CryptoWarningGold else CryptoBullishGreen
                    )

                    // Leverage Caution Banner
                    res.leverageWarning?.let { warn ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CryptoBearishRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(1.dp, CryptoBearishRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CryptoBearishRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = warn,
                                    color = CryptoBearishRed,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safety Disclaimer Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "Professional Risk Rule",
                    color = CryptoWarningGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Never risk more than 1% to 2% of total equity on any single trade. Crypto AI Friend does not place real automated trades. Always execute your own orders with an active stop-loss.",
                    color = CryptoTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ResultRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = CryptoTextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
