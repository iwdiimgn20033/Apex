package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AiTradingAdvice
import com.example.data.model.Asset
import com.example.data.model.EconomicEvent
import com.example.data.model.MarketNews
import com.example.data.model.MarketSentiment
import com.example.data.model.NewsImpact
import com.example.data.model.OrderSide
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Unified item representing either an AI Signal, Live Breaking News, or Macro Event
 */
sealed class AiFeedItem {
    abstract val id: String
    abstract val timestamp: Long
    abstract val timeAgoStr: String
    abstract val timeAgoStrAr: String
    abstract val relatedSymbol: String

    data class AiAdviceItem(
        val advice: AiTradingAdvice
    ) : AiFeedItem() {
        override val id: String = advice.id
        override val timestamp: Long = advice.generatedTimestamp
        override val timeAgoStr: String = "Just now"
        override val timeAgoStrAr: String = "الآن"
        override val relatedSymbol: String = advice.symbol
    }

    data class NewsItem(
        val news: MarketNews
    ) : AiFeedItem() {
        override val id: String = news.id
        override val timestamp: Long = news.timestamp
        override val timeAgoStr: String = news.timeAgo
        override val timeAgoStrAr: String = news.timeAgoAr
        override val relatedSymbol: String = news.relatedSymbol
    }

    data class EventItem(
        val event: EconomicEvent
    ) : AiFeedItem() {
        override val id: String = event.id
        override val timestamp: Long = System.currentTimeMillis() - 1800000L
        override val timeAgoStr: String = event.scheduledTime
        override val timeAgoStrAr: String = event.scheduledTimeAr
        override val relatedSymbol: String = event.affectedAssets.firstOrNull() ?: ""
    }
}

