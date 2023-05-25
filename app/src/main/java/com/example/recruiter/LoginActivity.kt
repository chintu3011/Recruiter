package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.hbb20.CountryCodePicker
import android.view.View.*
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    
    lateinit var cpp: CountryCodePicker
    lateinit var inputPhoneNo: EditText
    lateinit var btnLogin: Button
    lateinit var btnRegistration: TextView
    lateinit var progressBar: ProgressBar

    lateinit var phoneNo :String
    lateinit var copyCredential: PhoneAuthCredential
    lateinit var storedVerificationId:String
    lateinit var code:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var jobType: String ?= null
    
    lateinit var decorView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        fullScreen()
        mAuth = FirebaseAuth.getInstance()
        
        setXMLIds()
        setOnClickListener()
        cpp.registerCarrierNumberEditText(inputPhoneNo)

        inputPhoneNo.setOnFocusChangeListener { view, b ->
            inputPhoneNo.setBackground(ContextCompat.getDrawable(this,R.drawable.borderr))
        }
    }

    private fun setOnClickListener() {
        btnLogin.setOnClickListener(this)
        btnRegistration.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnLogin -> {
                sentOtp()
            }
            R.id.btnRegistration -> {
                startActivity(Intent(this@LoginActivity,AskActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                finish()
            }
        }
    }

    private fun sentOtp() {
        phoneNo = "+" + cpp.fullNumber.toString()
        getUserJobType(phoneNo)
        val correct = checkInputData(phoneNo)
//        val correct  = true
        if (correct){
            btnLogin.visibility = GONE
            progressBar.visibility = VISIBLE
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    // OnVerificationStateChangedCallbacks
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        copyCredential = credential
                        makeToast("Verification Successful.",1)
//                        code = credential.smsCode.toString()
                        //inputOTP.text = code
                        btnLogin.visibility = VISIBLE
                        progressBar.visibility = GONE
//                        navigateToNextActivity()
                    }
                    override fun onVerificationFailed(e: FirebaseException) {
                        progressBar.visibility = GONE
                        btnLogin.visibility = VISIBLE
                        Log.d("Task", "${e.message}")
                        makeToast("verificationFailed : ${e.message}",1)

                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(verificationId, token)
                        makeToast("code sent to $phoneNo",1)
                        storedVerificationId = verificationId
                        resendToken = token
                        btnLogin.visibility = VISIBLE
                        progressBar.visibility = GONE
                        navigateToNextActivity()
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }
    private fun getUserJobType(phoneNo: String){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var grandParentKey: String? = null

                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val userMobileNo = userSnapshot.child("phoneNo").getValue(String::class.java)
                        if (userMobileNo == phoneNo) {
                            grandParentKey =
                                userTypeSnapshot.key // Key of the grandparent ("JobSeeker" or "Recruiter")
                            break
                        }
                    }
                }

                if (grandParentKey != null) {
                    jobType = grandParentKey.toString()
//                    makeToast("Grandparent Key: $jobType",0)
                } else {
                    makeToast("Mobile number not found.",0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                makeToast("error: ${error.message}",0)
            }
        })
    }

    private fun checkInputData(phoneNo: String): Boolean {

        if (inputPhoneNo.text.toString().isEmpty()) {
            inputPhoneNo.error = "Please provide a mobile no."
            inputPhoneNo.requestFocus()
            return false
        }
        if (inputPhoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            inputPhoneNo.error = "Incorrect Mobile no"
            inputPhoneNo.requestFocus()
            return false
        }
//        if(jobType != "Recruiter" && jobType != "JobSeeker") {
//            makeToast("Mobile no not found",1)
//            return false
//        }
        return true
    }

    private fun navigateToNextActivity() {
        val intent = Intent(this@LoginActivity,OTPVerificationLoginActivity::class.java)
//        intent.putExtra("jobType",jobType)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("storedVerificationId",storedVerificationId)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
    }
    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

    private fun setXMLIds() {
        cpp = findViewById(R.id.cpp)
        inputPhoneNo = findViewById(R.id.inputPhoneNo)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistration = findViewById(R.id.btnRegistration)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
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