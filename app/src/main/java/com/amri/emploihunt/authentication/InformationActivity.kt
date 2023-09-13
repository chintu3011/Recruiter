package com.amri.emploihunt.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityInformationBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.CommonMessageModel
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.UserDataRepository
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.DEVICE_ID
import com.amri.emploihunt.util.DEVICE_NAME
import com.amri.emploihunt.util.DEVICE_TYPE
import com.amri.emploihunt.util.FCM_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.JOB_SEEKER
import com.amri.emploihunt.util.LATITUDE
import com.amri.emploihunt.util.LONGITUDE
import com.amri.emploihunt.util.MOB_NO
import com.amri.emploihunt.util.OS_VERSION
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.RECRUITER
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.USER_ID
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.convertUriToPdfFile
import com.amri.emploihunt.util.Utils.showNoInternetBottomSheet
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.BuildConfig
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.Stack


@AndroidEntryPoint
class InformationActivity : BaseActivity() ,OnClickListener, AdapterView.OnItemSelectedListener{


    companion object{
        const val TAG = "InformationActivity"
    }

    private lateinit var binding: ActivityInformationBinding
    private lateinit var prefManager: SharedPreferences

    private var layoutID = -1
    private var btnPointer = 0
    private var grpPointer = 0

    private var userType: Int? = null
    private var userId: String? = null

    //common data
    private var fName: String? = null
    private var lName: String? = null
    private var fullName: String? = null
    private var phoneNumber: String? = null
    private var emailId: String? = null
    private var tagLine: String? = null
    private var termsConditionsAcceptance: String? = null
    private var residentialCity:String? = null

    private var profileImgUri: String? = null
    private var profileBannerImgUri: String? = null

    //User Data
    private var bio: String? = null
    private var qualification: String? = null

    private var currentCompany: String? = null
    private var designation: String? = null
    private var jobLocation: String? = null
    private var workingMode:String? = null

    private lateinit var experienceList:MutableList<Experience>

    private var prefJobTitle: String? = null
    private var prefJobLocation: String? = null
    private var prefWorkingMode:String? = null

    private var resumeUri: String? = null
    private var resumeFileName: String? = null



    lateinit  var  qualifications:kotlin.Array<String>
    lateinit  var  jobs:kotlin.Array<String>
    /*var cityList: ArrayList<String> = ArrayList()*/
//    var prefLocations: ArrayList<String> = ArrayList()


    private var selectedQualification = String()
    private var selectedJobLocation = String()
    private var selectedPreJobLocation = String()
    private var selectedDesignation = String()
    private var selectedPrefJobTitle = String()
    private var selectedPrefCity = String()


    private var selectedQualificationR = String()


    private var isSkip: Boolean = false
    private lateinit var resumePdf: File
    private lateinit var profileImg: File


    /*private lateinit var nextStack:Stack<View>
    private lateinit var backStack:Stack<View>
    private lateinit var jGroupArray:ArrayList<View>
    private lateinit var rGroupArray:ArrayList<View>*/

    private lateinit var checkStack:Stack<ShapeableImageView>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        jobs = resources.getStringArray(R.array.indian_designations)
        qualifications = resources.getStringArray(R.array.degree_array)
        prefManager = prefManager(this@InformationActivity)

        /*nextStack = Stack()
        backStack = Stack()*/
        checkStack = Stack()

