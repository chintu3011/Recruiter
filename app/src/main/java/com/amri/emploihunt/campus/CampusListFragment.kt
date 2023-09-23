package com.amri.emploihunt.campus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.ApplyBottomsheetBinding
import com.amri.emploihunt.databinding.FragmentCampusListBinding
import com.amri.emploihunt.databinding.RowCampusPlacementBinding
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textview.MaterialTextView
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
    ): View {

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
                                    Log.d("#####", "onResponse: ${filteredDataList[1].tVacancy}")
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
                RowCampusPlacementBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ),
                mActivity
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val campusModel = dataList[position]

            Log.d(">>>>>", "onBindViewHolder: ${campusModel.tVacancy}")

            holder.onBind(campusModel)
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowCampusPlacementBinding, mActivity: Activity): RecyclerView.ViewHolder(binding.root){


            private val rowList:MutableList<TableRow> = mutableListOf()
            private var showFullList:Boolean = false
            @SuppressLint("NotifyDataSetChanged")
            fun onBind(campusModel: DataCampus) {
                if (campusModel.iIsApplied ==1 ){
                    binding.btnRegister.text = mActivity.resources.getString(R.string.already_applied)
                    binding.btnRegister.setTextColor(ContextCompat.getColor(mActivity, R.color.blue))
                    binding.btnRegister.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mActivity, R.color.disable_blue))
                }else{
                    binding.btnRegister.text = mActivity.resources.getString(R.string.register)
                    binding.btnRegister.setTextColor(ContextCompat.getColor(mActivity, R.color.white))
                    binding.btnRegister.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(mActivity, R.color.blue))

                }
                binding.collegeName.text = campusModel.vCampusName
                binding.campusAddress.text = campusModel.tCampusAddress

                val vacancyList = splitVacancy(campusModel.tVacancy)

                Log.d("^^^^^", "onBind: vacancyList = \n$campusModel.tVacancy")
                Log.d(",,,,,,", "onBind: vacancyList = \n$vacancyList")
                createTableRow(binding.vacancyTable,vacancyList)
                createQualificationChips(binding.qualificationChipGrp,campusModel.vQulification.split(","))

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
                    binding.txtRemainTime.text = mActivity.getString(R.string.expire1,days,hour,minutes)
                }else if (hour != 0){
                    binding.txtRemainTime.text = mActivity.getString(R.string.expire2,hour,minutes)
                }else if (minutes != 0){
                    binding.txtRemainTime.text = mActivity.getString(R.string.expire3,minutes)
                }
                
                binding.btnShowRows.setOnClickListener{
                    showFullList = !showFullList
                    createTableRow(binding.vacancyTable,vacancyList)
                }
                /*binding.executePendingBindings()*/
                binding.btnRegister.setOnClickListener {
                    notifyDataSetChanged()
                    onCategoryClick.onCategoryClicked(it, campusModel,position)
                }
            }

            private fun createQualificationChips(
                qualificationChipGrp: ChipGroup,
                list: List<String>
            ) {
                qualificationChipGrp.removeAllViews()
                for(qualification in list){
                    val chip = LayoutInflater.from(mActivity).inflate(R.layout.single_chip_qualification,null) as Chip

                    chip.text = qualification

                    chip.isCheckable = false
                    chip.isClickable = false

                    qualificationChipGrp.addView(chip)
                }
            }

            private fun splitVacancy(vacancy:String): MutableMap<String, String> {
                val map:MutableMap<String,String>  = mutableMapOf()
                for(str in vacancy.split(","))
                {
                    Log.d("......", "splitVacancy: $str")
                    val list = str.split("-")
                    val vacCount:String = list[1].trim().subSequence(0,list[1].trim().indexOf(" ")).toString()
                    if(map.containsKey(list[0].trim())){
                        map[list[0].trim()] = (vacCount.toInt() + (map[list[0].trim()]?.toInt() ?: 0)).toString()
                    }
                    else {
                        map[list[0].trim()] = vacCount
                    }
                }
                return map
            }

            private fun createTableRow(table:TableLayout,tableData:MutableMap<String,String>){

                table.removeAllViews()
                if (tableData.size > 3){
                    binding.btnShowRows.visibility = View.VISIBLE
                }
                else{
                    binding.btnShowRows.visibility = View.GONE
                }
                val tileRow = LayoutInflater.from(mActivity).inflate(R.layout.vacancy_table_title_row, null) as TableRow

                table.addView(tileRow)

                val entryList = tableData.entries.toList()

                if(entryList.isNotEmpty()) {
                    if (showFullList) {
                        binding.btnShowRows.setImageResource(R.drawable.ic_up)

                        for (entry in entryList) {
                            val row = LayoutInflater.from(mActivity)
                                .inflate(R.layout.vacancy_table_row, null) as TableRow

                            val role = row.findViewById<MaterialTextView>(R.id.role)
                            val vacancyCount = row.findViewById<MaterialTextView>(R.id.vacancyCount)

                            role.text = entry.key.trim()
                            vacancyCount.text = entry.value.trim()

                            table.addView(row)
                        }

                    } else {
                        binding.btnShowRows.setImageResource(R.drawable.ic_down)

                        for (index in 0 until 3) {
                            if (index < entryList.size) {
                                val row = LayoutInflater.from(mActivity)
                                    .inflate(R.layout.vacancy_table_row, null) as TableRow

                                val role = row.findViewById<MaterialTextView>(R.id.role)
                                val vacancyCount =
                                    row.findViewById<MaterialTextView>(R.id.vacancyCount)

                                role.text = entryList[index].key.trim()
                                vacancyCount.text = entryList[index].value.trim()

                                table.addView(row)
                            } else {
                                break
                            }
                        }
                    }
                }
            }

        }

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
                                    filteredDataList[position].iIsApplied = 1
                                    binding.campusAdapter!!.notifyItemChanged(position)
                                    hideProgressDialog()
                                    bottomSheetDialog.dismiss()

                                }
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
                if (user.tVacancy.lowercase(Locale.ROOT)
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