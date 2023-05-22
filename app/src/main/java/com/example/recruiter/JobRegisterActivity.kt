package com.example.recruiter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.view.View.*
class JobRegisterActivity : AppCompatActivity() {
    lateinit var btn_next: Button
    lateinit var fname : EditText; lateinit var lname : EditText
    lateinit var phone : EditText; lateinit var email : EditText
    lateinit var city : EditText; lateinit var expsal : EditText
    lateinit var radio : RadioGroup; lateinit var tv : TextView
    lateinit var decorView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_register)
        fullScreen()
        fname = findViewById(R.id.Fname)
        lname = findViewById(R.id.Lname)
        phone = findViewById(R.id.PhoneJ)
        email = findViewById(R.id.EmailJ)
        city = findViewById(R.id.CityJ)
        expsal = findViewById(R.id.ExpSalJ)
        radio = findViewById(R.id.radiogrp)
        tv = findViewById(R.id.loginbtnregj)
        btn_next = findViewById(R.id.nextbtnregj)
        tv.setOnClickListener {
            startActivity(Intent(this,JobLoginActivity::class.java))
            overridePendingTransition(R.anim.flip_in,R.anim.flip_out)
            finish()
        }
        fname.setOnFocusChangeListener { view, b ->
            fname.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        lname.setOnFocusChangeListener { view, b ->
            lname.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        phone.setOnFocusChangeListener { view, b ->
            phone.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        email.setOnFocusChangeListener { view, b ->
            email.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        city.setOnFocusChangeListener { view, b ->
            city.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        expsal.setOnFocusChangeListener { view, b ->
            expsal.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }

        var selectedValue : String = ""
        radio.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            selectedValue = radioButton.text.toString()
        }
        btn_next.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("fname",fname.text.toString())
            bundle.putString("lname",lname.text.toString())
            bundle.putString("phone",phone.text.toString())
            bundle.putString("email",email.text.toString())
            bundle.putString("city",city.text.toString())
            bundle.putString("expsal",expsal.text.toString())
            bundle.putString("workmode", selectedValue)
            val intent = Intent(this,JobRegisterActivity1::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@JobRegisterActivity,JobLoginActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        finish()
    }
    private fun fullScreen() {
        decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { i ->
            if (i == 0) {
                decorView.systemUiVisibility = hideSystemBars()
            }
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorView.systemUiVisibility = hideSystemBars()
        }
    }

    private fun hideSystemBars(): Int {
        return (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}
