package com.vr.smartreceiving.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.user.BeforeScanActivity
import com.vr.smartreceiving.adapter.ReportAdapter
import com.vr.smartreceiving.adapter.ReportAdapterUser
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.ReportDetailModel
import com.vr.smartreceiving.model.ReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var btnBack: ImageView
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val reportList: MutableList<ReportModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        initView()
        initRc()
        initData()
        initClick()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rcReport)
        contentView = findViewById(R.id.contentView)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ReportActivity, 1)
            // set the custom adapter to the RecyclerView
            reportAdapter = ReportAdapter(
                reportList,
                this@ReportActivity
            ){ report -> hapusBarang(report) }
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = reportAdapter
        reportAdapter.filter("")
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = mFirestore.collection("report").whereEqualTo("status","lengkap").get().await()
                val reports = mutableListOf<ReportModel>()
                for (document in result) {
                    val report = document.toObject(ReportModel::class.java)
                    val docId = document.id
                    report.docId = docId
                    reports.add(report)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    reportList.addAll(reports)
                    reportAdapter.filteredBarangList.addAll(reports)
                    reportAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
    }
    private fun hapusBarang(report: ReportModel) {
        // Dialog konfirmasi
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${report.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            progressDialog.show()
            val db = FirebaseFirestore.getInstance()

            // Ambil rakId dari report yang akan dihapus
            val rakId = report.rakId

            // Hapus data dari "reportDetail" dengan kunci "rakId"
            db.collection("reportDetail")
                .whereEqualTo("rakId", rakId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        db.collection("reportDetail").document(document.id)
                            .delete()
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error deleting reportDetail: $e")
                            }
                    }

                    // Setelah semua data "reportDetail" terhapus, baru hapus data "report"
                    db.collection("report").document(report.docId.toString())
                        .delete()
                        .addOnSuccessListener {
                            showSnack(this, "Berhasil menghapus rak dan datanya")
                            progressDialog.dismiss()

                            // Redirect to BeforeScanActivity
                            val intent = Intent(this, BeforeScanActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Error occurred while deleting "report"
                            Log.w(TAG, "Error deleting report: $e")
                            progressDialog.dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    // Error occurred while fetching "reportDetail" data
                    Log.w(TAG, "Error fetching reportDetail data: $e")
                    progressDialog.dismiss()
                }
        }

        // Menampilkan dialog konfirmasi
        builder.create().show()
    }

}