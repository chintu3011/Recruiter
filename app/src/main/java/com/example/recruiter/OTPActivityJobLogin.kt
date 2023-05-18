package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class OTPActivityJobLogin : AppCompatActivity() {
    lateinit var tv : TextView; lateinit var btn : Button
    lateinit var otp1 : EditText; lateinit var otp2 : EditText
    lateinit var otp3 : EditText; lateinit var otp4 : EditText
    lateinit var otp5 : EditText; lateinit var otp6 : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpjob_login)
        tv = findViewById(R.id.mobileotplog)
        otp1 = findViewById(R.id.ot1log)
        otp2 = findViewById(R.id.ot2log)
        otp3 = findViewById(R.id.ot3log)
        otp4 = findViewById(R.id.ot4log)
        otp5 = findViewById(R.id.ot5log)
        otp6 = findViewById(R.id.ot6log)
        btn = findViewById(R.id.btnotplogJ)
        val phonenum = "+91 " + intent.getStringExtra("phone")
        tv.text = phonenum
        btn.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
        }
    }
}