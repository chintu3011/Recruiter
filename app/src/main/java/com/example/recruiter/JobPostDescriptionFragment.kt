package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recruiter.databinding.FragmentJobPostDescriptionBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JobPostDescriptionFragment() : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    lateinit var sv : ScrollView
    lateinit var binding : FragmentJobPostDescriptionBinding
    lateinit var fragview : View
    private var userType:String ?= null
    private var jobTitle: String = "";
    private lateinit var selectedPost : Jobs
    companion object {
        private const val ARG_JOB_TITLE = "ARG_JOB_TITLE"
        fun newInstance(jobPost: Jobs): JobPostDescriptionFragment {
            val args = Bundle()
            args.putParcelable(ARG_JOB_TITLE, jobPost)
            val fragment = JobPostDescriptionFragment()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedPost = it.getParcelable(ARG_JOB_TITLE)!!
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentJobPostDescriptionBinding.inflate(inflater, container, false)
        fragview = binding.root
        database = FirebaseDatabase.getInstance().getReference("Jobs")
        dataList = mutableListOf()
        retreivedescription()
        binding.btnCancel.setOnClickListener {
            val homeFragment = HomeFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frameLayout,homeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return fragview
    }

    private fun retreivedescription() {
        binding.jobTitle.text = selectedPost.jobTile
        binding.companyName.text = selectedPost.companyName
        binding.jobLocation.text = selectedPost.jobLocation
        binding.jobPostDuration.text = selectedPost.postDuration
        binding.applications.text = selectedPost.jobApplications.toString() + " Applications"
        binding.workingMode.text = selectedPost.workingmode
        binding.jobRoll.text = selectedPost.jobRoll
        binding.jobDes.setText(selectedPost.aboutPost)
        binding.technicalSkills.text = selectedPost.technicalSkills
        binding.softSkills.text = selectedPost.softSkills
        binding.experience.text = selectedPost.experienceDuration.toString() + " Years"
        binding.education.text = selectedPost.education
        val img: ShapeableImageView = fragview.findViewById(R.id.companyLogo)
        Glide.with(img.context).load(selectedPost.companyLogo).into(img)
        binding.btnApply.setOnClickListener {
            val email = selectedPost.email
            val sub = selectedPost.companyName
            val dialIntent = Intent(Intent.ACTION_SENDTO)
            dialIntent.putExtra(Intent.EXTRA_EMAIL, email)
            dialIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Reg. Job Application for " + sub
            );
            dialIntent.type = "message/rfc822"
            startActivity(
                Intent.createChooser(
                    dialIntent,
                    "Choose an email client:"
                )
            )
        }
    }

//    private fun retreivedescription() {
//        val jobRef = database.child("Jobs")
//        jobRef.orderByChild("jobTile").equalTo(jobTitle)
//            .addListenerForSingleValueEvent(object : ValueEventListener
//            {
//                @SuppressLint("SetTextI18n")
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (data in snapshot.children) {
//                            val jobData = data.getValue(Jobs::class.java)
//                            if (jobData != null) {
//                                val jobtitletv = jobData.jobTile
//                                val companynametv = jobData.companyName
//                                val loctv = jobData.jobLocation
//                                val durtv = jobData.postDuration
//                                val appstv = jobData.jobApplications
//                                val workmodetv = jobData.workingmode
//                                val jobroletv = jobData.jobRoll
//                                val jobdesctv = jobData.aboutPost
//                                val techskilltv = jobData.technicalSkills
//                                val softskilltv = jobData.softSkills
//                                val exptv = jobData.experienceDuration
//                                val edutv = jobData.education
//                                val img: ShapeableImageView = fragview.findViewById(R.id.companyLogo)
//                                binding.jobTitle.text = jobtitletv
//                                binding.companyName.text = companynametv
//                                binding.jobLocation.text = loctv
//                                binding.jobPostDuration.text = durtv
//                                binding.applications.text = appstv.toString() + " Applications"
//                                binding.workingMode.text = workmodetv
//                                binding.jobRoll.text = jobroletv
//                                binding.jobDes.setText(jobdesctv)
//                                binding.technicalSkills.text = techskilltv
//                                binding.softSkills.text = softskilltv
//                                binding.experience.text = exptv
//                                binding.education.text = edutv
//                                Glide.with(img.context).load(jobData.companyLogo).into(img)
//                                binding.btnApply.setOnClickListener {
//                                    val email = jobData.email
//                                    val sub = jobData.companyName
//                                    val dialIntent = Intent(Intent.ACTION_SENDTO)
//                                    dialIntent.putExtra(Intent.EXTRA_EMAIL, email)
//                                    dialIntent.putExtra(
//                                        Intent.EXTRA_SUBJECT,
//                                        "Reg. Job Application for " + sub
//                                    );
//                                    dialIntent.type = "message/rfc822"
//                                    startActivity(
//                                        Intent.createChooser(
//                                            dialIntent,
//                                            "Choose an email client:"
//                                        )
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })
//    }

}

