package com.amri.emploihunt.authentication

import android.content.Intent
import android.content.SharedPreferences
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityOtpverificationLoginBinding
import com.amri.emploihunt.databinding.BottomsheetPhoneChangeBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.SignInCheckModel
import com.amri.emploihunt.model.UserExpModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.UserDataRepository
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
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class OTPVerificationLoginActivity : BaseActivity(),OnClickListener{


    private lateinit var binding:ActivityOtpverificationLoginBinding

    private val experienceViewModel: ExperienceViewModel by viewModels()
    private lateinit var mAuth: FirebaseAuth

    lateinit var phoneNo: String
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    lateinit var prefManager: SharedPreferences

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
    companion object{
        private const val TAG = "OTPVerificationLoginActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpverificationLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = prefManager(this)

        mAuth = FirebaseAuth.getInstance()
        phoneNo = intent.getStringExtra("phoneNo").toString()
        sentOtp(phoneNo)
        setOnClickListener()


        setPinViewSize()


    }

    private fun sentOtp(phoneNo: String) {


        binding.txtPhoneNo.text = phoneNo
        showProgressDialog("Please wait...")
        Log.d("##$", "sentOtp: correct")

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNo)
            .setTimeout(15L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        mAuth.signInWithCredential(credential)
                            .addOnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    val uid = user?.uid
                                    Log.d(TAG, "userId: $uid")
                                    makeEmptyDataStoreForNewUser {
                                        callUSerLogin(uid, user?.phoneNumber)
                                    }


                                } else {
                                    Log.d(TAG, "Login failed: ${task.exception}")
                                    makeToast("Login failed: ${task.exception}", 0)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        makeToast("Try again", 0)
                                    }, 100)
                                }
                            }
                    }
                    override fun onVerificationFailed(e: FirebaseException) {

                        Toast.makeText(
                            this@OTPVerificationLoginActivity,
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
                        makeToast("code sent to :${this@OTPVerificationLoginActivity.phoneNo}",0)
                        hideProgressDialog()
                    }
                }
            )
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }


    private fun makeEmptyDataStoreForNewUser(callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDataRepository = UserDataRepository(this@OTPVerificationLoginActivity)
            userDataRepository.emptyDataStore()
            callback()
        }
        /*experienceViewModel.clearFromLocal()*/

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
        binding.btnResendOtp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChange -> {
                val bottomSheetDialog = BottomSheetDialog(this@OTPVerificationLoginActivity)
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
                            Toast.makeText(this@OTPVerificationLoginActivity,"Enter Mobile No",Toast.LENGTH_SHORT).show()
                        }
                        dialogBinding.phoneNo.text?.trim()?.length!! < 10 -> {
                            Toast.makeText(this@OTPVerificationLoginActivity,"Enter Valid Mobile No",Toast.LENGTH_SHORT).show()
                        }
                        "+${dialogBinding.cpp.fullNumber?.toString()!!.trim()}" == phoneNo -> {
                            Log.d("####", "onClick: ${dialogBinding.cpp.fullNumber?.toString()!!.trim()} $phoneNo")
                        Toast.makeText(this@OTPVerificationLoginActivity,"Please change existing change mobile number",Toast.LENGTH_SHORT).show()
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
            timer.start()
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
                            mAuth.signInWithCredential(credential)
                                .addOnCompleteListener{ task ->
                                    if (task.isSuccessful) {
                                        val user = task.result?.user
                                        val uid = user?.uid
                                        Log.d(TAG, "userId: $uid")
                                        makeEmptyDataStoreForNewUser {
                                            callUSerLogin(uid, user?.phoneNumber)
                                        }


                                    } else {
                                        Log.d(TAG, "Login failed: ${task.exception}")
                                        makeToast("Login failed: ${task.exception}", 0)
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            makeToast("Try again", 0)
                                        }, 100)
                                    }
                                }
                        }
                        override fun onVerificationFailed(e: FirebaseException) {
                            Toast.makeText(
                                this@OTPVerificationLoginActivity,
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
                            storedVerificationId = verificationId
                            resendToken = token
                            makeToast("code sent to :$phoneNo",0)
                            binding.layResend.visibility = View.GONE
                            binding.tvTimer.visibility = View.VISIBLE
                            timer.start()
                            hideProgressDialog()
                        }
                    }
                )
                .setForceResendingToken(resendToken)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
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
                    makeEmptyDataStoreForNewUser {
                        callUSerLogin(uid, user?.phoneNumber)
                    }


                } else {
                    hideProgressDialog()
                    Log.d(TAG, "Login failed: ${task.exception}")
                    makeToast("Login failed: ${task.exception}", 0)
                    Handler(Looper.getMainLooper()).postDelayed({
                        makeToast("Try again", 0)
                    }, 1000)
                }
            }
    }


    private fun callUSerLogin(uid: String?, phoneNumber: String?) {

        if (Utils.isNetworkAvailable(this)){
            val jsonObject = JSONObject()
            jsonObject.put(MOB_NO, phoneNumber)
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
                                                val userDataRepository =
                                                    UserDataRepository(this@OTPVerificationLoginActivity)
                                                userDataRepository.storeBasicInfo(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCity
                                                )
                                                userDataRepository.storeAboutData(
                                                    response.data.user.tBio,
                                                )
                                                userDataRepository.storeCurrentPositionData(
                                                    response.data.user.vCurrentCompany,
                                                    response.data.user.vDesignation,
                                                    response.data.user.vJobLocation,
                                                    ""
                                                )
                                                userDataRepository.storeQualificationData(
                                                    response.data.user.vQualification
                                                )
                                                userDataRepository.storeJobPreferenceData(
                                                    response.data.user.vPreferJobTitle,
                                                    response.data.user.vPreferCity,
                                                    response.data.user.vWorkingMode
                                                )
                                                userDataRepository.storeProfileImg(
                                                    response.data.user.tProfileUrl
                                                )
                                                userDataRepository.storeResumeData(
                                                    response.data.user.tResumeUrl
                                                )

                                                Log.d(
                                                    TAG,
                                                    "onResponse: Recieved Data : ${response.data.user}"
                                                )
                                                prefManager[IS_LOGIN] = true
                                                prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                                prefManager[ROLE] = response.data.user.iRole
                                                prefManager[USER_ID] = response.data.user.id
                                                prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                                prefManager[IS_BLOCKED] = response.data.user.isBlock

                                                getExperienceData{
                                                    val intent = Intent(
                                                        this@OTPVerificationLoginActivity,
                                                        HomeJobSeekerActivity::class.java
                                                    )
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
                                            }
                                            RECRUITER -> {
                                                val userDataRepository = UserDataRepository(this@OTPVerificationLoginActivity)
                                                userDataRepository.storeBasicInfo(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCity
                                                )
                                                userDataRepository.storeAboutData(
                                                    response.data.user.tBio,
                                                )
                                                userDataRepository.storeQualificationData(
                                                    response.data.user.vQualification
                                                )
                                                userDataRepository.storeCurrentPositionData(
                                                    response.data.user.vCurrentCompany,
                                                    response.data.user.vDesignation,
                                                    response.data.user.vJobLocation,
                                                    response.data.user.vWorkingMode
                                                )
                                                userDataRepository.storeProfileImg(
                                                    response.data.user.tProfileUrl
                                                )
                                                Log.d(
                                                    TAG,
                                                    "onResponse: Recieved Data : ${response.data.user}"
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

    private fun getExperienceData(callback: () -> Unit)    {
        if (Utils.isNetworkAvailable(this)) {
            AndroidNetworking.get(NetworkUtils.GET_ALL_EXPERIENCE)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    UserExpModel::class.java,
                    object : ParsedRequestListener<UserExpModel> {
                        override fun onResponse(response: UserExpModel?) {
                            try {
                                if (response != null) {
                                    Log.d(
                                        TAG,
                                        "onResponse: Experience data received: ${response.data}"
                                    )
                                    experienceViewModel.writeToLocal(response.data.toList())
                                        .invokeOnCompletion {
                                            Log.d(
                                                TAG,
                                                "experienceInfoDialogView: experienceList is updated in datastore"
                                            )
                                            callback()
                                        }
                                }

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

                    }
                )
        }
        else{
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    
}
