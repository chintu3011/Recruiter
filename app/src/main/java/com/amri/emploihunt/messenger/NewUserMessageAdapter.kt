package com.amri.emploihunt.messenger

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.model.User
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView


class NewUserMessageAdapter(
    /*private val usersList: MutableList<Any>,*/
    private val usersList: MutableList<User>,
    private val activity: AppCompatActivity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        /*const val VIEW_TYPE_USER_J = 0
        const val VIEW_TYPE_USER_R = 1*/
        const val TAG = "NewUserMessageAdapter"
    }

    /*override fun getItemViewType(position: Int): Int {
        return when (usersList[position]) {
            is UsersJobSeeker -> VIEW_TYPE_USER_J
            is UsersRecruiter -> VIEW_TYPE_USER_R
            else -> throw IllegalArgumentException("Unknown user type at position $position")
        }
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_new_chat, parent, false)
        return UserViewHolder(view)
        /*return when (viewType) {
            VIEW_TYPE_USER_J -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_new_chat, parent, false)
                UserJViewHolder(view)
            }
            VIEW_TYPE_USER_R -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_new_chat, parent, false)
                UserRViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }*/
    }

    override fun getItemCount(): Int {
        Log.d(TAG,"${usersList.size}")
        return usersList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = usersList[position]
        when (holder) {
            is UserViewHolder -> {
                holder.bind(user)
            }
            else -> {
                Toast.makeText(activity,activity.getString(R.string.incorrect_view_holder),Toast.LENGTH_SHORT).show()
                Log.d(TAG, activity.getString(R.string.incorrect_view_holder, holder))
            }
            /*is UserJViewHolder -> {

                val user = user as UsersJobSeeker
                Log.d("userData", user.toString())

                val userFullName = user.userFName +" "+user.userLName
                Log.d("userName", userFullName)
                holder.personName.text = userFullName
                holder.tagLine.text = user.userTagLine
                if (user.userProfileImgUri.isNotEmpty()){
                    Glide.with(holder.profileImg.context).load(user.userProfileImgUri).into(holder.profileImg)
                }

                holder.cardView.setOnClickListener{

                    val selectedUser = usersList[position] as UsersJobSeeker
                    selectedUser.userProfileImg = ""
                    selectedUser.userProfileBannerImg = ""
                    val intent = Intent(activity, ChatBoardActivity::class.java)
                    intent.putExtra("userObjectJ", selectedUser)
                    Log.d("NewUsersMessageActivity","selected user = $selectedUser")
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
            is UserRViewHolder -> {
                val user = user as UsersRecruiter
                Log.d("userData", user.toString())

                val userFullName = user.userFName +" "+user.userLName
                Log.d("userName", userFullName)
                holder.personName.text = userFullName
                holder.tagLine.text = user.userTagLine
                if (user.userProfileImgUri.isNotEmpty()){
                    Glide.with(holder.profileImg.context).load(user.userProfileImgUri).into(holder.profileImg)
                }

                holder.cardView.setOnClickListener{
                    val selectedUser = usersList[position] as UsersRecruiter
                    selectedUser.userProfileImg = ""
                    selectedUser.userProfileBannerImg = ""
                    val intent = Intent(activity, ChatBoardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("userObjectR", selectedUser)
                    Log.d("NewUsersMessageActivity","selected user = $selectedUser")
                    activity.startActivity(intent)
                    activity.finish()
                }
            }*/
        }
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val personName:MaterialTextView = itemView.findViewById(R.id.personName)
        private val tagLine:TextView = itemView.findViewById(R.id.tagLine)
        private val profileImg:ImageView = itemView.findViewById(R.id.profileImg)
        private val cardView :MaterialCardView = itemView.findViewById(R.id.cardView)

        fun bind(user: User) {
            Log.d("userData", user.toString())

            val userFullName = user.vFirstName +" "+user.vLastName
            Log.d("userName", userFullName)
            personName.text = userFullName
            tagLine.text = user.tTagLine
            if ( user.tProfileUrl != null ){
                Glide.with(profileImg.context).load(user.tProfileUrl).placeholder(R.drawable.default_person_icon).into(profileImg)
            }

            cardView.setOnClickListener{
                val intent = Intent(activity, ChatBoardActivity::class.java)
                intent.putExtra("userObject", user)
                Log.d("NewUsersMessageActivity", "selected user = $user")
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
    /*inner class UserJViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val personName:MaterialTextView = itemView.findViewById(R.id.personName)
        val tagLine:TextView = itemView.findViewById(R.id.tagLine)
        val profileImg:ImageView = itemView.findViewById(R.id.profileImg)
        val cardView :MaterialCardView = itemView.findViewById(R.id.cardView)
    }
    inner class UserRViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val personName: MaterialTextView = itemView.findViewById(R.id.personName)
        val tagLine:MaterialTextView = itemView.findViewById(R.id.tagLine)
        val profileImg:ImageView = itemView.findViewById(R.id.profileImg)
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
    }*/
}