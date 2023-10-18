package com.vr.smartreceiving.activity.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.helper.showSnack
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

class RakScanActivity : AppCompatActivity() {
    var rackId=""
    var type=""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rak_scan)
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
                //val input = it.text
                //val startIndex = input.indexOf("rackId:") + "rackId:".length
                //val endIndex = input.indexOf(",", startIndex)
                //val rackIdValue = input.substring(startIndex, endIndex)
                //val rackId = rackIdValue.toInt()
                Log.d("Rak Code","Rak "+it.text)
                val jj = it.text
                val jsonString = "{$jj"
                try {
                    // Buat objek JSON dari string JSON
                    val jsonObject = JSONObject(jsonString)
                    // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                    val rid = jsonObject.getString("rackId")
                    cekData(rid)
                    showSnack(this, "Scan result: ${rid}")
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
        rackId = intent.getStringExtra("rackId").toString()
        type = intent.getStringExtra("type").toString()
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
    private fun cekData(rid:String){
        var ada = false
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                val result = mFirestore.collection("report")
                    .whereEqualTo("rakId", rid)
                    .get().await()
                //init model
                val reports = mutableListOf<ReportModel>()
                for (document in result) {
                    val report = document.toObject(ReportModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    Log.d("REPORT RACK", "Datanya : ${document.id} => ${document.data}")
                    ada = true
                }
                withContext(Dispatchers.Main) {
                    if (ada) {
                        Log.d("RAK", "data ada")
                        val intent = Intent(
                            this@RakScanActivity,
                            BeforeScanActivity::class.java
                        )
                        intent.putExtra("rackId", reports[0].rakId.toString())
                        intent.putExtra("rackDocId", reports[0].docId.toString())
                        intent.putExtra("namaRack", reports[0].nama.toString())
                        intent.putExtra("isRack", "true")
                        intent.putExtra("itemNama", reports[0].itemNama.toString())
                        intent.putExtra("type", type)
                        startActivity(intent)
                        finish()
                    } else {
                        val currentDateTime = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                        val formatted = currentDateTime.format(formatter)
                        val createdAt = formatted
                        val nama = "ra"+generateRandomString(7)
                        //ambil uid dan nama dari sharedpreferences
                        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        val petugasUid = sharedPreferences.getString("userUid", "")
                        val petugasNama = sharedPreferences.getString("userName", "")
                        val nomorPenerimaan = "np"+generateRandomString(8)
                        val barangData = hashMapOf(
                            "uid" to UUID.randomUUID().toString(),
                            "nama" to nama,
                            "perRak" to "",
                            "jenis" to "",
                            "merek" to "",
                            "rakId" to rid,
                            "satuan" to "",
                            "status" to "proses",
                            "itemNama" to "",
                            "itemUid" to "",
                            "petugasUid" to petugasUid,
                            "petugasNama" to petugasNama,
                            "nomorPenerimaan" to nomorPenerimaan,
                            "jumlah" to "1",
                            "createdAt" to createdAt,
                        )
                        val db = FirebaseFirestore.getInstance()
                        // Add the product data to Firestore
                        db.collection("report")
                            .add(barangData as Map<String, Any>)
                            .addOnSuccessListener { documentReference ->
                                showSnack(
                                    this@RakScanActivity,
                                    "Berhasil menyimpan rak"
                                )
                                Log.d("RAK", "tidak data ada")
                                val intent = Intent(
                                    this@RakScanActivity,
                                    BeforeScanActivity::class.java
                                )
                                intent.putExtra("rackId", rid)
                                intent.putExtra("namaRack", nama)
                                intent.putExtra("isRack", "true")
                                intent.putExtra("itemNama", "")
                                intent.putExtra("type", type)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                // Error occurred while adding product
                                showSnack(
                                    this@RakScanActivity,
                                    "Gagal menyimpan rak ${e.message}"
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
            }
        }
    }
    fun generateRandomString(jumlah: Int): String {
        val karakter = CharArray(jumlah) { ' ' }

        // Daftar karakter yang diizinkan untuk string acak (hanya angka)
        val karakterDiizinkan = "0123456789"

        for (i in karakter.indices) {
            karakter[i] = karakterDiizinkan[Random.nextInt(0, karakterDiizinkan.length)]
        }

        return String(karakter)
    }
}