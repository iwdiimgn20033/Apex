package com.example.data.model

enum class AssetCategory(val displayNameEn: String, val displayNameAr: String) {
    ALL("All", "الكل"),
    STOCKS("Stocks", "الأسهم"),
    FOREX("Forex", "العملات الأجنبية"),
    CRYPTO("Crypto", "العملات الرقمية"),
    COMMODITIES("Commodities", "السلع"),
    INDICES("Indices", "المؤشرات")
}

data class Asset(
    val symbol: String,
    val name: String,
    val category: AssetCategory,
    val price: Double,
    val change24h: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: String,
    val bid: Double,
    val ask: Double,
    val spreadPips: Double,
    val mt5Symbol: String,
    val marketCapOrSupply: String,
    val historicalPoints: List<Double> = emptyList(),
    val isFavorite: Boolean = false
) {
    val isPositive: Boolean get() = changePercent24h >= 0
}

data class CandleStick(
    val timestamp: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
) {
    val isBullish: Boolean get() = close >= open
}
