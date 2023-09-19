package com.amri.emploihunt.messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AbsListView
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityNewUsersMessageBinding
import com.amri.emploihunt.model.GetAllUsers
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.JOB_SEEKER
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.RECRUITER
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import java.util.Locale

class NewUsersMessageActivity : BaseActivity(), UserListUpdateListener {

    private lateinit var binding: ActivityNewUsersMessageBinding

    private var userType: Int? = null
    private var userId: String? = null

    private lateinit var userList: MutableList<User>
    private lateinit var filterUserList: MutableList<User>

    lateinit var prefManager: SharedPreferences

    private lateinit var layoutManager: LinearLayoutManager

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1

    private lateinit var newUserMessageAdapter: NewUserMessageAdapter

    companion object {
        private const val TAG = "NewUsersMessageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUsersMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@NewUsersMessageActivity.window
        window.statusBarColor =
            ContextCompat.getColor(this@NewUsersMessageActivity, android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Make New Chat"
        setMenuItemListener()

        userList = mutableListOf()
        filterUserList = mutableListOf()

        /*userType = intent.getStringExtra("userType")*/
        prefManager = PrefManager.prefManager(this)

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        userType = prefManager.get(ROLE, 0)
        userId = prefManager.get(FIREBASE_ID)

        Log.d(TAG, "$userId :: $userType")

        userType?.let { userType ->
            getUsersList(userType) {
                if (it) {
                    newUserMessageAdapter = NewUserMessageAdapter(filterUserList, this)
                    binding.recyclerView.adapter = newUserMessageAdapter
                } else {
                    makeToast(getString(R.string.something_error), 0)
                }
            }
        }

        /*binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                baseContext,
                layoutManager.orientation
            )
        )*/

        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                Log.d("###", "onScrolled: ${isScrolling} && ${totalItems} == $currentItems + $firstVisibleItemPosition")
                if (isScrolling && (totalItems == currentItems + firstVisibleItemPosition)) {
                    isScrolling = false
                    currentPage++
                    Log.d("###", "onScrolled: $currentPage")

                    getUsersList(userType!!){
                        newUserMessageAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun getUsersList(userType: Int, callBack: (Boolean) -> Unit) {

        if (Utils.isNetworkAvailable(this)) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE


            when (userType) {
                RECRUITER -> {
                    AndroidNetworking.get(NetworkUtils.GET_ALL_JOBSEEKER)
                        .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                        .addQueryParameter("current_page", currentPage.toString())
                        .setPriority(Priority.MEDIUM).build()
                        .getAsObject(
                            GetAllUsers::class.java,
                            object : ParsedRequestListener<GetAllUsers> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onResponse(response: GetAllUsers?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            Log.d("###", "onResponse: ${it.data}")
                                            filterUserList.addAll(it.data)
                                            userList.addAll(it.data)
                                            if (userList.isNotEmpty()) {
                                                totalPages = it.total_pages
                                                /*hideShowEmptyView(true)*/
                                            } else {
                                                /*hideShowEmptyView(false)*/
                                            }
                                            binding.progressCircular.visibility = View.GONE
                                            callBack(true)

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse: catch: ${e.message}")
                                        callBack(false)

                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    /*hideShowEmptyView(false)*/
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                        )
                                        /*if (it.errorCode >= 500) {
                                                binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
                                            }*/
                                    }
                                    //                                hideProgressDialog()
                                    callBack(false)
                                }
                            })
                }

                JOB_SEEKER -> {
                    AndroidNetworking.get(NetworkUtils.GET_ALL_RECRUITER)
                        .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                        .addQueryParameter("current_page", currentPage.toString())
                        .setPriority(Priority.MEDIUM).build()
                        .getAsObject(
                            GetAllUsers::class.java,
                            object : ParsedRequestListener<GetAllUsers> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onResponse(response: GetAllUsers?) {
                                    try {
                                        response?.let {
                                            hideProgressDialog()
                                            Log.d("###", "onResponse: ${it.data}")
                                            filterUserList.addAll(it.data)
                                            userList.addAll(it.data)
                                            if (userList.isNotEmpty()) {
                                                totalPages = it.total_pages
                                                /*hideShowEmptyView(true)*/
                                            } else {
                                                /*hideShowEmptyView(false)*/
                                            }
                                            binding.progressCircular.visibility = View.GONE
                                            callBack(true)

                                        }
                                    } catch (e: Exception) {
                                        Log.e("#####", "onResponse: catch: ${e.message}")
                                        callBack(false)
                                    }
                                }

                                override fun onError(anError: ANError?) {
                                    /*hideShowEmptyView(false)*/
                                    anError?.let {
                                        Log.e(
                                            "#####",
                                            "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                        )
                                        /*if (it.errorCode >= 500) {
                                                binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
                                            }*/
                                    }
                                    //                                hideProgressDialog()
                                    callBack(false)

                                }
                            })
                }

                else -> {
                    makeToast(getString(R.string.didn_t_get_proper_user_type), 0)
                    Log.d(
                        TAG,
                        "${getString(R.string.didn_t_get_proper_user_type)} => userType :: $userType"
                    )
                }
            }
        } else {
            Utils.showNoInternetBottomSheet(this, this)
            callBack(false)
//            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateUserList(query: String) {

        filterUserList.clear()

        if (!TextUtils.isEmpty(query)){
            for (user in userList) {
                val fullName = user.vFirstName + " " + user.vLastName
                if (fullName.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filterUserList.add(user)
                }
            }
        }
        else{
            filterUserList.addAll(userList)
        }
        newUserMessageAdapter.notifyDataSetChanged()
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
                            updateUserList(newText.orEmpty())
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
                updateUserList(query.orEmpty())
            }
        } else {
            Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
        }
    }

}