        val inn: Animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        val out: Animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)

        // set the animation type to ViewFlipper

        // set the animation type to ViewFlipper
        binding.jsViewFlipper.inAnimation = inn
        binding.jsViewFlipper.outAnimation = out
        
        binding.rViewFlipper.inAnimation = inn
        binding.rViewFlipper.outAnimation = out


        userType = intent.getIntExtra("role",0)
        setLayout(userType!!)

        setOnClickListener()
        setTabSelectedListener()

        /*getAllCity()
        cityList.add("City")
        prefLocations.add("City")*/
        /*setAdapters()*/


    }

   /* private fun setAdapters() {

        val jobLocationAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,cityList)
        jobLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.JobLocationSpinnerR.adapter = jobLocationAdapter
        binding.JobLocationSpinnerJ.adapter = jobLocationAdapter

        val prefJobLocationAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,prefLocations)
        prefJobLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputCitySpinnerJ.adapter = prefJobLocationAdapter

        val qualificationsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,qualifications)
        qualificationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputDegreeTypeSpinner.adapter = qualificationsAdapter
        binding.inputDegreeRSpinner.adapter = qualificationsAdapter

        val jobsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,jobs)
        jobsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.inputJobTypeSpinner.adapter = jobsAdapter
    }*/
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        /*selectedJobLocation = cityList[position]


        Log.d("###", "onItemSelected: $selectedJobLocation $selectedPreJobLocation $position")*/
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun setLayout(userType: Int) {

        when (userType) {
            RECRUITER -> {
                binding.inputLayoutRecruiter.visibility = VISIBLE
                changeLayout(false)
            }
            JOB_SEEKER -> {
                binding.inputLayoutJobSeeker.visibility = VISIBLE
                changeLayout(false)
            }
            else -> {
                makeToast(getString(R.string.something_error),1)
            }
        }
        btnPointer = 0
        userId = intent.getStringExtra("uid").toString().trim()
        fName = intent.getStringExtra("fName").toString().trim()
        lName = intent.getStringExtra("lName").toString().trim()
        phoneNumber = intent.getStringExtra("phoneNo").toString().trim()
        emailId = intent.getStringExtra("email").toString().trim()
        residentialCity = intent.getStringExtra("city").toString().trim()
        termsConditionsAcceptance = intent.getStringExtra("termsConditions").toString().trim()

    }
    var experienced = false
    private fun setTabSelectedListener(){


        binding.tbWorkingModeJ.addOnTabSelectedListener(object :OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                prefWorkingMode = tab?.text.toString()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
              
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                prefWorkingMode = tab?.text.toString()
            }

        })
        binding.tbWorkingModeR.addOnTabSelectedListener(object :OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                workingMode = tab?.text.toString()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                workingMode = tab?.text.toString()
            }

        })
        binding.tbFreshExpJ.addOnTabSelectedListener( object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.position == 0){
                    experienced = false

                }
                else if (tab?.position == 1){
                    experienced = true
                }

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                if(tab?.position == 0){
                    experienced = false
                }
                else if (tab?.position == 1){
                    experienced = true
                }
            }

        })
    }

    private fun setOnClickListener() {
        binding.layoutCV.setOnClickListener(this)
       /* binding.uploadBtn.setOnClickListener(this)*/
        binding.addProfileImgJ.setOnClickListener(this)
        binding.addProfileImgR.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)

        //job Seeker cur designation
        binding.spDesignationJ.setSearchDialogGravity(Gravity.TOP)
        binding.spDesignationJ.arrowPaddingRight = 19
        binding.spDesignationJ.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spDesignationJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spDesignationJ.isOutlined = true
                selectedDesignation = binding.spDesignationJ.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

        binding.spQualificationJ.setSearchDialogGravity(Gravity.TOP)
        binding.spQualificationJ.arrowPaddingRight = 19
        binding.spQualificationJ.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spQualificationJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spQualificationJ.isOutlined = true
                selectedQualification = binding.spQualificationJ.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

        binding.spPrefJobTitleJ.setSearchDialogGravity(Gravity.TOP)
        binding.spPrefJobTitleJ.arrowPaddingRight = 19
        binding.spPrefJobTitleJ.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spPrefJobTitleJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spPrefJobTitleJ.isOutlined = true
                selectedPrefJobTitle = binding.spPrefJobTitleJ.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

        binding.spQualificationR.setSearchDialogGravity(Gravity.TOP)
        binding.spQualificationR.arrowPaddingRight = 19
        binding.spQualificationR.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spQualificationR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spQualificationR.isOutlined = true
                selectedPrefJobTitle = binding.spQualificationR.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

        binding.spDesignationR.setSearchDialogGravity(Gravity.TOP)
        binding.spDesignationR.arrowPaddingRight = 19
        binding.spDesignationR.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spDesignationR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spDesignationR.isOutlined = true
                selectedPrefJobTitle = binding.spDesignationR.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }


        val cityList:ArrayList<String> = arrayListOf()

        getAllCity(cityList) {

            if(cityList.isNotEmpty()) {
                binding.spJobLocationJ.setSearchDialogGravity(Gravity.TOP)
                binding.spJobLocationJ.arrowPaddingRight = 19
                binding.spJobLocationJ.item = cityList.toList()
                    /*resources.getStringArray(R.array.indian_designations).toList()*/
                binding.spJobLocationJ.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            adapterView: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            binding.spJobLocationJ.isOutlined = true
                            selectedJobLocation = binding.spJobLocationJ.item[position].toString()
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {

                        }
                    }

                binding.spJobLocationR.setSearchDialogGravity(Gravity.TOP)
                binding.spJobLocationR.arrowPaddingRight = 19
                binding.spJobLocationR.item =  cityList.toList()
                   /* resources.getStringArray(R.array.degree_array).toList()*/
                binding.spJobLocationR.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            adapterView: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            binding.spJobLocationR.isOutlined = true
                            selectedPrefCity = binding.spJobLocationR.item[position].toString()
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {

                        }
                    }

                binding.spPrefCityJ.setSearchDialogGravity(Gravity.TOP)
                binding.spPrefCityJ.arrowPaddingRight = 19
                binding.spPrefCityJ.item = cityList.toList()
                    /*resources.getStringArray(R.array.degree_array).toList()*/
                binding.spPrefCityJ.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            adapterView: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            binding.spPrefCityJ.isOutlined = true
                            selectedPrefCity = binding.spPrefCityJ.item[position].toString()
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>?) {

                        }
                    }
            }
            else{
                makeToast(getString(R.string.something_error),0)
            }
        }
        /*binding.JobLocationSpinnerR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedJobLocation = cityList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.JobLocationSpinnerJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                Log.d("###", "onItemSelected: ")
                selectedJobLocation = cityList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.inputCitySpinnerJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedPreJobLocation = prefLocations[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.inputDegreeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedQualification = qualifications[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.inputJobTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedJob = jobs[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.inputDegreeRSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                selectedQualificationR = qualifications[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }*/


    }

    var profilLayoutStatusBack = false
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutCV -> {
                selectpdf()
            }
            R.id.addProfileImgJ ->{

                val deniedPermission:MutableList<String> = isGrantedPermission()
                if(deniedPermission.isEmpty()) {
                    selectImg()
                }
                else{
                    requestPermissions(deniedPermission){
                        if(it){
                            selectImg()
                        }
                        else{
                            val snackbar = Snackbar
                                .make(
                                    binding.root,
                                    "Sorry! you aren't given required permissions.",
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction(
                                    "Grant Permissions"
                                )
                                {
                                    showSettingsDialog()
                                }

                            snackbar.show()
                        }
                    }
                }
            }
            R.id.addProfileImgR ->{
                selectImg()
            }
            R.id.btnSubmit -> {
                if(userType == RECRUITER) storeInfoR()
                if (userType == JOB_SEEKER) storeInfoJ()
            }
            R.id.btnNext -> {

                if(userType == JOB_SEEKER){
                    if(binding.jsViewFlipper.currentView == binding.jsAboutGrp){
                        if(!experienced){
                            binding.jsViewFlipper.showNext()
                            binding.jsViewFlipper.showNext()
                            changeLayout(false)
                        }
                        else{
                            binding.jsViewFlipper.showNext()
                            changeLayout(false)
                        }
                    }
                    else{
                        binding.jsViewFlipper.showNext()
                        changeLayout(false)
                    }

                }
                else{
                    binding.rViewFlipper.showNext()
                    changeLayout(true)
                }
            }
            R.id.btnBack -> {
                if(userType == JOB_SEEKER){
                    binding.jsViewFlipper.showPrevious()
                    changeLayout(true)
                }
                else{
                    binding.rViewFlipper.showPrevious()
                    changeLayout(true)
                }
            }
            R.id.btnSkip -> {

                if (userType == JOB_SEEKER){
                    storeInfoJBySkip()
                }
                if(userType == RECRUITER){
                    storeInfoRSkip()
                }

            }
        }
    }
    private fun selectImg() {
        val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
        imgIntent.type = "image/*"
        imgIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(imgIntent, 22)
    }

    private fun selectpdf() {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pdfIntent, 12)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                12 -> if (resultCode == RESULT_OK) {
                    val pdfUri = data?.data!!
                    val uri: Uri = data.data!!
                    val uriString: String = uri.toString()
                    resumePdf = convertUriToPdfFile(this@InformationActivity,uri)!!

                    if (uriString.startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                uri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                resumeFileName =
                                    myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                binding.textPdfName.text = resumeFileName
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                }
                22 -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!

                    if(userType == JOB_SEEKER){
                        profileImg = File(Utils.getRealPathFromURI(this, photoUri).toString())
                        Glide.with(this@InformationActivity)
                            .load(photoUri)
                            .apply(
                                RequestOptions
                                    .placeholderOf(R.drawable.profile_default_image)
                                    .error(R.drawable.profile_default_image)
                                    .circleCrop()
                            )
                            .into(binding.profileImgJ)
                    }
                    else{
                        profileImg = File(Utils.getRealPathFromURI(this, photoUri).toString())
                        Glide.with(this@InformationActivity)
                            .load(photoUri)
                            .apply(
                                RequestOptions
                                    .placeholderOf(R.drawable.profile_default_image)
                                    .error(R.drawable.profile_default_image)
                                    .circleCrop()
                            )
                            .into(binding.profileImgR)
                    }
                    binding.submitBtnLayout.visibility = VISIBLE
                    /*
                    if (photoUri.toString().startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                photoUri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                val imgName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                val storageRef = Firebase.storage.reference
                                val path = "images/userImages/$userId/profileImg"
                                val imageRef = storageRef.child(path)

                                imageRef.putFile(photoUri)
                                    .addOnProgressListener {
                                        *//*binding.uploadProgressLayout.visibility = VISIBLE*//*
                                        *//*val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                                        binding.uploadProgressBar.progress = progress*//*
                                    }
                                    .addOnSuccessListener {

                                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                            *//*binding.uploadProgressLayout.visibility = GONE*//*
                                            profileImgUri = downloadUri.toString()
                                            Glide.with(this@InformationActivity)
                                                .load(profileImgUri)
                                                .apply(
                                                    RequestOptions
                                                        .placeholderOf(R.drawable.profile_default_image)
                                                        .error(R.drawable.profile_default_image)
                                                        .circleCrop()
                                                )
                                                .into(binding.profileImgJ)
                                            *//*val contentResolver: ContentResolver = contentResolver
                                            val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                                            photoBitmap = Bitmap.createScaledBitmap(bitmap, profileImgDia.width, profileImgDia.height, false)
                                            profileImgDia.setImageBitmap(photoBitmap)*//*
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        *//*binding.uploadProgressLayout.visibility = GONE*//*
                                        makeToast("Img is not stored successfully",0)
                                        Log.e(ProfileActivity.TAG, "onActivityResult: error while storing Img $exception" )
                                    }
                                *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                }
                                else{
                                    Log.d(TAG, "onActivityResult: ${Build.VERSION.SDK_INT} in not capable.")
                                }*//*
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                    */
                }
            }
        }

    }

    private fun changeLayout(isBackPress:Boolean) {
        when(userType) {

            JOB_SEEKER -> {
                binding.jsScrollView.scrollTo(0,0)
                when (binding.jsViewFlipper.currentView) {
                      binding.jsAboutGrp -> {
                          experienced = false
                          binding.btnNext.visibility = VISIBLE
                          binding.btnBack.visibility = GONE
                          binding.submitBtnLayout.visibility = GONE
                          setChecks(binding.jsAboutGrp)
                      }

                      binding.jsExperienceGrp -> {
                          if(isBackPress && !experienced){
                              binding.jsViewFlipper.showPrevious()
                              binding.submitBtnLayout.visibility = GONE
                              changeLayout(true)
                          }
                          else{
                              binding.btnNext.visibility = VISIBLE
                              binding.btnBack.visibility = VISIBLE
                              binding.submitBtnLayout.visibility = GONE
                              setChecks(binding.jsExperienceGrp)
                          }

                      }

                      binding.jsPreferJobGrp ->{
                          binding.btnBack.visibility = VISIBLE
                          binding.btnNext.visibility = VISIBLE
                          binding.submitBtnLayout.visibility = GONE
                          setChecks(binding.jsPreferJobGrp)
                      }
                      binding.jsResumeGrp-> {
                          binding.btnBack.visibility = VISIBLE
                          binding.btnNext.visibility = VISIBLE
                          binding.submitBtnLayout.visibility = GONE
                          setChecks(binding.jsResumeGrp)
                      }

                      binding.profileImgLayoutJ -> {
                          binding.btnBack.visibility = VISIBLE
                          binding.btnNext.visibility = GONE
                          binding.submitBtnLayout.visibility = GONE
                          setChecks(binding.profileImgLayoutJ)
                      }

                  }
              }

            RECRUITER ->{
                binding.rScrollView.scrollTo(0,0)
                when (binding.rViewFlipper.currentView) {
                    binding.rAboutGrp -> {
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = GONE
                        binding.submitBtnLayout.visibility = GONE
                        setChecks(binding.rAboutGrp)
                    }
                    binding.rCurrPosGrp -> {
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = VISIBLE
                        binding.submitBtnLayout.visibility = GONE
                        setChecks(binding.rCurrPosGrp)
                    }
                    binding.profileImgLayoutR -> {
                        binding.btnBack.visibility = VISIBLE
                        binding.btnNext.visibility = GONE
                        binding.submitBtnLayout.visibility = GONE
                        setChecks(binding.profileImgLayoutR)
                    }

                }
            }
        }

    }

    private fun setChecks(view: View){
        binding.check1.visibility = VISIBLE
        binding.check1.setBackgroundResource(R.color.blue)
        binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))

        when(userType){

            JOB_SEEKER -> {
                binding.check2.visibility = VISIBLE
                binding.check3.visibility = VISIBLE
                binding.check4.visibility = VISIBLE
                binding.check5.visibility = VISIBLE

                when (view) {
                    binding.jsAboutGrp -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(null)
                        binding.check3.setBackgroundResource(R.color.check_def_color)
                        binding.check3.setImageDrawable(null)
                        binding.check4.setBackgroundResource(R.color.check_def_color)
                        binding.check4.setImageDrawable(null)
                        binding.check5.setBackgroundResource(R.color.check_def_color)
                        binding.check5.setImageDrawable(null)

                    }
                    binding.jsExperienceGrp -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(null)
                        binding.check3.setBackgroundResource(R.color.check_def_color)
                        binding.check3.setImageDrawable(null)
                        binding.check4.setBackgroundResource(R.color.check_def_color)
                        binding.check4.setImageDrawable(null)
                        binding.check5.setBackgroundResource(R.color.check_def_color)
                        binding.check5.setImageDrawable(null)
                    }
                    binding.jsPreferJobGrp -> {
                        Log.d("____", "setChecks: $experienced")
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check3.setBackgroundResource(R.color.blue)
                        binding.check3.setImageDrawable(null)
                        binding.check4.setBackgroundResource(R.color.check_def_color)
                        binding.check4.setImageDrawable(null)
                        binding.check5.setBackgroundResource(R.color.check_def_color)
                        binding.check5.setImageDrawable(null)

                    }
                    binding.jsResumeGrp -> {
                        Log.d("____", "setChecks: $experienced")
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check3.setBackgroundResource(R.color.blue)
                        binding.check3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check4.setBackgroundResource(R.color.blue)
                        binding.check4.setImageDrawable(null)
                        binding.check5.setBackgroundResource(R.color.check_def_color)
                        binding.check5.setImageDrawable(null)
                    }
                    binding.profileImgLayoutJ -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check3.setBackgroundResource(R.color.blue)
                        binding.check3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check4.setBackgroundResource(R.color.blue)
                        binding.check4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check5.setBackgroundResource(R.color.blue)
                        binding.check5.setImageDrawable(null)

                    }
                }
            }
            RECRUITER -> {
                binding.check2.visibility = VISIBLE
                binding.check3.visibility = VISIBLE
                binding.check4.visibility = VISIBLE
                when (view) {
                    binding.rAboutGrp -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(null)
                        binding.check3.setBackgroundResource(R.color.check_def_color)
                        binding.check3.setImageDrawable(null)
                        binding.check4.setBackgroundResource(R.color.check_def_color)
                        binding.check4.setImageDrawable(null)
                    }
                    binding.rCurrPosGrp -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check3.setBackgroundResource(R.color.blue)
                        binding.check3.setImageDrawable(null)
                        binding.check4.setBackgroundResource(R.color.check_def_color)
                        binding.check4.setImageDrawable(null)
                    }
                    binding.profileImgLayoutR -> {
                        binding.check2.setBackgroundResource(R.color.blue)
                        binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check3.setBackgroundResource(R.color.blue)
                        binding.check3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))
                        binding.check4.setBackgroundResource(R.color.blue)
                        binding.check4.setImageDrawable(null)
                    }
                }
            }
        }


    }


    private fun storeInfoJ() {

        qualification = selectedQualification.trim()
        bio = binding.bio.text.toString().trim()
        currentCompany = binding.companyName.text.toString().trim()
        designation = selectedDesignation.trim()
        jobLocation = selectedJobLocation.trim()

        prefJobTitle = selectedPrefJobTitle.trim()
        prefJobLocation = selectedPrefCity.trim()

        val correct = inputFieldConformationJ(qualification!!,bio!!,currentCompany!!,designation!!,jobLocation!!,prefJobTitle!!,prefJobLocation!!,prefWorkingMode!!)
        if (!correct) return
        else{
            if (Utils.isNetworkAvailable(this)){
                Log.d(TAG, "storeInfoJ: $residentialCity $jobLocation $prefJobLocation")
                val versionCodeAndName =
                    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                if(experienced) {
                    AndroidNetworking.upload(NetworkUtils.REGISTER_USER)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addQueryParameter("vFirebaseId", userId)
                        .addQueryParameter("iRole", "0")
                        .addQueryParameter(DEVICE_ID, prefManager.get(DEVICE_ID))
                        .addQueryParameter(DEVICE_TYPE, "0")
                        .addQueryParameter(OS_VERSION, prefManager.get(OS_VERSION))
                        .addQueryParameter(FCM_TOKEN, prefManager.get(FCM_TOKEN))
                        .addQueryParameter(DEVICE_NAME, prefManager.get(DEVICE_NAME))
                        .addQueryParameter("vFirstName", fName)
                        .addQueryParameter("vLastName", lName)
                        .addQueryParameter(MOB_NO, phoneNumber)
                        .addQueryParameter("vcity", residentialCity)
                        .addQueryParameter("vEmail", emailId)
                        .addQueryParameter("tBio", bio)
                        .addQueryParameter("vQualification", qualification)
                        .addQueryParameter("vCurrentCompany", currentCompany)
                        .addQueryParameter("vDesignation", designation)
                        .addQueryParameter("vJobLocation", jobLocation)
                        .addQueryParameter("vPreferCity", prefJobLocation)
                        .addQueryParameter("vPreferJobTitle", prefJobTitle)
                        .addQueryParameter("vWorkingMode", prefWorkingMode)
                        .addQueryParameter("tTagLine", "")
                        .addMultipartFile("profilePic", profileImg)
                        .addMultipartFile("resume", resumePdf)
                        .addQueryParameter("fbid", "")
                        .addQueryParameter("googleid", "")
                        .addQueryParameter("tLongitude", prefManager.get(LONGITUDE))
                        .addQueryParameter("tLatitude", prefManager.get(LATITUDE))
                        .addQueryParameter("tAppVersion", versionCodeAndName)

                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            RegisterUserModel::class.java,
                            object : ParsedRequestListener<RegisterUserModel> {
                                override fun onResponse(response: RegisterUserModel?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            CoroutineScope(Dispatchers.IO).launch {

                                                val userDataRepository =
                                                    UserDataRepository(this@InformationActivity)
                                                userDataRepository.storeBasicInfo(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCity
                                                )
                                                userDataRepository.storeAboutData(
                                                    response.data.user.tBio
                                                )
                                                userDataRepository.storeQualificationData(
                                                    response.data.user.vQualification
                                                )
                                                userDataRepository.storeJobPreferenceData(
                                                    response.data.user.vPreferJobTitle,
                                                    response.data.user.vPreferCity,
                                                    response.data.user.vWorkingMode
                                                )

                                                userDataRepository.storeResumeData(
                                                    response.data.user.tResumeUrl
                                                )
                                                userDataRepository.storeProfileImg(
                                                    response.data.user.tProfileUrl
                                                )
                                            }
                                            binding.btnSubmit.visibility = GONE
                                            binding.btnBack.visibility = GONE
                                            prefManager[IS_LOGIN] = true
                                            prefManager[ROLE] = 0
                                            prefManager[USER_ID] = response.data.user.id
                                            prefManager[FIREBASE_ID] =
                                                response.data.user.vFirebaseId
                                            prefManager[AUTH_TOKEN] = response.data.tAuthToken

                                            storeExperienceData(response.data.tAuthToken)
                                            navigateToHomeActivity()

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse Exception: ${e.message}")
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    hideProgressDialog()
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                        )


                                    }
                                }
                            })
                }
                else{
                    AndroidNetworking.upload(NetworkUtils.REGISTER_USER)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addQueryParameter("vFirebaseId", userId)
                        .addQueryParameter("iRole", "0")
                        .addQueryParameter(DEVICE_ID, prefManager.get(DEVICE_ID))
                        .addQueryParameter(DEVICE_TYPE, "0")
                        .addQueryParameter(OS_VERSION, prefManager.get(OS_VERSION))
                        .addQueryParameter(FCM_TOKEN, prefManager.get(FCM_TOKEN))
                        .addQueryParameter(DEVICE_NAME, prefManager.get(DEVICE_NAME))
                        .addQueryParameter("vFirstName", fName)
                        .addQueryParameter("vLastName", lName)
                        .addQueryParameter(MOB_NO, phoneNumber)
                        .addQueryParameter("vcity", residentialCity)
                        .addQueryParameter("vEmail", emailId)
                        .addQueryParameter("tBio", bio)
                        .addQueryParameter("vQualification", qualification)
                        .addQueryParameter("vPreferCity", prefJobLocation)
                        .addQueryParameter("vPreferJobTitle", prefJobTitle)
                        .addQueryParameter("vWorkingMode", prefWorkingMode)
                        .addMultipartFile("profilePic", profileImg)
                        .addMultipartFile("resume", resumePdf)
                        .addQueryParameter("fbid", "")
                        .addQueryParameter("googleid", "")
                        .addQueryParameter("tLongitude", prefManager.get(LONGITUDE))
                        .addQueryParameter("tLatitude", prefManager.get(LATITUDE))
                        .addQueryParameter("tAppVersion", versionCodeAndName)

                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            RegisterUserModel::class.java,
                            object : ParsedRequestListener<RegisterUserModel> {
                                override fun onResponse(response: RegisterUserModel?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            CoroutineScope(Dispatchers.IO).launch {

                                                val userDataRepository =
                                                    UserDataRepository(this@InformationActivity)
                                                userDataRepository.storeBasicInfo(
                                                    response.data.user.vFirstName,
                                                    response.data.user.vLastName,
                                                    response.data.user.vMobile,
                                                    response.data.user.vEmail,
                                                    response.data.user.tTagLine,
                                                    response.data.user.vCity
                                                )
                                                userDataRepository.storeAboutData(
                                                    response.data.user.tBio
                                                )
                                                userDataRepository.storeQualificationData(
                                                    response.data.user.vQualification
                                                )
                                                userDataRepository.storeJobPreferenceData(
                                                    response.data.user.vPreferJobTitle,
                                                    response.data.user.vPreferCity,
                                                    response.data.user.vWorkingMode
                                                )

                                                userDataRepository.storeResumeData(
                                                    response.data.user.tResumeUrl
                                                )
                                                userDataRepository.storeProfileImg(
                                                    response.data.user.tProfileUrl
                                                )
                                            }
                                            binding.btnSubmit.visibility = GONE
                                            binding.btnBack.visibility = GONE
                                            prefManager[IS_LOGIN] = true
                                            prefManager[ROLE] = 0
                                            prefManager[USER_ID] = response.data.user.id
                                            prefManager[FIREBASE_ID] =
                                                response.data.user.vFirebaseId
                                            prefManager[AUTH_TOKEN] = response.data.tAuthToken

                                            navigateToHomeActivity()

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse Exception: ${e.message}")
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    hideProgressDialog()
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                        )


                                    }
                                }
                            })
                }
            }else{
                showNoInternetBottomSheet(this,this)
            }

        }
    }

    private val experienceViewModel: ExperienceViewModel by viewModels()

    private fun storeExperienceData(tAuthToken: String) {

        val jsonObject = JSONObject()
        jsonObject.put("vDesignation", designation)
        jsonObject.put("vCompany",currentCompany)
        jsonObject.put("vJobLocation",jobLocation)
        jsonObject.put("vDuration", "")

        val experienceList:MutableList<Experience> = mutableListOf(Experience(designation!!,currentCompany!!,jobLocation!!,""))

        AndroidNetworking.post(NetworkUtils.INSERT_EXPERIENCE)
            .addHeaders("Authorization", "Bearer $tAuthToken")
            .addJSONObjectBody(
                jsonObject
            )
            .setPriority(Priority.MEDIUM).build()
            .getAsObject(
                CommonMessageModel::class.java,
                object : ParsedRequestListener<CommonMessageModel>{
                    override fun onResponse(response: CommonMessageModel?) {
                        try {
                            response?.let {
                                Log.d(TAG, "onResponse: $jsonObject added in experience list")
                                experienceViewModel.writeToLocal(experienceList.toList())
                                    .invokeOnCompletion {
                                        Log.d(
                                            TAG,
                                            "experienceInfoDialogView: experienceList is updated in datastore"
                                        )
                                    }
                            }
                        }
                        catch (e: Exception) {
                            Log.e("#####", "onResponse Exception: ${e.message}")
                        }

                    }

                    override fun onError(anError: ANError?) {
                        anError?.let {
                            Log.e(
                                "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                            )
                        }
                    }

                }
            )

    }

    private fun storeInfoJBySkip() {

            if (Utils.isNetworkAvailable(this)){
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.post(NetworkUtils.REGISTER_USER)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addQueryParameter("vFirebaseId",userId)
                    .addQueryParameter("iRole","0")
                    .addQueryParameter(MOB_NO,phoneNumber)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",fName)
                    .addQueryParameter("vLastName",lName)
                    .addQueryParameter("vEmail",emailId)
                    .addQueryParameter("vcity",residentialCity)
                    .addQueryParameter("tTagLine",designation)
                    .addQueryParameter("tLongitude",prefManager.get(LONGITUDE))
                    .addQueryParameter("tLatitude",prefManager.get(LATITUDE))
                    .addQueryParameter("tAppVersion",versionCodeAndName)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        RegisterUserModel::class.java,
                        object : ParsedRequestListener<RegisterUserModel> {
                            override fun onResponse(response: RegisterUserModel?) {
                                try {
                                    response?.let {
                                        hideProgressDialog()
                                        CoroutineScope(Dispatchers.IO).launch {

                                            val userDataRepository =
                                                UserDataRepository(this@InformationActivity)
                                            userDataRepository.storeBasicInfo(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                response.data.user.tTagLine,
                                                response.data.user.vCity
                                            )

                                        }

                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[ROLE] = 0
                                        prefManager[USER_ID] = response.data.user.id
                                        prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                        prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                        navigateToHomeActivity()

                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                }
                            }

                            override fun onError(anError: ANError?) {
                                hideProgressDialog()
                                anError?.let {
                                    Log.e(
                                        "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )


                                }
                            }
                        })
            }else{
                showNoInternetBottomSheet(this,this)
            }


    }
    private fun inputFieldConformationJ(
        qualification: String,
        bio: String,
        currentCompany: String,
        designation: String,
        jobLocation: String,
        prefJobTitle: String,
        prefJobLocation: String,
        prefWorkingMode: String,
    ): Boolean {

        if (qualification.isEmpty()){
            binding.spQualificationJ.errorText = "Select your Qualification"
            binding.jsViewFlipper.displayedChild =
                binding.jsViewFlipper.indexOfChild(binding.jsAboutGrp)
            changeLayout(false)
            return  false
        }
        if (bio.length > 5000) {
            binding.bio.error = "bio length should not be exited to 5000"
            binding.jsViewFlipper.displayedChild =
                binding.jsViewFlipper.indexOfChild(binding.jsAboutGrp)
            changeLayout(false)
            return false
        }

        if(experienced) {
            if (currentCompany.isEmpty()){
                binding.companyName.error = "Enter your current company name"
                binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsExperienceGrp)
                changeLayout(false)
                return false
            }
            if(designation.isEmpty()){
                binding.spDesignationJ.errorText = "Select your designation"
                binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsExperienceGrp)
                changeLayout(false)
                return false
            }
            if(jobLocation.isEmpty()){
                binding.spDesignationJ.errorText = "Enter your job location"
                binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsExperienceGrp)
                changeLayout(false)
                return false
            }
        }

        if(prefJobTitle.isEmpty()) {
            binding.spPrefJobTitleJ.errorText = "Select your prefer job title"
            binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsPreferJobGrp)
            changeLayout(false)
            return false
        }
        if(prefJobLocation.isEmpty()){
            binding.spPrefCityJ.errorText = "Select your prefer job location"
            binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsPreferJobGrp)
            changeLayout(false)
            return false
        }

        if(prefWorkingMode.isEmpty()){
            makeToast("select one working mode",0)
            binding.jsViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.jsPreferJobGrp)
            changeLayout(false)
            return false
        }

        return true

    }



    private fun storeInfoR() {
        qualification = selectedQualification.trim()
        bio = binding.bioR.text.toString().trim()

        currentCompany = binding.companyNameR.text.toString().trim()
        designation = selectedDesignation.trim()
        jobLocation = selectedJobLocation.trim()

        /*workingMode = getSelectedRadioItem(binding.radioGrpWorkingModeR)*/
        val correct = inputFieldConformationR(qualification!!,bio!!,currentCompany!!,designation!!,jobLocation!!,workingMode!!)
        if (!correct) return
        else{
            if (Utils.isNetworkAvailable(this)){
                Log.d(TAG, "storeInfoJ: $residentialCity $jobLocation")
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.post(NetworkUtils.REGISTER_USER)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addQueryParameter("vFirebaseId",userId)
                    .addQueryParameter("iRole","1")
                    .addQueryParameter(MOB_NO,phoneNumber)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",fName)
                    .addQueryParameter("vLastName",lName)
                    .addQueryParameter("vEmail",emailId)
                    .addQueryParameter("tBio",bio)
                    .addQueryParameter("vcity",residentialCity)
                    .addQueryParameter("vCurrentCompany",currentCompany)
                    .addQueryParameter("vDesignation",designation)
                    .addQueryParameter("vQualification",qualification)
                    .addQueryParameter("vJobLocation",jobLocation)
                    .addQueryParameter("vWorkingMode",workingMode)
                    .addQueryParameter("tTagLine","")
                    .addQueryParameter("fbid","")
                    .addQueryParameter("googleid","")
                    .addQueryParameter("tLongitude",prefManager.get(LONGITUDE))
                    .addQueryParameter("tLatitude",prefManager.get(LATITUDE))
                    .addQueryParameter("tAppVersion",versionCodeAndName)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        RegisterUserModel::class.java,
                        object : ParsedRequestListener<RegisterUserModel> {
                            override fun onResponse(response: RegisterUserModel?) {
                                try {
                                    response?.let {
                                        hideProgressDialog()

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val userDataRepository = UserDataRepository(this@InformationActivity)
                                            userDataRepository.storeBasicInfo(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                response.data.user.tTagLine,
                                                response.data.user.vCity
                                            )
                                            userDataRepository.storeAboutData(
                                                response.data.user.tBio,
                                            )
                                            userDataRepository.storeCurrentPositionData(
                                                response.data.user.vCurrentCompany,
                                                response.data.user.vDesignation,
                                                response.data.user.vJobLocation,
                                                response.data.user.vWorkingMode
                                            )
                                            userDataRepository.storeProfileImg(
                                                response.data.user.tProfileUrl
                                            )

                                        }
                                        prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[USER_ID] = response.data.user.id
                                        prefManager[ROLE] = 1
                                        prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                        navigateToHomeActivity()

                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                }
                            }

                            override fun onError(anError: ANError?) {
                                hideProgressDialog()
                                anError?.let {
                                    Log.e(
                                        "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )
                                }
                            }
                        })
            }else{
                showNoInternetBottomSheet(this,this)
            }

        }
    }
    private fun storeInfoRSkip() {

            if (Utils.isNetworkAvailable(this)){
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.post(NetworkUtils.REGISTER_USER)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addQueryParameter("vFirebaseId",userId)
                    .addQueryParameter("iRole","1")
                    .addQueryParameter(MOB_NO,phoneNumber)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",fName)
                    .addQueryParameter("vLastName",lName)
                    .addQueryParameter("vEmail",emailId)
                    .addQueryParameter("tBio",bio)
                    .addQueryParameter("vcity",residentialCity)
                    .addQueryParameter("fbid","")
                    .addQueryParameter("googleid","")
                    .addQueryParameter("tLongitude",prefManager.get(LONGITUDE))
                    .addQueryParameter("tLatitude",prefManager.get(LATITUDE))
                    .addQueryParameter("tAppVersion",versionCodeAndName)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        RegisterUserModel::class.java,
                        object : ParsedRequestListener<RegisterUserModel> {
                            override fun onResponse(response: RegisterUserModel?) {
                                try {
                                    response?.let {
                                        hideProgressDialog()
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val userDataRepository = UserDataRepository(this@InformationActivity)
                                            userDataRepository.storeBasicInfo(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                response.data.user.tTagLine,
                                                response.data.user.vCity
                                            )
                                        }
                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[USER_ID] = response.data.user.id
                                        prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                        prefManager[ROLE] = 1
                                        prefManager[AUTH_TOKEN] = response.data.tAuthToken
                                        navigateToHomeActivity()

                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                }
                            }

                            override fun onError(anError: ANError?) {
                                hideProgressDialog()
                                anError?.let {
                                    Log.e(
                                        "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )


                                }
                            }
                        })
            }else{
                showNoInternetBottomSheet(this,this)
            }


    }
    private fun inputFieldConformationR(
        qualification: String,
        bio: String,
        currentCompany: String,
        designation: String,
        jobLocation: String,
        workingMode: String
    ): Boolean {

        if(qualification.isEmpty()){
            binding.spQualificationR.errorText = "Select a qualification"
            binding.rViewFlipper.displayedChild = binding.rViewFlipper.indexOfChild(binding.rAboutGrp)
            changeLayout(false)
        }
        if (bio.length > 5000){
            binding.bioR.error = "Job Description Length Should not exited to 5000"
            binding.rViewFlipper.displayedChild = binding.rViewFlipper.indexOfChild(binding.rAboutGrp)
            changeLayout(false)
            return false
        }
        if(currentCompany.isEmpty()) {
            binding.companyName.error = "Enter current company data"
            binding.rViewFlipper.displayedChild = binding.rViewFlipper.indexOfChild(binding.rCurrPosGrp)
            changeLayout(false)
            return false
        }
        if(designation.isEmpty()) {
            binding.spDesignationR.errorText = "Select your designation"
            binding.rViewFlipper.displayedChild = binding.rViewFlipper.indexOfChild(binding.rCurrPosGrp)
            changeLayout(false)
            return false
        }
        if(jobLocation.isEmpty()){
            binding.spJobLocationR.errorText = "Select your job location"
            binding.rViewFlipper.displayedChild = binding.jsViewFlipper.indexOfChild(binding.rCurrPosGrp)
            changeLayout(false)
            return false
        }
        if(workingMode.isEmpty()){

            makeToast("Select a working mode",0)
            binding.rViewFlipper.displayedChild = binding.rViewFlipper.indexOfChild(binding.rCurrPosGrp)
            changeLayout(false)
            return false
        }
       return true
    }

    private fun navigateToHomeActivity() {
        if (userType == JOB_SEEKER) {
            val intent = Intent(this@InformationActivity, HomeJobSeekerActivity::class.java)
            intent.putExtra("role", userType)
            val fullName = fName + lName
            makeToast("Welcome $fullName", 0)
            intent.putExtra("name", fullName)
            intent.putExtra("userId",userId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        if (userType == RECRUITER){
            val intent = Intent(this@InformationActivity, HomeRecruiterActivity::class.java)/** need **/
            intent.putExtra("role", userType)
            val fullName = fName + lName
            makeToast("Welcome $fullName", 0)
            intent.putExtra("name", fullName)
            intent.putExtra("userId",userId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    /*private fun changeLayout(layoutID: Int, btnPointer: Int) {
        if (layoutID == 0){
            if(btnPointer == 0){
                binding.recruiterLayout1.visibility = VISIBLE
                binding.recruiterLayout2.visibility = GONE
                binding.btnNext.visibility = VISIBLE
                binding.btnBack.visibility = GONE
                binding.btnSubmit.visibility = GONE

                binding.check1.setBackgroundResource(R.color.check_color)
                binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check2.setBackgroundResource(R.color.check_def_color)
                binding.check3.setBackgroundResource(R.color.check_def_color)
                binding.check4.setBackgroundResource(R.color.check_def_color)
            }
            if (btnPointer == 1){
                binding.recruiterLayout2.visibility = VISIBLE
                binding.recruiterLayout1.visibility = GONE
                binding.btnNext.visibility = GONE
                binding.btnBack.visibility = VISIBLE
                binding.btnSubmit.visibility = VISIBLE

                binding.check1.setBackgroundResource(R.color.check_color)
                binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check2.setBackgroundResource(R.color.check_color)
                binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check3.setBackgroundResource(R.color.check_def_color)
                binding.check4.setBackgroundResource(R.color.check_def_color)
            }
        }
        if (layoutID == 1){
            if (btnPointer == 0){
                binding.jsLayout1.visibility = VISIBLE
                binding.jsLayout2.visibility = GONE
                binding.jsLayout3.visibility = GONE
                binding.jsSubLayout.visibility = GONE
                binding.btnBack.visibility = GONE
                binding.btnNext.visibility = VISIBLE
                binding.btnSubmit.visibility = GONE

                binding.check1.setBackgroundResource(R.color.check_color)
                binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check2.setBackgroundResource(R.color.check_def_color)
                binding.check3.setBackgroundResource(R.color.check_def_color)
                binding.check4.setBackgroundResource(R.color.check_def_color)
            }
            else if (btnPointer == 1 ) {

                experience = getSelectedRadioItem(binding.radioGrpFreshExp)
                if(experience == "Experienced") {
//                    makeToast("You Selected $experience",0)
//                    makeToast("press next for further step",0)
                    binding.jsLayout1.visibility = GONE
                    binding.jsLayout2.visibility = GONE
                    binding.jsSubLayout.visibility = VISIBLE
                    //                    jsSubLayout2.visibility = GONE
                    binding.jsLayout3.visibility = GONE
                    binding.btnBack.visibility = VISIBLE
                    binding.btnNext.visibility = VISIBLE
                    binding.btnSubmit.visibility = GONE
                    binding.check1.setBackgroundResource(R.color.check_color)
                    binding.check1.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_check
                    ));
                    binding.check2.setBackgroundResource(R.color.check_def_color)
                    binding.check3.setBackgroundResource(R.color.check_def_color)
                    binding.check4.setBackgroundResource(R.color.check_def_color)
                }
                if (experience == "Fresher"){
                    binding.jsLayout1.visibility = GONE
                    binding.jsSubLayout.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                    binding.jsLayout2.visibility = VISIBLE
                    binding.jsLayout3.visibility = GONE
                    binding.btnBack.visibility = VISIBLE
                    binding.btnNext.visibility = VISIBLE
                    binding.btnSubmit.visibility = GONE
                    binding.check1.setBackgroundResource(R.color.check_color)
                    binding.check1.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_check
                    ));
                    binding.check2.setBackgroundResource(R.color.check_color)
                    binding.check2.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_check
                    ));
                    binding.check3.setBackgroundResource(R.color.check_def_color)
                    binding.check4.setBackgroundResource(R.color.check_def_color)
                }
            }

            else if (btnPointer == 2){
                binding.jsLayout1.visibility = GONE
                binding.jsSubLayout.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                binding.jsLayout2.visibility = VISIBLE
                binding.jsLayout3.visibility = GONE
                binding.btnBack.visibility = VISIBLE
                binding.btnNext.visibility = VISIBLE
                binding.btnSubmit.visibility = GONE

                binding.check1.setBackgroundResource(R.color.check_color)
                binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check2.setBackgroundResource(R.color.check_color)
                binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check3.setBackgroundResource(R.color.check_def_color)
                binding.check4.setBackgroundResource(R.color.check_def_color)
            }
            else if (btnPointer == 3){
                binding.jsLayout1.visibility = GONE
                binding.jsSubLayout.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                binding.jsLayout2.visibility = GONE
                binding.jsLayout3.visibility = VISIBLE
                binding.btnBack.visibility = VISIBLE
                binding.btnNext.visibility = GONE
                binding.btnSubmit.visibility = GONE
                binding.uploadBtn.visibility = VISIBLE
                binding.uploadProgressBar.visibility = GONE

                binding.check1.setBackgroundResource(R.color.check_color)
                binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check2.setBackgroundResource(R.color.check_color)
                binding.check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check3.setBackgroundResource(R.color.check_color)
                binding.check3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.check4.setBackgroundResource(R.color.check_def_color)
            }
        }
    }*/

    private fun getSelectedRadioItem(radioGroup: RadioGroup): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        if (selectedItemId != -1) {
            val radioButton = findViewById<View>(selectedItemId) as RadioButton
//            makeToast(radioButton.text.toString(),0)
            return radioButton.text.toString().trim()
        }
        return "not Selected"
    }
    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            makeToast("backPressed",0)

            val alterDialog  = AlertDialog.Builder(this@InformationActivity)
                .setTitle("Alert!!!")
                .setIcon(R.drawable.ic_alert)
                .setMessage("Your Phone Number is Registered.But your data will save..")
                .setPositiveButton("Continue"){ dialog, _ ->
                    startActivity(Intent(this@InformationActivity, RegistrationActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton("Back"){dialog,_ ->
                    dialog.dismiss()
                }
                .create()
            alterDialog.show()

            //moveTaskToBack(false);
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    /*private fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)
                                prefLocations.addAll(response.data)

                                hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                                hideProgressDialog()

                            }


                        }
                    })
        }else{
            showNoInternetBottomSheet(this,this)
        }

    }*/

}