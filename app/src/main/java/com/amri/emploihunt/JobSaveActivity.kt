package com.amri.emploihunt

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityApplyListBinding
import com.amri.emploihunt.databinding.ActivityJobSaveBinding
import com.amri.emploihunt.databinding.RowPostDesignBinding
import com.amri.emploihunt.model.ApplyList
import com.amri.emploihunt.model.DataApplyList
import com.amri.emploihunt.model.DataSaveList
import com.amri.emploihunt.model.SaveListModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.bumptech.glide.Glide

class JobSaveActivity : BaseActivity() {
    lateinit var binding: ActivityJobSaveBinding
    private lateinit var saveList: MutableList<DataSaveList>

    private lateinit var layoutManager: LinearLayoutManager
    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    lateinit var prefManager: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobSaveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PrefManager.prefManager(this)
        saveList = mutableListOf()
        binding.saveListRv.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this,  RecyclerView.VERTICAL, false)
        binding.saveListRv.layoutManager = layoutManager
        binding.saveAdapter =
            SaveListAdapter(
                saveList,
                object : SaveListAdapter.OnCategoryClick {


                    override fun onCategoryClicked(view: View, templateModel: DataSaveList) {
                        val intent = Intent(this@JobSaveActivity, JobPostActivity::class.java)
                        intent.putExtra("ARG_JOB_TITLE", templateModel.job)
                        intent.putExtra("applyList", 1)
                        startActivity(intent)
                    }

                })
        retrieveSaveData()

        binding.saveListRv.addOnScrollListener(object :
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
                    retrieveSaveData()
                }
            }
        })

        binding.ivBack.setOnClickListener {
            finish()
        }

    }
    private fun retrieveSaveData() {


        if (Utils.isNetworkAvailable(this)) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.SAVE_LIST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("current_page",currentPage.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    SaveListModel::class.java,
                    object : ParsedRequestListener<SaveListModel> {
                        override fun onResponse(response: SaveListModel?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    saveList.addAll(it.data)

                                    if (saveList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.saveAdapter!!.notifyDataSetChanged()
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
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    private fun hideShowEmptyView(
        isShow: Boolean,  isInternetAvailable: Boolean = true
    ) {
        binding.saveListRv.visibility = if (isShow) View.VISIBLE else View.GONE
        binding.layEmptyView.root.visibility = if (isShow) View.GONE else View.VISIBLE
        binding.layProgressPagination.root.visibility = View.GONE
        binding.progressCircular.visibility = View.GONE
        if (isInternetAvailable) {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_job_found_save_job)
            binding.layEmptyView.btnRetry.visibility = View.GONE
        } else {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_internet)
            binding.layEmptyView.btnRetry.visibility = View.VISIBLE
            binding.layEmptyView.btnRetry.setOnClickListener {
                retrieveSaveData()
            }
        }
    }
    class SaveListAdapter(
        private var dataList: MutableList<DataSaveList>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<SaveListAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowPostDesignBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val applyModel = dataList[position]

            holder.binding.jobTitle.text = applyModel.job.vJobTitle
            holder.binding.salary.text = applyModel.job.vSalaryPackage +"LPA"
            holder.binding.experiencedDuration.text = applyModel.job.vExperience + " years"
            holder.binding.qualification.text = applyModel.job.vEducation
            holder.binding.city.text = applyModel.job.vAddress
            holder.binding.aboutPost.text = applyModel.job.tDes
            holder.binding.companyName.text = applyModel.job.vCompanyName
            holder.binding.employees.text =  "${applyModel.job.iNumberOfVacancy} Vacancy"
            holder.binding.createTimeTV.text = Utils.getTimeAgo(
                holder.itemView.context,
                applyModel.tCreatedAt!!.toLong()
            )
            Glide.with(holder.itemView.context).load(applyModel.job.tCompanyLogoUrl).into(holder.binding.profileImg)
//            onCategoryClick.onCategoryClicked(it, templateModel)
            holder.binding.executePendingBindings()
            holder.itemView.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, applyModel)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowPostDesignBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: DataSaveList)
        }
    }
}