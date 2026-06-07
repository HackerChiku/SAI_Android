package com.saicomputer.sms.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/** Result of preparing a file for upload: pure base64 (no data: prefix) + mime. */
data class PreparedUpload(
    val base64: String,
    val mimeType: String,
    val sizeBytes: Int
)

@Singleton
class ImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Downscale an image to <= [maxEdge]px on the longest edge, JPEG q=[quality],
     * encode to pure base64 (NO_WRAP, no data: prefix).
     */
    suspend fun compressImage(
        uri: Uri,
        maxEdge: Int = 1200,
        quality: Int = 85
    ): PreparedUpload = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val srcW = bounds.outWidth
        val srcH = bounds.outHeight
        require(srcW > 0 && srcH > 0) { "Could not read image" }

        val longest = max(srcW, srcH)
        var sample = 1
        while (longest / sample > maxEdge * 2) sample *= 2

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        } ?: throw IllegalStateException("Could not decode image")

        val scale = maxEdge.toFloat() / max(decoded.width, decoded.height).toFloat()
        val finalBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }

        val baos = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        val bytes = baos.toByteArray()

        if (finalBitmap !== decoded) finalBitmap.recycle()
        decoded.recycle()

        PreparedUpload(
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            mimeType = "image/jpeg",
            sizeBytes = bytes.size
        )
    }

    /** Read a file (e.g. a PDF) as-is and encode to base64 without compression. */
    suspend fun readAsBase64(uri: Uri): PreparedUpload = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read file")
        PreparedUpload(
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            mimeType = mime,
            sizeBytes = bytes.size
        )
    }

    /** Best-effort byte size of a content Uri (for pre-upload size limits). */
    suspend fun sizeOf(uri: Uri): Long = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
        }.getOrNull() ?: -1L
    }
}