@Composable
fun AiNewsTickerBar(
    currentAsset: Asset,
    aiAdvice: AiTradingAdvice?,
    newsList: List<MarketNews>,
    economicEvents: List<EconomicEvent>,
    language: AppLanguage,
    onApplySignal: ((OrderSide, Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isAr = language == AppLanguage.ARABIC

    // Build unified feed prioritizing current asset
    val feedItems = remember(currentAsset.symbol, aiAdvice, newsList, economicEvents) {
        val list = mutableListOf<AiFeedItem>()

        // 1. Current Asset AI Advice
        if (aiAdvice != null && aiAdvice.symbol.equals(currentAsset.symbol, ignoreCase = true)) {
            list.add(AiFeedItem.AiAdviceItem(aiAdvice))
        }

        // 2. Direct Matching News for Current Asset
        val assetNews = newsList.filter { it.relatedSymbol.equals(currentAsset.symbol, ignoreCase = true) }
        assetNews.forEach { list.add(AiFeedItem.NewsItem(it)) }

        // 3. Relevant Economic Events impacting this asset
        val assetEvents = economicEvents.filter { it.affectedAssets.any { sym -> sym.equals(currentAsset.symbol, ignoreCase = true) } }
        assetEvents.forEach { list.add(AiFeedItem.EventItem(it)) }

        // 4. Fallback: other market news if list is small
        if (list.size < 2) {
            newsList.filter { !it.relatedSymbol.equals(currentAsset.symbol, ignoreCase = true) }
                .take(3)
                .forEach { list.add(AiFeedItem.NewsItem(it)) }
        }

        list
    }

    if (feedItems.isEmpty()) return

    var currentIndex by remember(currentAsset.symbol) { mutableIntStateOf(0) }
    var selectedItemForDetail by remember { mutableStateOf<AiFeedItem?>(null) }
    var isAutoCycleEnabled by remember { mutableStateOf(true) }

    // Auto-cycle ticker every 8 seconds if not paused
    LaunchedEffect(feedItems.size, isAutoCycleEnabled) {
        while (isAutoCycleEnabled && feedItems.size > 1) {
            delay(8000L)
            currentIndex = (currentIndex + 1) % feedItems.size
        }
    }

    val safeIndex = currentIndex.coerceIn(0, feedItems.lastIndex)
    val currentItem = feedItems[safeIndex]

    // Pulsing transition for AI LIVE indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_news_ticker_bar"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = when (currentItem) {
                is AiFeedItem.AiAdviceItem -> ApexCyan.copy(alpha = 0.5f)
                is AiFeedItem.NewsItem -> if (currentItem.news.sentiment == MarketSentiment.BULLISH) BullishGreen.copy(alpha = 0.35f) else BearishRed.copy(alpha = 0.35f)
                is AiFeedItem.EventItem -> ApexGold.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Ticker Top Header: Tag + Live Pulse + Timing + Carousel Arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category & Pulse
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentItem) {
                                    is AiFeedItem.AiAdviceItem -> ApexCyan.copy(alpha = pulseAlpha)
                                    is AiFeedItem.NewsItem -> BullishGreen.copy(alpha = pulseAlpha)
                                    is AiFeedItem.EventItem -> ApexGold.copy(alpha = pulseAlpha)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    val (badgeText, badgeBg, badgeTextColor) = when (currentItem) {
                        is AiFeedItem.AiAdviceItem -> Triple(
                            if (isAr) "توصية AI فورية" else "AI INSTANT SIGNAL",
                            ApexCyan.copy(alpha = 0.15f),
                            ApexCyan
                        )
                        is AiFeedItem.NewsItem -> Triple(
                            if (isAr) "خبر عاجل مؤثر" else "BREAKING CATALYST",
                            if (currentItem.news.sentiment == MarketSentiment.BULLISH) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f),
                            if (currentItem.news.sentiment == MarketSentiment.BULLISH) BullishGreen else BearishRed
                        )
                        is AiFeedItem.EventItem -> Triple(
                            if (isAr) "مؤشر اقتصادي" else "MACRO EVENT",
                            ApexGold.copy(alpha = 0.15f),
                            ApexGold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stock symbol tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentItem.relatedSymbol.ifBlank { currentAsset.symbol },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Timing & Carousel Navigation controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isAr) currentItem.timeAgoStrAr else currentItem.timeAgoStr,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${safeIndex + 1}/${feedItems.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Prev / Next Chevron buttons
                    IconButton(
                        onClick = {
                            isAutoCycleEnabled = false
                            currentIndex = if (currentIndex > 0) currentIndex - 1 else feedItems.lastIndex
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            isAutoCycleEnabled = false
                            currentIndex = (currentIndex + 1) % feedItems.size
                        },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area: Headline + Expected Stock Impact
            when (currentItem) {
                is AiFeedItem.AiAdviceItem -> {
                    val advice = currentItem.advice
                    val isBuy = advice.action.isBuy
                    val impactMovePct = if (isBuy) "+${String.format(Locale.US, "%.1f", (advice.takeProfit1 - advice.currentPrice) / advice.currentPrice * 100f)}%" 
                                        else "-${String.format(Locale.US, "%.1f", (advice.currentPrice - advice.takeProfit1) / advice.currentPrice * 100f)}%"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItemForDetail = currentItem },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isAr) advice.action.labelAr else advice.action.labelEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isBuy) BullishGreen else BearishRed
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "التأثير المتوقع على ${advice.symbol}: $impactMovePct" else "Target Impact: $impactMovePct",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBuy) BullishGreen else BearishRed
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAr) advice.technicalRationaleAr else advice.technicalRationale,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Confidence meter badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ApexCyan.copy(alpha = 0.12f))
                                .border(1.dp, ApexCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${advice.confidencePercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ApexCyan
                                )
                                Text(
                                    text = if (isAr) "ثقة AI" else "CONFIDENCE",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ApexCyan.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Action buttons bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TP1: $${advice.takeProfit1} • SL: $${advice.stopLoss} • R:R ${advice.riskRewardRatio}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Details button
                            Text(
                                text = if (isAr) "تحليل التأثير 🔍" else "Impact Breakdown 🔍",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { selectedItemForDetail = currentItem }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )

                            // 1-Click apply SL/TP
                            if (onApplySignal != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isBuy) BullishGreen else BearishRed)
                                        .clickable {
                                            val side = if (isBuy) OrderSide.BUY else OrderSide.SELL
                                            onApplySignal(side, advice.stopLoss, advice.takeProfit1)
                                        }
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isAr) "تطبيق الهدف ⚡" else "Apply ⚡",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                is AiFeedItem.NewsItem -> {
                    val news = currentItem.news
                    val isBullish = news.sentiment == MarketSentiment.BULLISH
                    val impactColor = if (isBullish) BullishGreen else if (news.sentiment == MarketSentiment.BEARISH) BearishRed else MaterialTheme.colorScheme.primary

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItemForDetail = currentItem }
                    ) {
                        // Title
                        Text(
                            text = if (isAr) news.titleAr else news.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stock Impact Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(impactColor.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = impactColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAr) "التأثير على السهم: ${if (news.expectedImpactPercent >= 0) "+" else ""}${news.expectedImpactPercent}%"
                                           else "Stock Impact: ${if (news.expectedImpactPercent >= 0) "+" else ""}${news.expectedImpactPercent}%",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = impactColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${news.impactProbability}% ${if (isAr) "احتمالية" else "Prob"})",
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = if (isAr) "التفاصيل >" else "Details >",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexCyan
                            )
                        }
                    }
                }

                is AiFeedItem.EventItem -> {
                    val event = currentItem.event
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItemForDetail = currentItem }
                    ) {
                        Text(
                            text = if (isAr) event.titleAr else event.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAr) "التقلب المتوقع: ${event.expectedVolatilityAr}" else "Expected Volatility: ${event.expectedVolatility}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ApexGold
                            )
                            Text(
                                text = if (isAr) "تحليل الفيدرالي >" else "Macro Analysis >",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ApexCyan
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Breakdown Dialog
    selectedItemForDetail?.let { item ->
        AiImpactDetailDialog(
            item = item,
            currentAsset = currentAsset,
            language = language,
            onDismiss = { selectedItemForDetail = null },
            onApplySignal = onApplySignal
        )
    }
}

