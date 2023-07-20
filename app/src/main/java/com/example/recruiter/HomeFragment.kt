package com.example.recruiter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeFragment : Fragment() {
    lateinit var gridView: GridView
    lateinit var searchView: SearchView
    lateinit var voice: ImageView
    lateinit var fragview: View
    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var jobListAdapter: CustomAdapter
    private var userType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment


//        if(companyName.isEmpty()){
//            recyclerView.setVisibility(View.INVISIBLE);
//        }
        val bundle = arguments
        if (bundle != null) {
            userType = bundle.getString("userType")
        }
        fragview = inflater.inflate(R.layout.fragment_home, container, false)
        gridView = fragview.findViewById(R.id.gv)
        searchView = fragview.findViewById(R.id.search)
        voice = fragview.findViewById(R.id.voicesearch)
        database = FirebaseDatabase.getInstance().reference
        dataList = mutableListOf()
        jobListAdapter = CustomAdapter()
        gridView.adapter = jobListAdapter
        retrieveJobData()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                filterJobList(p0)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterJobList(query)
                return false
            }

        })
        return fragview

    }

    private fun filterJobList(query: String) {
        val filteredlist = mutableListOf<Jobs>()
        for (job in dataList) {
            if (TextUtils.isEmpty(query) || job.jobTile?.lowercase(Locale.ROOT)
                    ?.contains(query.lowercase(Locale.ROOT)) == true
            ) {
                dataList.add(job)
            }
        }
        jobListAdapter.notifyDataSetChanged()
    }

    private fun retrieveJobData() {
        val jobRef = database.child("Jobs")

        jobRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear()

                for (snapshot in dataSnapshot.children) {
                    val job: Jobs? = snapshot.getValue(Jobs::class.java)
                    job?.let {
                        dataList.add(job)
                    }
                }

                // Notify the adapter that the data has changed
                jobListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("MainActivity", "Failed to retrieve job data from Firebase: ${error.message}")
            }
        })
    }

    private inner class CustomAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return dataList.size
        }

        override fun getItem(position: Int): Any {
            return dataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myview = convertView
            if (myview == null) {
                myview = layoutInflater.inflate(R.layout.row_post_design, null)
            }
            val name: MaterialTextView = myview!!.findViewById(R.id.jobTitle)
            val sal: MaterialTextView = myview.findViewById(R.id.salary)
            val exp: MaterialTextView = myview.findViewById(R.id.experiencedDuration)
            val qual: MaterialTextView = myview.findViewById(R.id.qualification)
            val loc: TextView = myview.findViewById(R.id.city)
            val img: ImageView = myview.findViewById(R.id.profileImg)
            val about: MaterialTextView = myview.findViewById(R.id.aboutPost)
            val compname: MaterialTextView = myview.findViewById(R.id.companyName)
            val employess: MaterialTextView = myview.findViewById(R.id.employees)
            val cv: CardView = myview.findViewById(R.id.cardViewinfo)
            val job: Jobs = dataList[position]
            name.text = job.jobTile
            sal.text = job.salary + " LPA"
            exp.text = job.experienceDuration + " years"
            qual.text = job.education
            loc.text = job.jobLocation
            about.text = job.aboutPost
            compname.text = job.companyName
            employess.text = job.employeeNeed + " Employees"
            Glide.with(img.context).load(job.companyLogo).into(img)
            cv.setOnClickListener {
                val activity: AppCompatActivity = view?.context as AppCompatActivity
                val jobTitle = dataList[position]
                val jobPostDescriptionFragment = JobPostDescriptionFragment.newInstance(jobTitle)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, jobPostDescriptionFragment)
                    .addToBackStack(null)
                    .commit()
            }
        return myview
    }
}

private fun makePhoneCall(num: String) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
    startActivity(dialIntent)
}

private fun makeToast(msg: String, len: Int) {
    if (len == 0) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    if (len == 1) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
}
}
