package com.amri.emploihunt.messenger

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.model.LatestChatMsg
import com.amri.emploihunt.model.User
import com.bumptech.glide.Glide

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
            .inflate(R.layout.row_latest_chat, parent, false)
        return LatestMsgViewHolder(view)
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
            }
        }
    }

    inner class LatestMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val profileImg: ImageView = itemView.findViewById(R.id.profileImg)
        private val personName: TextView = itemView.findViewById(R.id.personName)
        private val latestMessage: TextView = itemView.findViewById(R.id.latestMsg)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        private var user : User?= null
        fun bind(messageData: LatestChatMsg, userId: String){

            this.user = messageData.user
            latestMessage.text = messageData.latestChatMsg.message
            
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

        }
    }

    interface OnChatJClickListener{
        fun onChatJClick(position: Int, user: User)
        fun onChatJLongClick(position: Int, user: User)
    }

    interface GetPosition{
        fun onGetPosition(user: User)
    }
}