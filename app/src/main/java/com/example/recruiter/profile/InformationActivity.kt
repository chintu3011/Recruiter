package com.example.recruiter.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.example.recruiter.R
import com.example.recruiter.recruiterSide.RecruiterProfileInfo
import com.example.recruiter.authentication.RegistrationActivity
import com.example.recruiter.jobSeekerSide.HomeJobSeekerActivity
import com.example.recruiter.jobSeekerSide.JobSeekerProfileInfo
import com.example.recruiter.jobSeekerSide.UsersJobSeeker
import com.example.recruiter.recruiterSide.HomeRecruiterActivity
import com.example.recruiter.recruiterSide.UsersRecruiter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InformationActivity : AppCompatActivity() ,OnClickListener, AdapterView.OnItemSelectedListener{



    private lateinit var inputLayoutJobSeeker:ConstraintLayout

    private lateinit var jsLayout1:Group

    private lateinit var inputDegreeTypeSpinner:Spinner
    private lateinit var inputBioJ:EditText
    private lateinit var radioGrpFreshExp:RadioGroup
    private lateinit var radioBtnFresher:RadioButton
    private lateinit var radioBtnExperience:RadioButton

    private lateinit var jsLayout2:Group

    private lateinit var jsSubLayout1:Group

    private lateinit var inputPrevCompany:EditText
    private lateinit var inputDesignation:EditText
    private lateinit var inputDuration:EditText


//    lateinit var jsSubLayout2:LinearLayout

    private lateinit var inputSalaryJ:EditText
    private lateinit var inputCitySpinnerJ:Spinner //
    private lateinit var radioGrpWorkingMode:RadioGroup
    private lateinit var radioBtnOnsite:RadioButton
    private lateinit var radioBtnRemote:RadioButton
    private lateinit var radioBtnHybrid:RadioButton
    private lateinit var inputJobTypeSpinner:Spinner//

    private lateinit var jsLayout3:Group

    private lateinit var textPdfName:TextView
    private lateinit var btnSelectPdf: ImageView
    private lateinit var uploadProgressBar: ProgressBar
    private lateinit var uploadBtn:Button


    private lateinit var inputLayoutRecruiter:ConstraintLayout

    private lateinit var recruiterLayout1:Group

    private lateinit var inputPrevCompanyR:EditText
    private lateinit var inputDesignationR:EditText
    private lateinit var inputJobTitleSpinner:Spinner//
    private lateinit var inputJobDesR:EditText

    private lateinit var recruiterLayout2:Group

    private lateinit var inputSalaryR:EditText
    private lateinit var JobLocationSpinnerR:Spinner
    private lateinit var radioGrpWorkingModeR:RadioGroup
    private lateinit var radioBtnOnsiteR:RadioButton
    private lateinit var radioBtnRemoteR:RadioButton
    private lateinit var radioBtnHybridR:RadioButton

    private lateinit var submitBtnLayout:Group
    private lateinit var btnSubmit:Button
    private lateinit var progressBar:ProgressBar
    private lateinit var btnNext:FloatingActionButton
    private lateinit var btnBack:FloatingActionButton

    private lateinit var check1:ImageView
    private lateinit var check2:ImageView
    private lateinit var check3:ImageView
    private lateinit var check4:ImageView

    private lateinit var btnSkip : TextView
    
    private var userType = String()

    private var layoutID = -1
    private var btnPointer = 0

    private var pdfUri:Uri ?= null

    private var firstName = String()
    private var lastName = String()
    private var phoneNo = String()
    private var email = String()
    private var userId = String()
    private var qualification = String()
    private var bio = String()
    private var experience = String()
    private var companyName = String()
    private var designation = String()
    private var duration = String()
    private var salary = String()
    private var city = String()
    private var workingMode = String()
    private var jobTitle = String()
    private var jobDes = String()
    private var resume = String()
    private var termsConditionsAcceptance = String()
    private var pdfName = String()

    private val jobLocations = arrayOf("City","Ahmedabad(India)","US","Germany","UK")
    private val qualifications = arrayOf("Select Degree","B.com","B.E.","B.Tech","M.com","B.PHARM")
    private val jobs = arrayOf("Select JobType","Android Developer","Web Developer.","HR","Project Manager","CEO")

    private var selectedQualification = String()
    private var selectedJobLocation = String()
    private var selectedJob = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        val window: Window = this@InformationActivity.window
        val background = ContextCompat.getDrawable(this@InformationActivity,
            R.drawable.status_bar_color
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.statusBarColor = ContextCompat.getColor(this@InformationActivity,android.R.color.transparent)
        window.navigationBarColor = ContextCompat.getColor(this@InformationActivity,android.R.color.white)
        window.setBackgroundDrawable(background)

        setXMlIds()
        setOnClickListener()
        userType = intent.getStringExtra("userType").toString()
        setLayout(userType)
        setAdapters()

    }

    private fun setAdapters() {

        val jobLocationAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,jobLocations)
        jobLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        JobLocationSpinnerR.adapter = jobLocationAdapter
        inputCitySpinnerJ.adapter = jobLocationAdapter


        val qualificationsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,qualifications)
        qualificationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputDegreeTypeSpinner.adapter = qualificationsAdapter


        val jobsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,jobs)
        jobsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inputJobTitleSpinner.adapter = jobsAdapter
        inputJobTypeSpinner.adapter = jobsAdapter
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedJobLocation = jobLocations[position]
        selectedQualification = qualifications[position]
        selectedJob = jobs[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun setLayout(userType: String) {

        if (userType == "Recruiter") {
            inputLayoutRecruiter.visibility = VISIBLE
            layoutID = 0
            recruiterLayout1.visibility = VISIBLE
            recruiterLayout2.visibility = GONE
            btnNext.visibility = VISIBLE
            btnBack.visibility = GONE
            check1.visibility = VISIBLE
            check1.setBackgroundResource(R.color.check_color)
            check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
            check2.visibility = VISIBLE
            check2.setBackgroundResource(R.color.check_def_color)
            check3.visibility = VISIBLE
            check3.setBackgroundResource(R.color.check_def_color)
            check4.visibility = GONE
            check4.setBackgroundResource(R.color.check_def_color)
        }
        if(userType == "Job Seeker"){
            inputLayoutJobSeeker.visibility = VISIBLE
            layoutID = 1
            jsLayout1.visibility = VISIBLE
            jsSubLayout1.visibility = GONE
            jsLayout2.visibility = GONE
            jsLayout3.visibility = GONE
            btnNext.visibility = VISIBLE
            btnBack.visibility = GONE
            check1.visibility = VISIBLE
            check1.setBackgroundResource(R.color.check_color)
            check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
            check2.visibility = VISIBLE
            check2.setBackgroundResource(R.color.check_def_color)
            check3.visibility = VISIBLE
            check3.setBackgroundResource(R.color.check_def_color)
            check4.visibility = VISIBLE
            check4.setBackgroundResource(R.color.check_def_color)
        }
        btnPointer = 0
        userId = intent.getStringExtra("uid").toString()
        firstName = intent.getStringExtra("fName").toString()
        lastName = intent.getStringExtra("lName").toString()
        phoneNo = intent.getStringExtra("phoneNo").toString()
        email = intent.getStringExtra("email").toString()
        termsConditionsAcceptance = intent.getStringExtra("termsConditions").toString()

    }

    private fun setOnClickListener() {
        btnSelectPdf.setOnClickListener(this)
        uploadBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnSkip.setOnClickListener(this)
        btnSubmit.setOnClickListener(this)
        inputCitySpinnerJ.onItemSelectedListener = this
        inputDegreeTypeSpinner.onItemSelectedListener = this
        inputJobTypeSpinner.onItemSelectedListener = this
        inputJobTitleSpinner.onItemSelectedListener = this
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.uploadBtn -> {
                uploadProgressBar.visibility = VISIBLE
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
                }
                uploadBtn.visibility = GONE
                btnSubmit.visibility = VISIBLE
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
                btnSubmit.visibility = GONE
                btnBack.visibility = GONE
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
                    storeInfoJ()
                }
                if(userType == "Recruiter"){
                    storeInfoR()
                }

//                val  map = mutableMapOf<String,String>()
//                map["userId"] = userId
//                map["userFName"] = firstName
//                map["userLName"] = firstName
//                map["userPhoneNUmber"] = phoneNo
//                map["userEmailId"] = email
//
//                FirebaseDatabase.getInstance().getReference("Users")
//                    .child(userType)
//                    .child(userId)
//                    .setValue(map).addOnCompleteListener{ task ->
//                        if(task.isSuccessful){
//                            if(userType == "Job Seeker"){
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    val jobSeekerProfileInfo =
//                                        JobSeekerProfileInfo(this@InformationActivity)
//
//                                    jobSeekerProfileInfo.storeBasicProfileData(
//                                        firstName,
//                                        lastName,
//                                        phoneNo,
//                                        email,
//                                        "",
//                                        ""
//                                    )
//                                    jobSeekerProfileInfo.storeUserType(
//                                        userType,
//                                        userId
//                                    )
//                                }
//                                navigateToHomeActivity()
//                            }
//                            if(userType == "Recruiter"){
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    val recruiterProfileInfo =
//                                        RecruiterProfileInfo(this@InformationActivity)
//
//                                    recruiterProfileInfo.storeBasicProfileData(
//                                        firstName,
//                                        lastName,
//                                        phoneNo,
//                                        email,
//                                        "",
//                                        ""
//                                    )
//                                    recruiterProfileInfo.storeUserType(
//                                        userType,
//                                        userId
//                                    )
//                                }
//                                navigateToHomeActivity()
//                            }
//
//                        }
//                    }
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
                                textPdfName.text = pdfName
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
        qualification = selectedQualification.toString()
        bio = inputBioJ.text.toString()
        experience = getSelectedRadioItem(radioGrpFreshExp).toString()
        companyName = inputPrevCompany.text.toString()
        designation = inputDesignation.text.toString()
        duration = inputDuration.text.toString()
        salary = inputSalaryJ.text.toString()
        if(salary.isEmpty()){
            salary = 0.toString()
        }
        workingMode = getSelectedRadioItem(radioGrpWorkingMode).toString()
        jobTitle = selectedJob.toString()
        city = selectedJobLocation.toString()

        val correct = inputFieldConformationJ(bio,salary)
        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            val user = UsersJobSeeker(
                userId,
                userType,
                firstName,
                lastName,
                phoneNo,
                email,
                "",
                "",
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
                }
        }
    }

    private fun inputFieldConformationJ(
        bio: String,
        expectedSalary: String
    ): Boolean {

        if (bio.length > 5000 ) {
            inputBioJ.error = "bio length should not be exited to 5000"
            return false
        }
        if (!isNumeric(expectedSalary)){
            inputSalaryJ.error = "Invalid Salary"
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
        companyName = inputPrevCompanyR.text.toString()
        designation = inputDesignationR.text.toString()
        jobTitle = selectedJob
        jobDes = inputJobDesR.text.toString()
        salary = inputSalaryR.text.toString()
        if(salary.isEmpty()){
            salary = 0.toString()
        }
        city = selectedJobLocation
        workingMode = getSelectedRadioItem(radioGrpWorkingModeR)
        val correct = inputFieldConformationR(jobDes,salary)
        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            val user = UsersRecruiter(
                userId,
                userType,
                firstName,
                lastName,
                phoneNo,
                email,
                "",
                "",
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
                termsConditionsAcceptance,
            )

            FirebaseDatabase.getInstance().getReference("Users")
                .child(userType)
                .child(userId)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

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
                }
        }
    }

    private fun inputFieldConformationR(jobDes: String, expectedSalary: String): Boolean {
        if (jobDes.length > 5000){
            inputJobDesR.error = "Job Description Length Should not exited to 5000"
            return false
        }

        if (!isNumeric(salary)){
            inputSalaryR.error = "Invalid Salary"
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
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

    private fun changeLayout(layoutID: Int, btnPointer: Int) {
        if (layoutID == 0){
            if(btnPointer == 0){
                recruiterLayout1.visibility = VISIBLE
                recruiterLayout2.visibility = GONE
                btnNext.visibility = VISIBLE
                btnBack.visibility = GONE
                btnSubmit.visibility = GONE

                check1.setBackgroundResource(R.color.check_color)
                check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check2.setBackgroundResource(R.color.check_def_color)
                check3.setBackgroundResource(R.color.check_def_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
            if (btnPointer == 1){
                recruiterLayout2.visibility = VISIBLE
                recruiterLayout1.visibility = GONE
                btnNext.visibility = GONE
                btnBack.visibility = VISIBLE
                btnSubmit.visibility = VISIBLE

                check1.setBackgroundResource(R.color.check_color)
                check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check2.setBackgroundResource(R.color.check_color)
                check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check3.setBackgroundResource(R.color.check_def_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
        }
        if (layoutID == 1){
            if (btnPointer == 0){
                jsLayout1.visibility = VISIBLE
                jsLayout2.visibility = GONE
                jsLayout3.visibility = GONE
                jsSubLayout1.visibility = GONE
                btnBack.visibility = GONE
                btnNext.visibility = VISIBLE
                btnSubmit.visibility = GONE

                check1.setBackgroundResource(R.color.check_color)
                check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check2.setBackgroundResource(R.color.check_def_color)
                check3.setBackgroundResource(R.color.check_def_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
            else if (btnPointer == 1 ) {

                experience = getSelectedRadioItem(radioGrpFreshExp)
                if(experience == "Experienced") {
//                    makeToast("You Selected $experience",0)
//                    makeToast("press next for further step",0)
                    jsLayout1.visibility = GONE
                    jsLayout2.visibility = GONE
                    jsSubLayout1.visibility = VISIBLE
                    //                    jsSubLayout2.visibility = GONE
                    jsLayout3.visibility = GONE
                    btnBack.visibility = VISIBLE
                    btnNext.visibility = VISIBLE
                    btnSubmit.visibility = GONE
                    check1.setBackgroundResource(R.color.check_color)
                    check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                    check2.setBackgroundResource(R.color.check_def_color)
                    check3.setBackgroundResource(R.color.check_def_color)
                    check4.setBackgroundResource(R.color.check_def_color)
                }
                if (experience == "Fresher"){
                    jsLayout1.visibility = GONE
                    jsSubLayout1.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                    jsLayout2.visibility = VISIBLE
                    jsLayout3.visibility = GONE
                    btnBack.visibility = VISIBLE
                    btnNext.visibility = VISIBLE
                    btnSubmit.visibility = GONE
                    check1.setBackgroundResource(R.color.check_color)
                    check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                    check2.setBackgroundResource(R.color.check_color)
                    check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                    check3.setBackgroundResource(R.color.check_def_color)
                    check4.setBackgroundResource(R.color.check_def_color)
                }
            }

            else if (btnPointer == 2){
                jsLayout1.visibility = GONE
                jsSubLayout1.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                jsLayout2.visibility = VISIBLE
                jsLayout3.visibility = GONE
                btnBack.visibility = VISIBLE
                btnNext.visibility = VISIBLE
                btnSubmit.visibility = GONE

                check1.setBackgroundResource(R.color.check_color)
                check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check2.setBackgroundResource(R.color.check_color)
                check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check3.setBackgroundResource(R.color.check_def_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
            else if (btnPointer == 3){
                jsLayout1.visibility = GONE
                jsSubLayout1.visibility = GONE
//                    jsSubLayout2.visibility = GONE
                jsLayout2.visibility = GONE
                jsLayout3.visibility = VISIBLE
                btnBack.visibility = VISIBLE
                btnNext.visibility = GONE
                btnSubmit.visibility = GONE
                uploadBtn.visibility = VISIBLE
                uploadProgressBar.visibility = GONE

                check1.setBackgroundResource(R.color.check_color)
                check1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check2.setBackgroundResource(R.color.check_color)
                check2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check3.setBackgroundResource(R.color.check_color)
                check3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
                check4.setBackgroundResource(R.color.check_def_color)
            }
        }
    }

    private fun getSelectedRadioItem(radioGroup: RadioGroup): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        if (selectedItemId != -1) {
            val radioButton = findViewById<View>(selectedItemId) as RadioButton
//            makeToast(radioButton.text.toString(),0)
            return radioButton.text.toString()
        }
        return "not Selected"
    }

    private fun setXMlIds() {
        inputLayoutJobSeeker = findViewById(R.id.inputLayoutJobSeeker)

        jsLayout1 = findViewById(R.id.jsLayout1)
        inputDegreeTypeSpinner = findViewById(R.id.inputDegreeTypeSpinner)
        inputBioJ = findViewById(R.id.bio)
        radioGrpFreshExp = findViewById(R.id.radioGrpFreshExp)
        radioBtnFresher = findViewById(R.id.radioBtnFresher)
        radioBtnExperience = findViewById(R.id.radioBtnExperience)

        jsLayout2 = findViewById(R.id.jsLayout2)

        jsSubLayout1 = findViewById(R.id.jsSubLayout)

        inputPrevCompany = findViewById(R.id.companyName)
        inputDesignation = findViewById(R.id.designation)
        inputDuration = findViewById(R.id.duration)


//        jsSubLayout2 = findViewById(R.id.jsSubLayout2)

        inputSalaryJ = findViewById(R.id.salary)
        inputCitySpinnerJ = findViewById(R.id.inputCitySpinnerJ)
        radioGrpWorkingMode = findViewById(R.id.radioGrpWorkingMode)
        radioBtnOnsite = findViewById(R.id.radioBtnOnsite)
        radioBtnRemote = findViewById(R.id.radioBtnRemote)
        radioBtnHybrid = findViewById(R.id.radioBtnHybrid)
        inputJobTypeSpinner = findViewById(R.id.inputJobTypeSpinner)

        jsLayout3 = findViewById(R.id.jsLayout3)
        textPdfName = findViewById(R.id.textPdfName)
        btnSelectPdf = findViewById(R.id.btnSelectPdf)
        uploadProgressBar = findViewById(R.id.uploadProgressBar)
        uploadBtn = findViewById(R.id.uploadBtn)


        inputLayoutRecruiter = findViewById(R.id.inputLayoutRecruiter)

        recruiterLayout1 = findViewById(R.id.recruiterLayout1)
        inputPrevCompanyR = findViewById(R.id.inputPrevCompanyR)
        inputDesignationR = findViewById(R.id.inputDesignationR)
        inputJobTitleSpinner = findViewById(R.id.inputJobTitleSpinner)
        inputJobDesR = findViewById(R.id.inputJobDesR)

        recruiterLayout2 = findViewById(R.id.recruiterLayout2)
        inputSalaryR = findViewById(R.id.inputSalaryR)
        JobLocationSpinnerR = findViewById(R.id.JobLocationSpinnerR)
        radioGrpWorkingModeR = findViewById(R.id.radioGrpWorkingModeR)
        radioBtnOnsiteR = findViewById(R.id.radioBtnOnsiteR)
        radioBtnRemoteR = findViewById(R.id.radioBtnRemoteR)
        radioBtnHybridR = findViewById(R.id.radioBtnHybridR)

        submitBtnLayout = findViewById(R.id.submitBtnLayout)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        check1 = findViewById(R.id.check1)
        check2 = findViewById(R.id.check2)
        check3 = findViewById(R.id.check3)
        check4 = findViewById(R.id.check4)

        btnSkip = findViewById(R.id.btnSkip)

    }

    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }


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


}