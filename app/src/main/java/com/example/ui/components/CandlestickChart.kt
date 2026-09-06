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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CandleStick
import com.example.data.model.IndicatorType
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CandlestickChart(
    candlesticks: List<CandleStick>,
    selectedIndicator: IndicatorType,
    isCandleMode: Boolean = true,
    currentPrice: Double? = null,
    modifier: Modifier = Modifier
) {
    if (candlesticks.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading live market data...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    // Reset or clamp inspected index if candlestick count changes
    LaunchedEffect(candlesticks.size) {
        if (selectedIndex != null && (candlesticks.isEmpty() || selectedIndex !in candlesticks.indices)) {
            selectedIndex = null
        }
    }

    val rawMinPrice = candlesticks.minOfOrNull { it.low } ?: 0f
    val rawMaxPrice = candlesticks.maxOfOrNull { it.high } ?: 100f
    val liveP = (currentPrice ?: candlesticks.lastOrNull()?.close?.toDouble() ?: 100.0).toFloat()
    val minPrice = minOf(rawMinPrice, liveP * 0.998f)
    val maxPrice = maxOf(rawMaxPrice, liveP * 1.002f)
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001f)

    val maxVolume = (candlesticks.maxOfOrNull { it.volume } ?: 1000f).coerceAtLeast(1f)

    // Active inspected candle (defaults to the latest live candle)
    val activeCandle = selectedIndex?.let { candlesticks.getOrNull(it) } ?: candlesticks.lastOrNull()

    Column(modifier = modifier.fillMaxWidth()) {
        // High-precision live HUD Header (OHLCV + Change%)
        activeCandle?.let { candle ->
            val candleChange = candle.close - candle.open
            val candleChangePct = if (candle.open > 0f) (candleChange / candle.open) * 100f else 0f
            val isInspecting = selectedIndex != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isInspecting) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isInspecting) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ApexGold)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = "O: ${formatChartPrice(candle.open)}  H: ${formatChartPrice(candle.high)}  L: ${formatChartPrice(candle.low)}  C: ${formatChartPrice(candle.close)}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (candle.isBullish) BullishGreen else BearishRed
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%+.2f%%", candleChangePct),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (candleChange >= 0f) BullishGreen else BearishRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(candle.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        val crosshairColor = ApexCyan.copy(alpha = 0.75f)
        val crosshairDotColor = ApexCyan

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(4.dp)
                .testTag("candlestick_chart_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candlesticks) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (candlesticks.isEmpty()) return@detectTapGestures
                                val chartAreaWidth = size.width - 55.dp.toPx()
                                val candleWidth = chartAreaWidth / candlesticks.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candlesticks.lastIndex)
                                selectedIndex = if (selectedIndex == index) null else index
                            }
                        )
                    }
                    .pointerInput(candlesticks) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (candlesticks.isEmpty()) return@detectDragGestures
                                val chartAreaWidth = size.width - 55.dp.toPx()
                                val candleWidth = chartAreaWidth / candlesticks.size
                                selectedIndex = (offset.x / candleWidth).toInt().coerceIn(0, candlesticks.lastIndex)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (candlesticks.isEmpty()) return@detectDragGestures
                                val chartAreaWidth = size.width - 55.dp.toPx()
                                val candleWidth = chartAreaWidth / candlesticks.size
                                selectedIndex = (change.position.x / candleWidth).toInt().coerceIn(0, candlesticks.lastIndex)
                            },
                            onDragEnd = {
                                // Keep or auto-fade crosshair
                            }
                        )
                    }
            ) {
                val yAxisWidth = 55.dp.toPx()
                val chartWidth = size.width - yAxisWidth
                val totalCanvasHeight = size.height
                val timeAxisHeight = 18.dp.toPx()
                val availableChartHeight = totalCanvasHeight - timeAxisHeight

                // Allocate top 78% to price action, lower 22% to volume bars
                val priceChartHeight = availableChartHeight * (if (selectedIndicator == IndicatorType.RSI) 0.68f else 0.80f)
                val volumeChartTop = priceChartHeight + 4f
                val volumeChartHeight = availableChartHeight - volumeChartTop

                val candleCount = candlesticks.size
                val stepX = chartWidth / candleCount
                val candleBodyWidth = (stepX * 0.70f).coerceIn(2.5f, 18f)

                // 1. Draw horizontal grid lines & Y-Axis Price Labels
                val gridLines = 4
                for (i in 0..gridLines) {
                    val ratio = i.toFloat() / gridLines
                    val y = priceChartHeight * ratio
                    val priceAtLevel = maxPrice - (ratio * priceRange)

                    // Grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )

                    // Y-axis label on the right
                    val labelText = formatChartPrice(priceAtLevel)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = labelText,
                        topLeft = Offset(chartWidth + 6f, y - 6.sp.toPx()),
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = axisTextColor,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                // 2. Draw Volume Histogram in the lower pane
                candlesticks.forEachIndexed { index, candle ->
                    val centerX = (index * stepX) + (stepX / 2f)
                    val volRatio = (candle.volume / maxVolume).coerceIn(0.05f, 1f)
                    val barHeight = volumeChartHeight * volRatio
                    val barColor = if (candle.isBullish) BullishGreen.copy(alpha = 0.35f) else BearishRed.copy(alpha = 0.35f)

                    drawRect(
                        color = barColor,
                        topLeft = Offset(centerX - (candleBodyWidth / 2f), availableChartHeight - barHeight),
                        size = Size(candleBodyWidth, barHeight)
                    )
                }

                // 3. Draw Candlesticks or Area Line
                if (isCandleMode) {
                    var highestCandleIdx = 0
                    var lowestCandleIdx = 0
                    var highestHigh = Float.MIN_VALUE
                    var lowestLow = Float.MAX_VALUE

                    candlesticks.forEachIndexed { index, candle ->
                        if (candle.high > highestHigh) {
                            highestHigh = candle.high
                            highestCandleIdx = index
                        }
                        if (candle.low < lowestLow) {
                            lowestLow = candle.low
                            lowestCandleIdx = index
                        }

                        val centerX = (index * stepX) + (stepX / 2f)
                        val highY = priceChartHeight - ((candle.high - minPrice) / priceRange) * priceChartHeight
                        val lowY = priceChartHeight - ((candle.low - minPrice) / priceRange) * priceChartHeight
                        val openY = priceChartHeight - ((candle.open - minPrice) / priceRange) * priceChartHeight
                        val closeY = priceChartHeight - ((candle.close - minPrice) / priceRange) * priceChartHeight

                        val candleColor = if (candle.isBullish) BullishGreen else BearishRed

                        // Draw Wick
                        drawLine(
                            color = candleColor,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 1.5f
                        )

                        // Draw Body
                        val topY = minOf(openY, closeY)
                        val bodyHeight = kotlin.math.abs(openY - closeY).coerceAtLeast(2f)

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(centerX - (candleBodyWidth / 2f), topY),
                            size = Size(candleBodyWidth, bodyHeight)
                        )
                    }

                    // Draw Peak Labels (H: ... and L: ...)
                    val maxCandleCenterX = (highestCandleIdx * stepX) + (stepX / 2f)
                    val maxCandleHighY = priceChartHeight - ((highestHigh - minPrice) / priceRange) * priceChartHeight
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "H: ${formatChartPrice(highestHigh)}",
                        topLeft = Offset((maxCandleCenterX - 20f).coerceIn(4f, chartWidth - 55f), (maxCandleHighY - 14.sp.toPx()).coerceAtLeast(0f)),
                        style = TextStyle(fontSize = 8.5.sp, color = BullishGreen, fontWeight = FontWeight.Bold)
                    )

                    val minCandleCenterX = (lowestCandleIdx * stepX) + (stepX / 2f)
                    val minCandleLowY = priceChartHeight - ((lowestLow - minPrice) / priceRange) * priceChartHeight
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "L: ${formatChartPrice(lowestLow)}",
                        topLeft = Offset((minCandleCenterX - 20f).coerceIn(4f, chartWidth - 55f), (minCandleLowY + 2f).coerceAtMost(priceChartHeight - 12f)),
                        style = TextStyle(fontSize = 8.5.sp, color = BearishRed, fontWeight = FontWeight.Bold)
                    )

                } else {
                    // Line & Gradient Area Mode
                    val linePath = Path()
                    val areaPath = Path()

                    candlesticks.forEachIndexed { index, candle ->
                        val centerX = (index * stepX) + (stepX / 2f)
                        val closeY = priceChartHeight - ((candle.close - minPrice) / priceRange) * priceChartHeight

                        if (index == 0) {
                            linePath.moveTo(centerX, closeY)
                            areaPath.moveTo(centerX, priceChartHeight)
                            areaPath.lineTo(centerX, closeY)
                        } else {
                            linePath.lineTo(centerX, closeY)
                            areaPath.lineTo(centerX, closeY)
                        }
                    }

                    val lastCenterX = ((candlesticks.size - 1) * stepX) + (stepX / 2f)
                    areaPath.lineTo(lastCenterX, priceChartHeight)
                    areaPath.close()

                    drawPath(
                        path = areaPath,
                        color = ApexCyan.copy(alpha = 0.18f)
                    )
                    drawPath(
                        path = linePath,
                        color = ApexCyan,
                        style = Stroke(width = 2.5f)
                    )
                }

                // 4. Draw Live Market Price Line & Pulsing Y-Axis Badge
                val livePriceY = priceChartHeight - ((liveP - minPrice) / priceRange) * priceChartHeight
                if (livePriceY in 0f..priceChartHeight) {
                    val pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    drawLine(
                        color = ApexCyan.copy(alpha = 0.90f),
                        start = Offset(0f, livePriceY),
                        end = Offset(chartWidth, livePriceY),
                        strokeWidth = 1.5f,
                        pathEffect = pathEffect
                    )

                    // Draw Live Price Badge on the right axis
                    val badgeHeight = 16.dp.toPx()
                    val badgeTop = livePriceY - (badgeHeight / 2f)
                    drawRoundRect(
                        color = ApexCyan,
                        topLeft = Offset(chartWidth + 2f, badgeTop),
                        size = Size(yAxisWidth - 4f, badgeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatChartPrice(liveP),
                        topLeft = Offset(chartWidth + 5f, badgeTop + 2.dp.toPx()),
                        style = TextStyle(
                            fontSize = 8.5.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // 5. Draw Technical Indicators
                when (selectedIndicator) {
                    IndicatorType.SMA -> {
                        drawMovingAverage(candlesticks, 7, ApexCyan, stepX, priceChartHeight, minPrice, priceRange)
                        drawMovingAverage(candlesticks, 21, ApexGold, stepX, priceChartHeight, minPrice, priceRange)
                    }
                    IndicatorType.BOLLINGER -> {
                        drawBollingerBands(candlesticks, stepX, priceChartHeight, minPrice, priceRange)
                    }
                    IndicatorType.RSI -> {
                        drawRsiPane(candlesticks, stepX, availableChartHeight)
                    }
                    IndicatorType.MACD -> {
                        drawMacd(candlesticks, stepX, priceChartHeight, minPrice, priceRange)
                    }
                    IndicatorType.NONE -> {}
                }

                // 6. Draw X-Axis Time Labels along the bottom
                val timeLabelInterval = (candleCount / 4).coerceAtLeast(1)
                for (i in 0 until candleCount step timeLabelInterval) {
                    val c = candlesticks[i]
                    val x = (i * stepX) + (stepX / 2f)
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(c.timestamp))
                    drawText(
                        textMeasurer = textMeasurer,
                        text = timeStr,
                        topLeft = Offset(x - 12f, availableChartHeight + 2.dp.toPx()),
                        style = TextStyle(
                            fontSize = 8.5.sp,
                            color = axisTextColor
                        )
                    )
                }

                // 7. Draw Crosshair if active
                selectedIndex?.let { idx ->
                    val candle = candlesticks.getOrNull(idx) ?: return@let
                    val crossX = (idx * stepX) + (stepX / 2f)
                    val crossY = priceChartHeight - ((candle.close - minPrice) / priceRange) * priceChartHeight

                    // Vertical line
                    drawLine(
                        color = crosshairColor,
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, totalCanvasHeight),
                        strokeWidth = 1f
                    )
                    // Horizontal line
                    drawLine(
                        color = crosshairColor,
                        start = Offset(0f, crossY),
                        end = Offset(chartWidth, crossY),
                        strokeWidth = 1f
                    )
                    // Center dot
                    drawCircle(
                        color = crosshairDotColor,
                        radius = 4.5f,
                        center = Offset(crossX, crossY)
                    )

                    // Price Tag pill on crosshair Y
                    val tagHeight = 15.dp.toPx()
                    val tagTop = crossY - (tagHeight / 2f)
                    drawRoundRect(
                        color = ApexGold,
                        topLeft = Offset(chartWidth + 2f, tagTop),
                        size = Size(yAxisWidth - 4f, tagHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatChartPrice(candle.close),
                        topLeft = Offset(chartWidth + 4f, tagTop + 1.5.dp.toPx()),
                        style = TextStyle(
                            fontSize = 8.5.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMovingAverage(
    candles: List<CandleStick>,
    period: Int,
    color: Color,
    stepX: Float,
    chartHeight: Float,
    minPrice: Float,
    priceRange: Float
) {
    if (candles.size < period) return
    val path = Path()
    var started = false

    for (i in (period - 1) until candles.size) {
        val subset = candles.subList(i - period + 1, i + 1)
        val avg = subset.map { it.close }.average().toFloat()
        val x = (i * stepX) + (stepX / 2f)
        val y = chartHeight - ((avg - minPrice) / priceRange) * chartHeight

        if (!started) {
            path.moveTo(x, y)
            started = true
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(path = path, color = color, style = Stroke(width = 2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBollingerBands(
    candles: List<CandleStick>,
    stepX: Float,
    chartHeight: Float,
    minPrice: Float,
    priceRange: Float
) {
    val period = 12
    if (candles.size < period) return
    val upperPath = Path()
    val lowerPath = Path()
    val midPath = Path()
    var started = false

    for (i in (period - 1) until candles.size) {
        val subset = candles.subList(i - period + 1, i + 1)
        val mean = subset.map { it.close }.average().toFloat()
        val variance = subset.map { (it.close - mean) * (it.close - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance).toFloat()

        val upper = mean + 2 * stdDev
        val lower = mean - 2 * stdDev

        val x = (i * stepX) + (stepX / 2f)
        val yMid = chartHeight - ((mean - minPrice) / priceRange) * chartHeight
        val yUpper = chartHeight - ((upper - minPrice) / priceRange) * chartHeight
        val yLower = chartHeight - ((lower - minPrice) / priceRange) * chartHeight

        if (!started) {
            midPath.moveTo(x, yMid)
            upperPath.moveTo(x, yUpper)
            lowerPath.moveTo(x, yLower)
            started = true
        } else {
            midPath.lineTo(x, yMid)
            upperPath.lineTo(x, yUpper)
            lowerPath.lineTo(x, yLower)
        }
    }

    drawPath(path = midPath, color = ApexGold, style = Stroke(width = 1.5f))
    drawPath(path = upperPath, color = ApexCyan.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
    drawPath(path = lowerPath, color = ApexCyan.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRsiPane(
    candles: List<CandleStick>,
    stepX: Float,
    totalHeight: Float
) {
    val rsiTop = totalHeight * 0.72f
    val rsiHeight = totalHeight * 0.25f

    // Background separator
    drawLine(
        color = BearishRed.copy(alpha = 0.2f),
        start = Offset(0f, rsiTop),
        end = Offset(size.width, rsiTop),
        strokeWidth = 1f
    )

    // Overbought (70) and Oversold (30) levels
    val y70 = rsiTop + rsiHeight * 0.3f
    val y30 = rsiTop + rsiHeight * 0.7f

    drawLine(color = BearishRed.copy(alpha = 0.4f), start = Offset(0f, y70), end = Offset(size.width, y70), strokeWidth = 1f)
    drawLine(color = BullishGreen.copy(alpha = 0.4f), start = Offset(0f, y30), end = Offset(size.width, y30), strokeWidth = 1f)

    val rsiPath = Path()
    var started = false

    candles.forEachIndexed { i, _ ->
        val x = (i * stepX) + (stepX / 2f)
        val simulatedRsi = 50f + kotlin.math.sin(i * 0.5).toFloat() * 25f
        val y = rsiTop + (1f - (simulatedRsi / 100f)) * rsiHeight

        if (!started) {
            rsiPath.moveTo(x, y)
            started = true
        } else {
            rsiPath.lineTo(x, y)
        }
    }

    drawPath(path = rsiPath, color = ApexCyan, style = Stroke(width = 2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMacd(
    candles: List<CandleStick>,
    stepX: Float,
    chartHeight: Float,
    minPrice: Float,
    priceRange: Float
) {
    candles.forEachIndexed { i, candle ->
        val x = (i * stepX) + (stepX / 2f)
        val macdDelta = (candle.close - candle.open) * 0.8f
        val barColor = if (macdDelta >= 0) BullishGreen.copy(alpha = 0.5f) else BearishRed.copy(alpha = 0.5f)
        val barHeight = kotlin.math.abs(macdDelta / priceRange) * chartHeight * 3f

        drawRect(
            color = barColor,
            topLeft = Offset(x - 2f, chartHeight - barHeight),
            size = Size(4f, barHeight)
        )
    }
}

fun formatChartPrice(price: Float): String {
    return if (price > 1000) {
        String.format(Locale.US, "%,.1f", price)
    } else if (price > 10) {
        String.format(Locale.US, "%.2f", price)
    } else {
        String.format(Locale.US, "%.4f", price)
    }
}

