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
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.db.AppDatabase
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
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
    private var uid = ""
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
        uid = intent.getStringExtra("uid").toString()
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
            editData()
        }else{
            tambahData()
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
        progressDialog.show()
        //insert barang ke appdatabase
        val db = AppDatabase.getInstance(this)
        //cek kode barang sudah ada atau belum
        val barangKode = db.barangDao().getbarangByKode(etKode.text.toString())
        if(barangKode != null){
            progressDialog.dismiss()
            showSnack(this,"Kode barang sudah ada")
            return
        }
        val barang = BarangModel(
            UUID.randomUUID().toString(),
            etNama.text.toString(),
            etKode.text.toString(),
            etGroup.text.toString(),
        )
        db.barangDao().insert(barang)
        progressDialog.dismiss()
        showSnack(this,"Berhasil menambah barang")
        val intent = Intent(this, BarangActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    private fun editData(){
        progressDialog.show()
        //insert barang ke appdatabase
        val db = AppDatabase.getInstance(this)
        //cek kode barang sudah ada atau belum
        val barangKode = db.barangDao().getbarangByKode(etKode.text.toString())
        if(barangKode != null && barangKode.uid != uid){
            progressDialog.dismiss()
            showSnack(this,"Kode barang sudah ada")
            return
        }
        val barang = BarangModel(
            uid,
            etNama.text.toString(),
            etKode.text.toString(),
            etGroup.text.toString(),
        )
        db.barangDao().update(barang)
        progressDialog.dismiss()
        showSnack(this,"Berhasil mengedit barang")
        val intent = Intent(this, BarangActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}