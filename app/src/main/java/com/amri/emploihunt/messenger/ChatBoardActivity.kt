package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityChatBoardBinding
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.model.User
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.IMG_TYPE
import com.amri.emploihunt.util.PDF_TYPE
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.TXT_TYPE
import com.amri.emploihunt.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignTop
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ChatBoardActivity : BaseActivity() ,OnClickListener{



    private lateinit var binding: ActivityChatBoardBinding
    private lateinit var adapter:ChatAdapter

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

//    private lateinit var popupWindow:PopupWindow

    private lateinit var prefManager: SharedPreferences

    private var user:User ?= null

    private lateinit var balloon: Balloon

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG,"chat board started")

        val window: Window = this@ChatBoardActivity.window
        window.statusBarColor = ContextCompat.getColor(this@ChatBoardActivity,R.color.colorPrimary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        prefManager = prefManager(this)

        userType = prefManager.get(ROLE,0)
        fromId = prefManager.get(FIREBASE_ID)

        Log.d(TAG,"$fromId :: $userType")
        messageList = mutableListOf()


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
        binding.recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        setOnClickListener()

        textWatcherForMsgEditText()

        listenerForMessages {
            Log.d(TAG, messageList.size.toString())
            adapter = ChatAdapter(this,messageList,fromId)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
            adapter.notifyDataSetChanged()
        }

        /*popupWindow = PopupWindow(this@ChatBoardActivity)
        val popupView = layoutInflater.inflate(R.layout.chat_board_add_popup,null)
        popupWindow.contentView = popupView
        val btnAttach = popupView.findViewById<ShapeableImageView>(R.id.btnAttach)
        btnAttach.setOnClickListener {
            selectPdf()
            popupWindow.dismiss()
        }
        val btnCamara = popupView.findViewById<ShapeableImageView>(R.id.btnCamara)
        btnCamara.setOnClickListener {
            selectImg()
            popupWindow.dismiss()
        }*/

        /*val addPopupLayout: View = layoutInflater.inflate(com.amri.emploihunt.R.layout.chat_board_add_popup, null)
        val cardView = addPopupLayout.findViewById<CardView>(R.id.cardView)

        val cardHeight:Int = cardView.height
        val cardWidth:Int = cardView.width*/

       /* cardView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // The CardView's height and width are now available
                cardHeight = cardView.height
                cardWidth = cardView.width

                // Do something with these values

                // Remove the listener to prevent multiple callbacks
                cardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })*/
        balloon = createBalloon(baseContext){
            setLayout(R.layout.chat_board_add_popup)
            setArrowSize(10)
            setArrowOrientation(ArrowOrientation.TOP)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowPosition(0.5f)
            setWidth(190)
            setHeight(150)
            setCornerRadius(30f)
            setBackgroundColor(ContextCompat.getColor(this@ChatBoardActivity, R.color.white))
            setBalloonAnimation(BalloonAnimation.ELASTIC)
            setLifecycleOwner(lifecycleOwner)
            build()
        }

        val btnAttach = balloon.getContentView().findViewById<ShapeableImageView>(R.id.btnAttach)
        btnAttach.setOnClickListener {
            selectPdf()
            balloon.dismiss()
        }
        val btnCamara = balloon.getContentView().findViewById<ShapeableImageView>(R.id.btnCamara)
        btnCamara.setOnClickListener {
            selectImg()
            balloon.dismiss()
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
                       /* val messageData = snapshot.getValue(MessageData::class.java)
                        if (messageData != null ){
                            messageList.add(messageData)
                            Log.d("messageData", messageData.message.toString())
                            completion()
                        }*/
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
        binding.btnAdd.setOnClickListener (this)
        /*binding.btnAttach.setOnClickListener(this)
        binding.btnCamara.setOnClickListener(this)*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSend -> {
                Log.d(TAG,"Attempt to send Text message")
                performSendTextMsg(TXT_TYPE,"",binding.inputMessage.text.toString())
            }
            R.id.btnVoiceMsg -> {
                openVoice()
                Log.d(TAG,"Attempt to send Voice Message message")
            }
            R.id.btnAdd -> {
                
                binding.btnAdd.showAlignTop(balloon)

               /* val anchorViewLocation = IntArray(2)
                binding.btnAdd.getLocationOnScreen(anchorViewLocation)
                val yOffset = anchorViewLocation[1] - popupWindow.height

                Show the pop-up window with the calculated y-offset
                popupWindow.showAtLocation(binding.root, Gravity.NO_GRAVITY, 0, yOffset)*/
            }
            /*R.id.btnAttach -> {
                selectPdf()
            }
            R.id.btnCamara -> {
                selectImg()
            }*/

        }
    }

    private fun selectImg() {
        val imgIntent = Intent(Intent.ACTION_GET_CONTENT)
        imgIntent.type = "image/*"
        imgIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(imgIntent,24)
    }

    private fun selectPdf() {
        val pdfIntent = Intent(Intent.ACTION_GET_CONTENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pdfIntent, 12)
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

    @SuppressLint("Range", "NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                12 -> if (resultCode == RESULT_OK) {

                    val pdfUri: Uri = data?.data!!
                    val pdfFile = Utils.convertUriToPdfFile(this@ChatBoardActivity, pdfUri)!!

                    if (pdfUri.toString().startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                pdfUri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                val pdfName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val storageRef = Firebase.storage.reference
                                    val path = "docs/chatDocs/$fromId/$toId/$pdfName"
                                    val pdfRef = storageRef.child(path)

                                    pdfRef.putFile(pdfUri)
                                        .addOnProgressListener {
                                            binding.fileName.text = pdfName
                                            binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.disable_blue))
                                            binding.uploadProgressLayout.visibility = VISIBLE
                                            binding.btnSend.isEnabled = false
                                            binding.btnVoiceMsg.isEnabled = false
                                            val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                                            binding.uploadProgressBar.progress = progress
                                        }
                                        .addOnSuccessListener {

                                            pdfRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                binding.fileName.text = ""
                                                binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
                                                binding.uploadProgressLayout.visibility = GONE
                                                binding.btnSend.isEnabled = true
                                                binding.btnVoiceMsg.isEnabled = true
                                                performSendTextMsg(PDF_TYPE, downloadUri.toString(),pdfName)
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            binding.fileName.text = ""
                                            binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
                                            binding.uploadProgressLayout.visibility = GONE
                                            binding.btnSend.isEnabled = true
                                            binding.btnVoiceMsg.isEnabled = true
                                            makeToast("Document is not stored successfully",0)
                                            Log.e(TAG, "onActivityResult: error while storing document $exception" )
                                        }
                                }
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                }

                24 -> if(resultCode == RESULT_OK) {

                    val imageUri = data?.data!!
                    val imgFile = Utils.convertUriToPdfFile(this@ChatBoardActivity, imageUri)!!

                    if (imageUri.toString().startsWith("content://")) {
                        var myCursor: Cursor? = null
                        try {
                            myCursor = this.contentResolver.query(
                                imageUri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (myCursor != null && myCursor.moveToFirst()) {
                                val imgName = myCursor.getString(myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val storageRef = Firebase.storage.reference
                                    val path = "images/chatImages/$fromId/$toId/$imgName}"
                                    val imageRef = storageRef.child(path)

                                    imageRef.putFile(imageUri)
                                        .addOnProgressListener {
                                            binding.fileName.text = imgName
                                            binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.disable_blue))
                                            binding.uploadProgressLayout.visibility = VISIBLE
                                            binding.btnSend.isEnabled = false
                                            binding.btnVoiceMsg.isEnabled = false
                                            val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                                            binding.uploadProgressBar.progress = progress
                                        }
                                        .addOnSuccessListener {

                                            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                binding.fileName.text = ""
                                                binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
                                                binding.uploadProgressLayout.visibility = GONE
                                                binding.btnSend.isEnabled = true
                                                binding.btnVoiceMsg.isEnabled = true
                                                performSendTextMsg(IMG_TYPE,
                                                    downloadUri.toString(),imgName)
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            binding.fileName.text = ""
                                            binding.btnVoiceMsg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
                                            binding.uploadProgressLayout.visibility = GONE
                                            binding.btnSend.isEnabled = true
                                            binding.btnVoiceMsg.isEnabled = true
                                            makeToast("Img is not stored successfully",0)
                                            Log.e(TAG, "onActivityResult: error while storing Img $exception" )
                                        }
                                }
                            }
                        } finally {
                            myCursor?.close()
                        }
                    }
                }
                200 -> if (resultCode == RESULT_OK) {
                    val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val query = matches!![0]

                    if (matches.isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            performSendTextMsg(TXT_TYPE,"",query.orEmpty())
                        }
                    }
                    else{
                        Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun performSendTextMsg(msgType:Int,docUri:String,message: String) {

        if(fromId != null && toId != null){

            /** this reference helps in getting messages which are send by logged in user */
            val fromReference = FirebaseDatabase.getInstance().getReference("Messenger").child("userMessages")
                .child(fromId!!)
                .child(toId!!)
                .push()

            /** this reference helps in getting messages which are send by chat partner */
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
                msgType,
                docUri,
                message,
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


            /** this will help to get latest msg send by logged in user*/
            val latestMessageFromRef = FirebaseDatabase.getInstance().getReference("Messenger")
                .child("LatestMessage")
                .child(fromId!!)
                .child(toId!!)
                .setValue(messageData)

            /** this will help to get latest msg send by chat partner*/
            val latestMessageToRef = FirebaseDatabase.getInstance().getReference("Messenger")
                .child("LatestMessage")
                .child(toId!!)
                .child(fromId!!)
                .setValue(messageData)

        }

    }
}