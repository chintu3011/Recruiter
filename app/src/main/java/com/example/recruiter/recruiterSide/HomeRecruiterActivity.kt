package com.example.recruiter.recruiterSide

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.recruiter.R
import com.example.recruiter.messenger.MessengerHomeActivity
import com.example.recruiter.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class HomeRecruiterActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var frame : FrameLayout
    private var userType:String ?= null
    private var userId:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_home)

        if (!isGrantedPermission()) {
            requestPermissions()
        }

        bottomNavigationView = findViewById(R.id.bottomnavigationR)
        userType = intent.getStringExtra("userType").toString()

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
                    val intent =
                        Intent(this@HomeRecruiterActivity, MessengerHomeActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("userType", userType!!)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameRLayout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
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

    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}