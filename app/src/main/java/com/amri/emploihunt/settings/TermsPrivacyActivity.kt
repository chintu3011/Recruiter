package com.amri.emploihunt.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityTermsPrivacyBinding
import com.amri.emploihunt.model.PrivacyPolicyModel
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener

class TermsPrivacyActivity : BaseActivity() {
    lateinit var binding: ActivityTermsPrivacyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.getBooleanExtra("Privacy",false)){
            binding.tvTitle.text = resources.getString(R.string.privacy_policy)
            callPolicyAPI(1)

        }else{
            binding.tvTitle.text = resources.getString(R.string.terms_amp_amp_condition)
            callPolicyAPI(2)
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
    private fun callPolicyAPI(id: Int) {

        if (Utils.isNetworkAvailable(this)){
            AndroidNetworking.get(NetworkUtils.TERMS_PRIVACY)
                .setOkHttpClient(NetworkUtils.okHttpClient)
                .addQueryParameter("id",id.toString())
                .setPriority(Priority.MEDIUM).build().getAsObject(
                    PrivacyPolicyModel::class.java,
                    object : ParsedRequestListener<PrivacyPolicyModel> {
                        override fun onResponse(response: PrivacyPolicyModel?) {
                            response?.let {
                                Log.e("#####", "onResponse: $it")
                                binding.webView.loadData(response!!.data.content, "text/html","UTF-8")
                                binding.webView.visibility = View.VISIBLE
                                binding.progressCircular.visibility = View.GONE
                            }

                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e("#####", "onError: code: ${it.errorCode} & body: ${it.errorDetail}")
                            }

                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(this,this)
        }

    }
}