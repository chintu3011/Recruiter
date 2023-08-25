package com.amri.emploihunt.basedata

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

import com.amri.emploihunt.databinding.LayoutCommonDialogBinding
import com.amri.emploihunt.databinding.LayoutProgressbarBinding

open class BaseActivity : AppCompatActivity() {

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    fun showProgressDialog(msg: String) {
        try {
            val builder = AlertDialog.Builder(this)
            val binding = LayoutProgressbarBinding.inflate(layoutInflater)
            binding.tvMsg.text = msg
            builder.setView(binding.root)
            dialog = builder.create()
            dialog?.let {
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.setCancelable(false)
                if (!isFinishing && !it.isShowing) it.show()
            }
        } catch (e: Exception) {
            Log.e("#####", "showProgressDialog exception: ${e.message}")
        }
    }

    fun hideProgressDialog() {
        try {
            dialog?.let {
                if (!isFinishing && it.isShowing) it.dismiss()
            }
        } catch (e: Exception) {
            Log.e("#####", "hideProgressDialog exception: ${e.message}")
        }
    }

    fun animRecyclerView(view: View, rv: RecyclerView) {
        val left = (view.left - (rv.width / 2)) + (view.width / 2)
        if (left != 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                rv.smoothScrollBy(left, 0, DecelerateInterpolator(2.0f), 700)
            }, 100)
        }
    }

    fun animSlideUp(view: View) {
        if (view.visibility == View.GONE || view.visibility == View.INVISIBLE) {
            view.visibility = View.VISIBLE
            val animate = TranslateAnimation(0f, 0f, view.height.toFloat(), 0f)
            animate.duration = 500
            animate.fillAfter = false
            view.startAnimation(animate)
        }
    }

    fun animSlideDown(view: View) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
            val animate = TranslateAnimation(0f, 0f, 0f, view.height.toFloat())
            animate.duration = 500
            animate.fillAfter = false
            view.startAnimation(animate)
        }
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

    fun showCommonDialog(
        isHideImg: Boolean, title: String, positiveText: String,
        negativeText: String, positiveClick: () -> Unit, negativeClick: (dialog: Dialog) -> Unit,
    ) {
        val builder = AlertDialog.Builder(this)
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

    fun makeToast(msg: String, len: Int) {
        if (len == 0) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (len == 1) Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}