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
import com.vr.smartreceiving.helper.showSnack
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class AddActivity : AppCompatActivity() {
    lateinit var etNama : EditText
    lateinit var etSatuan : EditText
    lateinit var etPerRak : EditText
    lateinit var etQr1 : EditText
    lateinit var etQr2 : EditText //opsional
    lateinit var spJenis : Spinner
    lateinit var spMerek : Spinner
    lateinit var tvJudul: TextView
    lateinit var contentView: RelativeLayout
    lateinit var btnBack: ImageView
    lateinit var btnSimpan: Button

    private var type = "" //edit atau add
    private var uid = ""
    private var docId = ""
    private var kode1 = ""
    private var perRak = ""
    private var jenis = ""
    private var merek = ""
    private var nama = ""
    private var satuan = ""
    private var kode2 = ""
    private var banyakQr = ""
    private var scanAt = ""
    private var editAt = ""
    private var createdAt = ""
    lateinit var progressDialog: ProgressDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        initView()
        initSpinner()
        initIntent()
        setIntent()
        initClick()
    }
    private fun initView(){
        etNama = findViewById(R.id.etNama)
        etSatuan = findViewById(R.id.etSatuan)
        etPerRak = findViewById(R.id.etPerRak)
        etQr1 = findViewById(R.id.etQr1)
        etQr2 = findViewById(R.id.etQr2)
        tvJudul = findViewById(R.id.tvJudul)
        contentView = findViewById(R.id.contentView)
        btnBack = findViewById(R.id.btnBack)
        btnSimpan = findViewById(R.id.btnSimpan)
        spJenis = findViewById(R.id.spJenis)
        spMerek = findViewById(R.id.spMerek)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
    }
    private fun initSpinner(){
        val arJenis = resources.getStringArray(R.array.jenis)
        val arMerek = resources.getStringArray(R.array.merek)
        val adapterJenis = ArrayAdapter(this, android.R.layout.simple_spinner_item, arJenis)
        val adapterMerek = ArrayAdapter(this, android.R.layout.simple_spinner_item, arMerek)
        spJenis.adapter = adapterJenis
        spMerek.adapter = adapterMerek
    }
    private fun initIntent(){
        type = intent.getStringExtra("type").toString()
        uid = intent.getStringExtra("uid").toString()
        docId = intent.getStringExtra("docId").toString()
        kode1 = intent.getStringExtra("kode1").toString()
        perRak = intent.getStringExtra("perRak").toString()
        jenis = intent.getStringExtra("jenis").toString()
        merek = intent.getStringExtra("merek").toString()
        nama = intent.getStringExtra("nama").toString()
        satuan = intent.getStringExtra("satuan").toString()
        kode2 = intent.getStringExtra("kode2").toString()
        banyakQr = intent.getStringExtra("banyakQr").toString()
        scanAt = intent.getStringExtra("scanAt").toString()
        editAt = intent.getStringExtra("editAt").toString()
        createdAt = intent.getStringExtra("createdAt").toString()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIntent(){
        if(type == "edit"){
            etNama.setText(nama)
            etSatuan.setText(satuan)
            etPerRak.setText(perRak)
            etQr1.setText(kode1)
            etQr2.setText(kode2)
            tvJudul.text = "Edit Barang"
            btnSimpan.text = "Simpan"
            // Set spinner berdasarkan nilai yang telah Anda terima
            val jenisPosition = (spJenis.adapter as ArrayAdapter<String>).getPosition(jenis)
            spJenis.setSelection(jenisPosition)

            val merekPosition = (spMerek.adapter as ArrayAdapter<String>).getPosition(merek)
            spMerek.setSelection(merekPosition)
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            editAt = formatted
        }else{
            tvJudul.text = "Tambah Barang"
            btnSimpan.text = "Tambah"
            //tanggal sekarang dan jam
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            val formatted = currentDateTime.format(formatter)
            createdAt = formatted
        }
    }
    private fun chekData(){
        if(etNama.text.toString().isEmpty()){
            etNama.error = "Nama barang tidak boleh kosong"
            etNama.requestFocus()
            return
        }
        if(etSatuan.text.toString().isEmpty()){
            etSatuan.error = "Satuan barang tidak boleh kosong"
            etSatuan.requestFocus()
            return
        }
        if(etPerRak.text.toString().isEmpty()){
            etPerRak.error = "Per rak tidak boleh kosong"
            etPerRak.requestFocus()
            return
        }
        if(etQr1.text.toString().isEmpty()) {
            etQr1.error = "Kode 1 tidak boleh kosong"
            etQr1.requestFocus()
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
        //cek jika kode2 kosong
        if(etQr2.text.toString().isEmpty()){
            banyakQr = "1"
        }else{
            banyakQr = "2"
        }
        progressDialog.show()
        val barangData = hashMapOf(
            "uid" to UUID.randomUUID().toString(),
            "kode1" to etQr1.text.toString(),
            "perRak" to etPerRak.text.toString(),
            "jenis" to spJenis.selectedItem.toString(),
            "merek" to spMerek.selectedItem.toString(),
            "nama" to etNama.text.toString(),
            "satuan" to etSatuan.text.toString(),
            "kode2" to etQr2.text.toString(),
            "banyakQr" to banyakQr,
            "scanAt" to scanAt,
            "editAt" to editAt,
            "createdAt" to createdAt,
        )
        val db = FirebaseFirestore.getInstance()
        // Add the product data to Firestore
        db.collection("barang")
            .add(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                progressDialog.dismiss()
                showSnack(this,"Gagal menyimpan barang ${e.message}")
            }
    }
    private fun editData(){
        progressDialog.show()
        if(etQr2.text.toString().isEmpty()){
            banyakQr = "1"
        }else{
            banyakQr = "2"
        }
        val barangData = hashMapOf(
            "kode1" to etQr1.text.toString(),
            "perRak" to etPerRak.text.toString(),
            "jenis" to spJenis.selectedItem.toString(),
            "merek" to spMerek.selectedItem.toString(),
            "nama" to etNama.text.toString(),
            "satuan" to etSatuan.text.toString(),
            "kode2" to etQr2.text.toString(),
            "banyakQr" to banyakQr,
            "editAt" to editAt,
        )
        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .document(docId)
            .update(barangData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                showSnack(this,"Berhasil menyimpan barang")
                progressDialog.dismiss()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
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