package com.amri.emploihunt.authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R

class AskActivity : AppCompatActivity() {
    lateinit var jobseek : CardView
    lateinit var recruit : CardView
    lateinit var activity : Activity
    lateinit var userType: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)

        val window: Window = this@AskActivity.window
        val background = ContextCompat.getDrawable(this@AskActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        window.statusBarColor = ContextCompat.getColor(this@AskActivity, R.color.royal_blue)
        window.navigationBarColor = ContextCompat.getColor(this@AskActivity, R.color.royal_blue)
        window.setBackgroundDrawable(background)

        jobseek = findViewById(R.id.buycv)
        recruit = findViewById(R.id.sellcv)
        activity = this
        jobseek.setOnClickListener {
            userType = "Job Seeker"
            navigateToNextActivity(userType)
        }
        recruit.setOnClickListener {
            userType = "Recruiter"
            navigateToNextActivity(userType)
        }
    }

    private fun navigateToNextActivity(userType: String) {
        val intent = Intent(this@AskActivity, RegistrationActivity::class.java)
        intent.putExtra("userType",userType)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@AskActivity, LoginActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}