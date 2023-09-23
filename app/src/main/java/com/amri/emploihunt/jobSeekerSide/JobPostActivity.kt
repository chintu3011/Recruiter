package com.amri.emploihunt.jobSeekerSide

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityJobPostBinding
import com.amri.emploihunt.databinding.InternetBottomSheetLayoutBinding
import com.amri.emploihunt.model.ApplyModel
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.model.SaveModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.getTimeAgo
import com.amri.emploihunt.util.Utils.serializable
import com.google.android.material.bottomsheet.BottomSheetDialog


class JobPostActivity : BaseActivity() , ViewTreeObserver.OnScrollChangedListener{

    private lateinit var binding: ActivityJobPostBinding
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var selectedPost : Jobs
    lateinit var prefManager: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()
        prefManager = PrefManager.prefManager(this)
        selectedPost = intent.extras?.serializable("ARG_JOB_TITLE")!!
        if (intent.extras?.getInt("applyList",0) == 1){
            selectedPost.iIsApplied = 1
        }


        binding.frame.viewTreeObserver.addOnScrollChangedListener(this)
        retreivedescription()

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

    private fun callSaveJob() {
        if (Utils.isNetworkAvailable(this)){

            AndroidNetworking.post(NetworkUtils.SAVE)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("jobId", selectedPost.id.toString().trim())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    SaveModel::class.java,
                    object : ParsedRequestListener<SaveModel> {
                        override fun onResponse(response: SaveModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()


                                    binding.imgSave.setImageDrawable(resources.getDrawable(R.drawable.filled_heart))
                                    selectedPost.iIsSaved = 1
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    hideProgressDialog()


                                }
                                //hideProgressDialog()
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
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }
    }

    private fun callUnSaveJob() {
        if (Utils.isNetworkAvailable(this)){

            AndroidNetworking.post(NetworkUtils.UN_SAVE)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("jobId", selectedPost.id.toString().trim())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    SaveModel::class.java,
                    object : ParsedRequestListener<SaveModel> {
                        override fun onResponse(response: SaveModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()


                                    binding.imgSave.setImageDrawable(resources.getDrawable(R.drawable.unfilled_heart))
                                    selectedPost.iIsSaved = 0
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)



                                }
                                //hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

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
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }
    }

    private fun retreivedescription() {
        binding.jobTitle.text = selectedPost.vJobTitle

        binding.companyName.text = selectedPost.vCompanyName
        binding.jobLocation.text = selectedPost.vAddress
        binding.jobPostDuration.text = getTimeAgo(this, selectedPost.tCreatedAt!!.toLong())
        binding.applications.text = selectedPost.iNumberOfApplied.toString() + " Applications"
        binding.workingMode.text = selectedPost.vWrokingMode
        binding.rolDes.setText(selectedPost.vJobRoleResponsbility)
        binding.jobDes.setText(selectedPost.tDes)
        binding.technicalSkills.text = selectedPost.tTechnicalSkill
        binding.softSkills.text = selectedPost.tSoftSkill
        binding.tvSalary.text = "Salary - ${selectedPost.vSalaryPackage.toString()} Lakh"
        binding.education.text = selectedPost.vEducation
        Glide.with(this@JobPostActivity).load(selectedPost.tCompanyLogoUrl).placeholder(R.mipmap.ic_logo).into(binding.companyLogo)

        if (selectedPost.iIsApplied == 1){
            binding.btnApply.isEnabled = false
            binding.btnApply.text = getString(R.string.already_applied)
            binding.btnApply.setBackgroundColor( resources.getColor(R.color.colorPrimaryLight))
        }else{
            binding.btnApply.isEnabled = true
            binding.btnApply.text = getString(R.string.apply)
            binding.btnApply.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))

        }
        if (selectedPost.iIsSaved == 1){
            binding.imgSave.setImageDrawable(resources.getDrawable(R.drawable.filled_heart))
        }else{
            binding.imgSave.setImageDrawable(resources.getDrawable(R.drawable.unfilled_heart))
        }
        binding.btnApply.setOnClickListener {
            callApplyJob()
        }

        binding.imgSave.setOnClickListener {
            if (binding.imgSave.drawable.constantState == ContextCompat.getDrawable(this,
                    R.drawable.unfilled_heart
                )!!.constantState){
                callSaveJob()
            }else{
                callUnSaveJob()
            }

        }
    }

    private fun callApplyJob() {

        if (Utils.isNetworkAvailable(this)){

            AndroidNetworking.post(NetworkUtils.APPLY)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("jobId", selectedPost.id.toString())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    ApplyModel::class.java,
                    object : ParsedRequestListener<ApplyModel> {
                        override fun onResponse(response: ApplyModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()


                                    selectedPost.vCompanyName?.let { it1 -> showApplyBottomSheet(it1) }
                                    binding.btnApply.isEnabled = false
                                    binding.btnApply.text = getString(R.string.already_applied)
                                    binding.btnApply.setBackgroundColor( resources.getColor(R.color.colorPrimaryLight))
                                    selectedPost.iIsApplied = 1
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    hideProgressDialog()


                                }
                                //hideProgressDialog()
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
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }


    }
    fun showApplyBottomSheet(company:String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val binding_ = InternetBottomSheetLayoutBinding.inflate(layoutInflater)
        binding_.tvDes.text =
            getString(R.string.thank_you_dear_candidate_please_wait_hr_are_contact_soon, company)
        binding_.animationView.setAnimation(R.raw.apply)
        binding_.btnOk.setOnClickListener {
            bottomSheetDialog.dismiss()

        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(binding_.root)
        bottomSheetDialog.show()
    }

    override fun onScrollChanged() {
        val view = binding.frame.getChildAt(binding.frame.childCount - 1)
        val topDetector = binding.frame.scrollY
        val bottomDetector: Int = view.bottom - (binding.frame.height + binding.frame.scrollY)
        if (bottomDetector > 0) {
            binding.toolbar.title = selectedPost.vJobTitle
            binding.jobTitle.visibility = View.GONE
        }
        if (topDetector <= 0) {
            binding.toolbar.title = " "
            binding.jobTitle.visibility = View.VISIBLE
        }
    }
}