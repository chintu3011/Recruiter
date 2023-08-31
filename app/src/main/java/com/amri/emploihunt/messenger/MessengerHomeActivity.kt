package com.amri.emploihunt.messenger

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityMessengerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MessengerHomeActivity : BaseActivity() {

    private var currentFragment: Fragment ?= null
    private lateinit var binding: ActivityMessengerHomeBinding

    private lateinit var userId:String
    private var userType = String()
    
    companion object{
        private const val TAG = "MessengerHomeActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@MessengerHomeActivity.window
        window.statusBarColor = ContextCompat.getColor(this@MessengerHomeActivity, R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Messenger"

        userId = FirebaseAuth.getInstance().currentUser!!.uid

        userType = intent.getStringExtra("userType").toString()
        Log.d(TAG,"userType :: $userType")

        if(userType.isNotEmpty()){
            replaceFragment(userType)
            binding.btnCreateNewChat.setOnClickListener {
                val intent = Intent(this@MessengerHomeActivity, NewUsersMessageActivity::class.java)
                intent.putExtra("userType",userType)
                Log.d(TAG,"userType:$userType")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
//                finish()
            }
        }
        else{
            Log.d(TAG,"User type not found from intent")
            getUserType {userType ->
                replaceFragment(userType)
                binding.btnCreateNewChat.setOnClickListener {
                    val intent = Intent(this@MessengerHomeActivity, NewUsersMessageActivity::class.java)
                    intent.putExtra("userType",userType)
                    Log.d(TAG,"userType:$userType")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
//                finish()
                }
            }
        }

        setMenuItemListener()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        })

    }
    private var btnSearch: MenuItem? = null
    private var btnVoiceSearch: MenuItem? = null
    private var btnDelete: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu,menu)

        btnSearch = menu?.findItem(R.id.btnSearch)
        btnVoiceSearch = menu?.findItem(R.id.btnVoiceSearch)
        btnDelete = menu?.findItem(R.id.btnDelete)
        return true
    }
    private fun setMenuItemListener() {
        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.btnSearch -> {
                    val searchView = it.actionView as SearchView

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                            if (currentFragment is UserListUpdateListener) {
                                currentFragment.updateUserList(newText.orEmpty())
                            }
                            return true
                        }
                    })
                    true
                }
                R.id.btnVoiceSearch -> {
                    openVoice()
                    true
                }
                R.id.btnDelete -> {
//                    deleteUser()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun openVoice() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            startActivityForResult(intent, 200)
        } catch (e: ActivityNotFoundException) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://market.android.com/details?id=APP_PACKAGE_NAME")
            )
            startActivity(browserIntent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val query = matches!![0]
            if (matches.isNotEmpty()) {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                if (currentFragment is UserListUpdateListener) {
                    currentFragment.updateUserList(query.orEmpty())
                }
            }
        } else {

        }
    }

/*    override fun onRestart() {
        super.onRestart()
        getUserType{
            Log.d(TAG,"$userId :: $it")

            replaceFragment(it)
            binding.btnCreateNewChat.setOnClickListener {
                val intent = Intent(this@MessengerHomeActivity, NewUsersMessageActivity::class.java)
                intent.putExtra("userType",userType)
                Log.d(TAG,"userType:$userType")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                finish()

            }
        }
    }*/
    private fun getUserType(callback: (String) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (userTypeSnapshot in snapshot.children) {
                    val userReference = userTypeSnapshot.child(userId)

                    if (userReference.exists()) {
//                        userType = userTypeSnapshot.key.toString()
//                        val userTypeValue = userReference.child("userType").getValue(String::class.java)
                           callback(userTypeSnapshot.key.toString())
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG,"error :: ${error.message}")
            }
        })
    }

    private fun replaceFragment(userType: String){
        val fragment:Fragment
        when (userType) {
            "Job Seeker" -> {
                fragment = RecruiterUserListFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment,fragment.javaClass.simpleName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
                currentFragment = fragment
            }
            "Recruiter" -> {
                fragment = JobSeekerUsersListFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment,fragment.javaClass.simpleName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
                currentFragment = fragment
            }
            else -> {
                Log.d(TAG,"$userType is not correct")
                makeToast("Didn't get a proper user Type",0)
            }
        }

    }

    /*
        private fun setUsersList() {
            val recruiterUserListFragment = RecruiterUserListFragment()
            val normalUsersListFragment = NormalUsersListFragment()
            val myFragmentPagerAdapter = MyFragmentPageAdapter(supportFragmentManager, lifecycle)
            myFragmentPagerAdapter.addFragment(recruiterUserListFragment, "Recruiters")
            myFragmentPagerAdapter.addFragment(normalUsersListFragment, "Friends")
    //        recruiterUserList.arguments = bundleForFragment1
    //        normalUsersList.arguments = bundleForFragment2
            binding.viewPager.adapter = myFragmentPagerAdapter
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = myFragmentPagerAdapter.getPageTitle(position)
            }.attach()
        }
    */



}