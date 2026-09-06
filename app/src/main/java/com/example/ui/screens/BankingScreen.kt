package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import com.example.data.model.PaymentMethod
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.service.PayPalWebhookEvent
import com.example.ui.components.ReceiptModal
import com.example.ui.components.WebhookPayloadModal
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.Strings
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.PayPalBlue
import com.example.ui.viewmodel.BrokerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BankPreset(
    val id: String,
    val bankNameEn: String,
    val bankNameAr: String,
    val beneficiaryEn: String,
    val beneficiaryAr: String,
    val iban: String,
    val swift: String,
    val countryEn: String,
    val countryAr: String,
    val isPrimary: Boolean = false
)

private val SAMPLE_SAVED_BANKS = listOf(
    BankPreset(
        id = "BANK-01",
        bankNameEn = "JPMorgan Chase Bank, N.A.",
        bankNameAr = "جي بي مورغان تشيس بنك (JPMorgan)",
        beneficiaryEn = "Alexander Vance",
        beneficiaryAr = "ألكسندر فانس",
        iban = "US33 CHAS 0210 0002 1482 9104 88",
        swift = "CHASUS33",
        countryEn = "United States (USD)",
        countryAr = "الولايات المتحدة (USD)",
        isPrimary = true
    ),
    BankPreset(
        id = "BANK-02",
        bankNameEn = "Al Rajhi Bank",
        bankNameAr = "مصرف الراجحي",
        beneficiaryEn = "Alexander Vance",
        beneficiaryAr = "ألكسندر فانس",
        iban = "SA03 8000 0000 6080 1016 7102",
        swift = "RJHISARI",
        countryEn = "Saudi Arabia (SAR / USD)",
        countryAr = "المملكة العربية السعودية (SAR / USD)"
    ),
    BankPreset(
        id = "BANK-03",
        bankNameEn = "Emirates NBD Bank",
        bankNameAr = "بنك الإمارات دبي الوطني",
        beneficiaryEn = "Alexander Vance",
        beneficiaryAr = "ألكسندر فانس",
        iban = "AE29 0260 0010 0123 4567 891",
        swift = "EBILAEAD",
        countryEn = "United Arab Emirates (AED / USD)",
        countryAr = "الإمارات العربية المتحدة (AED / USD)"
    )
)

