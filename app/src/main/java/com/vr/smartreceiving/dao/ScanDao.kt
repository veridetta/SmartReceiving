package com.vr.smartreceiving.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ScanModel

@Dao
interface ScanDao {
    @Query("SELECT * From scan")
    fun getAllscan():List<ScanModel>
    @Query("SELECT * FROM scan WHERE kode = :kode")
    fun getscanByKode(kode: String): ScanModel

    @Query("SELECT COUNT(*) FROM scan WHERE `group` = :group")
    fun countScanByGroup(group: String): Int

    //get by group first item
    @Query("SELECT * FROM scan WHERE `group` = :group LIMIT 1")
    fun getScanByGroup(group: String): ScanModel

    @Insert
    fun insert(scan:ScanModel)

}