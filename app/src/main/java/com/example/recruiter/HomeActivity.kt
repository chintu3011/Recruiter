package com.example.recruiter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    lateinit var userName:TextView
    lateinit var decorView: View
    lateinit var fullname:String

    private companion object{
        private const val TAG = "PERMISSION_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val window: Window = this@HomeActivity.window
        val background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.status_bar_color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@HomeActivity,android.R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this@HomeActivity,android.R.color.white)
        window.setBackgroundDrawable(background)



        userName = findViewById(R.id.userName)
        fullname = intent.getStringExtra("name").toString()
        userName.text = fullname


    }


    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private val permissionLauncherMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result ->

        var areAllGranted = true
        for(isGranted in result.values){
            makeToast("permissionsStatus: $isGranted",0)
            areAllGranted = areAllGranted  && isGranted
        }

        if(areAllGranted){
              makeToast("permission Granted",0)
        }
        else{
            makeToast("permissionDenied..",0)
        }
    }
    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}