package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private var fileName = ""
    private var downloadStatus = ""

    companion object {
        private const val EXTRA_FILE_NAME = "${BuildConfig.APPLICATION_ID}.FILE_NAME"
        private const val EXTRA_DOWNLOAD_STATUS = "${BuildConfig.APPLICATION_ID}.DOWNLOAD_STATUS"

        fun bundleExtrasOf(fileName: String, downloadStatus: DownloadStatus) = bundleOf(
            EXTRA_FILE_NAME to fileName,
            EXTRA_DOWNLOAD_STATUS to downloadStatus.type
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        getIntentData()
        setDataToView()
        onFinishClick()
        onChangeViewForDownloadStatus()
    }

    private fun getIntentData() {
        fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        downloadStatus = intent.getStringExtra(EXTRA_DOWNLOAD_STATUS) ?: ""
    }

    private fun setDataToView() {
        file_name_text.text = fileName
        download_status_text.text = downloadStatus
    }

    private fun onFinishClick() = btnOk.setOnClickListener {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onChangeViewForDownloadStatus() {
        when (downloadStatus) {
            DownloadStatus.SUCCESSFUL.type -> {
                download_status_image.changeDownloadStatusImageTo(R.drawable.ic_check_circle_outline_24)
                download_status_image.changeDownloadStatusColorTo(R.color.TemplateGreen)
                download_status_text.changeDownloadStatusColorTo(R.color.TemplateGreen)
            }
            DownloadStatus.FAILED.type -> {
                download_status_image.changeDownloadStatusImageTo(R.drawable.ic_error_24)
                download_status_image.changeDownloadStatusColorTo(R.color.TemplateRed)
                download_status_text.changeDownloadStatusColorTo(R.color.TemplateRed)
            }
        }
    }
}
