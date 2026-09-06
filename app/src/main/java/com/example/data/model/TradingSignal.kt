package com.example.data.model

enum class SignalAction(val labelEn: String, val labelAr: String, val isBuy: Boolean) {
    STRONG_BUY("Strong Buy", "شراء قوي", true),
    BUY("Buy", "شراء", true),
    NEUTRAL("Neutral", "محايد", true),
    SELL("Sell", "بيع", false),
    STRONG_SELL("Strong Sell", "بيع قوي", false)
}

data class TradingSignal(
    val id: String,
    val symbol: String,
    val assetName: String,
    val action: SignalAction,
    val timeframe: String,
    val entryPrice: Double,
    val targetPrice1: Double,
    val targetPrice2: Double,
    val stopLoss: Double,
    val confidencePercent: Int,
    val technicalIndicatorTrigger: String,
    val analysisText: String,
    val analysisTextAr: String,
    val timestamp: Long,
    val status: String = "ACTIVE"
)
