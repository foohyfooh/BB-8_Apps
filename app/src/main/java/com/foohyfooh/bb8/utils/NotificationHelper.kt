package com.foohyfooh.bb8.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build

import com.foohyfooh.bb8.R

class NotificationHelper(context: Context) : ContextWrapper(context) {

    companion object {
        const val CHANNEL_DEFAULT = "default"
    }

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val defaultChannel = NotificationChannel(CHANNEL_DEFAULT,
                getString(R.string.notificationChannel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT)
        defaultChannel.description = getString(R.string.notificationChannel_default_description)

        notificationManager.createNotificationChannel(defaultChannel)
    }

    fun makeNotification(id: String, text: String): Notification {
        val builder = Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(id)
        return builder
                .build()
    }

    fun postNotification(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

}
