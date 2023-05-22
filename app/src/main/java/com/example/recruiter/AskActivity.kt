package com.example.recruiter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView

class AskActivity : AppCompatActivity() {
    lateinit var jobseek : CardView
    lateinit var recruit : CardView
    lateinit var activity : Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
        jobseek = findViewById(R.id.buycv)
        recruit = findViewById(R.id.sellcv)
        activity = this
        jobseek.setOnClickListener {
            startActivity(Intent(activity,JobLoginActivity::class.java))
            finish()
        }
        recruit.setOnClickListener {
            startActivity(Intent(activity,RecruiterLoginActivity::class.java))
            finish()
        }
    }
}