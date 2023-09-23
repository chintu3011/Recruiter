package com.amri.emploihunt.basedata

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.amri.emploihunt.databinding.LayoutCommonDialogBinding
import com.amri.emploihunt.databinding.LayoutProgressbarBinding
import com.amri.emploihunt.model.GetAllCity
import com.amri.emploihunt.networking.NetworkUtils
import com.amri.emploihunt.util.Utils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.ParsedRequestListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


open class BaseFragment : Fragment() {
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun showProgressDialog(msg: String) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val binding = LayoutProgressbarBinding.inflate(layoutInflater)
            binding.tvMsg.text = msg
            builder.setView(binding.root)
            dialog = builder.create()
            dialog?.let {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.setCancelable(false)
                if ((!activity?.isFinishing!!) && (!it.isShowing)) it.show()
            }
        } catch (e: Exception) {
            Log.e("#####", "showProgressDialog exception: ${e.message}")
        }
    }

    fun hideProgressDialog() {
        try {
            dialog?.let {
                if (it.isShowing) it.dismiss()
            }
        } catch (e: Exception) {
            Log.e("#####", "hideProgressDialog exception: ${e.message}")
        }
    }

    fun showCommonDialog(
        isHideImg: Boolean, title: String, positiveText: String,
        negativeText: String, positiveClick: () -> Unit, negativeClick: (dialog: Dialog) -> Unit,
    ) {
        val builder = AlertDialog.Builder(requireActivity())
        val dialogBinding = LayoutCommonDialogBinding.inflate(layoutInflater)
        dialogBinding.apply {
            if (isHideImg) imgDialog.visibility = View.GONE
            tvDialogTitle.text = title
            tvPositive.text = positiveText
            tvNegative.text = negativeText
        }
        builder.setView(dialogBinding.root)
        val dialog = builder.create()
        dialog.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setCancelable(false)
            it.show()
        }
        dialogBinding.layPositive.setOnClickListener {
            dialog.dismiss()
            positiveClick()
        }
        dialogBinding.layNegative.setOnClickListener {
            // dialog.dismiss() // don't dismiss here, sometimes it needs to be open
            negativeClick(dialog)
        }
    }

    fun getSelectedRadioItem(radioGroup: RadioGroup, dialogView: View): String {
        val selectedItemId = radioGroup.checkedRadioButtonId
        val radioButton = dialogView.findViewById<View>(selectedItemId) as RadioButton?
        if (selectedItemId != -1 && radioButton != null) {
            return radioButton.text.toString()
        }
        return ""
    }

    fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show()
    }

    fun animSlideFromEnd(view: View, isFromLeftSide: Boolean = false) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            if (isFromLeftSide) (-view.width.toFloat()) else view.width.toFloat(),
            0f,
            0f,
            0f
        )
        animate.duration = 500
        animate.fillAfter = false
        view.startAnimation(animate)
    }

    fun animSlideFromStart(view: View, isFromLeftSide: Boolean = false) {
        view.visibility = View.GONE
        val animate = TranslateAnimation(
            0f,
            if (isFromLeftSide) (-view.width.toFloat()) else view.width.toFloat(),
            0f,
            0f
        )
        animate.duration = 500
        animate.fillAfter = false
        view.startAnimation(animate)
    }

    private fun isNumeric(input: String): Boolean {
        return try {
            input.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    /*var cityList: ArrayList<String> = ArrayList()*/
    fun getAllCity(cityList: ArrayList<String>,callback:() -> Unit){

        if (Utils.isNetworkAvailable(requireContext())){
            showProgressDialog("Please wait....")
            AndroidNetworking.get(NetworkUtils.GET_CITIES)
                .setPriority(Priority.MEDIUM).build()
                .getAsObject(
                    GetAllCity::class.java,
                    object : ParsedRequestListener<GetAllCity> {
                        override fun onResponse(response: GetAllCity?) {
                            try {
                                hideProgressDialog()
                                if (response != null) {
                                    cityList.addAll(response.data)
                                }
                                callback()

                            } catch (e: Exception) {
                                Log.e("#####", "onResponse Exception: ${e.message}")
                                callback()
                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.let {
                                Log.e(
                                    "#####",
                                    "onError: code: ${it.errorCode} & message: ${it.message}"
                                )
                                callback()
                                hideProgressDialog()

                            }
                        }
                    })
        }else{
            Utils.showNoInternetBottomSheet(requireContext(), requireActivity())
            callback()
        }

    }


    fun requestPermissions(permissions: MutableList<String>,callback: (Boolean) -> Unit) {

        Log.d("####", "requestPermissions: $permissions")
        Dexter.withContext(requireContext()).withPermissions(
            permissions
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                Log.d("####", "onPermissionsChecked: $report")
                if (report?.areAllPermissionsGranted()!!) {
                    Log.d("permissions###", "permission granted")
                    callback(true)

                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    Log.d("permission###", "permission Denied")
                    showSettingsDialog()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()

            }
        }).withErrorListener { error -> Log.e("#####", "onError $error") }.check()
    }

    fun isGrantedPermission(): MutableList<String> {

        Log.d("Version*", Build.VERSION.SDK_INT.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("Version*", Build.VERSION.SDK_INT.toString())
            val requiredPermissions = listOf( Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS)
            val deniedPermissions: MutableList<String> = mutableListOf()
            for(permission in requiredPermissions){
                if(ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_DENIED ){
                    deniedPermissions.add(permission)
                }
            }
            return deniedPermissions
        } else {
            Log.d("Version**", Build.VERSION.SDK_INT.toString())
            val requiredPermissions = listOf( Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            val deniedPermissions: MutableList<String> = mutableListOf()

            for(permission in requiredPermissions){
                if(ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_DENIED ){
                    deniedPermissions.add(permission)
                }
            }
            return deniedPermissions
        }

    }

    fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            makeToast(("Permission in not Granted. You can not User this Feature"),0)
            /*          val snackbar = Snackbar
                          .make(
                              binding.layout,
                              "Sorry! you are not register, Please register first.",
                              Snackbar.LENGTH_LONG
                          )
                          .setAction(
                              "REGISTER"
                          )  // If the Undo button
          // is pressed, show
          // the message using Toast
                          {
                              startActivity(Intent(this, AskActivity::class.java))
                              overridePendingTransition(
                                  R.anim.slide_in_left,
                                  R.anim.slide_out_right
                              )
                          }

                      snackbar.show()*/
            dialog.cancel()
        }
        builder.show()
    }
}