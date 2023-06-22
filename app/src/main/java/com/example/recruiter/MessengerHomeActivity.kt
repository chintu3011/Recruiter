package com.example.recruiter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recruiter.databinding.ActivityMessengerHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MessengerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerHomeBinding

    private lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUsersList()


        userId = FirebaseAuth.getInstance().currentUser!!.uid

        makeToast("userID : $userId",0)

    }

    private fun setUsersList() {
        val recruiterUserList = RecruiterUserList()
        val normalUsersListFragment = NormalUsersListFragment()
        val myFragmentPagerAdapter = MyFragmentPageAdapter(supportFragmentManager,lifecycle)
        myFragmentPagerAdapter.addFragment(recruiterUserList, "Recruiters")
        myFragmentPagerAdapter.addFragment(normalUsersListFragment, "Friends")
//        recruiterUserList.arguments = bundleForFragment1
//        normalUsersList.arguments = bundleForFragment2
        binding.viewPager.adapter = myFragmentPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = myFragmentPagerAdapter.getPageTitle(position)
        }.attach()

        binding.btnCreateNewChat.setOnClickListener {
            val intent = Intent(this@MessengerHomeActivity, NewUsersMessageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

}