@Composable
fun BankingScreen(
    viewModel: BrokerViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val selectedReceipt by viewModel.selectedReceipt.collectAsStateWithLifecycle()

    val webhookStatus by viewModel.webhookListenerStatus.collectAsStateWithLifecycle()
    val webhookEvents by viewModel.webhookEvents.collectAsStateWithLifecycle()
    val trackedWithdrawals by viewModel.trackedWithdrawalIds.collectAsStateWithLifecycle()
    val selectedWebhookEvent by viewModel.selectedWebhookEvent.collectAsStateWithLifecycle()

    var activeBankingTab by remember { mutableIntStateOf(0) } // 0: Deposit, 1: Withdraw, 2: History
    var selectedHistoryFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Withdrawals, 2: Deposits
    var selectedDepositMethod by remember { mutableStateOf(PaymentMethod.PAYPAL) }
    var selectedWithdrawMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    
    var depositAmountText by remember { mutableStateOf("5000") }
    var paypalEmailText by remember { mutableStateOf(userProfile.email) }
    
    // Bank Withdrawal State - directly editable fields
    var selectedBankPresetId by remember { mutableStateOf("BANK-01") }
    var withdrawAmountText by remember { mutableStateOf("2500") }
    
    val initialBank = SAMPLE_SAVED_BANKS.first()
    var customBankName by remember { mutableStateOf(if (language == AppLanguage.ARABIC) initialBank.bankNameAr else initialBank.bankNameEn) }
    var customBeneficiaryName by remember { mutableStateOf(userProfile.fullName) }
    var customIban by remember { mutableStateOf(initialBank.iban) }
    var customSwift by remember { mutableStateOf(initialBank.swift) }
    var customBankCountry by remember { mutableStateOf(if (language == AppLanguage.ARABIC) initialBank.countryAr else initialBank.countryEn) }
    
    // Other withdraw targets
    var withdrawPayPalEmail by remember { mutableStateOf(userProfile.email) }
    var withdrawCryptoAddress by remember { mutableStateOf("TReZ9X3uPk7Yv1w8Nq4Mt9La2Jk6Hb8Qe") }
    var withdrawCardNumber by remember { mutableStateOf("•••• •••• •••• 9214") }

    val activeBankPreset = SAMPLE_SAVED_BANKS.find { it.id == selectedBankPresetId } ?: SAMPLE_SAVED_BANKS.first()

    val parsedWithdrawAmount = withdrawAmountText.toDoubleOrNull() ?: 0.0
    val isWithdrawAmountValid = parsedWithdrawAmount > 0 && parsedWithdrawAmount <= userProfile.freeMargin

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("banking_screen")
    ) {
        // Balance Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("banking_balance_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.get("available_balance", language),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", userProfile.freeMargin)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${Strings.get("total_equity_live", language)}: $${String.format(Locale.US, "%,.2f", userProfile.equity)}",
                        fontSize = 11.sp,
                        color = ApexCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BullishGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.ARABIC) "سحب بنكي فوري 0% رسوم" else "Instant 0% Wire Fee",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BullishGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Banking Tabs Selector (Deposit, Withdraw, History)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf(
                Strings.get("deposit", language),
                Strings.get("withdraw", language),
                Strings.get("transaction_history", language)
            ).forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeBankingTab == index) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { activeBankingTab = index }
                        .padding(vertical = 10.dp)
                        .testTag("banking_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeBankingTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeBankingTab) {
            0 -> {
                // Deposit Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "اختر طريقة الإيداع" else "Select Deposit Gateway",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                PaymentMethod.PAYPAL to Strings.get("paypal_instant", language),
                                PaymentMethod.CREDIT_DEBIT_CARD to Strings.get("credit_card", language),
                                PaymentMethod.BANK_TRANSFER to Strings.get("bank_wire", language)
                            ).forEach { (method, title) ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDepositMethod = method }
                                        .border(
                                            width = if (selectedDepositMethod == method) 2.dp else 0.dp,
                                            color = if (selectedDepositMethod == method) ApexCyan else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when (method) {
                                                PaymentMethod.PAYPAL -> Icons.Default.Payment
                                                PaymentMethod.CREDIT_DEBIT_CARD -> Icons.Default.CreditCard
                                                else -> Icons.Default.AccountBalance
                                            },
                                            contentDescription = null,
                                            tint = if (method == PaymentMethod.PAYPAL) PayPalBlue else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Amount presets
                        Text(
                            text = if (language == AppLanguage.ARABIC) "مبالغ سريعة (دولار)" else "Preset Amounts (USD)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1000", "2500", "5000", "10000", "25000").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (depositAmountText == preset) ApexCyan else MaterialTheme.colorScheme.surface)
                                        .clickable { depositAmountText = preset }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$$preset",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (depositAmountText == preset) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = depositAmountText,
                            onValueChange = { depositAmountText = it },
                            label = { Text(Strings.get("deposit_amount", language)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_deposit_amount"),
                            singleLine = true
                        )
                    }

                    if (selectedDepositMethod == PaymentMethod.PAYPAL) {
                        item {
                            OutlinedTextField(
                                value = paypalEmailText,
                                onValueChange = { paypalEmailText = it },
                                label = { Text(Strings.get("paypal_email", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_paypal_email"),
                                singleLine = true
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                val amount = depositAmountText.toDoubleOrNull() ?: 1000.0
                                viewModel.depositPayPal(amount, paypalEmailText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_deposit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedDepositMethod == PaymentMethod.PAYPAL) PayPalBlue else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "${Strings.get("confirm_deposit", language)} ($$depositAmountText)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            1 -> {
                // Withdraw Tab (with prominent Bank Cash Withdrawal option)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = Strings.get("select_withdrawal_method", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Withdrawal Gateway Selector
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                PaymentMethod.BANK_TRANSFER to (if (language == AppLanguage.ARABIC) "سحب نقدي للبنك" else "Bank Wire"),
                                PaymentMethod.PAYPAL to Strings.get("paypal_instant", language),
                                PaymentMethod.CREDIT_DEBIT_CARD to (if (language == AppLanguage.ARABIC) "بطاقة بنكية" else "Card Payout"),
                                PaymentMethod.CRYPTO_USDT to (if (language == AppLanguage.ARABIC) "كريبتو USDT" else "USDT Crypto")
                            ).forEach { (method, title) ->
                                val isSelected = selectedWithdrawMethod == method
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedWithdrawMethod = method }
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) BullishGreen else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .testTag("withdraw_method_${method.name}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when (method) {
                                                PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                                                PaymentMethod.PAYPAL -> Icons.Default.Payment
                                                PaymentMethod.CREDIT_DEBIT_CARD -> Icons.Default.CreditCard
                                                PaymentMethod.CRYPTO_USDT -> Icons.Default.CurrencyBitcoin
                                                else -> Icons.Default.AccountBalance
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) BullishGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bank Cash Withdrawal Details - Direct Active IBAN & Bank Editing
                    if (selectedWithdrawMethod == PaymentMethod.BANK_TRANSFER) {
                        item {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "حسابات بنكية محفوظة (اضغط للتعبئة السريعة):" else "Saved Bank Presets (Tap to Auto-Fill):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Saved Banks Quick Selection Chips/Cards
                        items(SAMPLE_SAVED_BANKS) { bank ->
                            val isSelected = selectedBankPresetId == bank.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBankPresetId = bank.id
                                        customBankName = if (language == AppLanguage.ARABIC) bank.bankNameAr else bank.bankNameEn
                                        customIban = bank.iban
                                        customSwift = bank.swift
                                        customBankCountry = if (language == AppLanguage.ARABIC) bank.countryAr else bank.countryEn
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) ApexCyan else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("saved_bank_${bank.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedBankPresetId = bank.id
                                            customBankName = if (language == AppLanguage.ARABIC) bank.bankNameAr else bank.bankNameEn
                                            customIban = bank.iban
                                            customSwift = bank.swift
                                            customBankCountry = if (language == AppLanguage.ARABIC) bank.countryAr else bank.countryEn
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = ApexCyan)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (language == AppLanguage.ARABIC) bank.bankNameAr else bank.bankNameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (bank.isPrimary) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(ApexCyan.copy(alpha = 0.2f))
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = if (language == AppLanguage.ARABIC) "رئيسي" else "Primary",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ApexCyan
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "IBAN: ${bank.iban}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = ApexCyan
                                        )
                                    }
                                }
                            }
                        }

                        // Active Editable Bank Details Form
                        item {
                            Text(
                                text = if (language == AppLanguage.ARABIC) "بيانات الحساب البنكي ورقم الآيبان (يمكنك الكتابة والتعديل):" else "Bank Account & IBAN Details (Fully Editable):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // IBAN Field (Primary active text input)
                        item {
                            OutlinedTextField(
                                value = customIban,
                                onValueChange = { customIban = it },
                                label = { Text(Strings.get("bank_account_iban", language)) },
                                placeholder = { Text("e.g. SA0380000000608010167102 / US33CHAS021000021482910488") },
                                supportingText = {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "اكتب رقم الآيبان أو الحساب البنكي الخاص بك هنا" else "Type or paste your IBAN / Bank Account number here",
                                        fontSize = 11.sp,
                                        color = ApexCyan
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_bank_iban"),
                                singleLine = true
                            )
                        }

                        // Bank Name Field
                        item {
                            OutlinedTextField(
                                value = customBankName,
                                onValueChange = { customBankName = it },
                                label = { Text(Strings.get("bank_name", language)) },
                                placeholder = { Text("e.g. Al Rajhi Bank, JPMorgan Chase, SNB, HSBC") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_bank_name"),
                                singleLine = true
                            )
                        }

                        // Beneficiary Full Name
                        item {
                            OutlinedTextField(
                                value = customBeneficiaryName,
                                onValueChange = { customBeneficiaryName = it },
                                label = { Text(Strings.get("beneficiary_name", language)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_bank_beneficiary"),
                                singleLine = true
                            )
                        }

                        // SWIFT / BIC & Country Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customSwift,
                                    onValueChange = { customSwift = it },
                                    label = { Text(Strings.get("swift_bic", language)) },
                                    placeholder = { Text("CHASUS33") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_bank_swift"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customBankCountry,
                                    onValueChange = { customBankCountry = it },
                                    label = { Text(Strings.get("bank_country", language)) },
                                    placeholder = { Text("e.g. KSA / USA / UAE") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_bank_country"),
                                    singleLine = true
                                )
                            }
                        }
                    } else if (selectedWithdrawMethod == PaymentMethod.PAYPAL) {
                        item {
                            OutlinedTextField(
                                value = withdrawPayPalEmail,
                                onValueChange = { withdrawPayPalEmail = it },
                                label = { Text(Strings.get("paypal_email", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_withdraw_paypal"),
                                singleLine = true
                            )
                        }
                    } else if (selectedWithdrawMethod == PaymentMethod.CRYPTO_USDT) {
                        item {
                            OutlinedTextField(
                                value = withdrawCryptoAddress,
                                onValueChange = { withdrawCryptoAddress = it },
                                label = { Text(Strings.get("crypto_wallet_address", language)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_withdraw_crypto"),
                                singleLine = true
                            )
                        }
                    } else if (selectedWithdrawMethod == PaymentMethod.CREDIT_DEBIT_CARD) {
                        item {
                            OutlinedTextField(
                                value = withdrawCardNumber,
                                onValueChange = { withdrawCardNumber = it },
                                label = { Text(if (language == AppLanguage.ARABIC) "رقم البطاقة المستلمة (Visa/Mastercard)" else "Destination Card Number") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_withdraw_card"),
                                singleLine = true
                            )
                        }
                    }

                    // Quick Preset Amount Selector for Withdrawal
                    item {
                        Text(
                            text = if (language == AppLanguage.ARABIC) "تحديد مبلغ السحب (USD)" else "Select Cash Withdrawal Amount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("500", "1000", "2500", "5000", "10000").forEach { preset ->
                                val isSelected = withdrawAmountText == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BullishGreen else MaterialTheme.colorScheme.surface)
                                        .clickable { withdrawAmountText = preset }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$$preset",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            // All Balance Button
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (withdrawAmountText == userProfile.freeMargin.toString()) ApexCyan else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        withdrawAmountText = String.format(Locale.US, "%.2f", userProfile.freeMargin)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get("all_balance", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (withdrawAmountText == userProfile.freeMargin.toString()) Color.Black else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Custom Amount Text Field
                    item {
                        OutlinedTextField(
                            value = withdrawAmountText,
                            onValueChange = { withdrawAmountText = it },
                            label = { Text(Strings.get("withdraw_amount", language)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = !isWithdrawAmountValid && parsedWithdrawAmount > 0,
                            supportingText = {
                                if (parsedWithdrawAmount > userProfile.freeMargin) {
                                    Text(
                                        text = if (language == AppLanguage.ARABIC) "المبلغ يتجاوز الرصيد الحر المتاح ($${String.format(Locale.US, "%,.2f", userProfile.freeMargin)})" else "Amount exceeds free margin ($${String.format(Locale.US, "%,.2f", userProfile.freeMargin)})",
                                        color = BearishRed,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = "${Strings.get("available_balance", language)}: $${String.format(Locale.US, "%,.2f", userProfile.freeMargin)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_withdraw_amount"),
                            singleLine = true
                        )
                    }

                    // Security & Speed Guarantee Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = BullishGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Strings.get("bank_security_guarantee", language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = ApexCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Strings.get("withdrawal_speed", language),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Cash Withdrawal Breakdown Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = Strings.get("withdrawal_summary", language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (language == AppLanguage.ARABIC) "المبلغ المطلوب:" else "Requested Amount:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$$withdrawAmountText USD", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (language == AppLanguage.ARABIC) "رسوم الحوالة البنكية:" else "Wire Transfer Fee:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (language == AppLanguage.ARABIC) "$0.00 (إعفاء VIP)" else "$0.00 (VIP Promo)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (language == AppLanguage.ARABIC) "بروتوكول المقاصة:" else "Clearing Protocol:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("MT5 FIX 4.4 Instant Wire", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ApexCyan)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.get("net_payout", language),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$$withdrawAmountText USD",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BullishGreen
                                    )
                                }
                            }
                        }
                    }

                    // Execute Withdrawal Request Button (زر تنفيذ عملية السحب)
                    item {
                        Button(
                            onClick = {
                                val amount = parsedWithdrawAmount
                                if (amount <= 0 || amount > userProfile.freeMargin) return@Button

                                val destinationSummary = when (selectedWithdrawMethod) {
                                    PaymentMethod.BANK_TRANSFER -> {
                                        "Bank: $customBankName | IBAN: $customIban | SWIFT: $customSwift | Ben: $customBeneficiaryName"
                                    }
                                    PaymentMethod.PAYPAL -> "PayPal: $withdrawPayPalEmail"
                                    PaymentMethod.CRYPTO_USDT -> "USDT (TRC20): $withdrawCryptoAddress"
                                    PaymentMethod.CREDIT_DEBIT_CARD -> "Card: $withdrawCardNumber"
                                    else -> "Direct Wire"
                                }

                                val customNote = when (selectedWithdrawMethod) {
                                    PaymentMethod.BANK_TRANSFER -> {
                                        "Direct Wire Transfer to $customBankName ($customBeneficiaryName) • Same-Day Settlement"
                                    }
                                    else -> "Instant Payout via ${selectedWithdrawMethod.displayName}"
                                }

                                viewModel.requestWithdrawal(
                                    amount = amount,
                                    method = selectedWithdrawMethod,
                                    destination = destinationSummary,
                                    notes = customNote
                                )
                            },
                            enabled = isWithdrawAmountValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("execute_withdrawal_button")
                                .testTag("submit_withdraw_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedWithdrawMethod == PaymentMethod.BANK_TRANSFER) BullishGreen else MaterialTheme.colorScheme.primary,
                                contentColor = if (selectedWithdrawMethod == PaymentMethod.BANK_TRANSFER) Color.Black else MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(
                                imageVector = if (selectedWithdrawMethod == PaymentMethod.BANK_TRANSFER) Icons.Default.AccountBalance else Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${Strings.get("execute_withdrawal", language)} ($$withdrawAmountText)",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
            2 -> {
                // Transaction & Cash-Out Withdrawal History Screen
                val filteredTransactions = remember(transactions, selectedHistoryFilter) {
                    when (selectedHistoryFilter) {
                        1 -> transactions.filter { !it.isIncoming }
                        2 -> transactions.filter { it.isIncoming }
                        else -> transactions
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("withdrawal_history_screen"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Filter Chips Row (All, Withdrawals, Deposits)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                0 to Strings.get("filter_all", language),
                                1 to Strings.get("filter_withdrawals", language),
                                2 to Strings.get("filter_deposits", language)
                            ).forEach { (filterIndex, filterLabel) ->
                                val isSelected = selectedHistoryFilter == filterIndex
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedHistoryFilter = filterIndex }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                        .testTag("filter_history_$filterIndex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filterLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // PayPal Sandbox Webhook Listener Live Monitor Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("paypal_webhook_listener_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(BullishGreen)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Strings.get("paypal_webhook_listener_title", language),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PayPalBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Sandbox API",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PayPalBlue
                                        )
                                    }
                                }

                                Text(
                                    text = Strings.get("webhook_active_status", language),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.get("paypal_webhook_endpoint", language),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = PayPalBlue
                                    )
                                    Text(
                                        text = "Events: ${webhookStatus.totalEventsReceived}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (webhookStatus.totalEventsReceived > 0) BullishGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (trackedWithdrawals.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ApexCyan.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Sync,
                                                contentDescription = null,
                                                tint = ApexCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (language == AppLanguage.ARABIC) {
                                                    "جاري تتبع ${trackedWithdrawals.size} عملية سحب عبر مستمع الويب هوك..."
                                                } else {
                                                    "Tracking ${trackedWithdrawals.size} active payout(s) via Webhook listener..."
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ApexCyan
                                            )
                                        }
                                    }
                                }

                                if (webhookEvents.isNotEmpty()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    Text(
                                        text = Strings.get("recent_webhook_events", language),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        webhookEvents.take(5).forEach { evt ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, PayPalBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.selectWebhookEvent(evt) }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    .testTag("webhook_event_chip_${evt.eventId}")
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Code,
                                                        contentDescription = null,
                                                        tint = PayPalBlue,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${evt.eventId} • ${evt.status}",
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cash Settlement Link & Gateway Verification Banner
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cash_settlement_verification_banner"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = ApexCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Strings.get("payout_cash_link_title", language),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = Strings.get("payout_cash_link_desc", language),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (filteredTransactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get("no_transactions", language),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredTransactions) { tx ->
                            val isTracked = trackedWithdrawals.contains(tx.id)
                            val matchingWebhookEvent = webhookEvents.find { it.transactionId == tx.id }

                            TransactionRowItem(
                                transaction = tx,
                                language = language,
                                isCurrentlyTracked = isTracked,
                                matchingWebhookEvent = matchingWebhookEvent,
                                onForceVerify = {
                                    viewModel.forceWebhookVerification(tx.id, tx.accountDestination, tx.amount)
                                },
                                onInspectWebhook = {
                                    if (matchingWebhookEvent != null) {
                                        viewModel.selectWebhookEvent(matchingWebhookEvent)
                                    } else {
                                        // Build a synthetic event from transaction
                                        val syntheticEvent = PayPalWebhookEvent(
                                            eventId = "WH-EVT-" + tx.id.takeLast(6),
                                            eventType = "PAYMENT.PAYOUTS-ITEM.SUCCEEDED",
                                            summary = "PayPal Payout Item verified via Sandbox API.",
                                            summaryAr = "تم التحقق من بند السحب عبر بوابة PayPal Sandbox.",
                                            transactionId = tx.id,
                                            payoutBatchId = "BATCH-" + tx.referenceHash.takeLast(6),
                                            payoutItemId = "ITEM-" + tx.id,
                                            receiverDestination = tx.accountDestination,
                                            amount = tx.amount,
                                            currency = tx.currency,
                                            status = if (tx.status == TransactionStatus.COMPLETED) "SUCCESS" else tx.status.name,
                                            rawPayloadJson = """
                                            {
                                              "id": "WH-EVT-${tx.id.takeLast(6)}",
                                              "event_type": "PAYMENT.PAYOUTS-ITEM.SUCCEEDED",
                                              "summary": "Payout item ${tx.status.name} for ${tx.accountDestination}",
                                              "resource": {
                                                "payout_batch_id": "BATCH-${tx.referenceHash.takeLast(6)}",
                                                "payout_item_id": "ITEM-${tx.id}",
                                                "transaction_status": "${if (tx.status == TransactionStatus.COMPLETED) "SUCCESS" else tx.status.name}",
                                                "payout_item": {
                                                  "amount": { "value": "${String.format(Locale.US, "%.2f", tx.amount)}", "currency": "USD" },
                                                  "receiver": "${tx.accountDestination}"
                                                }
                                              }
                                            }
                                            """.trimIndent()
                                        )
                                        viewModel.selectWebhookEvent(syntheticEvent)
                                    }
                                },
                                onViewReceipt = { viewModel.showReceipt(tx) }
                            )
                        }
                    }
                }
            }
        }

        // Receipt Modal
        selectedReceipt?.let { tx ->
            ReceiptModal(
                transaction = tx,
                language = language,
                onDismiss = { viewModel.dismissReceipt() }
            )
        }

        // Webhook Payload Modal
        selectedWebhookEvent?.let { event ->
            WebhookPayloadModal(
                event = event,
                language = language,
                onDismiss = { viewModel.selectWebhookEvent(null) }
            )
        }
    }
}

@Composable
private fun TransactionRowItem(
    transaction: Transaction,
    language: AppLanguage,
    isCurrentlyTracked: Boolean = false,
    matchingWebhookEvent: PayPalWebhookEvent? = null,
    onForceVerify: () -> Unit = {},
    onInspectWebhook: () -> Unit = {},
    onViewReceipt: () -> Unit
) {
    val isIncoming = transaction.isIncoming
    val formattedDate = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.US).format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon + Method + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isIncoming) BullishGreen.copy(alpha = 0.15f)
                                else if (transaction.method == PaymentMethod.BANK_TRANSFER) ApexCyan.copy(alpha = 0.15f)
                                else if (transaction.method == PaymentMethod.PAYPAL) PayPalBlue.copy(alpha = 0.15f)
                                else BearishRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (transaction.method) {
                                PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                                PaymentMethod.PAYPAL -> Icons.Default.Payment
                                PaymentMethod.CREDIT_DEBIT_CARD -> Icons.Default.CreditCard
                                PaymentMethod.CRYPTO_USDT -> Icons.Default.CurrencyBitcoin
                                else -> if (isIncoming) Icons.Default.Payment else Icons.Default.Receipt
                            },
                            contentDescription = null,
                            tint = when (transaction.method) {
                                PaymentMethod.BANK_TRANSFER -> ApexCyan
                                PaymentMethod.PAYPAL -> PayPalBlue
                                else -> if (isIncoming) BullishGreen else BearishRed
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (transaction.method == PaymentMethod.BANK_TRANSFER && language == AppLanguage.ARABIC) {
                                "سحب نقدي للبنك (Bank Wire)"
                            } else if (transaction.method == PaymentMethod.PAYPAL && language == AppLanguage.ARABIC) {
                                "سحب نقدي عبر PayPal"
                            } else {
                                transaction.method.displayName
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncoming) "+" else "-"}$${String.format(Locale.US, "%,.2f", transaction.amount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (isIncoming) BullishGreen else BearishRed
                    )
                    Text(
                        text = "${Strings.get("view_receipt", language)} ->",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ApexCyan
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Destination Account / Target line
            if (transaction.accountDestination.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("destination_target", language) + ":",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.accountDestination,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            // Real-Time Webhook Status & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (transaction.status) {
                    TransactionStatus.PENDING -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.get("webhook_pending_badge", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB300)
                                )
                            }
                        }
                    }
                    TransactionStatus.PROCESSING -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PayPalBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = PayPalBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.get("webhook_processing_badge", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PayPalBlue
                                )
                            }
                        }
                    }
                    TransactionStatus.COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BullishGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BullishGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.get("webhook_verified_badge", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BullishGreen
                                )
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BearishRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = transaction.status.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearishRed
                            )
                        }
                    }
                }

                // Action Affordances: Instant Sandbox Verify / Webhook Payload Inspector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (transaction.status == TransactionStatus.PENDING || transaction.status == TransactionStatus.PROCESSING) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PayPalBlue)
                                .clickable { onForceVerify() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("verify_now_${transaction.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = Strings.get("verify_now_btn", language),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else if (transaction.method == PaymentMethod.PAYPAL || transaction.notes.contains("Webhook", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onInspectWebhook() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("inspect_webhook_${transaction.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = ApexCyan,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = Strings.get("view_webhook_payload", language),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
