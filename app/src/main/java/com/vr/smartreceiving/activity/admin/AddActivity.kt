package com.vr.smartreceiving.activity.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.vr.smartreceiving.R
import com.vr.smartreceiving.db.AppDatabase
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddActivity : AppCompatActivity() {
    lateinit var etNama : EditText
    lateinit var etKode : EditText
    lateinit var etGroup : EditText //opsional
    lateinit var tvJudul: TextView
    lateinit var contentView: RelativeLayout
    lateinit var btnBack: ImageView
    lateinit var btnSimpan: Button

    private var type = "" //edit atau add
    private var uid = 0
    private var kode = ""
    private var nama = ""
    private var group = ""

    lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        initView()
        initIntent()
        setIntent()
        initClick()
    }
    private fun initView(){
        etNama = findViewById(R.id.etNama)
        etGroup = findViewById(R.id.etGroup)
        etKode = findViewById(R.id.etKode)
        tvJudul = findViewById(R.id.tvJudul)
        contentView = findViewById(R.id.contentView)
        btnBack = findViewById(R.id.btnBack)
        btnSimpan = findViewById(R.id.btnSimpan)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }

    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        uid = intent.getIntExtra("uid",0)
        nama = intent.getStringExtra("nama").toString()
        kode = intent.getStringExtra("kode").toString()
        group = intent.getStringExtra("group").toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIntent(){
        if(type == "edit"){
            etNama.setText(nama)
            etKode.setText(kode)
            etGroup.setText(group)
            tvJudul.text = "Edit Barang"
            btnSimpan.text = "Simpan"

        }else{
            tvJudul.text = "Tambah Barang"
            btnSimpan.text = "Tambah"

        }
    }
    private fun chekData(){
        if(etNama.text.toString().isEmpty()){
            etNama.error = "Nama barang tidak boleh kosong"
            etNama.requestFocus()
            return
        }
        if(etKode.text.toString().isEmpty()){
            etKode.error = "Kode barang tidak boleh kosong"
            etKode.requestFocus()
            return
        }
        if(etGroup.text.toString().isEmpty()){
            etGroup.error = "Group tidak boleh kosong"
            etGroup.requestFocus()
            return
        }
        //tambah atau edit data
        if(type == "edit"){
            if (!isFinishing) {
                progressDialog.show()
                editData()
            }
        }else{
            if (!isFinishing) {
                progressDialog.show()
                tambahData()
            }
        }

    }
    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnSimpan.setOnClickListener {
            chekData()
        }
    }
    private fun tambahData(){
        //insert barang ke appdatabase
        val db = AppDatabase.getInstance(this)
        //cek kode barang sudah ada atau belum
        GlobalScope.launch {
            val barangKode = db.barangDao().getbarangByKode(etKode.text.toString())
            withContext(Dispatchers.Main){
                if(barangKode != null){
                    progressDialog.dismiss()
                    showSnack(this@AddActivity,"Kode barang sudah ada")

                }else{
                    insertDaata()
                }
            }

        }
    }
    private fun insertDaata(){
        val barang = BarangModel(
            null,
            etKode.text.toString(),
            etNama.text.toString(),
            etGroup.text.toString(),
        )
        val db = AppDatabase.getInstance(this)
        GlobalScope.launch {
            db.barangDao().insert(barang)
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                showSnack(this@AddActivity, "Berhasil menambah barang")
                val intent = Intent(this@AddActivity, BarangActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
    private fun editData(){
        //insert barang ke appdatabase
        val db = AppDatabase.getInstance(this)
        //cek kode barang sudah ada atau belum
        GlobalScope.launch {
            val barang = BarangModel(
                uid,
                etNama.text.toString(),
                etKode.text.toString(),
                etGroup.text.toString(),
            )
            db.barangDao().update(barang)
            withContext(Dispatchers.Main){

                progressDialog.dismiss()
                showSnack(this@AddActivity,"Berhasil mengedit barang")
                val intent = Intent(this@AddActivity, BarangActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }

        }
    }
    override fun onDestroy() {
        progressDialog.dismiss()
        super.onDestroy()
    }

}