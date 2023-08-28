package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentRecruiterUserListBinding
import com.amri.emploihunt.model.GetUserById
import com.amri.emploihunt.model.LatestChatMsg
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.recruiterSide.UsersRecruiter
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
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
import java.util.Locale

class RecruiterUserListFragment : BaseFragment(), UserListUpdateListener,
    /*LatestMessageAdapterR.OnChatRClickListener,*/  LatestMessageAdapterJ.OnChatJClickListener {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRecruiterUserListBinding? = null
    private val binding get() = _binding!!
    companion object {
        private const val TAG = "RecruiterUserList"
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }

    /*private lateinit var latestMessageList: MutableList<MessageData>
    private lateinit var filterLatestMessageList: MutableList<MessageData>*/

    private lateinit var latestMessageList: MutableList<LatestChatMsg>
    private lateinit var filterLatestMessageList: MutableList<LatestChatMsg>

    private lateinit var adapter: LatestMessageAdapterJ
    /*private lateinit var adapter: LatestMessageAdapterR*/
    private var fromId :String ?= null
    private var userType :Int ?= null

    lateinit var prefManager: SharedPreferences

    private lateinit var layoutManager: LinearLayoutManager

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val bundle = arguments
        if (bundle != null){
            fromId = bundle.getString("userId")
            userType = bundle.getInt("role",-1)
        }
        else{
            Log.d(TAG,"Bundle is null")
            makeToast(getString(R.string.something_error),0)
        }*/
        /*fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()*/

        prefManager = prefManager(requireContext())

        userType = prefManager.get(ROLE,0)
        fromId = prefManager.get(FIREBASE_ID)

        _binding = FragmentRecruiterUserListBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recyclerView.layoutManager = layoutManager

        latestMessageList = mutableListOf()
        filterLatestMessageList = mutableListOf()

        /*adapter = LatestMessageAdapterR(filterLatestMessageList, requireActivity(),this)*/


        listenerForLatestMsg {

            /*latestMessageList.sortWith(
                compareByDescending<LatestChatMsg> {
                    it.latestChatMsg.dateStamp
                }
                    .thenByDescending {
                        it.latestChatMsg.timeStamp
                    }
            )
            filterLatestMessageList.sortWith(
                compareByDescending<LatestChatMsg> {
                    it.latestChatMsg.dateStamp
                }
                    .thenByDescending {
                        it.latestChatMsg.timeStamp
                    }
            )*/
            adapter = LatestMessageAdapterJ(filterLatestMessageList, requireActivity(),fromId,this)


            binding.recyclerView.adapter = adapter
           
        }

    }

//    private val latestMessagesMap = HashMap<String, MessageData>()


    /** need attention */
    /** Need to create a function similar to below for latest msg listener */
    private fun listenerForLatestMsg(completion: () -> Unit) {
        latestMessageList.clear()
        filterLatestMessageList.clear()
        if(fromId != null){
            FirebaseDatabase.getInstance().getReference("Messenger")
                .child("LatestMessage")
                .child(fromId!!)
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val chatMessage = snapshot.getValue(MessageData::class.java)
                        if (chatMessage != null) {
                            Log.d(TAG, "onChildAdded: ${chatMessage.msgId}")
                            retrieveRData(chatMessage)
                        }
//                        sortMainList()
                        completion()
                        /*FirebaseDatabase.getInstance().getReference("Users")
                            .child("Recruiter")
                            .addChildEventListener(object: ChildEventListener {
                                override fun onChildAdded(
                                    usersSnapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {

                                    if(chatMessage?.toId == usersSnapshot.key.toString()){
                                        chatMessage.let {
                                            filterLatestMessageList.add(chatMessage)
                                            latestMessageList.add(chatMessage)
                                        }
                                        Log.d("messageData", chatMessage.toId.toString())
                                    }

                                    Log.d(TAG, snapshot.key.toString())
                                    completion()
                                }

                                override fun onChildChanged(
                                    usersSnapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    if(chatMessage?.toId == usersSnapshot.key.toString()){
                                        chatMessage.let {

                                            filterLatestMessageList.add(chatMessage)
                                            latestMessageList.add(chatMessage)
                                        }
                                        Log.d("messageData", chatMessage.toId.toString())
                                    }

                                    Log.d(TAG, snapshot.key.toString())
                                    completion()
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })*/


                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val chatMessage = snapshot.getValue(MessageData::class.java)
                        if (chatMessage != null) {
                            retrieveRData(chatMessage)
                        }
//                        sortMainList()
                        completion()
                        /*FirebaseDatabase.getInstance().getReference("Users")
                            .child("Recruiter")
                            .addChildEventListener(object: ChildEventListener {
                                override fun onChildAdded(
                                    usersSnapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {

                                    if(chatMessage?.toId == usersSnapshot.key.toString()){
                                        chatMessage.let {
                                            filterLatestMessageList.add(chatMessage)
                                            latestMessageList.add(chatMessage)
                                        }
                                        Log.d("messageData", chatMessage.toId.toString())
                                    }

                                    Log.d(TAG, snapshot.key.toString())
                                    completion()
                                }

                                override fun onChildChanged(
                                    usersSnapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    if(chatMessage?.toId == usersSnapshot.key.toString()){
                                        chatMessage.let {

                                            filterLatestMessageList.add(chatMessage)
                                            latestMessageList.add(chatMessage)
                                        }
                                        Log.d("messageData", chatMessage.toId.toString())
                                    }

                                    Log.d(TAG, snapshot.key.toString())
                                    completion()
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })*/


                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
        else{
            makeToast("Didn't get user-Id",0)
            Log.d(TAG,"$fromId :: $userType")
            completion()
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

        adapter.notifyDataSetChanged()
    }

    private fun retrieveRData(chatMessage: MessageData) {
        if (Utils.isNetworkAvailable(requireContext())) {
            /*if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE*/


            val chatPartnerId:String = if(chatMessage.fromId == this.fromId){
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
//                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    filterLatestMessageList.add(LatestChatMsg(chatMessage,response.data))
                                    latestMessageList.add(LatestChatMsg(chatMessage,response.data))
//                                        sortFilterList()
                                    Log.d("messageData", chatMessage.toId.toString())
                                    adapter.notifyDataSetChanged()

                                    /*for(user in it.data){
                                        if(chatMessage?.toId == user.vFirebaseId){
                                            chatMessage.let {
//                                        filterLatestMessageList.add(chatMessage)
                                                latestMessageList.add(chatMessage)
//                                        sortFilterList()
                                                sortMainList()
                                            }
                                            Log.d("messageData", chatMessage.toId.toString())
                                        }

                                    }*/
                                    /*if (latestMessageList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        adapter.notifyDataSetChanged()
                                        *//*hideShowEmptyView(true)*//*
                                    } else {
                                        *//*hideShowEmptyView(false)*//*
                                    }*/
                                }
                            } catch (e: Exception) {
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
                            hideProgressDialog()
                        }
                    })
        } else {
            Utils.showNoInternetBottomSheet(requireContext(),requireActivity())
//            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }
    }

