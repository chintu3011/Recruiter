package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityMessaengerHomes2Binding
import com.amri.emploihunt.model.GetUserById
import com.amri.emploihunt.model.LatestChatMsg
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessaengerHomesActivity_2 : BaseActivity(), LatestMessageAdapter.OnChatClickListener {


    private var currentFragment: Fragment?= null
    private lateinit var binding: ActivityMessaengerHomes2Binding

    private var userId:String ?= null
    private var userType:Int ?= null


    lateinit var prefManager: SharedPreferences

    private lateinit var latestMessageAdapter: LatestMessageAdapter

    private lateinit var latestMessageList: MutableList<LatestChatMsg>
    private lateinit var filterLatestMessageList: MutableList<LatestChatMsg>

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1

    companion object{
        private const val TAG = "MessaengerHomesActivity_2"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessaengerHomes2Binding.inflate(layoutInflater)
        setContentView(binding.root)


        val window: Window = this@MessaengerHomesActivity_2.window
        window.statusBarColor = ContextCompat.getColor(this@MessaengerHomesActivity_2, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        prefManager = PrefManager.prefManager(this)
        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)
        Log.d("####","$userId :: $userType")

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Messenger"


        latestMessageList = mutableListOf()
        filterLatestMessageList = mutableListOf()

        binding.btnCreateNewChat.setOnClickListener {
            val intent = Intent(this@MessaengerHomesActivity_2, NewUsersMessageActivity::class.java)
            intent.putExtra("role",userType)
            Log.d(TAG,"userType:$userType")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
//                finish()
        }


        listenerForLatestMsg {
            latestMessageAdapter = LatestMessageAdapter(filterLatestMessageList,this@MessaengerHomesActivity_2,userId,userType!!,this)

            binding.recyclerView.adapter = latestMessageAdapter

        }

        setMenuItemListener()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        })
    }


    private fun listenerForLatestMsg(completion: () -> Unit) {
        binding.progressCircular.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        latestMessageList.clear()
        filterLatestMessageList.clear()
        if(userId != null){
           
            Log.d("###", "listenerForLatestMsg: $userId")
            val databaseReference = FirebaseDatabase.getInstance().getReference("Messenger").child("LatestMessage").child(userId!!)

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // The userId exists in the database, you can add the child event listener here
                        Log.d("###", "onDataChange: ")
                        databaseReference .addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(chatSnapshot: DataSnapshot, previousChildName: String?) {
                                /*Log.d(TAG,"previousChildName : $previousChildName")*/
                                Log.d("###", "onChildAdded: ${chatSnapshot.value}")
                                val chatMessage = chatSnapshot.getValue(MessageData::class.java)

                                if (chatMessage != null) {
                                    Log.d("####", "onChildAdded: ${chatMessage.fromId}")
                                    /***/
                                    retrieveUserData(chatMessage)
                                }
                                hideShowEmptyView(true)
                                /*sortMainList()*/
                                completion()
                                Log.d("test", "onChildAdded: ")

                            }

                            override fun onChildChanged(chatSnapshot: DataSnapshot, previousChildName: String?) {
                                Log.d(TAG,"previousChildName : $previousChildName")
                                val chatMessage = chatSnapshot.getValue(MessageData::class.java)
                                /***/
                                if (chatMessage != null) {
                                    Log.d("####", "onChildAdded: ${chatMessage.fromId}")
                                    /***/
                                    retrieveUserData(chatMessage)
                                }
                                Log.d("test", "onChildAdded: ")

                                /*sortMainList()*/
                                completion()

                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                binding.progressCircular.visibility = View.GONE
                                
                                Log.d("test", "onChildAdded: ")
                            }

                            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                                binding.progressCircular.visibility = View.GONE
                                
                                Log.d("test", "onChildAdded: ")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                binding.progressCircular.visibility = View.GONE
                                
                                Log.d("test", "onChildAdded: ")
                            }

                        })
                    } else {
                        // The userId does not exist in the database, handle it accordingly
                        hideShowEmptyView(false)
                        binding.progressCircular.visibility = View.GONE
                        binding.layEmptyView.tvNoData.text =
                            getString(R.string.sorry_you_have_no_any_chats_please_add_chat_user_using_below_add_people)
                      
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the onCancelled event if needed
                }
            })






        }
        else{
            makeToast("Didn't get user-Id",0)
            Log.d(TAG,"$userId :: $userType")
            completion()
        }

    }

    private fun hideShowEmptyView(
        isShow: Boolean,  isInternetAvailable: Boolean = true
    ) {
        binding.recyclerView.visibility = if (isShow) View.VISIBLE else View.GONE
        binding.layEmptyView.root.visibility = if (isShow) View.GONE else View.VISIBLE
        binding.layProgressPagination.root.visibility = View.GONE
        binding.progressCircular.visibility = View.GONE
        if (isInternetAvailable) {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_job_found)
            binding.layEmptyView.btnRetry.visibility = View.GONE
        } else {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_internet)
            binding.layEmptyView.btnRetry.visibility = View.VISIBLE
            binding.layEmptyView.btnRetry.setOnClickListener {
                listenerForLatestMsg {
                    latestMessageAdapter = LatestMessageAdapter(filterLatestMessageList,this@MessaengerHomesActivity_2,userId,userType!!,this)

                    binding.recyclerView.adapter = latestMessageAdapter

                }

            }
        }
    }
    private fun retrieveUserData(chatMessage: MessageData) {

        if (Utils.isNetworkAvailable(this)) {
            val chatPartnerId:String = if(chatMessage.fromId == this.userId){
                chatMessage.toId!!
            } else{
                chatMessage.fromId!!
            }

            AndroidNetworking.get(NetworkUtils.GET_USER_BY_FIREBASE_ID)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("vFirebaseId",chatPartnerId)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetUserById::class.java,
                    object : ParsedRequestListener<GetUserById> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(response: GetUserById?) {
                            try {
                                response?.let {
                                    
                                    Log.d("###", "onResponse: ${it.data}")

                                    updateOrAddLatestMessage(latestMessageList,LatestChatMsg(chatMessage,response.data))

                                    sortMainList()
                                    Log.d("messageData", chatMessage.toId.toString())

                                    binding.progressCircular.visibility = View.GONE


                                    /*if (latestMessageList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        adapter.notifyDataSetChanged()
                                        *//*hideShowEmptyView(true)*//*
                                    } else {
                                        *//*hideShowEmptyView(false)*//*
                                    }*/
                                }
                            } catch (e: Exception) {
                                
                                binding.recyclerView.visibility = View.VISIBLE
                                binding.progressCircular.visibility = View.GONE
                                Log.e("#####", "onResponse: catch: ${e.message}")
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
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.progressCircular.visibility = View.GONE
                            
                        }
                    })
        } else {
            
            binding.recyclerView.visibility = View.VISIBLE
            binding.progressCircular.visibility = View.GONE
            Utils.showNoInternetBottomSheet(this,this@MessaengerHomesActivity_2)
//            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }
    }


    fun updateOrAddLatestMessage(
        mainList:MutableList<LatestChatMsg>,
        newMessage: LatestChatMsg
    ) {
        val existingIndexM = mainList.indexOfFirst {
            it.user.vFirebaseId == newMessage.user.vFirebaseId
        }
        if (existingIndexM != -1) {
            mainList[existingIndexM] = newMessage
        } else {
            mainList.add(newMessage)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortMainList() {
        filterLatestMessageList.clear()
        latestMessageList.sortWith(
            compareByDescending<LatestChatMsg> {
                it.latestChatMsg.dateStamp
            }
                .thenByDescending {
                    it.latestChatMsg.timeStamp
                }
        )

        filterLatestMessageList.addAll(latestMessageList)
        latestMessageAdapter.notifyDataSetChanged()
        binding.recyclerView.visibility = View.VISIBLE

    }

    private var btnSearch: MenuItem? = null
    private var btnVoiceSearch: MenuItem? = null
    private var btnDelete: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu,menu)

        btnSearch = menu?.findItem(R.id.btnSearch)
        btnVoiceSearch = menu?.findItem(R.id.btnVoiceSearch)
        btnDelete = menu?.findItem(R.id.btnDelete)
        btnDelete?.isVisible = false
        return true
    }
    private fun setMenuItemListener() {
        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.btnSearch -> {
                    val searchView = it.actionView as SearchView
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return true
                        }
                        override fun onQueryTextChange(newText: String?): Boolean {
                            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                            if (currentFragment is UserListUpdateListener) {
                                currentFragment.updateUserList(newText.orEmpty())
                            }
                            return true
                        }
                    })
                    true
                }
                R.id.btnVoiceSearch -> {
                    openVoice()
                    true
                }
                R.id.btnDelete -> {
//                    deleteUser()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun openVoice() {
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

    override fun onChatClick(position: Int, user: User) {
        val intent = Intent(this@MessaengerHomesActivity_2, ChatBoardActivity::class.java)
        intent.putExtra("userId",userId)
        intent.putExtra("userObject",user)
        intent.putExtra("isNotification",false)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )
    }

    override fun onChatLongClick(position: Int, usersRecruiter: User) {

    }

    /*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
                val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val query = matches!![0]
                if (matches.isNotEmpty()) {
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                    if (currentFragment is UserListUpdateListener) {
                        currentFragment.updateUserList(query.orEmpty())
                    }
                }
            } else {

            }
        }*/

}

