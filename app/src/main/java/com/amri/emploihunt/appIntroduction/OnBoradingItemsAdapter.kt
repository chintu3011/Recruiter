package com.amri.emploihunt.appIntroduction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R

class OnBoradingItemsAdapter(private val onboardingItems: List<OnBoardingItem>) : RecyclerView.Adapter<OnBoradingItemsAdapter.OnBoardingItemViewHolder>() {

    

    inner class OnBoardingItemViewHolder(view : View ) : RecyclerView.ViewHolder(view){

        private val imageOnboarding = view.findViewById<ImageView>(R.id.imageOnboarding)
        private val textTitle = view.findViewById<TextView>(R.id.textTitle)
        private val textDescription = view.findViewById<TextView>(R.id.textDescription)

        fun bind(onBoardingItem: OnBoardingItem) {
            imageOnboarding.setImageResource(onBoardingItem.orboardingImage)
            textTitle.text = onBoardingItem.title
            textDescription.text = onBoardingItem.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingItemViewHolder {
        return OnBoardingItemViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.onboarding_item_container,parent,false
        ))
    }

    override fun onBindViewHolder(holder: OnBoardingItemViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }
}