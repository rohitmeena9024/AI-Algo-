package com.example.domain.risk

import com.example.data.model.PositionRiskResult
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

object RiskCalculator {

    fun calculate(
        accountEquity: Double,
        riskPercentage: Double,
        entryPrice: Double,
        stopLossPrice: Double
    ): PositionRiskResult {
        val safeEquity = max(1.0, accountEquity)
        val safeRiskPct = riskPercentage.coerceIn(0.1, 10.0)
        val safeEntry = max(0.000001, entryPrice)
        val safeSl = max(0.000001, stopLossPrice)

        val riskCapitalUsdt = safeEquity * (safeRiskPct / 100.0)
        val priceDiff = abs(safeEntry - safeSl)
        val slDistancePct = (priceDiff / safeEntry) * 100.0

        val positionSizeUsdt = if (slDistancePct > 0.05) {
            (riskCapitalUsdt / (slDistancePct / 100.0))
        } else {
            safeEquity
        }

        val positionSizeUnits = positionSizeUsdt / safeEntry
        val estimatedLossUsdt = riskCapitalUsdt

        val rawLeverage = positionSizeUsdt / safeEquity
        val suggestedLeverage = ceil(rawLeverage).toInt().coerceAtLeast(1)

        val warning = when {
            suggestedLeverage > 20 -> "EXTREME LEVERAGE HAZARD: ${suggestedLeverage}x leverage leaves less than ${(100.0 / suggestedLeverage).toInt()}% room for liquidation! High risk of total account wipeout on sudden market spikes."
            suggestedLeverage > 5 -> "High Leverage Warning: Requires ${suggestedLeverage}x leverage. Watch out for funding fees, slippage, and spread during high volatility."
            slDistancePct > 8.0 -> "Wide Stop-Loss Warning: Stop loss distance is ${String.format("%.1f", slDistancePct)}%. Consider waiting for a tighter pullback entry."
            else -> null
        }

        return PositionRiskResult(
            accountEquity = safeEquity,
            riskPercentage = safeRiskPct,
            riskCapitalUsdt = riskCapitalUsdt,
            entryPrice = safeEntry,
            stopLossPrice = safeSl,
            stopLossDistancePercent = slDistancePct,
            positionSizeUnits = positionSizeUnits,
            positionSizeUsdt = positionSizeUsdt,
            estimatedLossUsdt = estimatedLossUsdt,
            suggestedLeverage = suggestedLeverage,
            leverageWarning = warning
        )
    }
}
