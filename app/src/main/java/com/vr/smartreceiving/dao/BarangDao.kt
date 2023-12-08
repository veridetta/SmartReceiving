package com.vr.smartreceiving.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vr.smartreceiving.model.BarangModel

@Dao
interface BarangDao {
    @Query("SELECT * From barang")
    fun getAllbarang():List<BarangModel>
    //get barang bykode
    @Query("SELECT * FROM barang WHERE kode = :kode")
    fun getbarangByKode(kode: String): BarangModel
    @Insert
    fun insert(barang:BarangModel)

    @Update
    fun update(barang: BarangModel)

    @Query("DELETE FROM barang WHERE uid = :uid")
    fun delete(uid: Int)


}