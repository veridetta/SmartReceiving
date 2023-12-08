package com.vr.smartreceiving

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vr.smartreceiving.activity.LoginActivity
import com.vr.smartreceiving.activity.admin.BarangActivity
import com.vr.smartreceiving.activity.admin.ScanActivity
import com.vr.smartreceiving.db.AppDatabase
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var btnScan:LinearLayout
    lateinit var btnLogout:ImageView
    lateinit var btnBarang:LinearLayout
    lateinit var btnRefresh:LinearLayout
    lateinit var txCount:TextView
    lateinit var txName:TextView
    private lateinit var database: AppDatabase
    private val CAMERA_PERMISSION_CODE = 101

    var sCount = 0
    var sName = ""
    var sGroup =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initIntent()
        initDB()
        initAction()
        requestCameraPermission()
    }
    private fun initView(){
        btnScan = findViewById(R.id.btnScan)
        btnLogout = findViewById(R.id.btnLogout)
        btnBarang = findViewById(R.id.btnBarang)
        btnRefresh = findViewById(R.id.btnRefresh)
        txCount = findViewById(R.id.txCount)
        txName = findViewById(R.id.txName)

        database = AppDatabase.getInstance(applicationContext)
    }

    private fun initIntent(){
        sName = intent.getStringExtra("name").toString()
        //gruop ambil dari shared preferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sGroup = sharedPreferences.getString("lastGroup", "kosong").toString()
        Log.d("group", sGroup)
    }

    private fun initDB(){
        if(sGroup!=="kosong"){
            val db = AppDatabase.getInstance(this)
            GlobalScope.launch {
                sCount = db.scanDao().countScanByGroup(sGroup)
                val firstQuery = db.scanDao().getScanByGroup(sGroup)
                val getAll = db.barangDao().getAllbarang()
                Log.d("getAlll", getAll[0].nama.toString())
                withContext(Dispatchers.Main){
                    Log.d("count", sCount.toString())
                    txCount.text = sCount.toString()
                    if (firstQuery!=null){
                        txName.text = firstQuery.nama
                    }
                }
            }
        }
    }

    private fun initAction() {
        btnScan.setOnClickListener {
            //intent ke ScanActivity
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("group", "kosong")
            startActivity(intent)
        }
        btnLogout.setOnClickListener {
            // Hapus shared preferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Arahkan ke MainActivity dengan membersihkan stack aktivitas
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        btnBarang.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, BarangActivity::class.java)
            intent.putExtra("group", sGroup)
            startActivity(intent)
        }
        btnRefresh.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("group", "kosong")
            startActivity(intent)
            finish()
        }
    }
    //onresume
    override fun onResume() {
        super.onResume()
        initDB()
    }
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            showSnack(this,"Izin kamera sudah diberikan")
        }
    }

    // Override dari callback hasil permintaan izin
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan oleh pengguna
                // Lakukan tindakan yang diperlukan saat izin kamera sudah ada di sini
            } else {
                // Izin kamera ditolak oleh pengguna
                // Handle kasus saat pengguna menolak akses kamera di sini
            }
        }
    }
}