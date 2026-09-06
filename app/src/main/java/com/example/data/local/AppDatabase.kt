package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.daos.NotificationDao
import com.example.data.local.daos.PortfolioDao
import com.example.data.local.daos.PositionDao
import com.example.data.local.daos.TransactionDao
import com.example.data.local.daos.WatchlistDao
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PortfolioEntity
import com.example.data.local.entities.PositionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.WatchlistEntity

@Database(
    entities = [
        PortfolioEntity::class,
        PositionEntity::class,
        TransactionEntity::class,
        WatchlistEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun positionDao(): PositionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_broker.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
