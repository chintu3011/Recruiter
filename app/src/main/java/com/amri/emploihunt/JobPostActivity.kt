package com.amri.emploihunt

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat

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
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.getTimeAgo
import com.amri.emploihunt.util.Utils.serializable
import com.google.android.material.bottomsheet.BottomSheetDialog


class JobPostActivity : BaseActivity() {

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
        retreivedescription()
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
        Glide.with(this@JobPostActivity).load(selectedPost.tCompanyLogoUrl).into(binding.companyLogo)

        if (selectedPost.iIsApplied == 1){
            binding.btnApply.isEnabled = false
            binding.btnApply.text = getString(R.string.already_applied)
            binding.btnApply.setBackgroundColor( resources.getColor(R.color.check_def_color))
        }else{
            binding.btnApply.isEnabled = true
            binding.btnApply.text = getString(R.string.apply)
            binding.btnApply.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.theme_blue))

        }
        binding.btnApply.setOnClickListener {
            callApplyJob()
        }
        binding.imgBack.setOnClickListener {
            finish()
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
        val binding = InternetBottomSheetLayoutBinding.inflate(layoutInflater)
        binding.tvDes.text =
            getString(R.string.thank_you_dear_candidate_please_wait_hr_are_contact_soon, company)
        binding.animationView.setAnimation(R.raw.apply_success)
        binding.btnOk.setOnClickListener {
            bottomSheetDialog.dismiss()

        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
    }
}