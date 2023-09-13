package com.amri.emploihunt.filterFeature


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityFilterDataBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayoutMediator
import java.util.Locale


class FilterDataActivity : BaseActivity()/*,
    FilterTagAdapter.OnTagClickListener*/,MyPagerAdapter.OnTagClickListener, SelectedTagAdapter.OnSelectedTagClickListener,
    MyPagerAdapter.OnSearchQueryChanged {

    private lateinit var binding: ActivityFilterDataBinding

    companion object {
        private const val TAG = "FilterDataActivity"
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
    
    /*private lateinit var domainTagAdapter: FilterTagAdapter
    private lateinit var locationTagAdapter: FilterTagAdapter
    private lateinit var workingModeTagAdapter: FilterTagAdapter
    private lateinit var packageTagAdapter: FilterTagAdapter*/

    private lateinit var selectedTagAdapter: SelectedTagAdapter


    /** Main Lists */
    private lateinit var domainList:MutableList<String>
    private lateinit var locationList:ArrayList<String>
    private lateinit var workingModeList:MutableList<String>
    private lateinit var packageList:MutableList<String>

    /** filtered Lists */
    private lateinit var filterDomainList:MutableList<String>
    private  lateinit var filterLocationList:MutableList<String>
    private lateinit var filterWorkingModeList:MutableList<String>
    private lateinit var filterPackageList:MutableList<String>

    /** selected tagList*/
    private lateinit var selectedTagList:MutableList<FilterTagData>

    /*private lateinit var selectedDomainList:MutableList<String>
    private lateinit var selectedLocationList:MutableList<String>
    private lateinit var selectedWorkingModeList:MutableList<String>
    private lateinit var selectedPackageList:MutableList<String>*/

    /** final selected selected attributes need to pass to to filter lists */
    private lateinit var selectedDomain:String
    private lateinit var selectedLocation:String
    private lateinit var selectedWorkingMode:String
    private lateinit var selectedPackage:String


    private lateinit var myPagerAdapter: MyPagerAdapter
    /*var cityList: ArrayList<String> = ArrayList()*/

    lateinit var prefManager: SharedPreferences

    private var userType:Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityFilterDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = prefManager(this)
        userType = prefManager.get(ROLE,0)
        /*userType = intent.getIntExtra("role",-1)*/

        Log.d(TAG,userType.toString())


        /** setup of lists */
        domainList = resources.getStringArray(R.array.indian_designations).toMutableList()
        locationList = arrayListOf()
        workingModeList = mutableListOf("Hybrid","Remote","On site")
        packageList = resources.getStringArray(R.array.expected_salary).toMutableList()

        filterDomainList = resources.getStringArray(R.array.indian_designations).toMutableList()
        filterLocationList = mutableListOf()
        getAllCity(locationList){
            if(locationList.isNotEmpty()){
               filterLocationList.addAll(locationList)
            }
            else{
                makeToast(getString(R.string.something_error),0)
            }
        }
        filterWorkingModeList =mutableListOf("Hybrid","Remote","On site")
        filterPackageList = resources.getStringArray(R.array.expected_salary).toMutableList()

        /*selectedDomainList = mutableListOf()
        selectedLocationList = mutableListOf()
        selectedWorkingModeList = mutableListOf()
        selectedPackageList = mutableListOf()*/

        selectedDomain = String()
        selectedLocation = String()
        selectedWorkingMode = String()
        selectedPackage = String()

        selectedTagList = mutableListOf()

        /*domainTagAdapter = FilterTagAdapter(
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
        )*/


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
        )

        myPagerAdapter =  MyPagerAdapter(adapterList,this@FilterDataActivity,this, FilterCategories.JOB)*/

        /*when (userType) {
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
        }*/


        /** setup of pager adapter for view pager */
        when (userType) {
            UserType.JOB_SEEKERS -> {
                val categoriesLists: MutableList<MutableList<String>> = mutableListOf(
                    filterDomainList,
                    filterLocationList,
                    filterWorkingModeList,
                    filterPackageList
                )
                myPagerAdapter =  MyPagerAdapter(categoriesLists,this@FilterDataActivity,this,this, FilterCategories.JOB)

            }
            UserType.RECRUITER -> {
                val categoriesLists: MutableList<MutableList<String>> = mutableListOf(
                    filterDomainList,
                    filterLocationList,
                    filterWorkingModeList,
                    filterPackageList
                )
                myPagerAdapter = MyPagerAdapter(categoriesLists,this@FilterDataActivity,this, this,FilterCategories.APPLICATION)
            }
            else -> {
                val categoriesLists = mutableListOf<MutableList<String>>()
                myPagerAdapter = MyPagerAdapter(categoriesLists,this@FilterDataActivity,this,this,-1)
            }
        }

        val pagerAdapter = myPagerAdapter
        binding.viewPager.adapter = pagerAdapter


        /** setup of tabLayout */
        val tabLabels = listOf("Domain", "Location", "Mode", "Package")
        // Attach the TabLayoutMediator after setting up the adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabLabels[position]
        }.attach()

        
        /** for selected tags */
        binding.recycleSelectedTags.layoutManager = LinearLayoutManager(this@FilterDataActivity,LinearLayoutManager.HORIZONTAL,false)

        binding.recycleSelectedTags.adapter = selectedTagAdapter


        /** setup of menu */
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
                    /** pass selected attributes to FilterParameterTransferClass */
                    when(userType){
                        UserType.JOB_SEEKERS -> {
                            /*Log.d(TAG,selectedDomainList.toString())
                            Log.d(TAG,selectedLocationList.toString())
                            Log.d(TAG,selectedWorkingModeList.toString())
                            Log.d(TAG,selectedPackageList.toString())*/
                            Log.d(TAG,selectedDomain)
                            Log.d(TAG,selectedLocation)
                            Log.d(TAG,selectedWorkingMode)
                            Log.d(TAG,selectedPackage)

                            FilterParameterTransferClass.instance!!
                                /*.setJobData(
                                    selectedDomainList,
                                    selectedLocationList,
                                    selectedWorkingModeList,
                                    selectedPackageList
                                )*/
                                .setJobData(
                                    selectedDomain,
                                    selectedLocation,
                                    selectedWorkingMode,
                                    selectedPackage
                                )

                            finish()
                        }
                        UserType.RECRUITER -> {
                            /*Log.d(TAG,selectedDomainList.toString())
                            Log.d(TAG,selectedLocationList.toString())
                            Log.d(TAG,selectedWorkingModeList.toString())
                            Log.d(TAG,selectedPackageList.toString())*/
                            Log.d(TAG,selectedDomain)
                            Log.d(TAG,selectedLocation)
                            Log.d(TAG,selectedWorkingMode)
                            Log.d(TAG,selectedPackage)

                            FilterParameterTransferClass.instance!!
                                /*.setApplicationData(
                                    selectedDomainList,
                                    selectedLocationList,
                                    selectedWorkingModeList,
                                    selectedPackageList
                                )*/
                                .setApplicationData(
                                    selectedDomain,
                                    selectedLocation,
                                    selectedWorkingMode,
                                    selectedPackage
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    override fun searchTags(query: String, attribute: Int, chipGroup: ChipGroup,filterCategory: Int,onTagClickListener:MyPagerAdapter.OnTagClickListener) {
        updateTagList(attribute,query, chipGroup,filterCategory,onTagClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTagList(identifier: Int, query: String,chipGroup: ChipGroup, filterCategory: Int,onTagClickListener:MyPagerAdapter.OnTagClickListener) {

        when (filterCategory){
            FilterCategories.JOB -> {
                when (identifier) {
                    SearchViewIdentifier.DOMAIN_SEARCH -> {
                        // Update domain list based on the query
                        filterDomainList.clear()
                        chipGroup.removeAllViews()
                        
                        if (!TextUtils.isEmpty(query)){
                            for (domain in domainList) {
                                if (domain.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterDomainList.add(domain)

                                    addSingleChip(
                                        domain,
                                        filterDomainList.indexOf(domain),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.DOMAIN
                                    )

                                }
                            }
                        }
                        else{
                            filterDomainList.addAll(domainList)
                            addMultipleChips(
                                filterDomainList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.DOMAIN
                            )

                        }

                        /*domainTagAdapter.notifyDataSetChanged()*/

                        return
                    }

                    SearchViewIdentifier.LOCATION_SEARCH -> {
                        // Update location list based on the query
                        filterLocationList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (location in locationList) {
                                if (location.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterLocationList.add(location)
                                    addSingleChip(
                                        location,
                                        filterLocationList.indexOf(location),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.LOCATION
                                    )
                                }
                            }
                        }
                        else{
                            filterLocationList.addAll(locationList)

                            addMultipleChips(
                                filterLocationList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.LOCATION
                            )
                        }

                        /*locationTagAdapter.notifyDataSetChanged()*/
                        return
                    }

                    SearchViewIdentifier.WORKING_MODE_SEARCH -> {
                        // Update working mode list based on the query
                        filterWorkingModeList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (mode in workingModeList) {
                                if (mode.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterWorkingModeList.add(mode)
                                    addSingleChip(
                                        mode,
                                        filterWorkingModeList.indexOf(mode),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.WORKING_MODE
                                    )
                                }
                            }
                        }
                        else{
                            filterWorkingModeList.addAll(workingModeList)
                            addMultipleChips(
                                filterWorkingModeList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.WORKING_MODE
                            )
                        }

                        /*workingModeTagAdapter.notifyDataSetChanged()*/
                        return
                    }

                    SearchViewIdentifier.PACKAGE_SEARCH -> {
                        // Update package list based on the query
                        filterPackageList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (packageRange in packageList) {
                                if (packageRange.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterPackageList.add(packageRange)
                                    addSingleChip(
                                       packageRange,
                                        filterPackageList.indexOf(packageRange),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.PACKAGE
                                    )
                                }
                            }
                        }
                        else{
                            filterPackageList.addAll(packageList)
                            addMultipleChips(
                                filterPackageList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.PACKAGE
                            )
                        }

                        /*packageTagAdapter.notifyDataSetChanged()*/

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
                        chipGroup.removeAllViews()

                        if (!TextUtils.isEmpty(query)){
                            for (domain in domainList) {
                                if (domain.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterDomainList.add(domain)

                                    addSingleChip(
                                        domain,
                                        filterDomainList.indexOf(domain),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.DOMAIN
                                    )

                                }
                            }
                        }
                        else{
                            filterDomainList.addAll(domainList)
                            addMultipleChips(
                                filterDomainList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.DOMAIN
                            )

                        }

                        /*domainTagAdapter.notifyDataSetChanged()*/

                        return
                    }

                    SearchViewIdentifier.LOCATION_SEARCH -> {
                        // Update location list based on the query
                        filterLocationList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (location in locationList) {
                                if (location.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterLocationList.add(location)
                                    addSingleChip(
                                        location,
                                        filterLocationList.indexOf(location),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.LOCATION
                                    )
                                }
                            }
                        }
                        else{
                            filterLocationList.addAll(locationList)

                            addMultipleChips(
                                filterLocationList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.LOCATION
                            )
                        }

                        /*locationTagAdapter.notifyDataSetChanged()*/
                        return
                    }

                    SearchViewIdentifier.WORKING_MODE_SEARCH -> {
                        // Update working mode list based on the query
                        filterWorkingModeList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (mode in workingModeList) {
                                if (mode.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterWorkingModeList.add(mode)
                                    addSingleChip(
                                        mode,
                                        filterWorkingModeList.indexOf(mode),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.WORKING_MODE
                                    )
                                }
                            }
                        }
                        else{
                            filterWorkingModeList.addAll(workingModeList)
                            addMultipleChips(
                                filterWorkingModeList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.WORKING_MODE
                            )
                        }

                        /*workingModeTagAdapter.notifyDataSetChanged()*/
                        return
                    }

                    SearchViewIdentifier.PACKAGE_SEARCH -> {
                        // Update package list based on the query
                        filterPackageList.clear()
                        chipGroup.removeAllViews()
                        if (!TextUtils.isEmpty(query)){
                            for (packageRange in packageList) {
                                if (packageRange.lowercase(Locale.ROOT)
                                        .contains(query.lowercase(Locale.ROOT))
                                ) {
                                    filterPackageList.add(packageRange)
                                    addSingleChip(
                                        packageRange,
                                        filterPackageList.indexOf(packageRange),
                                        chipGroup,
                                        onTagClickListener,
                                        AttributeIdentifier.PACKAGE
                                    )
                                }
                            }
                        }
                        else{
                            filterPackageList.addAll(packageList)
                            addMultipleChips(
                                filterPackageList,
                                chipGroup,
                                onTagClickListener,
                                AttributeIdentifier.PACKAGE
                            )
                        }

                        /*packageTagAdapter.notifyDataSetChanged()*/

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

    private fun addMultipleChips(
        tagList: MutableList<String>,
        chipGroup: ChipGroup,
        onTagClickListener: MyPagerAdapter.OnTagClickListener,
        attribute: Int
    ) {
        for (index in tagList.indices) {
            val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
            if(attribute == 4){
                chip.text = tagList[index].plus(" LPA +")
            }
            else{
                chip.text = tagList[index]
            }


            chipGroup.addView(chip)

            chip.setOnClickListener {
                onTagClickListener.onTagClick(index,attribute)
            }
            chip.setOnLongClickListener {
                onTagClickListener.onTagLongClick(index,attribute)
                return@setOnLongClickListener true
            }

        }
    }

    private fun addSingleChip(
        tag: String,
        index: Int,
        chipGroup: ChipGroup,
        onTagClickListener: MyPagerAdapter.OnTagClickListener,
        attribute: Int
    ) {
        val chip = layoutInflater.inflate(R.layout.chip_layout, null) as Chip
        if(attribute == 4){
            chip.text = tag.plus(" LPA +")
        }
        else{
            chip.text = tag
        }

        chipGroup.addView(chip)

        chip.setOnClickListener {
            onTagClickListener.onTagClick(index,attribute)
        }
        chip.setOnLongClickListener {
            onTagClickListener.onTagLongClick(index,attribute)
            return@setOnLongClickListener true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onTagClick(position: Int, attribute: Int) {
        when(attribute){
            AttributeIdentifier.DOMAIN -> {
                Log.d(TAG,"onClick ${filterDomainList[position]}")

                val existingItem = selectedTagList.find {
                    it.attribute == AttributeIdentifier.DOMAIN
                }

                if (existingItem != null) {
                    val index = selectedTagList.indexOf(existingItem)
                    /*domainList.add(selectedTagList[index].tagName!!)
                    filterDomainList.add(selectedTagList[index].tagName!!)*/
                    selectedTagList[index] = FilterTagData(filterDomainList[position],
                        AttributeIdentifier.DOMAIN
                    )
                } else {
                    selectedTagList.add(
                        FilterTagData(filterDomainList[position],
                        AttributeIdentifier.DOMAIN
                        )
                    )
                }

                /*selectedTagList.add(
                    FilterTagData(filterDomainList[position],
                        AttributeIdentifier.DOMAIN
                    )
                )*/
                selectedTagAdapter.notifyDataSetChanged()

                selectedDomain = filterDomainList[position]
                /*selectedDomainList.add(filterDomainList[position])*/
                makeToast("${filterDomainList[position]} added",0)

                /*domainList.remove(filterDomainList[position])
                filterDomainList.removeAt(position)*/

//                domainTagAdapter.notifyDataSetChanged()

            }
            AttributeIdentifier.LOCATION -> {
                Log.d(TAG,"onClick ${filterLocationList[position]}")
                val existingItem = selectedTagList.find {
                    it.attribute == AttributeIdentifier.LOCATION
                }

                if (existingItem != null) {
                    val index = selectedTagList.indexOf(existingItem)
                    /*locationList.add(selectedTagList[index].tagName!!)
                    filterLocationList.add(selectedTagList[index].tagName!!)*/
                    selectedTagList[index] = FilterTagData(filterLocationList[position],
                        AttributeIdentifier.LOCATION
                    )
                } else {
                    selectedTagList.add(
                        FilterTagData(filterLocationList[position],
                            AttributeIdentifier.LOCATION
                        )
                    )
                }

                /*selectedTagList.add(
                    FilterTagData(filterLocationList[position],
                        AttributeIdentifier.LOCATION
                    )
                )*/
                selectedTagAdapter.notifyDataSetChanged()

                selectedLocation = filterLocationList[position]
                /*selectedLocationList.add(filterLocationList[position])*/
                makeToast("${filterLocationList[position]} added",0)

               /* locationList.remove(filterLocationList[position])
                filterLocationList.removeAt(position)*/
//                locationTagAdapter.notifyDataSetChanged()

            }
            AttributeIdentifier.WORKING_MODE -> {

                Log.d(TAG,"onClick ${filterWorkingModeList[position]}")


                val existingItem = selectedTagList.find { it.attribute == AttributeIdentifier.WORKING_MODE }

                if (existingItem != null) {

                    val index = selectedTagList.indexOf(existingItem)
                    /*workingModeList.add(selectedTagList[index].tagName!!)
                    filterWorkingModeList.add(selectedTagList[index].tagName!!)*/
                    selectedTagList[index] = FilterTagData(filterWorkingModeList[position],
                        AttributeIdentifier.WORKING_MODE
                    )
                } else {

                    selectedTagList.add(
                        FilterTagData(filterWorkingModeList[position],
                            AttributeIdentifier.WORKING_MODE
                        )
                    )
                }

                /*selectedTagList.add(
                    FilterTagData(filterWorkingModeList[position],
                        AttributeIdentifier.WORKING_MODE
                    )
                )*/
                selectedTagAdapter.notifyDataSetChanged()

                selectedWorkingMode = filterWorkingModeList[position]
                /*selectedWorkingModeList.add(filterWorkingModeList[position])*/
                makeToast("${filterWorkingModeList[position]} added",0)

               /* workingModeList.remove(filterWorkingModeList[position])
                filterWorkingModeList.removeAt(position)*/
//                workingModeTagAdapter.notifyDataSetChanged()
            }
            AttributeIdentifier.PACKAGE -> {
                Log.d(TAG,"onClick ${filterPackageList[position]}")

                val existingItem = selectedTagList.find { it.attribute == AttributeIdentifier.PACKAGE }

                if (existingItem != null) {

                    val index = selectedTagList.indexOf(existingItem)
                    /*packageList.add(selectedTagList[index].tagName!!)
                    filterPackageList.add(selectedTagList[index].tagName!!)*/

                    selectedTagList[index] = FilterTagData(filterPackageList[position],
                        AttributeIdentifier.PACKAGE
                    )
                } else {

                    selectedTagList.add(
                        FilterTagData(filterPackageList[position],
                            AttributeIdentifier.PACKAGE
                        )
                    )
                }
                
                /*selectedTagList.add(
                    FilterTagData(filterPackageList[position],
                        AttributeIdentifier.PACKAGE
                    )
                )*/
                selectedTagAdapter.notifyDataSetChanged()

                selectedPackage = filterPackageList[position]
                /*selectedPackageList.add(filterPackageList[position])*/
                makeToast("${filterPackageList[position]} added",0)

                /*packageList.remove(filterPackageList[position])
                filterPackageList.removeAt(position)*/
//                packageTagAdapter.notifyDataSetChanged()

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
                    selectedDomain = ""
                    /*selectedDomainList.remove(tag.tagName)*/

                    filterDomainList.add(tag.tagName!!)
                    domainList.add(tag.tagName!!)
//                    domainTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.LOCATION -> {
                    selectedLocation = ""
                    /*selectedLocationList.remove(tag.tagName)*/

                    filterLocationList.add(tag.tagName!!)
                    locationList.add(tag.tagName!!)
//                    locationTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.WORKING_MODE -> {
                    selectedWorkingMode = ""
                    /*selectedWorkingModeList.remove(tag.tagName)*/

                    filterWorkingModeList.add(tag.tagName!!)
                    workingModeList.add(tag.tagName!!)
//                    workingModeTagAdapter.notifyDataSetChanged()
                }

                AttributeIdentifier.PACKAGE -> {
                    selectedPackage = ""
                    /*selectedPackageList.remove(tag.tagName)*/

                    filterPackageList.add(tag.tagName!!)
                    packageList.add(tag.tagName!!)
//                    packageTagAdapter.notifyDataSetChanged()
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

  /*  private fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)
                                locationList.addAll(response!!.data)
                                filterLocationList.addAll(response!!.data)
//                                locationTagAdapter.notifyDataSetChanged()
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

    }*/
}