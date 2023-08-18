package com.amri.emploihunt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class IntroductionActivity : AppCompatActivity() {
    lateinit var decorView: View

    private lateinit var onboradingItemsAdapter: OnBoradingItemsAdapter

    private lateinit var indicatorContainer : LinearLayout
//    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        val window: Window = this@IntroductionActivity.window
        val background = ContextCompat.getDrawable(this@IntroductionActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@IntroductionActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@IntroductionActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

//        sharedPreferences = getSharedPreferences("SplashScreen", Context.MODE_PRIVATE)
        
        setOnBoardingItems()
        setupIndicators()
        setCurrentIndicator(0)
        
    }

    private fun setOnBoardingItems(){
        onboradingItemsAdapter = OnBoradingItemsAdapter(
            listOf(
                OnBoardingItem(
                    orboardingImage = R.drawable.logo,
                    title = "Welcome to Emploi Hunt",
                    description = "  Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                ),
                OnBoardingItem(
                    orboardingImage = R.drawable.hiring,
                    title = "Recruit best employee.",
                    description = "  Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                ),
                OnBoardingItem(
                    orboardingImage = R.drawable.jobsearch,
                    title = "Get your Best Jog Here.",
                    description = "  Lorem ipsum dolor sit amet, consectetur adipiscing elit."
                )

            )
        )
        val onBoardingViewPager = findViewById<ViewPager2>(R.id.onBoardingViewPager)
        onBoardingViewPager.adapter = onboradingItemsAdapter

        onBoardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

            }
        })
        (onBoardingViewPager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        findViewById<ImageView>(R.id.imageNext).setOnClickListener{
            if(onBoardingViewPager.currentItem +1 < onboradingItemsAdapter.itemCount){
                onBoardingViewPager.currentItem+=1
            }
            else{
                navigationToNextActivity()
            }
        }

        findViewById<TextView>(R.id.textSkip).setOnClickListener{
            navigationToNextActivity()
        }
        findViewById<Button>(R.id.buttonGetStarted).setOnClickListener{
            navigationToNextActivity()
        }
    }

    private fun navigationToNextActivity() {
        startActivity(Intent(this@IntroductionActivity,loginsignupActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
    }

    private fun setupIndicators (){
        indicatorContainer = findViewById(R.id.indicatorsContainer)
        val indicators = arrayOfNulls<ImageView>(onboradingItemsAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8,0,8,0)
        for(i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let{
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactivate_background
                    )
                )
                it.layoutParams = layoutParams
                indicatorContainer.addView(it)
            }
        }
    }

    private fun setCurrentIndicator(position : Int){
        val childCount = indicatorContainer.childCount
        for(i in 0 until childCount){
            val imageView = indicatorContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext, R.drawable.indicator_activity_background
                    )
                )
            }
            else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext, R.drawable.indicator_inactivate_background
                    )
                )
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        val editor = sharedPreferences.edit()
//        editor.putBoolean("hasShownSplash", true)
//        editor.apply()
//    }
//
//    override fun onRestart() {
//        super.onRestart()
//        val editor = sharedPreferences.edit()
//        editor.putBoolean("hasShownSplash", false)
//        editor.apply()
//
//    }
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}