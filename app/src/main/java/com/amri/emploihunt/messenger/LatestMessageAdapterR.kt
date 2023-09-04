package com.amri.emploihunt.messenger

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.model.User
import com.bumptech.glide.Glide
import com.amri.emploihunt.recruiterSide.UsersRecruiter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LatestMessageAdapterR(
    private val messages: MutableList<MessageData>,
    private val activity: Activity,
    private val onChatRClickListener: OnChatRClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object{
        private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
        private const val TAG = "LatestMessageAdapterR"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_latest_chat, parent, false)
        return LatestMsgViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val messageData = messages[position]
        when(holder){
            is LatestMsgViewHolder -> {
                holder.bind(messageData)
                /*holder.latestMessage.text = message.message
                val chatPartnerId:String = if(message.fromId == FirebaseAuth.getInstance().currentUser?.uid){
                    message.toId!!
                } else{
                    message.fromId!!
                }
                var user : UsersRecruiter

                FirebaseDatabase.getInstance().getReference("Users")
                    .child("Recruiter")
                    .child(chatPartnerId)
                    .addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG, chatPartnerId)
                            if (snapshot.exists()){
                                user = snapshot.getValue(UsersRecruiter::class.java)!!

                                val fullName =  user.userFName + " " + user.userLName
                                holder.personName.text = fullName
                                if(user.userProfileImgUri.isNotEmpty()){
                                    Log.d(TAG, user.userProfileImgUri)
                                    Glide.with(holder.itemView).load(user.userProfileImgUri).into(holder.profileImg)
                                }
                                user.userProfileImg = ""
                                user.userProfileBannerImg = ""
                                holder.cardView.setOnClickListener {
                                    val intent = Intent(activity, ChatBoardActivity::class.java)
                                    intent.putExtra("userObjectR",user)
                                    activity.startActivity(intent)
                                    activity.overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_left
                                    )
                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })*/
            }
        }
    }

    inner class LatestMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val profileImg: ImageView = itemView.findViewById(R.id.profileImg)
        private val personName: TextView = itemView.findViewById(R.id.personName)
        private val latestMessage: TextView = itemView.findViewById(R.id.latestMsg)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        private var user : User?= null
        fun bind(messageData: MessageData){
            latestMessage.text = messageData.message
            val chatPartnerId: String =
                if (messageData.fromId == FirebaseAuth.getInstance().currentUser?.uid) {
                    messageData.toId!!
                } else {
                    messageData.fromId!!
                }
            getChatPartner(chatPartnerId){ user ->
                /*this.user = user*/

                val fullName = user.userFName + " " + user.userLName
                personName.text = fullName
                if (user.userProfileImgUri.isNotEmpty()) {
                    /*Log.d(TAG, user.userProfileImgUri)*/
                    Glide.with(itemView)
                        .load(user.userProfileImgUri)
                        .placeholder(R.drawable.profile_default_image)
                        .into(profileImg)
                }
                cardView.setOnClickListener {
                    /*onChatRClickListener.onChatRClick(absoluteAdapterPosition, user)*/
                }
            }

        }

        init {
            cardView.setOnLongClickListener {
                user?.let {
                    onChatRClickListener.onChatRLongClick(absoluteAdapterPosition, it)
                }
                return@setOnLongClickListener true
            }
        }

        /** Need to implement which can get chatPartner From API*/

        private fun getChatPartner(chatPartnerId:String, completion: (UsersRecruiter) -> Unit) {
            FirebaseDatabase.getInstance().getReference("Users")
                .child("Recruiter")
                .child(chatPartnerId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val user = snapshot.getValue(UsersRecruiter::class.java)!!
                            user.userProfileImg = ""
                            user.userProfileBannerImg = ""
                            completion(user)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, error.message)
                        Toast.makeText(activity,"Something went wrong", Toast.LENGTH_SHORT).show()
                    }

                })

        }

    }

    interface OnChatRClickListener{
        fun onChatRClick(position: Int,usersRecruiter: User)
        fun onChatRLongClick(position: Int,usersRecruiter: User)
    }
}