package com.vr.smartreceiving

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.vr.smartreceiving.activity.LoginActivity
import com.vr.smartreceiving.activity.admin.BarangActivity
import com.vr.smartreceiving.activity.admin.ScanActivity
import com.vr.smartreceiving.db.AppDatabase

class MainActivity : AppCompatActivity() {
    lateinit var btnScan:LinearLayout
    lateinit var btnLogout:ImageView
    lateinit var btnBarang:LinearLayout
    lateinit var btnRefresh:LinearLayout
    lateinit var txCount:TextView
    lateinit var txName:TextView

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
    }
    private fun initView(){
        btnScan = findViewById(R.id.btnScan)
        btnLogout = findViewById(R.id.btnLogout)
        btnBarang = findViewById(R.id.btnBarang)
        btnRefresh = findViewById(R.id.btnRefresh)
        txCount = findViewById(R.id.txCount)
        txName = findViewById(R.id.txName)
    }

    private fun initIntent(){
        sName = intent.getStringExtra("name").toString()
        sGroup = intent.getStringExtra("group").toString()
    }

    private fun initDB(){
        if(sGroup!==""){
            val db = AppDatabase.getInstance(this)
            sCount = db.scanDao().countScanByGroup(sGroup)
            txCount.text = sCount.toString()
            val firstQuery = db.scanDao().getScanByGroup(sGroup)
            txName.text = firstQuery.nama
        }
    }

    private fun initAction() {
        btnScan.setOnClickListener {
            //intent ke ScanActivity
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("group", sGroup)
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
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("group", "")
            startActivity(intent)
            finish()
        }
    }
}