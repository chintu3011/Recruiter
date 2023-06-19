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
    private lateinit var jobTitle: String
    companion object {
        private const val ARG_JOB_TITLE = "job_title"
        fun newInstance(jobTitle: String): JobPostDescriptionFragment {
            val fragment = JobPostDescriptionFragment()
            val args = Bundle()
            args.putString(ARG_JOB_TITLE, jobTitle)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentJobPostDescriptionBinding.inflate(inflater, container, false)
        fragview = binding.root
        database = FirebaseDatabase.getInstance().reference
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
        val jobRef = database.child("Jobs")
        jobRef.orderByChild("jobTile").equalTo(jobTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener
            {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val jobData = snapshot.children.first()
                        val jobtitletv = jobData.child("jobTile").getValue(String::class.java)
                        val companynametv = jobData.child("companyName").getValue(String::class.java)
                        val loctv = jobData.child("jobLocation").getValue(String::class.java)
                        val durtv = jobData.child("postDuration").getValue(String::class.java)
                        val appstv = jobData.child("jobApplications").getValue(Long::class.java)
                        val workmodetv = jobData.child("workingmode").getValue(String::class.java)
                        val jobroletv = jobData.child("jobRoll").getValue(String::class.java)
                        val jobdesctv = jobData.child("aboutPost").getValue(String::class.java)
                        val techskilltv = jobData.child("technicalSkills").getValue(String::class.java)
                        val softskilltv = jobData.child("softSkills").getValue(String::class.java)
                        val exptv = jobData.child("experienceDuration").getValue(String::class.java)
                        val edutv = jobData.child("education").getValue(String::class.java)
                        val img : ShapeableImageView = fragview.findViewById(R.id.companyLogo)
                        binding.jobTitle.text = jobtitletv
                        binding.companyName.text = companynametv
                        binding.jobLocation.text = loctv
                        binding.jobPostDuration.text = durtv
                        binding.applications.text = appstv.toString() + " Applications"
                        binding.workingMode.text = workmodetv
                        binding.jobRoll.text = jobroletv
                        binding.jobDes.setText(jobdesctv)
                        binding.technicalSkills.text = techskilltv
                        binding.softSkills.text = softskilltv
                        binding.experience.text = exptv
                        binding.education.text = edutv
                        Glide.with(img.context).load(jobData.child("companyLogo")).into(img)
                        binding.btnApply.setOnClickListener {
                            val email = jobData.child("email").getValue(String::class.java)
                            val sub = jobData.child("companyName").getValue(String::class.java)
                            val dialIntent = Intent(Intent.ACTION_SENDTO)
                            dialIntent.putExtra(Intent.EXTRA_EMAIL, email)
                            dialIntent.putExtra(Intent.EXTRA_SUBJECT, "Reg. Job Application for "+sub);
                            dialIntent.type = "message/rfc822"
                            startActivity(Intent.createChooser(dialIntent,"Choose an email client:"))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}

