package com.amri.emploihunt.filterFeature


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityFilterDataBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale


class FilterDataActivity : BaseActivity(),
    FilterTagAdapter.OnTagClickListener, SelectedTagAdapter.OnSelectedTagClickListener,
    MyPagerAdapter.OnSearchQueryChanged {

    private lateinit var binding: ActivityFilterDataBinding

    companion object {
        private const val TAG = "FilterJobsActivity"
    }

    object FilterCategories{
        const val JOB = 1
        const val APPLICATION = 2
    }
    object UserType{
        const val JOB_SEEKERS = 0
        const val RECRUITER = 1
    }

    object SearchViewIdentifier {
        const val DOMAIN_SEARCH = 1
        const val LOCATION_SEARCH = 2
        const val WORKING_MODE_SEARCH = 3
        const val PACKAGE_SEARCH = 4
    }
    object AttributeIdentifier {
        const val DOMAIN = 1
        const val LOCATION = 2
        const val WORKING_MODE = 3
        const val PACKAGE = 4
    }
    
    private lateinit var domainTagAdapter: FilterTagAdapter
    private lateinit var locationTagAdapter: FilterTagAdapter
    private lateinit var workingModeTagAdapter: FilterTagAdapter
    private lateinit var packageTagAdapter: FilterTagAdapter

    private lateinit var selectedTagAdapter: SelectedTagAdapter

    private lateinit var domainList:MutableList<String>
    private var locationList:ArrayList<String>  = ArrayList()
    private lateinit var workingModeList:MutableList<String>
    private lateinit var packageList:MutableList<String>


    private lateinit var filterDomainList:MutableList<String>
    private  var filterLocationList:ArrayList<String>  = ArrayList()
    private lateinit var filterWorkingModeList:MutableList<String>
    private lateinit var filterPackageList:MutableList<String>

    private lateinit var selectedTagList:MutableList<FilterTagData>

    private lateinit var selectedDomainList:MutableList<String>
    private lateinit var selectedLocationList:MutableList<String>
    private lateinit var selectedWorkingModeList:MutableList<String>
    private lateinit var selectedPackageList:MutableList<String>

    private lateinit var myPagerAdapter: MyPagerAdapter
    var cityList: ArrayList<String> = ArrayList()

    private var userType:Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityFilterDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window: Window = this@FilterDataActivity.window
        window.statusBarColor = ContextCompat.getColor(this@FilterDataActivity,android.R.color.white)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        userType = intent.getIntExtra("role",-1)
        getAllCity()
        Log.d(TAG,userType.toString())

        domainList = resources.getStringArray(R.array.indian_designations).toMutableList()
        workingModeList = mutableListOf("Hybrid","Remote","On site")
        packageList = resources.getStringArray(R.array.expected_salary).toMutableList()

        filterDomainList = resources.getStringArray(R.array.indian_designations).toMutableList()
        filterWorkingModeList =mutableListOf("Hybrid","Remote","On site")
        filterPackageList = resources.getStringArray(R.array.expected_salary).toMutableList()

        selectedDomainList = mutableListOf()
        selectedLocationList = mutableListOf()
        selectedWorkingModeList = mutableListOf()
        selectedPackageList = mutableListOf()

        selectedTagList = mutableListOf()

        domainTagAdapter = FilterTagAdapter(
            filterDomainList,
            this,
            AttributeIdentifier.DOMAIN
        )
        locationTagAdapter = FilterTagAdapter(
            filterLocationList,
            this,
            AttributeIdentifier.LOCATION
        )
        workingModeTagAdapter = FilterTagAdapter(
            filterWorkingModeList,
            this,
            AttributeIdentifier.WORKING_MODE
        )
        packageTagAdapter = FilterTagAdapter(
            filterPackageList,
            this,
            AttributeIdentifier.PACKAGE
        )
        
        selectedTagAdapter = SelectedTagAdapter(
            selectedTagList,
            this,
            this
        )

        /*val adapterList: MutableList<FilterTagAdapter> = mutableListOf(
            domainTagAdapter,
            locationTagAdapter,
            workingModeTagAdapter,
            packageTagAdapter
        )*/

        /*myPagerAdapter =  MyPagerAdapter(adapterList,this@FilterDataActivity,this, FilterCategories.JOB)*/

        when (userType) {
            UserType.JOB_SEEKERS -> {
                val adapterList: MutableList<FilterTagAdapter> = mutableListOf(
                    domainTagAdapter,
                    locationTagAdapter,
                    workingModeTagAdapter,
                    packageTagAdapter
                )
                myPagerAdapter =  MyPagerAdapter(adapterList,this@FilterDataActivity,this, FilterCategories.JOB)

            }
            UserType.RECRUITER -> {
                val adapterList: MutableList<FilterTagAdapter> = mutableListOf(
                    domainTagAdapter,
                    locationTagAdapter,
                    workingModeTagAdapter,
                    packageTagAdapter
                )
                myPagerAdapter = MyPagerAdapter(adapterList,this@FilterDataActivity,this, FilterCategories.APPLICATION)
            }
            else -> {
                val adapterList = mutableListOf<FilterTagAdapter>()
                myPagerAdapter = MyPagerAdapter(adapterList,this@FilterDataActivity,this,-1)
            }
        }

        val pagerAdapter = myPagerAdapter
        binding.viewPager.adapter = pagerAdapter

        val tabLabels = listOf("Domain", "Location", "Mode", "Package")
        // Attach the TabLayoutMediator after setting up the adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabLabels[position]
        }.attach()

        binding.recycleSelectedTags.layoutManager = LinearLayoutManager(this@FilterDataActivity,LinearLayoutManager.HORIZONTAL,false)

        binding.recycleSelectedTags.adapter = selectedTagAdapter
        
        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        // showing the back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setMenuItemListener()
        supportFragmentManager.addOnBackStackChangedListener {
            invalidateOptionsMenu() // This triggers onPrepareOptionsMenu()
        }
    }

    private var btnFilter: MenuItem? = null
    private var btnRemoveSelection: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.filter_preference_menu,menu)

        btnFilter = menu?.findItem(R.id.btnFilter)
        
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        when (userType) {
            UserType.JOB_SEEKERS -> {
                supportActionBar?.title = "Filter job Preference"
            }

            UserType.RECRUITER -> {
                supportActionBar?.title = "Filter Applications"
            }
            else -> {
                makeToast("Something went wrong",0)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setMenuItemListener() {
        binding.toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.btnFilter -> {

                    when(userType){
                        UserType.JOB_SEEKERS -> {
                            Log.d(TAG,selectedDomainList.toString())
                            Log.d(TAG,selectedLocationList.toString())
                            Log.d(TAG,selectedWorkingModeList.toString())
                            Log.d(TAG,selectedPackageList.toString())

                            FilterParameterTransferClass.instance!!
                                .setJobData(
                                    selectedDomainList,
                                    selectedLocationList,
                                    selectedWorkingModeList,
                                    selectedPackageList
                                )

                            finish()
                        }
                        UserType.RECRUITER -> {
                            Log.d(TAG,selectedDomainList.toString())
                            Log.d(TAG,selectedLocationList.toString())
                            Log.d(TAG,selectedWorkingModeList.toString())
                            Log.d(TAG,selectedPackageList.toString())

                            FilterParameterTransferClass.instance!!
                                .setApplicationData(
                                    selectedDomainList,
                                    selectedLocationList,
                                    selectedWorkingModeList,
                                    selectedPackageList
                                )

                            finish()
                        }
                        else -> {
                            makeToast("Something went wrong",0)
                            return@setOnMenuItemClickListener false
                        }
                    }

                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onContextItemSelected(item)
    }


    override fun searchTags(query: String, attribute: Int, filterCategory: Int) {
        updateTagList(attribute,query,filterCategory)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTagList(identifier: Int, query: String, filterCategory: Int) {

        when (filterCategory){
            FilterCategories.JOB -> {
                when (identifier) {
                    SearchViewIdentifier.DOMAIN_SEARCH -> {
                        // Update domain list based on the query
                        filterDomainList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (domain in domainList) {
                                if (domain.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterDomainList.add(domain)
                                }
                            }
                        }
                        else{
                            filterDomainList.addAll(domainList)
                        }

                        domainTagAdapter.notifyDataSetChanged()

                        return
                    }

                    SearchViewIdentifier.LOCATION_SEARCH -> {
                        // Update location list based on the query
                        filterLocationList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (location in locationList) {
                                if (location.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterLocationList.add(location)
                                }
                            }
                        }
                        else{
                            filterLocationList.addAll(locationList)
                        }

                        locationTagAdapter.notifyDataSetChanged()
                        return
                    }

                    SearchViewIdentifier.WORKING_MODE_SEARCH -> {
                        // Update working mode list based on the query
                        filterWorkingModeList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (mode in workingModeList) {
                                if (mode.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterWorkingModeList.add(mode)
                                }
                            }
                        }
                        else{
                            filterWorkingModeList.addAll(workingModeList)
                        }

                        workingModeTagAdapter.notifyDataSetChanged()
                        return
                    }

                    SearchViewIdentifier.PACKAGE_SEARCH -> {
                        // Update package list based on the query
                        filterPackageList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (packageRange in packageList) {
                                if (packageRange.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterPackageList.add(packageRange)
                                }
                            }
                        }
                        else{
                            filterPackageList.addAll(packageList)
                        }

                        packageTagAdapter.notifyDataSetChanged()

                        return
                    }

                    else -> {
                        makeToast("Something went wrong",0)
                        return
                    }
                }
            }
            FilterCategories.APPLICATION -> {
                when (identifier) {
                    SearchViewIdentifier.DOMAIN_SEARCH -> {
                        // Update domain list based on the query
                        filterDomainList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (domain in domainList) {
                                if (domain.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterDomainList.add(domain)
                                }
                            }
                        }
                        else{
                            filterDomainList.addAll(domainList)
                        }

                        domainTagAdapter.notifyDataSetChanged()
                        return
                    }

                    SearchViewIdentifier.LOCATION_SEARCH -> {
                        // Update location list based on the query
                        filterLocationList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (location in locationList) {
                                if (location.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterLocationList.add(location)
                                }
                            }
                        }
                        else{
                            filterLocationList.addAll(locationList)
                        }

                        locationTagAdapter.notifyDataSetChanged()
                        return
                    }

                    SearchViewIdentifier.WORKING_MODE_SEARCH -> {
                        // Update working mode list based on the query
                        filterWorkingModeList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (mode in workingModeList) {
                                if (mode.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterWorkingModeList.add(mode)
                                }
                            }
                        }
                        else{
                            filterWorkingModeList.addAll(workingModeList)
                        }

                        workingModeTagAdapter.notifyDataSetChanged()
                        return
                    }

                    SearchViewIdentifier.PACKAGE_SEARCH -> {
                        // Update package list based on the query
                        filterPackageList.clear()

                        if (!TextUtils.isEmpty(query)){
                            for (packageRange in packageList) {
                                if (packageRange.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterPackageList.add(packageRange)
                                }
                            }
                        }
                        else{
                            filterPackageList.addAll(packageList)
                        }

                        packageTagAdapter.notifyDataSetChanged()
                        return
                    }

                    else -> {
                        makeToast("Something went wrong",0)
                        return
                    }
                }
            }
            else -> {
                makeToast("Filter Category not found",0
                )
                return
            }
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onTagClick(position: Int, attribute: Int) {
        when(attribute){
            AttributeIdentifier.DOMAIN -> {
                Log.d(TAG,"onClick ${filterDomainList[position]}")

                selectedTagList.add(
                    FilterTagData(filterDomainList[position],
                        AttributeIdentifier.DOMAIN
                    )
                )
                selectedTagAdapter.notifyDataSetChanged()

                selectedDomainList.add(filterDomainList[position])
                makeToast("${filterDomainList[position]} added",0)

                domainList.remove(filterDomainList[position])
                filterDomainList.removeAt(position)

                domainTagAdapter.notifyDataSetChanged()

            }
            AttributeIdentifier.LOCATION -> {
                Log.d(TAG,"onClick ${filterLocationList[position]}")

                selectedTagList.add(
                    FilterTagData(filterLocationList[position],
                        AttributeIdentifier.LOCATION
                    )
                )
                selectedTagAdapter.notifyDataSetChanged()

                selectedLocationList.add(filterLocationList[position])
                makeToast("${filterLocationList[position]} added",0)

                locationList.remove(filterLocationList[position])
                filterLocationList.removeAt(position)
                locationTagAdapter.notifyDataSetChanged()

            }
            AttributeIdentifier.WORKING_MODE -> {

                Log.d(TAG,"onClick ${filterWorkingModeList[position]}")

                selectedTagList.add(
                    FilterTagData(filterWorkingModeList[position],
                        AttributeIdentifier.WORKING_MODE
                    )
                )
                selectedTagAdapter.notifyDataSetChanged()

                selectedWorkingModeList.add(filterWorkingModeList[position])
                makeToast("${filterWorkingModeList[position]} added",0)

                workingModeList.remove(filterWorkingModeList[position])
                filterWorkingModeList.removeAt(position)
                workingModeTagAdapter.notifyDataSetChanged()
            }
            AttributeIdentifier.PACKAGE -> {
                Log.d(TAG,"onClick ${filterPackageList[position]}")

                selectedTagList.add(
                    FilterTagData(filterPackageList[position],
                        AttributeIdentifier.PACKAGE
                    )
                )
                selectedTagAdapter.notifyDataSetChanged()

                selectedPackageList.add(filterPackageList[position])
                makeToast("${filterPackageList[position]} added",0)

                packageList.remove(filterPackageList[position])
                filterPackageList.removeAt(position)
                packageTagAdapter.notifyDataSetChanged()

            }
            else -> {
                makeToast("Something went wrong",0)
            }
        }
        selectedTagAdapter.notifyDataSetChanged()

    }

    override fun onTagLongClick(position: Int, attribute: Int) {

        when(attribute){
            AttributeIdentifier.DOMAIN -> {
                Log.d(TAG,"onLongClick ${filterDomainList[position]}")
            }
            AttributeIdentifier.LOCATION -> {
                Log.d(TAG,"onLongClick ${filterLocationList[position]}")
            }
            AttributeIdentifier.WORKING_MODE -> {
                Log.d(TAG,"onLongClick ${filterWorkingModeList[position]}")
            }
            AttributeIdentifier.PACKAGE -> {
                Log.d(TAG,"onLongClick ${filterPackageList[position]}")
            }
            else -> {
                makeToast("Something went wrong",0)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelectedTagClick(position: Int, isChecked: Boolean) {
        val tag = selectedTagList[position]

        if (!tag.tagName.isNullOrEmpty()) {
//            Log.d(TAG,tag.attribute.toString())
            when (tag.attribute) {
                AttributeIdentifier.DOMAIN -> {
                    selectedDomainList.remove(tag.tagName)

                    filterDomainList.add(tag.tagName!!)
                    domainList.add(tag.tagName!!)
                    domainTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.LOCATION -> {
                    selectedLocationList.remove(tag.tagName)

                    filterLocationList.add(tag.tagName!!)
                    locationList.add(tag.tagName!!)
                    locationTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.WORKING_MODE -> {
                    selectedWorkingModeList.remove(tag.tagName)

                    filterWorkingModeList.add(tag.tagName!!)
                    workingModeList.add(tag.tagName!!)
                    workingModeTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.PACKAGE -> {
                    selectedPackageList.remove(tag.tagName)

                    filterPackageList.add(tag.tagName!!)
                    packageList.add(tag.tagName!!)
                    packageTagAdapter.notifyDataSetChanged()
                }

                else -> {
                    makeToast("Something went wrong", 0)
                }
            }
            makeToast("${tag.tagName} removed",0)
        }
        selectedTagList.removeAt(position)
        selectedTagAdapter.notifyDataSetChanged()
    }

    override fun onSelectedTagLongClick(position: Int, isChecked: Boolean) {

    }

    fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)
                                locationList = cityList
                                filterLocationList = cityList
                                locationTagAdapter.notifyDataSetChanged()
                                hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                                hideProgressDialog()

                            }


                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(this, this)
        }

    }
}