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
import com.amri.emploihunt.basedata.BaseActivity

class AskActivity : BaseActivity() {
    private lateinit var jobseek : CardView
    private lateinit var recruit : CardView
    lateinit var activity : Activity

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)

        jobseek = findViewById(R.id.buycv)
        recruit = findViewById(R.id.sellcv)
        activity = this
        jobseek.setOnClickListener {
            navigateToNextActivity(0)
        }
        recruit.setOnClickListener {
            navigateToNextActivity(1)
        }
    }

    private fun navigateToNextActivity(userType: Int) {
        val intent = Intent(this@AskActivity, RegistrationActivity::class.java)
        intent.putExtra("role",userType)
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