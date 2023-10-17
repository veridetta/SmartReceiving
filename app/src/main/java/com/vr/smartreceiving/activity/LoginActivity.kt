package com.vr.smartreceiving.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vr.smartreceiving.R
import com.vr.smartreceiving.activity.admin.AdminActivity
import com.vr.smartreceiving.activity.user.UserActivity
import com.vr.smartreceiving.helper.showSnack

class LoginActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    // Declare UI elements
    private lateinit var buttonLogin: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
        clickView()
    }

    fun initView(){
        firestore = FirebaseFirestore.getInstance()
        // Initialize UI elements
        buttonLogin = findViewById(R.id.buttonLogin)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
    }
    fun clickView(){
        buttonLogin.setOnClickListener {
            progressDialog.show()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            firestore.collection("users").whereEqualTo("username", email).whereEqualTo("password", password).limit(1).get()
                .addOnSuccessListener { documentSnapshot ->
                    progressDialog.dismiss()
                    val userRole = documentSnapshot.documents[0].getString("role")
                    // Save user role to SharedPreferences
                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isLogin", true)
                    editor.putString("userRole", userRole)
                    editor.putString("userUid", documentSnapshot.documents[0].getString("uid"))
                    editor.putString("userName",   documentSnapshot.documents[0].getString("nama"))
                    editor.putString("userUsername", documentSnapshot.documents[0].getString("username"))
                    editor.apply()
                    Log.d("Login","Role $userRole")
                    // Redirect to appropriate activity based on user role
                    when (userRole) {
                        "admin" ->
                            startActivity(
                                Intent(this, AdminActivity::class.java)
                            )
                        "user" ->
                            startActivity(
                                Intent(this, UserActivity::class.java)
                            )
                    }
                    finish()
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    showSnack(this, "Login failed. Please check your credentials.")
                }
        }
    }
}