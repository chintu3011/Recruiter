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
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.amri.emploihunt.BuildConfig
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityInformationBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
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
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Stack


class InformationActivity : BaseActivity() ,OnClickListener, AdapterView.OnItemSelectedListener{



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
    var cityList: ArrayList<String> = ArrayList()
    var prefLocations: ArrayList<String> = ArrayList()


    private var selectedQualification = String()
    private var selectedJobLocation = String()
    private var selectedPreJobLocation = String()
    private var selectedDesignation = String()
    private var selectedPrefJobTitle = String()
    private var selectedPrefCity = String()


    private var selectedQualificationR = String()


    private var isSkip: Boolean = false
    private lateinit var resumePdf: File


    private lateinit var nextStack:Stack<View>
    private lateinit var backStack:Stack<View>
    private lateinit var checkStack:Stack<ShapeableImageView>
    private lateinit var jGroupArray:ArrayList<View>
    private lateinit var rGroupArray:ArrayList<View>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        jobs = resources.getStringArray(R.array.indian_designations)
        qualifications = resources.getStringArray(R.array.degree_array)
        prefManager = prefManager(this@InformationActivity)

        nextStack = Stack()
        backStack = Stack()
        checkStack = Stack()
        jGroupArray = arrayListOf(binding.jsAboutGrp,binding.jsFreshExpGrp,binding.jsCurPosGrp,binding.jsPreferJobGrp,binding.jsResumeGrp,binding.profilImgGrp)

        rGroupArray = arrayListOf(binding.rAboutGrp,binding.rCurrPosGrp,binding.profilImgGrp)

