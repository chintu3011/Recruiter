package com.amri.emploihunt.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityProfileBinding
import com.amri.emploihunt.messenger.FullImageViewActivity
import com.amri.emploihunt.messenger.PDfViewActivity
import com.amri.emploihunt.model.CommonMessageModel
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.ExperienceModel
import com.amri.emploihunt.model.GetUserById
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.store.ExperienceViewModel
import com.amri.emploihunt.store.UserDataRepository
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.CURRENT_COMPANY
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PDF_TYPE
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.shockwave.pdfium.PdfiumCore
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

    lateinit var cityList:ArrayList<String>

    //common data
    private var fName: String? = null
    private var lName: String? = null
    private var fullName: String? = null
    private var phoneNumber: String? = null
    private var emailId: String? = null
    private var tagLine: String? = null

    private var residentialCity:String? = null

    private var profileImgUri: String? = null
    private var profileImgFile: File? =null
    private var profileBannerImgUri: String? = null
    private var profileBannerFile: File? =null
    //User Data
    private var bio: String? = null
    private var qualification: String? = null

    private var currentCompany: String? = null
    private var designation: String? = null
    private var jobLocation: String? = null
    private var workingMode:String? = null

    private lateinit var experienceList:MutableList<Experience>

    private var resumeUri: String? = null
    private var resumeFile:File? = null
    private var resumeFileName: String? = null

    private lateinit var experienceAdapter:ExperienceAdapter

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


        cityList = arrayListOf()
        getAllCity(cityList){
            Log.d(TAG, "onCreate: \"cityList filled\"")
        }

        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                Log.d("######", "handleOnBackPressed: $workingMode")
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

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun setProfileData() {



        /** profile Img */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserProfileImgUrl().collect {
                Log.d(TAG, "setProfileData: trying to update profile img data $it")
                profileImgUri = it
                Glide.with(this@ProfileActivity)
                    .load(NetworkUtils.BASE_URL_MEDIA+profileImgUri)
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
        /** profile banner */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserProfileBannerUrl().collect {
                Log.d(TAG, "setProfileData: trying to update profile banner data $it")
                profileBannerImgUri = it
                Glide.with(this@ProfileActivity)
                    .load(NetworkUtils.BASE_URL_MEDIA+profileBannerImgUri)
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
                currentCompany = it
                showViewIfNotEmpty(it,binding.currentCompany)
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
                if(userType == 1) {
                    showViewIfNotEmpty(it, binding.designationR)
                    binding.designationR.text = designation
                }
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: Designation data is updated")
        }
        /** Job Location */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserJobLocation().collect {
                Log.d(TAG, "setProfileData: trying to update job location data $it")
                jobLocation = it
                if(userType == 1) {
                    showViewIfNotEmpty(it, binding.jobLocation)
                    binding.jobLocation.text = jobLocation
                }
            }
        }.invokeOnCompletion {
            Log.d(TAG, "setProfileData: JobLocation data is updated")
        }
        /** working mode */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserWorkingMode().collect {
                Log.d("#####", "setProfileData: trying to update working mode data $it")

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
                addExperienceImg = createAddDataImg(binding.experienceLayout, R.id.txtExperienceJ)

                experienceViewModel.readFromLocal().collect { list ->
                    Log.d(TAG, "getExperienceList: Experience $list \n ${list.size}")
                    experienceList.clear()
                    experienceList.addAll(list)

                    for(index in 0 until experienceList.size){
                        if(experienceList[index].bIsCurrentCompany == CURRENT_COMPANY){
                             val currExp = experienceList[index]
                            experienceList.removeAt(index)
                            experienceList.add(0,currExp)
                        }
                    }
                    binding.experienceRecyclerView.visibility = GONE
                    if (addExperienceImg != null) {
                        decideAddImgToVisibility(
                            experienceList.isEmpty(),
                            binding.experienceRecyclerView,
                            addExperienceImg!!
                        )
                    } else {
                        makeToast(getString(R.string.something_error), 0)
                        Log.d(TAG, "setProfileData: addExperienceImg is null")
                    }
                    setExperiences(experienceList)
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

    private var isShowMore = false
    @SuppressLint("NotifyDataSetChanged")
    private fun setExperiences(experienceList: MutableList<Experience>) {

        Log.d(TAG, "setExperiences: removeView : ${binding.experienceRecyclerView.childCount}")
        binding.btnShowMoreLayout.visibility = GONE
        Log.d("#####", "setProfileData: Experience data \n ${experienceList.size}")
        binding.experienceRecyclerView.removeAllViews()
        if (experienceList.isNotEmpty()){
            binding.experienceRecyclerView.visibility = VISIBLE

            if(experienceList.size > 3){
                experienceAdapter = ExperienceAdapter(false,this,experienceList.subList(0,3),null,cityList)
                binding.experienceRecyclerView.adapter = experienceAdapter
                experienceAdapter.notifyDataSetChanged()
                Log.d(TAG, "setExperiences: removeView : ${binding.experienceRecyclerView.childCount}")
                binding.btnShowMoreLayout.visibility = VISIBLE
                isShowMore = true
            }
            else{
                experienceAdapter = ExperienceAdapter(false,this,experienceList,null,cityList)
                binding.experienceRecyclerView.adapter = experienceAdapter
                experienceAdapter.notifyDataSetChanged()
                Log.d(TAG, "setExperiences: removeView : ${binding.experienceRecyclerView.childCount}")
                isShowMore = false
                binding.btnShowMoreLayout.visibility = GONE
            }
        }
    }

    private fun decideAddImgToVisibility(
        dataState: Boolean,
        view: View,
        addImg: ShapeableImageView
    ) {
        Log.d(TAG, "decideAddImgToVisibility: $dataState")
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
        binding.resumeFileNameJ.setOnClickListener(this)
        binding.profileImg.setOnClickListener(this)
    }

    @SuppressLint("NotifyDataSetChanged")
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
                Log.d(TAG, "onClick: btnShowMore : $isShowMore")
                if(isShowMore){
                    binding.btnShowMore.setImageResource(R.drawable.ic_up)
                    binding.experienceRecyclerView.removeAllViews()
                    experienceAdapter = ExperienceAdapter(false,this,experienceList,null,cityList)
                    binding.experienceRecyclerView.adapter = experienceAdapter
                    experienceAdapter.notifyDataSetChanged()
                }
                else{
                    binding.btnShowMore.setImageResource(R.drawable.ic_down)
                    binding.experienceRecyclerView.removeAllViews()
                    experienceAdapter = ExperienceAdapter(false,this,experienceList.subList(0,3),null,cityList)
                    binding.experienceRecyclerView.adapter = experienceAdapter
                    experienceAdapter.notifyDataSetChanged()
                }
                isShowMore = !isShowMore

                /**setExperiences(experienceList)*/
                /*if (!binding.btnShowMore.isActivated) {
                    if(experienceList.size > 3){
                        for (index in 3 until experienceList.size)  {
                            val experience = experienceList[index]
                            val experienceRow = layoutInflater.inflate(R.layout.row_experience, null)

                            val designationTextView = experienceRow.findViewById<TextView>(R.id.designation)
                            val companyNameTextView = experienceRow.findViewById<TextView>(R.id.companyName)
                            val jobLocationTextView = experienceRow.findViewById<TextView>(R.id.jobLocation)
                            val durationTextView = experienceRow.findViewById<TextView>(R.id.duration)
                            val btnDelete = experienceRow.findViewById<LinearLayout>(R.id.btnDelete)
                            btnDelete.visibility = GONE

                            designationTextView.text = experience.vDesignation
                            companyNameTextView.text = experience.vCompanyName
                            jobLocationTextView.text = experience.vJobLocation
                            val txtPresent = experienceRow.findViewById<TextView>(R.id.txtPresent)
                            if(experience.bIsCurrentCompany == 1){
                                durationTextView.visibility = GONE
                                txtPresent.visibility = VISIBLE
                            }
                            else {
                                txtPresent.visibility = GONE
                                if (experience.vDuration.isNotEmpty()) {
                                    durationTextView.visibility = VISIBLE
                                    durationTextView.text = experience.vDuration.plus(" Years")
                                }
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
                            val btnDelete = experienceRow.findViewById<LinearLayout>(R.id.btnDelete)
                            btnDelete.visibility = GONE

                            designationTextView.text = experience.vDesignation
                            companyNameTextView.text = experience.vCompanyName
                            jobLocationTextView.text = experience.vJobLocation
                            val txtPresent = experienceRow.findViewById<TextView>(R.id.txtPresent)
                            if(experience.bIsCurrentCompany == 1){
                                durationTextView.visibility = GONE
                                txtPresent.visibility = VISIBLE
                            }
                            else {
                                txtPresent.visibility = GONE
                                if (experience.vDuration.isNotEmpty()) {
                                    durationTextView.visibility = VISIBLE
                                    durationTextView.text = experience.vDuration.plus(" Years")
                                }
                            }

                            binding.dataLayout.addView(experienceRow)
                        }
                    }
                    binding.btnShowMore.text = getString(R.string.show_more)
                }
                binding.btnShowMore.isActivated = !binding.btnShowMore.isActivated*/

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
            R.id.resumeFileNameJ -> {
                if(!resumeUri.isNullOrEmpty()) {

                    val intent = Intent(this, PDfViewActivity::class.java)
                    intent.putExtra("Uri", resumeUri!!)
                    startActivity(intent)
                }
                else{
                    makeToast("Resume not found",0)
                }
            }
            R.id.profileImg -> {
                if(!profileImgUri.isNullOrEmpty()) {
                    val intent = Intent(this, FullImageViewActivity::class.java)
                    intent.putExtra("Uri", profileImgUri!!)
                    startActivity(intent)
                }
                else{
                    makeToast("Profile image not found",0)
                }
            }
        }
    }

    private fun currentPositionDialogView() {
        val currentPositionDialog = layoutInflater.inflate(R.layout.dialog_current_position_info, null)

        var selectedJobLocation = String()
        /*var selectedDesignation = String()*/
        var selectedWorkingMode = String()

        val edCompanyName = currentPositionDialog.findViewById<EditText>(R.id.companyName)
        if(!currentCompany.isNullOrEmpty()) {
            edCompanyName.setText(currentCompany)
        }
        val edDesignation = currentPositionDialog.findViewById<AutoCompleteTextView>(R.id.inputDesignation)
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,resources.getStringArray(R.array.indian_designations).toList())
        edDesignation.setAdapter(adapter)
        if(!designation.isNullOrEmpty()){
            edDesignation.setText(designation)
        }
        /*val spDesignation = currentPositionDialog.findViewById<SmartMaterialSpinner<String>>(R.id.spDesignation)
        spDesignation.setSearchDialogGravity(Gravity.TOP)
        spDesignation.arrowPaddingRight = 19
        spDesignation.item = resources.getStringArray(R.array.indian_designations).toList()
        if(!designation.isNullOrEmpty()){
            spDesignation.setSelection(resources.getStringArray(R.array.indian_designations).toList().indexOf(designation))
        }
        spDesignation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spDesignation.isOutlined = true
                selectedDesignation = spDesignation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }*/



        val spJobLocation = currentPositionDialog.findViewById<SmartMaterialSpinner<String>>(R.id.spJobLocation)
        if(!jobLocation.isNullOrEmpty()){
            spJobLocation.setSelection(cityList.indexOf(jobLocation))
        }
        spJobLocation.setSearchDialogGravity(Gravity.TOP)
        spJobLocation.arrowPaddingRight = 19
        spJobLocation.item = cityList.toList()
        spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spJobLocation.isOutlined = true
                selectedJobLocation = spJobLocation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        
        val tbWorkingMode = currentPositionDialog.findViewById<TabLayout>(R.id.tbWorkingMode)
        selectedWorkingMode = tbWorkingMode.getTabAt(tbWorkingMode.selectedTabPosition)?.text.toString()
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
                designation = edDesignation.text.toString().trim()
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
    private lateinit var uploadProgressBar: LinearProgressIndicator
    private lateinit var btnUpload:LottieAnimationView
    private fun resumeInfoDialogView() {
        val resumeDialogView = layoutInflater.inflate(R.layout.dialog_resume_info, null)

        btnUpload = resumeDialogView.findViewById<LottieAnimationView>(R.id.btnUpload)

        tvResumeFileName = resumeDialogView.findViewById(R.id.resumeFileName)
        tvResumeFileName.text = resumeFileName!!

        btnUpload.setOnClickListener{
            tvResumeFileName.visibility = GONE
            selectPdf(SELECT_RESUME_FILE)
        }

        uploadProgressBar = resumeDialogView.findViewById(R.id.uploadProgressBar)
        uploadProgressBar.visibility = GONE

        alertDialogResumeInfo = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(resumeDialogView)
            .setTitle("Change Info")
            .setPositiveButton("Done") { dialog, _ ->
                storeResume(resumeFile) {
                    if (it) {
                        dialog.dismiss()
                    } else {
                        makeToast(getString(R.string.some_thing_wrong_try_later), 0)
                    }
                    uploadProgressBar.visibility = GONE
                }

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
    private lateinit var profileBackImgLodding:LottieAnimationView
    private fun profileBannerDialogView() {
        val profileBannerDialogView =
            layoutInflater.inflate(R.layout.dialog_profile_cover_img, null)

        profileBackImg = profileBannerDialogView.findViewById(R.id.profileBackImg)
        profileBackImg.setOnClickListener{
            if(!profileBannerImgUri.isNullOrEmpty()) {
                val intent = Intent(this, FullImageViewActivity::class.java)
                intent.putExtra("Uri", profileBannerImgUri)
                startActivity(intent)
            }
            else{
                makeToast("Profile banner image not found",0)
            }
        }
        profileBackImgLodding = profileBannerDialogView.findViewById(R.id.profileBackImgLodding)
        profileBackImgLodding.visibility  = GONE

        if(profileBannerImgUri != null) {
            Glide.with(this@ProfileActivity)
                .load(NetworkUtils.BASE_URL_MEDIA+profileBannerImgUri)
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

                storeProfileBannerImg(profileBannerFile)
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
    private lateinit var profileImgLodding:LottieAnimationView
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
        profileImgLodding = profileImgDialogView.findViewById(R.id.profileImgLodding)
        profileImgLodding.speed = 2f
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

                storeProfileImg(profileImgFile)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialogProfileImg.show()

    }

    lateinit var adapter:ExperienceAdapter
    @SuppressLint("NotifyDataSetChanged")
    private fun experienceInfoDialogView() {

        val expDialogView = layoutInflater.inflate(R.layout.dialog_experience_info, null)
        var enteredDesignation = String()
        /*var selectedDesignation = String()*/
        var enteredCompanyName = String()
        var selectedJobLocation = String()
        var enteredDuration = String()

        val btnAddNewExperience = expDialogView.findViewById<MaterialButton>(R.id.btnAddNewExperience)
        val inputLayout = expDialogView.findViewById<ConstraintLayout>(R.id.inputLayout)
        btnAddNewExperience.setOnClickListener{
            inputLayout.visibility = VISIBLE
            btnAddNewExperience.visibility = GONE
        }

        val edDesignation = expDialogView.findViewById<MaterialAutoCompleteTextView>(R.id.designation)
        val adapterDesignation: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,resources.getStringArray(R.array.indian_designations).toList())
        edDesignation.setAdapter(adapterDesignation)
        /*spDesignation.setSearchDialogGravity(Gravity.TOP)
        spDesignation.arrowPaddingRight = 19
        spDesignation.item = resources.getStringArray(R.array.indian_designations).toList()
        spDesignation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spDesignation.isOutlined = true
                selectedDesignation = spDesignation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }*/
        val edCompanyName = expDialogView.findViewById<TextInputEditText>(R.id.companyName)
        val spJobLocation = expDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.jobLocation)
        spJobLocation.setSearchDialogGravity(Gravity.TOP)
        spJobLocation.arrowPaddingRight = 19
        spJobLocation.item = cityList.toList()
        spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spJobLocation.isOutlined = true
                selectedJobLocation = spJobLocation.item[position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        val checkBox = expDialogView.findViewById<CheckBox>(R.id.checkBox)

        val edDuration = expDialogView.findViewById<TextInputEditText>(R.id.duration)
        val edDurationLayout = expDialogView.findViewById<TextInputLayout>(R.id.textLayoutDuration)
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                edDurationLayout.visibility = GONE
            }
            else{
                edDurationLayout.visibility = VISIBLE
            }
        }
        val btnAddExperience = expDialogView.findViewById<MaterialButton>(R.id.btnAddExperience)

        val recyclerView = expDialogView.findViewById<RecyclerView>(R.id.recyclerView)

        val addExpProgress = expDialogView.findViewById<ProgressBar>(R.id.addExpProgress)
        addExpProgress.visibility = GONE

        Log.d(TAG, "experienceInfoDialogView: experience list \n $experienceList")
        adapter = ExperienceAdapter(
            true,
            this,
            experienceList,
            object  : ExperienceAdapter.ExperienceEditUpdateListener{
                override fun delete(position: Int,experience: Experience) {
                    if (Utils.isNetworkAvailable(this@ProfileActivity)) {
                        AndroidNetworking.post((NetworkUtils.DELETE_EXPERIENCE))
                            .addHeaders("Authorization", "Bearer " + prefmanger[AUTH_TOKEN, ""])
                            .addQueryParameter("id", experience.id.toString())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsObject(CommonMessageModel::class.java,
                                object : ParsedRequestListener<CommonMessageModel>{
                                    override fun onResponse(response: CommonMessageModel?) {
                                        try {
                                            Log.d(TAG, "onResponse: ${response?.message}")
                                            makeToast("Experience deleted.",0)
                                            experienceList.removeAt(position)
                                            adapter.notifyItemRemoved(position)
                                        }
                                        catch (e: Exception) {
                                            Log.e("#####", "onResponse Exception: ${e.message}")
                                            makeToast(" Something wrong \n Please try again Later... ",0)
                                        }

                                    }

                                    override fun onError(anError: ANError?) {
                                        anError?.let {
                                            Log.e(
                                                "#####",
                                                "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                            )
                                        }
                                        makeToast(getString(R.string.some_thing_wrong_try_later),0)
                                    }

                                })
                    }
                    else{
                        Utils.showNoInternetBottomSheet(this@ProfileActivity, this@ProfileActivity)
                    }
                }

                override fun edit(position: Int, experience: Experience) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", experience.id)
                    jsonObject.put("vDesignation", experience.vDesignation)
                    jsonObject.put("vCompany", experience.vCompanyName)
                    jsonObject.put("vJobLocation", experience.vJobLocation)
                    jsonObject.put("bIsCurrentCompany", experience.bIsCurrentCompany)
                    if(experience.vDuration !=  null) {
                        jsonObject.put("vDuration",experience.vDuration)
                    }
                    /*jsonObject.put("iUserId",experience.iUserId)
                    jsonObject.put("tCreatedAt",experience.tCreatedAt)
                    jsonObject.put("tUpadatedAt",experience.tUpadatedAt)*/

                    if (Utils.isNetworkAvailable(this@ProfileActivity)){
                        AndroidNetworking.post(NetworkUtils.UPDATE_EXPERIENCE)
                            .addHeaders("Authorization", "Bearer " + prefmanger[AUTH_TOKEN, ""])
                            .addJSONObjectBody(
                                jsonObject
                            )
                            .setPriority(Priority.MEDIUM).build()
                            .getAsObject(
                                ExperienceModel::class.java,
                                object : ParsedRequestListener<ExperienceModel> {
                                    override fun onResponse(response: ExperienceModel?) {
                                        try {
                                            response?.let {
                                                Log.d(
                                                    TAG,
                                                    "onResponse: updated experience data  \n ${response.data}"
                                                )
                                                if (response.data.bIsCurrentCompany == CURRENT_COMPANY) {

                                                    currentCompany = response.data.vCompanyName
                                                    designation = response.data.vDesignation
                                                    jobLocation = response.data.vJobLocation
                                                    binding.currentCompany.text = currentCompany
                                                    val userDataRepository =
                                                        UserDataRepository(this@ProfileActivity)

                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        userDataRepository.storeCurrentPositionData(
                                                            response.data.vCompanyName,
                                                            response.data.vDesignation,
                                                            response.data.vJobLocation,
                                                            ""
                                                        )
                                                    }/*.invokeOnCompletion {
                                                        makeToast("Current position updated",0)
                                                    }*/
                                                    experienceList.removeAt(position)
                                                    adapter.notifyItemRemoved(position)
                                                    experienceList.add(
                                                        0,
                                                        Experience(
                                                        response.data.id,
                                                        response.data.vDesignation,
                                                        response.data.vCompanyName,
                                                        response.data.vJobLocation,
                                                        response.data.bIsCurrentCompany,
                                                        null,
                                                        response.data.iUserId,
                                                        response.data.tCreatedAt,
                                                        response.data.tUpadatedAt)
                                                    )
                                                    adapter.notifyItemInserted(0)
                                                    adapter.notifyItemChanged(0)

                                                } else {
                                                    experienceList[position] = Experience(
                                                        response.data.id,
                                                        response.data.vDesignation,
                                                        response.data.vCompanyName,
                                                        response.data.vJobLocation,
                                                        response.data.bIsCurrentCompany,
                                                        response.data.vDuration,
                                                        response.data.iUserId,
                                                        response.data.tCreatedAt,
                                                        response.data.tUpadatedAt
                                                    )
                                                    adapter.notifyItemChanged(position)
                                                }
                                                recyclerView.scrollToPosition(position)

                                                /*adapter.notifyDataSetChanged()*/
                                            }
                                        }
                                        catch (e: Exception) {
                                            Log.e("#####", "onResponse Exception: ${e.message}")
                                            makeToast(" Something wrong \n Please try again Later... ",0)
                                        }
                                    }
                                    override fun onError(anError: ANError?) {
                                        anError?.let {
                                            Log.e(
                                                "#####",
                                                "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                            )
                                        }
                                        makeToast(getString(R.string.some_thing_wrong_try_later),0)
                                    }
                                }
                            )
                    }
                    else{
                        Utils.showNoInternetBottomSheet(this@ProfileActivity, this@ProfileActivity)
                    }
                }

            },
            cityList)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(adapter.itemCount - 1)
        adapter.notifyDataSetChanged()


        btnAddExperience.setOnClickListener {
            enteredDesignation = edDesignation.text.toString().trim()
            enteredCompanyName = edCompanyName.text.toString().trim()
            enteredDuration = (edDuration.text.toString().trim())
            btnAddExperience.visibility = GONE
            addExpProgress.visibility = VISIBLE


            /*try {
                enteredDuration = (edDuration.text.toString().trim())
            }
            catch (e : NumberFormatException){
                Log.e(TAG, "experienceInfoDialogView: $e")
                edDuration.error = "Enter Valid Number"
            }*/

            if(enteredDesignation.isNotEmpty() && enteredCompanyName.isNotEmpty() && selectedJobLocation.isNotEmpty()){

                val jsonObject = JSONObject()
                if (checkBox.isChecked) {
                    jsonObject.put("vDesignation", enteredDesignation)
                    jsonObject.put("vCompany", enteredCompanyName)
                    jsonObject.put("vJobLocation", selectedJobLocation)
                    jsonObject.put("bIsCurrentCompany", 1)
                } else {
                    if (enteredDuration.isNotEmpty()){
                        jsonObject.put("vDesignation", enteredDesignation)
                        jsonObject.put("vCompany", enteredCompanyName)
                        jsonObject.put("vJobLocation", selectedJobLocation)
                        jsonObject.put("bIsCurrentCompany", 0)
                        jsonObject.put("vDuration", enteredDuration)
                    }else{
                        edDuration.error = "Enter Duration"
                        return@setOnClickListener
                    }
                }

                if (Utils.isNetworkAvailable(this)) {
                    AndroidNetworking.post(NetworkUtils.INSERT_EXPERIENCE)
                        .addHeaders("Authorization", "Bearer " + prefmanger[AUTH_TOKEN, ""])
                        .addJSONObjectBody(
                            jsonObject
                        )
                        .setPriority(Priority.MEDIUM).build()
                        .getAsObject(
                            ExperienceModel::class.java,
                            object : ParsedRequestListener<ExperienceModel> {
                                override fun onResponse(response: ExperienceModel?) {
                                    try {
                                        response?.let {
                                            Log.d(
                                                TAG,
                                                "onResponse: data \n $enteredDesignation, $edCompanyName, $enteredDuration"
                                            )
                                            if (checkBox.isChecked) {

                                                currentCompany = enteredCompanyName
                                                designation = enteredDesignation
                                                jobLocation = selectedJobLocation
                                                binding.currentCompany.text = currentCompany
                                                val userDataRepository =
                                                    UserDataRepository(this@ProfileActivity)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    userDataRepository.storeCurrentPositionData(
                                                        enteredCompanyName,
                                                        enteredDesignation,
                                                        selectedJobLocation,
                                                        ""
                                                    )
                                                }/*.invokeOnCompletion {
                                                    makeToast("Current position updated",0)
                                                }*/
                                                if(experienceList.isNotEmpty()) {
                                                    experienceList.add(
                                                        0,
                                                        Experience(
                                                            response.data.id,
                                                            response.data.vDesignation,
                                                            response.data.vCompanyName,
                                                            response.data.vJobLocation,
                                                            1,
                                                            null,
                                                            response.data.iUserId,
                                                            response.data.tCreatedAt,
                                                            response.data.tUpadatedAt
                                                        )
                                                    )
                                                    adapter.notifyItemInserted(0)
                                                }
                                                else{
                                                    experienceList.add(
                                                        Experience(
                                                            response.data.id,
                                                            response.data.vDesignation,
                                                            response.data.vCompanyName,
                                                            response.data.vJobLocation,
                                                            1,
                                                            null,
                                                            response.data.iUserId,
                                                            response.data.tCreatedAt,
                                                            response.data.tUpadatedAt
                                                        )
                                                    )
                                                    adapter.notifyItemInserted(experienceList.size -1)
                                                }
                                            } else {
                                                experienceList.add(
                                                    Experience(
                                                        response.data.id,
                                                        response.data.vDesignation,
                                                        response.data.vCompanyName,
                                                        response.data.vJobLocation,
                                                        0,
                                                        response.data.vDuration,
                                                        response.data.iUserId,
                                                        response.data.tCreatedAt,
                                                        response.data.tUpadatedAt
                                                    )
                                                )
                                                adapter.notifyItemInserted(experienceList.size -1)
                                            }

                                            recyclerView.scrollToPosition(adapter.itemCount - 1)
                                            inputLayout.visibility = GONE
                                            btnAddNewExperience.visibility = VISIBLE


                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse Exception: ${e.message}")
                                        makeToast(" Something wrong \n Please try again... ",0)
                                        inputLayout.visibility = VISIBLE
                                        btnAddNewExperience.visibility = GONE
                                    }
                                    finally {
                                        /*spDesignation.clearFocus()
                                        spDesignation.clearSelection()*/
                                        edDesignation.text?.clear()
                                        spJobLocation.clearFocus()
                                        spJobLocation.clearSelection()
                                        edCompanyName.text?.clear()
                                        edCompanyName.clearFocus()
                                        edDuration.text?.clear()
                                        edDuration.clearFocus()
                                        enteredDesignation = ""
                                        enteredCompanyName = ""
                                        selectedJobLocation = ""
                                        enteredDuration = ""
                                        checkBox.isChecked = false
                                        btnAddExperience.visibility = VISIBLE
                                        addExpProgress.visibility = GONE
                                    }

                                }

                                override fun onError(anError: ANError?) {
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                        )
                                    }
                                    makeToast(getString(R.string.some_thing_wrong_try_later),0)
                                    btnAddExperience.visibility = VISIBLE
                                    addExpProgress.visibility = GONE
                                }

                            }
                        )

                } else {
                    Utils.showNoInternetBottomSheet(this, this@ProfileActivity)
                    /*spDesignation.clearFocus()
                    spDesignation.clearSelection()*/
                    edDesignation.text?.clear()
                    spJobLocation.clearFocus()
                    spJobLocation.clearSelection()
                    edCompanyName.text?.clear()
                    edCompanyName.clearFocus()
                    edDuration.text?.clear()
                    edDuration.clearFocus()
                    enteredDesignation = ""
                    enteredCompanyName = ""
                    selectedJobLocation = ""
                    enteredDuration = ""
                    btnAddExperience.visibility = VISIBLE
                    addExpProgress.visibility = GONE
                }
            }
            else {
                if (enteredDesignation.isEmpty()) {
                    edDesignation.error = "Select A Designation"
                }

                if (enteredCompanyName.isEmpty()) {
                    edCompanyName.error = "Enter Company Name"
                }
                if (selectedJobLocation.isEmpty()) {
                    spJobLocation.errorText = "Select A Job Location"
                }

                btnAddExperience.visibility = VISIBLE
                addExpProgress.visibility = GONE
            }

        }
        alertDialogExperience = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            .setView(expDialogView)
            .setTitle("Change Info")
            .setNeutralButton("OK"){ dialog,_ ->
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
                experienceViewModel.writeToLocal(experienceList.toList()).invokeOnCompletion {
                    Log.d(TAG, "experienceInfoDialogView: experienceList is updated in datastore ${experienceList.isEmpty()}")
                }
                binding.btnShowMore.setImageResource(R.drawable.ic_down)
                if(experienceList.size > 3){
                    binding.btnShowMoreLayout.visibility = VISIBLE
                    isShowMore = true
                }
                else{
                    isShowMore = false
                    binding.btnShowMoreLayout.visibility = GONE
                }
                /*experienceAdapter.notifyDataSetChanged()*/
                if(addExperienceImg != null){
                    addExperienceImg!!.visibility = GONE

                    decideAddImgToVisibility(experienceList.isEmpty(),binding.experienceRecyclerView,addExperienceImg!!)
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }

            }
            .create()
        alertDialogExperience.show()
    }
    class ExperienceAdapter(
        private var btnVisibility:Boolean,
        private var mActivity: AppCompatActivity,
        private var experienceList: MutableList<Experience>,
        private val experienceEditUpdateListener: ExperienceEditUpdateListener?,
        private val cityList: ArrayList<String>
    ) : RecyclerView.Adapter<ExperienceAdapter.ExperiencesHolder>()
    {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperiencesHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_experience,parent,
                false)

            return if(experienceEditUpdateListener != null) {
                ExperiencesHolder(view, experienceEditUpdateListener)
            }
            else{
                ExperiencesHolder(view, null)
            }
        }

        override fun getItemCount(): Int {
            return experienceList.size
        }


        override fun onBindViewHolder(holder: ExperiencesHolder, position: Int) {
            val experience = experienceList[position]

            holder.bind(experience)
        }

        inner class ExperiencesHolder(itemView: View,experienceEditUpdateListener: ExperienceEditUpdateListener?):RecyclerView.ViewHolder(itemView) {

            private val dataLayout = itemView.findViewById<ConstraintLayout>(R.id.dataLayout)
            private val dataCard = itemView.findViewById<MaterialCardView>(R.id.dataCard)
            private val designation = itemView.findViewById<MaterialTextView>(R.id.designation)
            private val companyName = itemView.findViewById<MaterialTextView>(R.id.companyName)
            private val jobLocation = itemView.findViewById<MaterialTextView>(R.id.jobLocation)
            private val txtPresent = itemView.findViewById<MaterialTextView>(R.id.txtPresent)
            private val duration = itemView.findViewById<MaterialTextView>(R.id.duration)
            private val btnDelete = itemView.findViewById<AppCompatImageView>(R.id.btnDelete)
            private val btnEdit = itemView.findViewById<AppCompatImageView>(R.id.btnEdit)

            private val inputLayoutExperience = itemView.findViewById<CardView>(R.id.inputLayoutExperience)
            private val edDesignation = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.inputDesignation)
            private val edCompanyName = itemView.findViewById<TextInputEditText>(R.id.inputCompanyName)
            private val spJobLocation = itemView.findViewById<SmartMaterialSpinner<String>>(R.id.inputJobLocation)
            private val checkBox = itemView.findViewById<CheckBox>(R.id.inputCheckBox)
            private val edDuration = itemView.findViewById<TextInputEditText>(R.id.inputDuration)
            private val edDurationLayout = itemView.findViewById<TextInputLayout>(R.id.inputTextLayoutDuration)
            private val btnUpdateExperience = itemView.findViewById<MaterialButton>(R.id.btnUpdateExperience)
            private val updateExpProgress = itemView.findViewById<ProgressBar>(R.id.updateExpProgress)

            /*private var selectedDesignation = String()*/
            private var enteredDesignation = String()
            private var enteredCompanyName = String()
            private var selectedJobLocation = String()
            private var enteredDuration = String()
            private var isClickedEditBtn = false

            private fun setEditLayout(experience: Experience) {
                edCompanyName.setText(experience.vCompanyName)
                edDuration.setText(experience.vDuration)
                if(experience.bIsCurrentCompany == CURRENT_COMPANY){
                    checkBox.isChecked = true
                    edDurationLayout.visibility = GONE
                }
                else{
                    checkBox.isChecked = false
                    edDurationLayout.visibility = VISIBLE
                }
                edDesignation.setText(experience.vDesignation)
                enteredDesignation = experience.vDesignation
                val adapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(mActivity, android.R.layout.simple_dropdown_item_1line,mActivity.resources.getStringArray(R.array.indian_designations).toList())
                edDesignation.setAdapter(adapter)

                /*spDesignation.setSearchDialogGravity(Gravity.TOP)
                spDesignation.arrowPaddingRight = 19
                spDesignation.item = mActivity.resources.getStringArray(R.array.indian_designations).toList()
                spDesignation.setSelection(mActivity.resources.getStringArray(R.array.indian_designations).toList().indexOf(experience.vDesignation))
                spDesignation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                        spDesignation.isOutlined = true
                        selectedDesignation = spDesignation.item[position]
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>?) {

                    }
                }*/

                spJobLocation.setSearchDialogGravity(Gravity.TOP)
                spJobLocation.arrowPaddingRight = 19
                spJobLocation.item = cityList
                spJobLocation.setSelection(cityList.indexOf(experience.vJobLocation))
                spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                        spJobLocation.isOutlined = true
                        selectedJobLocation = spJobLocation.item[position]
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>?) {

                    }
                }


                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if(isChecked) {
                        edDurationLayout.visibility = GONE
                        edDuration.setText("")
                    }
                    else{
                        edDurationLayout.visibility = VISIBLE
                    }
                }


                btnUpdateExperience.setOnClickListener {
                    enteredCompanyName = edCompanyName.text.toString().trim()
                    enteredDuration = edDuration.text.toString().trim()

                    if(enteredDesignation.isNotEmpty() && enteredCompanyName.isNotEmpty() && selectedJobLocation.isNotEmpty()){
                        if (checkBox.isChecked) {
                            experienceEditUpdateListener!!.edit(
                                absoluteAdapterPosition,
                                Experience(
                                    experience.id,
                                    enteredDesignation,
                                    enteredCompanyName,
                                    selectedJobLocation,
                                    1,
                                    null,
                                    experience.iUserId,
                                    experience.tCreatedAt,
                                    experience.tUpadatedAt
                                )
                            )
                        } else {
                            if (enteredDuration.isNotEmpty()){
                                experienceEditUpdateListener!!.edit(
                                    absoluteAdapterPosition,
                                    Experience(
                                        experience.id,
                                        enteredDesignation,
                                        enteredCompanyName,
                                        selectedJobLocation,
                                        0,
                                        enteredDuration,
                                        experience.iUserId,
                                        experience.tCreatedAt,
                                        experience.tUpadatedAt
                                    )
                                )
                            }else{
                                edDuration.error = "Enter Duration"
                                return@setOnClickListener
                            }

                        }
                    }
                    else {
                        if (enteredDesignation.isEmpty()) {
                            edDesignation.error = "Select A Designation"
                        }
                        if (enteredCompanyName.isEmpty()) {
                            edCompanyName.error = "Enter Company Name"
                        }
                        if (selectedJobLocation.isEmpty()) {
                            spJobLocation.errorText = "Select A Job Location"
                        }
                        updateExpProgress.visibility = GONE
                    }
                    btnEdit.setImageResource(R.drawable.baseline_edit_18)
                    itemView.setBackgroundResource(R.color.white)
                    isClickedEditBtn = false
                    inputLayoutExperience.visibility = GONE
                    /*notifyItemChanged(absoluteAdapterPosition)*/

                }
            }

            @SuppressLint("NotifyDataSetChanged")
            fun bind(experience: Experience){
                designation.text = experience.vDesignation
                companyName.text = experience.vCompanyName
                jobLocation.text = experience.vJobLocation
                if(experience.bIsCurrentCompany == 1){
                    duration.visibility = GONE
                    txtPresent.visibility = VISIBLE
                    dataCard.strokeWidth = 4
                    dataCard.strokeColor = ContextCompat.getColor(mActivity,R.color.blue)
                }
                else {
                    txtPresent.visibility = GONE
                    if(!experience.vDuration.isNullOrEmpty()) {
                        duration.visibility = VISIBLE
                        duration.text = experience.vDuration.plus(" Years")
                    }
                    dataCard.strokeWidth = 0
                    dataCard.strokeColor = ContextCompat.getColor(mActivity,android.R.color.transparent)
                }
                if(btnVisibility){
                    btnDelete.visibility = VISIBLE
                    btnEdit.visibility  = VISIBLE
                }
                else{
                    btnDelete.visibility = GONE
                    btnEdit.visibility  = GONE

                }


                if(experienceEditUpdateListener != null) {
                    btnDelete.setOnClickListener {
                        experienceEditUpdateListener.delete(absoluteAdapterPosition,experience)
                        /*experienceList.removeAt(absoluteAdapterPosition)*/
                        notifyDataSetChanged()
                    }

                    btnEdit.setOnClickListener {
                        if (!isClickedEditBtn) {
                            itemView.setBackgroundResource(R.color.blueLight)
                            btnEdit.setImageResource(R.drawable.ic_close)
                            inputLayoutExperience.visibility = VISIBLE
                            setEditLayout(experience)
                        } else {
                            btnEdit.setImageResource(R.drawable.baseline_edit_18)
                            itemView.setBackgroundResource(R.color.white)
                            inputLayoutExperience.visibility = GONE
                        }
                        isClickedEditBtn = !isClickedEditBtn
                    }
                }
            }
        }
        interface ExperienceEditUpdateListener{

            fun delete(position:Int,experience: Experience)
            fun edit(position: Int, experience: Experience)
        }
    }

    private fun qualificationDialogView(){

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
        spQualification.item = resources.getStringArray(R.array.indian_streams).toList()
        if(!qualification.isNullOrEmpty()) {
            spQualification.setSelection(
                resources.getStringArray(R.array.indian_streams).toList().indexOf(qualification)
            )
        }
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

        if(!tagLine.isNullOrEmpty()){
            val list = tagLine!!.split(" || ")
            if(list.isNotEmpty()) {
                for (word in list) {
                    expertiseList.add(word)
                }
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

        val spResidentialCity = basicDialogView.findViewById<SmartMaterialSpinner<String>>(R.id.spResidentialCity)
        spResidentialCity.setSearchDialogGravity(Gravity.TOP)
        spResidentialCity.arrowPaddingRight = 19
        spResidentialCity.item = cityList.toList()
        if(!residentialCity.isNullOrEmpty()) {
            spResidentialCity.setSelection(cityList.indexOf(residentialCity))
        }
        spResidentialCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                spResidentialCity.isOutlined = true
                selectedJobLocation = spResidentialCity.item[position]
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
                    uploadProgressBar.visibility = View.GONE
                    val pdfUri = data?.data!!
                    val file = Utils.convertUriToPdfFile(this@ProfileActivity, pdfUri)!!
                    if(file.length().toFloat() > (1024 * 1024).toFloat()) {
                        makeToast("FIle size should be less then 1 Mb",0)
                    }
                    else{
                        btnUpload.playAnimation()
                        Handler(Looper.getMainLooper()).postDelayed({
                            resumeFile = file
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
                                        /*resumeUri = pdfUri.toString()*/
                                        tvResumeFileName.text = resumeFileName
                                        tvResumeFileName.visibility = VISIBLE
                                    }
                                } finally {
                                    myCursor?.close()
                                }
                            }
                        },3000)
                    }


                }

                SELECT_PROFILE_BANNER_IMG -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!

                    profileBackImg.visibility = INVISIBLE
                    profileBackImg.isClickable = false
                    profileBackImgLodding.visibility = VISIBLE
                    profileBackImgLodding.playAnimation()
                    Handler(Looper.getMainLooper()).postDelayed({
                        compressImg(this@ProfileActivity,photoUri,profileBackImg){

                            profileBannerFile = it
                            Glide.with(this@ProfileActivity)
                                .load(profileBannerFile)
                                .apply(
                                    RequestOptions
                                        .placeholderOf(R.drawable.profile_default_back_img)
                                        .error(R.drawable.profile_default_back_img)
                                        .fitCenter()
                                )
                                .into(profileBackImg)
                            profileBackImg.visibility = VISIBLE
                            profileBackImg.isClickable = true
                            profileBackImgLodding.visibility = GONE
                            /*profileBannerImgUri = photoUri.toString()*/
                        }
                    },3000)

                }

                SELECT_PROFILE_IMG -> if (resultCode == RESULT_OK) {
                    val photoUri = data?.data!!
                    val file = File(Utils.getRealPathFromURI(this, photoUri).toString())
                    profileImgLodding.visibility = VISIBLE
                    profileImgDia.visibility = INVISIBLE
                    profileImgLodding.playAnimation()
                    Handler(Looper.getMainLooper()).postDelayed({
                        compressImg(this@ProfileActivity,photoUri,profileImgDia){
                            profileImgFile = it
                            Glide.with(this@ProfileActivity)
                                .load(profileImgFile)
                                .apply(
                                    RequestOptions
                                        .placeholderOf(R.drawable.profile_default_image)
                                        .error(R.drawable.profile_default_image)
                                        .circleCrop()
                                )
                                .into(profileImgDia)
                            profileImgDia.visibility = VISIBLE
                            profileImgLodding.visibility = GONE
                            /*profileImgUri = photoUri.toString()*/
                        }

                    }, 3000)

                }
            }
        }
    }
    private fun storeProfileImg(profileImg: File?) {
        if(profileImg != null) {
            showProgressDialog("please wait")
            if (Utils.isNetworkAvailable(this@ProfileActivity)) {
                AndroidNetworking.upload(NetworkUtils.UPDATE_PROFILE_PIC)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN, "")}")
                    .addMultipartFile("profilePic", profileImg)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        GetUserById::class.java,
                        object : ParsedRequestListener<GetUserById> {
                            override fun onResponse(response: GetUserById?) {
                                try {
                                    if (response != null) {
                                        Log.d(
                                            TAG,
                                            "onResponse: storeProfileImg ${response.data.tProfileUrl}"
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            userDataRepository.storeProfileImg(
                                                response.data.tProfileUrl,
                                            )
                                        }.invokeOnCompletion {
                                            Log.d(
                                                TAG,
                                                "onActivityResult: Profile Img is Stored in datastore"
                                            )
                                        }
                                        profileImgUri = response.data.tProfileUrl
                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                } finally {
                                    hideProgressDialog()
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
            } else {
                Utils.showNoInternetBottomSheet(this, this)
                hideProgressDialog()
            }
        }
    }

    private fun storeProfileBannerImg(profileBannerImg:File?) {
        if(profileBannerImg!=null) {
            showProgressDialog("please wait")
            if (Utils.isNetworkAvailable(this@ProfileActivity)) {
                AndroidNetworking.upload(NetworkUtils.UPDATE_BANNER_PIC)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN, "")}")
                    .addMultipartFile("bannerPic", profileBannerImg)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        GetUserById::class.java,
                        object : ParsedRequestListener<GetUserById> {
                            override fun onResponse(response: GetUserById?) {
                                try {
                                    if (response != null) {
                                        Log.d(
                                            "test",
                                            "onResponse: storeProfileBannerImg : ${response.data}"
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            userDataRepository.storeProfileBannerImg(
                                                response.data.tProfileBannerUrl,
                                            )
                                        }.invokeOnCompletion {
                                            Log.d(
                                                TAG,
                                                "onActivityResult: Profile Img is Stored in datastore"
                                            )
                                        }
                                        profileBannerImgUri = response.data.tProfileUrl
                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                } finally {
                                    hideProgressDialog()
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
            } else {
                Utils.showNoInternetBottomSheet(this, this)
                hideProgressDialog()
            }
        }

    }

    private fun storeResume(resumeFile: File?,callback: (Boolean) -> Unit){

        if(resumeFile != null) {
            showProgressDialog("please wait")
            if (Utils.isNetworkAvailable(this@ProfileActivity)) {
                AndroidNetworking.upload(NetworkUtils.UPDATE_RESUME)
                    .setOkHttpClient(NetworkUtils.okHttpClient)
                    .addHeaders("Authorization", "Bearer ${prefmanger.get(AUTH_TOKEN, "")}")
                    .addMultipartFile("resume", resumeFile)
                    .setPriority(Priority.MEDIUM).build().getAsObject(
                        GetUserById::class.java,
                        object : ParsedRequestListener<GetUserById> {
                            override fun onResponse(response: GetUserById?) {
                                try {

                                    if (response != null) {
                                        Log.d("test", "onResponse: storeResume : ${response.data}")
                                        CoroutineScope(Dispatchers.IO).launch {
                                            userDataRepository.storeResumeData(
                                                response.data.tResumeUrl,
                                            )
                                        }.invokeOnCompletion {
                                            Log.d(
                                                TAG,
                                                "onActivityResult: Profile Img is Stored in datastore"
                                            )
                                        }
                                        resumeUri = response.data.tResumeUrl
                                        callback(true)
                                    }
                                } catch (e: Exception) {
                                    Log.e("#####", "onResponse Exception: ${e.message}")
                                    callback(false)
                                } finally {
                                    hideProgressDialog()
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
                                callback(false)
                            }

                        })
            } else {
                Utils.showNoInternetBottomSheet(this, this)
                callback(false)
                hideProgressDialog()
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