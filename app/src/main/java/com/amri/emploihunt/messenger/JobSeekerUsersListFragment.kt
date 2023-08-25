package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentJobSeekerUsersListBinding
import com.amri.emploihunt.jobSeekerSide.UsersJobSeeker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class JobSeekerUsersListFragment : BaseFragment(), UserListUpdateListener,
    LatestMessageAdapterJ.OnChatJClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentJobSeekerUsersListBinding? = null
    private val binding get() = _binding!!
    companion object {
        private const val TAG = "JobSeekerUsersListFragment"
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
    }

    private lateinit var latestMessageList: MutableList<MessageData>
    private lateinit var filterLatestMessageList: MutableList<MessageData>


    private lateinit var fromId :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    private lateinit var adapter: LatestMessageAdapterJ
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobSeekerUsersListBinding.inflate(inflater, container, false)

        return binding.root

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        latestMessageList = mutableListOf()
        filterLatestMessageList = mutableListOf()

        adapter = LatestMessageAdapterJ(filterLatestMessageList, requireActivity(),this)

        binding.recyclerView.adapter = adapter

        listenerForLatestMsg {
            adapter.notifyDataSetChanged()
        }

    }

//    private val latestMessagesMap = HashMap<String, MessageData>()

    private fun listenerForLatestMsg(completion: () -> Unit) {
        latestMessageList.clear()
        filterLatestMessageList.clear()
        FirebaseDatabase.getInstance().getReference("Messenger")
            .child("LatestMessage")
            .child(fromId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(chatSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG,"previousChildName : $previousChildName")
                    val chatMessage = chatSnapshot.getValue(MessageData::class.java)

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child("Job Seeker")
                        .addChildEventListener(object: ChildEventListener {
                            override fun onChildAdded(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage.let {
//                                        filterLatestMessageList.add(chatMessage)
                                        latestMessageList.add(chatMessage)
//                                        sortFilterList()
                                        sortMainList()
                                        /*latestMessagesMap[chatSnapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()*/
                                    }
                                    Log.d("messageData", chatMessage.toId.toString())
                                }

//                                Log.d(TAG, chatSnapshot.key.toString())
                                completion()
                            }

                            override fun onChildChanged(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage.let {
//                                        filterLatestMessageList.add(chatMessage)
                                        latestMessageList.add(chatMessage)
//                                        sortFilterList()
                                        sortMainList()
                                        /*latestMessagesMap[chatSnapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()*/
                                    }
                                    Log.d("messageData", chatMessage.toId.toString())
                                }

//                                Log.d(TAG, chatSnapshot.key.toString())
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

                        })


                }

                override fun onChildChanged(chatSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG,"previousChildName : $previousChildName")
                    val chatMessage = chatSnapshot.getValue(MessageData::class.java)

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child("Job Seeker")
                        .addChildEventListener(object: ChildEventListener {
                            override fun onChildAdded(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage.let {
//                                        filterLatestMessageList.add(chatMessage)
                                        latestMessageList.add(chatMessage)
//                                        sortFilterList()
                                        sortMainList()
                                        /*latestMessagesMap[chatSnapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()*/
                                    }
                                    Log.d("messageData", chatMessage.toId.toString())
                                }

                                Log.d(TAG, chatSnapshot.key.toString())
                                completion()
                            }

                            override fun onChildChanged(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage.let {
//                                        filterLatestMessageList.add(chatMessage)
                                        latestMessageList.add(chatMessage)
//                                        sortFilterList()
                                        sortMainList()
                                        /*latestMessagesMap[chatSnapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()*/
                                    }
                                    Log.d("messageData", chatMessage.toId.toString())
                                }

                                Log.d(TAG, chatSnapshot.key.toString())
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

                        })


                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortMainList() {
        filterLatestMessageList.clear()
        latestMessageList.sortWith(
            compareByDescending<MessageData> {
                it.dateStamp
            }
                .thenByDescending {
                    it.timeStamp
                }
        )

        filterLatestMessageList.addAll(latestMessageList)

        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortFilterList() {
        filterLatestMessageList.sortWith(
            compareByDescending<MessageData> {
                it.dateStamp
            }
                .thenByDescending {
                    it.timeStamp
                }
        )
        adapter.notifyDataSetChanged()

    }

    /*    private fun refreshRecyclerViewMessages() {
            latestMessageList.clear()
            filterLatestMessageList.clear()
            latestMessagesMap.values.forEach {
                filterLatestMessageList.add(it)
            }
            latestMessageList.addAll(filterLatestMessageList)
        }*/

    @SuppressLint("NotifyDataSetChanged")
    override fun updateUserList(query: String) {

        filterLatestMessageList.clear()
        if (!TextUtils.isEmpty(query)){
            Log.d(TAG,"latestMessageList :: $latestMessageList")
            for (latestMsg: MessageData in latestMessageList) {
                val chatPartnerId:String = if(latestMsg.fromId == FirebaseAuth.getInstance().currentUser?.uid){
                       latestMsg.toId!!
                }
                else{
                    latestMsg.fromId!!
                }

                filterList(chatPartnerId){ chatPartnerName ->
                    if (chatPartnerName.lowercase(Locale.ROOT)
                            .contains(query.lowercase(Locale.ROOT))
                    ) {
                        Log.d(TAG,"ChatPartnerName :: $chatPartnerName -> Query :: $query")
                        filterLatestMessageList.add(latestMsg)
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            Log.d(TAG,"filterLatestMessageList :: $filterLatestMessageList")
        }
        else{
            filterLatestMessageList.addAll(latestMessageList)
        }

        adapter.notifyDataSetChanged()

    }

    private fun filterList(chatPartnerId: String, completion: (String) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("Users")
            .child("Job Seeker")
            .child(chatPartnerId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val chatPartnerName = "${snapshot.child("userFName").getValue(String::class.java)} ${snapshot.child("userLName").getValue(String::class.java)}"

                    completion(chatPartnerName)

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onChatJClick(position: Int, usersJobSeeker: UsersJobSeeker) {
        val intent = Intent(activity, ChatBoardActivity::class.java)
        intent.putExtra("userObjectJ",usersJobSeeker)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity?.startActivity(intent)
        activity?.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_left
        )
//        activity?.finish()
    }

    override fun onChatJLongClick(position: Int, usersJobSeeker: UsersJobSeeker) {
        TODO("Not yet implemented")
    }


}