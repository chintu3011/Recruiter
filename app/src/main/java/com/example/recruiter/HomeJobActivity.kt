package com.example.recruiter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class HomeJobActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var homeFragment: HomeFragment
    lateinit var postFragment: PostFragment
    lateinit var profileFragment: ProfileFragment
    lateinit var frame : FrameLayout

    lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
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

              isReadStorageGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadStorageGranted
              isWriteStorageGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteStorageGranted
              isCallPhoneGranted = permissions[android.Manifest.permission.CALL_PHONE] ?: isCallPhoneGranted
              isReceiveSmsGranted = permissions[android.Manifest.permission.RECEIVE_SMS] ?: isReceiveSmsGranted
              isSendSmsGranted = permissions[android.Manifest.permission.SEND_SMS] ?: isSendSmsGranted
          }
        requestPermissions()

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

    private fun requestPermissions() {
        // below line is use to request permission in the current activity.
        // this method is use to handle error in runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(this)
                // below line is use to request the number of permissions which are required in our app.
                .withPermissions(
                    // below is the list of permissions
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CALL_PHONE)
                // after adding permissions we are calling an with listener method.
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                        // this method is called when all permissions are granted
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            // do you work now
                            Toast.makeText(this@HomeJobActivity, "All the permissions are granted..", Toast.LENGTH_SHORT).show()
                        }
                        // check for permanent denial of any permission
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                            // permission is denied permanently, we will show user a dialog message.
    //                        showSettingsDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(list: List<PermissionRequest>, permissionToken: PermissionToken) {
                        // this method is called when user grants some permission and denies some of them.
                        permissionToken.continuePermissionRequest()
                    }
                }).withErrorListener {
                    // we are displaying a toast message for error message.
                    Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
                }
                // below line is use to run the permissions on same thread and to check the permissions
                .onSameThread().check()
        }
    }

    // below is the shoe setting dialog method
    // which is use to display a dialogue message.
    private fun showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        val builder = AlertDialog.Builder(this@HomeJobActivity)

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions")

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel()
            // below is the intent from which we are redirecting our user.
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // this method is called when user click on negative button.
            dialog.cancel()
        }
        // below line is used to display our dialog
        builder.show()
    }



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