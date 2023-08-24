package com.amri.emploihunt

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentHomeBinding
import com.amri.emploihunt.databinding.RowPostDesignBinding
import com.amri.emploihunt.model.DataJobPreference
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.GetAllJob
import com.amri.emploihunt.model.GetJobPreferenceList
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_ADDED_JOB_PREFERENCE
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.getTimeAgo
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import java.util.Locale

class HomeFragment : BaseFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<Jobs>
    private lateinit var filteredDataList: MutableList<Jobs>
    private  lateinit var prefManager: SharedPreferences
    private var userType: Int? = null
    private lateinit var layoutManager: LinearLayoutManager
    var jobPreferenceList: ArrayList<DataJobPreferenceList> = ArrayList()
    private var adapter: SpinAdapter? = null

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
        jobPreferenceList.add(DataJobPreferenceList(0,0,"Select job preference","0","0",
            "0","0","0"))

        getJobPreference()

        binding.jobPreferenceSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                // Here you get the current item (a User object) that is selected by its position
                val pref: DataJobPreferenceList = adapter!!.getItem(position)
                // Here you can do the action you want to...
                dataList.clear()
                currentPage = 1
                retrieveJobData(pref.id)
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
            JobsAdapter(requireActivity(), dataList, object : JobsAdapter.OnCategoryClick {
                override fun onCategoryClicked(view: View, templateModel: Jobs) {
                    val intent  = Intent(requireContext(),JobPostActivity::class.java)
                    intent.putExtra("ARG_JOB_TITLE",templateModel)
                    changePostLauncher.launch(intent)

                }

            })
        retrieveJobData(0)
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
                    retrieveJobData(0)
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

            if (binding.jobPreferenceSp.visibility == View.VISIBLE){
                binding.jobPreferenceSp.setSelection(0)
            }else{
                dataList.clear()
                currentPage = 1
                retrieveJobData(0)
            }


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

    private fun retrieveJobData(jobpreferenceId: Int) {
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
                .addQueryParameter("iJobPreferenceId",jobpreferenceId.toString())
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
                retrieveJobData(0)
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
    var changePostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                dataList.clear()
                currentPage = 1
                retrieveJobData(0)
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

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobModel = dataList[position]

            holder.binding.jobTitle.text = jobModel.vJobTitle
            holder.binding.salary.text = jobModel.vSalaryPackage +"LPA"
            holder.binding.experiencedDuration.text = jobModel.vExperience + " years"
            holder.binding.qualification.text = jobModel.vEducation
            holder.binding.city.text = jobModel.vAddress
            holder.binding.aboutPost.text = jobModel.tDes
            holder.binding.companyName.text = jobModel.vCompanyName
            holder.binding.employees.text =  "${jobModel.iNumberOfVacancy} Vacancy"
            holder.binding.createTimeTV.text = getTimeAgo(holder.itemView.context ,
                jobModel.tCreatedAt!!.toLong())
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

                        }
                    })
        } else {
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
            label.text = "${values[position].vJobTitle}"

            // And finally return your dynamic (or custom) view for each spinner item
            return label
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val label = super.getDropDownView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            label.text = "${values[position].vJobTitle}"
            return label
        }
    }
    override fun onResume() {
        super.onResume()
        if (IS_ADDED_JOB_PREFERENCE){
            getJobPreference()
            IS_ADDED_JOB_PREFERENCE = false
        }
    }
}
