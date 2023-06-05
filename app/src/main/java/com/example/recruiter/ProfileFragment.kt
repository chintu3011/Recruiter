package com.example.recruiter

import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.coroutineScope
import com.example.recruiter.databinding.FragmentProfileBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(),View.OnClickListener {



    //    lateinit var fragview : View
    lateinit var alertDialogBasicInfo: AlertDialog; lateinit var alertDialogAboutInfo : AlertDialog
    lateinit var alertDialogExperience : AlertDialog; lateinit var alertDialogResumeInfo : AlertDialog
    lateinit var alertDialogRecruiterInfo : AlertDialog; lateinit var alertDialogPrefInfo:AlertDialog

    lateinit var alertDialogProfileBanner : AlertDialog;lateinit var alertDialogProfileImg: AlertDialog

    lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    lateinit var recruiterProfileInfo: RecruiterProfileInfo

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    var type:String ?= null
    var name:String ?= null
    var phoneNumber:String ?= null
    var emailId:String ?= null
    var profileImg:String ?= null
    var profileImgUri:Uri ?= null
    var profileBannerImg:String ?= null
    var imgUri:Uri ?= null
    var profileBannerImgUri:Uri ?= null
    var tageLine:String ?= null
    var currentCompany:String ?= null

    //jobSeeker Data
    var bio:String ?= null
    var qualification:String ?= null
    var experienceState:String ?= null
    var designation: String ?= null
    var prevCompany:String ?= null
    var prevJobDuration:String ?= null
    var resumeUri: String ?= null
    var resumeFileName:String ?= null
    val resumeFirebaseUri:String ?= null
    var pdfUri: Uri ?= null
    var prefJobTitle:String ?= null
    var expectedSalary:String ?= null
    var prefJobLocation:String ?= null
    var prefWorkingMode:String ?= null
    //Recruiter Data
    var jobTitleR:String ?= null
    var salaryR:String ?= null
    var jobLocationR:String ?= null
    var jobDesR:String ?= null
    var designationR:String ?= null
    var workingModeR:String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val context: Context = requireContext()
//        fragview = inflater.inflate(R.layout.fragment_profile, container, false)
        jobSeekerProfileInfo = JobSeekerProfileInfo(context)
        recruiterProfileInfo = RecruiterProfileInfo(context)

        setProfileData()
        setOnClickListener()

        return binding.root
    }

    private fun setProfileData() {

        if (binding.userType.text.toString().trim() == "Job Seeker") {
            binding.groupJobSeeker.visibility = VISIBLE
            binding.groupRecruiter.visibility = GONE

            lifecycle.coroutineScope.launch{
                jobSeekerProfileInfo.getUserProfileBannerImg().collect {
                    val imageUri: Uri? = if (it.isNotEmpty()) Uri.parse(it) else null
                    binding.profileBackImg.setImageURI(imageUri)
//                    makeToast(it,1)
                }
            }
//            lifecycle.coroutineScope.launch{
//                jobSeekerProfileInfo.getUserProfileImg().collect {
//                    binding.profileImg.setImageURI(it.toUri())
//                }
//            }
            lifecycle.coroutineScope.launch{
                jobSeekerProfileInfo.getUserName().collect {
                    binding.userName.text = it
                }
            }
            lifecycle.coroutineScope.launch{
                jobSeekerProfileInfo.getUserName().collect {
                    binding.userName.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserTageLine().collect{
                    binding.expertise.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserCurrentCompany().collect {
                    binding.currentCompany.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                 jobSeekerProfileInfo.getUserBio().collect{
                     binding.bioJ.setText(it)
                 }
            }
            lifecycle.coroutineScope.launch {
                  jobSeekerProfileInfo.getUserQualification().collect{
                      binding.qualificationJ.text = it
                  }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserExperienceState().collect {

                }
            }
            lifecycle.coroutineScope.launch {
                 jobSeekerProfileInfo.getUserDesignation().collect{
                     binding.designationJ.text = it
                 }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrevCompany().collect{
                    binding.companyNameJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                 jobSeekerProfileInfo.getUserPrevJobDuration().collect{
                     binding.duration.text = it
                 }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserResumeFileName().collect{
                    binding.resumeFileNameJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserResumeUri().collect{
//                    makeToast(it,1)
//                    makeToast(resumeUri.toString(),0)
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPerfJobTitle().collect{
                    binding.jobTitleJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                 jobSeekerProfileInfo.getUserExpectedSalary().collect{
                     binding.salaryJ.text = it
                 }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrefJobLocation().collect{
                    binding.jobLocationJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrefWorkingMode().collect{
                    binding.workingModeJ.text = it
                }
            }
        }
        if (binding.userType.text.toString().trim() == "Recruiter") {
            binding.groupJobSeeker.visibility = GONE
            binding.groupRecruiter.visibility = VISIBLE
            lifecycle.coroutineScope.launch{
                recruiterProfileInfo.getUserProfileBannerImg().collect {
                    binding.profileBackImg.setImageURI(it.toUri())
                }
            }
            lifecycle.coroutineScope.launch{
                recruiterProfileInfo.getUserProfileImg().collect {
                    binding.profileImg.setImageURI(it.toUri())
                }
            }
            lifecycle.coroutineScope.launch{
                recruiterProfileInfo.getUserName().collect {
                    binding.userName.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserTageLine().collect{
                    binding.expertise.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserCurrentCompany().collect {
                    binding.currentCompany.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserCurrentCompany().collect {
                    binding.currentCompany.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserJobTitle().collect {
                    binding.jobTitleR.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserSalary().collect {
                    binding.salaryR.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserJobLocation().collect {
                    binding.jobLocationR.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserBio().collect {
                    binding.jobDesR.setText(it)
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserDesignation().collect {
                    binding.designationR.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserWorkingMode().collect {
                    binding.workingModeR.text = it
                }
            }
        }
    }

    private fun setOnClickListener(){
        binding.profileBackImg.setOnClickListener(this)
        binding.profileImg.setOnClickListener(this)
        binding.editBasicInfo.setOnClickListener(this)
        binding.editAboutJ.setOnClickListener(this)
        binding.editExperienceJ.setOnClickListener(this)
        binding.editResumeJ.setOnClickListener(this)
        binding.editJobPrefJ.setOnClickListener(this)
        binding.editInfoR.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.profileBackImg ->{
                 profileBannerDialogView()
//                 selectImg("bannerImg")
            }
            R.id.profileImg -> {
//                selectImg("profileImg")
            }
            R.id.editBasicInfo -> {
                basicInfoDialogView()
            }
            R.id.editAboutJ -> {
                 aboutInfoJDialogView()
            }
            R.id.editExperienceJ -> {
                experienceInfoDialogView()
            }
            R.id.editResumeJ -> {
                resumeInfoDialogView()
            }
            R.id.editJobPrefJ -> {
                jobPrefInfoDialogView()
            }
            R.id.editInfoR -> {
                recruiterAboutInfoDialogView()
            }
        }
    }



    private fun recruiterAboutInfoDialogView() {
        val  recruiterinfodialogview= layoutInflater.inflate(R.layout.dialog_recruter_info,null)

        val edJobTitle = recruiterinfodialogview.findViewById<EditText>(R.id.jobTitle)
        val edSalary = recruiterinfodialogview.findViewById<EditText>(R.id.salary)
        val edJobLobLocation = recruiterinfodialogview.findViewById<EditText>(R.id.jobLocation)
        val edJobDescription = recruiterinfodialogview.findViewById<EditText>(R.id.jobDes)
        val edDesignation = recruiterinfodialogview.findViewById<EditText>(R.id.designation)
        val radioGrpWorkingMode = recruiterinfodialogview.findViewById<RadioGroup>(R.id.radioGrpWorkingMode)

        alertDialogRecruiterInfo = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(recruiterinfodialogview)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->
                jobTitleR = edJobTitle.text.toString().trim()
                salaryR = edSalary.text.toString().trim()
                jobLocationR = edJobLobLocation.text.toString().trim()
                jobDesR = edJobDescription.text.toString().trim()
                designationR = edDesignation.text.toString().trim()

                workingModeR = getSelectedRadioItem(radioGrpWorkingMode,recruiterinfodialogview)

                CoroutineScope(IO).launch{
                    recruiterProfileInfo.storeAboutData(
                        jobTitleR!!,
                        salaryR!!,
                        jobLocationR!!,
                        jobDesR!!,
                        designationR!!,
                        workingModeR!!
                    )
                }
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogRecruiterInfo.show()
    }

    private fun jobPrefInfoDialogView() {
        val jobprefdialogView = layoutInflater.inflate(R.layout.dialog_job_preference_info,null)

        val edJobTitle = jobprefdialogView.findViewById<EditText>(R.id.jobTitle)
        val tvLSalary = jobprefdialogView.findViewById<TextInputLayout>(R.id.textLayoutSalary)
        val edExpectedSalary = jobprefdialogView.findViewById<EditText>(R.id.salary)
        val edJobLocation = jobprefdialogView.findViewById<EditText>(R.id.jobLocation)
        val radioGroupWorkingMode = jobprefdialogView.findViewById<RadioGroup>(R.id.radioGrpWorkingMode)


        alertDialogPrefInfo = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(jobprefdialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                prefJobTitle = edJobTitle.text.toString()
                expectedSalary = edExpectedSalary.text.toString() +" "+ tvLSalary.suffixText.toString()
                prefJobLocation = edJobLocation.text.toString()
                prefWorkingMode = getSelectedRadioItem(radioGroupWorkingMode,jobprefdialogView)

                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeJobPreferenceData(
                        prefJobTitle!!,
                        expectedSalary!!,
                        prefJobLocation!!,
                        prefWorkingMode!!
                    )
                }
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogPrefInfo.show()
    }

    private lateinit var tvResumeFileName:TextView
    private lateinit var uploadProgressBar:ProgressBar
    private fun resumeInfoDialogView() {
        val resumedialogView = layoutInflater.inflate(R.layout.dialog_resume_info,null)

//        resumeUri = pdfUri.toString()

        val btnUpload = resumedialogView.findViewById<ShapeableImageView>(R.id.btnUpload)

        tvResumeFileName = resumedialogView.findViewById<TextView>(R.id.resumeFileName)
        btnUpload.setOnClickListener(View.OnClickListener {
            selectPdf()
        })
        uploadProgressBar = resumedialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)
        uploadProgressBar.visibility = GONE

        alertDialogResumeInfo = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(resumedialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeResumeData(
                        resumeFileName!!,
                        resumeUri!!
                    )
                }
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialogResumeInfo.show()

    }
    lateinit var profileBackImg:ImageView
    lateinit var btnSelectImg:ImageView
    private fun profileBannerDialogView() {
        val profileBannerDialogView = layoutInflater.inflate(R.layout.dialog_profile_cover_img,null)

        profileBackImg = profileBannerDialogView.findViewById(R.id.profileBackImg)
        btnSelectImg = profileBannerDialogView.findViewById(R.id.btnChangeImg)
        btnSelectImg.setOnClickListener{
            selectImg("bannerImg")
        }

        alertDialogProfileBanner = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setTitle("Change Banner Image")
            .setView(profileBannerDialogView)
            .setPositiveButton("Done") { dialog, _ ->
                
                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeProfileBannerImg(
                        profileBannerImg!!
                    )
                }

                makeToast(profileBannerImg!!,1)
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogProfileBanner.show()

    }

    private fun selectImg(s: String) {
        val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
        imgIntent.type = "image/*"
        imgIntent.addCategory(Intent.CATEGORY_OPENABLE)

        if (s == "bannerImg"){
            startActivityForResult(imgIntent, 24)
        }
        if (s == "profileImg"){
            startActivityForResult(imgIntent, 44)
        }
    }

    private fun selectPdf() {

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
                    uploadProgressBar.visibility = VISIBLE
                    uploadProgressBar.progress = 10
                    pdfUri = data?.data!!
                    val uri: Uri = data.data!!
                    val uriString: String = uri.toString()
                    resumeFileName = null.toString()
                    if (uriString.startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = context?.contentResolver?.query(
                                uri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                resumeFileName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                resumeUri = pdfUri.toString()
//                                makeToast(resumeUri!!,1)
                                tvResumeFileName.text = resumeFileName
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        uploadProgressBar.progress = 95
                        Handler(Looper.getMainLooper()).postDelayed({
                            uploadProgressBar.progress = 100
                            uploadProgressBar.visibility = GONE
                        },100)
                    },1000)

                }
                24 -> if (resultCode == RESULT_OK){
                    imgUri = data?.data!!
                    if (imgUri != null) {
                        profileBackImg.setImageURI(imgUri)
                        profileBannerImg = imgUri.toString()
//                        binding.profileBackImg.setImageURI(imgUri)
                    }
                }
                44 -> if (resultCode == RESULT_OK){
                    imgUri = data?.data!!
                    if (imgUri != null){
                        binding.profileImg.setImageURI(imgUri)
                    }
                }
            }
        }
    }

    private fun experienceInfoDialogView() {
        val expdialogView = layoutInflater.inflate(R.layout.dialog_experience_info,null)

        val edDesignation = expdialogView.findViewById<EditText>(R.id.designation)
        val edCompanyName = expdialogView.findViewById<EditText>(R.id.companyName)
        val edDuration = expdialogView.findViewById<EditText>(R.id.duration)

        alertDialogExperience = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(expdialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                experienceState = "Experienced"
                designation = edDesignation.text.toString().trim()
                prevCompany = edCompanyName.text.toString().trim()
                prevJobDuration = edDuration.text.toString().trim()

                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeExperienceData(
                        experienceState!!,
                        designation!!,
                        prevCompany!!,
                        prevJobDuration!!
                    )
                }
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogExperience.show()
    }

    private fun aboutInfoJDialogView() {
        val aboutdialogView = layoutInflater.inflate(R.layout.dialog_about_info,null)

        val edBio = aboutdialogView.findViewById<EditText>(R.id.bio)
        val edQualification = aboutdialogView.findViewById<EditText>(R.id.qualification)

        alertDialogAboutInfo = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(aboutdialogView)
            .setTitle("Change Basics")
            .setPositiveButton("Done") { dialog, _ ->
                bio = edBio.text.toString().trim()
                qualification = edQualification.text.toString().trim()

                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeAboutData(
                        bio!!,
                        qualification!!
                    )
                }
                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogAboutInfo.show()
    }

    private fun basicInfoDialogView() {
        val basicdialogView = layoutInflater.inflate(R.layout.dialog_profile_basic_info,null)


        val edUserName = basicdialogView.findViewById<EditText>(R.id.userName)
        val edExpertise = basicdialogView.findViewById<EditText>(R.id.expertise)
        val edCurrentCompany = basicdialogView.findViewById<EditText>(R.id.currentCompany)
        val edPhoneNo = basicdialogView.findViewById<EditText>(R.id.phoneNo)
        val edEmail = basicdialogView.findViewById<EditText>(R.id.email)
        alertDialogBasicInfo = AlertDialog.Builder(context,R.style.CustomAlertDialogStyle)
            .setView(basicdialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                name = edUserName.text.toString().trim()
                tageLine = edExpertise.text.toString().trim()
                currentCompany = edCurrentCompany.text.toString().trim()
                phoneNumber = edPhoneNo.text.toString().trim()
                emailId = edEmail.text.toString().trim()

                if (binding.userType.text.toString().trim() == "Job Seeker"){
                    CoroutineScope(IO).launch {
                        jobSeekerProfileInfo.storeBasicProfileData(
                            name!!,
                            phoneNumber!!,
                            emailId!!,
                            tageLine!!,
                            currentCompany!!
                        )
                    }
                }
                if (binding.userType.text.toString().trim() == "Recruiter"){
                    CoroutineScope(IO).launch {
                        recruiterProfileInfo.storeBasicProfileData(
                            name!!,
                            phoneNumber!!,
                            emailId!!,
                            tageLine!!,
                            currentCompany!!
                        )
                    }
                }

                Toast.makeText(context,"Data Saved",Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogBasicInfo.show()
    }

    private fun getSelectedRadioItem(radioGroup: RadioGroup,dialogView:View): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        val radioButton = dialogView.findViewById<View>(selectedItemId) as RadioButton
        if (selectedItemId != -1) {
            return radioButton.text.toString()
        }
        return ""
    }
    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}