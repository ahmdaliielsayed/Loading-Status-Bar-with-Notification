package com.udacity

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class DownloadNotificationUtils(private val context: Context, private val lifecycle: Lifecycle) :
    LifecycleObserver {

    init {
        lifecycle.addObserver(this).also {
            Log.d("Tag_Download", "Notificator added as a Lifecycle Observer")
        }
    }

    fun notify(fileName: String, downloadStatus: DownloadStatus) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Log.d("Tag_Download", "Notifying with a Toast. Lifecycle is resumed")
            when (downloadStatus) {
                DownloadStatus.SUCCESSFUL -> Toast.makeText(context, context.getString(R.string.download_completed), Toast.LENGTH_LONG).show()
                else -> Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_LONG).show()
            }
        }
        with(context.applicationContext) {
            getNotificationManager().run {
                createDownloadStatusChannel(applicationContext)
                sendDownloadCompletedNotification(
                    fileName,
                    downloadStatus,
                    applicationContext
                )
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregisterObserver() = lifecycle.removeObserver(this)
        .also { Log.d("Tag_Download", "Notificator removed from Lifecycle Observers") }
}
