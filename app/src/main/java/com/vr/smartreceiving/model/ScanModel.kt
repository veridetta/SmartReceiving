package com.vr.smartreceiving.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan")
class ScanModel(
    //uid primarykey autoincrement
    @PrimaryKey
    var uid: String,
    var kode: String,
    var nama: String,
    var group: String,
    var scanAt: String,
)



