package com.example.recruiter

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.recruiter.databinding.FragmentPostRecruitBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class PostRecruitFragment : Fragment() {
    lateinit var binding : FragmentPostRecruitBinding
    lateinit var databaseReference: DatabaseReference
    lateinit var storage: StorageReference
    private val PICK_IMAGE_REQUEST = 1
    lateinit var downloadUrl : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostRecruitBinding.inflate(layoutInflater, container, false)
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference
        binding.linearLayout2.setOnClickListener {
            uploadImage()
        }
        binding.btnpostjob.setOnClickListener {
            adddata()
            val homeFragment = HomeRecruitFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameRLayout,homeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
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
                uploadImageAndStoreUrl(imageUri)
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
        val role : String = binding.jobroleadd.text.toString()
        val exp : String = binding.experiencedDuration.text.toString()
        val techskill : String = binding.technicalSkills.text.toString()
        val softskill : String = binding.softSkills.text.toString()
        val edu : String = binding.eduadd.text.toString()
        val city : String = binding.cityadd.text.toString()
        val sal : String = binding.salary.text.toString()
        val workmode : String = binding.workingMode.text.toString()
        val empneed : String = binding.noOfEmployeeNeed.text.toString()
        val phone : Long = 9825154730
        val email = "info@amrisystems.com"
        val postduration = "02/04/2023"
        val jobapps = 20
        val jobref = databaseReference.child("Jobs")
        val key = jobref.push().key
        val jobs = Jobs(title,desc,compname,edu,email,empneed,exp,jobapps,city,role,
        phone,postduration,sal,softskill,techskill,workmode,downloadUrl)
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
        }
    }
}