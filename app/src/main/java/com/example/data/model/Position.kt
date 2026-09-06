package com.example.data.model

enum class OrderSide {
    BUY, SELL
}

enum class OrderType {
    MARKET, LIMIT, STOP
}

enum class PositionStatus {
    OPEN, CLOSED, PENDING
}

data class Position(
    val id: String,
    val symbol: String,
    val assetName: String,
    val side: OrderSide,
    val volumeLots: Double,
    val openPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double?,
    val takeProfit: Double?,
    val floatingPnL: Double,
    val marginRequired: Double,
    val leverage: Int,
    val openTimestamp: Long,
    val closeTimestamp: Long? = null,
    val closePrice: Double? = null,
    val realizedPnL: Double? = null,
    val status: PositionStatus = PositionStatus.OPEN,
    val mt5Ticket: Long = 100000L + (id.hashCode() % 900000).toLong().let { if (it < 0) -it else it }
) {
    val pnlPercent: Double get() = if (openPrice != 0.0) ((currentPrice - openPrice) / openPrice) * 100 * (if (side == OrderSide.BUY) 1 else -1) * leverage else 0.0
    val isProfitable: Boolean get() = floatingPnL >= 0
}
