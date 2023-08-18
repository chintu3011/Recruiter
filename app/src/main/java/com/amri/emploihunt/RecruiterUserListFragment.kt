package com.amri.emploihunt

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amri.emploihunt.databinding.FragmentRecruiterUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RecruiterUserList : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRecruiterUserListBinding? = null
    private val binding get() = _binding!!
    companion object {
        private const val TAG = "RecruiterUserList"
    }

    private var latestMessageList: MutableList<MessageData> = mutableListOf()


    private lateinit var fromId :String

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
        // Inflate the layout for this fragment
        _binding = FragmentRecruiterUserListBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        val adapter = LatestMessageAdapterR(latestMessageList,requireActivity(),)

        binding.recyclerView.adapter = adapter

        listenerForLatestMsg {
            // No need to create a new adapter, just notify the existing adapter that the data has changed

            adapter.notifyDataSetChanged()
        }
        return binding.root
    }

    private val latestMessagesMap = HashMap<String,MessageData>()

    private fun listenerForLatestMsg(completion: () -> Unit) {
        latestMessageList.clear()
        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("Messenger")
            .child("LatestMessage")
            .child(fromId)
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(MessageData::class.java)

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child("Recruiter")
                        .addChildEventListener(object: ChildEventListener {
                            override fun onChildAdded(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage?.let {
                                        latestMessagesMap[snapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()
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
                                    chatMessage?.let {
                                        latestMessagesMap[snapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()
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

                        })


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(MessageData::class.java)

                    FirebaseDatabase.getInstance().getReference("Users")
                        .child("Recruiter")
                        .addChildEventListener(object: ChildEventListener {
                            override fun onChildAdded(
                                usersSnapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                                if(chatMessage?.toId == usersSnapshot.key.toString()){
                                    chatMessage?.let {
                                        latestMessagesMap[snapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()
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
                                    chatMessage?.let {
                                        latestMessagesMap[snapshot.key!!] = chatMessage
                                        refreshRecyclerViewMessages()
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

    private fun refreshRecyclerViewMessages() {
        latestMessageList.clear()
        latestMessagesMap.values.forEach {
            latestMessageList.add(it)
        }
    }



}