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
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentSettingRecruiterBinding
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SettingRecruiterFragment : BaseFragment() {



    private lateinit var prefmanger: SharedPreferences

    private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
    lateinit var binding: FragmentSettingRecruiterBinding
    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingRecruiterBinding.inflate(layoutInflater)
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
                (activity as HomeRecruiterActivity).bottomNavigationView[0]
            }

        }
        binding.rlPostList.setOnClickListener {
            val intent = Intent(requireContext(), YourJosPostListActivity::class.java)
            startActivity(intent)
        }

        binding.rlInterestedCandidate.setOnClickListener {
            val intent = Intent(requireContext(), InterestedCandidateActivity::class.java)
            startActivity(intent)
        }
        binding.ivLogout.setOnClickListener {
            logoutUser()
        }




        return  binding.root
    }
    private fun logoutUser() {
        showLogoutBottomSheet()
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
    fun showLogoutBottomSheet() {

        val dialog = BottomSheetDialog(requireContext())
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


            if (Utils.isNetworkAvailable(requireContext())) {
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
                                    Toast.makeText(requireContext(), response.data.msg, Toast.LENGTH_LONG).show()
                                    prefmanger.set(IS_LOGIN,false)
                                    val intent = Intent(requireContext(), LoginActivity::class.java)
                                    requireContext().startActivity(intent)
                                    activity!!.finish()
                                    activity?.overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                }else{
                                    Toast.makeText(requireContext(),getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                }






                            }

                            override fun onError(anError: ANError?) {
                                anError?.let {
                                    Log.e("#####", "onError: code: ${it.errorCode} & body: ${it.errorDetail}")
                                    Toast.makeText(requireContext(),getString(R.string.something_error),
                                        Toast.LENGTH_SHORT).show()
                                    hideProgressDialog()

                                }

                            }
                        })
            }else{
                Utils.showNoInternetBottomSheet(requireContext(), requireActivity())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("#message", "onResponse: "+e.message)
            hideProgressDialog()
            Toast.makeText(requireContext(),getString(R.string.something_error), Toast.LENGTH_SHORT).show()
        }

    }

}