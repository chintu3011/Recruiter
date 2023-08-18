package com.amri.emploihunt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

//import kotlinx.android.synthetic.main.chat_from_row.view.*
//import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatAdapter(private val messages: List<MessageData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val VIEW_TYPE_FROM = 1
        private const val VIEW_TYPE_TO = 2
    }


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]


//        return VIEW_TYPE_TO
        return if (message.fromId == FirebaseAuth.getInstance().currentUser?.uid) {
            VIEW_TYPE_FROM
        } else {
            VIEW_TYPE_TO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FROM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_from_row, parent, false)
            FromViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_to_row, parent, false)
            ToViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is FromViewHolder -> {
                holder.msgFrom.text = message.message
                holder.timeStampFrom.text = message.timeStamp
                // Set other views specific to 'chat_from_row.xml'
            }
            is ToViewHolder -> {
                holder.msgTo.text = message.message
                holder.timeStampTo.text = message.timeStamp
                // Set other views specific to 'chat_to_row.xml'
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class FromViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var msgFrom: TextView = itemView.findViewById(R.id.msg)
        var timeStampFrom: TextView = itemView.findViewById(R.id.timeStamp)
    }
    inner class ToViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var msgTo: TextView = itemView.findViewById(R.id.msg)
        var timeStampTo: TextView = itemView.findViewById(R.id.timeStamp)
        var profileImg :ImageView = itemView.findViewById(R.id.profileImg)
    }
}