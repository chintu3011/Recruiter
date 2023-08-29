package com.amri.emploihunt.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import androidx.lifecycle.coroutineScope
import com.airbnb.lottie.LottieAnimationView
import com.amri.emploihunt.R
import com.amri.emploihunt.authentication.LoginActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentSettingBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.model.LogoutMain
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.HomeRecruitFragment
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_LOGIN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.set
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class SettingJobSeekerFragment : BaseFragment() {


    private lateinit var prefmanger: SharedPreferences
    private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
    lateinit var binding: FragmentSettingBinding
    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater)
        prefmanger = PrefManager.prefManager(requireContext())
        jobSeekerProfileInfo = JobSeekerProfileInfo(requireContext())
        recruiterProfileInfo = RecruiterProfileInfo(requireContext())
        setUserData()
        binding.rlProfile.setOnClickListener {

            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener {
            if (prefmanger.getInt(ROLE,0) == 0) {

                (activity as HomeJobSeekerActivity).binding.bottomNavigationView[0]
            }else{
                val  fragment = HomeRecruitFragment()
                (activity as HomeRecruiterActivity).binding.bottomNavigation[0]
            }

        }
        binding.rlJobPreference.setOnClickListener {
            val intent = Intent(requireContext(), JobPreferenceActivity::class.java)
            startActivity(intent)
        }

        binding.rlApplyList.setOnClickListener {
            val intent = Intent(requireContext(), ApplyListActivity::class.java)
            startActivity(intent)
        }

        binding.rlSaveJob.setOnClickListener {
            val intent = Intent(requireContext(), JobSaveActivity::class.java)
            startActivity(intent)
        }



        return  binding.root
    }

    private fun setUserData() {

            if (prefmanger.getInt(ROLE,0) == 0) {
                Log.d("###", "setUserData: ROLE")
                lifecycle.coroutineScope.launch {
                    jobSeekerProfileInfo.getUserProfileImg().collect {

                        Glide.with(requireContext())
                            .load(it)
                            .apply(
                                RequestOptions
                                    .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                                    .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            )
                            .into(binding.profileIv)
                    }
                }
                lifecycle.coroutineScope.launch {
                    jobSeekerProfileInfo.getUserFName().collect {

                        binding.userName.text = it

                    }
                }
                lifecycle.coroutineScope.launch {
                    jobSeekerProfileInfo.getUserLName().collect {
                        val fullName = "${binding.userName.text} $it"
                        binding.userName.text = fullName
                        Log.d("###", "setUserData: ROLE")

                    }
                }
                lifecycle.coroutineScope.launch {
                    jobSeekerProfileInfo.getUserCurrentCompany().collect {
                        binding.tvCompany.text = it
                    }
                }






            }else{
                lifecycle.coroutineScope.launch {
                    recruiterProfileInfo.getUserProfileImg().collect {
                        Log.d("###", "setUserData:getUserProfileImg $it")
//                        val imageUri: Uri? = if (it.isNotEmpty()) Uri.parse(it) else null
//                        binding.profileBackImg.setImageURI(imageUri)
                        Glide.with(requireContext())
                            .load(it)
                            .apply(
                                RequestOptions
                                    .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                                    .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            )
                            .into(binding.profileIv)
                    }
                }
                lifecycle.coroutineScope.launch {
                    recruiterProfileInfo.getUserFName().collect {
                        binding.userName.text = it

                    }
                }
                lifecycle.coroutineScope.launch {
                    recruiterProfileInfo.getUserLName().collect {
                        val fullName = "${binding.userName.text} $it"
                        binding.userName.text = fullName

                    }
                }

                lifecycle.coroutineScope.launch {

                    recruiterProfileInfo.getUserCurrentCompany().collect {
                        Log.d("###", "setUserData:getUserCurrentCompany $it")
                        binding.tvCompany.text = it
                    }
                }

            }







    }


}