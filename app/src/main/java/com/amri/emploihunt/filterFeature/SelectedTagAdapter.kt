package com.amri.emploihunt.filterFeature

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.google.android.material.button.MaterialButton

class SelectedTagAdapter(
    private val tagList: MutableList<FilterTagData>,
    private val activity: AppCompatActivity,
    private val onSelectedTagClickListener: OnSelectedTagClickListener,
    /*private val attribute: Int*/
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_filter_tag,parent,false)

        return SelectedTagViewHolder(view,onSelectedTagClickListener)
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val tag = tagList[position]

        when(holder){
            is SelectedTagViewHolder -> {
                holder.bind(tag.tagName!!,tag.attribute!!)
            }
        }
    }

    inner class SelectedTagViewHolder(itemView: View,onSelectedTagClickListener: OnSelectedTagClickListener) : RecyclerView.ViewHolder(itemView){
        private val btnFilterTag: MaterialButton = itemView.findViewById(R.id.btnFilterTag)
        fun bind(tag: String, attribute: Int){
            if(attribute == 4){
                btnFilterTag.text = tag.plus(" LPA +")
            }
            else{
                btnFilterTag.text = tag
            }
            val newTintColor = ContextCompat.getColor(activity, R.color.blue)
            val newTextColor = ContextCompat.getColor(activity, R.color.white)
            btnFilterTag.backgroundTintList =
                ColorStateList.valueOf(newTintColor)
            btnFilterTag.setTextColor(newTextColor)
        }
        init {
            val isChecked = false
            btnFilterTag.setOnClickListener {
                onSelectedTagClickListener.onSelectedTagClick(absoluteAdapterPosition,/*attribute,*/isChecked)
                
                btnFilterTag.isSelected = !btnFilterTag.isSelected
            }
            btnFilterTag.setOnLongClickListener {
                onSelectedTagClickListener.onSelectedTagLongClick(absoluteAdapterPosition,/*attribute,*/isChecked)
                btnFilterTag.isSelected = !btnFilterTag.isSelected
                return@setOnLongClickListener true
            }
        }
    }

    interface OnSelectedTagClickListener{
        fun onSelectedTagClick(position: Int,/* attribute: Int,*/ isChecked: Boolean)
        fun onSelectedTagLongClick(position: Int, /*attribute: Int,*/ isChecked: Boolean)
    }
}