package com.example.recruiter

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
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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
            if (TextUtils.isEmpty(query) || job.Role?.lowercase(Locale.getDefault())
                    ?.contains(query.lowercase(Locale.getDefault())) == true
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

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myview = convertView
            if (myview == null) {
                myview = layoutInflater.inflate(R.layout.jobentry, null)
            }
            val name: TextView = myview!!.findViewById(R.id.jobname)
            val skill: TextView = myview.findViewById(R.id.jobskill)
            val loc: TextView = myview.findViewById(R.id.jobloc)
            val type: TextView = myview.findViewById(R.id.jobtype)
            val contact: TextView = myview.findViewById(R.id.contact)
            val compname: TextView = myview.findViewById(R.id.compname)
            val email: TextView = myview.findViewById(R.id.email)
            val job: Jobs = dataList[position]
            name.text = job.Role
            skill.text = job.Skills
            loc.text = job.Location
            type.text = job.Type
            compname.text = job.compname
            contact.text = job.phone
            email.text = job.email
            contact.setOnClickListener {
                val num: String = contact.text.toString()
                makePhoneCall(num)
            }
            email.setOnClickListener {
                val emailsend = email.text.toString()
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailsend))
                intent.type = "message/rfc822"
                startActivity(Intent.createChooser(intent, "Choose an Email Client: "))
            }
            return myview
        }
    }

    private fun makePhoneCall(num: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
        startActivity(dialIntent)
    }
}
