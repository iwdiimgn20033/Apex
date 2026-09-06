package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.viewmodel.BrokerViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val closedPositions by viewModel.closedPositions.collectAsStateWithLifecycle()

    val totalTrades = (closedPositions.size + 12).coerceAtLeast(1)
    val winningTrades = (closedPositions.count { (it.realizedPnL ?: 0.0) > 0 } + 9)
    val winRate = (winningTrades.toDouble() / totalTrades * 100.0).coerceIn(10.0, 95.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = Strings.get("analytics_risk", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Equity Growth Curve Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(Strings.get("equity_growth_curve", language), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(if (language == AppLanguage.ARABIC) "+14,250.00$ (+15.8%) نمو صافي" else "+$14,250.00 (+15.8%) Net Growth", fontSize = 11.sp, color = BullishGreen, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val points = listOf(82000f, 83500f, 81000f, 86000f, 89000f, 88500f, 92000f, 94500f, 98250f)
                        val minVal = 80000f
                        val maxVal = 100000f
                        val range = maxVal - minVal

                        val path = Path()
                        val stepX = size.width / (points.size - 1)

                        points.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = size.height - ((pt - minVal) / range) * size.height
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(path = path, color = ApexCyan, style = Stroke(width = 3.dp.toPx()))
                    }
                }
            }
        }

        // Quantitative Trading Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricBox(
                    label = Strings.get("win_rate", language),
                    value = "${String.format(Locale.US, "%.1f", winRate)}%",
                    color = BullishGreen,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMetricBox(
                    label = Strings.get("profit_factor", language),
                    value = "2.45",
                    color = ApexCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsMetricBox(
                    label = Strings.get("sharpe_ratio", language),
                    value = "1.82",
                    color = ApexGold,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMetricBox(
                    label = Strings.get("max_drawdown", language),
                    value = "-4.2%",
                    color = BearishRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Asset Allocation Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(Strings.get("asset_allocation", language), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    AllocationBar(label = if (language == AppLanguage.ARABIC) "الأسهم والمؤشرات" else "Equities & Indices", percent = 45, color = ApexCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    AllocationBar(label = if (language == AppLanguage.ARABIC) "أزواج الفوركس (MT5)" else "Forex Majors (MT5)", percent = 30, color = BullishGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    AllocationBar(label = if (language == AppLanguage.ARABIC) "مشتقات العملات الرقمية" else "Crypto Derivatives", percent = 15, color = ApexGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    AllocationBar(label = if (language == AppLanguage.ARABIC) "السلع (الذهب / النفط)" else "Commodities (Gold/Oil)", percent = 10, color = BearishRed)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AnalyticsMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AllocationBar(label: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
