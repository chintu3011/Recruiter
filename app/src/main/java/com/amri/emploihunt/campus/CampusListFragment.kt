package com.amri.emploihunt.campus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.ApplyBottomsheetBinding
import com.amri.emploihunt.databinding.FragmentCampusListBinding
import com.amri.emploihunt.databinding.RowCampusBinding
import com.amri.emploihunt.jobSeekerSide.JobListUpdateListener
import com.amri.emploihunt.model.ApplyModel
import com.amri.emploihunt.model.DataCampus
import com.amri.emploihunt.model.GetAllCampus
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar
import java.util.GregorianCalendar

class CampusListFragment : BaseFragment(), JobListUpdateListener {

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var  bottomSheetDialog : BottomSheetDialog
    private lateinit var filteredDataList: MutableList<DataCampus>
    lateinit var binding:FragmentCampusListBinding
    lateinit var prefManager : SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCampusListBinding.inflate(layoutInflater)
        prefManager = prefManager(requireContext())
        filteredDataList = mutableListOf()
        binding.campusListRv.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireContext(),  RecyclerView.VERTICAL, false)
        binding.campusListRv.layoutManager = layoutManager
        binding.campusAdapter =
            CampusAdapter(requireActivity(), filteredDataList, object : CampusAdapter.OnCategoryClick {
                override fun onCategoryClicked(view: View, templateModel: DataCampus, position: Int) {
                    showApplyBottomSheet(templateModel,position)
                }
            })

        retrieveCampusData("")

        binding.campusListRv.addOnScrollListener(object :
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

                    retrieveCampusData("")
                }
            }
        })


        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true

            binding.campusListRv.visibility = View.GONE
            binding.layEmptyView.root.visibility  = View.GONE
            filteredDataList.clear()
            currentPage = 1
            retrieveCampusData("")
            binding.swipeRefreshLayout.isRefreshing = false
        }
        return binding.root
    }

    private fun retrieveCampusData(tag: String) {
        if (Utils.isNetworkAvailable(requireContext())) {
            if (currentPage != 1 && currentPage > totalPages) {
                return
            }
            if (currentPage != 1) binding.layProgressPagination.root.visibility = View.VISIBLE

            if (currentPage == 1) binding.progressCircular.visibility = View.VISIBLE

            AndroidNetworking.get(NetworkUtils.GET_ALL_CAMPUS)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("current_page",currentPage.toString())
                .addQueryParameter("tag",tag)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCampus::class.java,
                    object : ParsedRequestListener<GetAllCampus> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(response: GetAllCampus?) {
                            try {
                                response?.let {
                                    hideProgressDialog()
                                    Log.d("#####", "onResponse: ${it.data}")
                                    filteredDataList.addAll(it.data)
                                    if (filteredDataList.isNotEmpty()) {
                                        totalPages = it.total_pages
                                        binding.campusAdapter!!.notifyDataSetChanged()
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
                                    binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_campus_found)
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
        binding.campusListRv.visibility = if (isShow) View.VISIBLE else View.GONE
        binding.layEmptyView.root.visibility = if (isShow) View.GONE else View.VISIBLE
        binding.layProgressPagination.root.visibility = View.GONE
        binding.progressCircular.visibility = View.GONE
        if (isInternetAvailable) {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_campus_found)
            binding.layEmptyView.btnRetry.visibility = View.GONE
        } else {
            binding.layEmptyView.tvNoData.text = resources.getString(R.string.msg_no_internet)
            binding.layEmptyView.btnRetry.visibility = View.VISIBLE
            binding.layEmptyView.btnRetry.setOnClickListener {
                retrieveCampusData("")
            }
        }
    }
    class CampusAdapter(
        private var mActivity: Activity,
        private var dataList: MutableList<DataCampus>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<CampusAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowCampusBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val campusModel = dataList[position]

            if (campusModel.iIsApplied ==1 ){
                holder.binding.btnRegistration.text = mActivity.resources.getString(R.string.already_applied)
                holder.binding.btnRegistration.setBackgroundDrawable(mActivity.resources.getDrawable(R.drawable.btn_campus_shape_disble))

            }else{
                holder.binding.btnRegistration.text = mActivity.resources.getString(R.string.register)
                holder.binding.btnRegistration.setBackgroundDrawable(mActivity.resources.getDrawable(R.drawable.btn_campus_shape))

            }
            holder.binding.tvName.text = campusModel.vCampusName
            holder.binding.tvAddress.text = campusModel.tCampusAddress
            var vacancy = ""
            for (str in campusModel.tVacancy.split(",")){
                vacancy += "$str\n"
            }
            var qualification = ""
            for (str in campusModel.vQulification.split(",")){
                qualification += "$str\n"
            }
            holder.binding.tvVacancy.text = vacancy
            holder.binding.tvQulication.text = qualification

            val calendar: Calendar = GregorianCalendar()
            val time = (calendar.timeInMillis / 1000).toString()
            val differenceSec =(campusModel.tRegistrationEndDate.toInt() - time.toInt())
            val days =  differenceSec / (60 * 60 * 24)
            val remainder1 = differenceSec % 86400
            val hour =  remainder1 / (60 * 60)
            val remainder = differenceSec % 3600
            val minutes =  remainder / 60
            Log.d("###", "onBindViewHolder: $differenceSec")
            if (days!=0){

               holder.binding.remingTime.text = mActivity!!.getString(R.string.expire1,days,hour,minutes)
            }else if (hour != 0){
               holder.binding.remingTime.text = mActivity!!.getString(R.string.expire2,hour,minutes)
            }else if (minutes != 0){
               holder.binding.remingTime.text = mActivity.getString(R.string.expire3,minutes)

            }


//            onCategoryClick.onCategoryClicked(it, templateModel)
            holder.binding.executePendingBindings()
            holder.binding.btnRegistration.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, campusModel,position)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowCampusBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: DataCampus, position: Int)
        }
    }
    fun showApplyBottomSheet(campusPlacement: DataCampus, position: Int) {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val binding_ = ApplyBottomsheetBinding.inflate(layoutInflater)
        binding_.tvDes.text =
            getString(R.string.are_you_want_to_sure_for_apply_for_placement, campusPlacement.vCampusName)
        binding_.animationView.setAnimation(R.raw.excemilation_mark)
        binding_.btnOk.setOnClickListener {

            callApplyJob(campusPlacement,position)
        }
        binding_.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(binding_.root)
        bottomSheetDialog.show()
    }
    private fun callApplyJob(campus: DataCampus, position: Int) {

        if (Utils.isNetworkAvailable(requireContext())){

            AndroidNetworking.post(NetworkUtils.CAMPUS_APPLY)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addQueryParameter("campusId", campus.id.toString())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    ApplyModel::class.java,
                    object : ParsedRequestListener<ApplyModel> {
                        override fun onResponse(response: ApplyModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()

                                    filteredDataList[position].iIsApplied = 1
                                    binding.campusAdapter!!.notifyItemChanged(position)
                                    hideProgressDialog()
                                    bottomSheetDialog.dismiss()

                                }
                                //hideProgressDialog()
                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                                hideProgressDialog()
                            }
                        }

                        override fun onError(anError: ANError?) {
                            try {

                                anError?.let {
                                    Log.e(
                                        "#####",
                                        "onError: code: ${it.errorCode} & message: ${it.errorBody}"
                                    )
                                    /** errorCode == 404 means User number is not registered or New user */
                                    hideProgressDialog()
                                }
                            } catch (e: Exception) {
                                Log.e("#####", "onError: ${e.message}")
                            }
                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(requireContext(),requireActivity())
        }


    }

    override fun updateJobList(query: String) {
        /*filteredDataList.clear()
        if (!TextUtils.isEmpty(query)){
            for (user in dataList) {
                if (user.tVacancy!!.lowercase(Locale.ROOT)
                        .contains(query.lowercase(Locale.ROOT))
                ) {
                    filteredDataList.add(user)
                }
            }
        }
        else{
            filteredDataList.addAll(dataList)
        }
        binding.campusAdapter!!.notifyDataSetChanged()*/
        binding.campusListRv.visibility = View.GONE
        binding.layEmptyView.root.visibility  = View.GONE
        filteredDataList.clear()
        Log.d("#####", "updateJobList: ${filteredDataList.count()}")
        currentPage = 1
        retrieveCampusData(query)
    }

    override fun backToSearchView() {
        binding.campusListRv.visibility = View.GONE
        binding.layEmptyView.root.visibility  = View.GONE
        filteredDataList.clear()
        currentPage = 1
        retrieveCampusData("")
    }
}