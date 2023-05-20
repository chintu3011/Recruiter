package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
import android.view.View.*
class JobLoginActivity : AppCompatActivity() {
    private lateinit var phoneAuthProvider: PhoneAuthProvider
    private var verificationId: String? = null
    lateinit var auth : FirebaseAuth
    var phonereceived : String = ""
    lateinit var decorView: View
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var btn_next : Button
        lateinit var phonenum : EditText
        lateinit var tv : TextView
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_login)
        fullScreen()
        btn_next = findViewById(R.id.otpbtnloginj)
        phonenum = findViewById(R.id.phoneloginJ)
        tv = findViewById(R.id.regbtnlogj)
        auth = FirebaseAuth.getInstance()
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        phonenum.setOnFocusChangeListener { view, b ->
            phonenum.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
        tv.setOnClickListener {
            startActivity(Intent(this,JobRegisterActivity::class.java))
            finish()
        }

        btn_next.setOnClickListener {
            phonereceived = "+91 " + phonenum.text.toString()
            sendOtp()
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
            this@JobLoginActivity.verificationId = verificationId
            // Start the OTP verification activity
            val intent = Intent(this@JobLoginActivity, OTPJobActivity::class.java)
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