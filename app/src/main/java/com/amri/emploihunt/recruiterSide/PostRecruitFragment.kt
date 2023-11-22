package com.amri.emploihunt.recruiterSide

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentPostRecruitBinding
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.toast
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class PostRecruitFragment : BaseFragment() {
    lateinit var binding : FragmentPostRecruitBinding
    lateinit var databaseReference: DatabaseReference
    lateinit var storage: StorageReference
    private val PICK_IMAGE_REQUEST = 1
    lateinit var downloadUrl : String
    private  lateinit var prefManager: SharedPreferences
    private var companyLogoFile: File? = null
    /*var cityList: ArrayList<String> = ArrayList()*/
    var selectedJobLocation = String()
    var selectedJobTitle = String()
    var selectedEducation = String()
    lateinit var  jobLocationAdapter: ArrayAdapter<String>

    private lateinit var techSkillList:MutableList<String>
    private lateinit var softSkillList:MutableList<String>
    var cityValidator = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostRecruitBinding.inflate(layoutInflater, container, false)
        prefManager = prefManager(requireActivity())


        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line,resources.getStringArray(R.array.indian_designations).toList())
        binding.jobTitle.setAdapter(adapter)

        /*binding.spJobTitle.setSearchDialogGravity(Gravity.TOP)
        binding.spJobTitle.arrowPaddingRight = 19
        binding.spJobTitle.item = resources.getStringArray(R.array.indian_designations).toList()
        binding.spJobTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                binding.spJobTitle.isOutlined = true
                selectedJobTitle = binding.spJobTitle.item[position].toString()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }*/
        val adapter1: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line,resources.getStringArray(R.array.degree_array).toList())
        binding.education.setAdapter(adapter1)


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

        techSkillList = mutableListOf()
        binding.btnAddTechSkills.setOnClickListener{
            if(binding.technicalSkills.text.toString().trim().isNotEmpty()) {
                val chip = LayoutInflater.from(requireContext())
                    .inflate(R.layout.single_chip_qualification, null) as Chip
                chip.text = binding.technicalSkills.text.toString().trim()
                techSkillList.add(binding.technicalSkills.text.toString().trim())
                binding.technicalSkills.setText("")
                binding.techSkillsChipGrp.addView(chip)
            }
        }
        softSkillList = mutableListOf()
        binding.btnAddSoftSkills.setOnClickListener{
            if(binding.softSkills.text.toString().trim().isNotEmpty()) {
                val chip = LayoutInflater.from(requireContext())
                    .inflate(R.layout.single_chip_qualification, null) as Chip
                chip.text = binding.softSkills.text.toString().trim()
                softSkillList.add(binding.softSkills.text.toString().trim())
                binding.softSkills.setText("")
                binding.softSkillsChipGrp.addView(chip)
            }
        }
        binding.btnpostjob.setOnClickListener {
            showProgressDialog("Please wait")
            if (checkValidation()){
                adddata()
            }
            else{
               hideProgressDialog()
            }
        }
        binding.btnCancelPost.setOnClickListener {
            val homeFragment = HomeRecruitFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout,homeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }


        val cityList:ArrayList<String> = arrayListOf()
        getAllCity(cityList){
            if (cityList.isNotEmpty()){
                binding.spJobLocation.setSearchDialogGravity(Gravity.TOP)
                binding.spJobLocation.arrowPaddingRight = 19
                binding.spJobLocation.item = cityList.toList()
                binding.spJobLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                        binding.spJobLocation.isOutlined = true
                        selectedJobLocation = binding.spJobLocation.item[position].toString()
                    }

                    override fun onNothingSelected(adapterView: AdapterView<*>?) {

                    }
                }
            }
            else{
                makeToast(getString(R.string.something_error),0)
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

        /*binding.location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
       }*/
        /*binding.location.validator = object : AutoCompleteTextView.Validator {
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
        return binding.root
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
                compressImgForCompanyLogo(requireContext(),imageUri){
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "image_$timestamp.jpg"
                    binding.fileName.text = fileName
                    companyLogoFile = it
                    Glide.with(requireContext())
                        .load(imageUri)
                        .apply(
                            RequestOptions
                                .placeholderOf(R.drawable.default_company_logo)
                                .error(R.drawable.default_company_logo)
                                .circleCrop()
                        )
                        .into(binding.companyLogoIv)
                }

//                uploadImageAndStoreUrl(imageUri)
            }
        }
    }

    private fun uploadImageAndStoreUrl(imageUri: Uri) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "image_$timestamp.jpg"
        val imagesRef = storage.child("logos/$fileName")
        val uploadTask = imagesRef.putFile(imageUri)
        uploadTask.addOnProgressListener {snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred/snapshot.totalByteCount).toInt()
            /*binding.uploadProgress.progress = progress*/
        }
        uploadTask
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imagesRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUrl = task.result.toString()
                    binding.fileName.text = fileName
                } else {
                    // Error occurred during image upload
                    // Handle the error
                }
            }
    }
    private fun checkValidation(): Boolean {
        if (binding.jobTitle.text.isEmpty()){
            binding.jobTitle.requestFocus()
            binding.jobTitle.error = "Please enter job title"
            return  false

        }else if (binding.currentCompany.text.toString().isBlank()){
            binding.currentCompany.requestFocus()
            binding.currentCompany.error = "Please enter current company"
            return  false

        }else if (companyLogoFile == null){
            binding.companyLogoIv.requestFocus()
            makeToast("Please upload your company logo",0)
            return false
        }
        else if (binding.fileName.text.toString().isBlank()){
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

        }else if (binding.education.text.isEmpty()){
            binding.education.requestFocus()
            binding.education.error = "Please enter education"
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
    private fun adddata() {

        val title : String = binding.jobTitle.text.toString().trim()
        val compname : String = binding.currentCompany.text.toString().trim()
        val desc : String = binding.descadd.text.toString().trim()
        /*val jobLevel : String = binding.jobLevel.text.toString().trim()*/
        val role : String = binding.jobroleadd.text.toString().trim()
        val exp : String = binding.experiencedDuration.text.toString().trim()

        val techskill : String = techSkillList.joinToString(" || ")
        val softskill : String = softSkillList.joinToString(" || ")
        val edu : String = binding.education.text.toString().trim()
        val city : String = selectedJobLocation
        val workmodeid : Int = binding.textLayoutWorkingMode.checkedRadioButtonId
        lateinit var workmode : String
        when (workmodeid)
        {
            R.id.radioBtnOnsitepost -> workmode = requireContext().resources.getString(R.string.on_site)
            R.id.radioBtnRemotepost -> workmode = requireContext().resources.getString(R.string.remote)
            R.id.radioBtnHybridpost -> workmode = requireContext().resources.getString(R.string.hybrid)
        }
        val sal : String = binding.salary.text.toString().trim()
        val empneed : String = binding.noOfEmployeeNeed.text.toString().trim()
        /*val phone : Long = 9825556830
        val email = "info@amrisystems.com"
        val postduration = "02/04/2023"
        val jobapps = 20*/


        if (Utils.isNetworkAvailable(requireContext())){
            AndroidNetworking.upload(NetworkUtils.INSERT_POST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("vJobTitle",title)
                .addQueryParameter("vCompanyName",compname)
                .addQueryParameter("tDes",desc)
                .addQueryParameter("vJobLevel","")
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


                                    /*binding.spJobTitle.clearSelection()*/
                                    binding.jobTitle.text.clear()
                                    binding.currentCompany.text!!.clear()
                                    binding.descadd.text!!.clear()
                                    /*binding.jobLevel.text!!.clear()*/
                                    binding.jobroleadd.text!!.clear()
                                    binding.experiencedDuration.text!!.clear()
                                    binding.technicalSkills.text!!.clear()
                                    binding.techSkillsChipGrp.removeAllViews()
                                    binding.softSkills.text!!.clear()
                                    binding.softSkillsChipGrp.removeAllViews()
                                    binding.education.text.clear()
                                    binding.spJobLocation.clearSelection()
                                    binding.textLayoutWorkingMode.clearCheck()
                                    binding.salary.text!!.clear()
                                    binding.noOfEmployeeNeed.text!!.clear()
                                    binding.fileName.text = "File_Name.jpg"
                                    binding.companyLogoIv.setImageResource(R.drawable.ic_upload)
                                    toast("Post Uploaded Successfully")

                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                            }
                            finally {
                                hideProgressDialog()
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
            Utils.showNoInternetBottomSheet(requireContext(),requireActivity())
            hideProgressDialog()
        }


        /*val jobref = databaseReference.child("Jobs")
        val key = jobref.push().key
        val jobs = Jobs()
        if (key != null) {
            jobref.child(key).setValue(jobs)
                .addOnSuccessListener {
                    val channelId = "MyChannelId"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "My Channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        channel.enableLights(true)
                        channel.lightColor = Color.GREEN
                        channel.enableVibration(true)
                        getSystemService(
                            requireActivity(),
                            NotificationManager::class.java
                        )?.createNotificationChannel(channel)
                        val notificationBuilder = NotificationCompat.Builder(requireActivity(), channelId)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("Post Uploaded")
                            .setContentText("Congratulations! Your post has been uploaded")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)

                        with(NotificationManagerCompat.from(requireActivity())) {
                            if (ActivityCompat.checkSelfPermission(
                                    requireActivity(),
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                            }
                            notify(0, notificationBuilder.build())
                        }
                    }
                }
        }*/
    }
}