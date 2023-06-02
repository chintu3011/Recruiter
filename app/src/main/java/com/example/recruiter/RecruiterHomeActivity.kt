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
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_home)
        bottomNavigationView = findViewById(R.id.bottomnavigationR)
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
                    replaceFragment(ProfileRecruitFragment())
                }
                R.id.chatR -> {
                    replaceFragment(ChatRecruitFragment())
                }
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameRLayout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}