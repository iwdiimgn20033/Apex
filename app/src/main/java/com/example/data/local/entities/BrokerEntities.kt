package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_cache")
data class PortfolioEntity(
    @PrimaryKey val id: Int = 1,
    val totalBalance: Double,
    val equity: Double,
    val usedMargin: Double,
    val freeMargin: Double,
    val marginLevelPercent: Double,
    val todayPnl: Double,
    val todayPnlPercent: Double,
    val allTimeReturnPercent: Double,
    val lastUpdatedTimestamp: Long
)

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val assetName: String,
    val side: String,
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
    val status: String,
    val mt5Ticket: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val method: String,
    val status: String,
    val timestamp: Long,
    val referenceHash: String,
    val fee: Double,
    val accountDestination: String,
    val notes: String
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val isFavorite: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleAr: String,
    val message: String,
    val messageAr: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean
)
