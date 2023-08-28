package com.amri.emploihunt.recruiterSide

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityHomeRecruiterBinding
import com.amri.emploihunt.filterFeature.FilterParameterTransferClass
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerFragment
import com.amri.emploihunt.jobSeekerSide.JobListUpdateListener
import com.amri.emploihunt.messenger.MessengerHomeActivity
import com.amri.emploihunt.settings.SettingJobSeekerFragment
import com.amri.emploihunt.settings.SettingRecruiterFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class HomeRecruiterActivity : BaseActivity(), FilterParameterTransferClass.FilterApplicationListener {

    lateinit var binding:ActivityHomeRecruiterBinding

    private lateinit var homeRecruitFragment: HomeRecruitFragment
    private var doubleBackToExitPressedOnce = false

    private var userType:Int ?= null
    private var userId:String ?= null

    private var currentFragment:Fragment ?= null
    companion object{
        private const val TAG = "HomeRecruiterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeRecruiterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isGrantedPermission()) {
            requestPermissions()
        }

        userType = intent.getIntExtra("role",1)
        userId = intent.getStringExtra("userId")
        Log.d(TAG,"$userId::$userType")

        FilterParameterTransferClass.instance!!.setApplicationListener(this)

        homeRecruitFragment = HomeRecruitFragment()

        replaceFragment(homeRecruitFragment)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeRecruitFragment())
                }
                R.id.post -> {
                    replaceFragment(PostRecruitFragment())
                }
                R.id.setting -> {
                    replaceFragment(SettingRecruiterFragment())
                }
                /*R.id.chatR -> {
                    val intent = Intent(this@HomeRecruiterActivity, MessengerHomeActivity::class.java)
                    startActivity(intent)
                }*/
            }
            true
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (!homeRecruitFragment.isVisible) {
                        binding.bottomNavigation.selectedItemId = R.id.home
                        replaceFragment(homeRecruitFragment)

                    } else {

                        if (doubleBackToExitPressedOnce) {
                            finishAffinity()
                            return
                        }

                        doubleBackToExitPressedOnce = true
                        Toast.makeText(this@HomeRecruiterActivity, "Please click back again to exit", Toast.LENGTH_SHORT)
                            .show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000)
                    }

                }
            }
        )

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        /*supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)*/

        supportFragmentManager.addOnBackStackChangedListener {
            invalidateOptionsMenu() // This triggers onPrepareOptionsMenu()
        }

        setMenuItemListener()
    }

    private fun setMenuItemListener() {
        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.btnSearch -> {
                    val searchView = it.actionView as SearchView

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

                            if (currentFragment is ApplicationListUpdateListener) {
                                currentFragment.updateApplicationList(newText.orEmpty())
                            }
                            return true
                        }
                    })
                    true
                }
                R.id.btnVoiceSearch -> {
                    openVoice()
                    true
                }
                R.id.btnMessenger -> {
                    val intent = Intent(this@HomeRecruiterActivity,MessengerHomeActivity::class.java)
                    intent.putExtra("role", userType!!)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in_left,R.anim.slide_out_right)
                    }
                    else{
                        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    }
                    finish()
                    true
                }
                R.id.btnLogout -> {
//                    logoutUser()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private var btnSearch: MenuItem? = null
    private var btnVoiceSearch: MenuItem? = null
    private var btnMessenger : MenuItem? = null
    private var btnLogout: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recruiter_menu, menu)

        btnSearch = menu?.findItem(R.id.btnSearch)
        btnVoiceSearch = menu?.findItem(R.id.btnVoiceSearch)
        btnLogout = menu?.findItem(R.id.btnLogout)
        btnMessenger = menu?.findItem(R.id.btnMessenger)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        Log.d(TAG,"currentFragment : $currentFragment")
        when (currentFragment) {
            is HomeRecruitFragment -> {
                supportActionBar?.title = "Find Best Employees Here"
                btnSearch?.isVisible = true
                btnVoiceSearch?.isVisible = true
                btnMessenger?.isVisible = true
                btnLogout?.isVisible = false
            }
            is PostRecruitFragment -> {
                supportActionBar?.title = "Create Job Post"
                btnSearch?.isVisible = false
                btnVoiceSearch?.isVisible = false
                btnMessenger?.isVisible = false
                btnLogout?.isVisible = false
            }
            is SettingRecruiterFragment -> {
                supportActionBar?.title = "Settings"
                btnSearch?.isVisible = false
                btnVoiceSearch?.isVisible = false
                btnMessenger?.isVisible = false
                btnLogout?.isVisible = true
            }
            else -> {
                Log.e(TAG,"fragment not found")
                makeToast(getString(R.string.something_error),0)
            }

        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onContextItemSelected(item)
    }

    private fun openVoice() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent, 200)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://market.android.com/details?id=APP_PACKAGE_NAME")
            )
            startActivity(browserIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = matches!![0]
            if (matches.isNotEmpty()) {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

                if (currentFragment is ApplicationListUpdateListener) {
                    currentFragment.updateApplicationList(query.orEmpty())
                }
            }
        } else {
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDataReceivedFilterApplicationList(
        domainList: MutableList<String>,
        locationList: MutableList<String>,
        workingModeList: MutableList<String>,
        packageList: MutableList<String>
    ) {
        FilterParameterTransferClass.instance!!.setApplicationData(domainList,locationList,workingModeList,packageList)
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            if (fragment.isAdded) {
                show(fragment)

            } else {
                add(R.id.frameLayout, fragment)
            }
            currentFragment = fragment
            supportFragmentManager.fragments.forEach {
                if (it != fragment && it.isAdded) {
                    hide(it)
                }
            }

        }
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
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



}