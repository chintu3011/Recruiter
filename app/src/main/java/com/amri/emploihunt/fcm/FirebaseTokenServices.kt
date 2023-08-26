package com.amri.emploihunt.fcm

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.amri.emploihunt.jobSeekerSide.HomeJobSeekerActivity
import com.amri.emploihunt.R
import com.amri.emploihunt.util.FCM_TOKEN
import com.amri.emploihunt.util.PrefManager
import com.amri.emploihunt.util.PrefManager.get
import com.amri.emploihunt.util.PrefManager.set
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseTokenServices : FirebaseMessagingService() {
    private var TAG = FirebaseTokenServices::class.java.simpleName
    lateinit var prefManager: SharedPreferences



    override fun onNewToken(token: String) {
        super.onNewToken(token)
        prefManager = PrefManager.prefManager(this)
        prefManager.set(FCM_TOKEN,token)
        Log.e(TAG, "FirebaseMessagingService FCM_TOKEN: ${prefManager.get(FCM_TOKEN,"")}")
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.notification != null) {
            prefManager = PrefManager.prefManager(this)
            val title: String = message.notification!!.title.toString()
            val msg: String = message.notification!!.body.toString()

            val objData: MutableMap<String, String> = message.data
            val notiType: String = objData["notification_type"].toString()

            showNotification(title, msg, notiType)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun showNotification(
        title: String?, message: String?, notiType: String?) {
        // val notificationChannel = NotificationChannel(channelId, "name", NotificationManager.IMPORTANCE_LOW)
        val channelId = "Recruitment App"
        createNotificationChannel(channelId)



        val intent = when (notiType) {
            "1" -> {
                Intent(applicationContext, HomeJobSeekerActivity::class.java).apply {

                }
            }

            else -> {
                Intent(applicationContext, HomeJobSeekerActivity::class.java)
            }
        }

        /*val intent: Intent = if (notiType == "1") {
            Intent(applicationContext, CategoryListActivity::class.java).apply {
                putExtra("CATEGORY_TYPE", categoryType)
                putExtra("MAIN_CAT_ID", catTempId?.toIntOrNull())
            }
        } else {
            Intent(applicationContext, TemplateListActivity::class.java).apply {
                putExtra("SUB_CATEGORY_ID", catTempId?.toIntOrNull())
                putExtra("CATEGORY_NAME", categoryName)
            }
        }*/
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }
        val builder =
            NotificationCompat.Builder(applicationContext, channelId).setContentText(title)
                .setContentTitle(message).addAction(R.drawable.logo, "Title", pendingIntent)
                //.setChannelId(channelId)
                .setSmallIcon(R.drawable.logo).setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

        //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //notificationManager.createNotificationChannel(notificationChannel)
        //notificationManager.notify(1, notification)
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    this@FirebaseTokenServices,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }



    private fun createNotificationChannel(channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            //val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            //.apply {description = descriptionText}
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}