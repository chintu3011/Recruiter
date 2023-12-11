package com.amri.emploihunt.jobSeekerSide

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentHomeJobSeekerBinding
import com.amri.emploihunt.databinding.RowPostDesignBinding
import com.amri.emploihunt.filterFeature.FilterParameterTransferClass
import com.amri.emploihunt.messenger.MessaengerHomesActivity_2
import com.amri.emploihunt.messenger.MessengerHomeActivity
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.GetAllJob
import com.amri.emploihunt.model.GetJobPreferenceList
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.ADDRESS
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.JOB_TITLE
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.SALARY_PACKAGE
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.getTimeAgo
import com.amri.emploihunt.util.WORKING_MODE
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DatabaseReference
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignLeft

class HomeJobSeekerFragment : BaseFragment(),JobListUpdateListener,
FilterParameterTransferClass.FilterJobListListener {

    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var filteredDataList: MutableList<Jobs>
    private  lateinit var prefManager: SharedPreferences
    private var userType: Int? = null
    private var userId: String? = null
    private lateinit var layoutManager: LinearLayoutManager
    var jobPreferenceList: ArrayList<DataJobPreferenceList> = ArrayList()
    private var adapter: SpinAdapter? = null
    private  lateinit var binding: FragmentHomeJobSeekerBinding
    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    private var isFilter = false
    private var domain = ""
    private var location = ""
    private var workingMode = ""
    private var packageRange = ""


    private lateinit var balloon: Balloon

    companion object{
        const val TAG = "HomeJobSeekerFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        prefManager = PrefManager.prefManager(requireContext())
        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)
        /*val bundle = arguments
        if (bundle != null) {
            userType = bundle.getInt("role")
        }*/
        Log.d(TAG,"$userId :: $userType")

        binding = FragmentHomeJobSeekerBinding.inflate(layoutInflater)

        binding.imgOpenDrawer.visibility = View.VISIBLE
        FilterParameterTransferClass.instance!!.setJobListener(this)

        jobPreferenceList.add(DataJobPreferenceList(0,0,"Select job preference","0","0",
            "0","0","0"))
        initDrawersData()
        getJobPreference()

        binding.jobPreferenceSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                Log.d("###$", "onItemSelected: $position")
                // Here you get the current item (a User object) that is selected by its position
                val pref: DataJobPreferenceList = adapter!!.getItem(position)
                // Here you can do the action you want to...
                filteredDataList.clear()
                currentPage = 1
                binding.jobRvList.visibility = GONE
                binding.layEmptyView.root.visibility = GONE
                isFilter = false
                retrieveJobData(pref.id, "")

            }

            override fun onNothingSelected(adapter: AdapterView<*>?) {

            }
        }
        filteredDataList = mutableListOf()
        dataList = mutableListOf()
        binding.jobRvList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext(),  RecyclerView.VERTICAL, false)
        binding.jobRvList.layoutManager = layoutManager
        binding.jobsAdapter =
            JobsAdapter(requireActivity(), filteredDataList, object : JobsAdapter.OnCategoryClick {
                override fun onCategoryClicked(view: View, templateModel: Jobs) {
                    val intent  = Intent(requireContext(), JobPostActivity::class.java)
                    intent.putExtra("ARG_JOB_TITLE",templateModel)
                    changePostLauncher.launch(intent)
                }
            })

