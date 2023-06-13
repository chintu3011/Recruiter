package com.example.recruiter

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecruiterHomeActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var frame : FrameLayout
    private var userType:String ?= null
    private var userId:String ?= null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_home)
        bottomNavigationView = findViewById(R.id.bottomnavigationR)
        userType = intent.getStringExtra("userType").toString()
        userId = intent.getStringExtra("userId").toString()
        frame = findViewById(R.id.frameRLayout)
        replaceFragment(HomeRecruitFragment())
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeR -> {
                    replaceFragment(HomeRecruitFragment())
                }

                R.id.postR -> {
                    replaceFragment(PostRecruitFragment())
                }
                R.id.profileR -> {
                    replaceFragment(ProfileFragment())
                }
                R.id.chatR -> {
                    replaceFragment(ChatRecruitFragment())
                }
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("userType", userType!!)
        bundle.putString("userId", userId!!)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameRLayout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}