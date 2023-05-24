package com.example.recruiter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import android.view.View.*

class AskActivity : AppCompatActivity() {
    lateinit var jobseek : CardView
    lateinit var recruit : CardView
    lateinit var activity : Activity
    lateinit var decorView: View

    lateinit var jobType: String
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
        fullScreen()
        jobseek = findViewById(R.id.buycv)
        recruit = findViewById(R.id.sellcv)
        activity = this
        jobseek.setOnClickListener {
            jobType = "JobSeeker"
            navigateToNextActivity(jobType)
        }
        recruit.setOnClickListener {
            jobType = "Recruiter"
            navigateToNextActivity(jobType)
        }
    }

    private fun navigateToNextActivity(jobType: String) {
        val intent = Intent(this@AskActivity,RegistrationActivity::class.java)
        intent.putExtra("jobType",jobType)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@AskActivity,LoginActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        finish()
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