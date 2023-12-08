package com.vr.smartreceiving.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
class BarangModel(
    @PrimaryKey(autoGenerate = true) var uid:Int? = null,
    @ColumnInfo(name = "kode") var kode: String,
    @ColumnInfo(name = "nama") var nama:String,
    @ColumnInfo(name = "kelompok") var kelompok:String
)



