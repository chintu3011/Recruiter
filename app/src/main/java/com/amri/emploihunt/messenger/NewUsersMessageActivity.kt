package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityNewUsersMessageBinding
import com.amri.emploihunt.jobSeekerSide.UsersJobSeeker
import com.amri.emploihunt.recruiterSide.UsersRecruiter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class NewUsersMessageActivity : BaseActivity() {

    private lateinit var binding: ActivityNewUsersMessageBinding
    private var userType:String ?= null

    private lateinit var dataJobSeekerList: MutableList<Any>
    private lateinit var filterJobSeekerList: MutableList<Any>

    private lateinit var dataRecruiterList: MutableList<Any>
    private lateinit var filterRecruiterList: MutableList<Any>

//    private lateinit var jobSeekersAdapter: JobSeekersAdapter
//    private lateinit var recruitersAdapter: RecruitersAdapter
    private lateinit var newUserMessageAdapter : NewUserMessageAdapter

    companion object{
        private const val TAG = "NewUsersMessageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUsersMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@NewUsersMessageActivity.window
        window.statusBarColor = ContextCompat.getColor(this@NewUsersMessageActivity,android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Make New Chat"

        dataJobSeekerList = mutableListOf()
        filterJobSeekerList = mutableListOf()
        dataRecruiterList = mutableListOf()
        filterRecruiterList = mutableListOf()


        userType = intent.getStringExtra("userType")
        Log.d(TAG,userType.toString())

        userType?.let {
            if(it == "Job Seeker"){
                getUsersList("Recruiter"){taskComplete ->
                    if(taskComplete){
                        newUserMessageAdapter =  NewUserMessageAdapter(filterRecruiterList,this)
                        binding.recyclerView.adapter = newUserMessageAdapter
                    }
                }
            }
            if(it == "Recruiter"){
                getUsersList("Job Seeker"){taskComplete ->
                    if(taskComplete){
                        newUserMessageAdapter = NewUserMessageAdapter(filterJobSeekerList,this)
                        binding.recyclerView.adapter = newUserMessageAdapter
                    }

                }
            }

        }

//        binding.toggleGrp.addOnButtonCheckedListener{ toggleButtonGrp,checkedId, isChecked ->
//            if (isChecked){
//                when(checkedId){
//                    R.id.btnShowJobSeekers -> {
//                        userType = "Job Seeker"
//                        getUsersList()
//                        jobSeekersAdapter = JobSeekersAdapter()
//                        binding.gridView.adapter = jobSeekersAdapter
//
//                    }
//                    R.id.btnShowRecruiters -> {
//                        userType = "Recruiter"
//                        getUsersList()
//                        recruitersAdapter = RecruitersAdapter()
//                        binding.gridView.adapter = recruitersAdapter
//                    }
//                }
//            }
//            else {
//                if (toggleButtonGrp.checkedButtonId == View.NO_ID){
//                    userType = "Recruiter"
//                    getUsersList()
//                    recruitersAdapter = RecruitersAdapter()
//                    binding.gridView.adapter = recruitersAdapter
//
//                }
//            }
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterNormalUserList(query: String) {
        filterJobSeekerList.clear()

        if (!TextUtils.isEmpty(query)){
            for (user in dataJobSeekerList) {
                user as UsersJobSeeker
                if (user.userFName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filterJobSeekerList.add(user)
                }
            }
        }
        else{
            filterJobSeekerList.addAll(dataJobSeekerList)
        }


        newUserMessageAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterRecruiterList(query: String) {

        filterRecruiterList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in dataRecruiterList) {
                user as UsersRecruiter
                if (user.userFName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filterRecruiterList.add(user)
                }
            }
        }
        else{
            filterRecruiterList.addAll(dataRecruiterList)
        }

        newUserMessageAdapter.notifyDataSetChanged()
    }

    private fun getUsersList(userType: String, callBack:(Boolean) -> Unit) {

        FirebaseDatabase.getInstance().getReference("Users")
            .child(userType)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (userType == "Job Seeker"){

                        dataJobSeekerList.clear()
                        filterJobSeekerList.clear()
                        for (childSnapshot: DataSnapshot in snapshot.children) {
                            val user: UsersJobSeeker? = childSnapshot.getValue(UsersJobSeeker::class.java)

                            user?.let {
                                filterJobSeekerList.add(user)
                            }
                            Log.d("users Id", user?.userFName.toString())
                        }
                        dataJobSeekerList.addAll(filterJobSeekerList)
                        callBack(true)

                    }
                    else if(userType == "Recruiter"){

                        dataRecruiterList.clear()
                        filterRecruiterList.clear()
                        for (childSnapshot: DataSnapshot in snapshot.children) {
                            val user: UsersRecruiter? = childSnapshot.getValue(UsersRecruiter::class.java)
                            user?.let {
                                filterRecruiterList.add(user)
                            }
                            Log.d("users Id", childSnapshot.key.toString())
                        }
                        dataRecruiterList.addAll(filterRecruiterList)
                        callBack(true)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "NewUsersActivity",
                        "Failed to retrieve job data from Firebase: ${error.message}"
                    )
                }
            })
    }

/*    private inner class JobSeekersAdapter: BaseAdapter(){
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
            Log.d("userData", user.toString())

            val userFullName = user.userFName +" "+user.userLName
            Log.d("userName", userFullName)
            personName!!.text = userFullName
            tagLine!!.text = user.userTagLine
            if (user.userProfileImgUri.isNotEmpty()){
                Glide.with(profileImg!!.context).load(user.userProfileImgUri).into(profileImg)
            }

            cardView!!.setOnClickListener{

                val selectedUser = tempJobSeekerList[position]
                selectedUser.userProfileImg = ""
                selectedUser.userProfileBannerImg = ""
                val intent = Intent(this@NewUsersMessageActivity, ChatBoardActivity::class.java)
                intent.putExtra("userObjectJ", selectedUser)
                Log.d("NewUsersMessageActivity","selected user = $selectedUser")
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
            Log.d("userData", user.toString())

            val userFullName = user.userFName +" "+user.userLName
            Log.d("userName", userFullName)
            personName!!.text = userFullName
            tagLine!!.text = user.userTagLine
            if (user.userProfileImgUri.isNotEmpty()){
                Glide.with(profileImg!!.context).load(user.userProfileImgUri).into(profileImg)
            }

            cardView!!.setOnClickListener{
                val selectedUser = tempRecruiterList[position]
                selectedUser.userProfileImg = ""
                selectedUser.userProfileBannerImg = ""
                val intent = Intent(this@NewUsersMessageActivity, ChatBoardActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("userObjectR", selectedUser)
                Log.d("NewUsersMessageActivity","selected user = $selectedUser")
                startActivity(intent)
                finish()
            }
            return view
        }
    }*/
}