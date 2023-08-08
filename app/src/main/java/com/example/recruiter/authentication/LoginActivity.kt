package com.example.recruiter.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.recruiter.R
import com.example.recruiter.databinding.ActivityLoginBinding
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

    private lateinit var binding: ActivityLoginBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var userType:String ?= null

    private lateinit var phoneNo :String
    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    companion object{
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val window: Window = this@LoginActivity.window
        val background =ContextCompat.getDrawable(this@LoginActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()


        setOnClickListener()
        binding.cpp.registerCarrierNumberEditText(binding.inputPhoneNo)

        binding.inputPhoneNo.setOnFocusChangeListener { view, b ->
            binding.inputPhoneNo.background = ContextCompat.getDrawable(this, R.drawable.borderr)
        }
    }
    
    private fun setOnClickListener() {
        binding.btnLogin.setOnClickListener(this)
        binding.btnRegistration.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnLogin -> {
                sentOtp()
            }
            R.id.btnRegistration -> {
                val intent = Intent(this@LoginActivity,AskActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

            }
        }
    }

    private fun sentOtp() {
        binding.btnLogin.visibility = GONE
        binding.progressBar.visibility = VISIBLE
        phoneNo = "+" + binding.cpp.fullNumber.toString().trim{it <= ' '}
        getUserTypeIfNotSignIn(phoneNo) { userType ->

            if (userType.isNotEmpty()) {
                val correct = checkInputData(phoneNo)
                if (correct) {
                    mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                        override fun onVerificationFailed(e: FirebaseException) {
                            binding.btnLogin.visibility = VISIBLE
                            binding.progressBar.visibility = GONE
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
                            binding.btnLogin.visibility = VISIBLE
                            binding.progressBar.visibility = GONE
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
                }
                else {
                    Log.d(TAG, "Input data is incorrect")
                    makeToast("Input data is incorrect", 0)
                    binding.btnLogin.visibility = VISIBLE
                    binding.progressBar.visibility = GONE
                }
            }
        }
    }

    private fun checkInputData(phoneNo: String): Boolean {

        if (binding.inputPhoneNo.text.toString().isEmpty()) {
            binding.inputPhoneNo.error = "Please provide a mobile no."
            binding.inputPhoneNo.requestFocus()
            return false
        }
        if (binding.inputPhoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.inputPhoneNo.error = "Incorrect Mobile no"
            binding.inputPhoneNo.requestFocus()
            return false
        }
        return true
    }

    private fun navigateToNextActivity() {

        val intent = Intent(this@LoginActivity, OTPVerificationLoginActivity::class.java)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("storedVerificationId",storedVerificationId)
        intent.putExtra("userType",userType)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun getUserTypeIfNotSignIn(mobileNo: String, callback: (String) -> Unit){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grandParentKey:String
                Log.d(TAG,"Finding User for :${mobileNo}")
                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val userMobileNo = userSnapshot.child("userPhoneNumber").getValue(String::class.java)
                        if (userMobileNo.equals(mobileNo)) {
                            grandParentKey =
                                userTypeSnapshot.key.toString() // Key of the grandparent ("Job Seeker" or "Recruiter")
                            Log.d(TAG,"userPhoneNumber: $userMobileNo => userType: $grandParentKey")
                            userType = grandParentKey
                            if (!userType.isNullOrEmpty()){
                                callback(userType!!)
                            }
                            return
                        }
                    }
                }
                binding.btnLogin.visibility = VISIBLE
                binding.progressBar.visibility = GONE
                makeToast("phone number not found",0)
                Log.d(TAG,"$mobileNo : Not match")
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
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}