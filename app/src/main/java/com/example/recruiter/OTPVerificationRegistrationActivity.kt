package com.example.recruiter

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.chaos.view.PinView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPVerificationRegistrationActivity : AppCompatActivity(),OnClickListener {

    lateinit var txtPhoneNo : TextView
    lateinit var btnChange: TextView
    lateinit var inputOTP: PinView
    lateinit var btnVerify: Button
    lateinit var cardView: CardView

    private lateinit var mAuth: FirebaseAuth

    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var phoneNo: String
    lateinit var email: String
    lateinit var jobType: String
    lateinit var termsConditions:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification_registration)

        val window: Window = this@OTPVerificationRegistrationActivity.window
        val background = ContextCompat.getDrawable(this@OTPVerificationRegistrationActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@OTPVerificationRegistrationActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@OTPVerificationRegistrationActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()

        setXmlIDs()
        setOnClickListener()


        setPinViewSize()

        firstName = intent.getStringExtra("fName").toString()
        phoneNo = intent.getStringExtra("phoneNo").toString()
        email = intent.getStringExtra("email").toString()
        lastName = intent.getStringExtra("lName").toString()
        jobType = intent.getStringExtra("jobType").toString()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        termsConditions = intent.getStringExtra("termsConditions").toString()

        txtPhoneNo.text = phoneNo



    }
    private fun setPinViewSize() {
        cardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // This callback will be triggered when the layout has been measured and has dimensions

                // Get the measured width of the layout
                val layoutWidth = inputOTP.width

                // If the layout width is 0, it means it hasn't been measured yet, so return
                if (layoutWidth == 0) {
                    return
                }

                // Remove the listener to avoid multiple callbacks
                inputOTP.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Perform the division
                val division = layoutWidth / 6

                inputOTP.itemWidth = division
                inputOTP.itemHeight = division

                // Use the division as needed
                // ...
            }
        })
    }

    private fun setOnClickListener() {
        btnVerify.setOnClickListener(this)
        btnChange.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                startActivity(Intent(this@OTPVerificationRegistrationActivity,RegistrationActivity::class.java))
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
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    makeToast("Registration successful!",1)
                    passInfoToNextActivity();
                    finish()
                } else {
                    makeToast("Registration failed: ${task.exception}",1)
                    Handler(Looper.getMainLooper()).postDelayed({
                        makeToast("Try again",1)
                    },2000)
                }
            }
    }
    private fun setXmlIDs() {
        txtPhoneNo = findViewById(R.id.txtPhoneNo)
        btnChange = findViewById(R.id.btnChange)
        inputOTP = findViewById(R.id.inputOTP)
        btnVerify = findViewById(R.id.btnVerify)
        cardView = findViewById(R.id.cardView)
    }

    private fun passInfoToNextActivity() {
        val intent = Intent(this@OTPVerificationRegistrationActivity,InformationActivity::class.java)
        intent.putExtra("fName",firstName)
        intent.putExtra("lName",lastName)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("email",email)
        intent.putExtra("jobType",jobType)
        intent.putExtra("termsConditions",termsConditions)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
    }

    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }


}