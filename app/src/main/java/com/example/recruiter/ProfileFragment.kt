package com.example.recruiter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.example.recruiter.databinding.FragmentProfileBinding
import com.example.recruiter.store.JobSeekerProfileInfo
import com.example.recruiter.store.RecruiterProfileInfo
import com.example.recruiter.util.IS_LOGIN
import com.example.recruiter.util.PrefManager
import com.example.recruiter.util.PrefManager.set
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment(),View.OnClickListener {

    private lateinit var prefmanger: SharedPreferences
    private lateinit var alertDialogBasicInfo: AlertDialog;
    private lateinit var alertDialogAboutInfo: AlertDialog
    private lateinit var alertDialogExperience: AlertDialog;
    private lateinit var alertDialogResumeInfo: AlertDialog
    private lateinit var alertDialogRecruiterInfo: AlertDialog;
    private lateinit var alertDialogPrefInfo: AlertDialog
    private lateinit var alertDialogProfileBanner: AlertDialog;
    private lateinit var alertDialogProfileImg: AlertDialog

    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var type: String? = null
    private var id: String? = null
    private var fName: String? = null
    private var lName: String? = null
    private var phoneNumber: String? = null
    private var emailId: String? = null
    private var profileImg: String? = null
    private var profileImgUri: Uri? = null
    private var profileBannerImg: String? = null
    private var photoBitmap: Bitmap? = null
    private var profileBannerImgUri: Uri? = null
    private var tageLine: String? = null
    private var currentCompany: String? = null

    //jobSeeker Data
    private var bio: String? = null
    private var qualification: String? = null
    private var experienceState: String? = null
    private var designation: String? = null
    private var prevCompany: String? = null
    private var prevJobDuration: String? = null
    private var resumeUri: String? = null
    private var resumeFileName: String? = null
    private val resumeFirebaseUri: String? = null
    private var pdfUri: Uri? = null
    private var prefJobTitle: String? = null
    private var expectedSalary: String? = null
    private var prefJobLocation: String? = null
    private var prefWorkingMode: String? = null

    //Recruiter Data
    private var jobTitleR: String? = null
    private var salaryR: String? = null
    private var jobLocationR: String? = null
    private var jobDesR: String? = null
    private var designationR: String? = null
    private var workingModeR: String? = null

    private var map:MutableMap<String,String> ? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val context: Context = requireContext()
        jobSeekerProfileInfo = JobSeekerProfileInfo(context)
        recruiterProfileInfo = RecruiterProfileInfo(context)
        val bundle = arguments
        if (bundle != null) {
            type = bundle.getString("userType")
            binding.userType.text = type
        }
        prefmanger = PrefManager.prefManager(requireContext())
        id = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("$id", "$type")
        setProfileData()
        setOnClickListener()

        binding.toolbar.menu.clear()

        binding.toolbar.inflateMenu(R.menu.profile_menu)

        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.btnSearch -> {

                    val searchView = it.actionView as SearchView
                    
                    true
                }

                R.id.btnLogout -> {
                      logoutUser()
//                    makeToast("Logout",0)
                    true
                }

                else -> {
                    false
                }
            }
        }


        return binding.root
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        prefmanger[IS_LOGIN] = false
        activity?.finish()
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setProfileData() {

        if (type == "Job Seeker") {
            binding.groupJobSeeker.visibility = VISIBLE
            binding.groupRecruiter.visibility = GONE
            Log.d("isPermissionToShowImg", isGrantedPermission().toString())
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserProfileBannerImg().collect {
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        binding.profileBackImg.setImageBitmap(bitmap)
                    }
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserProfileImg().collect {

                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        binding.profileImg.setImageBitmap(bitmap)
                    }
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserFName().collect {
                    binding.userName.text = it
                    fName = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserLName().collect {
                    val fullName = "${binding.userName.text} $it"
                    binding.userName.text = fullName
                    lName = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPhoneNumber().collect {
                    phoneNumber = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserEmailId().collect {
                    emailId = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserTageLine().collect {
                    binding.expertise.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserCurrentCompany().collect {
                    binding.currentCompany.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserBio().collect {
                    binding.bioJ.setText(it)
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserQualification().collect {
                    binding.qualificationJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserExperienceState().collect {

                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserDesignation().collect {
                    binding.designationJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrevCompany().collect {
                    binding.companyNameJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrevJobDuration().collect {
                    binding.duration.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserResumeFileName().collect {
                    binding.resumeFileNameJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserResumeUri().collect {

                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPerfJobTitle().collect {
                    binding.jobTitleJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserExpectedSalary().collect {
                    binding.salaryJ.text = "$it LPA"
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrefJobLocation().collect {
                    binding.jobLocationJ.text = it
                }
            }
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserPrefWorkingMode().collect {
                    binding.workingModeJ.text = it
                }
            }
        }
        if (type == "Recruiter") {
            binding.groupJobSeeker.visibility = GONE
            binding.groupRecruiter.visibility = VISIBLE
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileBannerImg().collect {
//                        val imageUri: Uri? = if (it.isNotEmpty()) Uri.parse(it) else null
//                        binding.profileBackImg.setImageURI(imageUri)
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        binding.profileBackImg.setImageBitmap(bitmap)
                    }
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileImg().collect {
//                        val imageUri: Uri? = if (it.isNotEmpty()) Uri.parse(it) else null
//                        binding.profileBackImg.setImageURI(imageUri)
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        binding.profileImg.setImageBitmap(bitmap)
                    }
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileBannerImg().collect {
                    binding.profileBackImg.setImageURI(it.toUri())
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileImg().collect {
                    binding.profileImg.setImageURI(it.toUri())
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserFName().collect {
                    binding.userName.text = it
                    fName = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserLName().collect {
                    val fullName = "${binding.userName.text} $it"
                    binding.userName.text = fullName
                    lName = it
                }
            }
            
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserPhoneNumber().collect {
                    phoneNumber = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserEmailId().collect {
                    emailId = it
                }
            }
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserTageLine().collect {
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
                    binding.salaryR.text = "$it LPA"
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

    private fun setOnClickListener() {
        binding.profileBackImg.setOnClickListener(this)
        binding.addProfileImg.setOnClickListener(this)
        binding.editBasicInfo.setOnClickListener(this)
        binding.editAboutJ.setOnClickListener(this)
        binding.editExperienceJ.setOnClickListener(this)
        binding.editResumeJ.setOnClickListener(this)
        binding.editJobPrefJ.setOnClickListener(this)
        binding.editInfoR.setOnClickListener(this)
        binding.btnPhone.setOnClickListener(this)
        binding.btnEmail.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profileBackImg -> {
                profileBannerDialogView(binding.userType.text.toString())
            }

            R.id.addProfileImg -> {
                profileImgDialogView(binding.userType.text.toString())
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
            R.id.btnPhone -> {
                makePhoneCall(phoneNumber!!)
            }
            R.id.btnEmail -> {
                makeEmail(emailId!!)
            }
        }
    }


    private fun recruiterAboutInfoDialogView() {
        val recruiterInfoDialogView = layoutInflater.inflate(R.layout.dialog_recruter_info, null)

        val edJobTitle = recruiterInfoDialogView.findViewById<EditText>(R.id.jobTitle)
        val edSalary = recruiterInfoDialogView.findViewById<EditText>(R.id.salary)
        val edJobLobLocation = recruiterInfoDialogView.findViewById<EditText>(R.id.jobLocation)
        val edJobDescription = recruiterInfoDialogView.findViewById<EditText>(R.id.jobDes)
        val edDesignation = recruiterInfoDialogView.findViewById<EditText>(R.id.designation)
        val radioGrpWorkingMode =
            recruiterInfoDialogView.findViewById<RadioGroup>(R.id.radioGrpWorkingMode)

        alertDialogRecruiterInfo = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(recruiterInfoDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->
                jobTitleR = edJobTitle.text.toString().trim()
                salaryR = edSalary.text.toString().trim()
                jobLocationR = edJobLobLocation.text.toString().trim()
                jobDesR = edJobDescription.text.toString().trim()
                designationR = edDesignation.text.toString().trim()

                workingModeR = getSelectedRadioItem(radioGrpWorkingMode, recruiterInfoDialogView)

                CoroutineScope(IO).launch {
                    recruiterProfileInfo.storeAboutData(
                        jobTitleR!!,
                        salaryR!!,
                        jobLocationR!!,
                        jobDesR!!,
                        designationR!!,
                        workingModeR!!
                    )
                }
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogRecruiterInfo.show()
    }

    private fun jobPrefInfoDialogView() {
        val jobPrefDialogView = layoutInflater.inflate(R.layout.dialog_job_preference_info, null)

        val edJobTitle = jobPrefDialogView.findViewById<EditText>(R.id.jobTitle)
        val edExpectedSalary = jobPrefDialogView.findViewById<EditText>(R.id.salary)
        val edJobLocation = jobPrefDialogView.findViewById<EditText>(R.id.jobLocation)
        val radioGroupWorkingMode =
            jobPrefDialogView.findViewById<RadioGroup>(R.id.radioGrpWorkingMode)


        alertDialogPrefInfo = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(jobPrefDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                prefJobTitle = edJobTitle.text.toString()

                expectedSalary =
                    edExpectedSalary.text.toString()
                prefJobLocation = edJobLocation.text.toString()
                prefWorkingMode = getSelectedRadioItem(radioGroupWorkingMode, jobPrefDialogView)



                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeJobPreferenceData(
                        prefJobTitle!!,
                        expectedSalary!!,
                        prefJobLocation!!,
                        prefWorkingMode!!
                    )
                }
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogPrefInfo.show()
    }

    private lateinit var tvResumeFileName: TextView
    private lateinit var uploadProgressBar: ProgressBar
    private fun resumeInfoDialogView() {
        val resumeDialogView = layoutInflater.inflate(R.layout.dialog_resume_info, null)

//        resumeUri = pdfUri.toString()

        val btnUpload = resumeDialogView.findViewById<ShapeableImageView>(R.id.btnUpload)

        tvResumeFileName = resumeDialogView.findViewById<TextView>(R.id.resumeFileName)
        btnUpload.setOnClickListener{
            selectPdf()
        }

        uploadProgressBar = resumeDialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)
        uploadProgressBar.visibility = GONE

        alertDialogResumeInfo = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(resumeDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                CoroutineScope(IO).launch {
                    jobSeekerProfileInfo.storeResumeData(
                        resumeFileName!!,
                        resumeUri!!
                    )
                }
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialogResumeInfo.show()

    }

    private lateinit var profileBackImg: ImageView
    private lateinit var btnSelectImg: ImageView
    private fun profileBannerDialogView(userType: String) {
        val profileBannerDialogView =
            layoutInflater.inflate(R.layout.dialog_profile_cover_img, null)

        profileBackImg = profileBannerDialogView.findViewById(R.id.profileBackImg)
        if (userType == "Job Seeker"){
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserProfileBannerImg().collect {
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        profileBackImg.setImageBitmap(bitmap)
                    }
                }
            }
        }
        else{
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileBannerImg().collect {
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        profileBackImg.setImageBitmap(bitmap)
                    }
                }
            }
        }
        btnSelectImg = profileBannerDialogView.findViewById(R.id.btnChangeImg)
        btnSelectImg.setOnClickListener {
            if (isGrantedPermission()) {
                makeToast("hello please click",0)
                selectImg("bannerImg")
            } else {
                requestPermissions("bannerImg")
            }
        }



        alertDialogProfileBanner = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setTitle("Change Banner Image")
            .setView(profileBannerDialogView)
            .setPositiveButton("Done") { dialog, _ ->
                val baos = ByteArrayOutputStream()
                photoBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val b = baos.toByteArray()
                val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)
                profileBannerImg = encodedImage
                Log.d("Img Encoded String..", profileBannerImg!!)
                CoroutineScope(IO).launch {
                    if(userType == "Job Seeker"){
                        jobSeekerProfileInfo.storeProfileBannerImg(
                            profileBannerImg!!
                        )
                    }
                    if(userType == "Recruiter"){
                        recruiterProfileInfo.storeProfileBannerImg(
                            profileBannerImg!!
                        )
                    }

                }
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogProfileBanner.show()

    }
    private lateinit var profileImgDia: ImageView
    private lateinit var btnChangeImg: ImageView
    private fun profileImgDialogView(userType: String) {
        val profileImgDialogView =
            layoutInflater.inflate(R.layout.dialog_profile_img, null)

        profileImgDia = profileImgDialogView.findViewById(R.id.profileImg)
        if (userType == "Job Seeker"){
            lifecycle.coroutineScope.launch {
                jobSeekerProfileInfo.getUserProfileImg().collect {
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        profileImgDia.setImageBitmap(bitmap)
                    }
                }
            }
        }
        else{
            lifecycle.coroutineScope.launch {
                recruiterProfileInfo.getUserProfileImg().collect {
                    val previouslyEncodedImage: String = it
                    if (!previouslyEncodedImage.equals("", ignoreCase = true)) {
                        val b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
                        profileImgDia.setImageBitmap(bitmap)
                    }
                }
            }
        }
        btnChangeImg = profileImgDialogView.findViewById(R.id.btnChangeImg)
        btnChangeImg.setOnClickListener {
            if (isGrantedPermission()) {
                selectImg("profileImg")
            } else {
                requestPermissions("profileImg")
            }
        }

        alertDialogProfileImg = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setTitle("Change Banner Image")
            .setView(profileImgDialogView)
            .setPositiveButton("Done") { dialog, _ ->
                val baos = ByteArrayOutputStream()
                photoBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val b = baos.toByteArray()
                val encodedImage: String = Base64.encodeToString(b, Base64.DEFAULT)
                profileImg = encodedImage
                Log.d("Img Encoded String..", profileImg!!)

                CoroutineScope(IO).launch {
                    if(userType == "Job Seeker"){
                        jobSeekerProfileInfo.storeProfileImg(
                            profileImg!!
                        )
                    }
                    if(userType == "Recruiter"){
                        recruiterProfileInfo.storeProfileImg(
                            profileImg!!
                        )
                    }

                }
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogProfileImg.show()

    }

    private fun experienceInfoDialogView() {
        val expDialogView = layoutInflater.inflate(R.layout.dialog_experience_info, null)

        val edDesignation = expDialogView.findViewById<EditText>(R.id.designation)
        val edCompanyName = expDialogView.findViewById<EditText>(R.id.companyName)
        val edDuration = expDialogView.findViewById<EditText>(R.id.duration)

        alertDialogExperience = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(expDialogView)
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
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogExperience.show()
    }

    private fun aboutInfoJDialogView() {
        val aboutDialogView = layoutInflater.inflate(R.layout.dialog_about_info, null)

        val edBio = aboutDialogView.findViewById<EditText>(R.id.bio)
        val edQualification = aboutDialogView.findViewById<EditText>(R.id.qualification)

        alertDialogAboutInfo = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(aboutDialogView)
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
//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogAboutInfo.show()
    }

    private fun basicInfoDialogView() {
        val basicDialogView = layoutInflater.inflate(R.layout.dialog_profile_basic_info, null)


        val edUserFName = basicDialogView.findViewById<EditText>(R.id.userFName)
        val edUserLName = basicDialogView.findViewById<EditText>(R.id.userLName)
        val edExpertise = basicDialogView.findViewById<EditText>(R.id.expertise)
        val edCurrentCompany = basicDialogView.findViewById<EditText>(R.id.currentCompany)
        val edPhoneNo = basicDialogView.findViewById<EditText>(R.id.phoneNo)
        val edEmail = basicDialogView.findViewById<EditText>(R.id.email)
        alertDialogBasicInfo = AlertDialog.Builder(context, R.style.CustomAlertDialogStyle)
            .setView(basicDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                fName = edUserFName.text.toString().trim()
                lName = edUserLName.text.toString().trim()
                tageLine = edExpertise.text.toString().trim()
                currentCompany = edCurrentCompany.text.toString().trim()
                phoneNumber = edPhoneNo.text.toString().trim()
                emailId = edEmail.text.toString().trim()

                if (binding.userType.text.toString().trim() == "Job Seeker") {
                    CoroutineScope(IO).launch {
                        jobSeekerProfileInfo.storeBasicProfileData(
                            fName!!,
                            lName!!,
                            phoneNumber!!,
                            emailId!!,
                            tageLine!!,
                            currentCompany!!
                        )
                    }
                }
                if (binding.userType.text.toString().trim() == "Recruiter") {
                    CoroutineScope(IO).launch {
                        recruiterProfileInfo.storeBasicProfileData(
                            fName!!,
                            lName!!,
                            phoneNumber!!,
                            emailId!!,
                            tageLine!!,
                            currentCompany!!
                        )
                    }
                }

//                Toast.makeText(context, "Data Saved", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogBasicInfo.show()
    }


    private fun selectImg(s: String) {
        val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
        imgIntent.type = "image/*"
        imgIntent.addCategory(Intent.CATEGORY_OPENABLE)
        if (s == "bannerImg") {
            startActivityForResult(imgIntent, 24)
        }
        if (s == "profileImg") {
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
                                resumeFileName =
                                    myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                resumeUri = pdfUri.toString()
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
                        }, 100)
                    }, 1000)

                }

                24 -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!
                    if (photoUri != null) {

                        val context = requireContext()
                        val contentResolver:ContentResolver = context.contentResolver
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                        photoBitmap = Bitmap.createScaledBitmap(bitmap, profileBackImg.width, profileBackImg.height, false)
                        profileBackImg.setImageBitmap(photoBitmap)

                    }
                }

                44 -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!
                    if (photoUri != null) {

                        val context = requireContext()
                        val contentResolver:ContentResolver = context.contentResolver
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
                        photoBitmap = Bitmap.createScaledBitmap(bitmap, profileImgDia.width, profileImgDia.height, false)
                        profileImgDia.setImageBitmap(photoBitmap)
                    }
                }
            }
        }
    }

    private fun makeEmail(emailId:String){
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        intent.type = "message/rfc822"
        startActivity(Intent.createChooser(intent, "Choose an Email Client: "))
    }
    private fun makePhoneCall(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(dialIntent)
    }

    private fun getSelectedRadioItem(radioGroup: RadioGroup, dialogView: View): String {
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
        val updateDataServiceIntent = Intent(activity,UpdateProfileDataService::class.java)
        updateDataServiceIntent.putExtra("userType",type)
        updateDataServiceIntent.putExtra("userId",id)
        activity?.startService(updateDataServiceIntent)
    }

    private fun requestPermissions(s: String) {
        val permissions: Collection<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        Log.d("####", "requestPermissions: $permissions")
        Dexter.withContext(requireContext()).withPermissions(
            permissions
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                Log.d("####", "onPermissionsChecked: $report")
                if (report?.areAllPermissionsGranted()!!) {
                    Log.d("permissions###", "permission granted")
                    selectImg(s)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("Version*", Build.VERSION.SDK_INT.toString())
            listOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
            val isGranted1 =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            val isGranted2 =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            return isGranted1 == PackageManager.PERMISSION_GRANTED && isGranted2 == PackageManager.PERMISSION_GRANTED
        } else {
            Log.d("Version**", Build.VERSION.SDK_INT.toString())
            val isGranted1 =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            val isGranted2 =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            val isGranted3 =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)

            return isGranted1 == PackageManager.PERMISSION_GRANTED && isGranted2 == PackageManager.PERMISSION_GRANTED && isGranted3 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }
    
}