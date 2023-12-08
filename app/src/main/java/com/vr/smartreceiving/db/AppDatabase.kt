package com.vr.smartreceiving.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vr.smartreceiving.dao.BarangDao
import com.vr.smartreceiving.model.BarangModel

import android.content.Context
import androidx.room.Room
import com.vr.smartreceiving.dao.ScanDao
import com.vr.smartreceiving.model.ScanModel

@Database(entities = [BarangModel::class, ScanModel::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun barangDao(): BarangDao
    abstract fun scanDao(): ScanDao

    companion object{
        private var instance: AppDatabase? = null

        fun getInstance(context: Context):AppDatabase{
            if (instance==null){
                instance = Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }
}
