package com.vr.smartreceiving.activity.user

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
import com.vr.smartreceiving.adapter.BarangAdapter
import com.vr.smartreceiving.adapter.ReportAdapter
import com.vr.smartreceiving.adapter.ScanAdapter
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.ReportDetailModel
import com.vr.smartreceiving.model.ReportModel
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
    private var qr =""
    private var lengkap = false
    private var isRack = false
    private var namaRack = ""
    private var rackId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_before_scan)
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rcBarang)
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
            if(isRack){
                showSnack(this,"Rack sudah di scan")
            }else{
                val intent = Intent(this, ScanActivity::class.java)
                intent.putExtra("type",type)
                intent.putExtra("qr","rack")
                startActivity(intent)
            }
        }
        btnScanQr.setOnClickListener {
            if(lengkap){
                showSnack(this,"Semua barang sudah di scan")
            }else{
                if (!isRack){
                    showSnack(this,"Scan rack terlebih dahulu")
                }else{
                    val intent = Intent(this, ScanActivity::class.java)
                    intent.putExtra("type",type)
                    intent.putExtra("qr","qr")
                    startActivity(intent)
                }
            }
        }
        btnTerima.setOnClickListener {
            if (lengkap){
                progressDialog.setMessage("Loading...")
                progressDialog.show()
                //update ke fb set status = diterima
            }else{
                showSnack(this,"Data belum lengkap")
            }
        }
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        qr = intent.getStringExtra("qr").toString()
        isRack = intent.getBooleanExtra("isRack",false)
        lengkap = intent.getBooleanExtra("lengkap",false)
        namaRack = intent.getStringExtra("namaRack").toString()
    }
    private fun setIntent(){
        if (isRack){
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
            )
        }
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
                val result = mFirestore.collection("reportDetail").whereEqualTo("rackId",rackId)
                    .get().await()
                val reports = mutableListOf<ReportDetailModel>()
                var jumlah = 0
                var satuan = ""
                var jumlahScan = ""
                for (document in result) {
                    val report = document.toObject(ReportDetailModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                    jumlah++
                    satuan = report.satuan.toString()
                    jumlahScan = report.jumlah.toString()
                }

                withContext(Dispatchers.Main) {
                    tvItemJumlah.text = "Jumlah "+jumlah.toString()+"/"+jumlahScan+" "+satuan
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

}