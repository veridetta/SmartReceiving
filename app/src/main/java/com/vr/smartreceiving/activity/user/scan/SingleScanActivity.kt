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
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.admin.AdminActivity
import com.vr.smartreceiving.activity.user.BeforeScanActivity
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

class SingleScanActivity : AppCompatActivity() {
    var rackId=""
    var namaRack=""
    var itemNama=""
    var rackDocId=""
    var type=""
    var kode1=""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
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
                    kode1 = jsonObject.getString("itemNum")
                    val id = jsonObject.getString("id")
                    cekDuplicate(id)
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
        itemNama = intent.getStringExtra("itemNama").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()
        type = intent.getStringExtra("type").toString()
        namaRack = intent.getStringExtra("namaRack").toString()
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
    private fun readData(itemId:String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                val result = mFirestore.collection("barang").whereEqualTo("kode1",kode1)
                    .get().await()
                val reports = mutableListOf<BarangModel>()
                var ada = false
                var beda = false
                for (document in result) {
                    val report = document.toObject(BarangModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    Log.d("SCN", "Datanya : ${document.id} => ${document.data}")
                     ada = true
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
                    if(ada){
                        if(beda){
                            showSnack(this@SingleScanActivity, "Data tidak sesuai")
                        }else{
                            //simpan data ke report firebase
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
                            if(itemNama==""){
                                val barangData = hashMapOf(
                                    "perRak" to reports[0].perRak.toString(),
                                    "jenis" to reports[0].jenis.toString(),
                                    "merek" to reports[0].merek.toString(),
                                    "satuan" to reports[0].satuan.toString(),
                                    "itemNama" to reports[0].nama.toString(),
                                    "itemUid" to reports[0].uid.toString(),
                                )
                                val db = FirebaseFirestore.getInstance()
                                val reportCollection = db.collection("report")
                                reportCollection.document(rackDocId).update(barangData as Map<String, Any>)
                                    .addOnSuccessListener { documentReference ->
                                        showSnack(this@SingleScanActivity, "Berhasil memperbarui barang")
                                        val barangData2 = hashMapOf(
                                            "uid" to UUID.randomUUID().toString(),
                                            "nama" to namaRack,
                                            "perRak" to reports[0].perRak.toString(),
                                            "rakId" to rackId,
                                            "itemId" to itemId,
                                            "itemNama" to reports[0].nama.toString(),
                                            "nomorPenerimaan" to nomorPenerimaan,
                                            "itemUid" to reports[0].uid.toString(),
                                            "itemMerek" to reports[0].merek.toString(),
                                            "itemNum" to kode1,
                                            "itemJenis" to reports[0].jenis.toString(),
                                            "satuan" to reports[0].satuan.toString(),
                                            "petugasUid" to petugasUid,
                                            "petugasNama" to petugasNama,
                                            "createdAt" to createdAt,
                                            "scanAt" to createdAt
                                        )
                                        val db2 = FirebaseFirestore.getInstance()
                                        // Add the product data to Firestore
                                        db2.collection("reportDetail")
                                            .add(barangData2 as Map<String, Any>)
                                            .addOnSuccessListener { documentReferencex ->
                                                showSnack(this@SingleScanActivity,"Berhasil menyimpan barang")
                                                val intent = Intent(this@SingleScanActivity, BeforeScanActivity::class.java)
                                                intent.putExtra("rackId",rackId)
                                                intent.putExtra("namaRack",namaRack)
                                                intent.putExtra("aksi","reload")
                                                intent.putExtra("rackDocId",rackDocId)
                                                intent.putExtra("type",type)
                                                intent.putExtra("isRack", "true")
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                // Error occurred while adding product
                                                showSnack(this@SingleScanActivity,"Gagal menyimpan barang ${e.message}")
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        // Error occurred while adding product
                                        showSnack(this@SingleScanActivity,"Gagal menyimpan barang ${e.message}")
                                    }
                            }else{
                                val barangData2 = hashMapOf(
                                    "uid" to UUID.randomUUID().toString(),
                                    "nama" to nama,
                                    "perRak" to reports[0].perRak.toString(),
                                    "rackId" to rackId,
                                    "itemId" to itemId,
                                    "itemNama" to reports[0].nama.toString(),
                                    "nomorPenerimaan" to nomorPenerimaan,
                                    "itemUid" to reports[0].uid.toString(),
                                    "itemMerek" to reports[0].merek.toString(),
                                    "itemNum" to kode1,
                                    "itemJenis" to reports[0].jenis.toString(),
                                    "satuan" to reports[0].satuan.toString(),
                                    "petugasUid" to petugasUid,
                                    "petugasNama" to petugasNama,
                                    "createdAt" to createdAt,
                                    "scanAt" to createdAt
                                )
                                val db2 = FirebaseFirestore.getInstance()
                                // Add the product data to Firestore
                                db2.collection("reportDetail")
                                    .add(barangData2 as Map<String, Any>)
                                    .addOnSuccessListener { documentReferencex ->
                                        showSnack(this@SingleScanActivity,"Berhasil menyimpan barang")
                                        // Redirect to SellerActivity fragment home
                                        // Redirect to SellerActivity fragment home
                                        val intent = Intent(this@SingleScanActivity, BeforeScanActivity::class.java)
                                        intent.putExtra("rackId",rackId)
                                        intent.putExtra("namaRack",nama)
                                        intent.putExtra("rackDocId",rackDocId)
                                        intent.putExtra("type",type)
                                        intent.putExtra("isRack", "true")
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        // Error occurred while adding product
                                        showSnack(this@SingleScanActivity,"Gagal menyimpan barang ${e.message}")
                                    }
                            }
                            progressDialog.dismiss()
                        }
                    }else{
                        showSnack(this@SingleScanActivity, "Data tidak ditemukan")
                        codeScanner.startPreview()
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekDuplicate(itemId: String){
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                val result = mFirestore.collection("reportDetail").whereEqualTo("itemId", itemId)
                    .get().await()
                var duplicate = false
                for (document in result) {
                    Log.d("SCN", "Datanya : ${document.id} => ${document.data}")
                    duplicate = true
                }
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if(duplicate){
                        showSnack(this@SingleScanActivity, "Item sudah di scan")
                        codeScanner.startPreview()
                    }else{
                        readData(itemId)
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }

    }
    // Fungsi untuk menghasilkan string acak dengan angka
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