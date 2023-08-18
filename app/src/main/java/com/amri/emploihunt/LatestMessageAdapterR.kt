package com.amri.emploihunt

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LatestMessageAdapterR(
    private val messages: MutableList<MessageData>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object{
        private val DEFAULT_PROFILE_IMAGE_RESOURCE = R.drawable.profile_default_image
        private const val TAG = "LatestMessageAdapterR"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_old_chat, parent, false)
        return LatestMsgViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]
        when(holder){
            is LatestMsgViewHolder -> {
                holder.latestMessage.text = message.message
                val chatPartnerId:String = if(message.fromId == FirebaseAuth.getInstance().currentUser?.uid){
                    message.toId!!
                } else{
                    message.fromId!!
                }

                var user : UsersRecruiter

                FirebaseDatabase.getInstance().getReference("Users")
                    .child("Recruiter")
                    .child(chatPartnerId)
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d(TAG,chatPartnerId)
                            if (snapshot.exists()){
                                user = snapshot.getValue(UsersRecruiter::class.java)!!
                                holder.cardView.setOnClickListener {
                                    val intent = Intent(activity,ChatBoardActivity::class.java)
                                    intent.putExtra("userObjectR",user)
                                    activity.startActivity(intent)
                                    activity.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left)
                                }
                                val fullName =  user?.userFName + " " + user?.userLName
                                holder.personName.text = fullName
                                if(!user?.userProfileImg.isNullOrEmpty()){
                                    Log.d(TAG,"${user?.userProfileImg}")
                                    Glide.with(holder.itemView).load(user?.userProfileImg).into(holder.profileImg);
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }
        }
    }

    inner class LatestMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val profileImg: ImageView = itemView.findViewById(R.id.profileImg)
        val personName: TextView = itemView.findViewById(R.id.personName)
        val latestMessage: TextView = itemView.findViewById(R.id.latestMsg)
        val cardView:CardView = itemView.findViewById(R.id.cardView)

    }
}