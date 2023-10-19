package com.vr.smartreceiving.activity.user.scan

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.qrcode.encoder.QRCode
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.admin.AdminActivity
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ReportDetailModel
import com.vr.smartreceiving.model.ReportModel
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

class FirstScanActivity : AppCompatActivity() {
    var rackId=""
    var itemNama=""
    var namaRack=""
    var type=""
    var rackDocId=""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_scan)
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
                readData(it.text)
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
        rackId = intent.getStringExtra("rackId").toString()
        itemNama = intent.getStringExtra("itemNama").toString()
        type = intent.getStringExtra("type").toString()
        namaRack = intent.getStringExtra("namaRack").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()
        Log.d("RAK", "FIRSTSCAN docId : ${rackDocId}")
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun readData(qrCode: String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                val result = mFirestore.collection("barang").whereEqualTo("kode1",qrCode).get().await()
                val reports = mutableListOf<BarangModel>()
                var ada = false
                var beda = false
                for (document in result) {
                    val report = document.toObject(BarangModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    ada=true
                    //cek jika itemnama kosong
                    Log.d("FIrst", "itemNama "+itemNama)
                    Log.d("FIrst", "rerportNama "+report.nama)
                    if (itemNama =="" ){
                        beda=false
                    }else{
                        //itemNaa tidak sama  degan report.nama
                        if (itemNama==report.nama){
                            beda=false
                        }else{
                            beda=true
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if (!ada) {
                        showSnack(this@FirstScanActivity, "Data tidak ditemukan")
                        codeScanner.startPreview()
                    }else{
                        if(beda){
                            showSnack(this@FirstScanActivity, "Data tidak sesuai")
                        }else{
                            val intent = Intent(this@FirstScanActivity, SecondScanActivity::class.java)
                            intent.putExtra("rackId",rackId)
                            intent.putExtra("itemNama",itemNama)
                            intent.putExtra("namaRack",namaRack)
                            intent.putExtra("rackDocId",rackDocId)
                            intent.putExtra("kode1",reports[0].kode1)
                            intent.putExtra("kode2",reports[0].kode2)
                            intent.putExtra("type",type)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
}