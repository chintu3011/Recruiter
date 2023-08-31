package com.amri.emploihunt.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.DEVICE_ID
import com.amri.emploihunt.util.DEVICE_NAME
import com.amri.emploihunt.util.DEVICE_TYPE
import com.amri.emploihunt.util.FCM_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.LATITUDE
import com.amri.emploihunt.util.LONGITUDE
import com.amri.emploihunt.util.MOB_NO
import com.amri.emploihunt.util.OS_VERSION
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.convertUriToPdfFile
import com.amri.emploihunt.util.Utils.showNoInternetBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class InformationActivity : BaseActivity() ,OnClickListener, AdapterView.OnItemSelectedListener{


    private lateinit var binding: ActivityInformationBinding
    private lateinit var prefManager: SharedPreferences
    
    private var userType = String()

    private var layoutID = -1
    private var btnPointer = 0

    private var pdfUri:Uri ?= null

    private var firstName = String()
    private var lastName = String()
    private var phoneNo = String()
    private var email = String()
    private var city = String()
    private var userId = String()
    private var qualification = String()
    private var bio = String()
    private var experience = String()
    private var companyName = String()
    private var designation = String()
    private var jobLocation = String()
    private var duration = String()
    private var salary = String()
    private var pCity = String()
    private var workingMode = String()
    private var jobTitle = String()
    private var jobDes = String()
    private var resume = String()
    private var termsConditionsAcceptance = String()
    private var pdfName = String()



    lateinit  var  qualifications:kotlin.Array<String>
    lateinit  var  jobs:kotlin.Array<String>
    var cityList: ArrayList<String> = ArrayList()
    var prefLocations: ArrayList<String> = ArrayList()

    private var selectedQualification = String()
    private var selectedJobLocation = String()
    private var selectedPreJobLocation = String()
    private var selectedJob = String()
    private var selectedQualificationR = String()

    private var isSkip: Boolean = false
    private lateinit var resumePdf: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        jobs = resources.getStringArray(R.array.indian_designations)
        qualifications = resources.getStringArray(R.array.degree_array)
        prefManager = prefManager(this@InformationActivity)
        val window: Window = this@InformationActivity.window
        val background = ContextCompat.getDrawable(this@InformationActivity,
            R.drawable.status_bar_color
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@InformationActivity,R.color.colorPrimary)
        window.navigationBarColor = ContextCompat.getColor(this@InformationActivity,android.R.color.white)

        
        setOnClickListener()
        userType = intent.getStringExtra("userType").toString()
        setLayout(userType)

        getAllCity()
        cityList.add("City")
        prefLocations.add("City")
        setAdapters()


    }

    private fun setAdapters() {

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
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedJobLocation = cityList[position]


        Log.d("###", "onItemSelected: $selectedJobLocation $selectedPreJobLocation $position")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun setLayout(userType: String) {

        if (userType == "Recruiter") {
            binding.inputLayoutRecruiter.visibility = VISIBLE
            layoutID = 0
            binding.recruiterLayout1.visibility = VISIBLE
            binding.recruiterLayout2.visibility = GONE
            binding.btnNext.visibility = VISIBLE
            binding.btnBack.visibility = GONE
            binding.check1.visibility = VISIBLE
            binding.check1.setBackgroundResource(R.color.blue)
            binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
            binding.check2.visibility = VISIBLE
            binding.check2.setBackgroundResource(R.color.check_def_color)
            binding.check3.visibility = VISIBLE
            binding.check3.setBackgroundResource(R.color.check_def_color)
            binding.check4.visibility = GONE
            binding.check4.setBackgroundResource(R.color.check_def_color)
        }
        if(userType == "Job Seeker"){
            binding.inputLayoutJobSeeker.visibility = VISIBLE
            layoutID = 1
            binding.jsLayout1.visibility = VISIBLE
            binding.jsSubLayout.visibility = GONE
            binding.jsLayout2.visibility = GONE
            binding.jsLayout3.visibility = GONE
            binding.btnNext.visibility = VISIBLE
            binding.btnBack.visibility = GONE
            binding.check1.visibility = VISIBLE
            binding.check1.setBackgroundResource(R.color.blue)
            binding.check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
            binding.check2.visibility = VISIBLE
            binding.check2.setBackgroundResource(R.color.check_def_color)
            binding.check3.visibility = VISIBLE
            binding.check3.setBackgroundResource(R.color.check_def_color)
            binding.check4.visibility = VISIBLE
            binding.check4.setBackgroundResource(R.color.check_def_color)
        }
        btnPointer = 0
        userId = intent.getStringExtra("uid").toString().trim()
        firstName = intent.getStringExtra("fName").toString().trim()
        lastName = intent.getStringExtra("lName").toString().trim()
        phoneNo = intent.getStringExtra("phoneNo").toString().trim()
        email = intent.getStringExtra("email").toString().trim()
        city = intent.getStringExtra("city").toString().trim()
        termsConditionsAcceptance = intent.getStringExtra("termsConditions").toString().trim()

    }

    private fun setOnClickListener() {
        binding.btnSelectPdf.setOnClickListener(this)
        binding.uploadBtn.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnNext.setOnClickListener(this)
        binding.btnSkip.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        binding.JobLocationSpinnerR.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        }


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
                binding.btnSubmit.visibility = VISIBLE
            }
            R.id.btnSelectPdf -> {
                  selectpdf()
            }

            R.id.btnBack -> {
                btnPointer -= 1
                changeLayout(layoutID,btnPointer)
//                makeToast("$btnPointer",0)
            }
            R.id.btnSubmit -> {

                if(userType == "Recruiter") storeInfoR()
                if (userType == "Job Seeker") storeInfoJ()

            }
            R.id.btnNext -> {
                btnPointer += 1
                changeLayout(layoutID,btnPointer)
//                makeToast("$btnPointer",0)
            }
            R.id.btnSkip -> {

                if (userType == "Job Seeker"){

                    storeInfoJBySkip()
                }
                if(userType == "Recruiter"){
                    storeInfoRSkip()
                }

/*                val  map = mutableMapOf<String,String>()
                map["userId"] = userId
                map["userFName"] = firstName
                map["userLName"] = firstName
                map["userPhoneNUmber"] = phoneNo
                map["userEmailId"] = email

                FirebaseDatabase.getInstance().getReference("Users")
                    .child(userType)
                    .child(userId)
                    .setValue(map).addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            if(userType == "Job Seeker"){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val jobSeekerProfileInfo =
                                        JobSeekerProfileInfo(this@InformationActivity)

                                    jobSeekerProfileInfo.storeBasicProfileData(
                                        firstName,
                                        lastName,
                                        phoneNo,
                                        email,
                                        "",
                                        ""
                                    )
                                    jobSeekerProfileInfo.storeUserType(
                                        userType,
                                        userId
                                    )
                                }
                                navigateToHomeActivity()
                            }
                            if(userType == "Recruiter"){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val recruiterProfileInfo =
                                        RecruiterProfileInfo(this@InformationActivity)

                                    recruiterProfileInfo.storeBasicProfileData(
                                        firstName,
                                        lastName,
                                        phoneNo,
                                        email,
                                        "",
                                        ""
                                    )
                                    recruiterProfileInfo.storeUserType(
                                        userType,
                                        userId
                                    )
                                }
                                navigateToHomeActivity()
                            }

                        }
                    }*/
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
                    pdfUri = data?.data!!
                    val uri: Uri = data.data!!
                    val uriString: String = uri.toString()
                    resumePdf = convertUriToPdfFile(this@InformationActivity,uri)!!


                    pdfName = null.toString()
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
                                pdfName =
                                    myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                binding.textPdfName.text = pdfName
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

        qualification = selectedQualification.toString().trim()
        bio = binding.bio.text.toString().trim()
        experience = getSelectedRadioItem(binding.radioGrpFreshExp).toString()
        companyName = binding.companyName.text.toString().trim()
        designation = binding.designation.text.toString().trim()
        jobLocation = selectedJobLocation.toString().trim()
        duration = binding.duration.text.toString().trim()
        salary = binding.salary.text.toString().trim().plus(" LPA+")
        if(salary.isEmpty()){
            salary = 0.toString()
        }
        workingMode = getSelectedRadioItem(binding.radioGrpWorkingMode).toString()
        jobTitle = selectedJob.toString().trim()
        pCity = selectedPreJobLocation.toString().trim()

        val correct = inputFieldConformationJ(bio,salary)
        if (!correct) return
        else{
           /* progressBar.visibility = VISIBLE
            val user = UsersJobSeeker(
                userId,
                firstName,
                lastName,
                phoneNo,
                email,
                "",
                "",
                "",
                companyName,
                bio,
                qualification,
                experience,
                designation,
                companyName,
                duration,
                resume,
                pdfName,
                jobTitle,
                salary,
                city,
                workingMode,
            )

            FirebaseDatabase.getInstance().getReference("Users")
                .child(userType)
                .child(userId)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val jobSeekerProfileInfo = JobSeekerProfileInfo(this@InformationActivity)

                            jobSeekerProfileInfo.storeBasicProfileData(
                                firstName,
                                lastName,
                                phoneNo,
                                email,
                                "",
                                companyName
                            )
                            jobSeekerProfileInfo.storeAboutData(
                                bio,
                                qualification
                            )
                            jobSeekerProfileInfo.storeExperienceData(
                                experience,
                                designation,
                                companyName,
                                duration
                            )
                            jobSeekerProfileInfo.storeResumeData(
                                pdfName,
                                pdfUri.toString()
                            )
                            jobSeekerProfileInfo.storeJobPreferenceData(
                                jobTitle,
                                salary,
                                city,
                                workingMode
                            )
                            jobSeekerProfileInfo.storeUserType(
                                userType,
                                userId
                            )
                        }
                        makeToast("Data stored successfully",1)
                        navigateToHomeActivity()
                    } else {
                        makeToast("Try again",1)
                    }
                    check4.setBackgroundResource(R.color.check_color)
                    progressBar.visibility = GONE
                }*/
            if (Utils.isNetworkAvailable(this)){
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.upload(NetworkUtils.REGISTER_USER)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addQueryParameter("vFirebaseId",userId)
                    .addQueryParameter("iRole","0")
                    .addQueryParameter(MOB_NO,phoneNo)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",firstName)
                    .addQueryParameter("vLastName",lastName)
                    .addQueryParameter("vEmail",email)
                    .addQueryParameter("tBio",bio)
                    .addQueryParameter("vcity",city)
                    .addQueryParameter("vCurrentCompany",companyName)
                    .addQueryParameter("vDesignation",designation)
                    .addQueryParameter("vJobLocation",jobLocation)
                    .addQueryParameter("vDuration",duration)
                    .addQueryParameter("vPreferCity",pCity)
                    .addQueryParameter("vPreferJobTitle",jobTitle)
                    .addQueryParameter("vExpectedSalary",salary)
                    .addQueryParameter("vQualification",qualification)
                    .addQueryParameter("vWorkingMode",workingMode)
                    .addQueryParameter("tTagLine","")
                    .addQueryParameter("fbid","")
                    .addQueryParameter("googleid","")
                    .addQueryParameter("tLongitude",prefManager.get(LONGITUDE))
                    .addQueryParameter("tLatitude",prefManager.get(LATITUDE))
                    .addQueryParameter("tAppVersion",versionCodeAndName)
                    .addMultipartFile("resume",resumePdf)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        RegisterUserModel::class.java,
                        object : ParsedRequestListener<RegisterUserModel> {
                            override fun onResponse(response: RegisterUserModel?) {
                                try {
                                    response?.let {
                                        hideProgressDialog()
                                        CoroutineScope(Dispatchers.IO).launch {

                                            val jobSeekerProfileInfo =
                                                JobSeekerProfileInfo(this@InformationActivity)
                                            jobSeekerProfileInfo.storeBasicProfileData(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                response.data.user.tTagLine,
                                                response.data.user.vCurrentCompany
                                            )
                                            jobSeekerProfileInfo.storeAboutData(
                                                response.data.user.tBio,
                                                response.data.user.vQualification
                                            )
                                            jobSeekerProfileInfo.storeExperienceData(
                                                experience,
                                                response.data.user.vDesignation,
                                                "",
                                                ""
                                            )
                                            jobSeekerProfileInfo.storeResumeData(
                                                "",
                                                response.data.user.tResumeUrl,
                                            )
                                            jobSeekerProfileInfo.storeJobPreferenceData(
                                                "",
                                                salary,
                                                response.data.user.vCity,
                                                workingMode
                                            )

                                        }
                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[ROLE] = 0
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
                    .addQueryParameter(MOB_NO,phoneNo)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",firstName)
                    .addQueryParameter("vLastName",lastName)
                    .addQueryParameter("vEmail",email)
                    .addQueryParameter("vcity",city)
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

                                            val jobSeekerProfileInfo =
                                                JobSeekerProfileInfo(this@InformationActivity)
                                            jobSeekerProfileInfo.storeBasicProfileData(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                "",
                                                ""
                                            )


                                        }


                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[ROLE] = 0
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
        expectedSalary: String
    ): Boolean {

        if (bio.length > 5000 ) {
            binding.bio.error = "bio length should not be exited to 5000"
            return false
        }
        if (!isNumeric(expectedSalary)){
            binding.salary.error = "Invalid Salary"
        }
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
        companyName = binding.inputPrevCompanyR.text.toString().trim()
        designation = binding.inputDesignationR.text.toString().trim()
        jobTitle = selectedQualification.trim()
        jobDes = binding.inputJobDesR.text.toString().trim()
        salary = binding.inputSalaryR.text.toString().trim()
        if(salary.isEmpty()){
            salary = 0.toString()
        }
        jobLocation = selectedJobLocation.trim()
        workingMode = getSelectedRadioItem(binding.radioGrpWorkingModeR)
        val correct = inputFieldConformationR(jobDes,salary)
        if (!correct) return
        else{
           /* progressBar.visibility = VISIBLE
            val user = UsersRecruiter(
                userId,
                firstName,
                lastName,
                phoneNo,
                email,
                "",
                "",
                "",
                companyName,
                designation,
                jobTitle,
                jobDes,
                salary,
                city,
                workingMode,
                termsConditionsAcceptance
            )

            FirebaseDatabase.getInstance().getReference("Users")
                .child(userType)
                .child(userId)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        btnSubmit.visibility = GONE
                        btnBack.visibility = GONE
                        CoroutineScope(Dispatchers.IO).launch {
                            val recruiterProfileInfo =
                                RecruiterProfileInfo(this@InformationActivity)

                            recruiterProfileInfo.storeBasicProfileData(
                                firstName,
                                lastName,
                                phoneNo,
                                email,
                                "",
                                companyName
                            )
                            recruiterProfileInfo.storeAboutData(
                                jobTitle,
                                salary,
                                city,
                                jobDes,
                                designation,
                                workingMode
                            )
                            recruiterProfileInfo.storeUserType(
                                userType,
                                userId
                            )
                        }
                        makeToast("Data stored successfully",1)
                        navigateToHomeActivity()
                    } else {
                        makeToast("Try again",1)
                    }
                    check3.setBackgroundResource(R.color.check_color)
                    progressBar.visibility = GONE
                }*/
            if (Utils.isNetworkAvailable(this)){
                val versionCodeAndName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                AndroidNetworking.post(NetworkUtils.REGISTER_USER)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addQueryParameter("vFirebaseId",userId)
                    .addQueryParameter("iRole","1")
                    .addQueryParameter(MOB_NO,phoneNo)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",firstName)
                    .addQueryParameter("vLastName",lastName)
                    .addQueryParameter("vEmail",email)
                    .addQueryParameter("tBio",jobDes)
                    .addQueryParameter("vPreferCity","")
                    .addQueryParameter("vcity",city)
                    .addQueryParameter("vCurrentCompany",companyName)
                    .addQueryParameter("vDesignation",designation)
                    .addQueryParameter("vQualification",jobTitle)
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
                                            val recruiterProfileInfo = RecruiterProfileInfo(this@InformationActivity)
                                            recruiterProfileInfo.storeBasicProfileData(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                response.data.user.tTagLine,
                                                response.data.user.vCurrentCompany
                                            )
                                            recruiterProfileInfo.storeAboutData(
                                                response.data.user.vDesignation,
                                                "",
                                                "",
                                                response.data.user.tBio,
                                                response.data.user.vDesignation,
                                                response.data.user.vWorkingMode
                                            )
                                            recruiterProfileInfo.storeProfileImg(
                                                response.data.user.tProfileUrl
                                            )
                                            recruiterProfileInfo.storeProfileBannerImg(
                                                ""
                                            )

                                        }
                                        prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[ROLE] = 1
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
                    .addQueryParameter(MOB_NO,phoneNo)
                    .addQueryParameter(DEVICE_ID,prefManager.get(DEVICE_ID))
                    .addQueryParameter(DEVICE_TYPE,"0")
                    .addQueryParameter(OS_VERSION,prefManager.get(OS_VERSION))
                    .addQueryParameter(FCM_TOKEN,prefManager.get(FCM_TOKEN))
                    .addQueryParameter(DEVICE_NAME,prefManager.get(DEVICE_NAME))
                    .addQueryParameter("vFirstName",firstName)
                    .addQueryParameter("vLastName",lastName)
                    .addQueryParameter("vEmail",email)
                    .addQueryParameter("tBio",jobDes)
                    .addQueryParameter("vcity",city)
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
                                            val recruiterProfileInfo = RecruiterProfileInfo(this@InformationActivity)
                                            recruiterProfileInfo.storeBasicProfileData(
                                                response.data.user.vFirstName,
                                                response.data.user.vLastName,
                                                response.data.user.vMobile,
                                                response.data.user.vEmail,
                                                "",
                                               ""
                                            )
                                        }
                                        binding.btnSubmit.visibility = GONE
                                        binding.btnBack.visibility = GONE
                                        prefManager[IS_LOGIN] = true
                                        prefManager[FIREBASE_ID] = response.data.user.vFirebaseId
                                        prefManager[ROLE] = 1
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
    private fun inputFieldConformationR(jobDes: String, expectedSalary: String): Boolean {
        if (jobDes.length > 5000){
            binding.inputJobDesR.error = "Job Description Length Should not exited to 5000"
            return false
        }

        if (!isNumeric(salary)){
            binding.inputSalaryR.error = "Invalid Salary"
            return false
        }

       return true
    }

    private fun navigateToHomeActivity() {
        if (userType == "Job Seeker") {
            val intent = Intent(this@InformationActivity, HomeJobSeekerActivity::class.java)
            intent.putExtra("userType", userType)
            val fullName = firstName + lastName
            makeToast("Welcome $fullName", 0)
            intent.putExtra("name", fullName)
            intent.putExtra("userId",userId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        if (userType == "Recruiter"){
            val intent = Intent(this@InformationActivity, HomeRecruiterActivity::class.java)/** need **/
            intent.putExtra("userType", userType)
            val fullName = firstName + lastName
            makeToast("Welcome $fullName", 0)
            intent.putExtra("name", fullName)
            intent.putExtra("userId",userId)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun changeLayout(layoutID: Int, btnPointer: Int) {
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
    }

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