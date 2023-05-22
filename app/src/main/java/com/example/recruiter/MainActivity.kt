package com.example.recruiter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View.*
class MainActivity : AppCompatActivity() {
    val frag1 = SliderFragment()
    val frag2 = SliderFragment1()
    val frag3 = SliderFragment2()
    lateinit var view_pager : ViewPager
    lateinit var btn_next : Button; lateinit var btn_skip : Button
    lateinit var tv_1 : TextView; lateinit var tv_2 : TextView; lateinit var tv_3 : TextView
    lateinit var adapter: myPageAdapter
    lateinit var activity: Activity
    lateinit var preferences: SharedPreferences
    lateinit var decorView: View
    val pref_show = "Intro"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fullScreen()
        adapter = myPageAdapter(supportFragmentManager)
        adapter.list.add(frag1)
        adapter.list.add(frag2)
        adapter.list.add(frag3)
        activity = this
        preferences = getSharedPreferences("IntroSlider", Context.MODE_PRIVATE)
        if(!preferences.getBoolean(pref_show,true))
        {
            startActivity(Intent(activity,AskActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }
        view_pager = findViewById(R.id.viewpager);
        btn_skip = findViewById(R.id.skipbtn);
        btn_next = findViewById(R.id.nextbtn);
        tv_1 = findViewById(R.id.tv1);
        tv_2 = findViewById(R.id.tv2);
        tv_3 = findViewById(R.id.tv3);
        view_pager.adapter = adapter
        btn_skip.setOnClickListener {
            startActivity(Intent(activity,AskActivity::class.java))
            finish()
            val editor = preferences.edit()
            editor.putBoolean(pref_show,false)
            editor.apply()
        }
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener
        {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position==adapter.list.size-1)
                {
                    btn_next.text = "FINISH"
                    btn_next.setOnClickListener {
                        startActivity(Intent(activity,AskActivity::class.java))
                        finish()
                        val editor = preferences.edit()
                        editor.putBoolean(pref_show,false)
                        editor.apply()
                    }
                }else{
                    btn_next.text = "NEXT"
                    btn_next.setOnClickListener {
                        view_pager.currentItem++
                    }
                }
                when(view_pager.currentItem){
                    0 -> {
                        btn_next.text = "NEXT"
                        btn_next.setOnClickListener {
                            view_pager.currentItem++
                        }
                        tv_1.setTextColor(Color.BLACK)
                        tv_2.setTextColor(Color.GRAY)
                        tv_3.setTextColor(Color.GRAY)
                    }
                    1 -> {
                        btn_next.text = "NEXT"
                        btn_next.setOnClickListener {
                            view_pager.currentItem++
                        }
                        tv_2.setTextColor(Color.BLACK)
                        tv_1.setTextColor(Color.GRAY)
                        tv_3.setTextColor(Color.GRAY)
                    }
                    2 -> {
                        btn_next.text = "FINISH"
                        tv_3.setTextColor(Color.BLACK)
                        tv_2.setTextColor(Color.GRAY)
                        tv_1.setTextColor(Color.GRAY)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
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

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}
class myPageAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager)
{
    val list:MutableList<Fragment> = ArrayList();
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

}