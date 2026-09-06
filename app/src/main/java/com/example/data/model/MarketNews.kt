package com.example.data.model

enum class MarketSentiment {
    BULLISH, BEARISH, NEUTRAL
}

enum class NewsImpact {
    HIGH, MEDIUM, LOW
}

data class MarketNews(
    val id: String,
    val title: String,
    val titleAr: String,
    val summary: String,
    val summaryAr: String,
    val source: String,
    val timeAgo: String,
    val timeAgoAr: String,
    val sentiment: MarketSentiment,
    val impact: NewsImpact,
    val relatedSymbol: String,
    val url: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expectedImpactPercent: Double = 0.0,
    val impactProbability: Int = 85,
    val stockImpactAnalysis: String = "",
    val stockImpactAnalysisAr: String = ""
)

