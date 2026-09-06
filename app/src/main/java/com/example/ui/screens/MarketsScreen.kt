package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AiTradingAdvice
import com.example.data.model.Asset
import com.example.data.model.AssetCategory
import com.example.data.model.EconomicEvent
import com.example.data.model.MarketMoverAnalysis
import com.example.data.model.MarketNews
import com.example.data.model.MarketSentiment
import com.example.data.model.NewsImpact
import com.example.data.model.OrderSide
import com.example.data.model.SignalAction
import com.example.data.model.TradingSignal
import com.example.ui.components.AssetRowItem
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.viewmodel.BrokerTab
import com.example.ui.viewmodel.BrokerViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarketsScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val signals by viewModel.tradingSignals.collectAsStateWithLifecycle()
    val newsList by viewModel.marketNews.collectAsStateWithLifecycle()
    val economicEvents by viewModel.economicEvents.collectAsStateWithLifecycle()
    val marketMovers by viewModel.marketMovers.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.screenerCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.screenerQuery.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val selectedAsset by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val liveStatus by viewModel.liveMarketStatus.collectAsStateWithLifecycle()
    val isLiveInternet by viewModel.isLiveInternetDirect.collectAsStateWithLifecycle()

    var marketSubTab by remember { mutableStateOf(0) } 
    // 0: Screener & Search, 1: AI Advisor, 2: Upcoming News & Calendar, 3: Why Prices Moved, 4: Signals & News

    val isAr = language == AppLanguage.ARABIC

    val filteredAssets = assets.filter { asset ->
        val matchCategory = selectedCategory == AssetCategory.ALL || asset.category == selectedCategory
        val matchSearch = searchQuery.isBlank() ||
                asset.symbol.contains(searchQuery, ignoreCase = true) ||
                asset.name.contains(searchQuery, ignoreCase = true)
        val matchFavorites = !onlyFavorites || asset.isFavorite
        matchCategory && matchSearch && matchFavorites
    }

    val tabs = listOf(
        Strings.get("screener", language),
        Strings.get("ai_trading_agent", language),
        Strings.get("economic_calendar", language),
        Strings.get("market_movers", language),
        Strings.get("signals", language)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("markets_screen")
    ) {
        // Direct Live Market Connectivity Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isLiveInternet && liveStatus.isConnected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
                .border(
                    width = 1.dp,
                    color = if (isLiveInternet && liveStatus.isConnected) ApexCyan.copy(alpha = 0.4f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (isLiveInternet && liveStatus.isConnected) BullishGreen else BearishRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isLiveInternet) Strings.get("live_stream_active", language) else Strings.get("offline_mode", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${liveStatus.latencyMs}ms",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ApexCyan
                        )
                    }
                    Text(
                        text = Strings.get("live_source_tag", language),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Sync / Refresh Button
            IconButton(
                onClick = { viewModel.refreshLivePrices() },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag("refresh_live_prices_btn")
            ) {
                if (liveStatus.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = ApexCyan
                    )
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Sync Live Market",
                        tint = ApexCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sub-tabs Scrollable Row
        ScrollableTabRow(
            selectedTabIndex = marketSubTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[marketSubTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = marketSubTab == idx,
                    onClick = { marketSubTab = idx },
                    modifier = Modifier.testTag("market_subtab_$idx"),
                    text = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (marketSubTab == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (marketSubTab == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (marketSubTab) {
            0 -> {
                // Screener & Search View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setScreenerQuery(it) },
                        placeholder = { 
                            Text(
                                text = Strings.get("search_assets", language),
                                fontSize = 12.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setScreenerQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("screener_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Favorites Only Quick Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (onlyFavorites) ApexCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (onlyFavorites) ApexCyan else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.toggleOnlyFavorites() }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                            .testTag("toggle_favorites_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (onlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorites",
                                tint = if (onlyFavorites) ApexCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Strings.get("favorites_only", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (onlyFavorites) ApexCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AssetCategory.values()) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ApexCyan else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.setScreenerCategory(category) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("cat_chip_${category.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.ARABIC) category.displayNameAr else category.displayNameEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAr) "لا توجد أسهم تطابق بحثك" else "No matching assets found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAssets) { asset ->
                            AssetRowItem(
                                asset = asset,
                                onClick = {
                                    viewModel.selectAsset(asset)
                                    viewModel.selectTab(BrokerTab.TRADE)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(asset.symbol, asset.isFavorite)
                                }
                            )
                        }
                    }
                }
            }

            1 -> {
                // AI Agent Trading Advice Screen
                AiAgentAdviceSection(
                    viewModel = viewModel,
                    assets = assets,
                    selectedAsset = selectedAsset,
                    aiAdvice = aiAdvice,
                    isLoading = isAiLoading,
                    language = language
                )
            }

            2 -> {
                // Upcoming News & Economic Calendar Screen
                UpcomingEconomicEventsSection(
                    events = economicEvents,
                    language = language,
                    onAssetClick = { symbol ->
                        val asset = assets.find { it.symbol == symbol }
                        if (asset != null) {
                            viewModel.selectAsset(asset)
                            viewModel.selectTab(BrokerTab.TRADE)
                        }
                    }
                )
            }

            3 -> {
                // Reasons for Strong Rise & Fall Screen
                MarketMoversAnalysisSection(
                    movers = marketMovers,
                    language = language,
                    onTradeClick = { symbol ->
                        val asset = assets.find { it.symbol == symbol }
                        if (asset != null) {
                            viewModel.selectAsset(asset)
                            viewModel.selectTab(BrokerTab.TRADE)
                        }
                    }
                )
            }

            4 -> {
                // Signals & Market News
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(signals) { sig ->
                        SignalFullCard(
                            signal = sig,
                            language = language,
                            onExecute = {
                                val asset = assets.find { it.symbol == sig.symbol }
                                if (asset != null) {
                                    viewModel.openOrderSheet(asset, if (sig.action.isBuy) OrderSide.BUY else OrderSide.SELL)
                                }
                            }
                        )
                    }
                    items(newsList) { news ->
                        NewsCard(news = news, language = language)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiAgentAdviceSection(
    viewModel: BrokerViewModel,
    assets: List<Asset>,
    selectedAsset: Asset?,
    aiAdvice: AiTradingAdvice?,
    isLoading: Boolean,
    language: AppLanguage
) {
    val isAr = language == AppLanguage.ARABIC
    val currentAsset = selectedAsset ?: assets.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Asset Selector Carousel
        item {
            Text(
                text = if (isAr) "اختر السهم أو الأصل لتحليله عبر الذكاء الاصطناعي:" else "Select Asset for Real-Time AI Analysis:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(assets) { asset ->
                    val isSelected = currentAsset?.symbol == asset.symbol
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.selectAsset(asset) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("ai_select_${asset.symbol}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = asset.symbol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$${asset.price}",
                                fontSize = 11.sp,
                                color = if (asset.changePercent24h >= 0) BullishGreen else BearishRed
                            )
                        }
                    }
                }
            }
        }

        // Action / Refresh AI Recommendation
        item {
            Button(
                onClick = {
                    currentAsset?.let { viewModel.requestAiTradingAdvice(it.symbol) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("request_ai_advice_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("ai_generating", language),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${Strings.get("ask_ai_analyst", language)} (${currentAsset?.symbol})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Display AI Recommendation Card
        if (aiAdvice != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Header: Symbol, Action Badge & Confidence
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = ApexCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${aiAdvice.symbol} - ${aiAdvice.assetName}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${Strings.get("timeframe", language)}: ${aiAdvice.timeframe} • ${Strings.get("price", language)}: $${aiAdvice.currentPrice}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (aiAdvice.action.isBuy) BullishGreen.copy(alpha = 0.18f) else BearishRed.copy(alpha = 0.18f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isAr) aiAdvice.action.labelAr else aiAdvice.action.labelEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (aiAdvice.action.isBuy) BullishGreen else BearishRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Targets Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(Strings.get("ai_entry_target", language), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${aiAdvice.entryTarget}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(Strings.get("ai_take_profit_1", language), fontSize = 10.sp, color = BullishGreen)
                                Text("$${aiAdvice.takeProfit1}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(Strings.get("ai_take_profit_2", language), fontSize = 10.sp, color = BullishGreen)
                                Text("$${aiAdvice.takeProfit2}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(Strings.get("ai_stop_loss", language), fontSize = 10.sp, color = BearishRed)
                                Text("$${aiAdvice.stopLoss}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BearishRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metrics: Confidence & R:R
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${Strings.get("confidence", language)}: ${aiAdvice.confidencePercent}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexCyan
                            )
                            Text(
                                text = "${Strings.get("risk_reward", language)}: ${aiAdvice.riskRewardRatio}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Technical Analysis Rationale
                        Text(
                            text = if (isAr) "التحليل الفني:" else "Technical Rationale:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAr) aiAdvice.technicalRationaleAr else aiAdvice.technicalRationale,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Fundamental Analysis Rationale
                        Text(
                            text = if (isAr) "التحليل الأساسي والسيولة:" else "Fundamental & Liquidity:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAr) aiAdvice.fundamentalRationaleAr else aiAdvice.fundamentalRationale,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1-Click Order Execution Button
                        Button(
                            onClick = {
                                val asset = currentAsset ?: assets.first()
                                viewModel.openOrderSheet(
                                    asset,
                                    if (aiAdvice.action.isBuy) OrderSide.BUY else OrderSide.SELL
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (aiAdvice.action.isBuy) BullishGreen else BearishRed
                            )
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAr) "تنفيذ صفقة AI الفورية (${if (aiAdvice.action.isBuy) "شراء" else "بيع"})" else "Execute AI Trade (${if (aiAdvice.action.isBuy) "BUY" else "SELL"})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpcomingEconomicEventsSection(
    events: List<EconomicEvent>,
    language: AppLanguage,
    onAssetClick: (String) -> Unit
) {
    val isAr = language == AppLanguage.ARABIC

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isAr) "الأحداث والبيانات الاقتصادية المؤثرة على الأسواق:" else "Major High-Impact Economic Catalysts:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAr) event.scheduledTimeAr else event.scheduledTime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BearishRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = Strings.get("impact_high", language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearishRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAr) event.titleAr else event.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forecast & Previous comparison
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = "${Strings.get("forecast", language)}: ${event.forecast}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${Strings.get("previous", language)}: ${event.previous}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Volatility and Analysis Note
                    Text(
                        text = "${Strings.get("volatility", language)}: ${if (isAr) event.expectedVolatilityAr else event.expectedVolatility}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexCyan
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isAr) event.analysisNoteAr else event.analysisNote,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Affected assets tags
                    Text(
                        text = if (isAr) "الأصول المتأثرة مباشرة:" else "Directly Affected Assets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        event.affectedAssets.forEach { sym ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { onAssetClick(sym) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketMoversAnalysisSection(
    movers: List<MarketMoverAnalysis>,
    language: AppLanguage,
    onTradeClick: (String) -> Unit
) {
    val isAr = language == AppLanguage.ARABIC

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isAr) "تحليل أسباب الصعود والهبوط القوي في الأسواق:" else "Deep Analysis of Sharp Market Surges & Drops:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(movers) { mover ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (mover.isRise) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (mover.isRise) BullishGreen else BearishRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${mover.symbol} (${mover.name})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (mover.isRise) BullishGreen.copy(alpha = 0.18f) else BearishRed.copy(alpha = 0.18f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${if (mover.changePercent > 0) "+" else ""}${String.format(Locale.US, "%.2f", mover.changePercent)}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (mover.isRise) BullishGreen else BearishRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Catalyst Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isAr) mover.category.labelAr else mover.category.labelEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Headline
                    Text(
                        text = if (isAr) mover.headlineAr else mover.headline,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Deep Analysis Explanation
                    Text(
                        text = if (isAr) mover.deepAnalysisAr else mover.deepAnalysis,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Technical Key Levels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${Strings.get("key_levels", language)}: ${if (isAr) mover.keyPriceLevelsAr else mover.keyPriceLevels}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Trade Action Button
                    Button(
                        onClick = { onTradeClick(mover.symbol) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = if (isAr) "فتح شارت وتداول ${mover.symbol}" else "Open Chart & Trade ${mover.symbol}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalFullCard(
    signal: TradingSignal,
    language: AppLanguage,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("signal_full_${signal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${signal.timeframe}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (signal.action.isBuy) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) signal.action.labelAr else signal.action.labelEn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (signal.action.isBuy) BullishGreen else BearishRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (language == AppLanguage.ARABIC) signal.analysisTextAr else signal.analysisText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Entry: ${signal.entryPrice}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("TP1: ${signal.targetPrice1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                Text("SL: ${signal.stopLoss}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BearishRed)
                Text("Win: ${signal.confidencePercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onExecute() }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.ARABIC) "تنفيذ الصفقة بنقرة واحدة عبر MT5" else "1-Click MT5 Order Execution",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun NewsCard(news: MarketNews, language: AppLanguage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${news.source} • ${if (language == AppLanguage.ARABIC) news.timeAgoAr else news.timeAgo}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (news.sentiment) {
                                MarketSentiment.BULLISH -> BullishGreen.copy(alpha = 0.15f)
                                MarketSentiment.BEARISH -> BearishRed.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = news.sentiment.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (news.sentiment) {
                            MarketSentiment.BULLISH -> BullishGreen
                            MarketSentiment.BEARISH -> BearishRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (language == AppLanguage.ARABIC) news.titleAr else news.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (language == AppLanguage.ARABIC) news.summaryAr else news.summary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}
