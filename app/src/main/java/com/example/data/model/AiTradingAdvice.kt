package com.example.data.model

data class AiTradingAdvice(
    val id: String,
    val symbol: String,
    val assetName: String,
    val currentPrice: Double,
    val action: SignalAction,
    val entryTarget: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val stopLoss: Double,
    val riskRewardRatio: String,
    val confidencePercent: Int,
    val timeframe: String,
    val technicalRationale: String,
    val technicalRationaleAr: String,
    val fundamentalRationale: String,
    val fundamentalRationaleAr: String,
    val keyIndicatorsSummary: String,
    val keyIndicatorsSummaryAr: String,
    val generatedTimestamp: Long = System.currentTimeMillis()
)
