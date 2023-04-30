package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.DetailActivity.Companion.bundleExtrasOf

fun Context.getDownloadManager(): DownloadManager = ContextCompat.getSystemService(
    this,
    DownloadManager::class.java
) as DownloadManager

enum class DownloadStatus(val type: String) {
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    UNKNOWN("Unknown"),
}

@SuppressLint("Range")
fun DownloadManager.queryStatus(id: Long): DownloadStatus {
    query(DownloadManager.Query().setFilterById(id)).use {
        with(it) {
            if (this != null && moveToFirst()) {
                return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.SUCCESSFUL
                    DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                    else -> DownloadStatus.UNKNOWN
                }
            }
            return DownloadStatus.UNKNOWN
        }
    }
}

fun Context.getNotificationManager(): NotificationManager = ContextCompat.getSystemService(
    this,
    NotificationManager::class.java
) as NotificationManager

const val notification_channel_id = "loading_status_channel"
const val notification_channel_name = "Download status"
const val notification_channel_description = "loading_status"
const val NOTIFICATION_REQUEST_CODE = 1

fun createDownloadStatusChannel(context: Context) {
    ifSupportsOreo {
        val notificationChannel = NotificationChannel(
            notification_channel_id,
            notification_channel_name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                description = notification_channel_description
                setShowBadge(true)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

fun ifSupportsOreo(doSomething: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        doSomething()
    }
}

fun NotificationManager.sendDownloadCompletedNotification(
    fileName: String,
    downloadStatus: DownloadStatus,
    context: Context
) {
    val contentIntent = Intent(context, DetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(bundleExtrasOf(fileName, downloadStatus))
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        NOTIFICATION_REQUEST_CODE,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val checkStatusAction = NotificationCompat.Action.Builder(
        null,
        context.getString(R.string.notification_action_check_status),
        contentPendingIntent
    ).build()

    NotificationCompat.Builder(context, notification_channel_id) // Set the notification content
        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .addAction(checkStatusAction)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .run {
            notify(NOTIFICATION_REQUEST_CODE, this.build())
        }
}

fun ImageView.changeDownloadStatusImageTo(@DrawableRes imageRes: Int) = setImageResource(imageRes)

fun ImageView.changeDownloadStatusColorTo(@ColorRes colorRes: Int) {
    ContextCompat.getColor(context, colorRes)
        .also { color ->
            imageTintList = ColorStateList.valueOf(color)
        }
}

fun TextView.changeDownloadStatusColorTo(@ColorRes colorRes: Int) {
    ContextCompat.getColor(context, colorRes)
        .also { color ->
            setTextColor(color)
        }
}
