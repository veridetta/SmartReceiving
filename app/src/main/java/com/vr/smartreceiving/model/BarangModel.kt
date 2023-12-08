package com.vr.smartreceiving.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
class BarangModel(
    //uid primarykey autoincrement
    @PrimaryKey
    var uid: String,
    var kode: String,
    var nama: String,
    var group: String,
)