/**
 * Institutional AI Impact & Breaking News Breakdown Modal
 */
@Composable
private fun AiImpactDetailDialog(
    item: AiFeedItem,
    currentAsset: Asset,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onApplySignal: ((OrderSide, Double, Double) -> Unit)? = null
) {
    val isAr = language == AppLanguage.ARABIC

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Modal Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ApexCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ApexCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAr) "تحليل الذكاء الاصطناعي وتأثير الخبر" else "AI Stock Impact Breakdown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: ${currentAsset.symbol} (${currentAsset.name})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (item) {
                    is AiFeedItem.AiAdviceItem -> {
                        val advice = item.advice
                        val isBuy = advice.action.isBuy

                        // Big Signal Impact Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBuy) BullishGreen.copy(alpha = 0.12f) else BearishRed.copy(alpha = 0.12f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isAr) advice.action.labelAr else advice.action.labelEn,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isBuy) BullishGreen else BearishRed
                                        )
                                        Text(
                                            text = "${if (isAr) "توقيت الإشارة:" else "Signal Time:"} ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(advice.generatedTimestamp))}",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${advice.confidencePercent}%",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ApexCyan
                                        )
                                        Text(
                                            text = if (isAr) "دقة النموذج" else "AI Confidence",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ApexCyan
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Price Target Range Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(if (isAr) "الدخول المقترح" else "Entry Target", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$${advice.entryTarget}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BullishGreen.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(if (isAr) "هدف الربح TP1" else "Take Profit 1", fontSize = 9.5.sp, color = BullishGreen)
                                    Text("$${advice.takeProfit1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BearishRed.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(if (isAr) "وقف الخسارة SL" else "Stop Loss", fontSize = 9.5.sp, color = BearishRed)
                                    Text("$${advice.stopLoss}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BearishRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Technical & Fundamental Commentary
                        Text(
                            text = if (isAr) "التحليل الفني للذكاء الاصطناعي:" else "AI Technical Rationale:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isAr) advice.technicalRationaleAr else advice.technicalRationale,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isAr) "المحركات الأساسية والسيولة:" else "Fundamental & Liquidity Drivers:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isAr) advice.fundamentalRationaleAr else advice.fundamentalRationale,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (onApplySignal != null) {
                            Button(
                                onClick = {
                                    val side = if (isBuy) OrderSide.BUY else OrderSide.SELL
                                    onApplySignal(side, advice.stopLoss, advice.takeProfit1)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBuy) BullishGreen else BearishRed
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAr) "تطبيق هذه التوصية في شاشة التداول فوراً" else "Execute Trade With AI Targets",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                        }
                    }

                    is AiFeedItem.NewsItem -> {
                        val news = item.news
                        val isBullish = news.sentiment == MarketSentiment.BULLISH
                        val impactColor = if (isBullish) BullishGreen else if (news.sentiment == MarketSentiment.BEARISH) BearishRed else MaterialTheme.colorScheme.primary

                        // News Headline
                        Text(
                            text = if (isAr) news.titleAr else news.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Metadata badge row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Source: ${news.source} • ${if (isAr) news.timeAgoAr else news.timeAgo}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(impactColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isBullish) "BULLISH CATALYST" else "BEARISH IMPACT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = impactColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // AI Expected Impact Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = impactColor.copy(alpha = 0.10f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, impactColor.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isAr) "التأثير المباشر على سهم ${news.relatedSymbol}" else "Direct Impact on ${news.relatedSymbol}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${if (news.expectedImpactPercent >= 0) "+" else ""}${news.expectedImpactPercent}%",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = impactColor
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${news.impactProbability}%",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ApexCyan
                                        )
                                        Text(
                                            text = if (isAr) "احتمالية التحرك" else "Confidence",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ApexCyan
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (isAr) news.stockImpactAnalysisAr else news.stockImpactAnalysis,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // News Summary
                        Text(
                            text = if (isAr) "ملخص التقرير المالي:" else "Full Report Context:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isAr) news.summaryAr else news.summary,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    is AiFeedItem.EventItem -> {
                        val event = item.event
                        Text(
                            text = if (isAr) event.titleAr else event.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scheduled: ${if (isAr) event.scheduledTimeAr else event.scheduledTime} • Impact: ${event.impact.name}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = ApexGold.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isAr) "التحليل الاستباقي ومناطق التقلب:" else "Macro Volatility Outlook:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ApexGold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isAr) event.analysisNoteAr else event.analysisNote,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = if (isAr) "إغلاق النافذة" else "Dismiss",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
