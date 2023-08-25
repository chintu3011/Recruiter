package com.amri.emploihunt

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityYourJosPostListBinding
import com.amri.emploihunt.databinding.RowPostRecruiterBinding
import com.amri.emploihunt.model.DeletePostModel
import com.amri.emploihunt.model.GetAllJob
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialog

class YourJosPostListActivity : BaseActivity() {
    lateinit var binding: ActivityYourJosPostListBinding
    private lateinit var postList: MutableList<Jobs>

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
        binding = ActivityYourJosPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PrefManager.prefManager(this)
        postList = mutableListOf()
        binding.postListRv.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this,  RecyclerView.VERTICAL, false)
        binding.postListRv.layoutManager = layoutManager
        binding.postAdapter =
            PostListAdapter(
                postList,
                object : PostListAdapter.OnCategoryClick {
                    override fun onCategoryClicked(view: View, templateModel: Jobs) {
                        val intent = Intent(this@YourJosPostListActivity, UpdatePostActivity::class.java)
                        intent.putExtra("ARG_JOB_TITLE", templateModel)
                        changePostLauncher.launch(intent)

                    }

                    override fun onDelete(view: View, templateModel: Jobs) {
                        showCommonDialog(false, "Are you want to sure delete job post?",
                            resources.getString(R.string.yes), resources.getString(R.string.no),
                            {
                                callDeleteJob(templateModel)

                            },
                            {
                                it.dismiss()
                            })

                    }

                })
        retrieveJobData()

        binding.postListRv.addOnScrollListener(object :
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

    private fun callDeleteJob(templateModel: Jobs) {
        if (Utils.isNetworkAvailable(this)) {
            showProgressDialog("")
            AndroidNetworking.post(NetworkUtils.DELETE_POST)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("jobId",templateModel.id.toString())
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    DeletePostModel::class.java,
                    object : ParsedRequestListener<DeletePostModel> {
                        override fun onResponse(response: DeletePostModel?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("###", "onResponse: ${it.data}")
                                    toast("Delete Job post Successfully")

                                    showDeleteBottomSheet(templateModel)


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

    private fun retrieveJobData() {


        if (Utils.isNetworkAvailable(this)) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.GET_POST_JOB_BY_HR_ID)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("current_page",currentPage.toString())
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

                                    if (postList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.postAdapter!!.notifyDataSetChanged()
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
    var changePostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                postList.clear()
                currentPage = 1
                retrieveJobData()
            }
        }
    private fun hideShowEmptyView(
        isShow: Boolean,  isInternetAvailable: Boolean = true
    ) {
        binding.postListRv.visibility = if (isShow) View.VISIBLE else View.GONE
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
    fun showDeleteBottomSheet(templateModel: Jobs) {

        val dialog = BottomSheetDialog(this)
        val view: View = (this).layoutInflater.inflate(
            R.layout.logout_bottomsheet,
            null
        )


        val btnyes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)
        val tv_des = view.findViewById<TextView>(R.id.tv_des1)
        val animation = view.findViewById<LottieAnimationView>(R.id.animationView)
        tv_des.text = getString(R.string.your_job_post_deleted_successfully)
        btnNo.visibility = View.GONE
        btnyes.text = resources.getString(R.string.ok)
        animation.setAnimation(R.raw.delete)


        btnyes.setOnClickListener {
            postList.remove(templateModel)
            binding.postAdapter!!.notifyDataSetChanged()
            dialog.dismiss()



        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }


        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()

    }
    class PostListAdapter(
        private var dataList: MutableList<Jobs>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<PostListAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowPostRecruiterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val job = dataList[position]

            holder.binding.jobTitle.text =job.vJobTitle
            holder.binding.salary.text =job.vSalaryPackage +"LPA"
            holder.binding.experiencedDuration.text =job.vExperience + " years"
            holder.binding.qualification.text =job.vEducation
            holder.binding.city.text =job.vAddress
            holder.binding.companyName.text =job.vCompanyName
            holder.binding.employees.text =  "${job.iNumberOfVacancy} Vacancy"
            holder.binding.createTimeTV.text = Utils.getTimeAgo(
                holder.itemView.context,
               job.tCreatedAt!!.toLong()
            )

//            onCategoryClick.onCategoryClicked(it, templateModel)
            holder.binding.executePendingBindings()
            holder.binding.editButton.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, job)
            }
            holder.binding.deleteButton.setOnClickListener {
                onCategoryClick.onDelete(it, job)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowPostRecruiterBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: Jobs)
            fun onDelete(view: View, templateModel: Jobs)
        }
    }
}