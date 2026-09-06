package com.example.data.model

enum class NotificationType {
    TRADE, MARGIN, PAYMENT, SIGNAL, SECURITY
}

data class NotificationItem(
    val id: String,
    val title: String,
    val titleAr: String,
    val message: String,
    val messageAr: String,
    val type: NotificationType,
    val timestamp: Long,
    val isRead: Boolean = false
)

enum class IndicatorType(val label: String) {
    NONE("None"),
    SMA("SMA (20/50)"),
    BOLLINGER("Bollinger Bands"),
    RSI("RSI (14)"),
    MACD("MACD (12,26,9)")
}
