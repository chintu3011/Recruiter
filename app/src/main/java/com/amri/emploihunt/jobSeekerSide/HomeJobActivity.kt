package com.amri.emploihunt.jobSeekerSide


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.messenger.MessengerHomeActivity
import com.amri.emploihunt.settings.SettingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class HomeJobActivity : BaseActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var homeFragment: HomeJobFragment
    private lateinit var frame : FrameLayout
    private var doubleBackToExitPressedOnce = false
    private var userType:Int ?= null
    private var userId:String ?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_job)

        val window: Window = this@HomeJobActivity.window
        window.statusBarColor = ContextCompat.getColor(this@HomeJobActivity,android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        if (!isGrantedPermission()) {
            requestPermissions()
        }

        userType = intent.getIntExtra("role",0)
//        makeToast("$userId::$userType",0)
        
        bottomNavigationView = findViewById(R.id.bottomnavigation)
        frame = findViewById(R.id.frameLayout)

        homeFragment = HomeJobFragment()
        replaceFragment(homeFragment)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeJobFragment())
                }

                R.id.setting -> {
                    replaceFragment(SettingFragment())
                }
                R.id.chat -> {
                    val intent = Intent(this@HomeJobActivity, MessengerHomeActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {



                    if (!homeFragment.isVisible) {
                        bottomNavigationView.selectedItemId = R.id.home
                        replaceFragment(homeFragment)

                    } else {

                        if (doubleBackToExitPressedOnce) {

                            finishAffinity()
                            return
                        }

                        doubleBackToExitPressedOnce = true
                        Toast.makeText(this@HomeJobActivity, "Please click back again to exit", Toast.LENGTH_SHORT)
                            .show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000)
                    }

                }
            }
        )
    }
    fun replaceFragment(fragment: Fragment) {


        supportFragmentManager.beginTransaction().apply {
            if (fragment.isAdded) {

                show(fragment)
                setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            } else {
                add(R.id.frameLayout, fragment)
            }

            supportFragmentManager.fragments.forEach {
                if (it != fragment && it.isAdded) {
                    hide(it)
                }
            }

        }.commit()
    }

    private fun requestPermissions() {
        val permissions: Collection<String> =
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        Log.d("####", "requestPermissions: $permissions")
        Dexter.withContext(this).withPermissions(
            permissions
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                Log.d("####", "onPermissionsChecked: $report")
                if (report?.areAllPermissionsGranted()!!) {
                    Log.d("permissions###", "permission granted")
                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    // Show dialog when user denied permission permanently, show dialog message.
                    Log.d("permission###", "permission Denied")
                    showSettingsDialog()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()

            }
        }).withErrorListener { error -> Log.e("#####", "onError $error") }.check()
    }

    private fun isGrantedPermission(): Boolean {
        Log.d("Version*", Build.VERSION.SDK_INT.toString())
        listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
        val isGranted1 =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        val isGranted2 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return isGranted1 == PackageManager.PERMISSION_GRANTED && isGranted2 == PackageManager.PERMISSION_GRANTED
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

   /* override fun onBackPressed() {

        super.onBackPressed()
        Log.d("back", "onBackPressed: ")

    }*/

}