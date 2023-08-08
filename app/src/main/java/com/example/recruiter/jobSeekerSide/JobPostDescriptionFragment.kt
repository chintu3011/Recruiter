package com.example.recruiter.jobSeekerSide

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.recruiter.recruiterSide.Jobs
import com.example.recruiter.R
import com.example.recruiter.databinding.FragmentJobPostDescriptionBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val posted = dateFormat.parse(selectedPost.postDuration) ?: Date()
        val diffMil = currentDate.time - posted.time
        val diff = diffMil/(1000*60*60*24)
        binding.jobTitle.text = selectedPost.jobTile
        binding.companyName.text = selectedPost.companyName
        binding.jobLocation.text = selectedPost.jobLocation
        binding.jobPostDuration.text = diff.toString() + " Days ago"
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
            val dialIntent = Intent(Intent.ACTION_SEND)
            dialIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            dialIntent.putExtra(Intent.EXTRA_SUBJECT, "Reg. Job Application for " + sub)
            dialIntent.type = "message/rfc822"
            startActivity(
                Intent.createChooser(dialIntent, "Choose an email client:")
            )
        }
    }
}

