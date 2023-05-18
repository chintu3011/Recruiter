package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat

class RecruiterRegActivity1 : AppCompatActivity() {
    lateinit var btn_prev : Button
    lateinit var btn_next : Button
    lateinit var desg : EditText; lateinit var jobdesc : EditText
    lateinit var sal : EditText; lateinit var jobloc : EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_reg1)
        btn_prev = findViewById(R.id.prevbtnregr1)
        btn_next = findViewById(R.id.nextbtnregr1)
        desg = findViewById(R.id.jobtypeR)
        jobdesc = findViewById(R.id.jobdescR)
        sal = findViewById(R.id.SalaryR)
        jobloc = findViewById(R.id.JoblocationR)
        btn_next.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
        }
        btn_prev.setOnClickListener {
            startActivity(Intent(this,RecruiterRegActivity::class.java))
        }
        desg.setOnFocusChangeListener { view, b ->
            desg.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        jobloc.setOnFocusChangeListener { view, b ->
            jobloc.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        jobdesc.setOnFocusChangeListener { view, b ->
            jobdesc.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        sal.setOnFocusChangeListener { view, b ->
            sal.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
    }
}