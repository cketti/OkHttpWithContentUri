package de.cketti.demo.okhttp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer

private const val REQUEST_UPLOAD_DOCUMENT = Activity.RESULT_FIRST_USER
private const val REQUEST_DOWNLOAD_DOCUMENT = Activity.RESULT_FIRST_USER + 1

class MainActivity : AppCompatActivity() {
    private val viewModel: MyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uploadButton = findViewById<View>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            selectDocumentForUpload()
        }

        val downloadButton = findViewById<View>(R.id.downloadButton)
        downloadButton.setOnClickListener {
            createDocumentForDownload()
        }

        val textView = findViewById<TextView>(R.id.textView)

        viewModel.uiState.observe(this, Observer { uiState ->
            uploadButton.isEnabled = uiState.enableButtons
            downloadButton.isEnabled = uiState.enableButtons
            textView.text = uiState.statusText
        })
    }

    private fun selectDocumentForUpload() {
        val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(openDocumentIntent, REQUEST_UPLOAD_DOCUMENT)
    }

    private fun createDocumentForDownload() {
        val openDocumentIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            type = "image/jpeg"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_TITLE, "kitten.jpeg")
        }

        startActivityForResult(openDocumentIntent, REQUEST_DOWNLOAD_DOCUMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_UPLOAD_DOCUMENT -> onDocumentOpened(resultCode, data)
            REQUEST_DOWNLOAD_DOCUMENT -> onDocumentCreated(resultCode, data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onDocumentOpened(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        data.data?.let { contentUri ->
            viewModel.uploadDocument(contentUri)
        }
    }

    private fun onDocumentCreated(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        data.data?.let { contentUri ->
            viewModel.downloadDocument(contentUri)
        }
    }
}
