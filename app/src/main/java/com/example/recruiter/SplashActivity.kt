package com.example.recruiter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import androidx.appcompat.app.AppCompatActivity
import render.animations.Render


class SplashActivity : AppCompatActivity() {
    lateinit var decorView: View
    lateinit var activity: Activity
    private lateinit var preferencesForIntroScreen: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        fullScreen()
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
                val intent = Intent(this@SplashActivity,IntroductionActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_top,R.anim.slide_out_down)
                finish()
            },4000)
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashActivity,LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                finish()
            },4000)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private fun fullScreen() {
        decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { i ->
            if (i == 0) {
                decorView.systemUiVisibility = hideSystemBars()
            }
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorView.systemUiVisibility = hideSystemBars()
        }
    }

    private fun hideSystemBars(): Int {
        return (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}