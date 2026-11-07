package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.TechnicalIndicators
import com.example.data.model.TradeSignal
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoDarkSurfaceVariant
import com.example.ui.theme.CryptoNeonCyan
import com.example.ui.theme.CryptoTextMuted
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold
import com.example.ui.theme.Ema200Color
import com.example.ui.theme.Ema20Color
import com.example.ui.theme.Ema9Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    indicators: TechnicalIndicators?,
    activeSignal: TradeSignal? = null,
    showEmas: Boolean = true,
    showTrendline: Boolean = true,
    showTradeLevels: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(CryptoDarkSurfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading chart data...",
                color = CryptoTextSecondary,
                fontSize = 13.sp
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val displayCandles = remember(candles) { candles.takeLast(50) }
    val selectedCandle = selectedIndex?.let { idx -> displayCandles.getOrNull(idx) } ?: displayCandles.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CryptoDarkSurface, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Top Legend & Inspector Bar
        selectedCandle?.let { c ->
            val isBullish = c.close >= c.open
            val dateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(c.openTime)),
                    color = CryptoTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "O: ${formatNumber(c.open)}", fontSize = 11.sp, color = CryptoTextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = "H: ${formatNumber(c.high)}", fontSize = 11.sp, color = CryptoBullishGreen, fontFamily = FontFamily.Monospace)
                    Text(text = "L: ${formatNumber(c.low)}", fontSize = 11.sp, color = CryptoBearishRed, fontFamily = FontFamily.Monospace)
                    Text(
                        text = "C: ${formatNumber(c.close)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBullish) CryptoBullishGreen else CryptoBearishRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Indicator Toggle Chips & Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showEmas) {
                IndicatorLegendItem(color = Ema9Color, label = "EMA 9")
                IndicatorLegendItem(color = Ema20Color, label = "EMA 20")
                IndicatorLegendItem(color = Ema200Color, label = "EMA 200")
            }
            if (showTrendline && indicators?.activeTrendline != null) {
                IndicatorLegendItem(
                    color = if (indicators.activeTrendline.isSupport) CryptoBullishGreen else CryptoBearishRed,
                    label = if (indicators.activeTrendline.isSupport) "Support Trendline" else "Resistance Trendline"
                )
            }
        }

        // Candlestick & Overlays Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .testTag("candlestick_chart_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(displayCandles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val candleWidth = size.width / displayCandles.size
                                val idx = (offset.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                                selectedIndex = idx
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val candleWidth = size.width / displayCandles.size
                                val idx = (change.position.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                                selectedIndex = idx
                            },
                            onDragEnd = { selectedIndex = null },
                            onDragCancel = { selectedIndex = null }
                        )
                    }
                    .pointerInput(displayCandles) {
                        detectTapGestures(
                            onTap = { offset ->
                                val candleWidth = size.width / displayCandles.size
                                val idx = (offset.x / candleWidth).toInt().coerceIn(0, displayCandles.size - 1)
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        )
                    }
            ) {
                val chartWidth = size.width
                val totalHeight = size.height
                val priceHeight = totalHeight * 0.78f
                val volumeHeight = totalHeight * 0.20f
                val volumeTop = totalHeight * 0.80f

                val n = displayCandles.size
                if (n == 0) return@Canvas

                val minPrice = displayCandles.minOf { it.low } * 0.998
                val maxPrice = displayCandles.maxOf { it.high } * 1.002
                val priceRange = max(0.00001, maxPrice - minPrice)

                val maxVolume = max(1.0, displayCandles.maxOf { it.volume })

                val candleSlotWidth = chartWidth / n
                val candleBodyWidth = max(2f, candleSlotWidth * 0.65f)

                fun priceToY(price: Double): Float {
                    val normalized = (price - minPrice) / priceRange
                    return (priceHeight * (1.0 - normalized)).toFloat()
                }

                fun volumeToY(vol: Double): Float {
                    val normalized = (vol / maxVolume).coerceIn(0.0, 1.0)
                    return (volumeTop + (volumeHeight * (1.0 - normalized))).toFloat()
                }

                // 1. Draw Subtle Horizontal Grid Lines
                val gridLevels = 4
                for (g in 0..gridLevels) {
                    val gridY = (priceHeight / gridLevels) * g
                    drawLine(
                        color = CryptoBorder.copy(alpha = 0.5f),
                        start = Offset(0f, gridY),
                        end = Offset(chartWidth, gridY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // 2. Draw Volume Bars
                displayCandles.forEachIndexed { i, c ->
                    val x = (i * candleSlotWidth) + (candleSlotWidth / 2f)
                    val isBullish = c.close >= c.open
                    val volY = volumeToY(c.volume)
                    drawRect(
                        color = if (isBullish) CryptoBullishGreen.copy(alpha = 0.35f) else CryptoBearishRed.copy(alpha = 0.35f),
                        topLeft = Offset(x - (candleBodyWidth / 2f), volY),
                        size = Size(candleBodyWidth, totalHeight - volY)
                    )
                }

                // 3. Draw Candlesticks (Wicks + Bodies)
                displayCandles.forEachIndexed { i, c ->
                    val x = (i * candleSlotWidth) + (candleSlotWidth / 2f)
                    val isBullish = c.close >= c.open
                    val candleColor = if (isBullish) CryptoBullishGreen else CryptoBearishRed

                    val highY = priceToY(c.high)
                    val lowY = priceToY(c.low)
                    val openY = priceToY(c.open)
                    val closeY = priceToY(c.close)

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.8f
                    )

                    // Body
                    val bodyTop = min(openY, closeY)
                    val bodyBottom = max(openY, closeY)
                    val bodyH = max(2f, bodyBottom - bodyTop)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - (candleBodyWidth / 2f), bodyTop),
                        size = Size(candleBodyWidth, bodyH)
                    )
                }

                // 4. Draw EMAs
                if (showEmas && displayCandles.size >= 9) {
                    drawEmaLine(displayCandles, 9, Ema9Color, ::priceToY, candleSlotWidth)
                    drawEmaLine(displayCandles, 20, Ema20Color, ::priceToY, candleSlotWidth)
                    drawEmaLine(displayCandles, 50, Ema200Color, ::priceToY, candleSlotWidth)
                }

                // 5. Draw Trendline
                if (showTrendline && indicators?.activeTrendline != null) {
                    val tl = indicators.activeTrendline
                    // Project across current visible slice
                    val offsetIndex = candles.size - displayCandles.size
                    val visibleStart = tl.startIndex - offsetIndex
                    val visibleEnd = tl.endIndex - offsetIndex

                    if (visibleStart < displayCandles.size) {
                        val x1 = (max(0, visibleStart) * candleSlotWidth) + (candleSlotWidth / 2f)
                        val y1 = priceToY(tl.startPrice)
                        val x2 = chartWidth
                        val projectedEndPrice = tl.endPrice + tl.slope * (displayCandles.size - 1 - visibleEnd)
                        val y2 = priceToY(projectedEndPrice)

                        val tlColor = if (tl.isSupport) CryptoBullishGreen else CryptoBearishRed
                        drawLine(
                            color = tlColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    }
                }

                // 6. Draw Signal Levels (Entry, Stop Loss, Target TP2)
                if (showTradeLevels && activeSignal != null && activeSignal.action.name != "WAIT") {
                    val entryY = priceToY(activeSignal.currentPrice)
                    val slY = priceToY(activeSignal.stopLoss)
                    val tpY = priceToY(activeSignal.takeProfit2)

                    // Entry Line (Cyan)
                    drawLine(
                        color = CryptoNeonCyan,
                        start = Offset(0f, entryY),
                        end = Offset(chartWidth, entryY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                    )

                    // Stop Loss Line (Red)
                    drawLine(
                        color = CryptoBearishRed,
                        start = Offset(0f, slY),
                        end = Offset(chartWidth, slY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )

                    // Take Profit 2 Line (Green)
                    drawLine(
                        color = CryptoBullishGreen,
                        start = Offset(0f, tpY),
                        end = Offset(chartWidth, tpY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // 7. Interactive Crosshair Cursor
                selectedIndex?.let { idx ->
                    val crossX = (idx * candleSlotWidth) + (candleSlotWidth / 2f)
                    val c = displayCandles[idx]
                    val crossY = priceToY(c.close)

                    // Vertical Line
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, totalHeight),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Horizontal Line
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(0f, crossY),
                        end = Offset(chartWidth, crossY),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Dot at price
                    drawCircle(
                        color = CryptoNeonCyan,
                        radius = 4f,
                        center = Offset(crossX, crossY)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEmaLine(
    candles: List<Candle>,
    period: Int,
    color: Color,
    priceToY: (Double) -> Float,
    slotWidth: Float
) {
    if (candles.size < period) return
    val closes = candles.map { it.close }
    val k = 2.0 / (period + 1.0)
    var ema = closes.take(period).average()

    val path = Path()
    var started = false

    closes.forEachIndexed { i, close ->
        if (i >= period) {
            ema = (close * k) + (ema * (1.0 - k))
            val x = (i * slotWidth) + (slotWidth / 2f)
            val y = priceToY(ema)
            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }
    }

    if (started) {
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.8f)
        )
    }
}

@Composable
private fun IndicatorLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(3.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = CryptoTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatNumber(value: Double): String {
    return if (value >= 10.0) String.format("%.2f", value) else String.format("%.4f", value)
}
