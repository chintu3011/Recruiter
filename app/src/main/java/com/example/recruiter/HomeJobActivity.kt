package com.example.recruiter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
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