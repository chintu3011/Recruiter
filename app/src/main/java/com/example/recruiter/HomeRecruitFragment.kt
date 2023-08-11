package com.example.recruiter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide
import com.example.recruiter.basedata.BaseFragment
import com.example.recruiter.databinding.FragmentHomeRecruitBinding
import com.example.recruiter.databinding.RowPostDesignBinding
import com.example.recruiter.databinding.SinglerowjsBinding
import com.example.recruiter.model.GetAllJob
import com.example.recruiter.model.GetAllJobSeeker
import com.example.recruiter.model.User
import com.example.recruiter.networking.NetworkUtils
import com.example.recruiter.util.AUTH_TOKEN
import com.example.recruiter.util.PrefManager.get
import com.example.recruiter.util.PrefManager.prefManager
import com.example.recruiter.util.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeRecruitFragment : BaseFragment() {
    private lateinit var binding:FragmentHomeRecruitBinding
    lateinit var fragview: View
    private lateinit var database: DatabaseReference
    private lateinit var dataList: MutableList<User>
    private lateinit var filteredDataList: MutableList<User>

    private lateinit var layoutManager: LinearLayoutManager
    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    lateinit var prefManager: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeRecruitBinding.inflate(layoutInflater)
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
                dataList,
                object : JobSeekerAdapter.OnCategoryClick {
                    override fun onCategoryClicked(view: View, templateModel: Jobs) {
                        val intent = Intent(requireContext(), JobPostActivity::class.java)
                        intent.putExtra("ARG_JOB_TITLE", templateModel)
                        startActivity(intent)
                    }

                })
        retreivejsdata()

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
                    retreivejsdata()
                }
            }
        })
        binding.searchR.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String): Boolean {
                filterJobList(p0)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                filterJobList(query)
                return false
            }

        })
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            val query = binding.searchR.query?.trim()
            if (query!!.isEmpty()) {
//                callGetAllTemplateCategoriesAPI(state_name = stateName)
            } else {
                Utils.hideKeyboard(requireActivity())
//                callGetAllTemplateCategoriesAPI(query.toString(), stateName)
            }
            dataList.clear()
            currentPage = 1
            retreivejsdata()

            binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root
    }

    private fun retreivejsdata() {
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
                    GetAllJobSeeker::class.java,
                    object : ParsedRequestListener<GetAllJobSeeker> {
                        override fun onResponse(response: GetAllJobSeeker?) {
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
                retreivejsdata()
            }
        }
    }
    private fun filterJobList(query: String) {
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
        private var dataList: MutableList<User>,
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

            val job: User = dataList[position]
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
        fun makePhoneCall(num: String) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            mActivity.startActivity(dialIntent)
        }
        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: SinglerowjsBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: Jobs)
        }
    }
}