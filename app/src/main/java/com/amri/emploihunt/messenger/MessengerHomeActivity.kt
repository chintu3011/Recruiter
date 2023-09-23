package com.amri.emploihunt.messenger

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityMessengerHomeBinding
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.JOB_SEEKER
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.RECRUITER
import com.amri.emploihunt.util.ROLE
import com.google.firebase.database.ValueEventListener


class MessengerHomeActivity : BaseActivity() {

    private var currentFragment: Fragment ?= null
    private lateinit var binding: ActivityMessengerHomeBinding

    private var userId:String ?= null
    private var userType:Int ?= null

    lateinit var prefManager: SharedPreferences
    
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

        prefManager = prefManager(this)
        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)
        Log.d("####","$userId :: $userType")
        
        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Messenger"
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /*userId = FirebaseAuth.getInstance().currentUser!!.uid*/
        /*userId = intent.getStringExtra("userId")
        userType = intent.getIntExtra("role",-1)*/
        /*Log.d(TAG,"$userId :: $userType")
*/
        if(userType != -1 && userId != null){
            replaceFragment(userType)
            binding.btnCreateNewChat.setOnClickListener {
                val intent = Intent(this@MessengerHomeActivity, NewUsersMessageActivity::class.java)
                intent.putExtra("role",userType)
                Log.d(TAG,"userType:$userType")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
//                finish()
            }
        }
        else{
            Log.d(TAG,"User type not found from intent => : $userId :: $userType")
            makeToast(getString(R.string.something_error),0)
            /*getUserType {userType ->
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
            }*/
        }

        setMenuItemListener()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private var btnSearch: MenuItem? = null
    private var btnVoiceSearch: MenuItem? = null
    private var btnDelete: MenuItem? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu,menu)

        btnSearch = menu?.findItem(R.id.btnSearch)
        btnVoiceSearch = menu?.findItem(R.id.btnVoiceSearch)
        btnDelete = menu?.findItem(R.id.btnDelete)
        btnDelete?.isVisible = false
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

    private fun replaceFragment(userType: Int?){
        val fragment:Fragment
        val bundle:Bundle
        when (userType) {
            JOB_SEEKER -> {
                fragment = RecruiterUserListFragment()
                bundle = Bundle()
                bundle.putInt("role",userType)
                bundle.putString("userId",userId)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment,fragment.javaClass.simpleName)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
                currentFragment = fragment
            }
            RECRUITER -> {
                fragment = JobSeekerUsersListFragment()
                bundle = Bundle()
                bundle.putInt("role",userType)
                bundle.putString("userId",userId)
                fragment.arguments = bundle
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