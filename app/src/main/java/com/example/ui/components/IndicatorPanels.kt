package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.domain.indicators.TechnicalAnalysisEngine
import com.example.ui.theme.CryptoBearishRed
import com.example.ui.theme.CryptoBorder
import com.example.ui.theme.CryptoBullishGreen
import com.example.ui.theme.CryptoDarkSurface
import com.example.ui.theme.CryptoTextMuted
import com.example.ui.theme.CryptoTextPrimary
import com.example.ui.theme.CryptoTextSecondary
import com.example.ui.theme.CryptoWarningGold
import com.example.ui.theme.MacdLineColor
import com.example.ui.theme.MacdSignalColor
import com.example.ui.theme.RsiColor
import kotlin.math.max

@Composable
fun RsiIndicatorPanel(
    candles: List<Candle>,
    modifier: Modifier = Modifier
) {
    if (candles.size < 15) return
    val closes = remember(candles) { candles.map { it.close } }
    val rsiSeries = remember(closes) { TechnicalAnalysisEngine.calculateRsi(closes, 14).takeLast(50) }
    val currentRsi = rsiSeries.lastOrNull() ?: 50.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CryptoDarkSurface, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "RSI (14)",
                    color = CryptoTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1f", currentRsi),
                    color = when {
                        currentRsi >= 70 -> CryptoBearishRed
                        currentRsi <= 30 -> CryptoBullishGreen
                        else -> RsiColor
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = when {
                    currentRsi >= 70 -> "Overbought"
                    currentRsi <= 30 -> "Oversold"
                    else -> "Neutral Range"
                },
                color = CryptoTextSecondary,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                fun rsiToY(v: Double): Float {
                    return (h * (1.0 - (v / 100.0))).toFloat()
                }

                // 70 Line
                val y70 = rsiToY(70.0)
                drawLine(
                    color = CryptoBearishRed.copy(alpha = 0.5f),
                    start = Offset(0f, y70),
                    end = Offset(w, y70),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // 30 Line
                val y30 = rsiToY(30.0)
                drawLine(
                    color = CryptoBullishGreen.copy(alpha = 0.5f),
                    start = Offset(0f, y30),
                    end = Offset(w, y30),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // 50 Center
                val y50 = rsiToY(50.0)
                drawLine(
                    color = CryptoBorder.copy(alpha = 0.4f),
                    start = Offset(0f, y50),
                    end = Offset(w, y50),
                    strokeWidth = 1f
                )

                // RSI Path
                if (rsiSeries.size > 1) {
                    val step = w / (rsiSeries.size - 1)
                    val path = Path()
                    rsiSeries.forEachIndexed { i, valRsi ->
                        val x = i * step
                        val y = rsiToY(valRsi)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = RsiColor,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun MacdIndicatorPanel(
    candles: List<Candle>,
    modifier: Modifier = Modifier
) {
    if (candles.size < 28) return
    val closes = remember(candles) { candles.map { it.close } }
    val ema12 = remember(closes) { TechnicalAnalysisEngine.calculateEma(closes, 12) }
    val ema26 = remember(closes) { TechnicalAnalysisEngine.calculateEma(closes, 26) }
    val macdLineSeries = remember(ema12, ema26) {
        ema12.indices.map { i -> ema12[i] - ema26[i] }.takeLast(50)
    }
    val signalSeries = remember(macdLineSeries) {
        TechnicalAnalysisEngine.calculateEma(macdLineSeries, 9)
    }

    val currentMacd = macdLineSeries.lastOrNull() ?: 0.0
    val currentSignal = signalSeries.lastOrNull() ?: 0.0
    val currentHist = currentMacd - currentSignal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CryptoDarkSurface, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MACD (12, 26, 9)",
                    color = CryptoTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MACD: ${String.format("%.2f", currentMacd)}",
                    color = MacdLineColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SIG: ${String.format("%.2f", currentSignal)}",
                    color = MacdSignalColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "HIST: ${String.format("%+.2f", currentHist)}",
                color = if (currentHist >= 0) CryptoBullishGreen else CryptoBearishRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                val maxAbs = max(
                    0.001,
                    max(
                        macdLineSeries.maxOfOrNull { kotlin.math.abs(it) } ?: 0.1,
                        signalSeries.maxOfOrNull { kotlin.math.abs(it) } ?: 0.1
                    )
                )

                fun macdToY(v: Double): Float {
                    val norm = (v / maxAbs).coerceIn(-1.0, 1.0)
                    return (midY - (norm * (midY * 0.9f))).toFloat()
                }

                // Zero line
                drawLine(
                    color = CryptoBorder.copy(alpha = 0.5f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1f
                )

                // Histogram Bars
                val count = macdLineSeries.size
                if (count > 1) {
                    val barWidth = w / count
                    for (i in 0 until count) {
                        val mVal = macdLineSeries[i]
                        val sVal = signalSeries.getOrNull(i) ?: 0.0
                        val hist = mVal - sVal
                        val barColor = if (hist >= 0) CryptoBullishGreen.copy(alpha = 0.7f) else CryptoBearishRed.copy(alpha = 0.7f)

                        val barY = macdToY(hist)
                        val top = minOf(midY, barY)
                        val bottom = maxOf(midY, barY)
                        drawRect(
                            color = barColor,
                            topLeft = Offset(i * barWidth + (barWidth * 0.15f), top),
                            size = Size(barWidth * 0.7f, max(1f, bottom - top))
                        )
                    }

                    // MACD Line
                    val step = w / (count - 1)
                    val macdPath = Path()
                    macdLineSeries.forEachIndexed { i, v ->
                        val x = i * step
                        val y = macdToY(v)
                        if (i == 0) macdPath.moveTo(x, y) else macdPath.lineTo(x, y)
                    }
                    drawPath(path = macdPath, color = MacdLineColor, style = Stroke(width = 1.8f))

                    // Signal Line
                    val sigPath = Path()
                    signalSeries.forEachIndexed { i, v ->
                        val x = i * step
                        val y = macdToY(v)
                        if (i == 0) sigPath.moveTo(x, y) else sigPath.lineTo(x, y)
                    }
                    drawPath(path = sigPath, color = MacdSignalColor, style = Stroke(width = 1.8f))
                }
            }
        }
    }
}
