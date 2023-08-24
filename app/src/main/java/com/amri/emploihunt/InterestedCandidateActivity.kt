package com.amri.emploihunt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityInterestedCandidateBinding
import com.amri.emploihunt.databinding.SinglerowjsBinding
import com.amri.emploihunt.model.AppliedCandidateModel
import com.amri.emploihunt.model.DataAppliedCandidate
import com.amri.emploihunt.model.GetAllJob
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener


class InterestedCandidateActivity : BaseActivity() {
    lateinit var binding: ActivityInterestedCandidateBinding
    lateinit var prefManager : SharedPreferences
    private var postList: ArrayList<Jobs> = ArrayList()
    private var candidateList: ArrayList<DataAppliedCandidate> = ArrayList()
    private var adapter: SpinAdapter? = null
    private lateinit var layoutManager: LinearLayoutManager
    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityInterestedCandidateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = prefManager(this)
        retrieveJobData()
        adapter = SpinAdapter(
            this,
            android.R.layout.simple_spinner_item,
            postList
        )
        adapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJob.adapter = adapter
        binding.spinnerJob.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View?,
                position: Int, id: Long
            ) {
                // Here you get the current item (a userJobPref object) that is selected by its position
                val job: Jobs = adapter!!.getItem(position)
                // Here you can do the action you want to...
                candidateList.clear()
                currentPage = 1
                retrieveCandidateData(job.id!!)
            }

            override fun onNothingSelected(adapter: AdapterView<*>?) {

            }
        }
        binding.candidateListRv.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this,  RecyclerView.VERTICAL, false)
        binding.candidateListRv.layoutManager = layoutManager
        binding.candidateAdapter =
            CandidateAdapter(this,
                candidateList,
                object : CandidateAdapter.OnCategoryClick {

                    override fun onCategoryClicked(
                        view: View,
                        templateModel: DataAppliedCandidate
                    ) {
                        val intent =
                            Intent(this@InterestedCandidateActivity, JobSeekerDetailsActivity::class.java)
                        intent.putExtra("ARG_JOB_TITLE", templateModel)
                        startActivity(intent)
                    }


                })
        binding.candidateListRv.addOnScrollListener(object :
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
        binding.ivBack.setOnClickListener {
            finish()
        }

    }
    private fun retrieveJobData() {


        if (Utils.isNetworkAvailable(this)) {
            AndroidNetworking.get(NetworkUtils.GET_POST_JOB_BY_HR_ID_WITHOUT_PAGINATION)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllJob::class.java,
                    object : ParsedRequestListener<GetAllJob> {
                        override fun onResponse(response: GetAllJob?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    postList.addAll(it.data)
                                    adapter!!.notifyDataSetChanged()


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
                                if (it.errorCode >= 500) {
                                    binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_server_maintenance)
                                }
                            }
                            hideProgressDialog()
                        }
                    })
        } else {
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    private fun retrieveCandidateData(jobId: Int) {


        if (Utils.isNetworkAvailable(this)) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE
            AndroidNetworking.get(NetworkUtils.GET_APPLIED_CANDIDATE_LIST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("jobId", jobId.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    AppliedCandidateModel::class.java,
                    object : ParsedRequestListener<AppliedCandidateModel> {
                        override fun onResponse(response: AppliedCandidateModel?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    candidateList.addAll(it.data)

                                    if (postList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.candidateAdapter!!.notifyDataSetChanged()
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
                                        getString(R.string.no_applied_candidate_s_found)
                                }
                            }
                            hideProgressDialog()
                        }
                    })
        } else {
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    private fun hideShowEmptyView(
        isShow: Boolean,  isInternetAvailable: Boolean = true
    ) {
        binding.candidateListRv.visibility = if (isShow) View.VISIBLE else View.GONE
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
    class SpinAdapter(
        context: Context, textViewResourceId: Int,
        values: ArrayList<Jobs>
    ) : ArrayAdapter<Jobs>(context, textViewResourceId, values) {
        // Your sent context
        private val context: Context

        // Your custom values for the spinner (userJobPref)
        private val values: ArrayList<Jobs>

        init {
            this.context = context
            this.values = values
        }

        override fun getCount(): Int {
            return values.size
        }

        override fun getItem(position: Int): Jobs {
            return values[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }


        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
            val label = super.getView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            // Then you can get the current item using the values array (userJobPrefs array) and the current position
            // You can NOW reference each method you has created in your bean object (userJobPref class)
            label.text = "${values[position].vJobTitle}-${values[position].vCompanyName}-${values[position].vAddress}"

            // And finally return your dynamic (or custom) view for each spinner item
            return label
        }

        // And here is when the "chooser" is popped up
        // Normally is the same view, but you can customize it if you want
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val label = super.getDropDownView(position, convertView, parent) as TextView
            label.setTextColor(Color.BLACK)
            label.text = "${values[position].vJobTitle}-${values[position].vCompanyName}-${values[position].vAddress}"
            return label
        }
    }

    class CandidateAdapter(
        private var mActivity: Activity,
        private var dataList: ArrayList<DataAppliedCandidate>,
        private val onCategor: OnCategoryClick
    ) : RecyclerView.Adapter<CandidateAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                SinglerowjsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {

            val dataAppliedCandidate: DataAppliedCandidate = dataList[position]
            holder.binding.jsname.text = dataAppliedCandidate.userJobPref.vFirstName + " " + dataAppliedCandidate.userJobPref.vLastName
            holder.binding.qualificationjs.text = dataAppliedCandidate.userJobPref.vQualification
            holder.binding.citypref.text = dataAppliedCandidate.userJobPref. vPreferCity
            holder.binding.jsjobtype.text = dataAppliedCandidate.userJobPref.vWorkingMode
            holder.binding.jobrole.text = dataAppliedCandidate.userJobPref.vDesignation
            holder.binding.jscontact.text = dataAppliedCandidate.userJobPref.vMobile
            holder.binding.jsemail.text = dataAppliedCandidate.userJobPref.vEmail
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
            holder.itemView.setOnClickListener {
                val intent =
                    Intent(mActivity, JobSeekerDetailsActivity::class.java)
                intent.putExtra("ARG_JOB_TITLE", dataAppliedCandidate)
                mActivity.startActivity(intent)
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
            fun onCategoryClicked(view: View, templateModel: DataAppliedCandidate)
        }
    }
}