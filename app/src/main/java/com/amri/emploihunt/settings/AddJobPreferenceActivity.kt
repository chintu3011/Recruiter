package com.amri.emploihunt.settings

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityAddJobPreferenceBinding
import com.amri.emploihunt.databinding.PickerDialogBinding
import com.amri.emploihunt.model.DataJobPreferenceList
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.model.InsertJobPreferenceModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.AUTH_TOKEN
import com.amri.emploihunt.util.IS_ADDED_JOB_PREFERENCE
import com.amri.emploihunt.util.PrefManager

import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.Utils
import com.amri.emploihunt.util.Utils.serializable
import com.amri.emploihunt.util.Utils.toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.paulrybitskyi.valuepicker.ValuePickerView
import com.paulrybitskyi.valuepicker.model.Item
import com.paulrybitskyi.valuepicker.model.PickerItem

import org.json.JSONObject


class AddJobPreferenceActivity : BaseActivity() {
    private var dialog: Dialog? = null
    lateinit var binding: ActivityAddJobPreferenceBinding
    lateinit var expectedSalary: List<String>
    lateinit var string: List<Item>
    /*var cityList: ArrayList<String> = ArrayList()*/
    var cityValidator = false
    var expected = String()
    lateinit var prefManager: SharedPreferences
    private lateinit var selectedjobPreference : DataJobPreferenceList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddJobPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.extras!!.getBoolean("update")){
            selectedjobPreference = intent.extras?.serializable("jobPref")!!
            binding.jobTitle.setText(selectedjobPreference.vJobTitle)
            binding.cityTitle.setText(selectedjobPreference.vJobLocation)
            binding.tvSalary.setText(getString(R.string.lpa, selectedjobPreference.vExpectedSalary))
            expected = selectedjobPreference.vExpectedSalary

            when(selectedjobPreference.vWorkingMode){

                binding.radioBtnHybrid.text -> binding.radioBtnHybrid.isChecked = true
                binding.radioBtnOnsite.text -> binding.radioBtnOnsite.isChecked = true
                binding.radioBtnRemote.text -> binding.radioBtnRemote.isChecked = true
            }

            binding.btnAdd.setText(getString(R.string.update))


        }
        prefManager = PrefManager.prefManager(this)
        /*getAllCity()*/
        binding.textLayoutSalaryTitle.setOnClickListener {
            showDialog("Expected Salary")
        }
        expectedSalary = resources.getStringArray(R.array.expected_salary).toList()

        val cityList:ArrayList<String> = arrayListOf()
        getAllCity(cityList){
            if(cityList.isNotEmpty()){
                val adapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, cityList)
                binding.cityTitle.setAdapter(adapter)
            }
        }


        binding.cityTitle.validator = object : AutoCompleteTextView.Validator {
            override fun isValid(text: CharSequence): Boolean {
                Log.v("Test", "Checking if valid: $text ${cityList.contains(text.toString())}")

                if (cityList.contains(text.toString())) {
                    cityValidator = true
                    return true
                }
                cityValidator = false
                return false
            }

            override fun fixText(invalidText: CharSequence): CharSequence {
                // If .isValid() returns false then the code comes here
                // do whatever way you want to fix in the
                // users input and  return it
                binding.cityTitle.error = "Please select city in list"
                return ""
            }
        }
        binding.cityTitle.setOnFocusChangeListener { view, b ->
            if (view.id === R.id.cityTitle && !b) {
                (view as AutoCompleteTextView).performValidation()

            }
        }

        binding.btnAdd.setOnClickListener {
            binding.cityTitle.performValidation()
            if (checkValidation()){

                if (binding.btnAdd.text == getString(R.string.update))
                {
                    callUpdateJobPreference()

                }else{
                    callAddJobPreference()
                }


            }

        }

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

    private fun checkValidation(): Boolean {

        if (binding.jobTitle.text.isNullOrBlank() ){
            binding.jobTitle.error = "Please Enter job title"
            return false
        }else if (binding.radioGrpWorkingMode.checkedRadioButtonId == -1 ){
            toast("Please select working mode")
            return false
        }else if (expected.isNullOrBlank() ) {
            binding.tvSalary.error = "Please select Expected Salary"
            return false
        }else if (binding.cityTitle.text.isNullOrBlank() && !cityValidator) {
            binding.cityTitle.error  = "Please choose Job Location"
            return false
        }else {
            return true
        }

    }


    private fun showDialog(msg: String) {
        try {


            val builder = AlertDialog.Builder(this)
            val bindingDialog = PickerDialogBinding.inflate(layoutInflater)
            bindingDialog.pickedTeam.text = msg
            bindingDialog.teamPicker.items = generateTeamPickerItems()
            bindingDialog.teamPicker.onItemSelectedListener = ValuePickerView.OnItemSelectedListener {
                expected = it.title

            }

            builder.setView(bindingDialog.root)
            dialog = builder.create()
            dialog?.let {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.show()
            }
            dialog!!.setOnCancelListener {
                binding.tvSalary.text = getString(R.string.lpa, expected)
            }
        } catch (e: Exception) {
            Log.e("#####", "showProgressDialog exception: ${e.message}")
        }
    }
    private fun generateTeamPickerItems(): List<Item> {
        return expectedSalary.map {
            PickerItem(
                id = 0,
                title = it,
                payload = ""
            )
        }
    }
   /* private fun getAllCity(){

        if (Utils.isNetworkAvailable(this)){

            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {

                                cityList.addAll(response!!.data)



                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")

                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )


                            }


                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(this, this)
        }

    }*/
    private fun callAddJobPreference() {

        if (Utils.isNetworkAvailable(this)){
            val jsonObject = JSONObject()
            jsonObject.put("vJobTitle", binding.jobTitle.text!!.toString().trim())
            //jsonObject.put(FCM_TOKEN, prefManager[FCM_TOKEN, ""])

            jsonObject.put("vWorkingMode",getSelectedRadioItem(binding.radioGrpWorkingMode))
            jsonObject.put("vExpectedSalary", expected.split(" ")[0])
            jsonObject.put("vJobLocation", binding.cityTitle.text.toString().trim())

            AndroidNetworking.post(NetworkUtils.JOB_PREFERENCE)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    InsertJobPreferenceModel::class.java,
                    object : ParsedRequestListener<InsertJobPreferenceModel> {
                        override fun onResponse(response: InsertJobPreferenceModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()
                                    binding.jobTitle.text!!.clear()
                                    binding.radioGrpWorkingMode.clearCheck()
                                    binding.tvSalary.text = resources.getString(R.string.expected_salary)
                                    binding.cityTitle.text.clear()
                                    toast("Job preference added successfully")
                                    IS_ADDED_JOB_PREFERENCE = true
                                    hideProgressDialog()
                                    finish()


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
            Utils.showNoInternetBottomSheet(this,this)
        }


    }
    private fun callUpdateJobPreference() {

        if (Utils.isNetworkAvailable(this)){
            val jsonObject = JSONObject()
            jsonObject.put("id", selectedjobPreference.id)
            jsonObject.put("vJobTitle", binding.jobTitle.text.toString().trim())
            jsonObject.put("vWorkingMode",getSelectedRadioItem(binding.radioGrpWorkingMode))
            jsonObject.put("vExpectedSalary", expected.split(" ")[0])
            jsonObject.put("vJobLocation", binding.cityTitle.text.toString().trim())

            AndroidNetworking.post(NetworkUtils.JOB_PREFERENCE_UPDATE)
                .addHeaders("Authorization", "Bearer " + prefManager[AUTH_TOKEN, ""])
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    InsertJobPreferenceModel::class.java,
                    object : ParsedRequestListener<InsertJobPreferenceModel> {
                        override fun onResponse(response: InsertJobPreferenceModel?) {
                            try {
                                response?.let {
                                    //hideProgressDialog()
                                    binding.jobTitle.text!!.clear()
                                    binding.radioGrpWorkingMode.clearCheck()
                                    binding.tvSalary.text = resources.getString(R.string.expected_salary)
                                    binding.cityTitle.text.clear()
                                    toast("Job preference added successfully")
                                    IS_ADDED_JOB_PREFERENCE = true
                                    hideProgressDialog()
                                    finish()


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
            Utils.showNoInternetBottomSheet(this,this)
        }


    }
    private fun getSelectedRadioItem(radioGroup: RadioGroup): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        if (selectedItemId != -1) {
            val radioButton = findViewById<View>(selectedItemId) as RadioButton
//            makeToast(radioButton.text.toString(),0)
            return radioButton.text.toString().trim()
        }
        return ""
    }
}