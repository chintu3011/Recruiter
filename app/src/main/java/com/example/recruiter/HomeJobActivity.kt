package com.example.recruiter

import android.Manifest
//import android.content.Intent
import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
import android.os.Bundle
//import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.karumi.dexter.Dexter
//import com.karumi.dexter.MultiplePermissionsReport
//import com.karumi.dexter.PermissionToken
//import com.karumi.dexter.listener.PermissionRequest
//import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
//import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class HomeJobActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var homeFragment: HomeFragment
    lateinit var postFragment: PostFragment
    lateinit var profileFragment: ProfileFragment
    lateinit var frame : FrameLayout

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadStorageGranted = false
    private var isWriteStorageGranted = false
    private var isCallPhoneGranted = false
    private  var isReceiveSmsGranted = false
    private var isSendSmsGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_job)

        val window: Window = this@HomeJobActivity.window
//        val background = ContextCompat.getDrawable(this@HomeJobActivity, R.drawable.status_bar_color)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@HomeJobActivity,android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        window.setBackgroundDrawable(background)


        permissionLauncher = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions()){ permissions ->

              isReadStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadStorageGranted
              isWriteStorageGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteStorageGranted
//              isCallPhoneGranted = permissions[Manifest.permission.CALL_PHONE] ?: isCallPhoneGranted
//              isReceiveSmsGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: isReceiveSmsGranted
//              isSendSmsGranted = permissions[Manifest.permission.SEND_SMS] ?: isSendSmsGranted
          }
        requestPermissions()

        bottomNavigationView = findViewById(R.id.bottomnavigation)
        frame = findViewById(R.id.frameLayout)
//        replaceFragment(HomeFragment())
        replaceFragment((ProfileFragment()))
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

    
    private  fun requestPermissions(){
        isReadStorageGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        isWriteStorageGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
         val permissionRequest: MutableList<String> = ArrayList()


        if (!isReadStorageGranted){
            makeToast("not granted",0)
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        }
        if (!isWriteStorageGranted) permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionRequest.isNotEmpty()){
            makeToast("launched",0)
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

//    private fun requestPermissions() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            Dexter.withContext(this)
//
//                .withPermissions(
//
//                    Manifest.permission.READ_MEDIA_IMAGES,
//                    Manifest.permission.CALL_PHONE)
//                .withListener(object : MultiplePermissionsListener {
//                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
//                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                            Toast.makeText(this@HomeJobActivity, "All the permissions are granted..", Toast.LENGTH_SHORT).show()
//                        }
//
//                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
//
//                        }
//                    }
//
//                    override fun onPermissionRationaleShouldBeShown(list: List<PermissionRequest>, permissionToken: PermissionToken) {
//
//                        permissionToken.continuePermissionRequest()
//                    }
//                }).withErrorListener {
//
//                    Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
//                }
//
//                .onSameThread().check()
//        }
//    }
//
//    private fun showSettingsDialog() {
//
//        val builder = AlertDialog.Builder(this@HomeJobActivity)
//
//
//        builder.setTitle("Need Permissions")
//
//        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
//        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
//
//            dialog.cancel()
//
//            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//            val uri = Uri.fromParts("package", packageName, null)
//            intent.data = uri
//            startActivityForResult(intent, 101)
//        }
//        builder.setNegativeButton("Cancel") { dialog, which ->
//
//            dialog.cancel()
//        }
//
//        builder.show()
//    }



    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}