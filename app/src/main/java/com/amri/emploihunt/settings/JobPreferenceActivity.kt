package com.amri.emploihunt.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityJobPreferenceBinding
import com.amri.emploihunt.databinding.RowJobPreferenceBinding
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.GetJobPreferenceList
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_ADDED_JOB_PREFERENCE
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.prefManager
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener

class JobPreferenceActivity : BaseActivity() {
    lateinit var binding: ActivityJobPreferenceBinding
    lateinit var prefManager: SharedPreferences
    var jobPreferenceList: ArrayList<DataJobPreferenceList> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager  = prefManager(this)
        binding.jobPreferenceRv.setHasFixedSize(true)
        binding.jobPrefAdapter = JobPreferenceListAdapter(jobPreferenceList, object : JobPreferenceListAdapter.OnCategoryClick {
            override fun onCategoryClicked(view: View, templateModel: DataJobPreferenceList) {
                val intent = Intent(this@JobPreferenceActivity, AddJobPreferenceActivity::class.java)
                intent.putExtra("jobPref",templateModel)
                intent.putExtra("update",true)
                startActivity(intent)
            }

        })

        binding.addJobPreference.setOnClickListener {
            val intent = Intent(this@JobPreferenceActivity, AddJobPreferenceActivity::class.java)
            intent.putExtra("update",false)
            startActivity(intent)

        }


        getJobPreference()

        binding.toolbar.menu.clear()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private  fun getJobPreference(){
        jobPreferenceList.clear()
        if (Utils.isNetworkAvailable(this)) {
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
                                    jobPreferenceList.addAll(it.data)
                                    binding.jobPrefAdapter!!.notifyDataSetChanged()

                                    binding.tvCountPreference.text ="${response.total_records}/5"
                                    if (response.total_records == 5){
                                        binding.addJobPreference.setTextColor(resources.getColorStateList(
                                            R.color.dark_grey
                                        ))
                                        binding.addJobPreference.text =
                                            getString(R.string.opps_job_preference_full)
                                        binding.addJobPreference.isClickable = false
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
            Utils.showNoInternetBottomSheet(this,this)
        }
    }
    class JobPreferenceListAdapter(
        private var dataList: MutableList<DataJobPreferenceList>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<JobPreferenceListAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowJobPreferenceBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobPrefModel = dataList[position]

            holder.binding.jobTitle.text = jobPrefModel.vJobTitle
            holder.binding.tvLocation.text = jobPrefModel.vJobLocation
            holder.binding.salary.text = jobPrefModel.vExpectedSalary+" LPA +"



            holder.itemView.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, jobPrefModel)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowJobPreferenceBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: DataJobPreferenceList)
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