        binding.check1.visibility = VISIBLE
        binding.check1.setBackgroundResource(R.color.blue)
        binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check))

        checkStack.push(binding.check1)

        setOnClickListener()
        userType = intent.getIntExtra("role",0)
        setLayout(userType!!)

        getAllCity()
        cityList.add("City")
        prefLocations.add("City")
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
                /*binding.inputLayoutJobSeeker.visibility = GONE*/
                layoutID = 0
                binding.rAboutGrp.visibility = VISIBLE
                grpPointer++
                /*binding.recruiterLayout2.visibility = GONE*/

                binding.btnNext.visibility = VISIBLE
                binding.btnBack.visibility = GONE

                binding.check2.visibility = VISIBLE
                binding.check2.setBackgroundResource(R.color.blue)


                binding.check3.visibility = VISIBLE
                binding.check4.visibility = VISIBLE

           /*     binding.check3.setBackgroundResource(R.color.check_def_color)
                binding.check4.visibility = GONE

                binding.check4.setBackgroundResource(R.color.check_def_color)*/
            }
            JOB_SEEKER -> {
                binding.inputLayoutJobSeeker.visibility = VISIBLE
                /*binding.inputLayoutRecruiter.visibility = GONE*/
                layoutID = 1
                /*binding.jsLayout1.visibility = VISIBLE
                binding.jsSubLayout.visibility = GONE
                binding.jsLayout2.visibility = GONE
                binding.jsLayout3.visibility = GONE*/
                jGroupArray[grpPointer].visibility = VISIBLE
                grpPointer++
                binding.btnNext.visibility = VISIBLE
                binding.btnBack.visibility = GONE


                binding.check2.visibility = VISIBLE
                binding.check2.setBackgroundResource(R.color.blue)

                binding.check3.visibility = VISIBLE
                /*binding.check3.setBackgroundResource(R.color.check_def_color)*/
                binding.check4.visibility = VISIBLE
                /*binding.check4.setBackgroundResource(R.color.check_def_color)*/
                binding.check5.visibility = VISIBLE

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

    private fun setOnClickListener() {
        binding.btnSelectPdf.setOnClickListener(this)
        binding.uploadBtn.setOnClickListener(this)
        binding.addProfileImg.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.btnExperienced.setOnClickListener(this)
        binding.btnFresher.setOnClickListener(this)

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

        binding.spJobLocationJ.setSearchDialogGravity(Gravity.TOP)
        binding.spJobLocationJ.arrowPaddingRight = 19
        binding.spJobLocationJ.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spJobLocationJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spJobLocationJ.isOutlined = true
                selectedJobLocation = binding.spJobLocationJ.item[position].toString()
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

        binding.spPrefCityJ.setSearchDialogGravity(Gravity.TOP)
        binding.spPrefCityJ.arrowPaddingRight = 19
        binding.spPrefCityJ.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spPrefCityJ.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spPrefCityJ.isOutlined = true
                selectedPrefCity = binding.spPrefCityJ.item[position].toString()
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

        binding.spJobLocationR.setSearchDialogGravity(Gravity.TOP)
        binding.spJobLocationR.arrowPaddingRight = 19
        binding.spJobLocationR.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spJobLocationR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spJobLocationR.isOutlined = true
                selectedPrefJobTitle = binding.spJobLocationR.item[position].toString()
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.uploadBtn -> {
                /*uploadProgressBar.visibility = VISIBLE
                uploadProgressBar.progress = 70
                val mStorage = FirebaseStorage.getInstance().getReference("pdfs")
                val pdfRef = mStorage.child(pdfName)
                pdfUri?.let {
                    pdfRef.putFile(it).addOnSuccessListener {
                        pdfRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            uploadProgressBar.progress = 100
                            resume = downloadUrl.toString()
                        }
                    }
                }*/
                binding.uploadBtn.visibility = GONE
                /*binding.btnSubmit.visibility = VISIBLE*/
            }
            R.id.btnSelectPdf -> {
                  selectpdf()
            }


            R.id.btnSubmit -> {
                if(userType == RECRUITER) storeInfoR()
                if (userType == JOB_SEEKER) storeInfoJ()
            }
            R.id.btnExperienced -> {

            }
            R.id.btnFresher -> {
                btnPointer ++
            }
            R.id.btnNext -> {
                makeToast(grpPointer.toString(),0)
                /*btnPointer += 1*/
                /*changeLayout(layoutID,btnPointer)*/
                if(userType == JOB_SEEKER){
                    if(grpPointer < jGroupArray.size) {
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = VISIBLE
                        jGroupArray[grpPointer - 1].visibility = GONE
                        jGroupArray[grpPointer++].visibility = VISIBLE
                        if(grpPointer == jGroupArray.size-1){
                            binding.btnNext.visibility = GONE
                        }
                        if(grpPointer == jGroupArray.size){
                            binding.btnNext.visibility = GONE
                            binding.btnBack.visibility = VISIBLE
                            grpPointer -= 2
                        }
                        /*if(grpPointer == 1){
                            binding.btnNext.visibility = GONE
                        }*/
                    }
                    if(grpPointer == jGroupArray.size){
                        binding.btnNext.visibility = GONE
                        binding.btnBack.visibility = VISIBLE
                        grpPointer -= 2
                    }
                }
                else if (userType == RECRUITER){
                    if(grpPointer < rGroupArray.size){
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = VISIBLE
                        rGroupArray[grpPointer-1].visibility = GONE
                        rGroupArray[grpPointer++].visibility = VISIBLE
                        if(grpPointer == rGroupArray.size-1){
                            binding.btnNext.visibility = GONE
                            grpPointer--
                        }
                    }
                    else if(grpPointer == rGroupArray.size){
                        binding.btnNext.visibility = GONE
                        grpPointer--
                    }
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }
            }
            R.id.btnBack -> {
                makeToast(grpPointer.toString(),0)
                /*btnPointer -= 1*/
               /* changeLayout(layoutID,btnPointer)*/
                if(userType == JOB_SEEKER){
                    if(grpPointer > 0) {
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = VISIBLE
                        jGroupArray[grpPointer + 1].visibility = GONE
                        jGroupArray[grpPointer--].visibility = VISIBLE
                        if(grpPointer == 0){
                            binding.btnBack.visibility = GONE
                        }
                    }
                    if(grpPointer == 0){
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = GONE
                        grpPointer += 2
                    }
                }
                else if (userType == RECRUITER){
                    if(grpPointer >= 0){
                        binding.btnNext.visibility = VISIBLE
                        binding.btnBack.visibility = VISIBLE
                        rGroupArray[grpPointer + 1].visibility = GONE
                        rGroupArray[grpPointer--].visibility = VISIBLE
                        if(grpPointer == 0){
                            binding.btnBack.visibility = GONE
                            grpPointer++
                        }
                    }
                }
                else{
                    makeToast(getString(R.string.something_error),0)
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
            }
        }

    }

    private fun storeInfoJ() {

        qualification = selectedQualification.trim()
        bio = binding.bio.text.toString().trim()
        currentCompany = binding.companyName.text.toString().trim()
        designation = selectedDesignation.trim()
        jobLocation = selectedJobLocation.trim()

        prefWorkingMode = getSelectedRadioItem(binding.radioGrpWorkingMode)
        prefJobTitle = selectedPrefJobTitle.toString().trim()
        prefJobLocation = selectedPreJobLocation.toString().trim()

        val correct = inputFieldConformationJ(bio!!)
        if (!correct) return
        else{
            if (Utils.isNetworkAvailable(this)){
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.upload(NetworkUtils.REGISTER_USER)
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
                    .addQueryParameter("tBio",bio)
                    .addQueryParameter("vQualification",qualification)
                    .addQueryParameter("vcity",residentialCity)
                    .addQueryParameter("vCurrentCompany",currentCompany)
                    .addQueryParameter("vDesignation",designation)
                    .addQueryParameter("vJobLocation",jobLocation)
                    /*.addQueryParameter("vDuration",duration)*/
                    .addQueryParameter("vPreferCity",prefJobLocation)
                    .addQueryParameter("vPreferJobTitle",prefJobTitle)
                    .addQueryParameter("vWorkingMode",prefWorkingMode)
                    /*.addQueryParameter("vExpectedSalary",salary)*/
                    .addQueryParameter("tTagLine",designation)
                    .addQueryParameter("fbid","")
                    .addQueryParameter("googleid","")
                    .addQueryParameter("tLongitude",prefManager.get(LONGITUDE))
                    .addQueryParameter("tLatitude",prefManager.get(LATITUDE))
                    .addQueryParameter("tAppVersion",versionCodeAndName)
                    .addQueryParameter("profilePic",profileImgUri)/***/
                    .addMultipartFile("resume",resumePdf)
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
                                                response.data.user.tBio,
                                                /*response.data.user.vQualification*/
                                            )
                                            userDataRepository.storeQualificationData(
                                                response.data.user.vQualification
                                            )
                                            /*userDataRepository.storeExperienceData(
                                                experience,
                                                response.data.user.vDesignation,
                                                "",
                                                ""
                                            )*/
                                            userDataRepository.storeResumeData(
                                                resumeUri!!
                                            )
                                            userDataRepository.storeJobPreferenceData(
                                                response.data.user.vPreferJobTitle,
                                                response.data.user.vPreferCity,
                                                response.data.user.vWorkingMode
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
        bio: String,
    ): Boolean {

        if (bio.length > 5000 ) {
            binding.bio.error = "bio length should not be exited to 5000"
            return false
        }
        /*if (!isNumeric(expectedSalary)){
            binding.salary.error = "Invalid Salary"
        }*/
        return true

    }

    private fun isNumeric(input: String): Boolean {
        return try {
            input.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun storeInfoR() {
        qualification = selectedQualification.trim()
        bio = binding.bioR.text.toString().trim()

        currentCompany = binding.companyNameR.text.toString().trim()
        designation = selectedDesignation.trim()
        jobLocation = selectedJobLocation.trim()
        workingMode = getSelectedRadioItem(binding.radioGrpWorkingModeR)
        val correct = inputFieldConformationR(bio!!)
        if (!correct) return
        else{
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
                    /*.addQueryParameter("vPreferCity","")*/
                    .addQueryParameter("vcity",residentialCity)
                    .addQueryParameter("vCurrentCompany",currentCompany)
                    .addQueryParameter("vDesignation",designation)
                    .addQueryParameter("vQualification",qualification)
                    .addQueryParameter("vJobLocation",jobLocation)
                    .addQueryParameter("vWorkingMode",workingMode)
                    .addQueryParameter("tTagLine",designation)
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
    private fun inputFieldConformationR(jobDes: String): Boolean {
        if (jobDes.length > 5000){
            binding.bioR.error = "Job Description Length Should not exited to 5000"
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
    private fun getAllCity(){

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

    }

}