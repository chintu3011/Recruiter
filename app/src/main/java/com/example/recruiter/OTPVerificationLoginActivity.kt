package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OTPVerificationLoginActivity : AppCompatActivity(),OnClickListener{

    lateinit var txtPhoneNo : TextView
    lateinit var btnChange: TextView
    lateinit var inputOTP: PinView
    lateinit var btnVerify: Button
    lateinit var cardView: CardView


    private lateinit var mAuth: FirebaseAuth

    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var phoneNo: String
    private var userType:String ?= null

    companion object{
        private const val TAG = "OTPVerificationLoginActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otpverification_login)

        val window: Window = this@OTPVerificationLoginActivity.window
        val background = ContextCompat.getDrawable(this@OTPVerificationLoginActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@OTPVerificationLoginActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@OTPVerificationLoginActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()

        setXmlIDs()
        setOnClickListener()

        phoneNo = intent.getStringExtra("phoneNo").toString()
        userType = intent.getStringExtra("userType").toString()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()

        txtPhoneNo.text = phoneNo
        setPinViewSize()

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
                val space = inputOTP.itemSpacing * 6
                // Perform the division
                val division = (layoutWidth - space)/ 6
                inputOTP.itemWidth = division
                inputOTP.itemHeight = division

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
                startActivity(Intent(this@OTPVerificationLoginActivity,LoginActivity::class.java))
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
        navigateToNextActivity(credential)
    }

    private fun navigateToNextActivity(credential: PhoneAuthCredential) {
        if(userType == "Job Seeker" || userType == "Recruiter") {

            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        val uid = user?.uid
                        Log.d(TAG, "userId: $uid")
                        updateDataStore(uid)
                        when (userType) {
                            "Job Seeker" -> {
                                makeToast("Login successful!", 0)
                                val intent = Intent(
                                    this@OTPVerificationLoginActivity,
                                    HomeJobActivity::class.java
                                )
                                intent.putExtra("phoneNo", txtPhoneNo.text.toString())
                                intent.putExtra("userType", userType)
                                startActivity(intent)
                                overridePendingTransition(R.anim.flip_in, R.anim.flip_out)
                                finish()
                            }

                            "Recruiter" -> {
                                makeToast("Login successful!", 0)
                                val intent = Intent(
                                    this@OTPVerificationLoginActivity,
                                    HomeRecruiterActivity::class.java
                                )
                                intent.putExtra("phoneNo", txtPhoneNo.text.toString())
                                intent.putExtra("userType", userType)
                                startActivity(intent)
                                overridePendingTransition(R.anim.flip_in, R.anim.flip_out)
                                finish()
                            }
                            else -> {
                                Log.d(TAG, "navigateToNextActivity :: User not found.")
                                makeToast("User not found.", 0)
                            }
                        }

                    } else {
                        Log.d(TAG, "Login failed: ${task.exception}")
                        makeToast("Login failed: ${task.exception}", 0)
                        Handler(Looper.getMainLooper()).postDelayed({
                            makeToast("Try again", 0)
                        }, 1000)
                    }
                }
        }
        else{
            Log.d(TAG, "Login failed: User Not Found.")
            makeToast("Login failed: User Not Found.", 0)
            Handler(Looper.getMainLooper()).postDelayed({
                makeToast("Try again with another phone number", 0)
            }, 1000)
        }

    }

    private fun updateDataStore(uid: String?) {

        if(userType == "Job Seeker"){
            FirebaseDatabase.getInstance().getReference("Users").child("Job Seeker")
                .child(uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userData = snapshot.getValue(UsersJobSeeker::class.java)
                            if (userData != null){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val jobSeekerProfileInfo = JobSeekerProfileInfo(this@OTPVerificationLoginActivity)
                                    jobSeekerProfileInfo.storeBasicProfileData(
                                        userData.userFName,
                                        userData.userLName,
                                        userData.userPhoneNumber,
                                        userData.userEmailId,
                                        userData.userTagLine,
                                        userData.userCurrentCompany
                                    )
                                    jobSeekerProfileInfo.storeAboutData(
                                        userData.userBio,
                                        userData.userQualification
                                    )
                                    jobSeekerProfileInfo.storeExperienceData(
                                        userData.userExperienceState,
                                        userData.userDesignation,
                                        userData.userPrevCompany,
                                        userData.userPrevJobDuration
                                    )
                                    jobSeekerProfileInfo.storeResumeData(
                                        userData.userResumeFileName,
                                        userData.userResumeUri
                                    )
                                    jobSeekerProfileInfo.storeJobPreferenceData(
                                        userData.userPerfJobTitle,
                                        userData.userExpectedSalary,
                                        userData.userPrefJobLocation,
                                        userData.userWorkingMode
                                    )
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        makeToast("User Not Found",0)
                    }

                })
        }
        if(userType == "Recruiter"){
            FirebaseDatabase.getInstance().getReference("Users").child("Recruiter")
                .child(uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userData = snapshot.getValue(UsersRecruiter::class.java)
                            if (userData != null){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val recruiterProfileInfo = RecruiterProfileInfo(this@OTPVerificationLoginActivity)
                                    recruiterProfileInfo.storeBasicProfileData(
                                        userData.userFName,
                                        userData.userLName,
                                        userData.userPhoneNumber,
                                        userData.userEmailId,
                                        userData.userTagLine,
                                        userData.userCurrentCompany
                                    )
                                    recruiterProfileInfo.storeAboutData(
                                        userData.userJobTitle,
                                        userData.userSalary,
                                        userData.userJobLocation,
                                        userData.userBio,
                                        userData.userDesignation,
                                        userData.userWorkingMode
                                    )
                                    recruiterProfileInfo.storeProfileImg(
                                        userData.userProfileImg
                                    )
                                    recruiterProfileInfo.storeProfileBannerImg(
                                        userData.userProfileBannerImg
                                    )
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        makeToast("User Not Found",0)
                    }
                })
        }
    }

    private fun setXmlIDs() {
        txtPhoneNo = findViewById(R.id.txtPhoneNo)
        btnChange = findViewById(R.id.btnChange)
        inputOTP = findViewById(R.id.inputOTP)
        btnVerify = findViewById(R.id.btnVerify)
        cardView = findViewById(R.id.cardView)
    }
    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }
}
