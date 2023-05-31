package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class RecruiterLoginActivity : AppCompatActivity() {
    lateinit var btn_otp : Button
    lateinit var phone : EditText
    lateinit var tv: TextView
    lateinit var til : TextInputLayout
    private lateinit var phoneAuthProvider: PhoneAuthProvider
    private var verificationId: String? = null
    lateinit var auth : FirebaseAuth
    var phonereceived : String = ""
    lateinit var label : TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_login)
        btn_otp = findViewById(R.id.otpbtnloginR)
        phone = findViewById(R.id.phoneloginR)
        tv = findViewById(R.id.regbtnlogR)
        auth = FirebaseAuth.getInstance()
        //label = findViewById(R.id.mobileloginRtv)
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        til.editText?.setOnFocusChangeListener { view, b ->
            if(b) {
                phone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            } else{
                phone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
            }
        }
//        phone.setOnFocusChangeListener { view, b ->
//            if(b)
//            {
//                phone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
//            }
//            else{
//                phone.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
//            }
//        }
        btn_otp.setOnClickListener {
            phonereceived = "+91 " + phone.text.toString()
            sendOtp()
        }
        phone.setOnFocusChangeListener { view, b ->
            phone.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
        tv.setOnClickListener {
            startActivity(Intent(this,RecruiterRegActivity::class.java))
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
            this@RecruiterLoginActivity.verificationId = verificationId
            // Start the OTP verification activity
            val intent = Intent(this@RecruiterLoginActivity, OTPJobActivity::class.java)
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