package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.recruiter.databinding.ActivityJobPostBinding
import com.example.recruiter.util.Utils.serializable
import com.google.android.material.imageview.ShapeableImageView

class JobPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobPostBinding
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var selectedPost : Jobs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()

        selectedPost = intent.extras?.serializable("ARG_JOB_TITLE")!!
        retreivedescription()
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
        Glide.with(this@JobPostActivity).load(selectedPost.tCompanyLogoUrl).into(binding.companyLogo)
        binding.btnApply.setOnClickListener {
            val email = "test"
            val sub = selectedPost.vCompanyName
            val dialIntent = Intent(Intent.ACTION_SEND)
            dialIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            dialIntent.putExtra(Intent.EXTRA_SUBJECT, "Reg. Job Application for $sub")
            dialIntent.type = "message/rfc822"
            startActivity(
                Intent.createChooser(dialIntent, "Choose an email client:")
            )
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
    }
}