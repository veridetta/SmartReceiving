package com.vr.smartreceiving.activity.user

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.LoginActivity
import com.vr.smartreceiving.helper.showSnack

class UserActivity : AppCompatActivity() {
    private lateinit var btnFederal :CardView
    private lateinit var btnFdr :CardView
    private lateinit var btnOem :CardView
    private lateinit var btnHgp :CardView
    private lateinit var tvLaporan :TextView
    private lateinit var tvLogout :TextView
    private lateinit var lyFederal :RelativeLayout
    private val CAMERA_PERMISSION_REQUEST_CODE = 101 // Atur dengan kode permintaan izin yang Anda inginkan
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        initView()
        initListener()

    }
    private fun initView(){
        btnFederal = findViewById(R.id.btnFederal)
        btnFdr = findViewById(R.id.btnFdr)
        btnOem = findViewById(R.id.btnOem)
        btnHgp = findViewById(R.id.btnHgp)
        tvLaporan = findViewById(R.id.tvLaporan)
        tvLogout = findViewById(R.id.tvLogout)
        lyFederal = findViewById(R.id.lyFederal)
        lyFederal.visibility = View.GONE
    }
    private fun initListener(){
        btnFederal.setOnClickListener {
            lyFederal.visibility = View.VISIBLE
        }
        btnFdr.setOnClickListener {
            initIntent("FDR")
        }
        btnOem.setOnClickListener {
            initIntent("OEM")
        }
        btnHgp.setOnClickListener {
            initIntent("HGP")
        }
        tvLaporan.setOnClickListener {
            val intent = Intent(this, ReportActivityUser::class.java)
            startActivity(intent)
        }
        tvLogout.setOnClickListener {
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
    }
    private fun initIntent(va:String){
        lateinit var intent: Intent
        if(va =="OEM"){
            if (checkCameraPermission()) {
                intent = Intent(this, BeforeScanActivity::class.java)
                intent.putExtra("type", "OEM")
                intent.putExtra("qr","")
                intent.putExtra("isRack","")
                intent.putExtra("jumlahItem","")
                intent.putExtra("scanItem","")
                intent.putExtra("namaRack","")
                intent.putExtra("rakCode","")
                intent.putExtra("rackId","")
                startActivity(intent)
            } else {
                requestCameraPermission()
            }
        }else if(va =="FDR") {
            if (checkCameraPermission()) {
                intent = Intent(this, BeforeScanActivity::class.java)
                intent.putExtra("type", "FDR")
                intent.putExtra("qr","")
                intent.putExtra("isRack","")
                intent.putExtra("jumlahItem","")
                intent.putExtra("scanItem","")
                intent.putExtra("namaRack","")
                intent.putExtra("rakCode","")
                intent.putExtra("rackId","")
                startActivity(intent)
            } else {
                requestCameraPermission()
            }

        }else if(va =="HGP") {
            if (checkCameraPermission()) {
                intent = Intent(this, BeforeScanActivity::class.java)
                intent.putExtra("type", "HGP")
                intent.putExtra("qr","")
                intent.putExtra("isRack","")
                intent.putExtra("jumlahItem","")
                intent.putExtra("scanItem","")
                intent.putExtra("namaRack","")
                intent.putExtra("rakCode","")
                intent.putExtra("rackId","")
                startActivity(intent)
            } else {
                requestCameraPermission()
            }
        }

    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan, memulai intent kamera

            } else {
                // Izin kamera tidak diberikan, Anda dapat memberikan pesan atau tindakan lainnya di sini
                showSnack(this,"Izin kamera tidak diberikan")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}