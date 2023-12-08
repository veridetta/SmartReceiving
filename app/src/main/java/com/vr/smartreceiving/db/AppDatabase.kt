package com.vr.smartreceiving.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vr.smartreceiving.dao.BarangDao
import com.vr.smartreceiving.model.BarangModel

import android.content.Context
import androidx.room.Room
import com.vr.smartreceiving.dao.ScanDao

@Database(entities = [BarangModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun barangDao(): BarangDao
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
