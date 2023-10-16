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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.LoginActivity
import com.vr.smartreceiving.adapter.BarangAdapter
import com.vr.smartreceiving.adapter.UserAdapter
import com.vr.smartreceiving.helper.showSnack
import com.vr.smartreceiving.model.BarangModel
import com.vr.smartreceiving.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ListUserActivity : AppCompatActivity() {
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: RelativeLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var btnCari: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnAdd: Button
    private lateinit var progressDialog: ProgressDialog
    val TAG = "LOAD DATA"
    private val userList: MutableList<UserModel> = mutableListOf()
    lateinit var mFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_user)
        initView()
        initRc()
        initData()
        initCari()
        initClick()
    }
    private fun initView(){
        mFirestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.rcBarang)
        contentView = findViewById(R.id.contentView)
        searchLayout = findViewById(R.id.searchLayout)
        btnCari = findViewById(R.id.btnCari)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

    }
    private fun initRc(){
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@ListUserActivity, 1)
            // set the custom adapter to the RecyclerView
            userAdapter = UserAdapter(
                userList,
                this@ListUserActivity,
                { user -> editBarang(user) },
                { user -> hapusBarang(user) }
            )
        }
    }
    private fun initData(){
        readData()
        recyclerView.adapter = userAdapter
    }
    private fun initCari(){
        userAdapter.filter("")
        btnCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                userAdapter.filter(s.toString())
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
                val result = mFirestore.collection("user").whereEqualTo("role","user").get().await()
                val users = mutableListOf<UserModel>()
                for (document in result) {
                    val user = document.toObject(UserModel::class.java)
                    val docId = document.id
                    user.docId = docId
                    users.add(user)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    userList.addAll(users)
                    userAdapter.filteredBarangList.addAll(users)
                    userAdapter.notifyDataSetChanged()
                    progressDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                progressDialog.dismiss()
            }
        }
    }

    private fun editBarang(user: UserModel) {
        //intent ke homeActivity fragment add
        val intent = Intent(this, AddUserActivity::class.java)
        intent.putExtra("type", "edit")
        intent.putExtra("docId", user.docId)
        intent.putExtra("uid", user.uid)
        intent.putExtra("nama", user.nama)
        intent.putExtra("username", user.username)
        intent.putExtra("password", user.password)
        intent.putExtra("noHp", user.noHp)

        startActivity(intent)
    }
    private fun hapusBarang(user: UserModel) {
        //dialog konfirmasi
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Yakin ingin menghapus ${user.nama}?")
        builder.setPositiveButton("Ya") { dialog, which ->
            //hapus barang dari firestore
            progressDialog.show()
            val db = FirebaseFirestore.getInstance()
            db.collection("user").document(user.docId.toString())
                .delete()
                .addOnSuccessListener {
                    showSnack(this,"Berhasil menghapus user")
                    progressDialog.dismiss()
                    // Redirect to SellerActivity fragment home
                    val intent = Intent(this, ListUserActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Error occurred while adding product
                    Log.w(TAG, "Error getting documents : $e")
                    progressDialog.dismiss()
                }
        }
    }
    private fun initClick(){
        btnBack.setOnClickListener {
            finish()
        }
        btnAdd.setOnClickListener {
            //intent ke homeActivity fragment add
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra("type", "add")
            startActivity(intent)
        }
    }
}