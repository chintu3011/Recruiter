package com.example.recruiter.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.recruiter.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit


class RegistrationActivity : AppCompatActivity() ,OnClickListener{

    private lateinit var Fname:EditText
    private lateinit var Lname:EditText
    private lateinit var cpp: CountryCodePicker
    private lateinit var inputPhoneNo: EditText
    private lateinit var inputEmail: EditText
    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    //    lateinit var btnTerms:TextView
//    lateinit var btnConditions:TextView
    private lateinit var checkBox:CheckBox
    private lateinit var btnRegistration: Button
    private lateinit var progressBar:ProgressBar



    private lateinit var mAuth: FirebaseAuth

    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var phoneNo: String
    private lateinit var email: String
    private lateinit var userType: String
    private lateinit var termsConditionsAcceptance:String


    private lateinit var decorView: View
    private lateinit var copyCredential : PhoneAuthCredential

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        val window: Window = this@RegistrationActivity.window
        val background =ContextCompat.getDrawable(this@RegistrationActivity,
            R.drawable.status_bar_color
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@RegistrationActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@RegistrationActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

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
        FirebaseApp.initializeApp(this)
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.btnRegistration -> {
                registerUser()
            }
        }
    }
    private fun registerUser() {

        firstName = Fname.text.toString()
        lastName = Lname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        userType = intent.getStringExtra("userType").toString()
        termsConditionsAcceptance =  if (checkBox.isChecked) {
            "Accepted"
        }
        else{
            "Not Accepted"
        }
        val correct = inputFieldConformation(userType,firstName,lastName,phoneNo,email,termsConditionsAcceptance)

        isPhNoRegBefore(phoneNo,exist)
        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            btnRegistration.visibility = GONE
            Log.d("test", "registerUser: $phoneNo")
//            val options = PhoneAuthOptions.newBuilder(mAuth)
//                .setPhoneNumber(phoneNo)
//                .setTimeout(60L, TimeUnit.SECONDS)
//                .setActivity(this)
//                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        copyCredential = credential
//                        makeToast("onVerificationCompleted:$credential",1)
//                        passInfoToNextActivity()
//                    }
//                    override fun onVerificationFailed(e: FirebaseException) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        makeToast("Verification failed: ${e.message}",1)
//                        Log.d("test", "onVerificationFailed: ${e.message}")
//                    }
//                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                        progressBar.visibility = GONE
//                        btnRegistration.visibility = VISIBLE
//                        Log.d("test", "onCodeSent: $verificationId")
//                        makeToast("code sent",0)
//                        storedVerificationId = verificationId
//                        resendToken = token
//                        passInfoToNextActivity()
//                    }
//                })
//                .build()
//            PhoneAuthProvider.verifyPhoneNumber(options)
            mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
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

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                        progressBar.visibility = GONE
                        btnRegistration.visibility = VISIBLE
                        Log.d("test", "onCodeSent: $verificationId")
                        makeToast("code sent",0)
                        storedVerificationId = verificationId
                        resendToken = token
                        passInfoToNextActivity()
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
        }

    private fun passInfoToNextActivity() {
        firstName = Fname.text.toString()
        phoneNo = "+" + cpp.fullNumber.toString()
        email = inputEmail.text.toString()
        lastName = Lname.text.toString()

        isPhNoRegBefore(phoneNo,exist)
        val correct = inputFieldConformation(
            userType,
            firstName,
            lastName,
            phoneNo,
            email,
            termsConditionsAcceptance
        )

        if (!correct) return
        else{
            val intent = Intent(this@RegistrationActivity, OTPVerificationRegistrationActivity::class.java)
            intent.putExtra("fName",firstName)
            intent.putExtra("lName",lastName)
            intent.putExtra("phoneNo",phoneNo)
            intent.putExtra("email",email)
            intent.putExtra("userType",userType)
            intent.putExtra("termsConditions",termsConditionsAcceptance)
            intent.putExtra("storedVerificationId",storedVerificationId)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }
    var exist:Boolean=false
    private fun inputFieldConformation(
        userType: String,
        firstName: String,
        lastName: String,
        phoneNo: String,
        email: String,
        termsConditionsAcceptance: String
    ): Boolean {
        if(userType.isEmpty()){
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
        if (inputPhoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            inputPhoneNo.error = "Please provide valid 10 digit mobile no"
            inputPhoneNo.requestFocus()
            return false
        }
//        makeToast(exist.toString(),0)
        if(exist){
            inputPhoneNo.error = "This phone number is already exist"
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
            return false
        }
        return true
    }

    private fun isPhNoRegBefore(phoneNo: String, exist: Boolean){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val userMobileNo = userSnapshot.child("phoneNo").getValue(String::class.java)
                        if (userMobileNo == phoneNo) {
                            this@RegistrationActivity.exist = true
                            break
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                makeToast("error: ${error.message}",0)
            }
        })
    }


    private fun setXmlIDs() {
        Fname = findViewById(R.id.userFName)
        Lname = findViewById(R.id.userLName)
        cpp = findViewById(R.id.cpp)
        inputPhoneNo = findViewById(R.id.inputPhoneNo)
        inputEmail = findViewById(R.id.email)
//        btnTerms = findViewById(R.id.btnTerms)
//        btnConditions = findViewById(R.id.btnConditions)
        checkBox = findViewById(R.id.checkBox)
        btnRegistration = findViewById(R.id.btnRegistration)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@RegistrationActivity, AskActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}