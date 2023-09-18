package com.amri.emploihunt.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityProfileBinding
import com.amri.emploihunt.model.CommonMessageModel
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.GetUserById
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.UserDataRepository
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.SELECT_PROFILE_BANNER_IMG
import com.amri.emploihunt.util.SELECT_PROFILE_IMG
import com.amri.emploihunt.util.SELECT_RESUME_FILE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chivorn.smartmaterialspinner.SmartMaterialSpinner
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignTop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File


@AndroidEntryPoint
class ProfileActivity : BaseActivity(),OnClickListener,UpdateSeverHelperClass.UpdateProfileDataListener {
    private var _binding: ActivityProfileBinding? = null

    private lateinit var prefmanger: SharedPreferences

    companion object{
        const val TAG = "ProfileActivity"
    }

    private lateinit var alertDialogBasicInfo: AlertDialog
    private lateinit var alertDialogAboutInfo: AlertDialog
    private lateinit var alertDialogQualification: AlertDialog
    private lateinit var alertDialogExperience: AlertDialog
    private lateinit var alertDialogResumeInfo: AlertDialog
    private lateinit var alertDialogRecruiterInfo: AlertDialog
    private lateinit var alertDialogProfileBanner: AlertDialog
    private lateinit var alertDialogProfileImg: AlertDialog

    private var addAboutImg: ShapeableImageView ?= null
    private var addQualificationImg: ShapeableImageView ?= null
    private var addExperienceImg: ShapeableImageView ?= null
    private var addResumeImg: ShapeableImageView ?= null
    private var addCurrentPosImg: ShapeableImageView ?= null

    private var callBalloon: Balloon ?= null
    private var emailBalloon: Balloon ?= null
    
    private lateinit var userDataRepository: UserDataRepository
    private val experienceViewModel: ExperienceViewModel by viewModels()

    private val binding get() = _binding!!

    private var userType: Int? = null
    private var userId: String? = null

   //common data
    private var fName: String? = null
    private var lName: String? = null
    private var fullName: String? = null
    private var phoneNumber: String? = null
    private var emailId: String? = null
    private var tagLine: String? = null

    private var residentialCity:String? = null

    private var profileImgUri: String? = null
    private lateinit var profileImgFile: File
    private var profileBannerImgUri: String? = null
    private lateinit var profileBannerFile: File
    //User Data
    private var bio: String? = null
    private var qualification: String? = null

    private var currentCompany: String? = null
    private var designation: String? = null
    private var jobLocation: String? = null
    private var workingMode:String? = null

    private lateinit var experienceList:MutableList<Experience>
    
    private var resumeUri: String? = null
    private lateinit var resumeFile:File
    private var resumeFileName: String? = null
    
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        prefmanger = PrefManager.prefManager(this)
        userId = prefmanger.get(FIREBASE_ID)
        userType = prefmanger.get(ROLE,0)
        Log.d("$userId", "$userType")

        userDataRepository = UserDataRepository(this)

        UpdateSeverHelperClass.instance!!.setListener(this)
        
        experienceList = mutableListOf()

        setProfileData()
        setOnClickListener()

        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                val user = User(
                    -1,
                    "",
                    userType!!,
                    fName!!,
                    lName!!,
                    phoneNumber!!,
                    emailId!!,
                    qualification!!,
                    "",
                    "",
                    currentCompany!!,
                    designation!!,
                    jobLocation!!,
                    "",
                    workingMode!!,
                    bio!!,
                    tagLine!!,
                    residentialCity!!,
                    "",
                    "",
                    "",
                    "",
                    -1,
                    "",
                    ""
                )

