package com.vr.smartreceiving.activity.admin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.LoginActivity
import com.vr.smartreceiving.adapter.BarangAdapter
import com.vr.smartreceiving.db.AppDatabase
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BarangActivity : AppCompatActivity() {
    private lateinit var barangAdapter: BarangAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnAdd: CardView
    private lateinit var progressDialog: ProgressDialog

    private lateinit var database: AppDatabase

    val TAG = "LOAD DATA AdminActvy"
    private val barangList: MutableList<BarangModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        initView()
        initRc()
        initData()
        initCari()
        initClick()
    }
    private fun initView(){
        recyclerView = findViewById(R.id.rcBarang)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnAdd = findViewById(R.id.btnAdd)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        database = AppDatabase.getInstance(applicationContext)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@BarangActivity, 1)
            // set the custom adapter to the RecyclerView
            barangAdapter = BarangAdapter(
                barangList,
                this@BarangActivity,
                { barang -> editBarang(barang) },
                { barang -> hapusBarang(barang) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = barangAdapter
    }
    private fun initCari(){
        barangAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                barangAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    private fun readData() {
        progressDialog.show()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                //ambil barang dari database room
                val db = AppDatabase.getInstance(this@BarangActivity)
                val barangDao = db.barangDao()
                // Mendapatkan data barang dari database
                val barangFromRoom = barangDao.getAllbarang()
                // Memasukkan data barang dari Room ke dalam barangList
                withContext(Dispatchers.Main) {
                    barangList.clear() // Membersihkan list sebelum memasukkan data baru
                    barangList.addAll(barangFromRoom) // Menambahkan data dari Room ke dalam list
                    barangAdapter.filteredBarangList.addAll(barangFromRoom)
                    barangAdapter.notifyDataSetChanged() // Memberitahu adapter bahwa data telah berubah
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }


    private fun editBarang(barang: BarangModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("uid", barang.uid)
        intent.putExtra("nama", barang.nama)
        intent.putExtra("group", barang.kelompok)
        intent.putExtra("kode", barang.kode)
        startActivity(intent)
    }
    private fun hapusBarang(barang: BarangModel) {
        // Dialog konfirmasi
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${barang.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Hapus barang dari Firestore
            progressDialog.show()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    //hapus barang dari database room
                    val db = AppDatabase.getInstance(this@BarangActivity)
                    //hapus barang dari barangList
                    val barangDao = db.barangDao()
                    barangDao.delete(barang.uid!!.toInt())
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        showSnack(this@BarangActivity, "Berhasil menghapus ${barang.nama}")
                        val intent = Intent(this@BarangActivity, BarangActivity::class.java)
                        intent.putExtra("group", barang.kelompok)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting documents : $e")
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        showSnack(this@BarangActivity, "Gagal menghapus ${barang.nama}")
                    }
                }
            }
        }

        // Menampilkan dialog konfirmasi
        builder.create().show()
    }

    private fun initClick(){
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("type", "tambah")
            startActivity(intent)
        }
    }
}