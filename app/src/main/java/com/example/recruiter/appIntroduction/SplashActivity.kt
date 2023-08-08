package com.example.recruiter.appIntroduction

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.recruiter.R
import com.example.recruiter.authentication.LoginActivity
import com.example.recruiter.jobSeekerSide.HomeJobSeekerActivity
import com.example.recruiter.recruiterSide.HomeRecruiterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SplashActivity : AppCompatActivity() {
    lateinit var decorView: View
    lateinit var activity: Activity

    private var userType = String()
    private lateinit var preferencesForIntroScreen: SharedPreferences

    companion object{
        private const val TAG = "SplashActivity"
    }

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val window: Window = this@SplashActivity.window
        val background = ContextCompat.getDrawable(this@SplashActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@SplashActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@SplashActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        mAuth = FirebaseAuth.getInstance()


        setPreferencesForIntroScreen()
    }
    private fun setPreferencesForIntroScreen() {
        preferencesForIntroScreen = getSharedPreferences("IntroductionScreen",Context.MODE_PRIVATE)
        val hashShownIntro = preferencesForIntroScreen.getBoolean("isFirstTime",true)

        if (hashShownIntro){
            Handler(Looper.getMainLooper()).postDelayed({

                val editor = preferencesForIntroScreen.edit()
                editor.putBoolean("isFirstTime",false)
                editor.apply()
                val intent = Intent(this@SplashActivity, IntroductionActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_down)
                finish()
            },4000)
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                if(mAuth.currentUser != null){
                    Log.d(TAG,mAuth.currentUser!!.uid)
                    getUserType(mAuth.uid.toString()){
                        alreadyLogInNextActivity()
                    }
                }
                else{
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_down)
                    finish()
                }

            },4000)
        }
    }

    private fun getUserType(userId: String,callback: (String) -> Unit){
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var grandParentKey = String()

                for (userTypeSnapshot in snapshot.children) {
                    for (userSnapshot in userTypeSnapshot.children) {
                        val uid = userSnapshot.key
                        if (uid == userId) {
                            grandParentKey =
                                userTypeSnapshot.key.toString() // Key of the grandparent ("Job Seeker" or "Recruiter")
                            Log.d(TAG,"userId: $uid -> userTYpe: $grandParentKey")
                            break
                        }
                    }
                }
                userType = grandParentKey
                callback(userType)
//                handleTaskCompletion()
            }
            override fun onCancelled(error: DatabaseError) {
                makeToast("error: ${error.message}",0)
            }
        })
    }
    private fun alreadyLogInNextActivity() {
        Log.d(TAG,"usertype: $userType")
        if(userType == "Job Seeker"){
            val intent = Intent(this@SplashActivity, HomeJobSeekerActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        else if(userType == "Recruiter"){
            val intent = Intent(this@SplashActivity, HomeRecruiterActivity::class.java)
            intent.putExtra("userType",userType)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
    private fun makeToast(msg: String, len: Int){
        if(len == 0) Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

}