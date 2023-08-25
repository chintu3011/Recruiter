package com.amri.emploihunt.messenger

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager,lifecycle){

    private val fragList = ArrayList<Fragment> ()
    private val titleList = ArrayList<String>()
    override fun getItemCount(): Int {
        return fragList.size
    }
    override fun createFragment(position: Int): Fragment {
        return fragList[position]
    }

    fun addFragment(fragment: Fragment?, title: String?) {
        fragment?.let { fragList.add(it) }
        title?.let { titleList.add(it) }
    }
    fun getPageTitle(position: Int): CharSequence {
        return titleList[position]
    }


}