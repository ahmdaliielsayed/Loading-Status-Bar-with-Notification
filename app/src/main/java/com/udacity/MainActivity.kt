package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var downloadContentObserver: ContentObserver? = null
    private var downloadNotificator: DownloadNotificationUtils? = null

    private var downloadFileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            when (download_option_radio_group.checkedRadioButtonId) {
                View.NO_ID -> Toast.makeText(this@MainActivity, getString(R.string.select_option), Toast.LENGTH_SHORT).show()
                else -> {
                    downloadFileName = findViewById<RadioButton>(download_option_radio_group.checkedRadioButtonId).text.toString()
                    selectURL(downloadFileName)
                    download()
                }
            }
        }
    }

    private fun selectURL(downloadFileName: String) = when (downloadFileName) {
        getString(R.string.download_using_glide) -> {
            URL = "https://github.com/bumptech/glide/archive/master.zip"
        }
        getString(R.string.download_using_load_app) -> {
            URL = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        }
        else -> {
            URL = "https://github.com/square/retrofit/archive/master.zip"
        }
    }

    private fun DownloadManager.downloadContentObserver() {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                downloadContentObserver?.run { queryProgress() }
            }
        }.also {
            downloadContentObserver = it
            contentResolver.registerContentObserver(
                "content://downloads/my_downloads".toUri(),
                true,
                downloadContentObserver!!
            )
        }
    }

    @SuppressLint("Range")
    private fun DownloadManager.queryProgress() {
        query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    // check download status
                    val id = getInt(getColumnIndex(DownloadManager.COLUMN_ID))
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            Log.i("Test_Download", "Download $id: failed")
                            custom_button.changeButtonState(ButtonState.Completed)
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            Log.i("Test_Download", "Download $id: paused")
                        }
                        DownloadManager.STATUS_PENDING -> {
                            Log.i("Test_Download", "Download $id: pending")
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            Log.i("Test_Download", "Download $id: running")
                            custom_button.changeButtonState(ButtonState.Loading)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.i("Test_Download", "Download $id: successful")
                            custom_button.changeButtonState(ButtonState.Completed)
                        }
                    }
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val downloadStatus = getDownloadManager().queryStatus(it)
                Log.i("Test_Download", "Download $it completed with status: ${downloadStatus.type}")
                unregisterDownloadContentObserver()
                downloadStatus.takeIf { status -> status != DownloadStatus.UNKNOWN }?.run {
                    getDownloadNotificator().notify(downloadFileName, downloadStatus)
                }
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.

        downloadManager.downloadContentObserver()
    }

    companion object {
        private var URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

    private fun unregisterDownloadContentObserver() {
        downloadContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadContentObserver = null
        }
    }

    private fun getDownloadNotificator(): DownloadNotificationUtils = when (downloadNotificator) {
        null -> DownloadNotificationUtils(this, lifecycle).also { downloadNotificator = it }
        else -> downloadNotificator!!
    }

    override fun onStop() {
        super.onStop()
        custom_button.changeButtonState(ButtonState.Completed)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterDownloadContentObserver()
        downloadNotificator?.unregisterObserver()
        downloadNotificator = null
    }
}
