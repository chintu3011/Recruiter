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
import com.amri.emploihunt.jobSeekerSide.UsersJobSeeker
import com.amri.emploihunt.model.LatestChatMsg
import com.amri.emploihunt.model.User
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LatestMessageAdapterJ(
    private val messages: MutableList<LatestChatMsg>,
    private val activity: Activity,
    private val userId : String?,
    private val onChatJClickListener: OnChatJClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object{
        private const val TAG = "LatestMessageAdapterJ"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_old_chat, parent, false)
        return LatestMsgViewHolder(view,onChatJClickListener)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${messages.size}")
        return messages.size
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val messageData = messages[position]
        when(holder){
            is LatestMsgViewHolder -> {
                if(userId != null){
                    holder.bind(messageData,userId)

                }
                /*holder.latestMessage.text = messageData.message
                val chatPartnerId: String =
                    if (messageData.fromId == FirebaseAuth.getInstance().currentUser?.uid) {
                        messageData.toId!!
                    } else {
                        messageData.fromId!!
                    }

                var user : UsersJobSeeker

                FirebaseDatabase.getInstance().getReference("Users")
                    .child("Job Seeker")
                    .child(chatPartnerId)
                    .addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                user = snapshot.getValue(UsersJobSeeker::class.java)!!
                                user.userProfileImg = ""
                                user.userProfileBannerImg = ""
                                holder.cardView.setOnClickListener {
                                    val intent = Intent(activity, ChatBoardActivity::class.java)
                                    intent.putExtra("userObjectJ",user)
                                    activity.startActivity(intent)
                                    activity.overridePendingTransition(
                                        R.anim.slide_in_left,
                                        R.anim.slide_out_left
                                    )
                                }
                                val fullName =  user.userFName + " " + user.userLName
                                holder.personName.text = fullName
                                if(user.userProfileImgUri.isNotEmpty()){
                                    Log.d(TAG, user.userProfileImgUri)
                                    Glide.with(holder.itemView).load(user.userProfileImgUri).into(holder.profileImg)
                                }
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })*/
            }
        }
    }

    inner class LatestMsgViewHolder(itemView: View, onChatJClickListener: OnChatJClickListener) : RecyclerView.ViewHolder(itemView){
        private val profileImg: ImageView = itemView.findViewById(R.id.profileImg)
        private val personName: TextView = itemView.findViewById(R.id.personName)
        private val latestMessage: TextView = itemView.findViewById(R.id.latestMsg)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        private var user : User?= null
        fun bind(messageData: LatestChatMsg, userId: String){

            this.user = messageData.user
            latestMessage.text = messageData.latestChatMsg.message
            /*val chatPartnerId: String =
                if (messageData.latestChatMsg.fromId == userId*//*FirebaseAuth.getInstance().currentUser?.uid*//*) {
                    messageData.latestChatMsg.toId!!
                } else {
                    messageData.latestChatMsg.fromId!!
                }*/


            val fullName = messageData.user.vFirstName + " " + messageData.user.vLastName
            personName.text = fullName
            if (messageData.user.tProfileUrl != null) {
                /*Log.d(TAG, user.userProfileImgUri)*/
                Glide.with(itemView)
                    .load(messageData.user.tProfileUrl)
                    .placeholder(R.drawable.profile_default_image)
                    .into(profileImg)
            }
            cardView.setOnClickListener {
                onChatJClickListener.onChatJClick(absoluteAdapterPosition,messageData.user)
            }
            cardView.setOnLongClickListener {
                onChatJClickListener.onChatJLongClick(absoluteAdapterPosition,messageData.user)
                return@setOnLongClickListener true
            }
            /*getChatPartner(chatPartnerId){ user ->
                this.user = user

                val fullName = user.userFName + " " + user.userLName
                personName.text = fullName
                if (user.userProfileImgUri.isNotEmpty()) {
                    *//*Log.d(TAG, user.userProfileImgUri)*//*
                    Glide.with(itemView)
                        .load(user.userProfileImgUri)
                        .placeholder(R.drawable.profile_default_image)
                        .into(profileImg)
                }
                cardView.setOnClickListener {
                    onChatJClickListener.onChatJClick(absoluteAdapterPosition, user)
                }
            }*/

        }

        /*private fun getChatPartner(chatPartnerId:String, completion: (UsersJobSeeker) -> Unit) {

            FirebaseDatabase.getInstance().getReference("Users")
                .child("Job Seeker")
                .child(chatPartnerId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            val user = snapshot.getValue(UsersJobSeeker::class.java)!!
                            user.userProfileImg = ""
                            user.userProfileBannerImg = ""
                            completion(user)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, error.message)
                        Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT).show()
                    }

                })

        }*/
    }

    interface OnChatJClickListener{
        fun onChatJClick(position: Int, user: User)
        fun onChatJLongClick(position: Int, user: User)
    }
}