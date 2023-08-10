package com.example.recruiter.basedata

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.recruiter.databinding.LayoutCommonDialogBinding
import com.example.recruiter.databinding.LayoutProgressbarBinding


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
}