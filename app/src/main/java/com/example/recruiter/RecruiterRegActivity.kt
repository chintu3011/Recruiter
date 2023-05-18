package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat

class RecruiterRegActivity : AppCompatActivity() {
    lateinit var btn_next : Button
    lateinit var fname : EditText; lateinit var lname : EditText
    lateinit var phone : EditText; lateinit var email : EditText
    lateinit var compname : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_reg)
        btn_next = findViewById(R.id.nextbtnregr)
        fname = findViewById(R.id.FRname)
        lname = findViewById(R.id.LRname)
        phone = findViewById(R.id.PhoneR)
        email = findViewById(R.id.EmailR)
        compname = findViewById(R.id.CompnameR)
        btn_next.setOnClickListener {
            startActivity(Intent(this,RecruiterRegActivity1::class.java))
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
        compname.setOnFocusChangeListener { view, b ->
            compname.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
    }
}