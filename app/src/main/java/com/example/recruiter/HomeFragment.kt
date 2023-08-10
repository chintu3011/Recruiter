package com.example.recruiter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.example.recruiter.basedata.BaseFragment
import com.example.recruiter.databinding.FragmentHomeBinding
import com.example.recruiter.databinding.RowPostDesignBinding
import com.example.recruiter.model.GetAllJob
import com.example.recruiter.networking.NetworkUtils
import com.example.recruiter.util.AUTH_TOKEN
import com.example.recruiter.util.PrefManager
import com.example.recruiter.util.PrefManager.get
import com.example.recruiter.util.Utils
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import java.util.Locale

class HomeFragment : BaseFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var filteredDataList: MutableList<Jobs>
    private  lateinit var prefManager: SharedPreferences
    private var userType: Int? = null
    private lateinit var layoutManager: LinearLayoutManager

    private  lateinit var binding: FragmentHomeBinding
    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val bundle = arguments
        if (bundle != null) {
            userType = bundle.getInt("userType")
        }
        binding = FragmentHomeBinding.inflate(layoutInflater)
        prefManager = PrefManager.prefManager(requireContext())

        filteredDataList = mutableListOf()
        dataList = mutableListOf()
        binding.jobRvList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext(),  RecyclerView.VERTICAL, false)
        binding.jobRvList.layoutManager = layoutManager
        binding.jobsAdapter =
            JobsAdapter(requireActivity(), dataList, object : JobsAdapter.OnCategoryClick {
                override fun onCategoryClicked(view: View, templateModel: Jobs) {
                    val intent  = Intent(requireContext(),JobPostActivity::class.java)
                    intent.putExtra("ARG_JOB_TITLE",templateModel)
                    startActivity(intent)
                }

            })
        retrieveJobData()
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
                    retrieveJobData()
                }
            }
        })

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterJobList(query)
                return false
            }

        })
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            val query = binding.search.query?.trim()
            if (query!!.isEmpty()) {
//                callGetAllTemplateCategoriesAPI(state_name = stateName)
            } else {
                Utils.hideKeyboard(requireActivity())
//                callGetAllTemplateCategoriesAPI(query.toString(), stateName)
            }
            dataList.clear()
            currentPage = 1
            retrieveJobData()

            binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root

    }

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
    }

    private fun retrieveJobData() {
        Log.d("###", "retrieveJobData: ")
        /*val jobRef = database.child("Jobs")

        jobRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                filteredDataList.clear()
                dataList.clear()
                for (snapshot in dataSnapshot.children) {
                    val job: Jobs? = snapshot.getValue(Jobs::class.java)
                    job?.let {
                        filteredDataList.add(job)
                    }
                }
                dataList.addAll(filteredDataList)
                Log.d("###", "getCount: ${dataList}")
                // Notify the adapter that the data has changed
                jobListAdapter = CustomAdapter()
                gridView.adapter = jobListAdapter
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

            AndroidNetworking.get(NetworkUtils.GET_ALL_JOB)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("current_page",currentPage.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(GetAllJob::class.java,
                    object : ParsedRequestListener<GetAllJob> {
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
                retrieveJobData()
            }
        }
    }

    /*private inner class CustomAdapter : BaseAdapter() {
        override fun getCount(): Int {
            Log.d("###", "getCount: ${dataList.size}")
            return dataList.size
        }

        override fun getItem(position: Int): Any {
            return dataList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        @SuppressLint("SetTextI18n", "InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myview = convertView
            if (myview == null) {
                myview = layoutInflater.inflate(R.layout.row_post_design, null)
            }
            val name: MaterialTextView = myview!!.findViewById(R.id.jobTitle)
            val sal: MaterialTextView = myview.findViewById(R.id.salary)
            val exp: MaterialTextView = myview.findViewById(R.id.experiencedDuration)
            val qual: MaterialTextView = myview.findViewById(R.id.qualification)
            val loc: TextView = myview.findViewById(R.id.city)
            val img: ImageView = myview.findViewById(R.id.profileImg)
            val about: MaterialTextView = myview.findViewById(R.id.aboutPost)
            val compname: MaterialTextView = myview.findViewById(R.id.companyName)
            val employess: MaterialTextView = myview.findViewById(R.id.employees)
            val cv: CardView = myview.findViewById(R.id.cardViewinfo)
            val job: Jobs = dataList[position]
            name.text = job.jobTile
            sal.text = job.salary + " LPA"
            exp.text = job.experienceDuration + " years"
            qual.text = job.education
            loc.text = job.jobLocation
            about.text = job.aboutPost
            compname.text = job.companyName
            employess.text = job.employeeNeed + " Employees"
            Glide.with(img.context).load(job.companyLogo).into(img)
            cv.setOnClickListener {
                val activity: AppCompatActivity = view?.context as AppCompatActivity
                val jobTitle = dataList[position]
                val jobPostDescriptionFragment = JobPostDescriptionFragment.newInstance(jobTitle)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, jobPostDescriptionFragment)
                    .addToBackStack(null)
                    .commit()
            }
            return myview
        }
    }*/
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

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobModel = dataList[position]

            holder.binding.jobTitle.text = jobModel.vJobTitle
            holder.binding.salary.text = jobModel.vSalaryPackage
            holder.binding.experiencedDuration.text = jobModel.vExperience + " years"
            holder.binding.qualification.text = jobModel.vEducation
            holder.binding.city.text = jobModel.vAddress
            holder.binding.aboutPost.text = jobModel.tDes
            holder.binding.companyName.text = jobModel.vCompanyName
            holder.binding.employees.text =  "${jobModel.iNumberOfVacancy} Vacancy"
            Glide.with(holder.itemView.context).load(jobModel.tCompanyLogoUrl).into(holder.binding.profileImg)
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
}
