package com.amri.emploihunt.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityLoginBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobActivity
import com.amri.emploihunt.model.UserExistOrNotModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.showNoInternetBottomSheet
import com.google.android.material.snackbar.Snackbar
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


class LoginActivity : BaseActivity(),OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    lateinit var binding: ActivityLoginBinding
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val window: Window = this@LoginActivity.window
        val background =ContextCompat.getDrawable(this@LoginActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@LoginActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()
        getUserType(mAuth.uid.toString())



        setOnClickListener()
        binding.cpp.registerCarrierNumberEditText(binding.phoneNo)

        binding.phoneNo.setOnFocusChangeListener { view, b ->
            binding.phoneNo.background = ContextCompat.getDrawable(this, R.drawable.borderr)
        }
    }



    private fun alreadyLogInNextActivity() {

        Log.d(TAG,"usertype: $userType")
        if(userType == "Job Seeker"){
            val intent = Intent(this@LoginActivity, HomeJobActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }
        else if(userType == "Recruiter"){
            val intent = Intent(this@LoginActivity, HomeRecruiterActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
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
        binding.btnLogin.setOnClickListener(this)
        binding.btnRegistration.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnLogin -> {
                sentOtp()

            }
            R.id.btnRegistration -> {
                startActivity(Intent(this@LoginActivity, AskActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

            }
        }
    }

    private fun sentOtp() {
        phoneNo = "+" + binding.cpp.fullNumber.toString().trim{it <= ' '}
        getUserTypeIfNotSignIn(phoneNo) { userType ->

            if (userType.isNotEmpty()) {
                val correct = checkInputData(phoneNo)
                if (correct) {
//
                    Log.d("##", "sentOtp: correct")
                    mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                        override fun onVerificationFailed(e: FirebaseException) {
                            Toast.makeText(
                                this@LoginActivity,
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
                            resendToken = token
                            binding.btnLogin.visibility = VISIBLE
                            hideProgressDialog()
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
                    Log.d("##", "Input data is incorrect")
                    makeToast("Input data is incorrect", 0)
                }
            }
            }
        }

    private fun checkInputData(phoneNo: String): Boolean {

        if (binding.phoneNo.text.toString().isEmpty()) {
            binding.phoneNo.error = "Please provide a mobile no."
            binding.phoneNo.requestFocus()
            return false
        }
        if (binding.phoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.phoneNo.error = "Incorrect Mobile no"
            binding.phoneNo.requestFocus()
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

        val intent = Intent(this@LoginActivity, OTPVerificationLoginActivity::class.java)
//        intent.putExtra("jobType",jobType)
        intent.putExtra("phoneNo",phoneNo)
        intent.putExtra("storedVerificationId",storedVerificationId)
        intent.putExtra("userType",userType)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    private fun getUserTypeIfNotSignIn(mobileNo: String, callback: (String) -> Unit){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.CHECK_USER_EXISTING)
                .addQueryParameter("mobile", mobileNo)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(UserExistOrNotModel::class.java,
                    object : ParsedRequestListener<UserExistOrNotModel> {
                        override fun onResponse(response: UserExistOrNotModel?) {
                            try {

                                callback(response!!.message)

                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                                val snackbar = Snackbar
                                    .make(
                                        binding.layout,
                                        "Sorry! you are not register, Please register first.",
                                        Snackbar.LENGTH_LONG
                                    )
                                    .setAction(
                                        "REGISTER"
                                    )  // If the Undo button
// is pressed, show
// the message using Toast
                                    {
                                        startActivity(Intent(this@LoginActivity, AskActivity::class.java))
                                        overridePendingTransition(
                                            R.anim.slide_in_left,
                                            R.anim.slide_out_right
                                        )
                                    }

                                snackbar.show()
                            }
                            hideProgressDialog()

                        }
                    })
        }else{
            showNoInternetBottomSheet(this,this)
        }

        /* val database = FirebaseDatabase.getInstance()
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
         })*/
    }




    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }



}