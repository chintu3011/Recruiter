package com.amri.emploihunt

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.databinding.ActivityJobSeekarDetailsBinding
import com.amri.emploihunt.databinding.PdfViewerDialogBinding
import com.amri.emploihunt.databinding.RowJobPreferenceBinding
import com.amri.emploihunt.databinding.RowJobPreferenceForRecruiterBinding
import com.amri.emploihunt.model.DataAppliedCandidate
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.Jobs
import com.amri.emploihunt.util.Utils.serializable
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class JobSeekerDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityJobSeekarDetailsBinding
    private lateinit var selectedCandidate: DataAppliedCandidate
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobSeekarDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        selectedCandidate = intent.extras?.serializable("ARG_JOB_TITLE")!!
        binding.UserName.text = "${selectedCandidate.userJobPref.vFirstName} ${selectedCandidate.userJobPref.vLastName}"
        binding.city.text = selectedCandidate.userJobPref.vPreferCity
        binding.workingMode.text = selectedCandidate.userJobPref.vWorkingMode
        binding.tvBio.setText(selectedCandidate.userJobPref.tBio)
        binding.education.text = selectedCandidate.userJobPref.vQualification
        if (!selectedCandidate.userJobPref.vCurrentCompany.isNullOrBlank()){
            binding.layExperience.visibility = View.VISIBLE
            binding.flow12.visibility = View.VISIBLE
            binding.flow13.visibility = View.VISIBLE
            binding.lineDivider4.visibility = View.VISIBLE

            binding.tvDesigantion.text = selectedCandidate.userJobPref.vDesignation
            binding.tvCompany.text = selectedCandidate.userJobPref.vCurrentCompany
            binding.tvYear.text = "${selectedCandidate.userJobPref.vDuration} year"
            binding.tvLocation.text = selectedCandidate.userJobPref.vJobLocation

        }
        if (selectedCandidate.userJobPref.jobPreference!!.size >0){
            binding.layJobPref.visibility = View.VISIBLE
            binding.jobPreferenceRv.visibility = View.VISIBLE
            binding.lineDivider5.visibility = View.VISIBLE
            binding.jobPreferenceRv.setHasFixedSize(true)
            binding.jobPrefAdapter = JobPreferenceListAdapter(
                selectedCandidate.userJobPref.jobPreference!!,
                object : JobPreferenceListAdapter.OnCategoryClick {
                    override fun onCategoryClicked(
                        view: View,
                        templateModel: DataJobPreferenceList
                    ) {
                        val intent =
                            Intent(this@JobSeekerDetailsActivity, AddJobPreferenceActivity::class.java)
                        intent.putExtra("jobPref", templateModel)
                        intent.putExtra("update", true)
                        startActivity(intent)
                    }

                })

        }
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnopen.setOnClickListener {
            showDialog()
        }


    }

    private fun showDialog() {
        try {


            val builder = AlertDialog.Builder(this)
            val bindingDialog = PdfViewerDialogBinding.inflate(layoutInflater)

            builder.setView(bindingDialog.root)

            RetrievePDFFromURL(bindingDialog.idPDFView,bindingDialog.progressCircular).execute("https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf")
            dialog = builder.create()
            dialog?.let {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.show()
            }
            bindingDialog.closeIv.setOnClickListener {
                (dialog as AlertDialog).dismiss()
            }
        } catch (e: Exception) {
            Log.e("#####", "showProgressDialog exception: ${e.message}")
        }
    }
    class JobPreferenceListAdapter(
        private var dataList: MutableList<DataJobPreferenceList>,
        private val onCategoryClick: OnCategoryClick
    ) : RecyclerView.Adapter<JobPreferenceListAdapter.CategoriesHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
            return CategoriesHolder(
                RowJobPreferenceForRecruiterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
            val jobPrefModel = dataList[position]

            holder.binding.jobTitle.text = jobPrefModel.vJobTitle
            holder.binding.tvLocation.text = jobPrefModel.vJobLocation
            holder.binding.salary.text = jobPrefModel.vExpectedSalary+" LPA +"
            holder.binding.tvWorkingMode.text = jobPrefModel.vWorkingMode



            holder.itemView.setOnClickListener {
                notifyDataSetChanged()
                onCategoryClick.onCategoryClicked(it, jobPrefModel)
            }
        }

        override fun getItemCount(): Int {
            Log.d("###", "getItemCount: ${dataList.size}")
            return dataList.size
        }

        inner class CategoriesHolder(val binding: RowJobPreferenceForRecruiterBinding) :
            RecyclerView.ViewHolder(binding.root)

        interface OnCategoryClick {
            fun onCategoryClicked(view: View, templateModel: DataJobPreferenceList)
        }
    }
    class RetrievePDFFromURL(pdfView: PDFView,processBar: ProgressBar) :
        AsyncTask<String, Void, InputStream>() {

        // on below line we are creating a variable for our pdf view.
        val mypdfView: PDFView = pdfView

        val processBar: ProgressBar = processBar

        // on below line we are calling our do in background method.
        override fun doInBackground(vararg params: String?): InputStream? {
            // on below line we are creating a variable for our input stream.
            var inputStream: InputStream? = null
            try {
                // on below line we are creating an url
                // for our url which we are passing as a string.
                val url = URL(params.get(0))

                // on below line we are creating our http url connection.
                val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection

                // on below line we are checking if the response
                // is successful with the help of response code
                // 200 response code means response is successful
                if (urlConnection.responseCode == 200) {
                    // on below line we are initializing our input stream
                    // if the response is successful.
                    inputStream = BufferedInputStream(urlConnection.inputStream)
                }
            }
            // on below line we are adding catch block to handle exception
            catch (e: Exception) {
                // on below line we are simply printing
                // our exception and returning null
                e.printStackTrace()
                return null;
            }
            // on below line we are returning input stream.
            return inputStream;
        }

        // on below line we are calling on post execute
        // method to load the url in our pdf view.
        override fun onPostExecute(result: InputStream?) {
            // on below line we are loading url within our
            // pdf view on below line using input stream.
            mypdfView.fromStream(result).load()
            processBar.visibility = View.GONE

        }
    }
}