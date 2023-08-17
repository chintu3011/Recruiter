package com.example.recruiter

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.coroutineScope
import com.airbnb.lottie.LottieAnimationView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.recruiter.basedata.BaseFragment
import com.example.recruiter.databinding.FragmentSettingBinding
import com.example.recruiter.model.LogoutMain
import com.example.recruiter.networking.NetworkUtils
import com.example.recruiter.store.JobSeekerProfileInfo
import com.example.recruiter.store.RecruiterProfileInfo
import com.example.recruiter.util.AUTH_TOKEN
import com.example.recruiter.util.IS_LOGIN
import com.example.recruiter.util.PrefManager
import com.example.recruiter.util.PrefManager.get
import com.example.recruiter.util.PrefManager.set
import com.example.recruiter.util.ROLE
import com.example.recruiter.util.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch


class SettingFragment : BaseFragment() {


    private lateinit var prefmanger: SharedPreferences
    private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
    lateinit var binding: FragmentSettingBinding
    private lateinit var jobSeekerProfileInfo: JobSeekerProfileInfo
    private lateinit var recruiterProfileInfo: RecruiterProfileInfo
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater)
        prefmanger = PrefManager.prefManager(requireContext())
        jobSeekerProfileInfo = JobSeekerProfileInfo(requireContext())
        recruiterProfileInfo = RecruiterProfileInfo(requireContext())
        setUserData()
        binding.rlProfile.setOnClickListener {

            val intent = Intent(requireContext(),ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener {
            if (prefmanger.getInt(ROLE,0) == 0) {
                val  fragment = HomeFragment()
                (activity as HomeJobActivity).replaceFragment(fragment)
            }else{
                val  fragment = HomeRecruitFragment()
                (activity as HomeRecruiterActivity).replaceFragment(fragment)
            }

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
                    jobSeekerProfileInfo.getUserFName().collect {

                        binding.userName.text = it

                    }
                    jobSeekerProfileInfo.getUserLName().collect {
                        val fullName = "${binding.userName.text} $it"
                        binding.userName.text = fullName

                    }
                    jobSeekerProfileInfo.getUserCurrentCompany().collect {
                        binding.tvCompany.text = it
                    }
                }


            }else{
                lifecycle.coroutineScope.launch {
                    recruiterProfileInfo.getUserProfileImg().collect {
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
                    recruiterProfileInfo.getUserFName().collect {
                        Log.d("###", "setUserData:1 $it")
                        binding.userName.text = it

                    }
                    recruiterProfileInfo.getUserFName().collect {
                        binding.userName.text = it

                    }
                    recruiterProfileInfo.getUserLName().collect {
                        val fullName = "${binding.userName.text} $it"
                        binding.userName.text = fullName

                    }
                    recruiterProfileInfo.getUserCurrentCompany().collect {
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
                                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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