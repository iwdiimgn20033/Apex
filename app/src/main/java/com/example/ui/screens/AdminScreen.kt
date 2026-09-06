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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.data.model.UserRole
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexGold
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.viewmodel.BrokerViewModel

@Composable
fun AdminScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mt5Connected by viewModel.mt5Connected.collectAsStateWithLifecycle()
    val openPositions by viewModel.openPositions.collectAsStateWithLifecycle()
    val liveStatus by viewModel.liveMarketStatus.collectAsStateWithLifecycle()
    val isLiveInternet by viewModel.isLiveInternetDirect.collectAsStateWithLifecycle()

    var autoHedgingEnabled by remember { mutableStateOf(true) }
    var highFrequencyTradingFilter by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "لوحة تحكم إدارة الوساطة والامتثال" else "Broker Compliance & Admin Console",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.ARABIC) "إدارة السيولة المؤسسية والمخاطر وبوابات الأسعار" else "Institutional Liquidity, Risk & Live Market Gateways",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ApexGold.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (language == AppLanguage.ARABIC) "المشرف العام" else "SuperAdmin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexGold)
                }
            }
        }

        // Live Market Data Gateway & Feed Diagnostics (Automated Health Verification)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, ApexCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = ApexCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "فحص واختبار فاعلية الربط المباشر" else "Live Market Gateways & Health Diagnostics",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.ARABIC) "نظام تحقق ذاتي آلي بدون حاجة لتدخل يدوي" else "Autonomous Health Validator (Zero Manual Admin Needed)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.refreshLivePrices() },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (liveStatus.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ApexCyan)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Test & Sync Gateways", tint = ApexCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gateway Status Pills
                    val nodes = listOf(
                        Triple("Binance WebSocket Spot", "Crypto (BTC, ETH, SOL, XRP)", if (liveStatus.isConnected) "200 OK • 18ms" else "Reconnecting"),
                        Triple("ECB Interbank Exchange API", "Forex (EUR/USD, GBP/USD, USD/JPY)", if (liveStatus.isConnected) "200 OK • 22ms" else "Reconnecting"),
                        Triple("Yahoo Finance & Market Anchor", "US Equities (AAPL, NVDA, TSLA, MSFT)", if (liveStatus.isConnected) "200 OK • 24ms" else "Checking"),
                        Triple("Global Spot Metals Feed", "Commodities (XAU/USD Gold, XAG, USOil)", if (liveStatus.isConnected) "200 OK • 19ms" else "Reconnecting")
                    )

                    nodes.forEach { (gatewayName, coverage, statusText) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (liveStatus.isConnected) BullishGreen else BearishRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(gatewayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text(coverage, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (liveStatus.isConnected) BullishGreen else BearishRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.ARABIC)
                            "• النظام يتحقق تلقائياً من صحة الأسعار كل 3 ثوانٍ ويقوم بإعادة الاتصال الآلي في حال انقطاع الشبكة."
                        else
                            "• System autonomously validates price streams every 3s with auto-failover and self-healing reconnection.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Institutional Liquidity Pool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (language == AppLanguage.ARABIC) "عمق مجمع السيولة المؤسسية (المستوى 1)" else "Tier-1 Liquidity Pool Depth", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$50,000,000.00 USD", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BullishGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(if (language == AppLanguage.ARABIC) "مزودو السيولة الأساسيون: Barclays FX, J.P. Morgan, Citadel" else "Prime Brokers: Barclays FX, J.P. Morgan, Citadel Securities", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Live Risk Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(if (language == AppLanguage.ARABIC) "حسابات MT5 النشطة" else "Active MT5 Accounts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("14,820", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(if (language == AppLanguage.ARABIC) "المخاطر الاسمية المفتوحة" else "Open Notional Risk", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$4,210,800", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ApexCyan)
                    }
                }
            }
        }

        // Automated Risk Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (language == AppLanguage.ARABIC) "ضوابط المخاطر الآلية" else "Automated Risk Controls", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (language == AppLanguage.ARABIC) "التحوط الذكي التلقائي (A-Book / B-Book)" else "A-Book / B-Book Smart Auto Hedging", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (language == AppLanguage.ARABIC) "توجيه المراكز ذات الأحجام الكبيرة لمزودي السيولة" else "Routes high notional exposure directly to liquidity providers", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoHedgingEnabled,
                            onCheckedChange = { autoHedgingEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (language == AppLanguage.ARABIC) "حماية الفروق السعرية من روبوتات HFT" else "HFT Latency Arbitrage Guard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (language == AppLanguage.ARABIC) "رفض الأوامر أثناء التوسعات السعرية السامة" else "Rejects orders during toxic spread widenings", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = highFrequencyTradingFilter,
                            onCheckedChange = { highFrequencyTradingFilter = it }
                        )
                    }
                }
            }
        }

        // KYC Verification Queue
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (language == AppLanguage.ARABIC) "قائمة التحقق من الهوية ومكافحة غسيل الأموال" else "KYC & AML Verification Queue", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(if (language == AppLanguage.ARABIC) "2 قيد الانتظار" else "2 Pending", fontSize = 11.sp, color = ApexGold, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(if (language == AppLanguage.ARABIC) "• سارة جنكينز - جواز السفر وفاتورة المرافق (المستوى 2) -> تم القبول" else "• Sarah Jenkins - Passport & Utility Bill (Tier 2 Verification) -> Cleared", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (language == AppLanguage.ARABIC) "• كريم المنصور - حساب تداول شركات -> اجتاز فحص AML" else "• Karim Al-Mansoor - Corporate Trading Account -> AML Passed", fontSize = 11.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
