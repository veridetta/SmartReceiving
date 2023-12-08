package com.vr.smartreceiving.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.vr.smartreceiving.MainActivity
import com.vr.smartreceiving.R
import com.vr.smartreceiving.helper.showSnack

class LoginActivity : AppCompatActivity() {

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
            if(email.isEmpty() || password.isEmpty()) {
                progressDialog.dismiss()
                showSnack(this, "Oops, masih ada yang kosong.")
            }else if(email=="admin@gmail.com" && password=="admin"){
                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                var userRole = "admin"
                editor.putBoolean("isLogin", true)
                editor.putString("userRole", "admin")
                editor.putString("userUid", "1")
                editor.putString("userName",   userRole)
                editor.putString("userUsername", "admin")
                editor.apply()
                progressDialog.dismiss()
                // Redirect to appropriate activity based on user role
                when (userRole) {
                    "admin" ->
                        startActivity(
                            Intent(this, MainActivity::class.java)
                        )
                }
                finish()
            }else{
                progressDialog.dismiss()
                showSnack(this, "Login failed. Please check your credentials.")
            }
        }
    }
}