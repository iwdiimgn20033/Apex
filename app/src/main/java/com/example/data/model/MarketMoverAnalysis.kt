package com.example.data.model

enum class CatalystCategory(val labelEn: String, val labelAr: String) {
    EARNINGS_SURPRISE("Earnings Beat", "نتائج مالية وأرباح قياسية"),
    CENTRAL_BANK("Central Bank & Fed", "قرارات الفيدرالي والبنوك المركزية"),
    AI_TECH_BREAKTHROUGH("AI & Tech Demand", "طفرة الذكاء الاصطناعي والتكنولوجيا"),
    GEOPOLITICS_SUPPLY("Supply Shock & OPEC", "صدمات الإمداد وقرارات أوبك+"),
    CRYPTO_INSTITUTIONAL("Institutional ETF Inflow", "تدفقات مؤسسية وصناديق ETF")
}

data class MarketMoverAnalysis(
    val id: String,
    val symbol: String,
    val name: String,
    val changePercent: Double,
    val isRise: Boolean,
    val category: CatalystCategory,
    val headline: String,
    val headlineAr: String,
    val deepAnalysis: String,
    val deepAnalysisAr: String,
    val keyPriceLevels: String,
    val keyPriceLevelsAr: String,
    val marketReactionTime: String,
    val marketReactionTimeAr: String
)
