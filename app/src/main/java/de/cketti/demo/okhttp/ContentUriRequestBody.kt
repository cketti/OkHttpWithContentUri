package de.cketti.demo.okhttp

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.source

class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val contentUri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        val contentType = contentResolver.getType(contentUri) ?: return null
        return contentType.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        val inputStream = contentResolver.openInputStream(contentUri)
            ?: throw IOException("Couldn't open content URI for reading: $contentUri")

        inputStream.source().use { source ->
            sink.writeAll(source)
        }
    }

    override fun contentLength(): Long {
        val fileDescriptor = kotlin.runCatching {
            contentResolver.openAssetFileDescriptor(contentUri, "r")
        }.getOrNull() ?: return super.contentLength()

        val size = fileDescriptor.use { it.length }
        return if (size == AssetFileDescriptor.UNKNOWN_LENGTH) {
            super.contentLength()
        } else {
            size
        }
    }
}
