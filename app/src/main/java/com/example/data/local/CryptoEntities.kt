package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey val symbol: String, // e.g. "BTCUSDT"
    val displayName: String,       // e.g. "Bitcoin"
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val targetPrice: Double,
    val isAbove: Boolean, // true if alert when price rises above target
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_signals")
data class SavedSignalEntity(
    @PrimaryKey val id: String,
    val pair: String,
    val action: String,
    val currentPrice: Double,
    val entryMin: Double,
    val entryMax: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskReward: String,
    val timeframe: String,
    val timestamp: Long
)

@Dao
interface CryptoDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistItem)

    @Delete
    suspend fun removeFromWatchlist(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeBySymbol(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun isInWatchlist(symbol: String): Boolean

    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getPriceAlerts(): Flow<List<PriceAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert)

    @Delete
    suspend fun deleteAlert(alert: PriceAlert)

    @Query("SELECT * FROM saved_signals ORDER BY timestamp DESC")
    fun getSavedSignals(): Flow<List<SavedSignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSignal(signal: SavedSignalEntity)
}

@Database(
    entities = [WatchlistItem::class, PriceAlert::class, SavedSignalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CryptoDatabase : RoomDatabase() {
    abstract fun cryptoDao(): CryptoDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getDatabase(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    "crypto_ai_friend.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
