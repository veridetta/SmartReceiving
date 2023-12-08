package com.vr.smartreceiving.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan")
class ScanModel(
    //uid primarykey autoincrement
    @PrimaryKey(autoGenerate = true) var uid:Int? = null,
    var kode: String,
    var nama: String,
    var kelompok: String,
    var scanAt: String,
)



