package com.example.recruiter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class JobPostDescriptionFragment() : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    lateinit var sv : ScrollView
    lateinit var binding : JobPostDescriptionFragment
    lateinit var selectedjob : Jobs
    lateinit var fragview : View
    companion object {
        private const val ARG_PRODUCT = "arg_product"

        fun newInstance(job: Jobs): JobPostDescriptionFragment {
            val fragment = JobPostDescriptionFragment()
            val args = Bundle()
//            args.putParcelable(ARG_PRODUCT, )
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragview = inflater.inflate(R.layout.fragment_job_post_description, container, false)
        database = FirebaseDatabase.getInstance().reference
        dataList = mutableListOf()
        retreivedescription()
        return fragview
    }

    private fun retreivedescription() {
        val jobRef = database.child("Jobs")

    }

}

