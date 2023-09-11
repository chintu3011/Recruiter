package com.amri.emploihunt.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.amri.emploihunt.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.*
import java.net.URL
import java.util.*
import java.util.regex.Pattern


object Utils {
    const val FOLDER_NAME = "RecruitmentApp"

    /* Extension Fun */
    val Context.screenWidth: Int get() = resources.displayMetrics.widthPixels
    val Context.screenHeight: Int get() = resources.displayMetrics.heightPixels
    fun CharSequence?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun Context.toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun Fragment.toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    /*fun setOrientation(activity: Activity) {
        activity.requestedOrientation =
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }*/

    inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getSerializable(key) as? T
    }

    fun makeAppDirectory() {
        // val fileApp = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME)
        val fileApp = File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_PICTURES + "/" + FOLDER_NAME
        )
        if (!fileApp.exists()) fileApp.mkdirs()
    }

    fun getAppDirectory(preName: String, extension: String, isFixName: Boolean = false): File {
        // val fileApp = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), FOLDER_NAME)
        val fileApp = File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_PICTURES + "/" + FOLDER_NAME
        )
        if (!fileApp.exists()) fileApp.mkdirs()
        if (isFixName) {
            return File(fileApp, preName + extension)
        }
        return File(fileApp, preName + Calendar.getInstance().timeInMillis + extension)
        //return File(fileApp.absolutePath + preName + Calendar.getInstance().timeInMillis + extension)
    }

    fun getShareDirectory(preName: String, extension: String): File {
        val fileApp = File(
            Environment.getExternalStorageDirectory(),
            Environment.DIRECTORY_PICTURES + "/" + FOLDER_NAME + "/.share"
        )
        if (!fileApp.exists()) fileApp.mkdirs()
        return File(fileApp, preName + Calendar.getInstance().timeInMillis + extension)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val activeNetwork =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    // for other device how are able to connect with Ethernet
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    fun rateApp(activity: Activity) {
        try {
            val appLink = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")
            )
            activity.startActivity(appLink)
        } catch (e: Exception) {
            Log.e("#####", "rateApp Exception: ${e.message}")
        }
    }

    fun openSocialAcc(activity: Activity, url: String) {
        try {
            val appLink = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(appLink)
        } catch (e: Exception) {
            Log.e("#####", "openSocialAcc Exception: ${e.message}")
        }
    }

    fun openWhatsAppFromPost(activity: Activity, texts: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, texts)
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e("#####", "openWhatsAppFromPost Exception: ${e.message}")
        }
    }

    fun shareLink(activity: Activity, texts: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            //intent.putExtra(Intent.EXTRA_SUBJECT, "Please like Facebook & Instagram post")
            intent.putExtra(Intent.EXTRA_TEXT, texts)
            activity.startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            Log.e("#####", "rateApp Exception: ${e.message}")
        }
    }

    fun hideKeyboard(activity: Activity) {
        try {
            /*val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view: View = activity.currentFocus!!
            imm.hideSoftInputFromWindow(view.windowToken, 0)*/

            if (activity.currentFocus != null) {
                (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            }
        } catch (e: Exception) {
            Log.e("#####", "hideKeyboard Exception: ${e.message}")
        }
    }

    fun showKeyboard(activity: Activity, editText: EditText) {
        try {
            editText.requestFocus()
            val imm: InputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            Log.e("#####", "showKeyboard Exception: ${e.message}")
        }
    }

    fun isGPSEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun getGlideListener(progressBar: ProgressBar): RequestListener<Drawable> {
        return (object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?, model: Any?,
                target: Target<Drawable>?, isFirstResource: Boolean,
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?, target: Target<Drawable>?,
                dataSource: DataSource?, isFirstResource: Boolean,
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }
        })
    }

    fun Context.scanFile(path: String) {
        MediaScannerConnection.scanFile(this, arrayOf(path), null, null)
    }

    fun getGIFDuration(progress: Int?): Long {
        return when (progress) {
            0 -> 200L
            1 -> 300L
            2 -> 500L
            3 -> 700L
            4 -> 900L
            5 -> 1000L
            6 -> 1100L
            7 -> 1200L
            8 -> 1300L
            9 -> 1400L
            10 -> 1500L
            11 -> 1600L
            12 -> 1700L
            13 -> 1800L
            14 -> 2000L
            else -> 0L
        }
    }


    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    fun getTimeAgo(mActivity: Context, mTime: Long): String? {
        var time = mTime
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }

        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return null
        }

        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            mActivity.resources.getString(R.string.just_now)
        } else if (diff < 2 * MINUTE_MILLIS) {
            mActivity.resources.getString(R.string.a_minute_ago)
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + mActivity.resources.getString(R.string.minutes_ago)
        } else if (diff < 90 * MINUTE_MILLIS) {
            mActivity.resources.getString(R.string.an_hour_ago)
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + mActivity.resources.getString(R.string.hours_ago)
        } else if (diff < 48 * HOUR_MILLIS) {
            mActivity.resources.getString(R.string.yesterday)
        } else {
            Log.d("##", "getTimeAgo: else $time")

            (diff / DAY_MILLIS).toString() + mActivity.resources.getString(R.string.days_ago)
        }
    }
    fun getTimeReaming(mActivity: Context, mTime: Long): String? {
        var time = mTime
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }

        val now = System.currentTimeMillis()
        if (time < now || time <= 0) {
            return null
        }

        val differenceSec =(time - now)
        val days =  differenceSec / (60 * 60 * 24)
        val remainder1 = differenceSec % 86400
        val hour =  remainder1 / (60 * 60)
        val remainder = differenceSec % 3600
        val minutes =  remainder / 60
        if (days.toInt() !=0){
             return  mActivity.resources.getString(R.string.expire1,days,hour,minutes)
        }else if (hour.toInt() != 0){
             return mActivity.resources.getString(R.string.expire2,hour,minutes)
        }else if (minutes.toInt() != 0) {

             return  mActivity.resources.getString(R.string.expire3, minutes)
        }
        return null
    }


    suspend fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap {
        val finalBitmap: Deferred<Bitmap> = GlobalScope.async(Dispatchers.IO) {
            val bitmap =
                if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri!!)
                    )
                }
            bitmap
            /*val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)*/
        }
        /*GlobalScope.launch(Dispatchers.Main) {
            return finalBitmap.await()
        }*/
        return finalBitmap.await()
    }

    suspend fun getBitmapFromOnlineUri(onlineUri: String?): Bitmap {
        val finalBitmap: Deferred<Bitmap> = GlobalScope.async(Dispatchers.IO) {
            val url = URL(onlineUri)
            val bitmap = BitmapFactory.decodeStream(url.openStream())

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 85, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
        return finalBitmap.await()
    }

    fun trimBitmap(source: Bitmap): Bitmap? {
        var firstX = 0
        var firstY = 0
        var lastX = source.width
        var lastY = source.height
        val pixels = IntArray(source.width * source.height)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        loop@ for (x in 0 until source.width) {
            for (y in 0 until source.height) {
                if (pixels[x + y * source.width] != Color.TRANSPARENT) {
                    firstX = x
                    break@loop
                }
            }
        }
        loop@ for (y in 0 until source.height) {
            for (x in firstX until source.width) {
                if (pixels[x + y * source.width] != Color.TRANSPARENT) {
                    firstY = y
                    break@loop
                }
            }
        }
        loop@ for (x in source.width - 1 downTo firstX) {
            for (y in source.height - 1 downTo firstY) {
                if (pixels[x + y * source.width] != Color.TRANSPARENT) {
                    lastX = x
                    break@loop
                }
            }
        }
        loop@ for (y in source.height - 1 downTo firstY) {
            for (x in source.width - 1 downTo firstX) {
                if (pixels[x + y * source.width] != Color.TRANSPARENT) {
                    lastY = y
                    break@loop
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY)
    }

    fun dpToPx(dp: Int): Int {
        return (dp.toFloat() * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun Float.toPixel(mContext: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this, mContext.resources.displayMetrics
        ).toInt()
    }

    /*suspend fun getBitmapFromAsset(context: Context, path: String): Bitmap {
        val finalBitmap: Deferred<Bitmap> = GlobalScope.async(Dispatchers.IO) {
            val inputStream = context.assets.open(path)
            BitmapFactory.decodeStream(inputStream)
        }
        return finalBitmap.await()
    }

    suspend fun getBitmapFromView(view: View): Bitmap {
        val finalBitmap: Deferred<Bitmap> = GlobalScope.async(Dispatchers.IO) {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            view.drawingCache
        }
        return finalBitmap.await()
    }*/

    // compress
    fun getRealPathFromDocumentUri(context: Context, uri: Uri): String {
        val matcher = Pattern.compile("(\\d+)$").matcher(uri.toString())
        var str = ""
        if (!matcher.find()) {
            return str
        }
        val strArr = arrayOf("_data")
        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            strArr,
            "_id=?",
            arrayOf(matcher.group()),
            null
        )
        val columnIndex = query!!.getColumnIndex(strArr[0])
        if (query.moveToFirst()) {
            str = query.getString(columnIndex)
        }
        query.close()
        return str
    }

    private fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getCompressedBitmap(str: String?): Bitmap? {
        var bitmap: Bitmap?
        var screenHeight = getScreenHeight().toFloat()
        val screenWidth = getScreenWidth().toFloat()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var decodeFile = BitmapFactory.decodeFile(str, options)
        var i = options.outHeight
        var i2 = options.outWidth
        val f = i2.toFloat()
        val f2 = i.toFloat()
        val f3 = f / f2
        val f4 = screenWidth / screenHeight
        if (f2 > screenHeight || f > screenWidth) {
            if (f3 < f4) {
                i2 = (screenHeight / f2 * f).toInt()
                i = screenHeight.toInt()
            } else {
                if (f3 > f4) {
                    screenHeight = screenWidth / f * f2
                }
                i = screenHeight.toInt()
                i2 = screenWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, i2, i)
        options.inJustDecodeBounds = false
        options.inTempStorage = ByteArray(16384)
        try {
            decodeFile = BitmapFactory.decodeFile(str, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        bitmap = try {
            Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888)
        } catch (e2: OutOfMemoryError) {
            e2.printStackTrace()
            null
        }
        val f5 = i2.toFloat()
        val f6 = f5 / options.outWidth.toFloat()
        val f7 = i.toFloat()
        val f8 = f7 / options.outHeight.toFloat()
        val f9 = f5 / 2.0f
        val f10 = f7 / 2.0f
        val matrix = Matrix()
        matrix.setScale(f6, f8, f9, f10)
        val canvas = Canvas(bitmap!!)
        canvas.setMatrix(matrix)
        canvas.drawBitmap(
            decodeFile, f9 - decodeFile.width.toFloat() / 2.0f, f10 - decodeFile.height
                .toFloat() / 2.0f, Paint(2)
        )
        try {
            val attributeInt =
                android.media.ExifInterface(str!!).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix2 = Matrix()
            if (attributeInt == 6) {
                matrix2.postRotate(90.0f)
            } else if (attributeInt == 3) {
                matrix2.postRotate(180.0f)
            } else if (attributeInt == 8) {
                matrix2.postRotate(270.0f)
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix2, true)
        } catch (e3: IOException) {
            e3.printStackTrace()
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, i: Int, i2: Int): Int {
        var i3: Int
        val i4 = options.outHeight
        val i5 = options.outWidth
        if (i4 > i2 || i5 > i) {
            i3 = Math.round(i4.toFloat() / i2.toFloat())
            val round = Math.round(i5.toFloat() / i.toFloat())
            if (i3 >= round) {
                i3 = round
            }
        } else {
            i3 = 1
        }
        while ((i5 * i4).toFloat() / (i3 * i3).toFloat() > (i * i2 * 2).toFloat()) {
            i3++
        }
        return i3
    }

    // new method
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        when {
            // DocumentProvider
            DocumentsContract.isDocumentUri(context, uri) -> {
                when {
                    // ExternalStorageProvider
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        // This is for checking Main Memory
                        return if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + split[1]
                            } else {
                                Environment.getExternalStorageDirectory().toString() + "/"
                            }
                            // This is for checking SD Card
                        } else {
                            "storage" + "/" + docId.replace(":", "/")
                        }
                    }

                    isDownloadsDocument(uri) -> {
                        val fileName = getFilePath(context, uri)
                        if (fileName != null) {
                            return Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                        }
                        var id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            id = id.replaceFirst("raw:".toRegex(), "")
                            val file = File(id)
                            if (file.exists()) return id
                        }
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        return getDataColumn(context, contentUri, null, null)
                    }

                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        Log.d("####", "getRealPathFromURI: $type")
                        when (type) {
                            "image" -> {
                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }

                            "video" -> {
                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }

                            "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                            "document" -> {
                                contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                                }else{
                                    MediaStore.Files.getContentUri("external")
                                }
                            }
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(split[1])
                        Log.d("###", "getRealPathFromURI: ${getDataColumn(context, contentUri, selection, selectionArgs)} ")
                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }

            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context, uri, null, null
                )
            }

            "file".equals(uri.scheme, ignoreCase = true) -> {
                return uri.path
            }
        }
        return null
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /** For Upload Status */
    private const val EOF = -1
    private const val DEFAULT_BUFFER_SIZE = 1024 * 4
    fun from(context: Context, uri: Uri): File {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
        val fileName = getFileName(context, uri)
        val splitName = splitFileName(fileName)
        var tempFile: File = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        val out: FileOutputStream?
        try {
            out = FileOutputStream(tempFile)
            copy(inputStream, out)
            inputStream.close()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tempFile
    }

    private fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i) + "00"
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
            try {
                if (cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut: Int = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun rename(file: File, newName: String): File {
        val newFile = File(file.parent, newName)
        if (newFile != file) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old $newName file")
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to $newName")
            }
        }
        return newFile
    }

    private fun copy(input: InputStream, output: OutputStream): Long {
        var count: Long = 0
        var n: Int
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (EOF != input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    @SuppressLint("Range")
    @Throws(IOException::class)
    fun convertUriToPdfFile(context: Context, uri: Uri?): File? {
        val contentResolver = context.contentResolver
        var displayName: String? = null
        contentResolver.query(uri!!, null, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val outputFile = File(outputDir, displayName)
        contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                val buffer =
                    ByteArray(4 * 1024) // Adjust buffer size as needed
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }
        return outputFile
    }

    /*    public static void showOkDialog(Context context, String title, String message) {
        AlertDialog adb = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom)).setTitle(message).setCancelable(false)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(context.getText(R.string.ok), (dialog, which) -> dialog.dismiss()).create();
        adb.show();

        if (!((BaseActivity) context).isFinishing() && !adb.isShowing()) {

            Log.d("#Test", "isInternetConnected:4 ");
        }
    }*/
    fun showNoInternetBottomSheet(context: Context, activity: Activity?) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView: View = LayoutInflater.from(context)
            .inflate(
                R.layout.internet_bottom_sheet_layout,
                null
            )
        val tv_des = bottomSheetView.findViewById<TextView>(R.id.tv_des)
        val ok_btn = bottomSheetView.findViewById<Button>(R.id.btn_ok)
        val animationView = bottomSheetView.findViewById<LottieAnimationView>(R.id.animationView)
        tv_des.text = context.getText(R.string.default_internet_message)
        animationView.setAnimation(R.raw.no_internet)
        ok_btn.setOnClickListener {
            bottomSheetDialog.dismiss()
            ActivityCompat.finishAffinity(activity!!)
        }
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }
}