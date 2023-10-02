package com.amri.emploihunt.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityUpdatePostBinding
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
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UpdatePostActivity : BaseActivity() {
    lateinit var binding: ActivityUpdatePostBinding
    private lateinit var selectedPost : Jobs
    private val PICK_IMAGE_REQUEST = 1
    lateinit var downloadUrl : String
    private  lateinit var prefManager: SharedPreferences
    private var companyLogoFile: File? =null
    /*var cityList: ArrayList<String> = ArrayList()*/
    lateinit var  jobLocationAdapter: ArrayAdapter<String>
    var selectedJobLocation = String()
    var selectedJobTitle = String()
    var selectedEducation = String()

    private lateinit var techSkillList:MutableList<String>
    private lateinit var softSkillList:MutableList<String>

    var cityValidator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PrefManager.prefManager(this)

        selectedPost = intent.extras?.serializable("ARG_JOB_TITLE")!!


        binding.spJobTitle.setSearchDialogGravity(Gravity.TOP)
        binding.spJobTitle.arrowPaddingRight = 19
        binding.spJobTitle.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spJobTitle.setSelection(resources.getStringArray(R.array.indian_designations).toList().indexOf(selectedPost.vJobTitle))
        binding.spJobTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spJobTitle.isOutlined = true
                selectedJobTitle = binding.spJobTitle.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }
        binding.currentCompany.setText(selectedPost.vCompanyName)
        binding.fileName.setText(selectedPost.tCompanyLogoUrl)


        GlobalScope.launch(Dispatchers.IO) {
            /*val imgURL = URL(NetworkUtils.STATIC_BASE_URL + response.user.profile_pic_url)*/
            val imgURL =
                URL(NetworkUtils.BASE_URL_MEDIA+selectedPost.tCompanyLogoUrl)
            val imgBitmap = getBitmapFromURL(imgURL)
            Glide.with(this@UpdatePostActivity)
                .load(imgURL)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.default_company_logo)
                        .error(R.drawable.default_company_logo)
                        .circleCrop()
                )
                .into(binding.companyLogoIv)
            setProfileImage(false, fileUri = null, imgBitmap)
        }

        binding.descadd.setText(selectedPost.tDes)
        binding.jobroleadd.setText(selectedPost.vJobRoleResponsbility)
        /*binding.jobLevel.setText(selectedPost.vJobLevel*/
        binding.experiencedDuration.setText(selectedPost.vExperience)

        techSkillList = selectedPost.tTechnicalSkill!!.split(" || ").toMutableList()

        binding.techSkillsChipGrp.removeAllViews()
        if(techSkillList.isNotEmpty()) {
            for (skill in techSkillList) {

                val chip = LayoutInflater.from(this)
                    .inflate(R.layout.single_chip_qualification, null) as Chip
                chip.text = skill

                binding.techSkillsChipGrp.addView(chip)

            }
        }
        binding.btnAddTechSkills.setOnClickListener{
            if(binding.technicalSkills.text.toString().trim().isEmpty()){
                binding.technicalSkills.error = "Please enter a skill"
                return@setOnClickListener
            }
            else{
                val chip = LayoutInflater.from(this).inflate(R.layout.single_chip_qualification,null) as Chip
                chip.text = binding.technicalSkills.text.toString().trim()
                techSkillList.add(binding.technicalSkills.text.toString().trim())
                binding.technicalSkills.setText("")
                binding.techSkillsChipGrp.addView(chip)
            }

        }
        softSkillList =  selectedPost.tSoftSkill!!.split(" || ").toMutableList()

        binding.softSkillsChipGrp.removeAllViews()
        if(softSkillList.isNotEmpty()) {
            for (skill in softSkillList) {

                val chip = LayoutInflater.from(this)
                    .inflate(R.layout.single_chip_qualification, null) as Chip
                chip.text = skill

                binding.softSkillsChipGrp.addView(chip)

            }
        }

        binding.btnAddSoftSkills.setOnClickListener{
            if(binding.softSkills.text.toString().trim().isEmpty())   {
                binding.softSkills.error = "Please enter a skill"
                return@setOnClickListener
            }
            else{
                val chip = LayoutInflater.from(this).inflate(R.layout.single_chip_qualification,null) as Chip
                chip.text = binding.softSkills.text.toString().trim()
                softSkillList.add(binding.softSkills.text.toString().trim())
                binding.softSkills.setText("")
                binding.softSkillsChipGrp.addView(chip)
            }

        }

        binding.spEducation.setSearchDialogGravity(Gravity.TOP)
        binding.spEducation.arrowPaddingRight = 19
        binding.spEducation.item = resources.getStringArray(R.array.degree_array).toList()
        binding.spEducation.setSelection(resources.getStringArray(R.array.degree_array).toList().indexOf(selectedPost.vEducation))
        binding.spEducation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spEducation.isOutlined = true
                selectedEducation = binding.spEducation.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }


        binding.salary.setText(selectedPost.vSalaryPackage)
        when(selectedPost.vWrokingMode){

            binding.radioBtnOnsitepost.text -> binding.radioBtnOnsitepost.isChecked = true
            binding.radioBtnRemotepost.text -> binding.radioBtnRemotepost.isChecked = true
            binding.radioBtnHybridpost.text -> binding.radioBtnHybridpost.isChecked = true
        }
        binding.noOfEmployeeNeed.setText(selectedPost.iNumberOfVacancy.toString())
        val cityList:ArrayList<String> = arrayListOf()
        getAllCity(cityList){
            binding.spJobLocation.setSearchDialogGravity(Gravity.TOP)
            binding.spJobLocation.arrowPaddingRight = 19
            binding.spJobLocation.item = cityList.toList()
            binding.spJobLocation.setSelection(cityList.toList().indexOf(selectedPost.vAddress))
            binding.spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                    binding.spJobLocation.isOutlined = true
                    selectedJobLocation = binding.spJobLocation.item[position].toString()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {

                }
            }
        }


        binding.linearLayout2.setOnClickListener {
            val deniedPermissions:MutableList<String> = isGrantedPermission()
            if(deniedPermissions.isEmpty()) {
                uploadImage()
            }
            else{
                requestPermissions(deniedPermissions){
                    if(it){
                        uploadImage()
                    }
                    else{
                        val snackbar = Snackbar
                            .make(
                                binding.root,
                                "Sorry! you are not register, Please register first.",
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
       /* binding.location.validator = object : AutoCompleteTextView.Validator {
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
        }*/
        binding.btnUpdate.setOnClickListener { 
            if (checkValidation()){
                callUpdateJobPost()
            }
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
    fun getBitmapFromURL(url:URL) : Bitmap?
    {
        return try {
            val connection:HttpURLConnection  =url . openConnection () as HttpURLConnection
            connection.setDoInput(true);
            connection.connect();
            val input:InputStream = connection . getInputStream ();
            BitmapFactory.decodeStream(input);
        } catch (e: IOException) {
            null
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

                setProfileImage(true,imageUri,null)

//                uploadImageAndStoreUrl(imageUri)
            }
        }
    }

    private fun setProfileImage(fromCamGallery: Boolean, fileUri: Uri?, imgBitmap: Bitmap?) {
        if (fromCamGallery) {
            fileUri?.let {
                val name = getFileName(this, uri = it)
                val extension = name?.let { it1 -> getExtension(it1) }

                compressImgForCompanyLogo(this@UpdatePostActivity,fileUri){file ->

                    Glide.with(this@UpdatePostActivity)
                        .load(file)
                        .apply(
                            RequestOptions
                                .placeholderOf(R.drawable.default_company_logo)
                                .error(R.drawable.default_company_logo)
                                .circleCrop()
                        )
                        .into(binding.companyLogoIv)
                    companyLogoFile = file
                }


                /*profile_pic = uriToFile(
                    this, fileUri,
                    "${System.currentTimeMillis()}.$extension"
                )
                isOnlineUrlNull = false*/
            }
        } else {
            if (imgBitmap == null) {
                //Log.e("URL", "setProfileImage imgBitmap is -------> NULL <-------")
                return
            }
            companyLogoFile = getFileFromBitmap(this, imgBitmap, "CompanyLogo")
        }
    }

    private fun getFileFromBitmap(context:Context, bitmap:Bitmap, folderName:String ): File  {
        
        val file:File  = File(context.cacheDir, "$folderName.png")
        try {
            file.createNewFile();

            val bos: ByteArrayOutputStream = ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            val bitmapData: ByteArray = bos.toByteArray()


            val fos:FileOutputStream  = FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (e:Exception ) {
            e.printStackTrace();
        }
        return file

        }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            cursor.close()
        }
        return name

    }

    private fun getExtension(name: String): String {
        return name.substring(name.lastIndexOf(".",1))
    }
    private fun callUpdateJobPost() {
        val title : String = selectedJobTitle
        val compname : String = binding.currentCompany.text.toString().trim()
        val desc : String = binding.descadd.text.toString().trim()
        /*val jobLevel : String = binding.jobLevel.text.toString().trim()*/
        val role : String = binding.jobroleadd.text.toString().trim()
        val exp : String = binding.experiencedDuration.text.toString().trim()
        val techskill : String = techSkillList.joinToString(" || ")
        val softskill : String = softSkillList.joinToString(" || ")
        val edu : String = selectedEducation
        val city : String = selectedJobLocation
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
                /*.addQueryParameter("vJobLevel",jobLevel)*/
                .addQueryParameter("vExperience",exp)
                .addQueryParameter("tTechnicalSkill",techskill)
                .addQueryParameter("tSoftSkill",softskill)
                .addQueryParameter("vEducation",edu)
                .addQueryParameter("vAddress",city)
                .addQueryParameter("vSalaryPackage",sal)
                .addQueryParameter("iNumberOfVacancy",empneed)
                .addQueryParameter("vWrokingMode",workmode)
                .addQueryParameter("vJobRoleResponsbility",role)
                .addMultipartFile("tCompanyPic",companyLogoFile)

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
        if (selectedJobTitle.isEmpty()){
            binding.spJobTitle.requestFocus()
            binding.spJobTitle.errorText = "Please enter job title"
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

        }/*else if (binding.jobLevel.text.toString().isBlank()){
            binding.jobLevel.requestFocus()
            binding.jobLevel.error = "Please enter job level"
            return  false

        }*/else if (techSkillList.isEmpty()){
            binding.technicalSkills.requestFocus()
            binding.technicalSkills.error = "Please enter technical skill"
            return  false

        }else if (softSkillList.isEmpty()){
            binding.softSkills.requestFocus()
            binding.softSkills.error = "Please enter soft skill"
            return  false

        }else if (selectedEducation.isEmpty()){
            binding.spEducation.requestFocus()
            binding.spEducation.errorText = "Please enter education"
            return  false

        }else if (selectedJobLocation.isEmpty()){
            binding.spJobLocation.requestFocus()
            binding.spJobLocation.errorText = "Please select job location"
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
}