package com.amri.emploihunt.recruiterSide

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentHomeRecruitBinding
import com.amri.emploihunt.databinding.SinglerowjsBinding
import com.amri.emploihunt.filterFeature.FilterDataActivity
import com.amri.emploihunt.filterFeature.FilterParameterTransferClass
import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerFragment
import com.amri.emploihunt.model.GetAllUsers
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class HomeRecruitFragment : BaseFragment(),ApplicationListUpdateListener,
    FilterParameterTransferClass.FilterApplicationListener {

    private lateinit var binding:FragmentHomeRecruitBinding

    lateinit var fragview: View
    private lateinit var database: DatabaseReference

    private lateinit var dataList: MutableList<User>
    private lateinit var filteredDataList: MutableList<User>
    private var userType: Int? = null

    private lateinit var layoutManager: LinearLayoutManager

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1

    lateinit var prefManager: SharedPreferences

    companion object {
        private const val TAG = "HomeRecruitFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val bundle = arguments
        if (bundle != null) {
            userType = bundle.getInt("userType")
        }
        Log.d(HomeJobSeekerFragment.TAG,"User type : $userType")

        // Inflate the layout for this fragment
        binding = FragmentHomeRecruitBinding.inflate(layoutInflater)

        FilterParameterTransferClass.instance!!.setApplicationListener(this)

        prefManager = prefManager(requireContext())
        database = FirebaseDatabase.getInstance().reference
        
        dataList = mutableListOf()
        filteredDataList = mutableListOf()
        binding.jobSeekerListRv.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext(),  RecyclerView.VERTICAL, false)
        binding.jobSeekerListRv.layoutManager = layoutManager
        binding.jobSeekerAdapter =
            JobSeekerAdapter(
                requireActivity(),
                filteredDataList,
                object : JobSeekerAdapter.OnCategoryClick {
                    override fun onCategoryClicked(view: View, templateModel: Jobs) {
                        val intent = Intent(requireContext(), JobPostActivity::class.java)
                        intent.putExtra("ARG_JOB_TITLE", templateModel)
                        startActivity(intent)
                    }

                })
        retrieveJsData()

        binding.jobSeekerListRv.addOnScrollListener(object :
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
                    retrieveJsData()
                }
            }
        })
        /*binding.searchR.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                filterJobList(p0)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterJobList(query)
                return false
            }

        })*/

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            /*val query = binding.searchR.query?.trim()
            if (query!!.isEmpty()) {
//                callGetAllTemplateCategoriesAPI(state_name = stateName)
            } else {
                Utils.hideKeyboard(requireActivity())
//                callGetAllTemplateCategoriesAPI(query.toString(), stateName)
            }*/
            dataList.clear()
            currentPage = 1
            retrieveJsData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.btnFilter.setOnClickListener {

            if(userType == 0 || userType == 1){
                val intent = Intent(requireContext(), FilterDataActivity::class.java)
                intent.putExtra("userType", userType!!)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            else{
                makeToast(getString(R.string.something_error),0)
                Log.e(TAG,"Incorrect user type : $userType")
            }
        }

        return binding.root
    }

    private fun retrieveJsData() {
       /* val userRef = database.child("Users")
        val jobRef = userRef.child("Job Seeker")

        jobRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataList.clear()

                for (snapshot in dataSnapshot.children) {
                    val job: UsersJobSeeker? = snapshot.getValue(UsersJobSeeker::class.java)
                    job?.let {
                        dataList.add(job)
                    }
                }

                // Notify the adapter that the data has changed
                JSListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("MainActivity", "Failed to retrieve job data from Firebase: ${error.message}")
            }
        })*/

        if (Utils.isNetworkAvailable(requireContext())) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.GET_ALL_JOBSEEKER)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("current_page",currentPage.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllUsers::class.java,
                    object : ParsedRequestListener<GetAllUsers> {
                        override fun onResponse(response: GetAllUsers?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    filteredDataList.addAll(it.data)
                                    dataList.addAll(it.data)
                                    if (dataList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.jobSeekerAdapter!!.notifyDataSetChanged()
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
                                    "onError: code: ${it.errorCode} & message: ${it.errorDetail}"
                                )
                                if (it.errorCode >= 500) {
                                    binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
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
        binding.jobSeekerListRv.visibility = if (isShow) View.VISIBLE else View.GONE
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
                retrieveJsData()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun updateApplicationList(query: String) {
        // Update your job list based on the search query
        // You can use this method to filter the jobs based on the query
        // For example, filter the jobs based on job title or description
        filteredDataList.clear()
        if (!TextUtils.isEmpty(query)) {
            for (application in dataList) {
                if (application.vDesignation.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filteredDataList.add(application)
                }
            }
        } else {
            filteredDataList.addAll(dataList)
        }

        binding.jobSeekerAdapter!!.notifyDataSetChanged()
    }

    /*private fun filterJobList(query: String) {
        dataList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in filteredDataList) {
                if (user.vDesignation!!.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    dataList.add(user)
                }
            }
        }
        else{
            dataList.addAll(filteredDataList)
        }


        binding.jobSeekerAdapter!!.notifyDataSetChanged()
    }*/

    @SuppressLint("NotifyDataSetChanged")
    override fun onDataReceivedFilterApplicationList(
        domainList: MutableList<String>,
        locationList: MutableList<String>,
        workingModeList: MutableList<String>,
        packageList: MutableList<String>
    ) {
        Log.d(
            TAG,
            "${domainList.size}, ${locationList.size},${workingModeList.size} ,${packageList.size}"
        )

        filteredDataList.clear()
        if (domainList.size > 0 || locationList.size > 0 || workingModeList.size > 0 || packageList.size > 0) {

            for (application in dataList) {

                val domainMatches = if (domainList.isNotEmpty()) {

                    domainList.any { domain ->
                        val domainWords = domain.split(" ")
                        domainWords.any { word ->
                            if (application.vDesignation.isNotEmpty()) {
                                application.vDesignation.contains(word, ignoreCase = true)
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    true
                }
                val locationMatches = if (locationList.isNotEmpty()) {

                    locationList.any { location ->
                        val locationWords = location.split(" ")
                        locationWords.any { word ->
                            if (application.vPreferCity.isNotEmpty()) {
                                application.vPreferCity.contains(word, ignoreCase = true)
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    true
                }
                val workingModeMatches = if (workingModeList.isNotEmpty()) {

                    workingModeList.any { workingMode ->
                        val workingModeWords = workingMode.split(" ")
                        workingModeWords.any { word ->
                            if (application.vWorkingMode.isNotEmpty()) {
                                application.vWorkingMode.contains(word, ignoreCase = true)
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    true
                }
                val packageMatches = if (packageList.isNotEmpty()) {

                    packageList.any { packageRange ->
                        val packageRangeWords = packageRange.split(" ")
                        packageRangeWords.any { word ->
                            if (application.vExpectedSalary.isNotEmpty()) {
                                application.vExpectedSalary.contains(word, ignoreCase = true)
                            } else {
                                false
                            }
                        }
                    }

                } else {
                    true
                }
                // If all criteria match, add the job to the filteredJobs list
                if (domainMatches && locationMatches && workingModeMatches && packageMatches) {
                    filteredDataList.add(application)
                }
            }
        } else {
            filteredDataList.addAll(dataList)
        }
        Log.d(TAG, "FilteredList: $filteredDataList")
        binding.jobSeekerAdapter!!.notifyDataSetChanged()
    }


/*    private inner class CustomAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return dataList.size
        }

        override fun getItem(position: Int): Any {
            return dataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myview = convertView
            if (myview == null) {
                myview = layoutInflater.inflate(R.layout.singlerowjs, null)
            }
            val name: TextView = myview!!.findViewById(R.id.jsname)
            val skill: TextView = myview.findViewById(R.id.qualificationjs)
            val loc: TextView = myview.findViewById(R.id.citypref)
            val type: TextView = myview.findViewById(R.id.jsjobtype)
            val contact: TextView = myview.findViewById(R.id.jscontact)
            val jobrole: TextView = myview.findViewById(R.id.jobrole)
            val email: TextView = myview.findViewById(R.id.jsemail)
            val job: UsersJobSeeker = dataList[position]
            name.text = job.userFName + " " + job.userLName
            skill.text = job.userQualification
            loc.text = job.userPrefJobLocation
            type.text = job.userWorkingMode
            jobrole.text = job.userPerfJobTitle
            contact.text = job.userPhoneNumber
            email.text = job.userEmailId
            contact.setOnClickListener {
                val num: String = contact.text.toString()
                makePhoneCall(num)
            }
            email.setOnClickListener {
                val emailsend = email.text.toString()
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailsend))
                intent.type = "message/rfc822"
                startActivity(Intent.createChooser(intent, "Choose an Email Client: "))
            }
            return myview
        }

        private fun makePhoneCall(num: String) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            startActivity(dialIntent)
        }
    }*/
    class JobSeekerAdapter(
    private var mActivity: Activity,
    private var applicationList: MutableList<User>,
    private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<JobSeekerAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                SinglerowjsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {

            val job: User = applicationList[position]
           holder.binding.jsname.text = job.vFirstName + " " + job.vLastName
            holder.binding.qualificationjs.text = job.vQualification
            holder.binding.citypref.text = job. vPreferCity
            holder.binding.jsjobtype.text = job.vWorkingMode
            holder.binding.jobrole.text = job.vDesignation
            holder.binding.jscontact.text = job.vMobile
            holder.binding.jsemail.text = job.vEmail
            holder.binding.jscontact.setOnClickListener {
                val num: String =  holder.binding.jscontact.text.toString()
                makePhoneCall(num)
            }
            holder.binding.jsemail.setOnClickListener {
                val emailsend = holder.binding.jsemail.text.toString()
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailsend))
                intent.type = "message/rfc822"
                holder.itemView.context.startActivity(Intent.createChooser(intent, "Choose an Email Client: "))
            }

        }
        private fun makePhoneCall(num: String) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            mActivity.startActivity(dialIntent)
        }
        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${applicationList.size}")
            return applicationList.size
        }

        inner class CategoriesHolder(val binding: SinglerowjsBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: Jobs)
        }
    }
}