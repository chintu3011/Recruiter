package com.amri.emploihunt.messenger

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityChatBoardBinding
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.model.User
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.ROLE
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ChatBoardActivity : BaseActivity() ,OnClickListener{
    private lateinit var binding: ActivityChatBoardBinding

    companion object{
        private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
        private const val TAG = "ChatBoardActivity"
    }

    private var userType:Int ?= null
    private lateinit var messageList: MutableList<MessageData> // Get your list of messages
    private var toId:String ?= null
    private var fromId:String ?= null
    private var userFName:String ?= null
    private var userPhoneNumber:String ?= null

    private lateinit var prefManager: SharedPreferences
    /*private var usersJobSeeker =  UsersJobSeeker()
    private var usersRecruiter = UsersRecruiter()*/

    private var user:User ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG,"chat board started")

        val window: Window = this@ChatBoardActivity.window
        window.statusBarColor = ContextCompat.getColor(this@ChatBoardActivity,R.color.theme_blue)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        prefManager = prefManager(this)

        userType = prefManager.get(ROLE,0)
        fromId = prefManager.get(FIREBASE_ID)

        Log.d(TAG,"$fromId :: $userType")
        messageList = mutableListOf()

        /*val userJ = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("userObjectJ", UsersJobSeeker::class.java)
        } else {
            val bundle = intent.extras
            bundle?.getSerializable("userObjectJ") as? UsersJobSeeker
        }*/


        /*val userR = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("userObjectR", UsersRecruiter::class.java)
        } else {
            val bundle = intent.extras
            bundle?.getSerializable("userObjectR") as? UsersRecruiter
        }*/

        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG,"${Build.VERSION.SDK_INT}")
            intent.getSerializableExtra("userObject", User::class.java)
        } else {
            Log.d(TAG,"${Build.VERSION.SDK_INT}")
            val bundle = intent.extras
            bundle?.getSerializable("userObject") as? User
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.elevation = 0f

        /*fromId = FirebaseAuth.getInstance().currentUser?.uid.toString()*/

        if (user != null){
            userFName = user!!.vFirstName +" "+ (user!!.vLastName)
            if (userFName!!.isNotEmpty()) supportActionBar?.title = userFName
            toId = user!!.vFirebaseId
            if(user!!.tProfileUrl != null){
                Glide.with(this@ChatBoardActivity)
                    .load(user!!.tProfileUrl)
                    .apply(
                        RequestOptions
                            .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                    )
                    .into(binding.profileImg)
            }
            userPhoneNumber = user!!.vMobile
        }
        /*if (userJ != null){
            usersJobSeeker = userJ
            userFName = userJ.userFName +" "+ (userJ.userLName)
            toId = userJ.userId
            if(userJ.userProfileImgUri.isNotEmpty()){
                Glide.with(this@ChatBoardActivity)
                    .load(userJ.userProfileImgUri)
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
            if(userR.userProfileImgUri.isNotEmpty()){
                Glide.with(this@ChatBoardActivity)
                    .load(userR.userProfileImgUri)
                    .apply(
                        RequestOptions
                            .placeholderOf(DEFAULT_PROFILE_IMAGE_RESOURCE)
                            .error(DEFAULT_PROFILE_IMAGE_RESOURCE)
                    )
                    .into(binding.profileImg)
            }
            userPhoneNumber = userR.userPhoneNumber
        }*/



        setOnClickListener()

        textWatcherForMsgEditText()
////        setDummyData()
    }

    override fun onStart() {
        super.onStart()

        listenerForMessages {
            val adapter = ChatAdapter(messageList,fromId)
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
        if(fromId != null && toId != null){
            FirebaseDatabase.getInstance().getReference("Messenger")
                .child("userMessages")
                .child(fromId!!)
                .child(toId!!)
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


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_chat,menu)
        return true
    }

    private fun setOnClickListener(){
        binding.btnSend.setOnClickListener(this)
        binding.btnVoiceMsg.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performSendTextMsg() {

        if(fromId != null && toId != null){
            val fromReference = FirebaseDatabase.getInstance().getReference("Messenger").child("userMessages")
                .child(fromId!!)
                .child(toId!!)
                .push()

            val toReference = FirebaseDatabase.getInstance().getReference("Messenger").child("userMessages")
                .child(toId!!)
                .child(fromId!!)
                .push()

            val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                val calendar = Calendar.getInstance()
                LocalDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1, // Month is 0-based
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND)
                )
            }

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
                .child(fromId!!)
                .child(toId!!)
                .setValue(messageData)

            val latestMessageToRef = FirebaseDatabase.getInstance().getReference("Messenger")
                .child("LatestMessage")
                .child(toId!!)
                .child(fromId!!)
                .setValue(messageData)
        }

    }
}