//    private fun refreshRecyclerViewMessages() {
//        latestMessageList.clear()
//        latestMessagesMap.values.forEach {
//            latestMessageList.add(it)
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateUserList(query: String) {
        filterLatestMessageList.clear()
        if (!TextUtils.isEmpty(query)){
            Log.d(TAG,"latestMessageList :: $latestMessageList")
            for (latestMsg: LatestChatMsg in latestMessageList) {
                /*val chatPartnerId:String = if(latestMsg.latestChatMsg.fromId == fromId*//*FirebaseAuth.getInstance().currentUser?.uid*//*){
                    latestMsg.latestChatMsg.toId!!
                }
                else{
                    latestMsg.latestChatMsg.fromId!!
                }*/
                val fullName = latestMsg.user.vFirstName + " " + latestMsg.user.vLastName
                if (fullName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    Log.d(TAG,"ChatPartnerName :: $fullName -> Query :: $query")
                    filterLatestMessageList.add(latestMsg)
                    adapter.notifyDataSetChanged()
                }
                /*filterList(chatPartnerId){ chatPartnerName ->
                    if (chatPartnerName.lowercase(Locale.ROOT)
                            .contains(query.lowercase(Locale.ROOT))
                    ) {
                        Log.d(TAG,"ChatPartnerName :: $chatPartnerName -> Query :: $query")
                        filterLatestMessageList.add(latestMsg)
                        adapter.notifyDataSetChanged()
                    }
                }*/
            }

            Log.d(TAG,"filterLatestMessageList :: $filterLatestMessageList")
        }
        else{
            filterLatestMessageList.addAll(latestMessageList)
        }

        adapter.notifyDataSetChanged()
    }


    /** need attention */
    /** Need to create a function similar to below to chatPartnerName */
    /*private fun filterList(chatPartnerId: String, completion: (String) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child("Recruiter")
            .child(chatPartnerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    val chatPartnerName = "${snapshot.child("userFName").getValue(String::class.java)} ${snapshot.child("userLName").getValue(String::class.java)}"

                    completion(chatPartnerName)
                }

                override fun onCancelled(error: DatabaseError) {
                    
                }
            })
    }*/

    /*override fun onChatRClick(position: Int, user: User) {
        val intent = Intent(activity, ChatBoardActivity::class.java)
        intent.putExtra("userId",fromId)
        intent.putExtra("userObject",user)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
        activity?.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )
    }*/
    /*override fun onChatRClick(position: Int, usersRecruiter: UsersRecruiter) {
        val intent = Intent(activity, ChatBoardActivity::class.java)
        intent.putExtra("userId",fromId)
        intent.putExtra("userObjectR",usersRecruiter)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
        activity?.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )
    }*/


    /** Need to implement this function to delete chat on long click */
    /*override fun onChatRLongClick(position: Int, usersRecruiter: UsersRecruiter) {

    }*/

    override fun onChatJClick(position: Int, user: User) {
        val intent = Intent(activity, ChatBoardActivity::class.java)
        intent.putExtra("userId",fromId)
        intent.putExtra("userObject",user)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
        activity?.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )
    }

    override fun onChatJLongClick(position: Int, user: User) {
        TODO("Not yet implemented")
    }


}