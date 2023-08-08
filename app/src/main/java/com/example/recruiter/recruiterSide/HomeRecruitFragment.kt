package com.example.recruiter.recruiterSide

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.example.recruiter.R
import com.example.recruiter.jobSeekerSide.UsersJobSeeker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeRecruitFragment : Fragment() {
    lateinit var gridView: GridView
    lateinit var searchView: SearchView
    lateinit var voice: ImageView
    lateinit var fragview: View
    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<UsersJobSeeker>
    private lateinit var filteredDataList: MutableList<UsersJobSeeker>
    private lateinit var JSListAdapter: CustomAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragview = inflater.inflate(R.layout.fragment_home_recruit, container, false)
        gridView = fragview.findViewById(R.id.gvR)
        searchView = fragview.findViewById(R.id.searchR)
        voice = fragview.findViewById(R.id.voicesearchR)
        database = FirebaseDatabase.getInstance().reference
        dataList = mutableListOf()
        JSListAdapter = CustomAdapter()
        filteredDataList = mutableListOf()
        gridView.adapter = JSListAdapter
        retreivejsdata()
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
        voice.setOnClickListener {
            openvoice();
        }
        return fragview
    }

    private fun openvoice() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent, 200)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://market.android.com/details?id=APP_PACKAGE_NAME")
            )
            startActivity(browserIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = matches!![0]
            if (matches.isNotEmpty()) {
                dataList.clear()
                if (!TextUtils.isEmpty(query)){
                    for (user in filteredDataList) {
                        if (user.userFName!!.lowercase(Locale.ROOT)
                                .contains(query.lowercase(Locale.ROOT))
                        ) {
                            dataList.add(user)
                        }
                    }
                }
                else{
                    dataList.addAll(filteredDataList)
                }

                JSListAdapter.notifyDataSetChanged()
            }
        } else {
            Toast.makeText(activity, "Try Again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun retreivejsdata() {
        val userRef = database.child("Users")
        val jobRef = userRef.child("Job Seeker")

        jobRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                filteredDataList.clear()
                dataList.clear()

                for (snapshot in dataSnapshot.children) {
                    val job: UsersJobSeeker? = snapshot.getValue(UsersJobSeeker::class.java)
                    job?.userProfileImg = ""
                    job?.userProfileBannerImg = ""
                    job?.let {
                        filteredDataList.add(job)
                    }
                }
                dataList.addAll(filteredDataList)

                // Notify the adapter that the data has changed
                JSListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("MainActivity", "Failed to retrieve job data from Firebase: ${error.message}")
            }
        })
    }

    private fun filterJobList(query: String) {
        dataList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in filteredDataList) {
                if (user.userFName!!.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    dataList.add(user)
                }
            }
        }
        else{
            dataList.addAll(filteredDataList)
        }

        JSListAdapter.notifyDataSetChanged()
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
                myview = layoutInflater.inflate(R.layout.singlerowjs, null)
            }
            val name: TextView = myview!!.findViewById(R.id.jsname)
            val skill: TextView = myview.findViewById(R.id.qualificationjs)
            val loc: TextView = myview.findViewById(R.id.citypref)
            val type: TextView = myview.findViewById(R.id.jsjobtype)
            val contact: TextView = myview.findViewById(R.id.jscontact)
            val jobrole: TextView = myview.findViewById(R.id.jobrole)
            val email: TextView = myview.findViewById(R.id.jsemail)
            val job: UsersJobSeeker = dataList[position]
            name.text = job.userFName + " " + job.userLName
            skill.text = job.userQualification
            loc.text = job.userPrefJobLocation
            type.text = job.userWorkingMode
            jobrole.text = job.userPerfJobTitle
            contact.text = job.userPhoneNumber
            email.text = job.userEmailId
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

        private fun makePhoneCall(num: String) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            startActivity(dialIntent)
        }
    }
}