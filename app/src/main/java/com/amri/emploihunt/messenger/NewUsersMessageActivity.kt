package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityNewUsersMessageBinding
import com.amri.emploihunt.jobSeekerSide.UsersJobSeeker
import com.amri.emploihunt.model.GetAllUsers
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.UsersRecruiter
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.JOB_SEEKER
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.RECRUITER
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class NewUsersMessageActivity : BaseActivity() {

    private lateinit var binding: ActivityNewUsersMessageBinding

    private var userType:Int ?= null
    private var userId:String ?= null

    private lateinit var dataJobSeekerList: MutableList<Any>
    private lateinit var filterJobSeekerList: MutableList<Any>

    private lateinit var dataRecruiterList: MutableList<Any>
    private lateinit var filterRecruiterList: MutableList<Any>

    private lateinit var userList : MutableList<User>
    private lateinit var filterUserList : MutableList<User>

    lateinit var prefManager: SharedPreferences

    private lateinit var layoutManager: LinearLayoutManager

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1


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

        userList = mutableListOf()
        filterUserList = mutableListOf()

        /*userType = intent.getStringExtra("userType")*/
        prefManager = PrefManager.prefManager(this)

        layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.layoutManager = layoutManager

        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)

        Log.d(TAG,"$userId :: $userType")

        userType?.let { userType ->
            getUsersList(userType){
                if (it){
                    newUserMessageAdapter  = NewUserMessageAdapter(filterUserList,this)
                    binding.recyclerView.adapter = newUserMessageAdapter
                }
                else{
                    makeToast(getString(R.string.something_error),0)
                }

            }
        }
        
        /*userType?.let {
            if(it == JOB_SEEKER){
                getUsersList("Recruiter"){taskComplete ->
                    if(taskComplete){
                        newUserMessageAdapter =  NewUserMessageAdapter(filterRecruiterList,this)
                        binding.recyclerView.adapter = newUserMessageAdapter
                    }
                }
            }
            if(it == RECRUITER){
                getUsersList("Job Seeker"){taskComplete ->
                    if(taskComplete){
                        newUserMessageAdapter = NewUserMessageAdapter(filterJobSeekerList,this)
                        binding.recyclerView.adapter = newUserMessageAdapter
                    }
                }
            }

        }*/

/*        binding.toggleGrp.addOnButtonCheckedListener{ toggleButtonGrp,checkedId, isChecked ->
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
        }*/

        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (isScrolling && (totalItems == currentItems + firstVisibleItemPosition)) {
                    isScrolling = false
                    currentPage++
                    Log.d("###", "onScrolled: $currentPage")
                    /*listenerForLatestMsg{
                        adapter.notifyDataSetChanged()
                    }*/
                }
            }
        })
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

    /*private fun getUsersList(userType: String, callBack:(Boolean) -> Unit) {



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
    }*/

    private fun getUsersList(userType: Int,callBack:(Boolean) -> Unit) {

        if (Utils.isNetworkAvailable(this)) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE


            when (userType) {
                RECRUITER -> {
                    AndroidNetworking.get(NetworkUtils.GET_ALL_JOBSEEKER)
                        .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                        .addQueryParameter("current_page", currentPage.toString())
                        .setPriority(Priority.MEDIUM).build()
                        .getAsObject(
                            GetAllUsers::class.java,
                            object : ParsedRequestListener<GetAllUsers> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onResponse(response: GetAllUsers?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            Log.d("###", "onResponse: ${it.data}")
                                            filterUserList.addAll(it.data)
                                            userList.addAll(it.data)
                                            if (userList.isNotEmpty()) {
                                                totalPages = it.total_pages
                                                /*hideShowEmptyView(true)*/
                                            } else {
                                                /*hideShowEmptyView(false)*/
                                            }
                                            binding.progressCircular.visibility = View.GONE
                                            callBack(true)

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse: catch: ${e.message}")
                                        callBack(false)

                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    /*hideShowEmptyView(false)*/
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                        )
                                        /*if (it.errorCode >= 500) {
                                                binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
                                            }*/
                                    }
        //                                hideProgressDialog()
                                    callBack(false)
                                }
                            })
                }
                JOB_SEEKER -> {
                    AndroidNetworking.get(NetworkUtils.GET_ALL_RECRUITER)
                        .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                        .addQueryParameter("current_page", currentPage.toString())
                        .setPriority(Priority.MEDIUM).build()
                        .getAsObject(
                            GetAllUsers::class.java,
                            object : ParsedRequestListener<GetAllUsers> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onResponse(response: GetAllUsers?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            Log.d("###", "onResponse: ${it.data}")
                                            filterUserList.addAll(it.data)
                                            userList.addAll(it.data)
                                            if (userList.isNotEmpty()) {
                                                totalPages = it.total_pages
                                                /*hideShowEmptyView(true)*/
                                            } else {
                                                /*hideShowEmptyView(false)*/
                                            }
                                            binding.progressCircular.visibility = View.GONE
                                            callBack(true)

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse: catch: ${e.message}")
                                        callBack(false)
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    /*hideShowEmptyView(false)*/
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                        )
                                        /*if (it.errorCode >= 500) {
                                                binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
                                            }*/
                                    }
        //                                hideProgressDialog()
                                    callBack(false)

                                }
                            })
                }
                else -> {
                    makeToast(getString(R.string.didn_t_get_proper_user_type),0)
                    Log.d(TAG,"${getString(R.string.didn_t_get_proper_user_type)} => userType :: $userType" )
                }
            }
        } else {
            Utils.showNoInternetBottomSheet(this, this)
            callBack(false)
//            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }
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