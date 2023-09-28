package com.amri.emploihunt.messenger

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.amri.emploihunt.R
import com.amri.emploihunt.basedata.BaseActivity
import com.amri.emploihunt.databinding.ActivityFullImageViewBinding
import com.amri.emploihunt.databinding.FullScreenImageViewBinding
import com.amri.emploihunt.databinding.PdfViewerDialogBinding
import com.amri.emploihunt.networking.NetworkUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class FullImageViewActivity : BaseActivity() {
    lateinit var binding: ActivityFullImageViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setBackgroundDrawableResource(R.color.transparent)
        Glide.with(this)
            .load(NetworkUtils.BASE_URL_MEDIA+intent.getStringExtra("Uri"))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    //TODO: something on exception
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressCircular.visibility = View.GONE
                    //do something when picture already loaded
                    return false
                }
            })
            .into(binding.ivImageView)
        binding.closeIv.setOnClickListener {
            finish()
        }
    }
}