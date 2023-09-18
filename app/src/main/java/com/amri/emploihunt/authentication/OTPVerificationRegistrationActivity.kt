package com.amri.emploihunt.authentication


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.viewModels
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityOtpVerificationRegistrationBinding
import com.amri.emploihunt.databinding.BottomsheetPhoneChangeBinding
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.UserDataRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    val timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            //binding.tvTimer.text = resources.getString(R.string.app_name) + millisUntilFinished / 1000
            binding.tvTimer.text =
                resources.getString(R.string.otp_timer, millisUntilFinished / 1000L)
        }

        override fun onFinish() {
            binding.tvTimer.text = ""
            binding.tvTimer.visibility = View.GONE
            binding.layResend.visibility = View.VISIBLE
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpVerificationRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        phoneNo = intent.getStringExtra("phoneNo").toString()
        mAuth = FirebaseAuth.getInstance()
        sentOtp(phoneNo)

        setOnClickListener()
        setPinViewSize()


    }

    private fun sentOtp(phone: String) {
        firstName = intent.getStringExtra("fName").toString()
        binding.txtPhoneNo.text = phone
        email = intent.getStringExtra("email").toString()
        city = intent.getStringExtra("city").toString()
        lastName = intent.getStringExtra("lName").toString()
        userType = intent.getIntExtra("role",-1)
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        termsConditions = intent.getStringExtra("termsConditions").toString()


        showProgressDialog("Please wait...")
        Log.d("##", "sentOtp: correct")

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone)
            .setTimeout(15L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        hideProgressDialog()
                        makeToast("verification completed",0)
                        signInWithPhoneAuthCredential(credential)
                    }
                    override fun onVerificationFailed(e: FirebaseException) {

                        Toast.makeText(
                            this@OTPVerificationRegistrationActivity,
                            e.localizedMessage,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        hideProgressDialog()
                        finish()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        timer.start()
                        storedVerificationId = verificationId
                        resendToken = token
                        makeToast("code sent to :$phone",0)
                        hideProgressDialog()
                    }
                }
            )
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun setOnClickListener() {
        binding.btnVerify.setOnClickListener(this)
        binding.btnChange.setOnClickListener(this)
        binding.btnResendOtp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                val bottomSheetDialog = BottomSheetDialog(this@OTPVerificationRegistrationActivity)
                val dialogBinding = BottomsheetPhoneChangeBinding.inflate(layoutInflater)
                bottomSheetDialog.show()
                bottomSheetDialog.setContentView(dialogBinding.root)

                dialogBinding.cpp.registerCarrierNumberEditText(dialogBinding.phoneNo)
                dialogBinding.cpp.setTalkBackTextProvider { country ->
                    if (country != null) {

                        country.phoneCode
                    } else {
                        ""
                    }
                }
                dialogBinding.cpp.fullNumber = phoneNo
                dialogBinding.btnEdit.setOnClickListener {
                    when {
                        dialogBinding.phoneNo.text?.trim()?.isEmpty()!! -> {
                            Toast.makeText(this@OTPVerificationRegistrationActivity,"Enter Mobile No",Toast.LENGTH_SHORT).show()
                        }
                        dialogBinding.phoneNo.text?.trim()?.length!! < 10 -> {
                            Toast.makeText(this@OTPVerificationRegistrationActivity,"Enter Valid Mobile No",Toast.LENGTH_SHORT).show()
                        }
                        "+${dialogBinding.cpp.fullNumber?.toString()!!.trim()}" == phoneNo -> {
                            Log.d("####", "onClick: ${dialogBinding.cpp.fullNumber?.toString()!!.trim()} $phoneNo")
                            Toast.makeText(this@OTPVerificationRegistrationActivity,"Please change existing change mobile number",Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Log.d("####", "else: ${dialogBinding.cpp.fullNumber?.toString()!!.trim()} $phoneNo")
                            bottomSheetDialog.dismiss()
                            timer.cancel()
                            sentOtp("+${dialogBinding.cpp.fullNumber?.toString()!!.trim()}")
                            //sendVerificationCode(fetchedMobNo)

                        }
                    }
                }
                dialogBinding.btnCancel.setOnClickListener { bottomSheetDialog.dismiss() }
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
            showProgressDialog("Please wait...")
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNo)
                .setTimeout(15L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            hideProgressDialog()
                            makeToast("verification completed",0)
                            signInWithPhoneAuthCredential(credential)
                        }
                        override fun onVerificationFailed(e: FirebaseException) {
                            Toast.makeText(
                                this@OTPVerificationRegistrationActivity,
                                e.localizedMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            hideProgressDialog()
                            finish()
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            hideProgressDialog()
                            storedVerificationId = verificationId
                            resendToken = token
                            makeToast("code sent to :$phoneNo",0)
                            binding.layResend.visibility = View.GONE
                            binding.tvTimer.visibility = View.VISIBLE
                            timer.start()

                        }
                    }
                )
                .setForceResendingToken(resendToken)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun verifyOtp() {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId,binding.inputOTP.text.toString())
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        showProgressDialog("Please wait")
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgressDialog()
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val uid = user?.uid
                    Log.d(TAG,"userId:$uid")
                    makeEmptyDataStoreForNewUser{
                        passInfoToNextActivity(uid,user?.phoneNumber)
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

    private fun passInfoToNextActivity(uid: String?, phoneNumber: String?) {
        val intent = Intent(this@OTPVerificationRegistrationActivity, InformationActivity::class.java)
        intent.putExtra("uid",uid)
        intent.putExtra("fName",firstName)
        intent.putExtra("lName",lastName)
        intent.putExtra("phoneNo",phoneNumber)
        intent.putExtra("email",email)
        intent.putExtra("city",city)
        intent.putExtra("role",userType)
        intent.putExtra("termsConditions",termsConditions)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
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

}