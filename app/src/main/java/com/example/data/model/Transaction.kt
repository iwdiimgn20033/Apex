package com.example.data.model

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, TRADE_PROFIT, TRADE_LOSS, DIVIDEND
}

enum class PaymentMethod(val displayName: String, val iconResName: String) {
    PAYPAL("PayPal Instant", "paypal"),
    BANK_TRANSFER("Direct Bank Wire (SWIFT/ACH)", "account_balance"),
    CREDIT_DEBIT_CARD("Visa / Mastercard Instant", "credit_card"),
    CRYPTO_USDT("Crypto (USDT TRC20)", "currency_bitcoin")
}

enum class TransactionStatus {
    COMPLETED, PENDING, PROCESSING, COMPLIANCE_REVIEW, REJECTED
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val currency: String = "USD",
    val method: PaymentMethod,
    val status: TransactionStatus,
    val timestamp: Long,
    val referenceHash: String,
    val fee: Double = 0.0,
    val accountDestination: String,
    val complianceChecked: Boolean = true,
    val notes: String = ""
) {
    val isIncoming: Boolean get() = type == TransactionType.DEPOSIT || type == TransactionType.TRADE_PROFIT || type == TransactionType.DIVIDEND
}
