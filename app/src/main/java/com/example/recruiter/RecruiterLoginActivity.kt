package com.example.recruiter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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

class RecruiterLoginActivity : AppCompatActivity() {
    lateinit var btn_otp : Button
    lateinit var phone : EditText
    lateinit var tv: TextView
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
        label = findViewById(R.id.mobileloginRtv)
        phoneAuthProvider = PhoneAuthProvider.getInstance()
        phone.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                phone.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
                animateLabelUp()
            } else {
                if (phone.text.isNullOrEmpty()) {
                    animateLabelDown()
                }
            }
        }
        btn_otp.setOnClickListener {
            phonereceived = "+91 " + phone.text.toString()
            sendOtp()
        }
        phone.setOnFocusChangeListener { view, b ->
            phone.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
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
    private fun animateLabelUp() {
        label.animate()
            .translationY(-label.height.toFloat())
            .alpha(0f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    label.visibility = View.GONE
                }
            })
    }

    private fun animateLabelDown() {
        label.visibility = View.VISIBLE
        label.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200)
    }
}