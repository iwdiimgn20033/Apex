package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexGold
import com.example.ui.theme.BullishGreen

@Composable
fun DocsScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val isAr = language == AppLanguage.ARABIC

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("docs_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isAr) "المخطط الهيكلي وتكامل واجهات برمجة التطبيقات (API)" else "Full-Stack Architecture & API Integration Blueprint",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isAr) "تصميم نظام الوساطة المالية وتداول الأسهم والفوركس" else "Enterprise Stock & Forex Brokerage System Design",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: MetaTrader 5 Bridge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "1. بوابة MetaTrader 5 (MT5) وبروتوكول FIX" else "1. MetaTrader 5 (MT5) Gateway & FIX Protocol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr)
                            "• الاتصال: بروتوكول FIX 4.4 / MT5 Manager API عبر WebSocket وتدفق TCP مباشر.\n" +
                            "• بث الأسعار: أسعار لحظية وشموع يابانية مجمعة بتأخير أقل من 20 مللي ثانية.\n" +
                            "• توجيه الأوامر: وصول مباشر للأسواق (DMA) ومعالجة مستقيمة (STP).\n" +
                            "• محرك الهامش: حساب ديناميكي للرافعة المالية وحماية الإيقاف الإجباري عند مستوى هامش 50%."
                        else
                            "• Connection: FIX 4.4 / MetaQuotes MT5 Manager API via WebSocket and TCP binary stream.\n" +
                            "• Market Feed: Real-time tick & aggregated candlestick bars with sub-20ms latency.\n" +
                            "• Order Routing: Direct Market Access (DMA) & STP Straight-Through-Processing.\n" +
                            "• Margin Engine: Real-time dynamic leverage calculation and automated stop-out guard at 50% margin level.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 2: PayPal API Integration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "2. واجهة PayPal REST API v2 ومعالجة المدفوعات" else "2. PayPal REST API v2 & Payment Processing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BullishGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr)
                            "• نقطة النهاية: /v2/checkout/orders (الغرض: CAPTURE).\n" +
                            "• مستمع Webhook: معالجة إشعارات الدفع مع التحقق من توقيع HMAC-SHA256.\n" +
                            "• تسوية فورية: إضافة الرصيد إلى قاعدة بيانات Room ورصيد MT5 النقدي فوراً.\n" +
                            "• مكافحة غسيل الأموال والامتثال: فحص القوائم المحظورة والتحقق المشفر من المعاملات."
                        else
                            "• Endpoint: /v2/checkout/orders (Intent: CAPTURE)\n" +
                            "• Webhook Listener: /api/v1/webhooks/paypal with HMAC-SHA256 signature verification.\n" +
                            "• Instant Settlement: Dispatched to Room Database & MT5 Cash Balance in real time.\n" +
                            "• AML & Compliance: Automated Sanction list check & transaction hash verification.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 3: Room Database & Offline Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "3. قاعدة بيانات Room للعمل بدون إنترنت وهيكل MVVM" else "3. Offline-First Room DB & Reactive MVVM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr)
                            "• التخزين المحلي: قاعدة بيانات SQLite Room لتخزين الأصول، الأوامر، التوصيات والإيصالات.\n" +
                            "• المزامنة الثنائية: تبديل سلس بين بث MT5 الحي والبيانات المحفوظة محلياً.\n" +
                            "• إدارة الحالة: استخدام Coroutines Flow و StateFlow لتحديث واجهة Jetpack Compose تفاعلياً."
                        else
                            "• Local Storage: SQLite backed Room Database caching Assets, Orders, Signals, and Receipts.\n" +
                            "• Bi-directional Sync: Seamless switch between live MT5 tick streams and offline cached local records.\n" +
                            "• State Management: Coroutines Flow and StateFlow providing declarative Jetpack Compose UI binding.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 4: Security & Compliance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "4. الأمان والامتثال للتشريعات المالية (MiFID II / FINRA)" else "4. Security & Regulatory Compliance (MiFID II / FINRA)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr)
                            "• المصادقة الثنائية 2FA: خوارزمية RFC 6238 TOTP.\n" +
                            "• المقاييس الحيوية: تكامل مع مستشعر البصمة والوجه عبر Android BiometricPrompt.\n" +
                            "• سجل التدقيق: إيصالات معاملات غير قابلة للتعديل برمز تجزئة مشفر."
                        else
                            "• 2FA Authenticator: RFC 6238 TOTP algorithm.\n" +
                            "• Biometrics: Android BiometricPrompt hardware keystore integration.\n" +
                            "• Audit Log: Immutable cryptographic transaction reference receipts.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
