package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JobRegisterActivity1 : AppCompatActivity() {
    lateinit var btn_next: Button
    lateinit var btn_prev : Button
    lateinit var bio : EditText; lateinit var jobtype : EditText
    lateinit var compname : EditText; lateinit var expesal : EditText
    lateinit var expdur : EditText; lateinit var freshexp : RadioGroup
    lateinit var degree : Spinner; lateinit var tv : TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_register1)
        btn_next = findViewById(R.id.nextbtnregj1)
        btn_prev = findViewById(R.id.prevbtnregj1)
        bio = findViewById(R.id.BioJ)
        jobtype = findViewById(R.id.jobtypeJ)
        compname = findViewById(R.id.ExpCompJ)
        expesal = findViewById(R.id.ExpeSalJ)
        expdur = findViewById(R.id.ExpDurJ)
        freshexp = findViewById(R.id.radiogrpfrsh)
        tv = findViewById(R.id.loginbtnregj2)
        degree = findViewById(R.id.degreetype)
        tv.setOnClickListener {
            startActivity(Intent(this,JobLoginActivity::class.java))
        }
        bio.setOnFocusChangeListener { view, b ->
            bio.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        jobtype.setOnFocusChangeListener { view, b ->
            jobtype.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        compname.setOnFocusChangeListener { view, b ->
            compname.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        expesal.setOnFocusChangeListener { view, b ->
            expesal.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        expdur.setOnFocusChangeListener { view, b ->
            expdur.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        degree.setOnFocusChangeListener { view, b ->
            degree.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.degree_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        degree.adapter = adapter
        val selectedValue: String = degree.selectedItem.toString()
        var selected : String = ""
        freshexp.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            selected = radioButton.text.toString()
        }
        btn_next.setOnClickListener {
            val bundle = Bundle()
            val bundle1 = intent.extras
            var fname : String = ""; var lname : String = ""
            var phone : String = ""; var email : String = ""
            var city : String = ""; var expsal : String = ""
            var workmode : String = ""
            if(bundle1!=null)
            {
                fname = bundle1.getString("fname").toString()
                lname = bundle1.getString("lname").toString()
                phone = bundle1.getString("phone").toString()
                email = bundle1.getString("email").toString()
                city = bundle1.getString("city").toString()
                expsal = bundle1.getString("expsal").toString()
                workmode = bundle1.getString("workmode").toString()
            }
            bundle.putString("fname",fname)
            bundle.putString("lname",lname)
            bundle.putString("phone",phone)
            bundle.putString("email",email)
            bundle.putString("city",city)
            bundle.putString("expsal",expsal)
            bundle.putString("workmode", workmode)
            bundle.putString("bio",bio.text.toString())
            bundle.putString("jobtype",jobtype.text.toString())
            bundle.putString("compname",compname.text.toString())
            bundle.putString("expesal",expesal.text.toString())
            bundle.putString("expdur",expdur.text.toString())
            bundle.putString("frshexp",selected)
            bundle.putString("degree",selectedValue)
            val intent = Intent(this,JobRegisterActivity2::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }
        btn_prev.setOnClickListener {
            startActivity(Intent(this,JobRegisterActivity::class.java))
            finish()
        }
    }
}