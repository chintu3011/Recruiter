package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.hbb20.CountryCodePicker
import android.view.View.*
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import java.util.concurrent.TimeUnit

class RegistrationActivity : AppCompatActivity() ,OnClickListener{

    lateinit var Fname:EditText
    lateinit var Lname:EditText
    lateinit var cpp: CountryCodePicker
    lateinit var inputPhoneNo: EditText
    lateinit var inputEmail: EditText

//    lateinit var btnTerms:TextView
//    lateinit var btnConditions:TextView
    lateinit var checkBox:CheckBox
    lateinit var btnRegistration: Button
    lateinit var progressBar:ProgressBar


    
    private lateinit var mAuth: FirebaseAuth

    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var phoneNo: String
    lateinit var email: String
    lateinit var jobType: String
    lateinit var termsConditionsAcceptance:String


    lateinit var decorView: View
    lateinit var copyCredential : PhoneAuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        fullScreen()

        setXmlIDs()
        setOnClickListener()
        mAuth = FirebaseAuth.getInstance()
        Firebase.initialize(context = this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )
        cpp.registerCarrierNumberEditText(inputPhoneNo)
    }

    private fun setOnClickListener() {
        btnRegistration.setOnClickListener(this)
//        btnConditions.setOnClickListener(this)
//        btnTerms.setOnClickListener(this)
        FirebaseApp.initializeApp(this)
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.btnRegistration -> {
                registerUser()
            }
//            R.id.btnTerms -> {
//
//            }
//            R.id.btnConditions -> {
//
//            }
        }
    }
    private fun registerUser() {

        firstName = Fname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        lastName = Lname.text.toString()
        jobType = intent.getStringExtra("jobType").toString()
        termsConditionsAcceptance =  if (checkBox.isChecked) {
            "Accepted"
        }
        else{
            "Not Accepted"
        }

        val correct = inputFieldConformation(jobType,firstName,lastName,phoneNo,email,termsConditionsAcceptance)


        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            btnRegistration.visibility = GONE
            Log.d("test", "registerUser: $phoneNo")
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        progressBar.visibility = GONE
                        btnRegistration.visibility = VISIBLE
                        copyCredential = credential
                        makeToast("onVerificationCompleted:$credential",1)
                        passInfoToNextActivity()
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        progressBar.visibility = GONE
                        btnRegistration.visibility = VISIBLE
                        makeToast("Verification failed: ${e.message}",1)
                        Log.d("test", "onVerificationFailed: ${e.message}")

                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        progressBar.visibility = GONE
                        btnRegistration.visibility = VISIBLE
                        Log.d("test", "onCodeSent: $verificationId")
                        makeToast("code sent",0)
                        storedVerificationId = verificationId
                        resendToken = token
                        passInfoToNextActivity()
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        }
    }

    private fun passInfoToNextActivity() {
        firstName = Fname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        lastName = Lname.text.toString()


        val correct = inputFieldConformation(
            jobType,
            firstName,
            lastName,
            phoneNo,
            email,
            termsConditionsAcceptance
        )

        if (!correct) return
        else{
            val intent = Intent(this@RegistrationActivity,OTPVerificationRegistrationActivity::class.java)
            intent.putExtra("fName",firstName)
            intent.putExtra("lName",lastName)
            intent.putExtra("phoneNo",phoneNo)
            intent.putExtra("email",email)
            intent.putExtra("jobType",jobType)
            intent.putExtra("termsConditions",termsConditionsAcceptance)
            intent.putExtra("storedVerificationId",storedVerificationId)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }
    }

    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

    private fun inputFieldConformation(
        jobType: String,
        firstName: String,
        lastName: String,
        mobileNo: String,
        email: String,
        termsConditionsAcceptance: String
    ): Boolean {
        if(jobType.isEmpty()){
            makeToast("Please go back and select one job type.", 1)
            return false
        }
        if (firstName.isEmpty()) {
            Fname.error = "Please provide a first-name"
            Fname.requestFocus()
            return false
        }
        if (lastName.isEmpty()) {
            Lname.error = "Please provide a last-name"
            Lname.requestFocus()
            return false
        }
        if (inputPhoneNo.text.toString().isEmpty()) {
            inputPhoneNo.error = "Please provide a mobile no."
            inputPhoneNo.requestFocus()
            return false
        }
        if (inputPhoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(mobileNo).matches()) {
            inputPhoneNo.error = "Please provide valid 10 digit mobile no"
            inputPhoneNo.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            inputEmail.error = "Please provide a email address"
            inputEmail.requestFocus()
            return  false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.error = "Please provide valid email address"
            inputEmail.requestFocus()
            return false
        }
        if (termsConditionsAcceptance != "Accepted"){
            checkBox.error = "Accept terms & conditions"
        }
        return true
    }



    private fun setXmlIDs() {
        Fname = findViewById(R.id.Fname)
        Lname = findViewById(R.id.Lname)
        cpp = findViewById(R.id.cpp)
        inputPhoneNo = findViewById(R.id.inputPhoneNo)
        inputEmail = findViewById(R.id.inputEmail)
//        btnTerms = findViewById(R.id.btnTerms)
//        btnConditions = findViewById(R.id.btnConditions)
        checkBox = findViewById(R.id.checkBox)
        btnRegistration = findViewById(R.id.btnRegistration)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@RegistrationActivity,AskActivity::class.java))
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