//        retrieveJobData(0)

        binding.jobRvList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItems = layoutManager.childCount
                totalItems = layoutManager.itemCount
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (isScrolling && (totalItems == currentItems + firstVisibleItemPosition)) {
                    isScrolling = false
                    currentPage++
                    Log.d("###", "onScrolled: $currentPage")
                    if (isFilter){
                        filterJobsApi(domain, location, workingMode, packageRange)
                    }else{
                        retrieveJobData(0, "")
                        isFilter = false
                    }

                }
            }
        })

        /*binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterJobList(query)
                return false
            }

        })*/
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            binding.jobRvList.visibility = GONE
            binding.layEmptyView.root.visibility = GONE
            /*val query = binding.search.query?.trim()
            if (query!!.isEmpty()) {
//                callGetAllTemplateCategoriesAPI(state_name = stateName)
            } else {
                Utils.hideKeyboard(requireActivity())
//                callGetAllTemplateCategoriesAPI(query.toString(), stateName)
            }*/

            if (jobPreferenceList.size != 0){
                binding.jobPreferenceSp.visibility = View.VISIBLE
            }
            if (binding.jobPreferenceSp.visibility == View.VISIBLE){
                binding.jobPreferenceSp.setSelection(0)

            }

            filteredDataList.clear()
            currentPage = 1
            retrieveJobData(0, "")
            isFilter = false
            binding.swipeRefreshLayout.isRefreshing = false
        }
        binding.imgOpenDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        val balloonLayout = LayoutInflater.from(requireContext()).inflate(R.layout.messenger_balloon,null,false)
        val txtMessage = balloonLayout.findViewById<MaterialTextView>(R.id.txtMessage)
        txtMessage.text = "Chat with \n Recruiters!."

        balloon = createBalloon(requireContext()){
            setLayout(balloonLayout)
            setArrowSize(10)
            setArrowOrientation(ArrowOrientation.END)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setArrowPosition(0.5f)
            setWidth(200)
            setHeight(200)
            setCornerRadius(30f)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_ripple_color_blue))
            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            setLifecycleOwner(lifecycleOwner)
            build()
        }

        binding.imgOpenDrawer.showAlignLeft(balloon)
        balloon.dismissWithDelay(2000)


        binding.btnMessenger.setOnClickListener {
            val intent = Intent(requireContext(), MessaengerHomesActivity_2::class.java)
            intent.putExtra("userType", userType!!)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            if (Build.VERSION.SDK_INT >= 34) {
                requireActivity().overrideActivityTransition(AppCompatActivity.OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in_left,R.anim.slide_out_right)
            }
            else{
                requireActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            }
        }
        return binding.root

    }

    /*@SuppressLint("NotifyDataSetChanged")
    private fun filterJobList(query: String) {
        dataList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in filteredDataList) {
                if (user.vJobTitle!!.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    dataList.add(user)
                }
            }
        }
        else{
            dataList.addAll(filteredDataList)
        }
        binding.jobsAdapter!!.notifyDataSetChanged()
    }*/
    private fun initDrawersData() {
        binding.viewNav.setOnTouchListener { v, event ->
            binding.drawerLayout.closeDrawer(GravityCompat.END)
            false
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                animSlideFromStart(binding.imgOpenDrawer)
            }

            override fun onDrawerClosed(drawerView: View) {
                animSlideFromEnd(binding.imgOpenDrawer)
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }
    private fun retrieveJobData(jobPreferenceId: Int, tag: String) {
        Log.d("###", "retrieveJobData: ")

        if (Utils.isNetworkAvailable(requireContext())) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.GET_ALL_JOB)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("iJobPreferenceId",jobPreferenceId.toString())
                .addQueryParameter("tag",tag)
                .addQueryParameter("current_page",currentPage.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(GetAllJob::class.java,
                    object : ParsedRequestListener<GetAllJob> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(response: GetAllJob?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    filteredDataList.addAll(it.data)
                                    dataList.addAll(it.data)
                                    if (dataList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.jobsAdapter!!.notifyDataSetChanged()
                                        hideShowEmptyView(true)
                                    } else {
                                        hideShowEmptyView(false)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse: catch: ${e.message}")
                            }
                        }

                        override fun onError(anError: ANError?) {
                            hideShowEmptyView(false)
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                )
                                if (it.errorCode >= 500) {
                                    binding.layEmptyView.tvNoData.text =
                                        getString(R.string.opps_sorry_jobs_not_available_at_moment)
                                }
                            }
                            hideProgressDialog()
                        }
                    })
        } else {
            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }

    }
    private fun hideShowEmptyView(
        isShow: Boolean,  isInternetAvailable: Boolean = true
    ) {
        binding.jobRvList.visibility = if (isShow) View.VISIBLE else View.GONE
        binding.layEmptyView.root.visibility = if (isShow) View.GONE else View.VISIBLE
        binding.layProgressPagination.root.visibility = View.GONE
        binding.progressCircular.visibility = View.GONE
        if (isInternetAvailable) {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_job_found)
            binding.layEmptyView.btnRetry.visibility = View.GONE
        } else {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_internet)
            binding.layEmptyView.btnRetry.visibility = View.VISIBLE
            binding.layEmptyView.btnRetry.setOnClickListener {
                retrieveJobData(0, "")
                isFilter = false
            }
        }
    }
    var changePostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                dataList.clear()
                filteredDataList.clear()
                currentPage = 1
                binding.jobRvList.visibility = GONE
                retrieveJobData(0, "")
                isFilter = false
            }
        }
    class JobsAdapter(
        private var mActivity: Activity,
        private var dataList: MutableList<Jobs>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<JobsAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowPostDesignBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobModel = dataList[position]

            holder.binding.jobTitle.text = jobModel.vJobTitle
            holder.binding.salary.text = jobModel.vSalaryPackage +" LPA +"
            holder.binding.experiencedDuration.text = jobModel.vExperience + " years"
            holder.binding.qualification.text = jobModel.vEducation
            holder.binding.city.text = jobModel.vAddress
            holder.binding.aboutPost.text = jobModel.tDes
            holder.binding.companyName.text = jobModel.vCompanyName
            holder.binding.recruiterName.text = "${jobModel.user.vFirstName} ${jobModel.user.vLastName}"
            holder.binding.employees.text =  "${jobModel.iNumberOfVacancy} Vacancy"
            holder.binding.createTimeTV.text = getTimeAgo(holder.itemView.context ,
                jobModel.tCreatedAt!!.toLong())
            Glide.with(holder.itemView.context)
                .load(NetworkUtils.BASE_URL_MEDIA+jobModel.tCompanyLogoUrl)
                .placeholder(R.mipmap.ic_logo)
                .into(holder.binding.profileImg)
//            onCategoryClick.onCategoryClicked(it, templateModel)
            holder.binding.executePendingBindings()
            holder.itemView.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, jobModel)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowPostDesignBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: Jobs)
        }
    }
    private  fun getJobPreference(){
        jobPreferenceList.clear()
        if (Utils.isNetworkAvailable(requireContext())) {
            AndroidNetworking.get(NetworkUtils.JOB_PREFERENCE_LIST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetJobPreferenceList::class.java,
                    object : ParsedRequestListener<GetJobPreferenceList> {
                        override fun onResponse(response: GetJobPreferenceList?) {
                            try {
                                response?.let {

                                    Log.d("###", "onResponse: ${it.data}")
                                    jobPreferenceList.add(DataJobPreferenceList(0,0,"For you","0","","","",
                                        ""))
                                    jobPreferenceList.addAll(it.data)

                                    adapter = SpinAdapter(
                                        requireContext(),
                                        android.R.layout.simple_spinner_item,
                                        jobPreferenceList
                                    )
                                    adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    binding.jobPreferenceSp.adapter = adapter
                                    if (response.total_records >= 1){
                                        binding.jobPreferenceSp.visibility = View.VISIBLE
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse: catch: ${e.message}")
                            }
                        }

                        override fun onError(anError: ANError?) {

                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                )

                            }
                            retrieveJobData(0, "")
                            isFilter = false
                        }
                    })
        }
        else {
            Utils.showNoInternetBottomSheet(requireContext(),requireActivity())
        }
    }
    class SpinAdapter(
        context: Context, textViewResourceId: Int,
        values: ArrayList<DataJobPreferenceList>
    ) : ArrayAdapter<DataJobPreferenceList>(context, textViewResourceId, values) {
        // Your sent context
        private val context: Context

        // Your custom values for the spinner (User)
        private val values: ArrayList<DataJobPreferenceList>

        init {
            this.context = context
            this.values = values
        }

        override fun getCount(): Int {
            return values.size
        }

        override fun getItem(position: Int): DataJobPreferenceList {
            return values[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            val label = super.getView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            // Then you can get the current item using the values array (Users array) and the current position
            // You can NOW reference each method you has created in your bean object (User class)
            if (position != 0){
                label.text = "${values[position].vJobTitle}-${values[position].vJobTitle}-${values[position].vExpectedSalary}LPA"
            }else{
                label.text = "${values[position].vJobTitle}"
            }


            // And finally return your dynamic (or custom) view for each spinner item
            return label
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val label = super.getDropDownView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            if (position != 0){
                label.text = "${values[position].vJobTitle}-${values[position].vJobTitle}-${values[position].vExpectedSalary}LPA"
            }else{
                label.text = "${values[position].vJobTitle}"
            }
            return label
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDataReceivedFilterJobList(
        domain: String,
        location: String,
        workingMode: String,
        packageRange: String
    ) {

        Log.d("##","${domain}, ${location},${workingMode} ,${packageRange}")

        filteredDataList.clear()
        currentPage = 1
        totalPages = 1
        isFilter = true
        this@HomeJobSeekerFragment.domain = domain
        this@HomeJobSeekerFragment.location = location
        this@HomeJobSeekerFragment.workingMode = workingMode
        this@HomeJobSeekerFragment.packageRange = packageRange
        binding.jobPreferenceSp.visibility = View.GONE
        binding.layEmptyView.root.visibility = View.GONE
        binding.jobRvList.visibility = View.GONE
        filterJobsApi(domain,location,workingMode,packageRange)


    }

    fun filterJobsApi(domain: String, location: String, workingMode: String, packageRange: String) {
        if (Utils.isNetworkAvailable(requireContext())) {
            Log.d("###", "onDataReceivedFilterJobList: $currentPage $totalPages")

            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.FIlTER_JOBS)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter(JOB_TITLE,domain)
                .addQueryParameter(ADDRESS,location)
                .addQueryParameter(WORKING_MODE,workingMode)
                .addQueryParameter(SALARY_PACKAGE,packageRange)
                .addQueryParameter("current_page",currentPage.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(GetAllJob::class.java,
                    object : ParsedRequestListener<GetAllJob> {
                           @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(response: GetAllJob?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    binding.progressCircular.visibility = GONE
                                    Log.d("###", "onResponse FilterJobList: ${it.data}")
                                    filteredDataList.addAll(it.data)
                                    binding.jobsAdapter!!.notifyDataSetChanged()
                                    hideShowEmptyView(true)
                                }
                            } catch (e: Exception) {
                                binding.progressCircular.visibility = GONE
                                hideShowEmptyView(false)
                                Log.e("#####", "onResponse FilterJobList: catch: ${e.message}")
                            }
                        }

                        override fun onError(anError: ANError?) {
                            binding.progressCircular.visibility = GONE
                            hideShowEmptyView(false)
                            anError?.let {
                                Log.e(
                                    TAG,
                                    "onError FilterJobList: code: ${it.errorCode} & message: ${it.errorDetail}"
                                )
                                if (it.errorCode >= 500) {
                                    binding.layEmptyView.tvNoData.text =
                                        activity?.getString(R.string.not_match_any_data_based_on_your_filter)
                                }
                            }
                            hideProgressDialog()
                        }
                    })
        } else {
            binding.progressCircular.visibility = GONE
            hideShowEmptyView(isShow = false, isInternetAvailable = false)
        }
        Log.d(TAG,"FilteredList: $filteredDataList")
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateJobList(query: String) {
       /* filteredDataList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in dataList) {
                if (user.vJobTitle!!.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filteredDataList.add(user)
                }
            }
        }
        else{
            filteredDataList.addAll(dataList)
        }
        binding.jobsAdapter!!.notifyDataSetChanged()*/
        filteredDataList.clear()
        currentPage = 1
        binding.jobRvList.visibility = GONE
        binding.layEmptyView.root.visibility = GONE
        isFilter = false
        retrieveJobData(0,query)
    }

    override fun backToSearchView() {
        filteredDataList.clear()
        currentPage = 1
        binding.jobRvList.visibility = GONE
        binding.layEmptyView.root.visibility = GONE
        isFilter = false
        retrieveJobData(0,"")
    }


}
