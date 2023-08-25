package com.amri.emploihunt.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityUpdatePostBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.serializable
import com.amri.emploihunt.util.Utils.toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UpdatePostActivity : BaseActivity() {
    lateinit var binding: ActivityUpdatePostBinding
    private lateinit var selectedPost : Jobs
    private val PICK_IMAGE_REQUEST = 1
    lateinit var downloadUrl : String
    private  lateinit var prefManager: SharedPreferences
    lateinit var profilePicFile: File
    var cityList: ArrayList<String> = ArrayList()
    lateinit var  jobLocationAdapter: ArrayAdapter<String>
    var selectedJobLocation = String()
    var cityValidator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PrefManager.prefManager(this)

        getAllCity()
        selectedPost = intent.extras?.serializable("ARG_JOB_TITLE")!!

        binding.jobTitle.setText(selectedPost.vJobTitle)
        binding.currentCompany.setText(selectedPost.vCompanyName)
        binding.fileName.setText(selectedPost.tCompanyLogoUrl)
        Glide.with(this@UpdatePostActivity).load(selectedPost.tCompanyLogoUrl).into(binding.companyLogoIv)
        binding.descadd.setText(selectedPost.tDes)
        binding.jobroleadd.setText(selectedPost.vJobRoleResponsbility)
        binding.jobLevel.setText(selectedPost.vJobLevel)
        binding.experiencedDuration.setText(selectedPost.vExperience)
        binding.technicalSkills.setText(selectedPost.tTechnicalSkill)
        binding.softSkills.setText(selectedPost.tSoftSkill)
        binding.eduadd.setText(selectedPost.vEducation)
        binding.location.setText(selectedPost.vAddress)
        binding.salary.setText(selectedPost.vSalaryPackage)
        when(selectedPost.vWrokingMode){

            binding.radioBtnOnsitepost.text -> binding.radioBtnOnsitepost.isChecked = true
            binding.radioBtnRemotepost.text -> binding.radioBtnRemotepost.isChecked = true
            binding.radioBtnHybridpost.text -> binding.radioBtnHybridpost.isChecked = true
        }
        binding.noOfEmployeeNeed.setText(selectedPost.iNumberOfVacancy.toString())
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cityList)
        binding.location.setAdapter(adapter)
        binding.location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?, arg1: View?,
                arg2: Int, arg3: Long
            ) {
                binding.location.clearFocus()
                Log.d("###", "onItemSelected: ")
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {
                // TODO Auto-generated method stub
            }
        }
        binding.linearLayout2.setOnClickListener {
            uploadImage()
        }
        binding.location.validator = object : AutoCompleteTextView.Validator {
            override fun isValid(text: CharSequence): Boolean {
                Log.v("Test", "Checking if valid: $text ${cityList.contains(text.toString())}")

                if (cityList.contains(text.toString())) {
                    cityValidator = true
                    return true
                }
                cityValidator = false
                return false
            }

            override fun fixText(invalidText: CharSequence): CharSequence {
                // If .isValid() returns false then the code comes here
                // do whatever way you want to fix in the
                // users input and  return it
                binding.location.error = "Please select city in list"
                return ""
            }
        }
        binding.location.setOnFocusChangeListener { view, b ->
            if (view.id === R.id.location && !b) {
                (view as AutoCompleteTextView).performValidation()

            }
        }
        binding.btnUpdate.setOnClickListener { 
            if (checkValidation()){
                callUpdateJobPost()
            }
        }
        binding.ivBack.setOnClickListener {
            finish()
        }

        

    }
    private fun uploadImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent,PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                // Upload image and store URL
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "image_$timestamp.jpg"
                binding.fileName.text = fileName
                profilePicFile = File(Utils.getRealPathFromURI(this, imageUri).toString())
                Glide.with(this).load(imageUri).into(binding.companyLogoIv)
