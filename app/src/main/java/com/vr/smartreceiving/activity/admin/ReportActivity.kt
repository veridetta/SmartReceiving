package com.vr.smartreceiving.activity.admin

import android.app.ProgressDialog
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
import com.vr.smartreceiving.adapter.ReportAdapter
import com.vr.smartreceiving.adapter.ReportAdapterUser
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
            )
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
}