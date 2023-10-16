package com.vr.smartreceiving.activity.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vr.smartreceiving.R

class ScanActivity : AppCompatActivity() {
    var type = ""
    var qr = ""
    var totalScan = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        qr = intent.getStringExtra("qr").toString()
        totalScan = intent.getStringExtra("totalScan")!!.toInt()
    }
    private fun setIntent(){
        if(type == "HGP" || type == "FDR") {
            if(totalScan == 1) {
                //scan barcode dulu
            }else{
                //scan qr
            }
        }else{
            //scan qr
        }
    }
    //scan rack berhasil maka insert ke fb report, nama, status, uid, docId, jumlah, createdAt
    //ketika berhasil scan, barcode maka akan ambil data sesuai qr1,
    //kemudian simpan variabel qr2, satuan, nama, jumlah, dan id barang
    //setelah itu intent kesini lagi dengan kirim data tersebut kemudian scan qr
    //pas scan dicek qr1 = qr2
    //kalau sama maka ubah data laporan(jumlah, satuan) dan insert laporan detail (insertjuga itemnum)
    //ketika 22nya sudah beres maka intent ke BeforeScanActivity dengan namaRack
}