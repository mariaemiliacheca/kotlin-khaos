package com.kotlinkhaos.classes.services;

import android.util.Log
import com.kotlinkhaos.classes.errors.KotlinKhaosApiError
import com.kotlinkhaos.classes.errors.UserApiError
import com.kotlinkhaos.classes.errors.UserNetworkError
import com.kotlinkhaos.classes.errors.UserUploadS3Error
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.toByteReadChannel
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.nio.channels.UnresolvedAddressException

class KotlinKhaosUserApi {
    private val apiHost = "https://kotlin-khaos-api.maximoguk.com"
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
    }

    suspend fun getProfilePictureHash(
        token: String,
    ): UserProfilePictureHashRes {
        try {
            val res = client.get("$apiHost/user/profile-picture") {
                addRequiredApiHeaders(token)
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in getProfilePictureHash", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw UserNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun getPresignedProfilePictureUploadUrl(
        token: String,
        sha256Hash: String
    ): UserProfilePictureUploadUrlRes {
        try {
            val res = client.get("$apiHost/user/profile-picture/upload") {
                parameter("sha256", sha256Hash)
                addRequiredApiHeaders(token)
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in getPresignedProfilePictureUploadUrl", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw UserNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun uploadImageToS3(inputStream: InputStream, preSignedUrl: String) {
        try {
            val contentLength = withContext(Dispatchers.IO) {
                inputStream.available()
            }
            val response = client.put(preSignedUrl) {
                setBody(object : OutgoingContent.ReadChannelContent() {
                    override fun readFrom(): ByteReadChannel = inputStream.toByteReadChannel()
                    override val contentLength: Long =
                        contentLength.toLong()
                })
                header(HttpHeaders.ContentType, "image/jpeg")
            }

            if (!response.status.isSuccess()) {
                Log.e("UploadError", "Failed to upload")
                throw UserUploadS3Error("Failed to upload image: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e("UploadException", "Error during s3 upload", e)
            throw e
        } finally {
            client.close()
        }
    }

    private fun HttpRequestBuilder.addRequiredApiHeaders(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }

    private suspend inline fun <reified T> parseResponseFromApi(res: HttpResponse): T {
        if (res.status.isSuccess()) {
            return res.body()
        }
        val apiError: KotlinKhaosApiError = res.body()
        throw UserApiError(apiError.status, apiError.error)
    }
}

@Serializable
data class UserProfilePictureHashRes(val sha256: String? = null)

@Serializable
data class UserProfilePictureUploadUrlRes(val sha256: String, val uploadUrl: String)