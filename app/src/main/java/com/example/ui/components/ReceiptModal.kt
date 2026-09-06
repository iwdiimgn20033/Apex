package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.PayPalBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptModal(
    transaction: Transaction,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isDeposit = transaction.isIncoming
    val formattedDate = SimpleDateFormat("MMM dd, yyyy - HH:mm:ss z", Locale.US).format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("receipt_modal"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Secured",
                        tint = BullishGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "SEC & FINRA Verified",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BullishGreen
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_receipt_button")) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Verified Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BullishGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = BullishGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isDeposit) {
                    if (language == AppLanguage.ARABIC) "إيصال إيداع معتمد" else "Official Deposit Receipt"
                } else {
                    if (language == AppLanguage.ARABIC) "كشف سحب نقدي معتمد" else "Official Cash Withdrawal Statement"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${if (isDeposit) "+" else "-"}$${String.format(Locale.US, "%,.2f", transaction.amount)} ${transaction.currency}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isDeposit) BullishGreen else BearishRed
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Details List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "رقم المعاملة:" else "Transaction ID:",
                    transaction.id
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "كود التوثيق المالي:" else "Reference Hash:",
                    transaction.referenceHash,
                    isMonospace = true
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "طريقة التحويل:" else "Payment Method:",
                    if (transaction.method == PaymentMethod.BANK_TRANSFER && language == AppLanguage.ARABIC) "تحويل بنكي مباشر (Wire/IBAN)" else transaction.method.displayName
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "التاريخ والوقت:" else "Timestamp:",
                    formattedDate
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "الحساب المستلم / الوجهة:" else "Account / Destination:",
                    transaction.accountDestination
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "رسوم المعاملة:" else "Network & Processing Fee:",
                    if (transaction.fee == 0.0) {
                        if (language == AppLanguage.ARABIC) "$0.00 (إعفاء VIP)" else "$0.00 (VIP Zero-Fee)"
                    } else "$${String.format(Locale.US, "%.2f", transaction.fee)} USD"
                )
                ReceiptRow(
                    if (language == AppLanguage.ARABIC) "حالة التحقق والامتثال:" else "Compliance Status:",
                    if (language == AppLanguage.ARABIC) "موثق ومطابق (AML/KYC Cleared)" else "AML / KYC Cleared (Tier 2)"
                )
                if (transaction.notes.isNotEmpty()) {
                    ReceiptRow(
                        if (language == AppLanguage.ARABIC) "ملاحظات بوابة الصرف:" else "Gateway Note:",
                        transaction.notes
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer action
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("dismiss_receipt_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    if (language == AppLanguage.ARABIC) "مشاركة وتحميل الإيصال الرقمي" else "Export & Share Digital Receipt",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isMonospace: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
