package com.amri.emploihunt

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.amri.emploihunt.databinding.FragmentJobPostDescriptionBinding
import com.amri.emploihunt.model.Jobs
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
      /*  fun newInstance(jobPost: Jobs): JobPostDescriptionFragment {
            val args = Bundle()
            args.putParcelable(ARG_JOB_TITLE, jobPost)
            val fragment = JobPostDescriptionFragment()
            fragment.arguments = args
            return fragment
        }*/
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
        binding.jobTitle.text = selectedPost.vJobTitle
        binding.companyName.text = selectedPost.vCompanyName
        binding.jobLocation.text = selectedPost.vAddress
        binding.jobPostDuration.text = selectedPost.tCreatedAt
        binding.applications.text = selectedPost.iNumberOfApplied.toString() + " Applications"
        binding.workingMode.text = selectedPost.vWrokingMode
        binding.jobRoll.text = selectedPost.vJobRoleResponsbility
        binding.jobDes.setText(selectedPost.tDes)
        binding.technicalSkills.text = selectedPost.tTechnicalSkill
        binding.softSkills.text = selectedPost.tSoftSkill
        binding.experience.text = selectedPost.vExperience.toString() + " Years"
        binding.education.text = selectedPost.vEducation
        val img: ShapeableImageView = fragview.findViewById(R.id.companyLogo)
        Glide.with(img.context).load(selectedPost.tCompanyLogoUrl).into(img)
        binding.btnApply.setOnClickListener {
            val email = "test"
            val sub = selectedPost.vCompanyName
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

