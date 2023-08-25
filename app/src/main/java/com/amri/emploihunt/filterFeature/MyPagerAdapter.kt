package com.amri.emploihunt.filterFeature

import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.google.android.material.imageview.ShapeableImageView

class MyPagerAdapter(
    private val adapterList: MutableList<FilterTagAdapter>,
    private val activity: AppCompatActivity,
    private val onSearchQueryChanged: OnSearchQueryChanged,
    private val filterCategory: Int
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val JOB = 1
        private const val APPLICATION = 2
    }
/*    override fun getItemViewType(position: Int): Int {
        return when (filterCategory) {
            JOB -> {
                JOB
            }
            APPLICATION -> {
                APPLICATION
            }
            else -> {
                -1
            }
        }
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_recycler_view,parent,false)

        return TagViewViewHolder(view,onSearchQueryChanged)
    }

    override fun getItemCount(): Int {
        return adapterList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val adapter: FilterTagAdapter = adapterList[position]

        when(holder){
            is TagViewViewHolder -> {
                holder.bind(adapter,activity,filterCategory,position+1)
            }
        }
    }

    inner class TagViewViewHolder(itemView: View, onSearchQueryChanged: OnSearchQueryChanged) : RecyclerView.ViewHolder(itemView){

        private val recyclerView:RecyclerView = itemView.findViewById(R.id.recyclerView)
        private val btnSearch:ShapeableImageView = itemView.findViewById(R.id.btnSearch)
        private val searchView:SearchView = itemView.findViewById(R.id.searchView)

        private var attribute = -1

        fun bind(adapter: FilterTagAdapter, activity: AppCompatActivity, filterCategory: Int, attribute: Int){

            this.attribute = attribute
            val gridLayoutManagerDomain = GridLayoutManager(activity, 2)
            setSpanLookUP(gridLayoutManagerDomain)
            recyclerView.layoutManager = gridLayoutManagerDomain
            recyclerView.adapter = adapter

            when (filterCategory) {
                JOB -> {
                    when(attribute){
                        1 -> {
                            searchView.queryHint = "Search Domain"
                        }

                        2 -> {
                            searchView.queryHint = "Search Location"
                        }

                        3 -> {
                            searchView.queryHint = "Search Working Mode"
                        }

                        4 -> {
                            searchView.queryHint = "Search Package"
                        }

                        else -> {
                            searchView.queryHint = "Search Tags"
                            Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                APPLICATION -> {
                    when(attribute){
                        1 -> {
                            searchView.queryHint = "Search Domain"
                        }

                        2 -> {
                            searchView.queryHint = "Search Location"
                        }

                        3 -> {
//                            searchView.queryHint = "Search Package"
                            searchView.queryHint = "Search Working Mode"
                        }

                        4 -> {
                            searchView.queryHint = "Search Package"
                        }

                        else -> {
                            searchView.queryHint = "Search Tags"
                            Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else -> {
                    Toast.makeText(activity,"Filter Category not found",Toast.LENGTH_SHORT).show()
                }
            }



        }

        init {
            var isChecked = false
            btnSearch.setOnClickListener {
                btnSearch.isSelected = !btnSearch.isSelected
                if(!isChecked){
                    btnSearch.setImageResource(R.drawable.ic_cancle)
                    searchView.visibility = VISIBLE
                    searchView.requestFocus()
                    isChecked = true
                }
                else{
                    searchView.visibility = GONE
                    btnSearch.setImageResource(R.drawable.ic_search)
                    isChecked = false
                }
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    onSearchQueryChanged.searchTags(newText.orEmpty(),attribute,filterCategory)
                    return true
                }

            })

        }
    }


    private fun setSpanLookUP(gridLayoutManager: GridLayoutManager) {
        gridLayoutManager.spanSizeLookup = object :GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return if (position % 3 == 0) 2 else 1
            }
        }
    }

    interface OnSearchQueryChanged{
        fun searchTags(query:String,attribute:Int,filterCategory: Int)
    }

}