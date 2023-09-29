package com.amri.emploihunt.recruiterSide

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.amri.emploihunt.basedata.BaseFragment
import com.amri.emploihunt.databinding.FragmentHomeRecruitBinding
import com.amri.emploihunt.databinding.RowAppicationsBinding
import com.amri.emploihunt.filterFeature.FilterParameterTransferClass
import com.amri.emploihunt.jobSeekerSide.JobPostActivity
import com.amri.emploihunt.messenger.MessaengerHomesActivity_2
import com.amri.emploihunt.messenger.MessengerHomeActivity
import com.amri.emploihunt.model.GetAllUsers
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.model.User
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.FIREBASE_ID
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.ROLE
import com.amri.emploihunt.util.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import com.skydoves.balloon.showAlignLeft
import com.skydoves.balloon.showAlignTop
import java.util.Locale

class HomeRecruitFragment : BaseFragment(),ApplicationListUpdateListener,
    FilterParameterTransferClass.FilterApplicationListener {

    private lateinit var binding:FragmentHomeRecruitBinding

    lateinit var fragview: View
    private lateinit var database: DatabaseReference

    private lateinit var dataList: MutableList<User>
    private lateinit var filteredDataList: MutableList<User>
    private var userType: Int? = null
    private var userId: String? = null

    private lateinit var layoutManager: LinearLayoutManager

    private var firstVisibleItemPosition = 0
    private var isScrolling = false
    private var currentItems = 0
    private var currentPage = 1
    private var totalItems = 0
    private var totalPages = 1

    lateinit var prefManager: SharedPreferences

    private lateinit var balloon: Balloon

    companion object {
        private const val TAG = "HomeRecruitFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /*val bundle = arguments
        if (bundle != null) {
            userType = bundle.getInt("userType")
        }*/
        prefManager = prefManager(requireContext())
        userType = prefManager.get(ROLE,0)
        userId = prefManager.get(FIREBASE_ID)

        Log.d(TAG,"User type : $userType")

        // Inflate the layout for this fragment
        binding = FragmentHomeRecruitBinding.inflate(layoutInflater)

        FilterParameterTransferClass.instance!!.setApplicationListener(this)


        database = FirebaseDatabase.getInstance().reference
        binding.imgOpenDrawer.visibility = View.VISIBLE
        initDrawersData()
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
            filteredDataList.clear()
            currentPage = 1
            retrieveJsData()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        binding.imgOpenDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        val balloonLayout = LayoutInflater.from(requireContext()).inflate(R.layout.messenger_balloon,null,false)
        val txtMessage = balloonLayout.findViewById<MaterialTextView>(R.id.txtMessage)
        txtMessage.text = "Chat with \n Job Seekers!."

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
        balloon.dismissWithDelay(5000)
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
    private fun retrieveJsData() {

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
                                    binding.layEmptyView.tvNoData.text =
                                        getString(R.string.opps_sorry_job_seeker_not_available_at_the_moment)
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

   /* @SuppressLint("NotifyDataSetChanged")
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
    }*/

     @SuppressLint("NotifyDataSetChanged")
    override fun onDataReceivedFilterApplicationList(
        domain: String,
        location: String,
        workingMode: String,
        packageRange: String
    ) {
         Log.d(TAG,"${domain}, ${location},${workingMode} ,${packageRange}")

        filteredDataList.clear()
        if (domain.isNotEmpty() || location.isNotEmpty() || workingMode.isNotEmpty() || packageRange.isNotEmpty()) {

            for (application in dataList) {
                val domainMatches = if (application.vDesignation.isNullOrEmpty()){
                    false
                }
                else if (domain.isNullOrEmpty()) {
                    Log.d(TAG, "onDataReceivedFilterApplicationList: $application")
                    application.vDesignation.contains(
                        domain.substring(
                            0,
                            if (domain.indexOf(" ") != -1) domain.indexOf(" ")
                            else domain.length
                        ),
                        ignoreCase = true)
                }
                else {
                    true
                }
                val locationMatches = if (location.isNotEmpty()){

                    application.vPreferCity.contains(
                        location.substring(
                            0,
                            if (location.indexOf(" ") != -1) location.indexOf(" ")
                            else location.length
                        ),
                        ignoreCase = true)
                }
                else{
                    true
                }
                val workingModeMatches = if (workingMode.isNotEmpty()){

                    application.vWorkingMode.contains(
                        workingMode.substring(
                            0,
                            if (workingMode.indexOf(" ") != -1) workingMode.indexOf(" ")
                            else workingMode.length
                        ),
                        ignoreCase = true)

                }
                else{
                    true
                }
                val packageMatches = if(packageRange.isNotEmpty()){
                    application.vExpectedSalary.trim().toInt() >= packageRange.toInt()
                }
                else{
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
                RowAppicationsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {

            val job: User = applicationList[position]
            holder.binding.applicantName.text = job.vFirstName.plus(" ").plus(job.vLastName)
            holder.binding.applicantQualification.text = job.vQualification
            holder.binding.applicantPrefCity.text = job. vPreferCity
            holder.binding.applicantWorkingMode.text = job.vWorkingMode
            holder.binding.applicantDesignation.text = job.vDesignation
            Glide.with(mActivity)
                .load(NetworkUtils.BASE_URL_MEDIA+job.tProfileUrl)
                .apply(
                    RequestOptions
                        .placeholderOf(R.drawable.profile_default_image)
                        .error(R.drawable.profile_default_image)
                        .circleCrop()
                )
                .into(holder.binding.profileImg)
            var callBalloon: Balloon ?= null
            var emailBalloon: Balloon ?= null
            val phoneNo: String =  job.vMobile
            if (phoneNo.isNotEmpty()) {
                callBalloon = createMsgBalloon(phoneNo, R.drawable.ic_call, mActivity)
                if (callBalloon != null) {
                    callBalloon.setOnBalloonClickListener {
                        makePhoneCall(phoneNo)
                        callBalloon!!.dismiss()
                    }
                    callBalloon.setOnBalloonOutsideTouchListener { view, motionEvent ->
                        callBalloon!!.dismiss()
                    }
                } else {
                    Toast.makeText(mActivity ,mActivity.getString(R.string.something_error), Toast.LENGTH_SHORT).show()
                }
            } else {

                callBalloon = createMsgBalloon(
                    "Phone no. Not Found",
                    R.drawable.ic_call,
                    mActivity
                )
                if (callBalloon != null) {
                    callBalloon.setOnBalloonOutsideTouchListener { view, motionEvent ->
                        callBalloon.dismiss()
                    }
                } else {
                    Toast.makeText(mActivity ,mActivity.getString(R.string.something_error), Toast.LENGTH_SHORT).show()
                }
            }
            holder.binding.btnPhone.setOnClickListener {

                if(callBalloon != null){
                    holder.binding.btnPhone.showAlignTop(callBalloon)
                }

            }

            val email = job.vEmail
            if (email.isNotEmpty()) {
                emailBalloon = createMsgBalloon(email, R.drawable.ic_email, mActivity)

                if (emailBalloon != null) {
                    emailBalloon.setOnBalloonClickListener {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                        intent.type = "message/rfc822"
                        holder.itemView.context.startActivity(Intent.createChooser(intent, "Choose an Email Client: "))
                        emailBalloon!!.dismiss()
                    }
                } else {
                    Toast.makeText(mActivity ,mActivity.getString(R.string.something_error), Toast.LENGTH_SHORT).show()
                }
            } else {
                emailBalloon = createMsgBalloon(
                    "Email Id Not Found",
                    R.drawable.ic_email,
                    mActivity
                )
                if (emailBalloon != null) {
                    emailBalloon.setOnBalloonOutsideTouchListener { view, motionEvent ->
                        emailBalloon.dismiss()
                    }
                } else {
                    Toast.makeText(mActivity ,mActivity.getString(R.string.something_error), Toast.LENGTH_SHORT).show()
                }
            }

            holder.binding.btnEmail.setOnClickListener {
                if(emailBalloon != null){
                    holder.binding.btnEmail.showAlignTop(emailBalloon)
                }

            }

        }

        private fun createMsgBalloon(msg: String, icon: Int, baseContext: Context): Balloon {
            val balloon = createBalloon(baseContext){
                setWidth(BalloonSizeSpec.WRAP)
                setHeight(BalloonSizeSpec.WRAP)
                setText(msg)
                setText(msg)
                setTextSize(8f)
                setTextTypeface(Typeface.BOLD)
                setTextColorResource(R.color.black)
                setTextGravity(Gravity.CENTER)
                setIconDrawableResource(icon)
                setIconHeight(12)
                setIconWidth(12)
                setIconColorResource(R.color.black)
                setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                setArrowSize(8)
                setArrowPosition(0.5f)
                setPadding(12)
                setCornerRadius(8f)
                setBackgroundColorResource(R.color.white)
                setElevation(3)
                setBalloonAnimation(BalloonAnimation.ELASTIC)
                setLifecycleOwner(lifecycleOwner)
                build()
            }

            return balloon
        }
        private fun makePhoneCall(num: String) {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
            mActivity.startActivity(dialIntent)
        }
        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${applicationList.size}")
            return applicationList.size
        }

        inner class CategoriesHolder(val binding: RowAppicationsBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: Jobs)
        }
    }
}