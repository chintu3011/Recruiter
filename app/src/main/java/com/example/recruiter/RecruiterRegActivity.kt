package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat

class RecruiterRegActivity : AppCompatActivity() {
    lateinit var btn_next : Button
    lateinit var fname : EditText; lateinit var lname : EditText
    lateinit var phone : EditText; lateinit var email : EditText
    lateinit var compname : EditText; lateinit var tv : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_reg)
        btn_next = findViewById(R.id.nextbtnregr)
        fname = findViewById(R.id.FRname)
        lname = findViewById(R.id.LRname)
        phone = findViewById(R.id.PhoneR)
        email = findViewById(R.id.EmailR)
        compname = findViewById(R.id.CompnameR)
        tv = findViewById(R.id.loginbtnregr)
        btn_next.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("fname",fname.text.toString())
            bundle.putString("lname",lname.text.toString())
            bundle.putString("phone",phone.text.toString())
            bundle.putString("email",email.text.toString())
            bundle.putString("compname",compname.text.toString())
            val intent = Intent(this,RecruiterRegActivity1::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
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
        compname.setOnFocusChangeListener { view, b ->
            compname.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        tv.setOnClickListener {
            startActivity(Intent(this,RecruiterLoginActivity::class.java))
        }
    }
}