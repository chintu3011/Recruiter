package com.amri.emploihunt.appIntroduction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.amri.emploihunt.BuildConfig
import com.amri.emploihunt.R
import com.amri.emploihunt.authentication.LoginsignupActivity
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.UpdateBottomSheetBinding

import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.UpdateAppModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.settings.ContactUsActivity
import com.amri.emploihunt.util.DEVICE_ID
import com.amri.emploihunt.util.DEVICE_NAME
import com.amri.emploihunt.util.DEVICE_TYPE
import com.amri.emploihunt.util.IS_BLOCKED
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.OS_VERSION
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.USER_ID
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialog


class SplashActivity : BaseActivity() {
    lateinit var decorView: View
    lateinit var activity: Activity
    private lateinit var preferencesForIntroScreen: SharedPreferences
    private lateinit var prefManager: SharedPreferences
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        prefManager = prefManager(this)
        val window: Window = this@SplashActivity.window
        val background = ContextCompat.getDrawable(this@SplashActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@SplashActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@SplashActivity,android.R.color.white)




        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        prefManager.set(DEVICE_ID,deviceId)
        prefManager.set(OS_VERSION,Build.VERSION.RELEASE)
        prefManager.set(DEVICE_NAME,Build.MODEL)
        prefManager.set(DEVICE_TYPE,"0")


        callUpdateAppAPI()

    }
    private fun setPreferencesForIntroScreen() {
        preferencesForIntroScreen = getSharedPreferences("IntroductionScreen",Context.MODE_PRIVATE)
        val hashShownIntro = preferencesForIntroScreen.getBoolean("isFirstTime",true)

        if (hashShownIntro){
            Handler(Looper.getMainLooper()).postDelayed({

                val editor = preferencesForIntroScreen.edit()
                editor.putBoolean("isFirstTime",false)
                editor.apply()
                val intent = Intent(this@SplashActivity, IntroductionActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_down)
                finish()
            },4000)
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                if (prefManager.get(IS_LOGIN,false)!!){

                    if (prefManager.get(ROLE,0) == 1){
                        val intent = Intent(this@SplashActivity, HomeRecruiterActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        finish()
                    }else{
                        val intent = Intent(this@SplashActivity, HomeJobSeekerActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        finish()
                    }

                }else{
                    val intent = Intent(this@SplashActivity, LoginsignupActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                }

            },4000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
    private fun callUpdateAppAPI() {
        var iUserId  = prefManager.getInt(USER_ID,0)
        if (Utils.isNetworkAvailable(this)) {
            AndroidNetworking.get(NetworkUtils.GET_LATEST_APP_VERSION_CODE)
                .addQueryParameter("iUserId",iUserId.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    UpdateAppModel::class.java,
                    object : ParsedRequestListener<UpdateAppModel> {
                        override fun onResponse(response: UpdateAppModel?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.e("#####", "onResponse: $it")
                                    val latestAppVersionCode = it.data.latestAppVersionCode
                                    val isForceUpdate = it.data.isForceUpdate
                                    val updateMsg = it.data.tMessage

                                    val currentVersionCode = BuildConfig.VERSION_CODE
                                    prefManager[IS_BLOCKED] = it.data.isBlock
                                    if (it.data.isBlock == 1){
                                        showAccountBlockBottomSheet()
                                    }else{
                                        if (currentVersionCode < latestAppVersionCode) {
                                            //Log.e("#####","UPDATE AVAILABLE")
                                            openAppUpdateDialog(isForceUpdate,updateMsg)
                                        } else {
                                            //Log.e("#####","NO UPDATE AVAILABLE")
                                            setPreferencesForIntroScreen()
                                        }
                                    }

                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                                setPreferencesForIntroScreen()// Add here, bcz sometimes API not work then user can't able to open app
                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                            }
                            hideProgressDialog()
                            setPreferencesForIntroScreen()// Add here, bcz sometimes API not work then user can't able to open app
                        }
                    })
        }
    }

    private fun openAppUpdateDialog(isForceUpdate: Int, updateMsg: String) {
        val dialog = BottomSheetDialog(this@SplashActivity)
        val dialogBinding = UpdateBottomSheetBinding.inflate(layoutInflater)

        dialogBinding.btnNo.visibility = if (isForceUpdate == 0) View.VISIBLE else View.GONE
        dialog.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.show()
        }
        dialogBinding.tvMsg.text = updateMsg
        dialogBinding.animationView.setAnimation(R.raw.update)
        dialogBinding.btnUpdate.setOnClickListener {
            goToUpdateApp()
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
            setPreferencesForIntroScreen()
        }
        dialog.setCancelable(true)

        dialog.setContentView(dialogBinding.root)

        dialog.show()

    }
    fun showAccountBlockBottomSheet() {

        val dialog = BottomSheetDialog(this)
        val view: View = layoutInflater.inflate(
            R.layout.account_block_bottomsheet,
            null
        )

        val tvDes = view.findViewById<TextView>(R.id.tv_des)
        val btn_contactUs = view.findViewById<Button>(R.id.btn_contactUs)
        val btn_ok = view.findViewById<Button>(R.id.btn_cancel)
        val animationView = view.findViewById<LottieAnimationView>(R.id.animationView)

        animationView.setAnimation(R.raw.block)


        btn_contactUs.setOnClickListener {
            val intent = Intent (this, ContactUsActivity::class.java)
            intent.putExtra("for_block",true)
            startActivity(intent)

        }
        btn_ok.setOnClickListener {
            ActivityCompat.finishAffinity(this)
            dialog.dismiss()
        }



        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()

    }
    private fun goToUpdateApp() {
        try {
            val appLink = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(appLink)
        } catch (e: Exception) {
            Toast.makeText(this@SplashActivity, "Unable to find market app", Toast.LENGTH_SHORT).show()
        }
    }
}