package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Asset
import com.example.data.model.IndicatorType
import com.example.data.model.OrderSide
import com.example.ui.components.AiNewsTickerBar
import com.example.ui.components.CandlestickChart
import com.example.ui.components.formatChartPrice
import com.example.ui.components.TradeOrderSheet
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.viewmodel.BrokerViewModel
import java.util.Locale

@Composable
fun TradeScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val selectedAsset by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val candlesticks by viewModel.candlesticks.collectAsStateWithLifecycle()
    val timeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val indicator by viewModel.selectedIndicator.collectAsStateWithLifecycle()
    val isCandleMode by viewModel.isCandleMode.collectAsStateWithLifecycle()
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isOrderSheetOpen by viewModel.isOrderSheetOpen.collectAsStateWithLifecycle()
    val orderSheetSide by viewModel.orderSheetSide.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val marketNews by viewModel.marketNews.collectAsStateWithLifecycle()
    val economicEvents by viewModel.economicEvents.collectAsStateWithLifecycle()

    var showAssetPicker by remember { mutableStateOf(false) }
    var prefilledStopLoss by remember { mutableStateOf<Double?>(null) }
    var prefilledTakeProfit by remember { mutableStateOf<Double?>(null) }

    val currentAsset = selectedAsset ?: assets.firstOrNull() ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("trade_screen")
    ) {
        // Asset Selector Top Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAssetPicker = !showAssetPicker }
                .testTag("asset_picker_toggle"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentAsset.symbol.take(3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentAsset.symbol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select")
                        }
                        Text(
                            text = currentAsset.name,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (currentAsset.price > 1000) String.format(Locale.US, "$%,.2f", currentAsset.price) else String.format(Locale.US, "$%.4f", currentAsset.price),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (currentAsset.isPositive) "+" else ""}${String.format(Locale.US, "%.2f", currentAsset.changePercent24h)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentAsset.isPositive) BullishGreen else BearishRed
                    )
                }
            }
        }

        // Expanded Asset Picker Dropdown if open
        if (showAssetPicker) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(assets) { asset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectAsset(asset)
                                    showAssetPicker = false
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${asset.symbol} - ${asset.name}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("$${asset.price}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Advice & Real-Time Breaking News Ticker Bar
        AiNewsTickerBar(
            currentAsset = currentAsset,
            aiAdvice = aiAdvice,
            newsList = marketNews,
            economicEvents = economicEvents,
            language = language,
            onApplySignal = { side, sl, tp ->
                prefilledStopLoss = sl
                prefilledTakeProfit = tp
                viewModel.openOrderSheet(currentAsset, side)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Timeframe & Chart Style Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeframes (Scrollable or compact)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("1M", "5M", "15M", "1H", "4H", "1D", "1W").forEach { tf ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (timeframe == tf) ApexCyan else MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.setTimeframe(tf) }
                            .padding(horizontal = 6.dp, vertical = 5.dp)
                            .testTag("timeframe_$tf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeframe == tf) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Indicators & Chart Style
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf(IndicatorType.SMA, IndicatorType.BOLLINGER, IndicatorType.RSI, IndicatorType.MACD).forEach { ind ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (indicator == ind) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.setIndicator(if (indicator == ind) IndicatorType.NONE else ind) }
                            .padding(horizontal = 6.dp, vertical = 5.dp)
                            .testTag("indicator_${ind.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ind.label.take(4),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (indicator == ind) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Toggle Candlestick / Line
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { viewModel.toggleChartStyle() }
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCandleMode) Icons.Default.CandlestickChart else Icons.Default.ShowChart,
                        contentDescription = "Chart Style",
                        modifier = Modifier.size(15.dp),
                        tint = ApexCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Real-Time Feed Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BullishGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE MT5 DIRECT FEED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BullishGreen,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "24h H: ${formatChartPrice(currentAsset.high24h.toFloat())}  L: ${formatChartPrice(currentAsset.low24h.toFloat())}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Chart Canvas with Real-Time Live Financial Data
        CandlestickChart(
            candlesticks = candlesticks,
            selectedIndicator = indicator,
            isCandleMode = isCandleMode,
            currentPrice = currentAsset.price,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live MT5 Depth Order Book (Mini)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Strings.get("order_book_depth", language), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${Strings.get("spread", language)}: ${currentAsset.spreadPips} ${Strings.get("pips", language)}", fontSize = 11.sp, color = ApexCyan)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bids side
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(Strings.get("bid_usd", language), fontSize = 10.sp, color = BullishGreen)
                            Text(Strings.get("vol", language), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${String.format(Locale.US, "%.4f", currentAsset.bid)}  •  45.2 Lots", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BullishGreen)
                        Text("${String.format(Locale.US, "%.4f", currentAsset.bid - 0.0005)}  •  80.0 Lots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Asks side
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(Strings.get("ask_usd", language), fontSize = 10.sp, color = BearishRed)
                            Text(Strings.get("vol", language), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${String.format(Locale.US, "%.4f", currentAsset.ask)}  •  38.5 Lots", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BearishRed)
                        Text("${String.format(Locale.US, "%.4f", currentAsset.ask + 0.0005)}  •  92.0 Lots", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // AI Trading Advice Quick Summary Card
        if (aiAdvice != null && aiAdvice?.symbol == currentAsset.symbol) {
            val isAr = language == AppLanguage.ARABIC
            val advice = aiAdvice!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ApexCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAr) "توصية AI الفورية:" else "AI Instant Signal:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAr) advice.action.labelAr else advice.action.labelEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (advice.action.isBuy) BullishGreen else BearishRed
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${advice.confidencePercent}% ${Strings.get("confidence", language)})",
                                    fontSize = 10.sp,
                                    color = ApexCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "TP1: $${advice.takeProfit1} • SL: $${advice.stopLoss} • R:R ${advice.riskRewardRatio}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 1-Click Buy / Sell Execution Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.openOrderSheet(currentAsset, OrderSide.SELL) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("trade_screen_sell_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = BearishRed, contentColor = Color.White)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Strings.get("sell", language), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(String.format(Locale.US, "%.4f", currentAsset.bid), fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = { viewModel.openOrderSheet(currentAsset, OrderSide.BUY) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("trade_screen_buy_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = BullishGreen, contentColor = Color.White)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Strings.get("buy", language), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(String.format(Locale.US, "%.4f", currentAsset.ask), fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        // Overlay Order Execution Sheet
        if (isOrderSheetOpen) {
            TradeOrderSheet(
                asset = currentAsset,
                language = language,
                freeMargin = userProfile.freeMargin,
                initialSide = orderSheetSide,
                initialStopLoss = prefilledStopLoss,
                initialTakeProfit = prefilledTakeProfit,
                onDismiss = { 
                    prefilledStopLoss = null
                    prefilledTakeProfit = null
                    viewModel.closeOrderSheet() 
                },
                onExecuteOrder = { side, lots, leverage, sl, tp ->
                    viewModel.executeOrder(currentAsset.symbol, side, lots, leverage, sl, tp)
                }
            )
        }
    }
}
