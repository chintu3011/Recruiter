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
import com.amri.emploihunt.model.UserExistOrNotModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.showNoInternetBottomSheet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class LoginActivity : BaseActivity(),OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    lateinit var mCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    lateinit var binding: ActivityLoginBinding

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
        binding.cpp.registerCarrierNumberEditText(binding.phoneNo)

        mAuth = FirebaseAuth.getInstance()

        setOnClickListener()


        binding.phoneNo.setOnFocusChangeListener { view, b ->
            binding.phoneNo.background = ContextCompat.getDrawable(this, R.drawable.borderr)
        }
    }
    
    private fun setOnClickListener() {
        binding.btnLogin.setOnClickListener(this)
        binding.btnRegistration.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnLogin -> {
                phoneNo = "+" + binding.cpp.fullNumber.toString().trim{it <= ' '}
                getPhoneNoStatus(phoneNo){
                    if(it.isNotEmpty()) {
                        val correct = checkInputData(phoneNo)
                        if (correct) {
                            navigateToNextActivity()
                        } else {
                            Log.d("##", "Input data is incorrect")
                            binding.phoneNo.error = "Please enter valid phone number"
                        }
                    }
                }
            }
            R.id.btnRegistration -> {
                startActivity(Intent(this@LoginActivity, AskActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

            }
        }
    }



    private fun getPhoneNoStatus(mobileNo: String, callback: (String) -> Unit){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.CHECK_USER_EXISTING)
                .addQueryParameter("mobile", mobileNo)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(UserExistOrNotModel::class.java,
                    object : ParsedRequestListener<UserExistOrNotModel> {
                        override fun onResponse(response: UserExistOrNotModel?) {
                            try {

                                Log.d(TAG, "onResponse: User type = ${response!!.message}")
                                hideProgressDialog()
                                callback(response.message)

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
                                    )
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
        return true
    }

    private fun navigateToNextActivity() {

        binding.phoneNo.text!!.clear()
        val intent = Intent(this@LoginActivity, OTPVerificationLoginActivity::class.java)
        intent.putExtra("phoneNo",phoneNo)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }




    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }



}