//                uploadImageAndStoreUrl(imageUri)
            }
        }
    }
    private fun callUpdateJobPost() {
        val title : String = binding.jobTitle.text.toString().trim()
        val compname : String = binding.currentCompany.text.toString().trim()
        val desc : String = binding.descadd.text.toString().trim()
        val jobLevel : String = binding.jobLevel.text.toString().trim()
        val role : String = binding.jobroleadd.text.toString().trim()
        val exp : String = binding.experiencedDuration.text.toString().trim()
        val techskill : String = binding.technicalSkills.text.toString().trim()
        val softskill : String = binding.softSkills.text.toString().trim()
        val edu : String = binding.eduadd.text.toString().trim()
        val city : String = binding.location.text.toString().trim()
        val workmodeid : Int = binding.textLayoutWorkingMode.checkedRadioButtonId
        lateinit var workmode : String
        when (workmodeid)
        {
            R.id.radioBtnOnsitepost -> workmode = resources.getString(R.string.on_site)
            R.id.radioBtnRemotepost -> workmode = resources.getString(R.string.remote)
            R.id.radioBtnHybridpost -> workmode = resources.getString(R.string.hybrid)
        }
        val sal : String = binding.salary.text.toString().trim()
        val empneed : String = binding.noOfEmployeeNeed.text.toString().trim()


        Log.d("##", "callUpdateJobPost: $empneed $city")
        if (Utils.isNetworkAvailable(this)){
            AndroidNetworking.upload(NetworkUtils.UPDATE_POST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("iJobId", selectedPost.id.toString())
                .addQueryParameter("vJobTitle",title)
                .addQueryParameter("vCompanyName",compname)
                .addQueryParameter("tDes",desc)
                .addQueryParameter("vJobLevel",jobLevel)
                .addQueryParameter("vExperience",exp)
                .addQueryParameter("tTechnicalSkill",techskill)
                .addQueryParameter("tSoftSkill",softskill)
                .addQueryParameter("vEducation",edu)
                .addQueryParameter("vAddress",city)
                .addQueryParameter("vSalaryPackage",sal)
                .addQueryParameter("iNumberOfVacancy",empneed)
                .addQueryParameter("vWrokingMode",workmode)
                .addQueryParameter("vJobRoleResponsbility",role)
                .addMultipartFile("tCompanyPic",profilePicFile)

                .setPriority(Priority.MEDIUM).build().getAsObject(
                    RegisterUserModel::class.java,
                    object : ParsedRequestListener<RegisterUserModel> {
                        override fun onResponse(response: RegisterUserModel?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    finish()
                                    toast("Post Updated Successfully")

                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                            }
                        }

                        override fun onError(anError: ANError?) {
                            hideProgressDialog()
                            anError?.let {
                                Log.e(
                                    "#####", "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                )


                            }
                        }
                    })
        }else{

            Utils.showNoInternetBottomSheet(this,this)
        }
    }

    private fun checkValidation(): Boolean {
        if (binding.jobTitle.text.toString().isBlank()){
            binding.jobTitle.requestFocus()
            binding.jobTitle.error = "Please enter job title"
            return  false

        }else if (binding.currentCompany.text.toString().isBlank()){
            binding.currentCompany.requestFocus()
            binding.currentCompany.error = "Please enter current company"
            return  false

        }else if (binding.fileName.text.toString().isBlank()){
            binding.fileName.requestFocus()
            binding.fileName.error = "Please upload your company logo"
            return  false

        }else if (binding.descadd.text.toString().isBlank()){
            binding.descadd.requestFocus()
            binding.descadd.error = "Please enter job description"
            return  false

        }else if (binding.jobroleadd.text.toString().isBlank()){
            binding.jobroleadd.requestFocus()
            binding.jobroleadd.error = "Please enter job role"
            return  false

        }else if (binding.jobLevel.text.toString().isBlank()){
            binding.jobLevel.requestFocus()
            binding.jobLevel.error = "Please enter job level"
            return  false

        }else if (binding.technicalSkills.text.toString().isBlank()){
            binding.technicalSkills.requestFocus()
            binding.technicalSkills.error = "Please enter technical skill"
            return  false

        }else if (binding.softSkills.text.toString().isBlank()){
            binding.softSkills.requestFocus()
            binding.softSkills.error = "Please enter soft skill"
            return  false

        }else if (binding.eduadd.text.toString().isBlank()){
            binding.eduadd.requestFocus()
            binding.eduadd.error = "Please enter education"
            return  false

        }else if (binding.location.text.toString().isNullOrBlank()){
            binding.location.requestFocus()
            binding.location.error = "Please select job location"
            return  false
        }else if (binding.salary.text.toString().isBlank()){
            binding.salary.requestFocus()
            binding.salary.error = "Please enter salary package"
            return  false

        }else if (binding.textLayoutWorkingMode.checkedRadioButtonId == -1){
            binding.textLayoutWorkingMode.requestFocus()
            toast("please choose working mode")
            return  false

        }else if (binding.noOfEmployeeNeed.text.toString().isBlank()){
            binding.noOfEmployeeNeed.requestFocus()
            binding.noOfEmployeeNeed.error = "Please enter a vacancy"
            return  false
        }else{
            return true
        }

    }
    private fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){

            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)



                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )


                            }


                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }

    }
}