                val updateDataServiceIntent = Intent(this@ProfileActivity, UpdateProfileDataService::class.java)
                updateDataServiceIntent.putExtra("userObject",user)
                startService(updateDataServiceIntent)
                finish()
            }
        })

    }

    private fun setProfileData() {


        /** profile banner */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserProfileBannerUrl().collect {
                Log.d(TAG, "setProfileData: trying to update profile banner data $it")
                profileBannerImgUri = it
                Glide.with(this@ProfileActivity)
                    .load(profileBannerImgUri)
                    .apply(
                        RequestOptions
                            .placeholderOf(R.drawable.profile_default_back_img)
                            .error(R.drawable.profile_default_back_img)
                            .fitCenter()

                    )
                    .into(binding.profileBackImg)
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: profile banner data is updated")
        }
        /** profile Img */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserProfileImgUrl().collect {
                Log.d(TAG, "setProfileData: trying to update profile img data $it")
                profileImgUri = it
                Glide.with(this@ProfileActivity)
                    .load(profileImgUri)
                    .apply(
                        RequestOptions
                            .placeholderOf(R.drawable.profile_default_image)
                            .error(R.drawable.profile_default_image)
                            .circleCrop()
                    )
                    .into(binding.profileImg)
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: profile img data is updated")
        }
        /** FName */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserFName().collect {
                Log.d(TAG, "setProfileData: trying to update fName data $it")
                fName = it
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: first name data is updated")
        }
        /** LName */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserLName().collect {
                Log.d(TAG, "setProfileData: trying to update LName data $it")
                lName = it
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: Last name data is updated")
        }
        /** full Name */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserFullName().collect{
                Log.d(TAG, "setProfileData: trying to update fullName data $it")
                showViewIfNotEmpty(it,binding.userName)
                fullName = it
                binding.userName.text = fullName
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: Full name data is updated")
        }
        /** phone number */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserPhoneNumber().collect { phoneNo ->
                Log.d(TAG, "setProfileData: trying to update phone no data $phoneNo")
                phoneNumber = phoneNo
                if (phoneNo.isNotEmpty()) {
                    callBalloon = createMsgBalloon(phoneNo, R.drawable.ic_call, baseContext)
                    if (callBalloon != null) {
                        callBalloon!!.setOnBalloonClickListener {
                            makePhoneCall(phoneNo)
                            callBalloon!!.dismiss()
                        }
                        callBalloon!!.setOnBalloonOutsideTouchListener { view, motionEvent ->
                            callBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                } else {
                    binding.btnPhone.foreground =
                        ContextCompat.getDrawable(baseContext, R.drawable.glass_effect)
                    callBalloon = createMsgBalloon(
                        "Phone no. Not Found",
                        R.drawable.ic_call,
                        baseContext
                    )
                    if (callBalloon != null) {
                        callBalloon!!.setOnBalloonOutsideTouchListener { view, motionEvent ->
                            callBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                }

            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: phone no data is updated")
        }
        /** email id*/
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserEmailId().collect { email ->
                Log.d(TAG, "setProfileData: trying to update email data $email")
                emailId = email
                if (email.isNotEmpty()) {
                    emailBalloon = createMsgBalloon(email, R.drawable.ic_email, baseContext)

                    if (emailBalloon != null) {
                        emailBalloon!!.setOnBalloonClickListener {
                            makeEmail(email)
                            emailBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                } else {
                    emailBalloon = createMsgBalloon(
                        "Email Id Not Found",
                        R.drawable.ic_email,
                        baseContext
                    )
                    if (emailBalloon != null) {
                        emailBalloon!!.setOnBalloonOutsideTouchListener { view, motionEvent ->
                            emailBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                }

            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: email data is updated")
        }
        /** tagLine */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserTageLine().collect {
                Log.d(TAG, "setProfileData: trying to update tagline data $it")
                showViewIfNotEmpty(it, binding.expertise)
                tagLine = it
                binding.expertise.text = tagLine
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: Tage line data is updated")
        }
        /** current company */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserCurrentCompany().collect {
                Log.d(TAG, "setProfileData: trying to update current company data $it")
                showViewIfNotEmpty(it,binding.currentCompany)
                currentCompany = it
                binding.currentCompany.text = currentCompany
                if(userType == 1){
                    showViewIfNotEmpty(it,binding.companyNameR)
                    binding.companyNameR.text = currentCompany
                }
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: current Company: data is updated")
        }
        /** residential City*/
        lifecycle.coroutineScope.launch {
            userDataRepository.getResidentialCity().collect{
                Log.d(TAG, "setProfileData: trying to update residential city data $it")
                showViewIfNotEmpty(it,binding.residentialCity)
                residentialCity = it
                binding.residentialCity.text = residentialCity
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: residential city data is updated")
        }
        /** Bio */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserBio().collect {
                Log.d(TAG, "setProfileData: trying to update bio data $it")
                bio = it
                if(userType == 0) {
                    showViewIfNotEmpty(it,binding.bioJ)
                    binding.bioJ.setText(bio)
                    addAboutImg = createAddDataImg(binding.aboutInfoLayoutJ, R.id.txtAboutJ)

                    if (addAboutImg != null) {
                        decideAddImgToVisibility(bio.isNullOrEmpty(), binding.bioJ, addAboutImg!!)
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addAboutImg is null")
                    }
                }
                else{
                    showViewIfNotEmpty(it,binding.bioR)
                    binding.bioR.setText(bio)
                    addAboutImg = createAddDataImg(binding.aboutInfoLayoutR, R.id.txtAboutR)

                    if (addAboutImg != null) {
                        decideAddImgToVisibility(bio.isNullOrEmpty(), binding.bioR, addAboutImg!!)
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addAboutImg is null")
                    }
                }

            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: About data is updated")
        }
        /** qualification */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserQualification().collect {
                Log.d(TAG, "setProfileData: trying to update qualification data $it")
                qualification = it
                if(userType == 0) {
                    showViewIfNotEmpty(it,binding.qualificationJ)

                    binding.qualificationJ.text = qualification
                    addQualificationImg =
                        createAddDataImg(binding.qualificationLayoutJ, R.id.txtQualificationJ)

                    if (addQualificationImg != null) {
                        decideAddImgToVisibility(
                            qualification.isNullOrEmpty(),
                            binding.qualificationJ,
                            addQualificationImg!!
                        )
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addQualificationImg is null")
                    }
                }
                else{
                    showViewIfNotEmpty(it,binding.qualificationJ)

                    binding.qualificationR.text = qualification
                    addQualificationImg =
                        createAddDataImg(binding.qualificationLayoutR, R.id.txtQualificationR)

                    if (addQualificationImg != null) {
                        decideAddImgToVisibility(
                            qualification.isNullOrEmpty(),
                            binding.qualificationR,
                            addQualificationImg!!
                        )
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addQualificationImg is null")
                    }
                }
            }
        } .invokeOnCompletion {
            Log.d(TAG, "setProfileData: Qualification data is updated")
        }
        
        /** Designation */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserDesignation().collect {
                Log.d(TAG, "setProfileData: trying to update designation data $it")
                designation = it
                showViewIfNotEmpty(it, binding.designationR)
                binding.designationR.text = designation
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: Designation data is updated")
        }
        /** Job Location */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserJobLocation().collect {
                Log.d(TAG, "setProfileData: trying to update job location data $it")
                jobLocation = it
                showViewIfNotEmpty(it, binding.jobLocation)
                binding.jobLocation.text = jobLocation
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: JobLocation data is updated")
        }
        /** working mode */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserWorkingMode().collect {
                Log.d(TAG, "setProfileData: trying to update working mode data $it")
                workingMode = it
                if (userType == 1) {
                    showViewIfNotEmpty(it, binding.workingModeR)
                    binding.workingModeR.text = workingMode
                    addCurrentPosImg =
                        createAddDataImg(binding.currPosLayoutR, R.id.txtCurrentPositionR)
                    if (addCurrentPosImg != null) {
                        val b =
                            (currentCompany.isNullOrEmpty() && designation.isNullOrEmpty() && jobLocation.isNullOrEmpty() && workingMode.isNullOrEmpty())
                        decideAddImgToVisibility(b, binding.positionLayoutR, addCurrentPosImg!!)
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addAboutImg is null")
                    }
                }
            }
        }.invokeOnCompletion {

            Log.d(TAG, "setProfileData: Working mode R data is updated")
        }
        /** Experience */
        if(userType == 0) {
            lifecycle.coroutineScope.launch {
                Log.d(TAG, "setProfileData: trying to update experience data")
                addExperienceImg = createAddDataImg(binding.experienceLayout, R.id.txtExperienceJ)
                experienceList.clear()
                experienceViewModel.readFromLocal().collect { list ->
                    Log.d(TAG, "setProfileData: Experience $list")

                    for (experience in list.toMutableList()) {
                        experienceList.add(experience)
                    }

                    if (addExperienceImg != null) {
                        decideAddImgToVisibility(
                            experienceList.isEmpty(),
                            binding.dataLayout,
                            addExperienceImg!!
                        )
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addExperienceImg is null")
                    }
                    Log.d(TAG, "setProfileData: Experience data \n $experienceList")
                    setExperiences()
                }
            }.invokeOnCompletion {
                Log.d(TAG, "setProfileData: experience data is updated")
            }
        }
        /** Resume file */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserResumeUri().collect {
                Log.d(TAG, "setProfileData: trying to update resume data $it")
                if(userType == 0) {
                    resumeUri = it
                    showViewIfNotEmpty(it,binding.resumeFileNameJ)
                    addResumeImg = createAddDataImg(binding.resumeLayout, R.id.txtResumeJ)
                    Log.d(TAG, "setProfileData: $fName")
                    if (fName != null) {
                        resumeFileName = fName!!.plus("'s ").plus("Resume")
                        binding.resumeFileNameJ.text = resumeFileName
                    }
                    else{
                        resumeFileName = "Resume"
                        binding.resumeFileNameJ.text = resumeFileName
                    }
                    if (addResumeImg != null) {

                        decideAddImgToVisibility(
                            resumeUri.isNullOrEmpty(),
                            binding.resumeFileNameJ,
                            addResumeImg!!
                        )
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addResumeImg is null")
                    }
                }
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: resume file data is updated")
        }

        if (userType == 0) {
            binding.groupJobSeeker.visibility = VISIBLE
            binding.groupRecruiter.visibility = GONE
            binding.userType.text = getString(R.string.job_seeker)
        }
        else{
            binding.groupJobSeeker.visibility = GONE
            binding.groupRecruiter.visibility = VISIBLE
            binding.userType.text = getString(R.string.recruiter)
        }
        Log.d(TAG, "setProfileData: profile data updated")
    }

    private fun setExperiences(){
        binding.dataLayout.removeAllViews()
        binding.btnShowMore.isActivated = false
        binding.btnShowMore.text = getString(R.string.show_more)
        if (experienceList.isEmpty()){
            binding.dataLayout.visibility = GONE

        }
        else{

            binding.dataLayout.visibility = VISIBLE

            for (index in 0 until 3) {
                if(index < experienceList.size){
                    val experience = experienceList[index]
                    val experienceRow = layoutInflater.inflate(R.layout.row_experience, null)

                    val designationTextView = experienceRow.findViewById<TextView>(R.id.designation)
                    val companyNameTextView = experienceRow.findViewById<TextView>(R.id.companyName)
                    val jobLocationTextView = experienceRow.findViewById<TextView>(R.id.jobLocation)
                    val durationTextView = experienceRow.findViewById<TextView>(R.id.duration)
                    val btnDelete = experienceRow.findViewById<FloatingActionButton>(R.id.btnDelete)
                    btnDelete.visibility = GONE
                    designationTextView.text = experience.vDesignation
                    companyNameTextView.text = experience.vCompanyName
                    jobLocationTextView.text = experience.vJobLocation
                    if(experience.vDuration.isNotEmpty()){
                        durationTextView.text = experience.vDuration.plus(" Years")
                    }
                    binding.dataLayout.addView(experienceRow)
                }
                else{
                    break
                }
            }
            if(experienceList.size > 3){
                binding.btnShowMore.visibility = VISIBLE
            }
            else{
                binding.btnShowMore.visibility = GONE
            }
        }

    }

    private fun decideAddImgToVisibility(
        dataState: Boolean,
        view: View,
        addImg: ShapeableImageView
    ) {
        if(dataState){
            Log.d(TAG, "decideAddImgToVisibility: ${true}")
            view.visibility = GONE
            addImg.visibility = VISIBLE

        }
        else{
            Log.d(TAG, "decideAddImgToVisibility: ${false}")
            view.visibility = VISIBLE
            addImg.visibility = GONE
        }
    }

    private fun showViewIfNotEmpty(value: String, view: View) {
        if(value.isNotEmpty()){
            view.visibility = VISIBLE
        }
        else{
            view.visibility = GONE
        }
    }

    private fun createAddDataImg(layout:ConstraintLayout,view:Int):ShapeableImageView{

        val imageView = ShapeableImageView(baseContext)
        imageView.id = View.generateViewId()
        imageView.layoutParams = ConstraintLayout.LayoutParams(
            0,
            200
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageResource(R.drawable.default_add_data_icon)

        layout.addView(imageView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)
        constraintSet.connect(
            imageView.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            0
        )
        constraintSet.connect(
            imageView.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END,
            0
        )
        constraintSet.connect(
            imageView.id,
            ConstraintSet.TOP,
            view,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            imageView.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.applyTo(layout)
        imageView.visibility = GONE
        return imageView
    }

    private fun createMsgBalloon(msg: String, icon: Int, baseContext: Context): Balloon {
        val balloon = createBalloon(baseContext){
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setText(msg)
            setText(msg)
            setTextSize(8f)
            setTextTypeface(Typeface.BOLD)
            setTextColorResource(R.color.black)
            setTextGravity(Gravity.CENTER)
            setIconDrawableResource(icon)
            setIconHeight(12)
            setIconWidth(12)
            setIconColorResource(R.color.black)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowSize(8)
            setArrowPosition(0.5f)
            setPadding(12)
            setCornerRadius(8f)
            setBackgroundColorResource(R.color.white)
            setElevation(3)
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(lifecycleOwner)
            build()
        }

        return balloon
    }

    private fun setOnClickListener() {
        binding.btnPhone.setOnClickListener(this)
        binding.btnEmail.setOnClickListener(this)
        binding.profileBackImg.setOnClickListener(this)
        binding.addProfileImg.setOnClickListener(this)
        binding.editBasicInfo.setOnClickListener(this)
        binding.editAboutJ.setOnClickListener(this)
        binding.editQualificationJ.setOnClickListener(this)
        binding.editExperienceJ.setOnClickListener(this)
        binding.btnShowMore.setOnClickListener(this)
        binding.editResumeJ.setOnClickListener(this)
        binding.editAboutR.setOnClickListener(this)
        binding.editCurrPosR.setOnClickListener(this)
        binding.editQualificationR.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profileBackImg -> {
                profileBannerDialogView()
            }

            R.id.addProfileImg -> {
                profileImgDialogView()
            }

            R.id.editBasicInfo -> {
                basicInfoDialogView()
            }
            R.id.editAboutJ -> {
                addAboutImg!!.visibility = GONE
                aboutInfoJDialogView()
            }
            R.id.editQualificationJ ->{
                addQualificationImg!!.visibility = GONE
                qualificationDialogView()
            }
            R.id.editExperienceJ -> {
                addExperienceImg!!.visibility = GONE
                experienceInfoDialogView()
            }
            R.id.btnShowMore -> {

                if (!binding.btnShowMore.isActivated) {
                    if(experienceList.size > 3){
                        for (index in 3 until experienceList.size)  {
                            val experience = experienceList[index]
                            val experienceRow = layoutInflater.inflate(R.layout.row_experience, null)

                            val designationTextView = experienceRow.findViewById<TextView>(R.id.designation)
                            val companyNameTextView = experienceRow.findViewById<TextView>(R.id.companyName)
                            val jobLocationTextView = experienceRow.findViewById<TextView>(R.id.jobLocation)
                            val durationTextView = experienceRow.findViewById<TextView>(R.id.duration)
                            val btnDelete = experienceRow.findViewById<FloatingActionButton>(R.id.btnDelete)
                            btnDelete.visibility = GONE

                            designationTextView.text = experience.vDesignation
                            companyNameTextView.text = experience.vCompanyName
                            jobLocationTextView.text = experience.vJobLocation
                            if(experience.vDuration.isNotEmpty()) {
                                durationTextView.text =
                                    experience.vDuration.plus(" Years")
                            }

                            binding.dataLayout.addView(experienceRow)
                        }
                        binding.btnShowMore.text = getString(R.string.show_less)
                    }
                }
                else{
                    if(experienceList.size > 3) {
                        binding.dataLayout.removeAllViews()
                        for (index in 0 until 3) {
                            val experience = experienceList[index]
                            val experienceRow = layoutInflater.inflate(R.layout.row_experience, null)

                            val designationTextView = experienceRow.findViewById<TextView>(R.id.designation)
                            val companyNameTextView = experienceRow.findViewById<TextView>(R.id.companyName)
                            val jobLocationTextView = experienceRow.findViewById<TextView>(R.id.jobLocation)
                            val durationTextView = experienceRow.findViewById<TextView>(R.id.duration)
                            val btnDelete = experienceRow.findViewById<FloatingActionButton>(R.id.btnDelete)
                            btnDelete.visibility = GONE

                            designationTextView.text = experience.vDesignation
                            companyNameTextView.text = experience.vCompanyName
                            jobLocationTextView.text = experience.vJobLocation
                            if(experience.vDuration.isNotEmpty()) {
                                durationTextView.text =
                                    experience.vDuration.plus(" Years")
                            }

                            binding.dataLayout.addView(experienceRow)
                        }
                    }
                    binding.btnShowMore.text = getString(R.string.show_more)
                }
                binding.btnShowMore.isActivated = !binding.btnShowMore.isActivated

            }

            R.id.editResumeJ -> {
                addResumeImg!!.visibility = GONE
                resumeInfoDialogView()
            }
            R.id.editAboutR -> {
                addAboutImg!!.visibility = GONE
                aboutInfoJDialogView()
            }
            R.id.editQualificationR ->{
                addQualificationImg!!.visibility = GONE
                qualificationDialogView()
            }
            R.id.editCurrPosR -> {
                addCurrentPosImg!!.visibility = GONE
                currentPositionDialogView()
            }

            R.id.btnPhone -> {
                if(callBalloon != null){
                    binding.btnPhone.showAlignTop(callBalloon!!)
                }
            }
            R.id.btnEmail -> {
                if(emailBalloon != null){
                    binding.btnEmail.showAlignTop(emailBalloon!!)
                }
            }
        }
    }

    private fun currentPositionDialogView() {
        val currentPositionDialog = layoutInflater.inflate(R.layout.dialog_current_position_info, null)

        var selectedJobLocation = String()
        var selectedWorkingMode = String()

        val edCompanyName = currentPositionDialog.findViewById<EditText>(R.id.companyName)
        val edJobTitle = currentPositionDialog.findViewById<EditText>(R.id.designation)
        val spJobLocation = currentPositionDialog.findViewById<SmartMaterialSpinner<String>>(R.id.jobLocation)
        spJobLocation.setSearchDialogGravity(Gravity.TOP)
        spJobLocation.arrowPaddingRight = 19
        spJobLocation.item = resources.getStringArray(R.array.degree_array).toList()
        spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spJobLocation.isOutlined = true
                selectedJobLocation = spJobLocation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        val tbWorkingMode = currentPositionDialog.findViewById<TabLayout>(R.id.tbWorkingMode)
        tbWorkingMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedWorkingMode = tab?.text.toString()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectedWorkingMode = tab?.text.toString()
            }

        })


        alertDialogRecruiterInfo = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(currentPositionDialog)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->
                currentCompany = edCompanyName.text.toString().trim()
                designation = edJobTitle.text.toString().trim()
                jobLocation = selectedJobLocation.trim()
                workingMode = selectedWorkingMode.trim()


                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeCurrentPositionData(
                        currentCompany!!,
                        designation!!,
                        jobLocation!!,
                        workingMode!!
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                if(addCurrentPosImg != null){
                    val b = (currentCompany.isNullOrEmpty() && designation.isNullOrEmpty() && jobLocation.isNullOrEmpty() && workingMode.isNullOrEmpty())
                    decideAddImgToVisibility(b,binding.positionLayoutR,addCurrentPosImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }
            }
            .create()
        alertDialogRecruiterInfo.show()
    }

    private lateinit var tvResumeFileName: TextView
    private lateinit var uploadProgressBar: ProgressBar
    private fun resumeInfoDialogView() {
        val resumeDialogView = layoutInflater.inflate(R.layout.dialog_resume_info, null)

        val btnUpload = resumeDialogView.findViewById<ShapeableImageView>(R.id.btnUpload)

        tvResumeFileName = resumeDialogView.findViewById(R.id.resumeFileName)
        tvResumeFileName.text = resumeFileName!!
        btnUpload.setOnClickListener{
            selectPdf(SELECT_RESUME_FILE)
        }

        uploadProgressBar = resumeDialogView.findViewById(R.id.uploadProgressBar)
        uploadProgressBar.visibility = GONE

        alertDialogResumeInfo = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(resumeDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeResumeData(
                        resumeUri!!
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                if(addResumeImg != null){

                    decideAddImgToVisibility(resumeUri.isNullOrEmpty(), binding.resumeFileNameJ, addResumeImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }
            }
            .create()

        alertDialogResumeInfo.show()

    }

    private lateinit var profileBackImg: ImageView
    private lateinit var btnSelectImg: ImageView
    private fun profileBannerDialogView() {
        val profileBannerDialogView =
            layoutInflater.inflate(R.layout.dialog_profile_cover_img, null)

        profileBackImg = profileBannerDialogView.findViewById(R.id.profileBackImg)

        if(profileBannerImgUri != null) {
            Glide.with(this@ProfileActivity)
                .load(profileBannerImgUri)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.profile_default_back_img)
                        .error(R.drawable.profile_default_back_img)
                        .fitCenter()
                )
                .into(profileBackImg)
        }

        btnSelectImg = profileBannerDialogView.findViewById(R.id.btnChangeImg)
        btnSelectImg.setOnClickListener {

            val deniedPermissions:MutableList<String> = isGrantedPermission()

            if (deniedPermissions.isEmpty()) {
                selectImg(SELECT_PROFILE_BANNER_IMG)
            } else {
                requestPermissions(deniedPermissions) {
                    if (it) {
                        selectImg(SELECT_PROFILE_BANNER_IMG)
                    } else {
                        alertDialogProfileBanner.dismiss()
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

        alertDialogProfileBanner = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setTitle("Change Banner Image")
            .setView(profileBannerDialogView)
            .setPositiveButton("Done") { dialog, _ ->

                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeProfileBannerImg(
                        profileBannerImgUri!!
                    )
                }.invokeOnCompletion {
                    Log.d(
                        TAG,
                        "onActivityResult: Profile Banner Img is Stored in datastore"
                    )
                }
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
    private fun profileImgDialogView() {
        val profileImgDialogView =
            layoutInflater.inflate(R.layout.dialog_profile_img, null)

        profileImgDia = profileImgDialogView.findViewById(R.id.profileImg)

        if(profileImgUri != null) {
            Glide.with(this@ProfileActivity)
                .load(profileImgUri)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.profile_default_image)
                        .error(R.drawable.profile_default_image)
                        .circleCrop()
                )
                .into(profileImgDia)
        }

        btnChangeImg = profileImgDialogView.findViewById(R.id.btnChangeImg)
        btnChangeImg.setOnClickListener {
            val deniedPermissions:MutableList<String> = isGrantedPermission()

            if (deniedPermissions.isEmpty()) {
                selectImg(SELECT_PROFILE_IMG)
            } else {
                requestPermissions(deniedPermissions){
                    if (it) {
                        selectImg(SELECT_PROFILE_IMG)
                    }
                    else{
                        alertDialogProfileImg.dismiss()
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

        alertDialogProfileImg = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setTitle("Change Image")
            .setView(profileImgDialogView)
            .setPositiveButton("Done") { dialog, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeProfileImg(
                        profileImgUri!!,
                    )
                }.invokeOnCompletion {
                        Log.d(
                            TAG,
                            "onActivityResult: Profile Img is Stored in datastore"
                        )
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogProfileImg.show()

    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun experienceInfoDialogView() {
        Log.d(TAG, "experienceInfoDialogView: experience list \n $experienceList")
        val expDialogView = layoutInflater.inflate(R.layout.dialog_experience_info, null)

        var selectedDesignation = String()
        var enteredCompanyName = String()
        var selectedJobLocation = String()
        var enteredDuration = String()
        val spDesignation = expDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.designation)
        spDesignation.setSearchDialogGravity(Gravity.TOP)
        spDesignation.arrowPaddingRight = 19
        spDesignation.item = resources.getStringArray(R.array.indian_designations).toList()
        spDesignation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spDesignation.isOutlined = true
                selectedDesignation = spDesignation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                
            }
        }
        val edCompanyName = expDialogView.findViewById<TextInputEditText>(R.id.companyName)
        val spJobLocation = expDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.jobLocation)
        spJobLocation.setSearchDialogGravity(Gravity.TOP)
        spJobLocation.arrowPaddingRight = 19
        spJobLocation.item = resources.getStringArray(R.array.degree_array).toList()
        spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spJobLocation.isOutlined = true
                selectedJobLocation = spJobLocation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        val edDuration = expDialogView.findViewById<TextInputEditText>(R.id.duration)

        val btnAddExperience = expDialogView.findViewById<MaterialButton>(R.id.btnAddExperience)


        val recyclerView = expDialogView.findViewById<RecyclerView>(R.id.recyclerView)

        val adapter = ExperienceAdapter(this,experienceList)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        adapter.notifyDataSetChanged()

        btnAddExperience.setOnClickListener {
            enteredCompanyName = edCompanyName.text.toString().trim()
            enteredDuration = (edDuration.text.toString().trim())
            /*try {
                enteredDuration = (edDuration.text.toString().trim())
            }
            catch (e : NumberFormatException){
                Log.e(TAG, "experienceInfoDialogView: $e")
                edDuration.error = "Enter Valid Number"
            }*/

            if(selectedDesignation.isNotEmpty() && selectedJobLocation.isNotEmpty() && enteredCompanyName.isNotEmpty() && enteredDuration.isNotEmpty()){
                val jsonObject = JSONObject()
                jsonObject.put("vDesignation", selectedDesignation)
                jsonObject.put("vCompany",enteredCompanyName)
                jsonObject.put("vJobLocation",selectedJobLocation)
                jsonObject.put("vDuration", enteredDuration)

                if(Utils.isNetworkAvailable(this)){
                      AndroidNetworking.post(NetworkUtils.INSERT_EXPERIENCE)
                          .addHeaders("Authorization", "Bearer " + prefmanger[AUTH_TOKEN, ""])
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
                                                Log.d(TAG, "onResponse: data \n $selectedDesignation, $edCompanyName, $enteredDuration"
                                                )
                                                experienceList.add(Experience(
                                                    selectedDesignation,
                                                    enteredCompanyName,
                                                    selectedJobLocation,
                                                    enteredDuration
                                                ))
                                                recyclerView.scrollToPosition(adapter.itemCount - 1)
                                                adapter.notifyDataSetChanged()
                                            }
                                        }
                                        catch (e: Exception) {
                                            Log.e("#####", "onResponse Exception: ${e.message}")
                                        }
                                        finally {
                                            spDesignation.clearFocus()
                                            spDesignation.clearSelection()
                                            spJobLocation.clearFocus()
                                            spJobLocation.clearSelection()
                                            edCompanyName.text?.clear()
                                            edCompanyName.clearFocus()
                                            edDuration.text?.clear()
                                            edDuration.clearFocus()
                                            selectedDesignation = ""
                                            enteredCompanyName = ""
                                            selectedJobLocation = ""
                                            enteredDuration = ""
                                        }

                                    }

                                    override fun onError(anError: ANError?) {
                                        anError?.let {
                                            Log.e(
                                                "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                            )
                                        }
                                        spDesignation.clearFocus()
                                        spDesignation.clearSelection()
                                        spJobLocation.clearFocus()
                                        spJobLocation.clearSelection()
                                        edCompanyName.text?.clear()
                                        edCompanyName.clearFocus()
                                        edDuration.text?.clear()
                                        edDuration.clearFocus()
                                        selectedDesignation = ""
                                        enteredCompanyName = ""
                                        selectedJobLocation = ""
                                        enteredDuration = ""
                                    }

                                }
                              )

                } else {
                    Utils.showNoInternetBottomSheet(this,this@ProfileActivity)
                    spDesignation.clearFocus()
                    spDesignation.clearSelection()
                    spJobLocation.clearFocus()
                    spJobLocation.clearSelection()
                    edCompanyName.text?.clear()
                    edCompanyName.clearFocus()
                    edDuration.text?.clear()
                    edDuration.clearFocus()
                    selectedDesignation = ""
                    enteredCompanyName = ""
                    selectedJobLocation = ""
                    enteredDuration = ""
                }


            }
            else {
                if (selectedDesignation.isEmpty()) {
                    spDesignation.errorText = "Select A Designation"
                }

                if (enteredCompanyName.isEmpty()) {
                    edCompanyName.error = "Enter Company Name"
                }
                if (selectedJobLocation.isEmpty()) {
                    spJobLocation.errorText = "Select A Job Location"
                }
                if (enteredDuration.isEmpty()) {
                    edDuration.error = "Enter Duration"
                }
            }

        }
        alertDialogExperience = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(expDialogView)
            .setTitle("Change Info")
            .setNeutralButton("OK"){ dialog,_ ->
                experienceViewModel.writeToLocal(experienceList.toList()).invokeOnCompletion {
                    Log.d(TAG, "experienceInfoDialogView: experienceList is updated in datastore")
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            /*
            .setPositiveButton("Done") { dialog, _ ->

                /*experienceState = "Experienced"*/
                /*designation = edDesignation.text.toString().trim()
                prevCompany = edCompanyName.text.toString().trim()
                prevJobDuration = edDuration.text.toString().trim()*/

               /* CoroutineScope(Dispatchers.IO)
                    .launch {
                        experienceDataStore.saveExperienceList(experienceList.toList())
                    }
                    .invokeOnCompletion {
                        lifecycle.coroutineScope.launch {
                            val experienceFlow: Flow<List<Experience>> = experienceDataStore.getExperienceList()
                            experienceFlow.collect{
                                val experiences = it.toMutableList()
                                Log.d(TAG, "experienceInfoDialogView: OnDataStored in DataStore \n $experiences")

                            }
                        }
                    }*/

                /*experienceList.clear()
                experienceList.addAll(experienceList)*/
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            } */
            .setOnDismissListener {
                setExperiences()
                if(addExperienceImg != null){
                    decideAddImgToVisibility(experienceList.isEmpty(),binding.dataLayout,addExperienceImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }

            }
            .create()
        alertDialogExperience.show()
    }
    class ExperienceAdapter(
        private var mActivity: AppCompatActivity,
        private var experienceList: MutableList<Experience>,
       /* private val onExperienceClickLiner:OnExperienceClickLiner*/
    ) : RecyclerView.Adapter<ExperienceAdapter.ExperiencesHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperiencesHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_experience,parent,
                false)

            return ExperiencesHolder(view/*,onExperienceClickLiner*/)
        }

        override fun getItemCount(): Int {
            return experienceList.size
        }

        override fun onBindViewHolder(holder: ExperiencesHolder, position: Int) {
            val experience = experienceList[position]

            holder.bind(experience)
        }

        inner class ExperiencesHolder(itemView: View/*,onExperienceClickLiner: OnExperienceClickLiner*/):RecyclerView.ViewHolder(itemView) {

            private val designation = itemView.findViewById<MaterialTextView>(R.id.designation)
            private val companyName = itemView.findViewById<MaterialTextView>(R.id.companyName)
            private val jobLocation = itemView.findViewById<MaterialTextView>(R.id.jobLocation)
            private val duration = itemView.findViewById<MaterialTextView>(R.id.duration)

            private val cardView = itemView.findViewById<CardView>(R.id.cardView)
            private val btnDelete = itemView.findViewById<FloatingActionButton>(R.id.btnDelete)

            @SuppressLint("NotifyDataSetChanged")
            fun bind(experience: Experience){
                designation.text = experience.vDesignation
                companyName.text = experience.vCompanyName
                jobLocation.text = experience.vJobLocation
                duration.text = experience.vDuration.plus(" Years")
                btnDelete.visibility = GONE
                btnDelete.setOnClickListener {
                    btnDelete.visibility = GONE
                    experienceList.removeAt(absoluteAdapterPosition)
                    notifyDataSetChanged()
                }
                cardView.setOnLongClickListener {
                    btnDelete.visibility = VISIBLE
                    return@setOnLongClickListener true
                }
            }
        }
    }


    private fun qualificationDialogView(){
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.sp_fade)

        /*var selectedQualifications:MutableList<String> = mutableListOf()
        if(qualification != null){
            val list = qualification!!.split(" - ")
            for(word in list){
                selectedQualifications.add(word)
            }
        }*/

        val qualificationDialog = layoutInflater.inflate(R.layout.dialog_qualification_info,null)
        /*val chipGroup = qualificationDialog.findViewById<ChipGroup>(R.id.chipGroup)
        for(qualification in selectedQualifications){
            val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
            chip.text = qualification
            chipGroup.addView(chip)
            chip.setOnClickListener {
                chip.startAnimation(animation)
                chip.postDelayed({ chipGroup.removeView(chip) }, 200)
                selectedQualifications.remove(qualification)
            }
        }*/
        val spQualification = qualificationDialog.findViewById<SmartMaterialSpinner<String>>(R.id.qualification)

        spQualification.setSearchDialogGravity(Gravity.TOP)
        spQualification.arrowPaddingRight = 19
        spQualification.item = resources.getStringArray(R.array.degree_array).toList()
        spQualification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spQualification.isOutlined = true

                qualification = spQualification.item[position]
               /* val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
                makeToast(spQualification.item[position],0)
                chip.text = spQualification.item[position]
                selectedQualifications.add(spQualification.item[position])
                chipGroup.addView(chip)
                *//*chip.startAnimation(animation)
                chip.postDelayed({  }, 20)*//*

                chip.setOnClickListener {
                    chip.startAnimation(animation)
                    chip.postDelayed({ chipGroup.removeView(chip) }, 200)
                    selectedQualifications.remove(qualification)
                }*/
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                spQualification.errorText = "On Nothing Selected"
            }
        }

        alertDialogQualification = AlertDialog.Builder(this,R.style.CustomAlertDialogStyle)
            .setView(qualificationDialog)
            .setTitle("Edit Qualifications")
            .setPositiveButton("Done"){dialog,_ ->

                /*qualification = selectedQualifications.joinToString(" - ")*/
                binding.qualificationJ.text = qualification

                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeQualificationData(
                        qualification!!,
                    )
                }
                Log.d(TAG, "qualificationDialogView: $qualification")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                if(addQualificationImg != null){
                    decideAddImgToVisibility(qualification.isNullOrEmpty(),binding.qualificationJ,addQualificationImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }
            }
                .create()
        alertDialogQualification.show()
    }

    private fun aboutInfoJDialogView() {
        val aboutDialogView = layoutInflater.inflate(R.layout.dialog_about_info, null)

        val edBio = aboutDialogView.findViewById<EditText>(R.id.bio)
        edBio.setText(bio!!)

        alertDialogAboutInfo = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(aboutDialogView)
            .setTitle("Change Bio")
            .setPositiveButton("Done") { dialog, _ ->
                bio = edBio.text.toString().trim()

                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeAboutData(
                        bio!!,
                    )
                }.invokeOnCompletion {
                    Log.d(TAG, "aboutInfoJDialogView: bio stored in dataStore")
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                if(addAboutImg != null) {
                    decideAddImgToVisibility(bio.isNullOrEmpty(), binding.qualificationJ, addAboutImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }
            }
            .create()
        alertDialogAboutInfo.show()
    }

    private fun basicInfoDialogView() {
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.sp_fade)

        val basicDialogView = layoutInflater.inflate(R.layout.dialog_profile_basic_info, null)


        val expertiseList:MutableList<String> = mutableListOf()

        var selectedJobLocation = String()

        if(tagLine != null){
            val list = tagLine!!.split(" || ")
            for(word in list){
                expertiseList.add(word)
            }
        }

        val edUserFName = basicDialogView.findViewById<EditText>(R.id.userFName)
        edUserFName.setText(fName!!)
        val edUserLName = basicDialogView.findViewById<EditText>(R.id.userLName)
        edUserLName.setText(lName!!)
        val edExpertise = basicDialogView.findViewById<EditText>(R.id.expertise)
        val chipGroup = basicDialogView.findViewById<ChipGroup>(R.id.chipGroup)
        for(expertise  in expertiseList){
            val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
            chip.text = expertise
            chipGroup.addView(chip)
            chip.setOnClickListener {
                chip.startAnimation(animation)
                chip.postDelayed({ chipGroup.removeView(chip) }, 200)
                expertiseList.remove(expertise)
            }
        }

        val addExpertise = basicDialogView.findViewById<FloatingActionButton>(R.id.btnAdd)
        addExpertise.setOnClickListener {
            val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
            val expertise = edExpertise.text.trim().toString()
            chip.text = expertise
            expertiseList.add(expertise)
            chipGroup.addView(chip)
            /*chip.startAnimation(animation)
            chip.postDelayed({  }, 20)*/

            chip.setOnClickListener {
                chip.startAnimation(animation)
                chip.postDelayed({ chipGroup.removeView(chip) }, 200)
                expertiseList.remove(expertise)
            }
            edExpertise.text?.clear()
            edExpertise.requestFocus()
        }

        val spJobLocation = basicDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.jobLocation)
        spJobLocation.setSearchDialogGravity(Gravity.TOP)
        spJobLocation.arrowPaddingRight = 19
        spJobLocation.item = resources.getStringArray(R.array.degree_array).toList()
        spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spJobLocation.isOutlined = true
                selectedJobLocation = spJobLocation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        /*val edCurrentCompany = basicDialogView.findViewById<EditText>(R.id.currentCompany)
        edCurrentCompany.setText(currentCompany!!)*/
        /*val edPhoneNo = basicDialogView.findViewById<EditText>(R.id.phoneNo)
        edPhoneNo.setText(phoneNumber)*/
        val edEmail = basicDialogView.findViewById<EditText>(R.id.email)
        edEmail.setText(emailId!!)
        alertDialogBasicInfo = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(basicDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->

                fName = edUserFName.text.toString().trim()
                lName = edUserLName.text.toString().trim()
                fullName = fName.plus(lName)
                tagLine = expertiseList.joinToString(" || ")
                residentialCity = selectedJobLocation
                /*phoneNumber = edPhoneNo.text.toString().trim()*/
                emailId = edEmail.text.toString().trim()


                CoroutineScope(Dispatchers.IO).launch {
                    userDataRepository.storeBasicInfo(
                        fName!!,
                        lName!!,
                        phoneNumber!!,
                        emailId!!,
                        tagLine!!,
                        residentialCity!!
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                showViewIfNotEmpty(fullName!!, binding.userName)
                showViewIfNotEmpty(tagLine!!, binding.expertise)
                showViewIfNotEmpty(residentialCity!!, binding.residentialCity)

                if (emailId!!.isNotEmpty()) {
                    emailBalloon = createMsgBalloon(emailId!!, R.drawable.ic_email, baseContext)

                    if (emailBalloon != null) {
                        emailBalloon!!.setOnBalloonClickListener {
                            makeEmail(emailId!!)
                            emailBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                } else {
                    emailBalloon = createMsgBalloon(
                        "Email Id Not Found",
                        R.drawable.ic_email,
                        baseContext
                    )
                    if (emailBalloon != null) {
                        emailBalloon!!.setOnBalloonOutsideTouchListener { view, motionEvent ->
                            emailBalloon!!.dismiss()
                        }
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                    }
                }
            }
            .create()
        alertDialogBasicInfo.show()
    }


    private fun selectImg(code: Int) {
        val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
        imgIntent.type = "image/*"
        imgIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(imgIntent, code)

    }

    private fun selectPdf(code: Int) {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pdfIntent, code)
    }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                SELECT_RESUME_FILE -> if (resultCode == RESULT_OK) {
                    uploadProgressBar.visibility = View.VISIBLE
                    uploadProgressBar.progress = 10
                    val pdfUri = data?.data!!

                    resumeFile = Utils.convertUriToPdfFile(this@ProfileActivity, pdfUri)!!

                    if (pdfUri.toString().startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                pdfUri,
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
                    resumeUri = pdfUri.toString()

                }

                SELECT_PROFILE_BANNER_IMG -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!
                    profileBannerFile = File(Utils.getRealPathFromURI(this, photoUri).toString())
                    Glide.with(this@ProfileActivity)
                        .load(photoUri)
                        .apply(
                            RequestOptions
                                .placeholderOf(R.drawable.profile_default_back_img)
                                .error(R.drawable.profile_default_back_img)
                                .fitCenter()
                        )
                        .into(profileBackImg)
                    profileBannerImgUri = photoUri.toString()
                }

                SELECT_PROFILE_IMG -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!
                    profileImgFile = File(Utils.getRealPathFromURI(this, photoUri).toString())
                    Glide.with(this@ProfileActivity)
                        .load(photoUri)
                        .apply(
                            RequestOptions
                                .placeholderOf(R.drawable.profile_default_image)
                                .error(R.drawable.profile_default_image)
                                .fitCenter()
                        )
                        .into(profileImgDia)
                    profileImgUri = photoUri.toString()
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
        val radioButton = dialogView.findViewById<View>(selectedItemId) as RadioButton?
        if (selectedItemId != -1 && radioButton != null) {
            return radioButton.text.toString()
        }
        return ""
    }


  /*  override fun onDestroy() {
        super.onDestroy()

        val updateDataServiceIntent = Intent(this, UpdateProfileDataService::class.java)
        startService(updateDataServiceIntent)
    }*/
    override fun updateData() {
        lifecycle.coroutineScope.launch{
            Log.d(TAG, "updateData: Updated data process service running")

            if (Utils.isNetworkAvailable(this@ProfileActivity)) {
                if (userType == 0) {
                    AndroidNetworking.post(NetworkUtils.UPDATE_PROFILE_DETAILS)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN,"")}")
                        .addQueryParameter("vFirstName", fName)
                        .addQueryParameter("vLastName", lName)
                        .addQueryParameter("vEmail", emailId)
                        .addQueryParameter("tBio", bio)
                        .addQueryParameter("vcity", residentialCity)
                        .addQueryParameter("vCurrentCompany", currentCompany)
                        .addQueryParameter("vDesignation", designation)
                        .addQueryParameter("vJobLocation", jobLocation)
                        .addQueryParameter("vQualification", qualification)
                        .addQueryParameter("tTagLine",tagLine)
                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            GetUserById::class.java,
                            object : ParsedRequestListener<GetUserById> {
                                override fun onResponse(response: GetUserById?) {
                                    try {
                                        response?.let {
                                            Log.d(TAG, "onResponse: Profile Data Updated Successfully")
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
                    AndroidNetworking.post(NetworkUtils.UPDATE_PROFILE_DETAILS)
                        .setOkHttpClient(NetworkUtils.okHttpClient)
                        .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN,"")}")
                        .addQueryParameter("vFirstName", fName)
                        .addQueryParameter("vLastName", lName)
                        .addQueryParameter("vEmail", emailId)
                        .addQueryParameter("tBio", bio)
                        .addQueryParameter("vQualification", qualification)
                        .addQueryParameter("vcity", residentialCity)
                        .addQueryParameter("vCurrentCompany", currentCompany)
                        .addQueryParameter("vDesignation", designation)
                        .addQueryParameter("vJobLocation", jobLocation)
                        .addQueryParameter("vWorkingMode","")
                        .addQueryParameter("tTagLine", tagLine)
                        .setPriority(Priority.MEDIUM).build().getAsObject(
                            GetUserById::class.java,
                            object : ParsedRequestListener<GetUserById> {
                                override fun onResponse(response: GetUserById?) {
                                    try {
                                        response?.let {
                                            Log.d(TAG, "onResponse: Profile Data Updated Successfully")
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
            }
            else {
                Utils.showNoInternetBottomSheet(this@ProfileActivity, this@ProfileActivity)
            }
        }
    }

    /*private fun requestPermissions(s: String) {
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
        Dexter.withContext(this).withPermissions(
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
    }*/

/*    private fun isGrantedPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        } else {
            Log.d("Version**", Build.VERSION.SDK_INT.toString())
            val isGranted1 =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            val isGranted2 =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            val isGranted3 =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

            return isGranted1 == PackageManager.PERMISSION_GRANTED && isGranted2 == PackageManager.PERMISSION_GRANTED && isGranted3 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }*/

    /*fun showLogoutBottomSheet() {

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
                                    Toast.makeText(this@ProfileActivity, response.data.msg, Toast.LENGTH_LONG).show()
                                    prefmanger.set(IS_LOGIN,false)
                                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                }else{
                                    Toast.makeText(this@ProfileActivity,getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onError(anError: ANError?) {
                                anError?.let {
                                    Log.e("#####", "onError: code: ${it.errorCode} & body: ${it.errorDetail}")
                                    Toast.makeText(this@ProfileActivity,getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                    hideProgressDialog()

                                }

                            }
                        })
            }else{
                Utils.showNoInternetBottomSheet(this@ProfileActivity, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("#message", "onResponse: "+e.message)
            hideProgressDialog()
            Toast.makeText(this@ProfileActivity,getString(R.string.something_error), Toast.LENGTH_SHORT).show()
        }

    }
*/


}