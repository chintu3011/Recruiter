package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat

class JobLoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var btn_next : Button
        lateinit var phonenum : EditText
        lateinit var tv : TextView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_login)
        btn_next = findViewById(R.id.otpbtnloginj)
        phonenum = findViewById(R.id.phoneloginJ)
        tv = findViewById(R.id.regbtnlogj)
        phonenum.setOnFocusChangeListener { view, b ->
            phonenum.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        tv.setOnClickListener {
            startActivity(Intent(this,JobRegisterActivity::class.java))
            finish()
        }
        btn_next.setOnClickListener {
            val intent = Intent(this,OTPActivityJobLogin::class.java)
            intent.putExtra("phone",phonenum.text.toString())
            startActivity(intent)
            finish()
        }
    }
}