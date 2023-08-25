package com.amri.emploihunt.filterFeature

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R

import com.google.android.material.button.MaterialButton

class FilterTagAdapter(
    private val tagList: MutableList<String>,
    private val onTagClickListener: OnTagClickListener,
    private val attribute: Int

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val TAG = "FilterTagAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_filter_tag,parent,false)

        return TagViewHolder(view,onTagClickListener)
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tag = tagList[position]

        when (holder) {
            is TagViewHolder -> {
                holder.bind(tag)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class TagViewHolder(itemView: View, onTagClickListener: OnTagClickListener) : RecyclerView.ViewHolder(itemView){

        private val btnFilterTag:MaterialButton = itemView.findViewById(R.id.btnFilterTag)
        fun bind (tag:String){
            btnFilterTag.text = tag
        }
        init {
            btnFilterTag.setOnClickListener {
                onTagClickListener.onTagClick(absoluteAdapterPosition,attribute)
            }
            btnFilterTag.setOnLongClickListener {
                onTagClickListener.onTagLongClick(absoluteAdapterPosition,attribute)
                return@setOnLongClickListener true
            }
        }

    }

    interface OnTagClickListener{
        fun onTagClick(position: Int, attribute: Int)
        fun onTagLongClick(position: Int, attribute: Int)
    }

}