package com.amri.emploihunt.jobSeekerSide


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.airbnb.lottie.LottieAnimationView
import com.amri.emploihunt.R
import com.amri.emploihunt.authentication.LoginActivity
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.campus.CampusListFragment
import com.amri.emploihunt.databinding.ActivityHomeJobSeekerBinding
import com.amri.emploihunt.filterFeature.FilterDataActivity
import com.amri.emploihunt.filterFeature.FilterParameterTransferClass
import com.amri.emploihunt.model.LogoutMain
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.settings.ContactUsActivity
import com.amri.emploihunt.settings.SettingJobSeekerFragment
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.IS_BLOCKED
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.Utils.toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class HomeJobSeekerActivity : BaseActivity(), FilterParameterTransferClass.FilterJobListListener {

    lateinit var binding : ActivityHomeJobSeekerBinding
    private lateinit var homeFragment: HomeJobSeekerFragment
    private var doubleBackToExitPressedOnce = false
    lateinit var prefmanger: SharedPreferences
    private var userType:Int ?= null
    private var userId:String ?= null

    private var currentFragment: Fragment ?= null

    companion object{
        private const val TAG = "HomeJobSeekerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeJobSeekerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@HomeJobSeekerActivity.window
        window.statusBarColor = ContextCompat.getColor(this@HomeJobSeekerActivity,R.color.colorPrimary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        if (!isGrantedPermission()) {
            requestPermissions()
        }

        prefmanger = prefManager(this)
        userType = prefmanger.get(ROLE,0)
        userId = prefmanger.get(FIREBASE_ID)

        /*userType = intent.getIntExtra("role",0)
        userId = intent.getStringExtra("userId")*/

        if (prefmanger.getInt(IS_BLOCKED,0)==1){
            showAccountBlockBottomSheet()
        }
        Log.d(TAG,"$userId::$userType")

        FilterParameterTransferClass.instance!!.setJobListener(this)

        homeFragment = HomeJobSeekerFragment()
        replaceFragment(homeFragment)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeJobSeekerFragment())
                }
                R.id.setting -> {
                    replaceFragment(SettingJobSeekerFragment())
                }
                R.id.campus -> {
                    replaceFragment(CampusListFragment())
                }
            }
            true
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!homeFragment.isVisible) {
                        binding.bottomNavigationView.selectedItemId = R.id.home
                        replaceFragment(homeFragment)

                    } else {

                        if (doubleBackToExitPressedOnce) {

                            finishAffinity()
                            return
                        }

                        doubleBackToExitPressedOnce = true
                        Toast.makeText(this@HomeJobSeekerActivity, "Please click back again to exit", Toast.LENGTH_SHORT)
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
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.btnSearch -> {
                    val searchView = it.actionView as SearchView

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val currentFragment =
                                supportFragmentManager.findFragmentById(R.id.frameLayout)

                            if (currentFragment is JobListUpdateListener) {
                                currentFragment.updateJobList(newText.orEmpty())
                            }else if (currentFragment is CampusListFragment) {
                                currentFragment.updateJobList(newText.orEmpty())
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
                R.id.btnFilter -> {
                    if(userType == 0 || userType == 1){
                        val intent = Intent(this, FilterDataActivity::class.java)
                        intent.putExtra("role",userType)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    else{
                        makeToast(getString(R.string.something_error),0)
                        Log.e(HomeJobSeekerFragment.TAG,"Incorrect user type : $userType")
                    }

                    true
                }

                R.id.btnLogout -> {
                    logoutUser()
//                    finish()
                    true
                }

                /*R.id.btnLogout -> {
                    logoutUser()
                    true
                }*/

                else -> {
                    false
                }
            }
        }
    }

    private var btnSearch: MenuItem? = null
    private var btnVoiceSearch: MenuItem? = null
    private var btnFilter : MenuItem? = null
    private var btnLogout: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.job_seeker_menu, menu)

        btnSearch = menu?.findItem(R.id.btnSearch)
        btnVoiceSearch = menu?.findItem(R.id.btnVoiceSearch)
        btnLogout = menu?.findItem(R.id.btnLogout)
        btnFilter = menu?.findItem(R.id.btnFilter)
        return true
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        Log.d(TAG,"currentFragment : $currentFragment")
        when (currentFragment) {
            is HomeJobSeekerFragment -> {
                supportActionBar?.title = "Find Best Jobs Here"
                btnSearch?.isVisible = true
                btnVoiceSearch?.isVisible = true
                btnFilter?.isVisible = true
                /*btnFilter?.isVisible = true*/
                btnLogout?.isVisible = false
            }
            is SettingJobSeekerFragment -> {
                supportActionBar?.title = "Settings"
                btnSearch?.isVisible = false
                btnVoiceSearch?.isVisible = false
                btnFilter?.isVisible = false
                /*btnFilter?.isVisible = true*/
                btnLogout?.isVisible = true
            }
            is CampusListFragment -> {
                supportActionBar?.title = "Campus Placement"
                btnSearch?.isVisible = true
                btnVoiceSearch?.isVisible = true

                btnFilter?.isVisible = false
                /*btnFilter?.isVisible = true*/
                btnLogout?.isVisible = false
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
            toast("Problem in voice search,${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = matches!![0]
            if (matches.isNotEmpty()) {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

                if (currentFragment is JobListUpdateListener) {
                    currentFragment.updateJobList(query.orEmpty())
                }else if (currentFragment is CampusListFragment) {
                    currentFragment.updateJobList(query.orEmpty())
                }
            }
        } else {

        }
    }

    /*override fun onDataReceivedFilterJobList(
        domainList: MutableList<String>,
        locationList: MutableList<String>,
        workingModeList: MutableList<String>,
        packageList: MutableList<String>
    ) {
        FilterParameterTransferClass.instance!!.setJobData(
            domainList,
            locationList,
            workingModeList,
            packageList
        )
    }*/
    override fun onDataReceivedFilterJobList(
        domain: String,
        location: String,
        workingMode: String,
        packageRange: String
    ) {
        FilterParameterTransferClass.instance!!.setJobData(
            domain,
            location,
            workingMode,
            packageRange
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

    private fun logoutUser() {
        showLogoutBottomSheet()
    }

    /* override fun onBackPressed() {

         super.onBackPressed()
         Log.d("back", "onBackPressed: ")

     }*/
    fun showLogoutBottomSheet() {

        val dialog = BottomSheetDialog(this)
        val view: View = (this).layoutInflater.inflate(
            R.layout.logout_bottomsheet,
            null
        )


        val btnyes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)
        val tv_des = view.findViewById<TextView>(R.id.tv_des1)
        val animation = view.findViewById<LottieAnimationView>(R.id.animationView)
        tv_des.text = "Are you sure you want to log out?"

        animation.setAnimation(R.raw.logout)

        btnyes.setOnClickListener {
            logoutAPI(prefmanger.get(AUTH_TOKEN,""))
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()

    }
    fun logoutAPI(
        auth: String?,

        ) {
        try {
            if (Utils.isNetworkAvailable(this)) {
                AndroidNetworking.post(NetworkUtils.LOGOUT)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addHeaders("Authorization", "Bearer $auth")
                    .setPriority(Priority.MEDIUM).build()
                    .getAsObject(
                        LogoutMain::class.java,
                        object : ParsedRequestListener<LogoutMain> {
                            override fun onResponse(response: LogoutMain?) {

                                if (response!= null){
                                    hideProgressDialog()
                                    Toast.makeText(this@HomeJobSeekerActivity, response.data.msg, Toast.LENGTH_LONG).show()
                                    prefmanger.set(IS_LOGIN,false)
                                    val intent = Intent(this@HomeJobSeekerActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                }else{
                                    Toast.makeText(this@HomeJobSeekerActivity,getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onError(anError: ANError?) {
                                anError?.let {
                                    Log.e("#####", "onError: code: ${it.errorCode} & body: ${it.errorDetail}")
                                    Toast.makeText(this@HomeJobSeekerActivity,getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                    hideProgressDialog()

                                }

                            }
                        })
            }else{
                Utils.showNoInternetBottomSheet(this, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("#message", "onResponse: "+e.message)
            hideProgressDialog()
            Toast.makeText(this,getString(R.string.something_error), Toast.LENGTH_SHORT).show()
        }

    }
    fun showAccountBlockBottomSheet() {

        val dialog = BottomSheetDialog(this)
        val view: View = layoutInflater.inflate(
            R.layout.account_block_bottomsheet,
            null
        )

        val tvDes = view.findViewById<TextView>(R.id.tv_des)
        val btn_contactUs = view.findViewById<Button>(R.id.btn_contactUs)
        val btn_ok = view.findViewById<Button>(R.id.btn_cancel)
        val animationView = view.findViewById<LottieAnimationView>(R.id.animationView)

        animationView.setAnimation(R.raw.block)


        btn_contactUs.setOnClickListener {
            val intent = Intent (this, ContactUsActivity::class.java)
            intent.putExtra("for_block",true)
            startActivity(intent)

        }
        btn_ok.setOnClickListener {
            ActivityCompat.finishAffinity(this)
            dialog.dismiss()
        }



        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()

    }

}