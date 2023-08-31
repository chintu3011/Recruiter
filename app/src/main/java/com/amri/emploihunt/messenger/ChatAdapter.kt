package com.amri.emploihunt.messenger

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.util.IMG_TYPE
import com.amri.emploihunt.util.PDF_TYPE
import com.amri.emploihunt.util.TXT_TYPE
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class ChatAdapter(
    private val messages: List<MessageData>,
    private val userId: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val TAG = "ChatAdapter"
        
        private const val VIEW_TYPE_TXT_FROM = 1
        private const val VIEW_TYPE_TXT_TO = 2
        private const val VIEW_TYPE_PDF_FROM = 3
        private const val VIEW_TYPE_PDF_TO = 4
        private const val VIEW_TYPE_IMG_FROM = 5
        private const val VIEW_TYPE_IMG_TO = 6
    }


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return if (message.fromId == userId) {

            when(message.msgType){
                TXT_TYPE -> {
                    VIEW_TYPE_TXT_FROM
                }
                PDF_TYPE -> {
                    VIEW_TYPE_PDF_FROM
                }
                IMG_TYPE -> {
                    VIEW_TYPE_IMG_FROM
                }
                else -> {
                    Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                    -1
                }
            }
            
        } else {
            when(message.msgType) {
                TXT_TYPE -> {
                    VIEW_TYPE_TXT_TO
                }
                PDF_TYPE -> {
                    VIEW_TYPE_PDF_TO
                }
                IMG_TYPE -> {
                    VIEW_TYPE_IMG_TO
                }
                else -> {
                    Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                    -1
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TXT_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_txt_row, parent, false)
                FromTxtViewHolder(view)
            }
            VIEW_TYPE_TXT_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_txt_row, parent, false)
                ToTxtViewHolder(view)
            }
            VIEW_TYPE_PDF_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_docs_row, parent, false)
                FromPdfViewHolder(view)
            }
            VIEW_TYPE_PDF_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_docs_row, parent, false)
                ToPdfViewHolder(view)
            }
            VIEW_TYPE_IMG_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_img_row, parent, false)
                FromImgViewHolder(view)
            }
            VIEW_TYPE_IMG_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_img_row, parent, false)
                ToImgViewHolder(view)
            }
            else -> {
                Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_txt_row, parent, false)
                FromTxtViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
         holder.setIsRecyclable(false)
        when (holder) {
            is FromTxtViewHolder -> {
                holder.bind(message)
            }
            is ToTxtViewHolder -> {
                holder.bind(message)
            }
            is FromPdfViewHolder -> {
                holder.bind(message)
            }
            is ToPdfViewHolder -> {
                holder.bind(message)
            }
            is FromImgViewHolder -> {
                holder.bind(message)
            }
            is ToImgViewHolder -> {
                holder.bind(message)
            }
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    inner class FromTxtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var msg: TextView = itemView.findViewById(R.id.msg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            msg.text = messageData.message
            timeStamp.text = messageData.timeStamp
        }
    }
    inner class ToTxtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var msg: TextView = itemView.findViewById(R.id.msg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            msg.text = messageData.message
            timeStamp.text = messageData.timeStamp
        }
    }
    inner class FromPdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var pdfName: TextView = itemView.findViewById(R.id.pdfName)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
        fun bind(messageData: MessageData){
            pdfName.text = messageData.message
            timeStamp.text = messageData.timeStamp

            val pdfUri = messageData.docUri
        }

    }
    inner class ToPdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var pdfName: TextView = itemView.findViewById(R.id.pdfName)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            pdfName.text = messageData.message
            timeStamp.text = messageData.timeStamp

            val pdfUri = messageData.docUri
        }

    }
    inner class FromImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val btnShowImg:ShapeableImageView = itemView.findViewById(R.id.btnShowImg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
        fun bind(messageData: MessageData){
            Glide.with(btnShowImg.context).load(messageData.docUri?.toUri()).placeholder(R.drawable.default_img).into(btnShowImg)
            timeStamp.text = messageData.timeStamp

            val imgUri = messageData.docUri
        }

    }
    inner class ToImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val btnShowImg:ShapeableImageView = itemView.findViewById(R.id.btnShowImg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
        fun bind(messageData: MessageData){
            Glide.with(btnShowImg.context).load(messageData.docUri?.toUri()).placeholder(R.drawable.default_img).into(btnShowImg)
            timeStamp.text = messageData.timeStamp

            val imgUri = messageData.docUri
        }

    }
}