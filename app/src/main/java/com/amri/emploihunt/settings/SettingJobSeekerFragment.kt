package com.amri.emploihunt.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.lifecycle.coroutineScope
import com.amri.emploihunt.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentSettingBinding
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.recruiterSide.HomeRecruitFragment
import com.amri.emploihunt.recruiterSide.HomeRecruiterActivity
import com.amri.emploihunt.store.JobSeekerProfileInfo
import com.amri.emploihunt.store.RecruiterProfileInfo
import com.amri.emploihunt.store.UserDataRepository
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.ROLE
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
        binding.rlContactUs.setOnClickListener {
            val intent = Intent(requireContext(), ContactUsActivity::class.java)
            startActivity(intent)
        }

        binding.rlPolicy.setOnClickListener {
            val intent = Intent(requireContext(), TermsPrivacyActivity::class.java)
            intent.putExtra("Privacy",true)
            startActivity(intent)
        }
        binding.rlterms.setOnClickListener {
            val intent = Intent(requireContext(), TermsPrivacyActivity::class.java)
            intent.putExtra("Privacy",false)
            startActivity(intent)
        }

        return  binding.root
    }

    private fun setUserData() {
        Log.d("###", "setUserData: ROLE")

        val  userDataRepository = UserDataRepository(requireContext())
        /** profile Img */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserProfileImgUrl().collect {
                Log.d(ProfileActivity.TAG, "setProfileData: trying to update profile img data $it")

                Glide.with(requireContext())
                    .load(it)
                    .apply(
                        RequestOptions
                            .placeholderOf(R.drawable.profile_default_image)
                            .error(R.drawable.profile_default_image)
                            .circleCrop()
                    )
                    .into(binding.profileIv)
            }
        }.invokeOnCompletion {
            Log.d(ProfileActivity.TAG, "setProfileData: profile img data is updated")
        }

        /** full Name */
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserFullName().collect{
                Log.d(ProfileActivity.TAG, "setProfileData: trying to update fullName data $it")
                binding.userName.text = it
            }
        }.invokeOnCompletion {
            Log.d(ProfileActivity.TAG, "setProfileData: Full name data is updated")
        }
        lifecycle.coroutineScope.launch {
            userDataRepository.getUserCurrentCompany().collect {
                Log.d(ProfileActivity.TAG, "setProfileData: trying to update current company data $it")
                binding.tvCompany.text = it
            }
        }.invokeOnCompletion {
            Log.d(ProfileActivity.TAG, "setProfileData: current Company: data is updated")
        }
    }


}