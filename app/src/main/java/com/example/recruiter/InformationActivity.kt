package com.example.recruiter

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class InformationActivity : AppCompatActivity() ,OnClickListener, AdapterView.OnItemSelectedListener{


    lateinit var inputLayoutJobSeeker:LinearLayout
    
    lateinit var jsLayout1:LinearLayout

    lateinit var inputDegreeTypeSpinner:Spinner
    lateinit var inputBioJ:EditText
    lateinit var radioGrpFreshExp:RadioGroup
    lateinit var radioBtnFresher:RadioButton
    lateinit var radioBtnExperience:RadioButton

    lateinit var jsLayout2:LinearLayout

    lateinit var jsSubLayout1:LinearLayout

    lateinit var inputPrevCompany:EditText
    lateinit var inputDesignation:EditText
    lateinit var inputDuration:EditText


//    lateinit var jsSubLayout2:LinearLayout

    lateinit var inputSalaryJ:EditText
    lateinit var inputCitySpinnerJ:Spinner //
    lateinit var radioGrpWorkingMode:RadioGroup
    lateinit var radioBtnOnsite:RadioButton
    lateinit var radioBtnRemote:RadioButton
    lateinit var radioBtnHybrid:RadioButton
    lateinit var inputJobTypeSpinner:Spinner//

    lateinit var jsLayout3:LinearLayout

    lateinit var textPdfName:TextView
    lateinit var uploadBtn:Button


    lateinit var inputLayoutRecruiter:LinearLayout

    lateinit var recruiterLayout1:LinearLayout

    lateinit var inputPrevCompanyR:EditText
    lateinit var inputDesignationR:EditText
    lateinit var inputJobTitleSpinner:Spinner//
    lateinit var inputJobDesR:EditText

    lateinit var recruiterLayout2:LinearLayout

    lateinit var inputSalaryR:EditText
    lateinit var JobLocationSpinnerR:Spinner //
    lateinit var radioGrpWorkingModeR:RadioGroup
    lateinit var radioBtnOnsiteR:RadioButton
    lateinit var radioBtnRemoteR:RadioButton
    lateinit var radioBtnHybridR:RadioButton

    lateinit var submitBtnLayout:LinearLayout
    lateinit var btnSubmit:Button
    lateinit var progressBar:ProgressBar
    lateinit var btnNext:ImageView
    lateinit var btnBack:ImageView

    lateinit var check1:ImageView
    lateinit var check2:ImageView
    lateinit var check3:ImageView
    lateinit var check4:ImageView

    lateinit var btnSkip : TextView

    lateinit var decorView: View

    lateinit var jobType:String

    var layoutID = -1
    var btnPointer = 0

    lateinit var firstName:String
    lateinit var lastName:String
    lateinit var phoneNo:String
    lateinit var email:String
    lateinit var userId:String
    lateinit var qualification:String
    lateinit var bio:String
    lateinit var experience:String
    lateinit var companyName:String
    lateinit var designation:String
    lateinit var duration:String
    lateinit var salary:String
    lateinit var city:String
    lateinit var workingMode:String
    lateinit var jobTitle:String
    lateinit var jobDes:String
    lateinit var resume:String
    lateinit var termsConditionsAcceptance:String

    private val jobLocations = arrayOf("Ahmedabad(India)","US","Germany","UK")
    private val qualifications = arrayOf("B.com","B.E.","B.Tech","M.com","B.FAM")
    private val jobs = arrayOf("Android Developer","Web Developer.","HR","Project Manager","CEO")

    private lateinit var selectedQualification:String
    private lateinit var selectedJobLocation:String
    private lateinit var selectedJob : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)
        fullScreen()

        setXMlIds()
        setOnClickListener()
        jobType = intent.getStringExtra("jobType").toString()
        setLayout(jobType)
        setAdapters()


    }
    private fun setAdapters() {

        val jobLocationAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,jobLocations)
        jobLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        JobLocationSpinnerR.adapter = jobLocationAdapter
        inputCitySpinnerJ.adapter = jobLocationAdapter


        val qualificationsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,qualifications)
        qualificationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputDegreeTypeSpinner.adapter = qualificationsAdapter


        val jobsAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,jobs)
        jobsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    private fun setLayout(jobType: String) {

        if (jobType == "Recruiter") {
            inputLayoutRecruiter.visibility = VISIBLE
            layoutID = 0
            recruiterLayout1.visibility = VISIBLE
            recruiterLayout2.visibility = GONE
            check1.visibility = VISIBLE
            check1.setBackgroundResource(R.color.check_color)
            check2.visibility = VISIBLE
            check2.setBackgroundResource(R.color.check_def_color)
            check3.visibility = VISIBLE
            check3.setBackgroundResource(R.color.check_def_color)
            check4.visibility = GONE
            check4.setBackgroundResource(R.color.check_def_color)
        }
        if(jobType == "JobSeeker"){
            inputLayoutJobSeeker.visibility = VISIBLE
            layoutID = 1
            jsLayout1.visibility = VISIBLE
            jsSubLayout1.visibility = GONE
            jsLayout2.visibility = GONE
            jsLayout3.visibility = GONE
            check1.visibility = VISIBLE
            check1.setBackgroundResource(R.color.check_color)
            check2.visibility = VISIBLE
            check2.setBackgroundResource(R.color.check_def_color)
            check3.visibility = VISIBLE
            check3.setBackgroundResource(R.color.check_def_color)
            check4.visibility = VISIBLE
            check4.setBackgroundResource(R.color.check_def_color)
        }
        btnPointer = 0
        firstName = intent.getStringExtra("fName").toString()
        lastName = intent.getStringExtra("lName").toString()
        phoneNo = intent.getStringExtra("phoneNo").toString()
        email = intent.getStringExtra("email").toString()
        termsConditionsAcceptance = intent.getStringExtra("termsConditions").toString()


    }

    private fun setOnClickListener() {
        uploadBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnSubmit.setOnClickListener(this)
        inputCitySpinnerJ.onItemSelectedListener = this
        inputDegreeTypeSpinner.onItemSelectedListener = this
        inputJobTypeSpinner.onItemSelectedListener = this
        inputJobTitleSpinner.onItemSelectedListener = this
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.uploadBtn -> {

            }
            R.id.btnBack -> {
                btnPointer -= 1
                changeLayout(layoutID,btnPointer)
//                makeToast("$btnPointer",0)
            }
            R.id.btnSubmit -> {
                btnSubmit.visibility = GONE
                btnBack.visibility = GONE
                if(jobType == "Recruiter") storeInfoR()
                if (jobType == "JobSeeker") storeInfoJ()

            }
            R.id.btnNext -> {
                btnPointer += 1
                changeLayout(layoutID,btnPointer)
//                makeToast("$btnPointer",0)
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
        workingMode = getSelectedRadioItem(radioGrpWorkingMode).toString()
        jobTitle = selectedJob.toString()
        city = selectedJobLocation.toString()

        val correct = inputFieldConformationJ(bio,salary)
        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            val user = UsersJobSeeker(
                firstName,
                lastName,
                phoneNo,
                email,
                qualification,
                experience,
                companyName,
                designation,
                duration,
                bio,
                jobTitle,
                city,
                salary,
                workingMode,
                termsConditionsAcceptance
            )

            userId = FirebaseDatabase.getInstance().getReference("Users").child("JobSeeker").push().key.toString()
            FirebaseDatabase.getInstance().getReference("Users")
                .child("JobSeeker")
                .child(userId)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
            return false
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
        jobTitle = selectedJob.toString()
        jobDes = inputJobDesR.text.toString()
        salary = inputSalaryR.text.toString()
        city = selectedJobLocation.toString()
        workingMode = getSelectedRadioItem(radioGrpWorkingModeR).toString()
        val correct = inputFieldConformationR(jobDes,salary)
        if (!correct) return
        else{
            progressBar.visibility = VISIBLE
            val user = UsersRecruiter(
                firstName,
                lastName,
                phoneNo,
                email,
                companyName,
                designation,
                jobTitle,
                jobDes,
                salary,
                city,
                workingMode,
                termsConditionsAcceptance
            )
            userId = FirebaseDatabase.getInstance().getReference("Users").child("Recruiter").push().key.toString()
            FirebaseDatabase.getInstance().getReference("Users")
                .child("Recruiter")
                .child(userId)
                .setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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

    private fun inputFieldConformationR(jobDes: String, salary: String): Boolean {
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
        val intent = Intent(this@InformationActivity,HomeActivity::class.java)
        intent.putExtra("jobType",jobType)
        val fullName = firstName + lastName
        makeToast(fullName,0)
        intent.putExtra("name",fullName)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
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
                check2.setBackgroundResource(R.color.check_color)
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
                check2.setBackgroundResource(R.color.check_def_color)
                check3.setBackgroundResource(R.color.check_def_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
            else if (btnPointer == 1 ) {
                experience = getSelectedRadioItem(radioGrpFreshExp)
                if(experience == "Experienced") {
                    makeToast("You Selected $experience",0)
                    makeToast("press next for further step",0)
                    jsLayout1.visibility = GONE
                    jsLayout2.visibility = GONE
                    jsSubLayout1.visibility = VISIBLE
                    //                    jsSubLayout2.visibility = GONE
                    jsLayout3.visibility = GONE
                    btnBack.visibility = VISIBLE
                    btnNext.visibility = VISIBLE
                    btnSubmit.visibility = GONE
                    check1.setBackgroundResource(R.color.check_color)
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
                    check2.setBackgroundResource(R.color.check_color)
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
                check2.setBackgroundResource(R.color.check_color)
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
                btnSubmit.visibility = VISIBLE

                check1.setBackgroundResource(R.color.check_color)
                check2.setBackgroundResource(R.color.check_color)
                check3.setBackgroundResource(R.color.check_color)
                check4.setBackgroundResource(R.color.check_def_color)
            }
        }
    }

    private fun getSelectedRadioItem(radioGroup: RadioGroup): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        val radioButton = findViewById<View>(selectedItemId) as RadioButton
        if (selectedItemId != -1) {
//            makeToast(radioButton.text.toString(),0)
            return radioButton.text.toString()
        }
        return "not Selected"
    }

    private fun setXMlIds() {
        inputLayoutJobSeeker = findViewById(R.id.inputLayoutJobSeeker)

        jsLayout1 = findViewById(R.id.jsLayout1)
        inputDegreeTypeSpinner = findViewById(R.id.inputDegreeTypeSpinner)
        inputBioJ = findViewById(R.id.inputBioJ)
        radioGrpFreshExp = findViewById(R.id.radioGrpFreshExp)
        radioBtnFresher = findViewById(R.id.radioBtnFresher)
        radioBtnExperience = findViewById(R.id.radioBtnExperience)

        jsLayout2 = findViewById(R.id.jsLayout2)

        jsSubLayout1 = findViewById(R.id.jsSubLayout1)

        inputPrevCompany = findViewById(R.id.inputPrevCompany)
        inputDesignation = findViewById(R.id.inputDesignation)
        inputDuration = findViewById(R.id.inputDuration)


//        jsSubLayout2 = findViewById(R.id.jsSubLayout2)

        inputSalaryJ = findViewById(R.id.inputSalaryJ)
        inputCitySpinnerJ = findViewById(R.id.inputCitySpinnerJ)
        radioGrpWorkingMode = findViewById(R.id.radioGrpWorkingMode)
        radioBtnOnsite = findViewById(R.id.radioBtnOnsite)
        radioBtnRemote = findViewById(R.id.radioBtnRemote)
        radioBtnHybrid = findViewById(R.id.radioBtnHybrid)
        inputJobTypeSpinner = findViewById(R.id.inputJobTypeSpinner)

        jsLayout3 = findViewById(R.id.jsLayout3)
        textPdfName = findViewById(R.id.textPdfName)
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

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this@InformationActivity,RegistrationActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        finish()
    }
    private fun fullScreen() {
        decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { i ->
            if (i == 0) {
                decorView.systemUiVisibility = hideSystemBars()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            decorView.systemUiVisibility = hideSystemBars()
        }
    }

    private fun hideSystemBars(): Int {
        return (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }


}