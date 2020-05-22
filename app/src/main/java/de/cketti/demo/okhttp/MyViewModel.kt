package de.cketti.demo.okhttp

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import okio.sink

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }
    private val uiLiveData = MutableLiveData<UiState>()

    val uiState: LiveData<UiState>
        get() = uiLiveData

    fun uploadDocument(contentUri: Uri) {
        uiLiveData.value = UiState(enableButtons = false, statusText = "Uploading…")

        viewModelScope.launch(Dispatchers.IO) {
            val contentResolver = getApplication<Application>().contentResolver
            val requestBody = ContentUriRequestBody(contentResolver, contentUri)

            val request = Request.Builder()
                .url("http://requestbin.net/r/14b8f721")
                .addHeader("User-Agent", "Fancy demo app")
                .post(requestBody)
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    val statusText = if (response.isSuccessful) {
                        "Upload was successful"
                    } else {
                        "Upload has failed (${response.code})"
                    }

                    uiLiveData.postValue(UiState(enableButtons = true, statusText = statusText))
                }
            } catch (e: IOException) {
                uiLiveData.postValue(
                    UiState(
                        enableButtons = true,
                        statusText = "Upload has failed with exception: ${e.message}"
                    )
                )
            }
        }
    }

    fun downloadDocument(contentUri: Uri) {
        uiLiveData.value = UiState(enableButtons = false, statusText = "Downloading…")

        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://placekitten.com/300/300")
                .addHeader("User-Agent", "Fancy demo app")
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->

                    uiLiveData.postValue(
                        UiState(
                            enableButtons = false,
                            statusText = "Saving response to document…"
                        )
                    )

                    if (response.isSuccessful) {
                        response.body!!.source().use { source ->
                            val contentResolver = getApplication<Application>().contentResolver
                            val outputStream = contentResolver.openOutputStream(contentUri)
                                ?: throw IOException("Couldn't open content URI for writing: $contentUri")

                            outputStream.sink().use { sink ->
                                source.readAll(sink)
                            }
                        }
                    }

                    val statusText = if (response.isSuccessful) {
                        "Download was successful"
                    } else {
                        "Download has failed (${response.code})"
                    }

                    uiLiveData.postValue(UiState(enableButtons = true, statusText = statusText))
                }
            } catch (e: IOException) {
                uiLiveData.postValue(
                    UiState(
                        enableButtons = true,
                        statusText = "Download has failed with exception: ${e.message}"
                    )
                )
            }
        }
    }
}

data class UiState(val enableButtons: Boolean, val statusText: String?)
