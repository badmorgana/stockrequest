package com.example.stockrequest.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.stockrequest.data.dao.StockRequestDao
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.utils.StockRequestStatusConverter

@Database(entities = [StockRequest::class], version = 2, exportSchema = false)
@TypeConverters(StockRequestStatusConverter::class)
abstract class StockRequestDatabase : RoomDatabase() {
    abstract fun stockRequestDao(): StockRequestDao

    companion object {
        @Volatile
        private var INSTANCE: StockRequestDatabase? = null

        fun getDatabase(context: Context): StockRequestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockRequestDatabase::class.java,
                    "stock_request_database"
                )
                    .fallbackToDestructiveMigration() // This will reset the database on version change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}