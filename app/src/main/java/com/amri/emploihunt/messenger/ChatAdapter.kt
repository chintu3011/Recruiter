package com.amri.emploihunt.messenger

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.amri.emploihunt.R
import com.amri.emploihunt.databinding.PdfViewerDialogBinding
import com.amri.emploihunt.model.MessageData
import com.amri.emploihunt.util.IMG_TYPE
import com.amri.emploihunt.util.PDF_TYPE
import com.amri.emploihunt.util.TXT_TYPE
import com.amri.emploihunt.util.Utils
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.imageview.ShapeableImageView
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ChatAdapter(
    context: Context,
    private val messages: List<MessageData>,
    private val userId: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val TAG = "ChatAdapter"
        
        private const val VIEW_TYPE_TXT_FROM = 1
        private const val VIEW_TYPE_TXT_TO = 2
        private const val VIEW_TYPE_PDF_FROM = 3
        private const val VIEW_TYPE_PDF_TO = 4
        private const val VIEW_TYPE_IMG_FROM = 5
        private const val VIEW_TYPE_IMG_TO = 6
    }
    private var dialog: Dialog? = null


    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return if (message.fromId == userId) {

            when(message.msgType){
                TXT_TYPE -> {
                    VIEW_TYPE_TXT_FROM
                }
                PDF_TYPE -> {
                    VIEW_TYPE_PDF_FROM
                }
                IMG_TYPE -> {
                    VIEW_TYPE_IMG_FROM
                }
                else -> {
                    Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                    -1
                }
            }
            
        } else {
            when(message.msgType) {
                TXT_TYPE -> {
                    VIEW_TYPE_TXT_TO
                }
                PDF_TYPE -> {
                    VIEW_TYPE_PDF_TO
                }
                IMG_TYPE -> {
                    VIEW_TYPE_IMG_TO
                }
                else -> {
                    Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                    -1
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TXT_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_txt_row, parent, false)
                FromTxtViewHolder(view)
            }
            VIEW_TYPE_TXT_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_txt_row, parent, false)
                ToTxtViewHolder(view)
            }
            VIEW_TYPE_PDF_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_docs_row, parent, false)
                FromPdfViewHolder(view)
            }
            VIEW_TYPE_PDF_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_docs_row, parent, false)
                ToPdfViewHolder(view)
            }
            VIEW_TYPE_IMG_FROM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_img_row, parent, false)
                FromImgViewHolder(view)
            }
            VIEW_TYPE_IMG_TO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_to_img_row, parent, false)
                ToImgViewHolder(view)
            }
            else -> {
                Log.e(TAG, "onCreateViewHolder: ${R.string.incorrect_view_holder}")
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_from_txt_row, parent, false)
                FromTxtViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
         holder.setIsRecyclable(false)
        when (holder) {
            is FromTxtViewHolder -> {
                holder.bind(message)
            }
            is ToTxtViewHolder -> {
                holder.bind(message)
            }
            is FromPdfViewHolder -> {
                holder.bind(message)
                holder.itemView.setOnClickListener {
                    showDialog(holder.itemView.context,true,message.docUri)
                }
            }
            is ToPdfViewHolder -> {
                holder.bind(message)
                holder.itemView.setOnClickListener {
                    showDialog(holder.itemView.context, true, message.docUri)
                }
            }
            is FromImgViewHolder -> {
                holder.bind(message)
            }
            is ToImgViewHolder -> {
                holder.bind(message)
            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class FromTxtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var msg: TextView = itemView.findViewById(R.id.msg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            msg.text = messageData.message
            timeStamp.text = messageData.timeStamp
        }
    }
    inner class ToTxtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var msg: TextView = itemView.findViewById(R.id.msg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            msg.text = messageData.message
            timeStamp.text = messageData.timeStamp
        }
    }
    inner class FromPdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var pdfName: TextView = itemView.findViewById(R.id.pdfName)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            pdfName.text = messageData.message
            timeStamp.text = messageData.timeStamp

            val pdfUri = messageData.docUri
        }
    }
    inner class ToPdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private var pdfName: TextView = itemView.findViewById(R.id.pdfName)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)

        fun bind(messageData: MessageData){
            pdfName.text = messageData.message
            timeStamp.text = messageData.timeStamp

            val pdfUri = messageData.docUri
        }

    }
    inner class FromImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val btnShowImg:ShapeableImageView = itemView.findViewById(R.id.btnShowImg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
        fun bind(messageData: MessageData){

            // Decode the image file to get its dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(messageData.docUri, options)
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

// Define min and max dimensions for the ImageView
            val minWidth = 600 // Change this to your desired minimum width
            val minHeight = 700 // Change this to your desired minimum height
            val maxWidth = 1500 // Change this to your desired maximum width
            val maxHeight = 1500 // Change this to your desired maximum height

// Calculate scaling factors to fit within the specified range
            val widthScaleFactor = minOf(1.0, maxWidth.toDouble() / imageWidth)
            val heightScaleFactor = minOf(1.0, maxHeight.toDouble() / imageHeight)

// Use the smaller scaling factor to ensure the image fits within both dimensions
            val scaleFactor = minOf(widthScaleFactor, heightScaleFactor)

// Calculate the ImageView dimensions
            val imageViewWidth = (imageWidth * scaleFactor).toInt()
            val imageViewHeight = (imageHeight * scaleFactor).toInt()

// Ensure the dimensions are within the specified min and max dimensions
            val finalWidth = maxOf(minWidth, minOf(maxWidth, imageViewWidth))
            val finalHeight = maxOf(minHeight, minOf(maxHeight, imageViewHeight))

// Set the ImageView dimensions
            btnShowImg.layoutParams.width = finalWidth
            btnShowImg.layoutParams.height = finalHeight

            Glide.with(btnShowImg.context).load(messageData.docUri?.toUri()).placeholder(R.drawable.baseline_image_24).into(btnShowImg)
            timeStamp.text = messageData.timeStamp

            val imgUri = messageData.docUri
        }

    }
    inner class ToImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val btnShowImg:ShapeableImageView = itemView.findViewById(R.id.btnShowImg)
        private var timeStamp: TextView = itemView.findViewById(R.id.timeStamp)
        fun bind(messageData: MessageData){

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(messageData.docUri, options)
            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

// Define min and max dimensions for the ImageView
            val minWidth = 600 // Change this to your desired minimum width
            val minHeight = 700 // Change this to your desired minimum height
            val maxWidth = 1500 // Change this to your desired maximum width
            val maxHeight = 1500 // Change this to your desired maximum heigh

// Calculate scaling factors to fit within the specified range
            val widthScaleFactor = minOf(1.0, maxWidth.toDouble() / imageWidth)
            val heightScaleFactor = minOf(1.0, maxHeight.toDouble() / imageHeight)

// Use the smaller scaling factor to ensure the image fits within both dimensions
            val scaleFactor = minOf(widthScaleFactor, heightScaleFactor)

// Calculate the ImageView dimensions
            val imageViewWidth = (imageWidth * scaleFactor).toInt()
            val imageViewHeight = (imageHeight * scaleFactor).toInt()

// Ensure the dimensions are within the specified min and max dimensions
            val finalWidth = maxOf(minWidth, minOf(maxWidth, imageViewWidth))
            val finalHeight = maxOf(minHeight, minOf(maxHeight, imageViewHeight))

// Set the ImageView dimensions
            btnShowImg.layoutParams.width = finalWidth
            btnShowImg.layoutParams.height = finalHeight

            Glide.with(btnShowImg.context).load(messageData.docUri?.toUri()).placeholder(R.drawable.baseline_image_24).into(btnShowImg)
            timeStamp.text = messageData.timeStamp

            val imgUri = messageData.docUri
        }

    }
    private fun showDialog(context: Context, isOnline: Boolean, docUri: String?) {
        try {


            val builder = AlertDialog.Builder(context)
            val bindingDialog = PdfViewerDialogBinding.inflate(LayoutInflater.from(context))

            builder.setView(bindingDialog.root)
            if (isOnline){
                RetrievePDFFromURL(bindingDialog.idPDFView,bindingDialog.progressCircular).execute(docUri)

            }else{
                context.contentResolver.takePersistableUriPermission(
                    Uri.parse(docUri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val resumePdf = Utils.convertUriToPdfFile(context, Uri.parse(docUri))!!
                bindingDialog.idPDFView.fromFile(resumePdf).load()
            }

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
    class RetrievePDFFromURL(pdfView: PDFView, processBar: ProgressBar) :
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