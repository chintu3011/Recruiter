package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
    lateinit var decorView: View
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_reg1)

        fullScreen()
        btn_prev = findViewById(R.id.prevbtnregr1)
        btn_next = findViewById(R.id.nextbtnregr1)
        desg = findViewById(R.id.jobtypeR)
        jobdesc = findViewById(R.id.jobdescR)
        sal = findViewById(R.id.salary_r)
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
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            finish()
        }
        desg.setOnFocusChangeListener { view, b ->
            desg.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
        jobloc.setOnFocusChangeListener { view, b ->
            jobloc.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
        jobdesc.setOnFocusChangeListener { view, b ->
            jobdesc.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
        sal.setOnFocusChangeListener { view, b ->
            sal.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
        tv.setOnClickListener {
            startActivity(Intent(this,RecruiterLoginActivity::class.java))
            overridePendingTransition(R.anim.flip_in,R.anim.flip_out)
            finish()
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
            val intent = Intent(this@RecruiterRegActivity1, OTPRecruiterActivity::class.java)
            intent.putExtra("verification_id", verificationId)
            intent.putExtra("phonenum",phonereceived)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this,RecruiterRegActivity::class.java))
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