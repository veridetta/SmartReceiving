package com.vr.smartreceiving.activity.user

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

class ScanActivity : AppCompatActivity() {
    var type = ""
    var kode1 = ""
    var totalScan = 1
    var itemSekarang=""
    var barangSekarag=""
    var kode2=""
    var scanItem=0
    var jumlahItem=0
    var rackId=""
    var rakCode=""
    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var progressDialog : ProgressDialog
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        initView()
        initIntent()
        setIntent()
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
                Log.d("Rak Code","Rak "+rakCode)
                if(rakCode==""){
                    val jsonString = it.text
                    try {
                        // Buat objek JSON dari string JSON
                        val jsonObject = JSONObject(jsonString)
                        // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                        val rid = jsonObject.getString("rackId")
                        val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                        intent.putExtra("rackId",rid)
                        intent.putExtra("namaRack","Scan item dahulu")
                        Log.d("nama", "nama Rack "+rid)
                        intent.putExtra("isRack",true)
                        intent.putExtra("scanItem","0")
                        intent.putExtra("jumlahItem","0")
                        intent.putExtra("rakCode",rid)
                        intent.putExtra("type",type)
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        // Tangani kesalahan jika parsing JSON gagal
                        e.printStackTrace()
                    }

                }else{
                    if(scanItem<1){
                        jumlahItem=1
                    }
                    if(scanItem<jumlahItem){
                        if(totalScan == 1) {
                            if(type == "HGP" || type == "FDR") {
                                cekDuplicate("",false,it.text)
                            }else{
                                //hasil dimasukkan ke jsonstring
                                val jsonString = it.text
                                try {
                                    // Buat objek JSON dari string JSON
                                    val jsonObject = JSONObject(jsonString)
                                    // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                                    val itemNum = jsonObject.getString("itemNum")
                                    val id = jsonObject.getString("id")
                                    //cek itemsama
                                    if(scanItem>0){
                                        if(itemSekarang.equals(itemNum)){
                                            //cek item duplicate
                                            cekDuplicate(id,false,itemNum)
                                            // Gunakan nilai "itemNum" dan "id" sesuai kebutuhan
                                            println("itemNum: $itemNum")
                                            println("id: $id")
                                        }else{
                                            showSnack(this, "Item berbeda")
                                            codeScanner.startPreview()
                                        }
                                    }

                                } catch (e: Exception) {
                                    // Tangani kesalahan jika parsing JSON gagal
                                    e.printStackTrace()
                                }
                            }
                        }else{
                            //hasil dimasukkan ke jsonstring
                            val jsonString = it.text
                            try {
                                // Buat objek JSON dari string JSON
                                val jsonObject = JSONObject(jsonString)
                                // Dapatkan nilai "itemNum" dan "id" dari objek JSON
                                val itemNum = jsonObject.getString("itemNum")
                                val id = jsonObject.getString("id")
                                //cek itemsama
                                if(itemSekarang == itemNum){
                                    //cek item duplicate
                                    cekDuplicate(id,true,itemNum)
                                    // Gunakan nilai "itemNum" dan "id" sesuai kebutuhan
                                    println("itemNum: $itemNum")
                                    println("id: $id")
                                }else{
                                    showSnack(this, "Item berbeda")
                                    codeScanner.startPreview()
                                }

                            } catch (e: Exception) {
                                // Tangani kesalahan jika parsing JSON gagal
                                e.printStackTrace()
                            }
                        }
                    }else{
                        showSnack(this,"Tidak bisa melakukan Scan pada Rack Ini")
                        codeScanner.startPreview()
                    }

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
        type = intent.getStringExtra("type").toString()
        kode1 = intent.getStringExtra("qr").toString()
        rackId = intent.getStringExtra("rackId").toString()
        rakCode = intent.getStringExtra("rakCode").toString()
        totalScan = intent.getStringExtra("totalScan")?.toIntOrNull() ?: 1
    }
    private fun setIntent(){
        if(type == "HGP" || type == "FDR") {
            if(totalScan == 1) {
                //scan barcode dulu
            }else{
                //scan qr
            }
        }else{
            if(type=="Rack"){
                //scan rack
            }else{
                //scan qr
            }
        }
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
    private fun readData(qr : String, qr2:Boolean, itemId:String) {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                if(qr2){
                    val result = mFirestore.collection("barang").whereEqualTo("kode2",qr)
                        .get().await()
                    val reports = mutableListOf<BarangModel>()
                    for (document in result) {
                        val report = document.toObject(BarangModel::class.java)
                        val docId = document.id
                        report.docId = docId
                        reports.add(report)
                        Log.d("SCN", "Datanya : ${document.id} => ${document.data}")
                    }
                    withContext(Dispatchers.Main) {
                        barangSekarag = reports[0].nama.toString()
                        progressDialog.dismiss()
                    }
                }else{
                    val result = mFirestore.collection("barang").whereEqualTo("kode1",qr)
                        .get().await()
                    val reports = mutableListOf<BarangModel>()
                    for (document in result) {
                        val report = document.toObject(BarangModel::class.java)
                        val docId = document.id
                        report.docId = docId
                        reports.add(report)
                        Log.d("SCN", "Datanya : ${document.id} => ${document.data}")
                    }
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        if(type == "HGP" || type == "FDR") {
                            if(totalScan == 1) {
                                barangSekarag = reports[0].nama.toString()
                                itemSekarang = reports[0].kode2.toString()
                                kode2 = reports[0].kode2.toString()
                                totalScan++
                                codeScanner.startPreview()
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
                                if(scanItem==0){
                                    val barangData = hashMapOf(
                                        "uid" to UUID.randomUUID().toString(),
                                        "nama" to nama,
                                        "perRak" to reports[0].perRak.toString(),
                                        "jenis" to reports[0].jenis.toString(),
                                        "merek" to reports[0].merek.toString(),
                                        "satuan" to reports[0].satuan.toString(),
                                        "status" to "proses",
                                        "itemNama" to reports[0].nama.toString(),
                                        "itemUid" to reports[0].uid.toString(),
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
                                            showSnack(this@ScanActivity,"Berhasil menyimpan barang")
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
                                                "itemNum" to qr,
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
                                                    showSnack(this@ScanActivity,"Berhasil menyimpan barang")
                                                    // Redirect to SellerActivity fragment home
                                                    // Redirect to SellerActivity fragment home
                                                    val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                                                    intent.putExtra("rackId",rackId)
                                                    intent.putExtra("namaRack",nama)
                                                    intent.putExtra("isRack",true)
                                                    intent.putExtra("scanItem",scanItem++)
                                                    intent.putExtra("jumlahItem",reports[0].perRak.toString())
                                                    intent.putExtra("type",type)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                                .addOnFailureListener { e ->
                                                    // Error occurred while adding product
                                                    showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                                }

                                        }
                                        .addOnFailureListener { e ->
                                            // Error occurred while adding product
                                            showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
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
                                        "itemNum" to qr,
                                        "itemJenis" to reports[0].jenis.toString(),
                                        "satuan" to reports[0].satuan.toString(),
                                        "petugasUid" to petugasUid,
                                        "petugasNama" to petugasNama,
                                        "createdAt" to createdAt,
                                        "scanAt" to createdAt
                                    )
                                    mFirestore.collection("reportDetail")
                                        .add(barangData2 as Map<String, Any>)
                                        .addOnSuccessListener { documentReferencex ->
                                            showSnack(this@ScanActivity,"Berhasil menyimpan barang")
                                            // Redirect to SellerActivity fragment home
                                            // Redirect to SellerActivity fragment home
                                            val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                                            intent.putExtra("rackId",rackId)
                                            intent.putExtra("namaRack",nama)
                                            intent.putExtra("isRack",true)
                                            intent.putExtra("scanItem",scanItem++)
                                            intent.putExtra("jumlahItem",reports[0].perRak.toString())
                                            intent.putExtra("type",type)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Error occurred while adding product
                                            showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                        }
                                }
                            }
                        }else{
                            if(qr=="Rack"){
                                //scan rack
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
                                if(scanItem==0){
                                    var ada = false
                                    GlobalScope.launch(Dispatchers.IO) {
                                        try {
                                            val mFirestore = FirebaseFirestore.getInstance()
                                            val result = mFirestore.collection("report")
                                                .whereEqualTo("rackId", rackId)
                                                .get().await()
                                            for (document in result) {
                                                ada = true
                                            }
                                            withContext(Dispatchers.Main) {
                                                if(ada){
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
                                                        "itemNum" to qr,
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
                                                            showSnack(this@ScanActivity,"Berhasil menyimpan barang")
                                                            // Redirect to SellerActivity fragment home
                                                            // Redirect to SellerActivity fragment home
                                                            val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                                                            intent.putExtra("rackId",rackId)
                                                            intent.putExtra("namaRack",nama)
                                                            intent.putExtra("isRack",true)
                                                            intent.putExtra("scanItem",scanItem++)
                                                            intent.putExtra("jumlahItem",reports[0].perRak.toString())
                                                            intent.putExtra("type",type)
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Error occurred while adding product
                                                            showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                                        }
                                                }else{
                                                    val barangData = hashMapOf(
                                                        "uid" to UUID.randomUUID().toString(),
                                                        "nama" to nama,
                                                        "perRak" to reports[0].perRak.toString(),
                                                        "jenis" to reports[0].jenis.toString(),
                                                        "merek" to reports[0].merek.toString(),
                                                        "satuan" to reports[0].satuan.toString(),
                                                        "status" to "proses",
                                                        "itemNama" to reports[0].nama.toString(),
                                                        "itemUid" to reports[0].uid.toString(),
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
                                                            showSnack(this@ScanActivity,"Berhasil menyimpan barang")
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
                                                                "itemNum" to qr,
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
                                                                    showSnack(this@ScanActivity,"Berhasil menyimpan barang")
                                                                    // Redirect to SellerActivity fragment home
                                                                    // Redirect to SellerActivity fragment home
                                                                    val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                                                                    intent.putExtra("rackId",rackId)
                                                                    intent.putExtra("namaRack",nama)
                                                                    intent.putExtra("isRack",true)
                                                                    intent.putExtra("scanItem",scanItem++)
                                                                    intent.putExtra("jumlahItem",reports[0].perRak.toString())
                                                                    intent.putExtra("type",type)
                                                                    startActivity(intent)
                                                                    finish()
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    // Error occurred while adding product
                                                                    showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                                                }

                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Error occurred while adding product
                                                            showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                                        }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.w("SCAN", "Error getting documents : $e")
                                        }
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
                                        "itemNum" to qr,
                                        "itemJenis" to reports[0].jenis.toString(),
                                        "satuan" to reports[0].satuan.toString(),
                                        "petugasUid" to petugasUid,
                                        "petugasNama" to petugasNama,
                                        "createdAt" to createdAt,
                                        "scanAt" to createdAt
                                    )
                                    mFirestore.collection("reportDetail")
                                        .add(barangData2 as Map<String, Any>)
                                        .addOnSuccessListener { documentReferencex ->
                                            showSnack(this@ScanActivity,"Berhasil menyimpan barang")
                                            // Redirect to SellerActivity fragment home
                                            // Redirect to SellerActivity fragment home
                                            val intent = Intent(this@ScanActivity, BeforeScanActivity::class.java)
                                            intent.putExtra("rackId",rackId)
                                            intent.putExtra("namaRack",nama)
                                            intent.putExtra("isRack",true)
                                            intent.putExtra("scanItem",scanItem++)
                                            intent.putExtra("jumlahItem",reports[0].perRak.toString())
                                            intent.putExtra("type",type)
                                            startActivity(intent)
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Error occurred while adding product
                                            showSnack(this@ScanActivity,"Gagal menyimpan barang ${e.message}")
                                        }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("SCAN", "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekDuplicate(itemId: String, qr2: Boolean, qr: String){
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                val result = mFirestore.collection("reportDetail").whereEqualTo("itemId", itemId)
                    .get().await()
                val reports = mutableListOf<ReportDetailModel>()
                var duplicate = false
                for (document in result) {
                    Log.d("SCN", "Datanya : ${document.id} => ${document.data}")
                    duplicate = true
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    if(duplicate){
                        showSnack(this@ScanActivity, "Item sudah di scan")
                        codeScanner.startPreview()
                    }else{
                        readData(qr, qr2, itemId)
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