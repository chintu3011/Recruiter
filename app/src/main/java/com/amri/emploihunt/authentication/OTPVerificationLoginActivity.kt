package com.amri.emploihunt.authentication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityOtpverificationLoginBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.SignInCheckModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.DEVICE_ID
import com.amri.emploihunt.util.DEVICE_NAME
import com.amri.emploihunt.util.FCM_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.IS_BLOCKED
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.JOB_SEEKER
import com.amri.emploihunt.util.MOB_NO
import com.amri.emploihunt.util.OS_VERSION
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.RECRUITER
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.USER_ID
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class OTPVerificationLoginActivity : BaseActivity(),OnClickListener{


    private lateinit var binding:ActivityOtpverificationLoginBinding


    private lateinit var mAuth: FirebaseAuth

    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var prefManager: SharedPreferences
    lateinit var phoneNo: String

    companion object{
        private const val TAG = "OTPVerificationLoginActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpverificationLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = prefManager(this)



        mAuth = FirebaseAuth.getInstance()

        setOnClickListener()

        phoneNo = intent.getStringExtra("phoneNo").toString()
        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()

        binding.txtPhoneNo.text = phoneNo
        setPinViewSize()

    }

    private fun setPinViewSize() {
        binding.cardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // This callback will be triggered when the layout has been measured and has dimensions

                // Get the measured width of the layout
                val layoutWidth = binding.inputOTP.width

                // If the layout width is 0, it means it hasn't been measured yet, so return
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

            }
        })
    }

    private fun setOnClickListener() {
        binding.btnVerify.setOnClickListener(this)
        binding.btnChange.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                startActivity(Intent(this@OTPVerificationLoginActivity, LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                finish()
            }
            R.id.btnVerify -> {
                verifyOtp()
            }
        }
    }
    private fun verifyOtp() {
        showProgressDialog("Please wait....")
        val credential = PhoneAuthProvider.getCredential(storedVerificationId,binding.inputOTP.text.toString())

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val uid = user?.uid
                    Log.d(TAG, "userId: $uid")
                    /*updateDataStore(uid)*/
                    callUSerLogin(uid)
                    /*when (userType) {
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
                    }*/

                } else {
                    Log.d(TAG, "Login failed: ${task.exception}")
                    makeToast("Login failed: ${task.exception}", 0)
                    Handler(Looper.getMainLooper()).postDelayed({
                        makeToast("Try again", 0)
                    }, 1000)
                }
            }

    }
    /*private fun navigateToNextActivity(credential: PhoneAuthCredential) {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        val uid = user?.uid
                        Log.d(TAG, "userId: $uid")
                        *//*updateDataStore(uid)*//*
                        callUSerLogin(uid)
                        *//*when (userType) {
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
                        }*//*

                    } else {
                        Log.d(TAG, "Login failed: ${task.exception}")
                        makeToast("Login failed: ${task.exception}", 0)
                        Handler(Looper.getMainLooper()).postDelayed({
                            makeToast("Try again", 0)
                        }, 1000)
                    }
                }
    }*/

    private fun callUSerLogin(uid: String?) {

        if (Utils.isNetworkAvailable(this)){
            val jsonObject = JSONObject()
            jsonObject.put(MOB_NO, phoneNo)
            //jsonObject.put(FCM_TOKEN, prefManager[FCM_TOKEN, ""])

            jsonObject.put(DEVICE_ID, prefManager.get(DEVICE_ID,""))
            jsonObject.put(OS_VERSION, prefManager.get(OS_VERSION,""))
            jsonObject.put(FCM_TOKEN, prefManager.get(FCM_TOKEN,""))
            jsonObject.put(DEVICE_NAME, prefManager.get(DEVICE_NAME,""))
            jsonObject.put(FIREBASE_ID,uid)


            AndroidNetworking.post(NetworkUtils.LOGIN)
                .setOkHttpClient(NetworkUtils.okHttpClient).addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    SignInCheckModel::class.java,
                    object : ParsedRequestListener<SignInCheckModel> {
                        override fun onResponse(response: SignInCheckModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()
                                    CoroutineScope(Dispatchers.IO).launch {
                                        when (response.data.user.iRole) {
                                            JOB_SEEKER -> {
                                                val jobSeekerProfileInfo = JobSeekerProfileInfo(this@OTPVerificationLoginActivity)
                                                jobSeekerProfileInfo.storeBasicProfileData(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCurrentCompany
                                                )
                                                jobSeekerProfileInfo.storeAboutData(
                                                    response.data.user.tBio,
                                                    /*response.data.user.vQualification*/
                                                )
                                                jobSeekerProfileInfo.storeQualificationData(
                                                    response.data.user.vQualification
                                                )
                                                /*jobSeekerProfileInfo.storeExperienceData(
                                                    "",
                                                    response.data.user.vDesignation,
                                                    "",
                                                    ""
                                                )*/
                                                jobSeekerProfileInfo.storeResumeData(
                                                    "",
                                                    response.data.user.tResumeUrl,
                                                )
                                                jobSeekerProfileInfo.storeJobPreferenceData(
                                                    "",
                                                    "",
                                                    response.data.user.vCity,
                                                )
                                                prefManager[IS_LOGIN] = true
                                                prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                                prefManager[ROLE] = response.data.user.iRole
                                                prefManager[USER_ID] = response.data.user.id
                                                prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                                prefManager[IS_BLOCKED] = response.data.user.isBlock
                                                val intent = Intent(
                                                    this@OTPVerificationLoginActivity,
                                                    HomeJobSeekerActivity::class.java
                                                )
                                                /*intent.putExtra("phoneNo", binding.txtPhoneNo.text.toString())*/
                                                intent.putExtra("userId",response.data.user.vFirebaseId)
                                                intent.putExtra("role",response.data.user.iRole)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startActivity(intent)
                                                overridePendingTransition(
                                                    R.anim.flip_in,
                                                    R.anim.flip_out
                                                )


                                                finish()

                                            }
                                            RECRUITER -> {
                                                val recruiterProfileInfo = RecruiterProfileInfo(this@OTPVerificationLoginActivity)
                                                recruiterProfileInfo.storeBasicProfileData(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCurrentCompany
                                                )
                                                recruiterProfileInfo.storeAboutData(
                                                    response.data.user.vDesignation,
                                                    "",
                                                    "",
                                                    response.data.user.tBio,
                                                    response.data.user.vDesignation,
                                                    response.data.user.vWorkingMode
                                                )
                                                recruiterProfileInfo.storeProfileImg(
                                                    response.data.user.tProfileUrl
                                                )
                                                recruiterProfileInfo.storeProfileBannerImg(
                                                    ""
                                                )
                                                val intent = Intent(
                                                    this@OTPVerificationLoginActivity,
                                                    HomeRecruiterActivity::class.java
                                                )
                                                prefManager[IS_LOGIN] = true
                                                prefManager[USER_ID] = response.data.user.id
                                                prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                                prefManager[ROLE] = response.data.user.iRole
                                                prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                                prefManager[IS_BLOCKED] = response.data.user.isBlock
                                                /*intent.putExtra("phoneNo", binding.txtPhoneNo.text.toString())*/
                                                intent.putExtra("userId",response.data.user.vFirebaseId)
                                                intent.putExtra("role", response.data.user.iRole)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startActivity(intent)
                                                overridePendingTransition(
                                                    R.anim.flip_in,
                                                    R.anim.flip_out
                                                )


                                                finish()
                                            }
                                            else -> {

                                                Log.d(TAG, "onResponse: incorrect user type : ${response.data.user.iRole}")


                                            }
                                        }
                                    }
                                    hideProgressDialog()
                                }
                                //hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                                hideProgressDialog()
                            }
                        }

                        override fun onError(anError: ANError?) {
                            try {

                                anError?.let {
                                    Log.e(
                                        "#####",
                                        "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )
                                    /** errorCode == 404 means User number is not registered or New user */
                                    hideProgressDialog()
                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onError: ${e.message}")
                            }
                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    /*private fun updateDataStore(uid: String?) {

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
    }*/
    
}
