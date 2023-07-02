package com.example.recruiter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.recruiter.databinding.ActivityChatBoardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatBoardActivity : AppCompatActivity() ,OnClickListener{
    private lateinit var binding: ActivityChatBoardBinding

    companion object{
        private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
        private const val TAG = "ChatBoardActivity"
    }


    private val messageList: MutableList<MessageData> = mutableListOf()// Get your list of messages
    private var toId = String()
    private var fromId = String()
    private var userFName = String()
    private var userPhoneNumber = String()
    private var usersJobSeeker =  UsersJobSeeker()
    private var usersRecruiter = UsersRecruiter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userJ = intent.getSerializableExtra("userObjectJ", UsersJobSeeker::class.java)
        val userR = intent.getSerializableExtra("userObjectR", UsersRecruiter::class.java)

        fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        if (userJ != null){
            usersJobSeeker = userJ
            userFName = userJ.userFName +" "+ (userJ.userLName)
            toId = userJ.userId
            if(userJ.userProfileImg.isNotEmpty()){
                Glide.with(this@ChatBoardActivity)
                    .load(userJ.userProfileImg)
                    .apply(
                        RequestOptions
                            .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                    )
                    .into(binding.profileImg)
            }
            userPhoneNumber = userJ.userPhoneNumber
        }
        else if (userR != null){
            usersRecruiter = userR
            userFName = userR.userFName +" "+ (userR.userLName)
            toId = userR.userId
            if(userR.userProfileImg.isNotEmpty()){
                Glide.with(this@ChatBoardActivity)
                    .load(userR.userProfileImg)
                    .apply(
                        RequestOptions
                            .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                    )
                    .into(binding.profileImg)
            }
            userPhoneNumber = userR.userPhoneNumber
        }

        setSupportActionBar(binding.toolbar)
        if (userFName.isNotEmpty()) supportActionBar?.title = userFName
        supportActionBar?.elevation = 0f


        setOnClickListener()

        textWatcherForMsgEditText()
//        setDummyData()
    }

    override fun onStart() {
        super.onStart()

        listenerForMessages {
            val adapter = ChatAdapter(messageList)
            binding.recyclerView.adapter = adapter
            Log.d(TAG, messageList.size.toString())
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }

    }

    private fun textWatcherForMsgEditText() {
        binding.inputMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                binding.btnSend.visibility = INVISIBLE
                binding.btnVoiceMsg.visibility = VISIBLE
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if (s.isNotEmpty()) {

                    binding.btnSend.visibility = VISIBLE
                    binding.btnVoiceMsg.visibility = INVISIBLE
                } else {

                    binding.btnSend.visibility = INVISIBLE
                    binding.btnVoiceMsg.visibility = VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {
                // No implementation needed
//                binding.btnSend.visibility = INVISIBLE
//                binding.btnVoiceMsg.visibility = VISIBLE
            }
        })
    }

    private fun listenerForMessages(completion: () -> Unit) {
        messageList.clear()
        FirebaseDatabase.getInstance().getReference("Messenger")
            .child("userMessages")
            .child(fromId)
            .child(toId)
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messageData = snapshot.getValue(MessageData::class.java)

                    if (messageData != null ){
                        messageList.add(messageData)
                        Log.d("messageData", messageData.message.toString())
                        completion()
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_chat,menu)
        return true
    }

    private fun setOnClickListener(){
        binding.btnSend.setOnClickListener(this)
        binding.btnVoiceMsg.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSend -> {
                Log.d(TAG,"Attempt to send Text message")
                performSendTextMsg()
                
            }
            R.id.btnVoiceMsg -> {
                Log.d(TAG,"Attempt to send Voice Message message")
            }
        }
    }

    private fun performSendTextMsg() {
        val fromReference = FirebaseDatabase.getInstance().getReference("Messenger").child("userMessages")
            .child(fromId)
            .child(toId)
            .push()

        val toReference = FirebaseDatabase.getInstance().getReference("Messenger").child("userMessages")
            .child(toId)
            .child(fromId)
            .push()

        val currentDateTime = LocalDateTime.now()

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = currentDateTime.format(dateFormatter)

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time = currentDateTime.format(timeFormatter)

        Log.d(TAG,"From:${fromId} :: To:${toId}")
        val messageData = MessageData(
            fromReference.key.toString(),
            binding.inputMessage.text.toString(),
            toId,
            fromId,
            date,
            time
        )
        fromReference.setValue(messageData)
            .addOnSuccessListener {
                binding.inputMessage.text?.clear()
                binding.recyclerView.scrollToPosition(messageList.size -1)
                Log.d(TAG,"SuccessFully saved Send Msg To database:${fromReference.key}")
            }
            .addOnFailureListener {
                binding.inputMessage.text?.clear()
                makeToast("There is Something wrong in our System.",0)
                Log.d(TAG,"Couldn't saved Send Msg To database:${fromReference.key}")
            }

        toReference.setValue(messageData)
            .addOnSuccessListener{
                binding.recyclerView.scrollToPosition(messageList.size -1)
                binding.inputMessage.text?.clear()
                Log.d(TAG,"SuccessFully saved Send Msg To database:${toReference.key}")
            }
            .addOnFailureListener {
                binding.inputMessage.text?.clear()
                makeToast("There is Something wrong in our System.",0)
                Log.d(TAG,"Couldn't saved Send Msg To database:${toReference.key}")
            }


        val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("Messenger")
            .child("LatestMessage")
            .child(fromId)
            .child(toId)
            .setValue(messageData)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("Messenger")
            .child("LatestMessage")
            .child(toId)
            .child(fromId)
            .setValue(messageData)
    }

    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}