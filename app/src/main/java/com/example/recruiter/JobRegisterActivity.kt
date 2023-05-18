package com.example.recruiter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JobRegisterActivity : AppCompatActivity() {
    lateinit var btn_next: Button
    lateinit var fname : EditText; lateinit var lname : EditText
    lateinit var phone : EditText; lateinit var email : EditText
    lateinit var city : EditText; lateinit var expsal : EditText
    lateinit var radio : RadioGroup; lateinit var tv : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_register)
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
            finish()
        }

    }
}