package com.amri.emploihunt

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentPostRecruitBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.RegisterUserModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.toast
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
    lateinit var profilePicFile: File
    var cityList: ArrayList<String> = ArrayList()
    var selectedJobLocation = String()
    lateinit var  jobLocationAdapter: ArrayAdapter<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostRecruitBinding.inflate(layoutInflater, container, false)
        prefManager = prefManager(requireActivity())
        cityList.add("Select Job Location")
        getAllCity()
        setAdapters()
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        binding.linearLayout2.setOnClickListener {
            uploadImage()
        }
        binding.btnpostjob.setOnClickListener {

            if (checkValidation()){
                adddata()
            }


        }
        binding.btnCancelPost.setOnClickListener {
            val homeFragment = HomeRecruitFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameRLayout,homeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.inputJobRSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                Log.d("###", "onItemSelected: ")
                selectedJobLocation = cityList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        return binding.root
    }



    private fun setAdapters() {

        jobLocationAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,cityList)
        jobLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputJobRSpinner.adapter = jobLocationAdapter



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
                profilePicFile = File(Utils.getRealPathFromURI(requireContext(), imageUri).toString())
                Glide.with(requireContext()).load(imageUri).into(binding.companyLogoIv)
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
            binding.uploadProgress.progress = progress
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

        }else if (selectedJobLocation.isNullOrBlank()){
            binding.inputJobRSpinner.requestFocus()
            toast("Please select job location")
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
        val jobLevel : String = binding.jobLevel.text.toString().trim()
        val role : String = binding.jobroleadd.text.toString().trim()
        val exp : String = binding.experiencedDuration.text.toString().trim()
        val techskill : String = binding.technicalSkills.text.toString().trim()
        val softskill : String = binding.softSkills.text.toString().trim()
        val edu : String = binding.eduadd.text.toString().trim()
        val city : String = selectedJobLocation.trim()
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
        val phone : Long = 9825154730
        val email = "info@amrisystems.com"
        val postduration = "02/04/2023"
        val jobapps = 20

        if (Utils.isNetworkAvailable(requireContext())){
            AndroidNetworking.upload(NetworkUtils.INSERT_POST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
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

                                    binding.jobTitle.text!!.clear()
                                    binding.currentCompany.text!!.clear()
                                    binding.descadd.text!!.clear()
                                    binding.jobLevel.text!!.clear()
                                    binding.jobroleadd.text!!.clear()
                                    binding.experiencedDuration.text!!.clear()
                                    binding.technicalSkills.text!!.clear()
                                    binding.softSkills.text!!.clear()
                                    binding.eduadd.text!!.clear()
                                    binding.inputJobRSpinner.setSelection(0)
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
    private fun getAllCity(){

        if (Utils.isNetworkAvailable(requireContext())){

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
            Utils.showNoInternetBottomSheet(requireContext(), requireActivity())
        }

    }
}