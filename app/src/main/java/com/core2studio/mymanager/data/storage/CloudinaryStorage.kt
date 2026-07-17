package com.core2studio.mymanager.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class CloudinaryStorage(
    private val cloudName: String,
    private val uploadPreset: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(uri: Uri, context: Context, userId: String): Result<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(IllegalStateException("Cannot open image"))

                try {
                    tempFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                } finally {
                    inputStream.close()
                }

                val publicId = "users/$userId/${System.currentTimeMillis()}"

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .addFormDataPart("public_id", publicId)
                    .addFormDataPart(
                        "file",
                        "image.jpg",
                        tempFile.asRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.use { resp ->
                    val body = resp.body?.string()
                        ?: return@withContext Result.failure(IllegalStateException("Empty response from Cloudinary"))

                    if (resp.isSuccessful) {
                        val json = JSONObject(body)
                        val url = json.getString("secure_url")
                        Result.success(Pair(url, publicId))
                    } else {
                        Result.failure(RuntimeException("Cloudinary upload failed: ${resp.code} $body"))
                    }
                }
            } catch (e: Exception) {
                Log.e("CloudinaryStorage", "Upload error", e)
                Result.failure(e)
            } finally {
                tempFile.delete()
            }
        }
    }
}
