package com.example.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PortfolioEntity
import com.example.data.local.entities.PositionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_cache WHERE id = 1 LIMIT 1")
    fun getPortfolio(): Flow<PortfolioEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePortfolio(portfolio: PortfolioEntity)
}

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions WHERE status = 'OPEN' ORDER BY openTimestamp DESC")
    fun getOpenPositions(): Flow<List<PositionEntity>>

    @Query("SELECT * FROM positions WHERE status = 'CLOSED' ORDER BY closeTimestamp DESC")
    fun getClosedPositions(): Flow<List<PositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PositionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(positions: List<PositionEntity>)

    @Update
    suspend fun updatePosition(position: PositionEntity)

    @Query("DELETE FROM positions WHERE id = :id")
    suspend fun deletePosition(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' OR status = 'PROCESSING' ORDER BY timestamp ASC")
    suspend fun getPendingOrProcessingTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status, notes = :notes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, notes: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT symbol FROM watchlist")
    fun getFavoriteSymbols(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun toggleFavorite(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeFavorite(symbol: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
