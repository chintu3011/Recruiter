package com.example.recruiter

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
import com.example.recruiter.basedata.BaseFragment
import com.example.recruiter.databinding.FragmentPostRecruitBinding
import com.example.recruiter.model.GetAllCity
import com.example.recruiter.model.RegisterUserModel
import com.example.recruiter.networking.NetworkUtils
import com.example.recruiter.util.AUTH_TOKEN
import com.example.recruiter.util.PrefManager.get
import com.example.recruiter.util.PrefManager.prefManager
import com.example.recruiter.util.Utils
import com.example.recruiter.util.Utils.toast
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
        cityList.add("Last Graduation &amp; Degree")
        getAllCity()
        setAdapters()
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        binding.linearLayout2.setOnClickListener {
            uploadImage()
        }
        binding.btnpostjob.setOnClickListener {
            adddata()

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
    private fun adddata() {

        val title : String = binding.jobTitle.text.toString()
        val compname : String = binding.currentCompany.text.toString()
        val desc : String = binding.descadd.text.toString()
        val jobLevel : String = binding.jobLevel.text.toString()
        val role : String = binding.jobroleadd.text.toString()
        val exp : String = binding.experiencedDuration.text.toString()
        val techskill : String = binding.technicalSkills.text.toString()
        val softskill : String = binding.softSkills.text.toString()
        val edu : String = binding.eduadd.text.toString()
        val city : String = selectedJobLocation
        val workmodeid : Int = binding.textLayoutWorkingMode.checkedRadioButtonId
        lateinit var workmode : String
        when (workmodeid)
        {
            R.id.radioBtnOnsitepost -> workmode = "On-site"
            R.id.radioBtnRemotepost -> workmode = "Remote"
            R.id.radioBtnHybridpost -> workmode = "Hybrid"
        }
        val sal : String = binding.salary.text.toString()
        val empneed : String = binding.noOfEmployeeNeed.text.toString()
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