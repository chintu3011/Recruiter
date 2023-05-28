package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    lateinit var userName:TextView
    lateinit var decorView: View
    lateinit var fullname:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val window: Window = this@HomeActivity.window
        val background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@HomeActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@HomeActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        userName = findViewById(R.id.userName)
        fullname = intent.getStringExtra("name").toString()
        userName.text = fullname
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}