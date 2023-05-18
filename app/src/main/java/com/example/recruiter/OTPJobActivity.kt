package com.example.recruiter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class OTPJobActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var verificationId: String
    lateinit var otp_ver: Button;
    lateinit var tv: TextView
    lateinit var ot_1: EditText;
    lateinit var ot_2: EditText
    lateinit var ot_3: EditText;
    lateinit var ot_4: EditText
    lateinit var ot_5: EditText;
    lateinit var ot_6: EditText
    var mCallbacks: OnVerificationStateChangedCallbacks? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpjob)
        auth = FirebaseAuth.getInstance()
        otp_ver = findViewById(R.id.btnotp)
        tv = findViewById(R.id.mobile)
        ot_1 = findViewById(R.id.ot1)
        ot_2 = findViewById(R.id.ot2)
        ot_3 = findViewById(R.id.ot3)
        ot_4 = findViewById(R.id.ot4)
        ot_5 = findViewById(R.id.ot5)
        ot_6 = findViewById(R.id.ot6)
        val phonenum = "+91 " + intent.getStringExtra("phonenum")
        verificationId = intent.getStringExtra("verificationId").toString()
        tv.text = phonenum
        otp_ver.setOnClickListener {
            if (ot_1.text.isEmpty() || ot_2.text.isEmpty() ||
                ot_3.text.isEmpty() || ot_4.text.isEmpty() ||
                ot_5.text.isEmpty() || ot_6.text.isEmpty()
            ) {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_LONG).show()
            } else {
                if(verificationId!=null)
                {
                    Toast.makeText(this,verificationId,Toast.LENGTH_LONG).show()
                    val otp = ot_1.text.toString() + ot_2.text.toString() +
                            ot_3.text.toString() + ot_4.text.toString() +
                            ot_5.text.toString() + ot_6.text.toString()
                    val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Invalid OTP!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

            }
            startPhoneNumberVerification(phonenum)
        }
    }
    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener(this@OTPJobActivity) { task ->
                            if (task.isSuccessful) {
                                // User successfully signed in
                                val intent = Intent(this@OTPJobActivity, HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                // Verification failed
                                Toast.makeText(this@OTPJobActivity, "Invalid OTP", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    Toast.makeText(
                        this@OTPJobActivity,
                        "Verification failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@OTPJobActivity.verificationId = verificationId
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User successfully signed in
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Verification failed
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
