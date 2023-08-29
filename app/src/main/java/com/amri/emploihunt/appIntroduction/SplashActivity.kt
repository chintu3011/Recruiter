package com.amri.emploihunt.appIntroduction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.amri.emploihunt.authentication.LoginsignupActivity

import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.util.DEVICE_ID
import com.amri.emploihunt.util.DEVICE_NAME
import com.amri.emploihunt.util.DEVICE_TYPE
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.OS_VERSION
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.ROLE


class SplashActivity : AppCompatActivity() {
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



        setPreferencesForIntroScreen()
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

}