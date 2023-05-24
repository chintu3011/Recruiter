package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.chaos.view.PinView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPVerificationLoginActivity : AppCompatActivity(),OnClickListener{

    lateinit var txtPhoneNo : TextView
    lateinit var btnChange: TextView
    lateinit var inputOTP: PinView
    lateinit var btnVerify: Button


    private lateinit var mAuth: FirebaseAuth

    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var phoneNo: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpverification_login)
        mAuth = FirebaseAuth.getInstance()

        setXmlIDs()
        setOnClickListener()

        phoneNo = intent.getStringExtra("phoneNo").toString()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()

        txtPhoneNo.text = phoneNo
    }
    private fun setOnClickListener() {
        btnVerify.setOnClickListener(this)
        btnChange.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                startActivity(Intent(this@OTPVerificationLoginActivity,LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right)
                finish()
            }
            R.id.btnVerify -> {
                verifyOtp()
            }
        }
    }

    private fun verifyOtp() {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId,inputOTP.text.toString())
        navigateToNextActivity(credential)
    }

    private fun navigateToNextActivity(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    makeToast("Login successful!",0)
                    val intent = Intent(this@OTPVerificationLoginActivity,HomeActivity::class.java)
                    intent.putExtra("phoneNo",txtPhoneNo.text.toString())
                    startActivity(intent)
                    overridePendingTransition(R.anim.flip_in,R.anim.flip_out)
                    finish()
                } else {
                    makeToast("Login failed: ${task.exception}",1)
                    Handler(Looper.getMainLooper()).postDelayed({
                        makeToast("Try again",1)
                    },1000)
                }
            }
    }
    private fun setXmlIDs() {
        txtPhoneNo = findViewById(R.id.txtPhoneNo)
        btnChange = findViewById(R.id.btnChange)
        inputOTP = findViewById(R.id.inputOTP)
        btnVerify = findViewById(R.id.btnVerify)
    }
    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }
}
