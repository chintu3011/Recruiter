package com.example.recruiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.bumptech.glide.Glide

import com.example.recruiter.databinding.ActivityNewUsersMessageBinding
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class NewUsersMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewUsersMessageBinding
    private var userType:String ?= null

    private lateinit var dataJobSeekerList: MutableList<UsersJobSeeker>
    private lateinit var tempJobSeekerList: MutableList<UsersJobSeeker>

    private lateinit var dataRecruiterList: MutableList<UsersRecruiter>
    private lateinit var tempRecruiterList: MutableList<UsersRecruiter>

    private lateinit var jobSeekersAdapter: JobSeekersAdapter
    private lateinit var recruitersAdapter: RecruitersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUsersMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataJobSeekerList = mutableListOf()
        tempJobSeekerList = mutableListOf()
        dataRecruiterList = mutableListOf()
        tempRecruiterList = mutableListOf()
//        userType = "Job Seeker"
        binding.toggleGrp.addOnButtonCheckedListener{ toggleButtonGrp,checkedId, isChecked ->
            if (isChecked){
                when(checkedId){
                    R.id.btnShowJobSeekers -> {
                        userType = "Job Seeker"
                        getUsersList()
                        jobSeekersAdapter = JobSeekersAdapter()
                        binding.gridView.adapter = jobSeekersAdapter

                    }
                    R.id.btnShowRecruiters -> {
                        userType = "Recruiter"
                        getUsersList()
                        recruitersAdapter = RecruitersAdapter()
                        binding.gridView.adapter = recruitersAdapter
                    }
                }
            }
            else {
                if (toggleButtonGrp.checkedButtonId == View.NO_ID){
                    userType = "Recruiter"
                    getUsersList()
                    recruitersAdapter = RecruitersAdapter()
                    binding.gridView.adapter = recruitersAdapter

                }
            }
        }


    }

    override fun onStart() {
        super.onStart()
        if(binding.searchView.isNotEmpty()){
            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String): Boolean {

//                if(userType == "Job Seeker") filterNormalUserList(p0)
//                if(userType == "Recruiter")  filterRecruiterList(p0)
                    return false
                }

                override fun onQueryTextChange(query: String): Boolean {

                    if(userType == "Job Seeker") filterNormalUserList(query)
                    if(userType == "Recruiter")  filterRecruiterList(query)
                    return false
                }
            })
        }

    }

    private fun filterNormalUserList(query: String) {
        tempJobSeekerList.clear()

        if (!TextUtils.isEmpty(query)){
            for (user in dataJobSeekerList) {
                if (user.userFName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    tempJobSeekerList.add(user)
                }
            }
        }
        else{
            tempJobSeekerList.addAll(dataJobSeekerList)
        }


        jobSeekersAdapter.notifyDataSetChanged()
    }

    private fun filterRecruiterList(query: String) {

        tempRecruiterList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in dataRecruiterList) {
                if (user.userFName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    tempRecruiterList.add(user)
                }
            }
        }
        else{
            tempRecruiterList.addAll(dataRecruiterList)
        }

        recruitersAdapter.notifyDataSetChanged()
    }

    private fun getUsersList() {
//        makeToast(userType.toString(),0)
        userType?.let {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(it)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (userType == "Job Seeker"){
                            dataJobSeekerList.clear()
                            tempJobSeekerList.clear()
                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val user: UsersJobSeeker? = childSnapshot.getValue(UsersJobSeeker::class.java)

                                user?.let {
                                    dataJobSeekerList.add(user)
                                }
                                Log.d("users Id", user?.userFName.toString())
                            }
//                            tempJobSeekerList.clear()
                            tempJobSeekerList.addAll(dataJobSeekerList)
                        }
                        if(userType == "Recruiter"){
                            dataRecruiterList.clear()
                            tempRecruiterList.clear()
                            for (childSnapshot: DataSnapshot in snapshot.children) {
                                val user: UsersRecruiter? = childSnapshot.getValue(UsersRecruiter::class.java)
                                user?.let {
                                    dataRecruiterList.add(user)
                                }
                                Log.d("users Id", childSnapshot.key.toString())
                            }
//                            tempRecruiterList.clear()
                            tempRecruiterList.addAll(dataRecruiterList)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("NewUsersActivity", "Failed to retrieve job data from Firebase: ${error.message}")
                    }
                })
        }
    }

    private inner class JobSeekersAdapter: BaseAdapter(){
        override fun getCount(): Int {
            return tempJobSeekerList.size
        }

        override fun getItem(position: Int): Any {
            return tempJobSeekerList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            if (view == null){
                view = layoutInflater.inflate(R.layout.row_new_chat,null)
            }

            val personName = view?.findViewById<MaterialTextView>(R.id.personName)
            val tagLine = view?.findViewById<TextView>(R.id.tagLine)
            val profileImg = view?.findViewById<ImageView>(R.id.profileImg)
            val cardView = view?.findViewById<CardView>(R.id.cardView)
            val user: UsersJobSeeker = tempJobSeekerList[position]
            Log.d("userData",user.toString())

            val userFullName = user.userFName +" "+user.userLName
            Log.d("userName",userFullName)
            personName!!.text = userFullName
            tagLine!!.text = user.userTagLine
            if (user.userProfileImg.isNotEmpty()){
                Glide.with(profileImg!!.context).load(user.userProfileImg).into(profileImg)
            }

            cardView!!.setOnClickListener{
                val activity : AppCompatActivity = view?.context as AppCompatActivity
                val selectedUser = tempJobSeekerList[position]
                val intent = Intent(this@NewUsersMessageActivity, ChatBoardActivity::class.java)
                intent.putExtra("userObjectJ", user)
                startActivity(intent)
            }
            return view

        }
    }
    private inner class RecruitersAdapter: BaseAdapter(){
        override fun getCount(): Int {
            return tempRecruiterList.size
        }

        override fun getItem(position: Int): Any {
            return tempRecruiterList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var view = convertView
            if (view == null){
                view = layoutInflater.inflate(R.layout.row_new_chat,null)
            }

            val personName = view?.findViewById<MaterialTextView>(R.id.personName)
            val tagLine = view?.findViewById<TextView>(R.id.tagLine)
            val profileImg = view?.findViewById<ImageView>(R.id.profileImg)
            val cardView = view?.findViewById<CardView>(R.id.cardView)
            val user: UsersRecruiter = tempRecruiterList[position]
            Log.d("userData",user.toString())

            val userFullName = user.userFName +" "+user.userLName
            Log.d("userName",userFullName)
            personName!!.text = userFullName
            tagLine!!.text = user.userTagLine.toString()
            if (user.userProfileImg.isNotEmpty()){
                Glide.with(profileImg!!.context).load(user.userProfileImg).into(profileImg)
            }

            cardView!!.setOnClickListener{
                val selectedUser = tempRecruiterList[position]
                val intent = Intent(this@NewUsersMessageActivity, ChatBoardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("userObjectR", user)
                startActivity(intent)
                finish()
            }
            return view
        }
    }
    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}



