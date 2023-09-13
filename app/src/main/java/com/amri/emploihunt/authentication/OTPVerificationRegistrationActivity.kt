package com.amri.emploihunt.authentication


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityOtpVerificationRegistrationBinding
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.store.UserDataRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OTPVerificationRegistrationActivity : BaseActivity(),OnClickListener {

    companion object{
        private const val TAG = "OTPVerificationRegistrationActivity"
    }

    private lateinit var binding: ActivityOtpVerificationRegistrationBinding

    private lateinit var mAuth: FirebaseAuth

    private lateinit var storedVerificationId:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var phoneNo: String
    private lateinit var email: String
    private lateinit var city: String
    private var userType: Int ? = null
    private lateinit var termsConditions:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpVerificationRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)





        mAuth = FirebaseAuth.getInstance()

        setOnClickListener()
        setPinViewSize()

        firstName = intent.getStringExtra("fName").toString()
        phoneNo = intent.getStringExtra("phoneNo").toString()
        email = intent.getStringExtra("email").toString()
        city = intent.getStringExtra("city").toString()
        lastName = intent.getStringExtra("lName").toString()
        userType = intent.getIntExtra("role",-1)
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        termsConditions = intent.getStringExtra("termsConditions").toString()

        binding.txtPhoneNo.text = phoneNo
    }
    private fun setPinViewSize() {
        binding.cardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // This callback will be triggered when the layout has been measured and has dimensions

                // Get the measured width of the layout
                val layoutWidth = binding.inputOTP.width

                if (layoutWidth == 0) {
                    return
                }

                // Remove the listener to avoid multiple callbacks
                binding.inputOTP.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val space = binding.inputOTP.itemSpacing * 6
                // Perform the division
                val division = (layoutWidth - space)/ 6
                binding.inputOTP.itemWidth = division
                binding.inputOTP.itemHeight = division

                // Use the division as needed
                // ...
            }
        })
    }

    private fun setOnClickListener() {
        binding.btnVerify.setOnClickListener(this)
        binding.btnChange.setOnClickListener(this)
        binding.btnResendOtp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                startActivity(Intent(this@OTPVerificationRegistrationActivity,
                    RegistrationActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                finish()
            }
            R.id.btnVerify -> {
                verifyOtp()
            }
            R.id.btnResendOtp -> {
                resendOtp()
            }
        }
    }
    private fun resendOtp() {

        if(phoneNo.isNotEmpty()) {
            Log.d("##", "sentOtp: correct")
            val mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    hideProgressDialog()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        this@OTPVerificationRegistrationActivity,
                        e.localizedMessage,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    hideProgressDialog()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    hideProgressDialog()
                    resendToken = token
                    verifyOtp()
                }
            }
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(
                    phoneNo
                ) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(mCallback)
                .setForceResendingToken(resendToken!!)// OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun verifyOtp() {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId,binding.inputOTP.text.toString())
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val uid = user?.uid
                    Log.d(TAG,"userId:$uid")
                    makeEmptyDataStoreForNewUser{
                        passInfoToNextActivity(uid)
                    }

                } else {
                    makeToast("Registration failed: ${task.exception}",1)
                    Handler(Looper.getMainLooper()).postDelayed({
                        makeToast("Try again",1)
                    },2000)
                }
            }
    }
    private val experienceViewModel: ExperienceViewModel by viewModels()

    private fun makeEmptyDataStoreForNewUser(callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDataRepository = UserDataRepository(this@OTPVerificationRegistrationActivity)
            userDataRepository.emptyDataStore()
            callback()
        }
        experienceViewModel.clearFromLocal()

    }

    private fun passInfoToNextActivity(uid: String?) {
        val intent = Intent(this@OTPVerificationRegistrationActivity, InformationActivity::class.java)
        intent.putExtra("uid",uid)
        intent.putExtra("fName",firstName)
        intent.putExtra("lName",lastName)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("email",email)
        intent.putExtra("city",city)
        intent.putExtra("role",userType)
        intent.putExtra("termsConditions",termsConditions)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }




}