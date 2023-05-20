package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class RecruiterRegActivity1 : AppCompatActivity() {
    lateinit var btn_prev : Button
    lateinit var btn_next : Button; lateinit var tv : TextView
    lateinit var desg : EditText; lateinit var jobdesc : EditText
    lateinit var sal : EditText; lateinit var jobloc : EditText
    var phonereceived = ""
    private lateinit var phoneAuthProvider: PhoneAuthProvider
    private var verificationId: String? = null
    lateinit var auth : FirebaseAuth
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
        tv = findViewById(R.id.loginbtnregr1)
        auth=FirebaseAuth.getInstance()
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        btn_next.setOnClickListener {
            val bundle = intent.extras

            if(bundle!=null)
            {
                phonereceived = "+91 " + bundle.getString("phone").toString()
            }
            sendOtp()
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
        tv.setOnClickListener {
            startActivity(Intent(this,RecruiterLoginActivity::class.java))
        }
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            //signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // Handle verification failure
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@RecruiterRegActivity1.verificationId = verificationId
            // Start the OTP verification activity
            val intent = Intent(this@RecruiterRegActivity1, OTPJobActivity::class.java)
            intent.putExtra("verification_id", verificationId)
            intent.putExtra("phonenum",phonereceived)
            startActivity(intent)
        }
    }
    private fun sendOtp() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phonereceived)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}