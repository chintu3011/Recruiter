package com.amri.emploihunt.jobSeekerSide

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.settings.AddJobPreferenceActivity
import com.amri.emploihunt.databinding.ActivityJobSeekarDetailsBinding
import com.amri.emploihunt.databinding.PdfViewerDialogBinding
import com.amri.emploihunt.databinding.RowJobPreferenceForRecruiterBinding
import com.amri.emploihunt.messenger.FullImageViewActivity
import com.amri.emploihunt.model.DataAppliedCandidate
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.Experience
import com.amri.emploihunt.model.UserExpModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.settings.ProfileActivity
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.CURRENT_COMPANY
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.serializable
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignTop
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class JobSeekerDetailsActivity : BaseActivity() {
    lateinit var binding: ActivityJobSeekarDetailsBinding
    private lateinit var selectedCandidate: DataAppliedCandidate
    private var dialog: Dialog? = null
    private var callBalloon: Balloon ?= null
    private var emailBalloon: Balloon ?= null


    private lateinit var prefManager:SharedPreferences

    companion object{
        private const val TAG = "JobSeekerDetailsActivity"
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobSeekarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedCandidate = intent.extras?.serializable("ARG_JOB_TITLE")!!

        prefManager = prefManager(this)
        if (!selectedCandidate.userJobPref.tProfileBannerUrl.isNullOrEmpty()) {
            Glide.with(this@JobSeekerDetailsActivity)
                .load(NetworkUtils.BASE_URL_MEDIA+selectedCandidate.userJobPref.tProfileBannerUrl)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.profile_default_back_img)
                        .error(R.drawable.profile_default_back_img)
                        .fitCenter()

                )
                .into(binding.profileBackImg)
        }


        if (!selectedCandidate.userJobPref.tProfileUrl.isNullOrEmpty()) {
            Glide.with(this@JobSeekerDetailsActivity)
                .load(NetworkUtils.BASE_URL_MEDIA+selectedCandidate.userJobPref.tProfileUrl)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.profile_default_back_img)
                        .error(R.drawable.profile_default_back_img)
                        .circleCrop()

                )
                .into(binding.profileImg)
        }

        binding.profileImg.setOnClickListener {
            if(!selectedCandidate.userJobPref.tProfileUrl.isNullOrEmpty()) {
                val intent = Intent(this, FullImageViewActivity::class.java)
                intent.putExtra("Uri", selectedCandidate.userJobPref.tProfileUrl)
                startActivity(intent)
            }
            else{
                makeToast("Profile image not found",0)
            }
        }

        val fullName = "${selectedCandidate.userJobPref.vFirstName} ${selectedCandidate.userJobPref.vLastName}"
        binding.userName.text = fullName
        binding.residentialCity.text = selectedCandidate.userJobPref.vCity
        showViewIfNotEmpty(selectedCandidate.userJobPref.vCity,binding.residentialCity)
        binding.expertise.text = selectedCandidate.userJobPref.tTagLine
        showViewIfNotEmpty(selectedCandidate.userJobPref.tTagLine,binding.expertise)

        binding.currentCompany.text = selectedCandidate.userJobPref.vCurrentCompany
        showViewIfNotEmpty(selectedCandidate.userJobPref.vCurrentCompany,binding.currentCompany)

        val phoneNo = selectedCandidate.userJobPref.vMobile
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

        val email = selectedCandidate.userJobPref.vEmail
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


        binding.btnPhone.setOnClickListener{
            if(callBalloon != null){
                binding.btnPhone.showAlignTop(callBalloon!!)
            }
        }
        binding.btnEmail.setOnClickListener{
            if(emailBalloon != null){
                binding.btnEmail.showAlignTop(emailBalloon!!)
            }
        }


        /*binding.workingMode.text = selectedCandidate.userJobPref.vWorkingMode*/
        binding.bioJ.text = selectedCandidate.userJobPref.tBio
        showViewIfNotEmpty(selectedCandidate.userJobPref.tBio,binding.bioJ)
        decideAddImgToVisibility(selectedCandidate.userJobPref.tBio.isEmpty(),binding.bioJ,binding.aboutInfoLayoutJ,R.id.txtAboutJ)

        binding.qualificationJ.text = selectedCandidate.userJobPref.vQualification
        showViewIfNotEmpty(selectedCandidate.userJobPref.vQualification,binding.qualificationJ)
        decideAddImgToVisibility(selectedCandidate.userJobPref.vQualification.isEmpty(),binding.qualificationJ,binding.qualificationLayoutJ,R.id.txtQualificationJ)


        getExperiences(selectedCandidate.userJobPref.id){ experienceList ->
            if(experienceList.isNotEmpty()) {
                for(index in 0 until experienceList.size){
                    if(experienceList[index].bIsCurrentCompany == CURRENT_COMPANY){
                        val currExp = experienceList[index]
                        experienceList.removeAt(index)
                        experienceList.add(0,currExp)
                        break
                    }
                }

                Log.d(TAG, "onCreate: getExperiences : $experienceList")
                setExperiences(experienceList)
                binding.btnShowMoreLayout.setOnClickListener {
                    if(isShowMore){
                        binding.btnShowMore.setImageResource(R.drawable.ic_up)
                        binding.experienceRecyclerView.removeAllViews()
                        experienceAdapter = ExperienceAdapter(false, this, experienceList)
                        binding.experienceRecyclerView.adapter = experienceAdapter
                        experienceAdapter.notifyDataSetChanged()
                    }
                    else{
                        binding.btnShowMore.setImageResource(R.drawable.ic_down)
                        binding.experienceRecyclerView.removeAllViews()
                        experienceAdapter = ExperienceAdapter(false, this, experienceList.subList(0, 3))
                        binding.experienceRecyclerView.adapter = experienceAdapter
                        experienceAdapter.notifyDataSetChanged()
                    }
                    isShowMore = !isShowMore
                }
            }
            else{
                decideAddImgToVisibility(true,binding.experienceRecyclerView,binding.experienceLayout,R.id.txtExperienceJ)
            }
        }


        /*if (!selectedCandidate.userJobPref.vCurrentCompany.isNullOrBlank()){
            binding.layExperience.visibility = View.VISIBLE
            binding.flow12.visibility = View.VISIBLE
            binding.flow13.visibility = View.VISIBLE
            binding.lineDivider4.visibility = View.VISIBLE

            binding.tvDesigantion.text = selectedCandidate.userJobPref.vDesignation
            binding.tvCompany.text = selectedCandidate.userJobPref.vCurrentCompany
            binding.tvYear.text = "${selectedCandidate.userJobPref.vDuration} year"
            binding.tvLocation.text = selectedCandidate.userJobPref.vJobLocation

        }*/
        if (selectedCandidate.userJobPref.jobPreference!!.size >0){
            binding.layJobPref.visibility = View.VISIBLE
            binding.jobPreferenceRv.visibility = View.VISIBLE
            /*binding.lineDivider5.visibility = View.VISIBLE*/
            binding.jobPreferenceRv.setHasFixedSize(true)
            binding.jobPrefAdapter = JobPreferenceListAdapter(
                selectedCandidate.userJobPref.jobPreference!!,
                object : JobPreferenceListAdapter.OnCategoryClick {
                    override fun onCategoryClicked(
                        view: View,
                        templateModel: DataJobPreferenceList
                    ) {
                        val intent =
                            Intent(this@JobSeekerDetailsActivity, AddJobPreferenceActivity::class.java)
                        intent.putExtra("jobPref", templateModel)
                        intent.putExtra("update", true)
                        startActivity(intent)
                    }

                })

        }
        else{
            decideAddImgToVisibility(true,binding.jobPreferenceRv,binding.jobPreferLayout,R.id.layJobPref)
        }
        /*binding.imgBack.setOnClickListener {
            finish()
        }

        binding.btnopen.setOnClickListener {
            showDialog()
        }*/



        if(!selectedCandidate.userJobPref.tResumeUrl.isNullOrEmpty()){
            binding.resumeFileNameJ.visibility = View.VISIBLE
            binding.resumeFileNameJ.text = selectedCandidate.userJobPref.vFirstName.plus("'s resume")
        }
        else{
            decideAddImgToVisibility(true,binding.resumeFileNameJ,binding.resumeLayout,R.id.txtResumeJ)
        }
        binding.resumeFileNameJ.setOnClickListener {
            showDialog()
        }


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

    private fun decideAddImgToVisibility(
        dataState: Boolean,
        dataView: View,
        layout: ConstraintLayout,
        titleViewId:Int


    ) {
        Log.d(TAG, "decideAddImgToVisibility: $dataState")
        if(dataState){
            Log.d(TAG, "decideAddImgToVisibility: ${true}")
            dataView.visibility = View.GONE
            createEmptyDataImg(layout,titleViewId)
        }
        else{
            Log.d(TAG, "decideAddImgToVisibility: ${false}")
            dataView.visibility = View.VISIBLE
        }
    }

    private fun createEmptyDataImg(layout: ConstraintLayout, view:Int)/*: ShapeableImageView*/ {

        val imageView = ShapeableImageView(baseContext)
        imageView.id = View.generateViewId()
        imageView.layoutParams = ConstraintLayout.LayoutParams(
            0,
            200
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageResource(R.drawable.default_empty_data)

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
        imageView.visibility = View.VISIBLE
        /*return imageView*/
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

    private fun showViewIfNotEmpty(value: String, view: View) {
        if(value.isNotEmpty()){
            view.visibility = View.VISIBLE
        }
        else{
            view.visibility = View.GONE
        }
    }

    private fun getExperiences(userId : Int,callback: (MutableList<Experience>) -> Unit) {

        if (Utils.isNetworkAvailable(this)) {
            AndroidNetworking.get(NetworkUtils.GET_ALL_EXPERIENCE_BY_ID)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("iUserId", userId.toString())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    UserExpModel::class.java,
                    object : ParsedRequestListener<UserExpModel> {
                        override fun onResponse(response: UserExpModel?) {
                            try {
                                if (response != null) {
                                    Log.d(
                                        TAG,
                                        "onResponse: Experience data received: ${response.data}"
                                    )

                                    callback(response.data.toMutableList())
                                }

                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                                hideProgressDialog()
                            }

                        }

                        override fun onError(anError: ANError?) {
                            try {

                                anError?.let {
                                    Log.e(
                                        "#####",
                                        "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )
                                    /** errorCode == 404 means User number is not registered or New user */
                                    hideProgressDialog()
                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onError: ${e.message}")
                            }
                        }

                    }
                )
        } else {
            Utils.showNoInternetBottomSheet(this, this)
        }
    }

    private lateinit var experienceAdapter:ExperienceAdapter
    private var isShowMore = false
    @SuppressLint("NotifyDataSetChanged")
    private fun setExperiences(experienceList: MutableList<Experience>) {

        Log.d(ProfileActivity.TAG, "setExperiences: removeView : ${binding.experienceRecyclerView.childCount}")
        binding.btnShowMoreLayout.visibility = View.GONE
        Log.d("#####", "setProfileData: Experience data \n ${experienceList.size}")
        binding.experienceRecyclerView.removeAllViews()
        if (experienceList.isNotEmpty()){
            binding.experienceRecyclerView.visibility = View.VISIBLE

            experienceAdapter =
                ExperienceAdapter(false, this, experienceList.subList(0, 3))
            binding.experienceRecyclerView.adapter = experienceAdapter
            experienceAdapter.notifyDataSetChanged()
            Log.d(ProfileActivity.TAG, "setExperiences: removeView : ${binding.experienceRecyclerView.childCount}")

            if(experienceList.size > 3){
                binding.btnShowMoreLayout.visibility = View.VISIBLE
                isShowMore = true
            }
            else{
                isShowMore = false
                binding.btnShowMoreLayout.visibility = View.GONE
            }

        }
    }
    class ExperienceAdapter(
        private var btnVisibility:Boolean,
        private var mActivity: AppCompatActivity,
        private var experienceList: MutableList<Experience>,
        /* private val onExperienceClickLiner:OnExperienceClickLiner*/
    ) : RecyclerView.Adapter<ExperienceAdapter.ExperiencesHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperiencesHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.row_experience,parent,
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

            private val dataLayout = itemView.findViewById<ConstraintLayout>(R.id.dataLayout)
            private val dataCard = itemView.findViewById<MaterialCardView>(R.id.dataCard)
            private val designation = itemView.findViewById<MaterialTextView>(R.id.designation)
            private val companyName = itemView.findViewById<MaterialTextView>(R.id.companyName)
            private val jobLocation = itemView.findViewById<MaterialTextView>(R.id.jobLocation)

            private val txtPresent = itemView.findViewById<MaterialTextView>(R.id.txtPresent)
            private val duration = itemView.findViewById<MaterialTextView>(R.id.duration)
            private val inputLayoutExperience = itemView.findViewById<CardView>(R.id.inputLayoutExperience)
            private val btnDelete = itemView.findViewById<AppCompatImageView>(R.id.btnDelete)
            private val btnEdit = itemView.findViewById<AppCompatImageView>(R.id.btnEdit)

            @SuppressLint("NotifyDataSetChanged")
            fun bind(experience: Experience){
                dataLayout.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
                btnEdit.visibility  = View.GONE
                inputLayoutExperience.visibility = View.GONE
                
                designation.text = experience.vDesignation
                companyName.text = experience.vCompanyName
                jobLocation.text = experience.vJobLocation
                if(experience.bIsCurrentCompany == 1){
                    duration.visibility = View.GONE
                    txtPresent.visibility = View.VISIBLE
                    dataCard.strokeWidth = 4
                    dataCard.strokeColor = ContextCompat.getColor(mActivity,R.color.blue)
                }
                else {
                    txtPresent.visibility = View.GONE
                    if (!experience.vDuration.isNullOrEmpty()) {
                        duration.visibility = View.VISIBLE
                        duration.text = experience.vDuration.plus(" Years")
                    }
                    dataCard.strokeWidth = 0
                    dataCard.strokeColor = ContextCompat.getColor(mActivity,android.R.color.transparent)
                }
            }
        }
    }


    private fun showDialog() {
        try {


            val builder = AlertDialog.Builder(this)
            val bindingDialog = PdfViewerDialogBinding.inflate(layoutInflater)

            builder.setView(bindingDialog.root)

            RetrievePDFFromURL(bindingDialog.idPDFView,bindingDialog.progressCircular).execute("https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf")
            dialog = builder.create()
            dialog?.let {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.show()
            }
            bindingDialog.closeIv.setOnClickListener {
                (dialog as AlertDialog).dismiss()
            }
        } catch (e: Exception) {
            Log.e("#####", "showProgressDialog exception: ${e.message}")
        }
    }
    class JobPreferenceListAdapter(
        private var dataList: MutableList<DataJobPreferenceList>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<JobPreferenceListAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowJobPreferenceForRecruiterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobPrefModel = dataList[position]

            holder.binding.jobTitle.text = jobPrefModel.vJobTitle
            holder.binding.tvLocation.text = jobPrefModel.vJobLocation
            holder.binding.salary.text = jobPrefModel.vExpectedSalary.plus(" LPA +")
            holder.binding.tvWorkingMode.text = jobPrefModel.vWorkingMode



            holder.itemView.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, jobPrefModel)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowJobPreferenceForRecruiterBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: DataJobPreferenceList)
        }
    }
    class RetrievePDFFromURL(pdfView: PDFView,processBar: ProgressBar) :
        AsyncTask<String, Void, InputStream>() {

        // on below line we are creating a variable for our pdf view.
        val mypdfView: PDFView = pdfView

        val processBar: ProgressBar = processBar

        // on below line we are calling our do in background method.
        override fun doInBackground(vararg params: String?): InputStream? {
            // on below line we are creating a variable for our input stream.
            var inputStream: InputStream? = null
            try {
                // on below line we are creating an url
                // for our url which we are passing as a string.
                val url = URL(params.get(0))

                // on below line we are creating our http url connection.
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection

                // on below line we are checking if the response
                // is successful with the help of response code
                // 200 response code means response is successful
                if (urlConnection.responseCode == 200) {
                    // on below line we are initializing our input stream
                    // if the response is successful.
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            }
            // on below line we are adding catch block to handle exception
            catch (e: Exception) {
                // on below line we are simply printing
                // our exception and returning null
                e.printStackTrace()
                return null;
            }
            // on below line we are returning input stream.
            return inputStream;
        }

        // on below line we are calling on post execute
        // method to load the url in our pdf view.
        override fun onPostExecute(result: InputStream?) {
            // on below line we are loading url within our
            // pdf view on below line using input stream.
            mypdfView.fromStream(result).load()
            processBar.visibility = View.GONE

        }
    }
}