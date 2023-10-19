package com.vr.smartreceiving.activity.user

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.admin.AddUserActivity
import com.vr.smartreceiving.activity.admin.AdminActivity
import com.vr.smartreceiving.activity.admin.ListUserActivity
import com.vr.smartreceiving.activity.user.scan.FirstScanActivity
import com.vr.smartreceiving.activity.user.scan.SingleScanActivity
import com.vr.smartreceiving.adapter.BarangAdapter
import com.vr.smartreceiving.adapter.ReportAdapter
import com.vr.smartreceiving.adapter.ScanAdapter
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ReportDetailModel
import com.vr.smartreceiving.model.ReportModel
import com.vr.smartreceiving.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BeforeScanActivity : AppCompatActivity() {
    lateinit var btnScanRack: CardView
    lateinit var btnScanQr: CardView
    lateinit var tvRack: TextView
    lateinit var tvItemNama: TextView
    lateinit var tvItemJumlah: TextView
    lateinit var btnTerima: CardView
    private lateinit var itemAdapter: ScanAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val itemList: MutableList<ReportDetailModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore
    private var type =""
    private var lengkap = false
    private var isRack = "false"
    private var namaRack = ""
    private var scannedItem = ""
    private var maxItem = ""
    private var itemNama = ""
    private var rackId = ""
    private var rackDocId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_scan)
        initView()
        initIntent()
        setIntent()
        initListener()
        initRc()
        initData()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rcItem)
        btnScanRack = findViewById(R.id.btnScanRack)
        btnScanQr = findViewById(R.id.btnScanQr)
        tvRack = findViewById(R.id.tvRack)
        tvItemNama = findViewById(R.id.tvItemNama)
        tvItemJumlah = findViewById(R.id.tvItemJumlah)
        btnTerima = findViewById(R.id.btnTerima)
        progressDialog = ProgressDialog(this)
    }
    private fun initListener(){
        btnScanRack.setOnClickListener {
            if(isRack=="true"){
                showSnack(this,"Rack sudah di scan")
            }else{
                val intent = Intent(this, RakScanActivity::class.java)
                intent.putExtra("type",type)
                intent.putExtra("namaRack",namaRack)
                startActivity(intent)
                finish()
            }
        }
        btnScanQr.setOnClickListener {
            if(lengkap){
                showSnack(this,"Semua barang sudah di scan")
            }else{
                if (isRack=="false"){
                    showSnack(this,"Scan rack terlebih dahulu")
                }else{
                    if(type=="HGP" || type=="FDR"){
                        val intent = Intent(this, FirstScanActivity::class.java)
                        intent.putExtra("rackId",rackId)
                        intent.putExtra("itemNama",itemNama)
                        intent.putExtra("namaRack",namaRack)
                        intent.putExtra("type",type)
                        intent.putExtra("rackDocId",rackDocId)
                        startActivity(intent)
                        finish()
                    }else{
                        val intent = Intent(this, SingleScanActivity::class.java)
                        intent.putExtra("rackId",rackId)
                        intent.putExtra("itemNama",itemNama)
                        intent.putExtra("rackDocId",rackDocId)
                        intent.putExtra("namaRack",namaRack)
                        intent.putExtra("type",type)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        btnTerima.setOnClickListener {
            if (lengkap){
                progressDialog.setMessage("Loading...")
                terima()
            }else{
                showSnack(this,"Data belum lengkap")
            }
        }
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        Log.d("TYPE",type)
        isRack = intent.getStringExtra("isRack")?:"false"
        lengkap = intent.getBooleanExtra("lengkap",false)
        namaRack = intent.getStringExtra("namaRack").toString()
        rackId = intent.getStringExtra("rackId").toString()
        itemNama = intent.getStringExtra("itemNama").toString()
        rackDocId = intent.getStringExtra("rackDocId").toString()
        Log.d("RAK", "BEFORE SCAN docId : ${rackDocId}")
    }
    private fun setIntent(){
        if (isRack=="true"){
            tvRack.text = namaRack
        }
    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@BeforeScanActivity, 1)
            // set the custom adapter to the RecyclerView
            itemAdapter = ScanAdapter(
                itemList,
                this@BeforeScanActivity
            ){ scan -> hapusBarang(scan) }
        }
    }
    private fun hapusBarang(scan: ReportDetailModel) {
        //dialog konfirmasi
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${scan.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            //hapus barang dari firestore
            progressDialog.show()
            val db = FirebaseFirestore.getInstance()
            db.collection("reportDetail").document(scan.docId.toString())
                .delete()
                .addOnSuccessListener {
                    showSnack(this,"Berhasil menghapus item")
                    progressDialog.dismiss()
                    // Redirect to SellerActivity fragment home
                    val intent = Intent(this, BeforeScanActivity::class.java)
                    intent.putExtra("rackId",rackId)
                    intent.putExtra("namaRack",namaRack)
                    intent.putExtra("aksi","reload")
                    intent.putExtra("type",type)
                    startActivity(intent)
                    finish()
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Error occurred while adding product
                    Log.w(TAG, "Error getting documents : $e")
                    progressDialog.dismiss()
                }
        }
        // Menampilkan dialog konfirmasi
        builder.create().show()
    }
    private fun initData(){
        readData()
        recyclerView.adapter = itemAdapter
        itemAdapter.filter("")
    }
    private fun readData(){
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("reportDetail").whereEqualTo("rakId",rackId)
                    .get().await()
                val reports = mutableListOf<ReportDetailModel>()
                var jumlah = 0
                var satuan = ""
                for (document in result) {
                    val report = document.toObject(ReportDetailModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                    jumlah++
                }

                withContext(Dispatchers.Main) {
                    if(jumlah.equals(maxItem)){
                        lengkap=true
                    }
                    satuan = reports[0].satuan.toString()
                    itemNama = reports[0].itemNama.toString()
                    maxItem = reports[0].perRak.toString()
                    var itemz = "("+reports[0].itemMerek+") "+reports[0].itemNama
                    if(reports[0].itemJenis == ""){
                        itemz += " - "+reports[0].itemJenis
                    }else{
                        itemz += ""
                    }
                    tvItemNama.text = "Item "+itemz
                    scannedItem = jumlah.toString()
                    tvItemJumlah.text = "Jumlah "+jumlah.toString()+"/"+maxItem+" "+satuan
                    itemList.addAll(reports)
                    itemAdapter.filteredBarangList.addAll(reports)
                    itemAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }
    private fun terima(){
        progressDialog.show()
        val barangData = hashMapOf(
            "jumlah" to maxItem,
            "status" to "lengkap"
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("report")
            .document(rackId)
            .update(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, ReportActivityUser::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan barang ${e.message}")
            }
    }
}