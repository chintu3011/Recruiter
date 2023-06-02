package com.example.recruiter

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeJobActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var homeFragment: HomeFragment
    lateinit var postFragment: PostFragment
    lateinit var profileFragment: ProfileFragment
    lateinit var frame : FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_job)

        val window: Window = this@HomeJobActivity.window
//        val background = ContextCompat.getDrawable(this@HomeJobActivity, R.drawable.status_bar_color)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@HomeJobActivity,android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        window.setBackgroundDrawable(background)

        bottomNavigationView = findViewById(R.id.bottomnavigation)
        frame = findViewById(R.id.frameLayout)
        replaceFragment(HomeFragment())
        homeFragment = HomeFragment()
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                }

                R.id.post -> {
                    replaceFragment(PostFragment())
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                }
                R.id.chat -> {
                    replaceFragment(ChatFragment())
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}