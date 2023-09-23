package com.amri.emploihunt.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityContactUsBinding
import com.amri.emploihunt.model.CommonMessageModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.USER_ID
import com.amri.emploihunt.util.Utils.toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import org.json.JSONObject
import java.util.regex.Pattern

class ContactUsActivity : BaseActivity() {
    lateinit var binding:ActivityContactUsBinding
    lateinit var prefManager:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)


        if (intent.getBooleanExtra("for_block",false)){
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
        else{
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
       /* binding.ivBack.setOnClickListener {
            finish()
        }*/
        binding.cpp.registerCarrierNumberEditText(binding.phoneNo)
        prefManager = PrefManager.prefManager(this)
        binding.btnRegistration.setOnClickListener {
            val firstName = binding.userFName.text.toString().trim()
            val phoneNo = "+" + binding.cpp.fullNumber.toString()
            val email = binding.email.text.toString().trim()
            val message = binding.message.text.toString().trim()
            if (inputFieldConformation(firstName,phoneNo,email,message)){
                callContactUsAPI(firstName,email,phoneNo,message)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    private fun inputFieldConformation(
        firstName: String,
        phoneNo: String,
        email: String,
        message: String,
    ): Boolean {

        if (firstName.isEmpty()) {
            binding.userFName.error = "Please provide a first-name"
            binding.userFName.requestFocus()
            return false
        }

        if (firstName.equals(
                "EmploiHunt", true
            ) || firstName.equals("EmploiHunt", true) || firstName.equals(
                "Emploi", true
            )) {
            toast(resources.getString(R.string.enter_another_first_name))
        }


        if (firstName.length == 2)  {
            toast(resources.getString(R.string.first_last_name_required_min_2_letters))
        }
        if (TextUtils.isDigitsOnly(firstName))  {
            toast(resources.getString(R.string.first_last_name_should_be_character))
        }

        if (Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]").matcher(firstName)
                .find()) {
            toast(resources.getString(R.string.first_last_name_should_be_character))
        }
        if (binding.phoneNo.text.toString().isEmpty()) {
            binding.phoneNo.error = "Please provide a mobile no."
            binding.phoneNo.requestFocus()
            return false
        }
        if (binding.phoneNo.text.toString().length in 11 downTo 9 && !Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.phoneNo.error = "Please provide valid 10 digit mobile no"
            binding.phoneNo.requestFocus()
            return false
        }
//        makeToast(exist.toString(),0)
        if (email.isEmpty()) {
            binding.email.error = "Please provide a email address"
            binding.email.requestFocus()
            return  false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.error = "Please provide valid email address"
            binding.email.requestFocus()
            return false
        }
        if (message.isEmpty()) {
            binding.message.error = "Please provide valid city"
            binding.message.requestFocus()
            return  false
        }

        return true
    }
    private fun callContactUsAPI(name: String, email: String, mobile: String, message: String) {
        showProgressDialog(resources.getString(R.string.please_wait))
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("email", email)
        jsonObject.put("mobile", mobile)
        jsonObject.put("message", message)
        jsonObject.put("iUserId", if (prefManager.getBoolean(IS_LOGIN,false)) prefManager.getInt(USER_ID,0) else 0)
        jsonObject.put("tCreatedAt", System.currentTimeMillis().toString())

        AndroidNetworking.post(NetworkUtils.CONTACT_US)
            .setOkHttpClient(NetworkUtils.okHttpClient)
            .addJSONObjectBody(jsonObject).setPriority(Priority.MEDIUM).build().getAsObject(
                CommonMessageModel::class.java,
                object : ParsedRequestListener<CommonMessageModel> {
                    override fun onResponse(response: CommonMessageModel?) {
                        try {
                            response?.let {
                                hideProgressDialog()
                                //Log.e("#####", "onResponse: $it")
                                toast(it.data.message)
                                binding.userFName.text?.clear()
                                binding.phoneNo.text?.clear()
                                binding.email.text?.clear()
                                binding.message.text?.clear()
                                if(intent.getBooleanExtra("for_block",false)){
                                    finishAffinity()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("#####", "onResponse Exception: ${e.message}")
                        }
                    }

                    override fun onError(anError: ANError?) {
                        hideProgressDialog()
                        anError?.let {
                            Log.e(
                                "#####",
                                "onError: code: ${it.errorCode} & message: ${it.message}"
                            )
                            if (it.errorCode >= 500) {
                                toast(resources.getString(R.string.msg_server_maintenance))
                            }
                        }
                    }
                })
    }
}