package com.vr.smartreceiving.activity.admin

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.db.AppDatabase
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ScanModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

class ScanActivity : AppCompatActivity() {
    var nama=""
    var kode=""
    var group =""
    var groupResult=""
    var kodeResult=""
    var namaResult=""

    var status = true
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    private var mediaPlayer: MediaPlayer? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_scan)
        initView()
        initIntent()
        initCodeScanner()
    }
    private fun initView(){
        scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        // Inisialisasi MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.sound)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initCodeScanner(){
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                showSnack(this, "Scan result: ${it.text}")
                val jsonString = it.text
                try {
                    // Buat objek JSON dari string JSON
                    val jsonObject = JSONObject(jsonString)
                    // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                    groupResult = jsonObject.getString("itemNum")
                    namaResult = jsonObject.getString("itemDesc")
                    kodeResult = jsonObject.getString("sequenceQr")
                    if(group==""){
                        cekDuplicate(kodeResult)
                    }else{
                        if(group==groupResult){
                            cekDuplicate(kodeResult)
                        }else{
                            showSnack(this, "Item tidak sesuai")
                            codeScanner.startPreview()
                        }
                    }
                } catch (e: Exception) {
                    // Tangani kesalahan jika parsing JSON gagal
                    e.printStackTrace()
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    private fun initIntent(){
        group = intent.getStringExtra("group").toString()
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }
    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekDuplicate(itemId: String){
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                //cek duplicate appDatabase
                val db = AppDatabase.getInstance(this@ScanActivity)
                val scan = db.scanDao().getscanByKode(itemId)
                if(scan!=null){
                    withContext(Dispatchers.Main){
                        showSnack(this@ScanActivity, "Item sudah di scan")
                        progressDialog.dismiss()
                        codeScanner.startPreview()
                    }
                }else{
                    //insert data ke appDatabase
                    val scanModel = ScanModel(
                        UUID.randomUUID().toString(),
                        kodeResult,
                        groupResult,
                        namaResult,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    db.scanDao().insert(scanModel)
                    withContext(Dispatchers.Main){
                        showSnack(this@ScanActivity, "Item berhasil di scan")
                        progressDialog.dismiss()
                        //intent ke ScanActivity
                        val intent = Intent(this@ScanActivity, ScanActivity::class.java)
                        intent.putExtra("group", groupResult)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }

    }
}