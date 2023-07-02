package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.hbb20.CountryCodePicker
import android.view.View.*
import android.view.Window
import android.view.WindowManager
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

class LoginActivity : AppCompatActivity(),OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var cpp: CountryCodePicker
    private lateinit var inputPhoneNo: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistration: TextView
    private lateinit var progressBar: ProgressBar

    private var userType:String ?= null

    private lateinit var phoneNo :String
    private lateinit var copyCredential: PhoneAuthCredential
    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    companion object{
        private const val TAG = "LoginActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val window: Window = this@LoginActivity.window
        val background =ContextCompat.getDrawable(this@LoginActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()
        getUserType(mAuth.uid.toString())


        setXMLIds()
        setOnClickListener()
        cpp.registerCarrierNumberEditText(inputPhoneNo)

        inputPhoneNo.setOnFocusChangeListener { view, b ->
            inputPhoneNo.background = ContextCompat.getDrawable(this,R.drawable.borderr)
        }
    }



    private fun alreadyLogInNextActivity() {

        Log.d(TAG,"usertype: $userType")
        if(userType == "Job Seeker"){
            val intent = Intent(this@LoginActivity,HomeJobActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left)
        }
        else if(userType == "Recruiter"){
            val intent = Intent(this@LoginActivity,HomeRecruiterActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left)
        }
    }
    private fun getUserType(userId: String){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var grandParentKey = String()

                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val uid = userSnapshot.key
                        if (uid == userId) {
                            grandParentKey =
                                userTypeSnapshot.key.toString() // Key of the grandparent ("Job Seeker" or "Recruiter")
                            Log.d(TAG,"userId: $uid -> userTYpe: $grandParentKey")
                            break
                        }
                    }
                }
                userType = grandParentKey
                handleTaskCompletion()
            }
            override fun onCancelled(error: DatabaseError) {
                makeToast("error: ${error.message}",0)
            }
        })
    }

    private fun handleTaskCompletion() {
        onStart()
    }
    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null){
            alreadyLogInNextActivity()
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

            }
        }
    }

    private fun sentOtp() {
        phoneNo = "+" + cpp.fullNumber.toString().trim{it <= ' '}
        getUserTypeIfNotSignIn(phoneNo) { userType ->

            if (userType.isNotEmpty()) {
                val correct = checkInputData(phoneNo)
                if (correct) {
//                    btnLogin.visibility = GONE
//                    progressBar.visibility = VISIBLE
//                    val options = PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phoneNo)
//                        .setTimeout(60L, TimeUnit.SECONDS)
//                        .setActivity(this)
//                        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
//                            // OnVerificationStateChangedCallbacks
//                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                                copyCredential = credential
//                                makeToast("Verification Successful.",0)
//                                btnLogin.visibility = VISIBLE
//                                progressBar.visibility = GONE
////                        navigateToNextActivity()
//                            }
//                            override fun onVerificationFailed(e: FirebaseException) {
//                                progressBar.visibility = GONE
//                                btnLogin.visibility = VISIBLE
//                                Log.d("Task", "${e.message}")
//                                makeToast("verificationFailed : ${e.message}",1)
//
//                            }
//
//                            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                                super.onCodeSent(verificationId, token)
//                                makeToast("code sent to $phoneNo",0)
//                                storedVerificationId = verificationId
//                                resendToken = token
//                                btnLogin.visibility = VISIBLE
//                                progressBar.visibility = GONE
//                                navigateToNextActivity()
//                            }
//                        })
//                        .build()
//                    PhoneAuthProvider.verifyPhoneNumber(options)
                    mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                        override fun onVerificationFailed(e: FirebaseException) {
                            Toast.makeText(
                                this@LoginActivity,
                                e.localizedMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            storedVerificationId = verificationId
                            resendToken = token
                            btnLogin.visibility = VISIBLE
                            progressBar.visibility = GONE
                            navigateToNextActivity()
                        }
                    }
                    val options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(
                            phoneNo
                        ) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this) // Activity (for callback binding)
                        .setCallbacks(mCallback) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                } else {
                    Log.d(TAG, "Input data is incorrect")
                    makeToast("Input data is incorrect", 0)
                }
            }
            }
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
//        if(userType.equals("")){
//            makeToast("Login failed : user not found.",0)
//            Log.d(TAG,"Login failed : user not found with $phoneNo")
//            return false
//        }
//        return if (userType.equals("Job Seeker")) true
//        else if (userType.equals("Recruiter")) true
//        else {
//            makeToast("Login failed : user not found.",0)
//            Log.d(TAG,"Login failed : user not found with $phoneNo")
//            false
//        }
        return true
    }

    private fun navigateToNextActivity() {

        val intent = Intent(this@LoginActivity,OTPVerificationLoginActivity::class.java)
//        intent.putExtra("jobType",jobType)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("storedVerificationId",storedVerificationId)
        intent.putExtra("userType",userType)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }

    private fun getUserTypeIfNotSignIn(mobileNo: String, callback: (String) -> Unit){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var grandParentKey = String()
                Log.d(TAG,"Finding User for :${mobileNo}")
                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val userMobileNo = userSnapshot.child("userPhoneNumber").getValue(String::class.java)
                        if (userMobileNo.equals(mobileNo)) {
                            grandParentKey =
                                userTypeSnapshot.key.toString() // Key of the grandparent ("Job Seeker" or "Recruiter")
                            Log.d(TAG,"userPhoneNumber: $userMobileNo => userType: $grandParentKey")
                            userType = grandParentKey
                            callback(grandParentKey)
                            break
                        }
                        else{
                            Log.d(TAG,"$userMobileNo : Not match")
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG,"error: ${error.message}")
                makeToast("error: ${error.message}",0)
            }
        })
    }
    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

    private fun setXMLIds() {
        cpp = findViewById(R.id.cpp)
        inputPhoneNo = findViewById(R.id.phoneNo)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistration = findViewById(R.id.btnRegistration)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }



}