package com.example.recruiter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    lateinit var activity: Activity
    lateinit var preferences: SharedPreferences
    val pref_show = "Intro"
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        preferences = getSharedPreferences("IntroSlider", Context.MODE_PRIVATE)
        if(!preferences.getBoolean(pref_show,true))
        {
            android.os.Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this,AskActivity::class.java)
                startActivity(intent)
                finish()
            } ,
                3000)
        }
        else{
            android.os.Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            } ,
                3000)
        }

    }
}